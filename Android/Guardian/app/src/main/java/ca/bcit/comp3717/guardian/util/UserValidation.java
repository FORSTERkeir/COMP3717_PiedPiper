package ca.bcit.comp3717.guardian.util;

import android.util.Log;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class UserValidation {
    private static String TAG = UserValidation.class.getSimpleName();

    private UserValidation() {}

    public static boolean validateInputs(EditText userName, EditText password) {
        if (userName == null || userName.getText().toString().length() == 0) {
            Log.e(TAG, "Username must not be blank");
            return false;
        }

        if (password == null || password.getText().toString().length() == 0) {
            Log.e(TAG, "Password must not be blank");
            return false;
        }
        return true;
    }

    public static boolean validateInputs(EditText userName, EditText email, EditText phone, EditText password) {
        if (userName == null || userName.getText().toString().length() == 0) {
            Log.e(TAG, "Username must not be blank");
            return false;
        }

        if (password == null || password.getText().toString().length() == 0) {
            Log.e(TAG, "Password must not be blank");
            return false;
        }

        if (email == null || email.getText().toString().length() == 0) {
            Log.e(TAG, "email must not be blank");
            return false;
        }

        if (phone == null || phone.getText().toString().length() == 0) {
            Log.e(TAG, "phone must not be blank");
            return false;
        }
        return true;
    }

    public static boolean createUserAccountValidation(String jsonResponse) {
        try {
            JSONObject obj = new JSONObject(jsonResponse);
            if (obj.getString("Message").equalsIgnoreCase("The request is processed.")) {
                return true;
            }

        } catch (JSONException e) {
            Log.e(TAG, "ERROR in userCreateAccountValidation(): " + e.getMessage());
            return false;
        }
        return false;
    }
}
