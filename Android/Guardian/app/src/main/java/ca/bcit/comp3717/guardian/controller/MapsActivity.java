package ca.bcit.comp3717.guardian.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.bcit.comp3717.guardian.HttpHandler;
import ca.bcit.comp3717.guardian.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String TAG = MapsActivity.class.getSimpleName();
    ArrayList<EmergencyBuilding> countryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        countryList = new ArrayList<>();
        new GetContacts().execute();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void back (View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng newWest = new LatLng(49.2057, -122.9110);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newWest));
        mMap.setMinZoomPreference(12);
        for (int i = 0; i < countryList.size(); i++) {
            EmergencyBuilding item = countryList.get(i);
            LatLng latLng = new LatLng(Float.parseFloat(item.getLatitutde()), Float.parseFloat(item.getLongitude()));
            mMap.addMarker(new MarkerOptions().position(latLng).title(item.getBldgName()));

        }
    }
    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String SERVICE_URL = "http://guardiannewwestapi.azurewebsites.net/emergencybldg/get/all/";
            String jsonStr = sh.makeServiceCall(SERVICE_URL);
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
                        // tmp hash map for single contact
                        final EmergencyBuilding countryInfo = new EmergencyBuilding();

                        // adding each child node to HashMap key => value
                        countryInfo.setBldgName(BldgName);
                        countryInfo.setLatitutde(Lat);
                        countryInfo.setLongitude(Lng);
                        countryList.add(countryInfo);
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
        }
    }
}
