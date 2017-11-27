package ca.bcit.comp3717.guardian.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.api.HttpHandler;
import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.DialogBuilder;
import ca.bcit.comp3717.guardian.util.UserValidation;

public class LandingActivity extends AppCompatActivity {

    private String TAG = LandingActivity.class.getSimpleName();
    private AlertDialog registerDialog;
    private AlertDialog alertDialog;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/Guardians.ttf");
        TextView tx = (TextView)findViewById(R.id.guardianText);
        tx.setTypeface(custom_font);
        tx = (TextView)findViewById(R.id.button_landingActivity_login);
        tx.setTypeface(custom_font);
        loadingDialog = DialogBuilder.constructLoadingDialog(LandingActivity.this,
                R.layout.dialog_loading);
    }

    public void displayRegisterUserDialog(View view){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(LandingActivity.this);
        final View dialogView = LayoutInflater.from(LandingActivity.this).inflate(R.layout.dialog_register_user, null);

        Button btnRegister = dialogView.findViewById(R.id.button_dialogRegisterUser_register);
        mBuilder.setView(dialogView);

        registerDialog = mBuilder.create();
        registerDialog.show();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUserRequest(registerDialog);
            }
        });
    }

    private void registerUserRequest(Dialog d) {
        EditText userName = d.findViewById(R.id.editText_dialogRegisterUser_userName);
        EditText phone = d.findViewById(R.id.editText_dialogRegisterUser_phone);
        EditText email = d.findViewById(R.id.editText_dialogRegisterUser_email);
        EditText password = d.findViewById(R.id.editText_dialogRegisterUser_password);
        boolean validUsername = UserValidation.validateRegisterInputUsername(userName);
        boolean validPhone = UserValidation.validateRegisterInputPhone(phone);
        boolean validEmail = UserValidation.validateRegisterInputEmail(email);
        boolean validPassword = UserValidation.validateRegisterInputPassword(password);

        if (validUsername && validPhone && validEmail && validPassword) {
            new RegisterUserTask(userName.getText().toString(), password.getText().toString(),
                    email.getText().toString(), phone.getText().toString()).execute();
        } else {
            String msg = UserValidation.constructInvalidRegisterUserInputMessage(validUsername,
                    validPhone, validEmail, validPassword);
            alertDialog = DialogBuilder.constructAlertDialog(LandingActivity.this,
                    "Invalid Fields", msg);
            alertDialog.show();
            Log.e(TAG, "Invalid Fields. " + msg);
        }
    }

    private void registerUserResponse(User user) {
        if (user == null) {
            alertDialog = DialogBuilder.constructAlertDialog(LandingActivity.this,
                    "Invalid Username", "Username ia already taken.");
            Log.e(TAG, "Invalid Username. Username ia already taken.");

        } else {
            new LoginUserTask(user.getEmail(), user.getPassword()).execute();
            registerDialog.dismiss();
        }
    }

    public void loginUserRequest(View v) {
        EditText email = (EditText) findViewById(R.id.editText_landingActivity_email);
        EditText password = (EditText) findViewById(R.id.editText_landingActivity_password);
        boolean validEmail = UserValidation.validateLoginInputEmail(email);
        boolean validPassword = UserValidation.validateLoginInputPassword(password);

        if (validEmail && validPassword) {
            new LoginUserTask(email.getText().toString(), password.getText().toString()).execute();

        } else {
            String msg = UserValidation.constructInvalidLoginUserInputMessage(validEmail, validPassword);
            alertDialog = DialogBuilder.constructAlertDialog(LandingActivity.this,
                    "Invalid Login", msg);
            alertDialog.show();
            Log.e(TAG, "Invalid login. " + msg);
        }
    }

    private void loginUserResponse(User user) {
        if (user == null) {
            alertDialog = DialogBuilder.constructAlertDialog(LandingActivity.this,
                    "Invalid Login", "Email or password is incorrect");
            alertDialog.show();
            Log.e(TAG, "Invalid login. Email or password is incorrect or account does not exist");

        } else {
            goToMainActivity(user);
        }
    }

    private void goToMainActivity(User user) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("userId", user.getId());
        i.putExtra("userName", user.getUserName());
        i.putExtra("password", user.getPassword());
        i.putExtra("email", user.getEmail());
        i.putExtra("phoneNumber", user.getPhone());
        Toast.makeText(this.getBaseContext(), user.getUserName() + " Logged in", Toast.LENGTH_SHORT).show();
        startActivity(i);
    }


    private class LoginUserTask extends AsyncTask<Void, Void, User> {
        private String password;
        private String email;

        public LoginUserTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected User doInBackground(Void... voidArgs) {
            return HttpHandler.UserController.loginByEmail(this.email, this.password);
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            loginUserResponse(user);
            loadingDialog.dismiss();
        }
    }

    private class RegisterUserTask extends AsyncTask<Void, Void, User> {
        private String userName;
        private String password;
        private String email;
        private String phone;

        public RegisterUserTask(String userName, String password, String email, String phone) {
            this.userName = userName;
            this.password = password;
            this.email = email;
            this.phone = phone;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected User doInBackground(Void... voidArgs) {
            return HttpHandler.UserController.createUser(this.userName, this.password, this.email, this.phone);
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            registerUserResponse(user);
            loadingDialog.dismiss();
        }
    }
}
