package ca.bcit.comp3717.guardian;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void alert (View view) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(MainActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.alert_layout);
        // Set dialog title
        dialog.setTitle("Alert");

        dialog.show();
    }

    public void map (View view) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }

    public void linkAcc (View view) {
        Intent i = new Intent(this, LinkedAccountActivity.class);
        startActivity(i);
    }

    public void userAcc (View view) {
        Intent i = new Intent(this, UserAccountActivity.class);
        startActivity(i);
    }

    public void logout (View view) {
        Intent i = new Intent(this, LandingActivity.class);
        startActivity(i);
    }

}
