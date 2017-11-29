package ca.bcit.comp3717.guardian.controller;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import android.content.Context;

import java.util.HashSet;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.api.HttpHandler;
import ca.bcit.comp3717.guardian.api.NotificationHandler;
import ca.bcit.comp3717.guardian.api.NotificationSettings;
import ca.bcit.comp3717.guardian.api.RegisterClient;
import ca.bcit.comp3717.guardian.api.RegistrationIntentService;
import ca.bcit.comp3717.guardian.model.EmergencyBuilding;
import ca.bcit.comp3717.guardian.model.LinkedUser;
import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.DialogBuilder;
import ca.bcit.comp3717.guardian.util.UserBuilder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.microsoft.windowsazure.notifications.NotificationsManager;

public class MainActivity extends Activity {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 3;

    private FusedLocationProviderClient mFusedLocationClient;
    private String TAG = MapsActivity.class.getSimpleName();
    ArrayList<EmergencyBuilding> locationList;
    private User user;
    private Dialog loadingDialog;
    private List<LinkedUser> linkedUsersDisplayList;
    boolean alertNotification = false;
    User targetUser;

    // firebase ------------------------------------------------------------------------------------
    public static MainActivity mainActivity;
    public static Boolean isVisible = false;
    private static final String TAG_MAIN = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // ver2
    //private NotificationHub hub;
    private RegisterClient registerClient;
    private static final String BACKEND_ENDPOINT = "http://guardiannewwestapi.azurewebsites.net";
    private GoogleCloudMessaging gcm;
    // firebase ------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            user = UserBuilder.constructUserFromIntent(getIntent());
            loadingDialog = DialogBuilder.constructLoadingDialog(MainActivity.this, R.layout.dialog_loading);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationList = new ArrayList<>();
        linkedUsersDisplayList = new ArrayList<>();

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Guardians.ttf");

        Button tx = (Button) findViewById(R.id.mapBtn);
        tx.setTypeface(custom_font);
        tx = (Button) findViewById(R.id.linkAccBtn);
        tx.setTypeface(custom_font);
        tx = (Button) findViewById(R.id.userAccBtn);
        tx.setTypeface(custom_font);
        tx = (Button) findViewById(R.id.logoutBtn);
        tx.setTypeface(custom_font);


        // firebase --------------------------------------------------------------------------------
        mainActivity = this;
        NotificationsManager.handleNotifications(this, NotificationSettings.SenderId, NotificationHandler.class);
        //registerWithNotificationHubs();
        gcm = GoogleCloudMessaging.getInstance(this);
        registerClient = new RegisterClient(this, BACKEND_ENDPOINT);
        try {
            login();
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.toString());
        }
        // firebase --------------------------------------------------------------------------------
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("userId", user.getId());
        outState.putString("userName", user.getUserName());
        outState.putString("email", user.getEmail());
        outState.putString("password", user.getPassword());
        outState.putString("phoneNumber", user.getPhone());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        user = new User();
        user.setId(savedInstanceState.getInt("userId"));
        user.setUserName(savedInstanceState.getString("userName"));
        user.setEmail(savedInstanceState.getString("email"));
        user.setPassword(savedInstanceState.getString("password"));
        user.setPhone(savedInstanceState.getString("phoneNumber"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true; // firebase
    }

    @Override
    protected void onPause() {
        super.onPause();
        isVisible = false; // firebase
    }

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true; // firebase
    }

    @Override
    protected void onStop() {
        super.onStop();
        isVisible = false; // firebase
    }

    public void alert(View view) {
        new GetLocations().execute();
    }

    public void map(View view) {
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("userId", user.getId());
        i.putExtra("userName", user.getUserName());
        i.putExtra("email", user.getEmail());
        i.putExtra("password", user.getPassword());
        i.putExtra("phoneNumber", user.getPhone());

        startActivity(i);
    }

    public void userAcc(View view) {
        Intent i = new Intent(this, UserAccountActivity.class);
        i.putExtra("userId", user.getId());
        i.putExtra("userName", user.getUserName());
        i.putExtra("email", user.getEmail());
        i.putExtra("password", user.getPassword());
        i.putExtra("phoneNumber", user.getPhone());
        startActivity(i);
    }

    public void goToLandingActivity() {
        Intent i = new Intent(this, LandingActivity.class);
        i.putExtra("logout", true);
        startActivity(i);
    }
    private void showAlertDialog() {

        final long numbers[] = new long[4];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            if (alertNotification == false) {
                                alertNotification = true;
                                HttpHandler.UserController.setConnAlertProperties(user.getEmail(), user.getPassword(), user.getId(), location.getLatitude(), location.getLongitude());
                            }
                            else {
                                alertNotification = false;
                                HttpHandler.UserController.setConnUnalertProperties(user.getEmail(), user.getPassword(), user.getId());
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Location null", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        for (int i = 0; i < locationList.size(); i++) {
            EmergencyBuilding item = locationList.get(i);
            if (numbers[0] == 0 && item.getCategory() == 2) {
                numbers[0] = item.getPhone();
            }
            if (numbers[1] == 0 && item.getCategory() == 3) {
                numbers[1] = item.getPhone();
            }
            if (numbers[2] == 0 && item.getCategory() == 4) {
                numbers[2] = item.getPhone();
            }
        }
        if (linkedUsersDisplayList.size() > 0) {
            numbers[3] = Long.parseLong(targetUser.getPhone());
        }

        // Create custom dialog object
        final Dialog dialog = new Dialog(MainActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.alert_layout);
        // Set dialog title
        dialog.setTitle("Alert");
        Button fire = (Button) dialog.findViewById(R.id.fireBtn);
        fire.setText("" + numbers[0]);
        Button hospital = (Button) dialog.findViewById(R.id.hospitalBtn);
        hospital.setText("" + numbers[1]);
        Button police = (Button) dialog.findViewById(R.id.policeBtn);
        police.setText("" + numbers[2]);
        Button guardian = (Button) dialog.findViewById(R.id.guardianBtn);
        guardian.setText("" + numbers[3]);

        dialog.show();

    }

    public void callPolice(View v) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        Button btn = (Button) v.findViewById(R.id.policeBtn);

        callIntent.setData(Uri.parse("tel:" + btn.getText()));
        startActivity(callIntent);
    }

    public void callGuardian(View v) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        Button btn = (Button) v.findViewById(R.id.guardianBtn);

        callIntent.setData(Uri.parse("tel:" + btn.getText()));
        startActivity(callIntent);
    }

    public void linkAcc(View view) {
        Intent i = new Intent(this, LinkedAccountActivity.class);
        i.putExtra("userId", user.getId());
        i.putExtra("userName", user.getUserName());
        i.putExtra("email", user.getEmail());
        i.putExtra("password", user.getPassword());
        i.putExtra("phoneNumber", user.getPhone());
        startActivity(i);
    }

    public void callHospital(View v) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        Button btn = (Button) v.findViewById(R.id.hospitalBtn);

        callIntent.setData(Uri.parse("tel:" + btn.getText()));
        startActivity(callIntent);
    }

    public void callFire(View v) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        Button btn = (Button) v.findViewById(R.id.fireBtn);

        callIntent.setData(Uri.parse("tel:" + btn.getText()));
        startActivity(callIntent);
    }

    public void logout(View view) {
        new LogoutUserTask().execute();
    }

    public void callEmergency(View v) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        Button btn = (Button) v.findViewById(R.id.emerg);

        callIntent.setData(Uri.parse("tel:" + btn.getText()));
        startActivity(callIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(this, "Can not show your location.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(this, "Can not show your location.", Toast.LENGTH_SHORT).show();
                }
            }
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    Toast.makeText(this, "Can not make phone calls.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetLocations extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String SERVICE_URL = "http://guardiannewwestapi.azurewebsites.net/emergencybldg/get/all/";
            String jsonStr = sh.makeServiceCall(SERVICE_URL, user.getEmail(), user.getPassword());
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    // Getting JSON Array node
                    JSONObject emergBldgJSONArray = new JSONObject(jsonStr);
                    // looping through All Contacts
                    String building = emergBldgJSONArray.getString("buildings");
                    JSONArray buildingJSONArray = new JSONArray(building);

                    // looping through All Contacts
                    for (int i = 0; i < buildingJSONArray.length(); i++) {
                        JSONObject c = buildingJSONArray.getJSONObject(i);
                        String BldgName = c.getString("BldgName");
                        String Lat = c.getString("Lat");
                        String Lng = c.getString("Lng");
                        String category = c.getString("Category");
                        Long phone = c.getLong("Phone");
                        // tmp hash map for single contact
                        final EmergencyBuilding emergencyBldg = new EmergencyBuilding();

                        // adding each child node to HashMap key => value
                        emergencyBldg.setBldgName(BldgName);
                        emergencyBldg.setLatitutde(Lat);
                        emergencyBldg.setLongitude(Lng);
                        emergencyBldg.setCategory(Integer.parseInt(category));
                        emergencyBldg.setPhone(phone);
                        locationList.add(emergencyBldg);
                    }
                    List<LinkedUser> luList = HttpHandler.LinkedUserController.getLinkedUsersById(user.getEmail(), user.getPassword(), user.getId());

                    for (LinkedUser lu : luList) {
                        if (lu.isAddedMe() && lu.isAddedTarget() && !lu.isDeleted()) {
                            linkedUsersDisplayList.add(lu);
                        }
                    }
                    if (linkedUsersDisplayList.size() > 0) {
                        LinkedUser lu = linkedUsersDisplayList.get(0);
                        int userId = lu.getUserIdTarget();

                        targetUser = HttpHandler.UserController.getUserById(user.getEmail(), user.getPassword(), userId);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            showAlertDialog();
        }
    }

    private class LogoutUserTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            loadingDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voidArgs) {
            HttpHandler.UserController.logoutByEmail(user.getEmail(), user.getPassword());
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            super.onPostExecute(args);
            loadingDialog.dismiss();
            goToLandingActivity();
        }
    }

    // firebase

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
                Log.i(TAG_MAIN, "This device is supported by Google Play Services.");
                ToastNotify("This device is supported by Google Play Services.");
            } else {
                Log.i(TAG_MAIN, "This device is not supported by Google Play Services.");
                ToastNotify("This device is not supported by Google Play Services.");
                finish();
            }
            return false;
        }
        return true;
    }

    // firebase
    public void registerWithNotificationHubs() {
        if (checkPlayServices()) {
            // Start IntentService to displayRegisterUserDialog this application with FCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    // firebase
    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // firebase
    public void login() throws UnsupportedEncodingException {
        this.registerClient.setAuthorizationHeader(getAuthorizationHeader());

        final Context context = this;
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    String regid = gcm.register(NotificationSettings.SenderId);
                    registerClient.register(regid, new HashSet<String>());
                } catch (Exception e) {
                    Log.d(TAG, "Failed to register: " + e.getMessage());
                    return e;
                }
                return null;
            }

            protected void onPostExecute(Object result) {
                Toast.makeText(context, "Logged in and registered.",
                        Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

    private String getAuthorizationHeader() throws UnsupportedEncodingException {
        String username = user.getEmail();
        String password = user.getPassword();
        String basicAuthHeader = username + ":" + password;
        basicAuthHeader = Base64.encodeToString(basicAuthHeader.getBytes("UTF-8"), Base64.NO_WRAP);
        return basicAuthHeader;
    }
}

