package ca.bcit.comp3717.guardian.controller;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import ca.bcit.comp3717.guardian.R;
import ca.bcit.comp3717.guardian.api.HttpHandler;
import ca.bcit.comp3717.guardian.database.DatabaseHelper;
import ca.bcit.comp3717.guardian.database.QueryGenerator;
import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.DialogBuilder;
import ca.bcit.comp3717.guardian.util.UserValidation;

public class LandingActivity extends AppCompatActivity {

    private String TAG = LandingActivity.class.getSimpleName();
    private AlertDialog registerDialog;
    private AlertDialog alertDialog;
    private Dialog loadingDialog;
    private SQLiteDatabase db;

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
        if (!getIntent().getBooleanExtra("logout", false)) {
            new GetLocalLoginValuesTask().execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (db != null) {
            db.close();
        }
    }

    private void getDatabaseInstance() {
        try {
            // create database (if not exists) and get instance
            db = new DatabaseHelper(LandingActivity.this).getWritableDatabase();
        } catch (Exception e) {
            Log.e(TAG, "ERROR in getDatabaseInstance(): " + e.getMessage());
        }
    }

    public void displayRegisterUserDialog(View view){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(LandingActivity.this);
        final View dialogView = LayoutInflater.from(LandingActivity.this).inflate(R.layout.dialog_register_user, null);

        Button btnRegister = dialogView.findViewById(R.id.button_dialogRegisterUser_register);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Guardians.ttf");

        btnRegister.setTypeface(custom_font);

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
            registerDialog.dismiss();
            new LoginUserTask(user.getEmail(), user.getPassword()).execute();
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
            getDatabaseInstance();
            DatabaseHelper.updateDbValues(db, QueryGenerator.SAVED_LOGIN_TABLE, getContentValues(),
                    QueryGenerator.UpdateQuery.SavedLoginWhereClause);
            return HttpHandler.UserController.loginByEmail(this.email, this.password);
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            loginUserResponse(user);
            loadingDialog.dismiss();
        }

        private ContentValues getContentValues() {
            ContentValues values = new ContentValues();
            values.put("Email", this.email);
            values.put("Password", this.password);
            return values;
        }
    }

    private class RegisterUserTask extends AsyncTask<Void, Void, User> {
        private String userName;
        private String password;
        private String email;
        private String phone;

        public RegisterUserTask(String userName, String password, String email, String phone) {
            this.userName = userName;
            this.password = password; // the actual password
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
            user.setPassword(this.password); // update null password from api call
            registerUserResponse(user);
            loadingDialog.dismiss();
        }
    }

    private class GetUserByEmailTask extends AsyncTask<Void, Void, User> {
        private String email;
        private String password; // the actual password

        public GetUserByEmailTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected User doInBackground(Void... params) {
            return HttpHandler.UserController.getUserByEmail(this.email, this.password, this.email);
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null && user.isLogin()) {
                user.setPassword(this.password); // update null password from api call
                goToMainActivity(user);
            }
            super.onPostExecute(user);
        }
    }

    private class GetLocalLoginValuesTask extends AsyncTask<Void, Void, User> {
        public GetLocalLoginValuesTask() {}

        @Override
        protected User doInBackground(Void... args) {
            getDatabaseInstance();
            return DatabaseHelper.getLocalLoginValues(db, QueryGenerator.SelectQuery.LastLoggedInUser);
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            if (user != null) {
                new GetUserByEmailTask(user.getEmail(), user.getPassword()).execute();
            }
        }
    }
}
