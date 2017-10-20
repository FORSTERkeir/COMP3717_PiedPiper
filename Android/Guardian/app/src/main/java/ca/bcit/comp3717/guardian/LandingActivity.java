package ca.bcit.comp3717.guardian;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
    }

    public void register(View view){
        // Create custom dialog object
        final Dialog dialog = new Dialog(LandingActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.register_layout);
        // Set dialog title
        dialog.setTitle("Register");

        dialog.show();
    }

    public void login (View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
