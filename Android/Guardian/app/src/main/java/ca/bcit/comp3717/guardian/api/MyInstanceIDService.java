package ca.bcit.comp3717.guardian.api;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.UserBuilder;
import ca.bcit.comp3717.guardian.controller.MainActivity;

public class MyInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyInstanceIDService";
    private User user;

    @Override
    public void onTokenRefresh() {
        //Log.d(TAG, "Refreshing GCM Registration Token");
        //Intent intent = new Intent(this, RegistrationIntentService.class);
        //startService(intent);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }

    public void sendRegistrationToServer(String token) {
        user = UserBuilder.constructUserFromIntent(MainActivity.mainActivity.getIntent());
        new RefreshTokenTask().execute(token);
    }

    private class RefreshTokenTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strArgs) {
            HttpHandler.UserController.refreshToken(user.getEmail(), user.getPassword(), user.getId(), strArgs[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            super.onPostExecute(args);
        }
    }
}