package ca.bcit.comp3717.guardian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class UserAccountActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
    }

    public void back (View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

}
