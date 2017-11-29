package ca.bcit.comp3717.guardian.controller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.model.User;

public class UserAccountActivity extends Activity {
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        Intent i = getIntent();

        user = new User();
        user.setId(i.getIntExtra("userId", -1));
        user.setUserName(i.getStringExtra("userName"));
        user.setEmail(i.getStringExtra("email"));
        user.setPassword(i.getStringExtra("password"));
        user.setPhone(i.getStringExtra("phoneNumber"));

        TextView tx = (TextView) findViewById(R.id.usernameInput);
        tx.setText(user.getUserName());
        tx = (TextView) findViewById(R.id.emailInput);
        tx.setText(user.getEmail());
        tx = (TextView) findViewById(R.id.phoneInput);
        tx.setText(user.getPhone());
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Guardians.ttf");

        tx = (Button) findViewById(R.id.backBtn);
        tx.setTypeface(custom_font);
        tx = (TextView) findViewById(R.id.usernameText);
        tx.setTypeface(custom_font);
        tx = (TextView) findViewById(R.id.phoneText);
        tx.setTypeface(custom_font);
        tx = (TextView) findViewById(R.id.emailText);
        tx.setTypeface(custom_font);

    }

    public void back(View view) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("userId", user.getId());
        i.putExtra("userName", user.getUserName());
        i.putExtra("password", user.getPassword());
        i.putExtra("email", user.getEmail());
        i.putExtra("phoneNumber", user.getPhone());
        startActivity(i);
    }
}
