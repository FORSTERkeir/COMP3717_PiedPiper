package ca.bcit.comp3717.guardian.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.model.User;

public class MainActivity extends Activity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        user = new User();
        user.setUserName(i.getStringExtra("userName"));
        user.setEmail(i.getStringExtra("email"));
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
        Toast.makeText(this.getBaseContext(), "Goodbye " + user.getUserName(), Toast.LENGTH_SHORT).show();
        startActivity(i);
    }

}
