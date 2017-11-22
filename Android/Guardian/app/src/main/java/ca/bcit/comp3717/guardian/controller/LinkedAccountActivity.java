package ca.bcit.comp3717.guardian.controller;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.controller.MainActivity;
import ca.bcit.comp3717.guardian.util.UserValidation;

public class LinkedAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linked_account);
    }

    public void back (View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    public void addLinkedUser (View view) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(LinkedAccountActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.add_linked_user);
        // Set dialog title
        dialog.setTitle("Add User");

        dialog.show();
        Button addUser = (Button) dialog.findViewById(R.id.sendRequestBtn);
        addUser.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                addUser(dialog);
            }
        });

    }

    private void addUser(Dialog d) {
        EditText targetUserName = (EditText) d.findViewById(R.id.targetUsername);
        Intent i = getIntent();
        String currentUserId = i.getStringExtra("Userid");
    }

}
