package ca.bcit.comp3717.guardian.util;

import android.util.Log;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class UserValidation {
    private static String TAG = UserValidation.class.getSimpleName();

    private UserValidation() {}

    public static boolean validateLoginInputEmail(EditText email) {
        if (email == null || email.getText().toString().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean validateLoginInputPassword(EditText password) {
        if (password == null || password.getText().toString().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean validateRegisterInputUsername(EditText username) {
        if (username == null || username.getText().toString().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean validateRegisterInputPhone(EditText phone) {
        if (phone == null || phone.getText().toString().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean validateRegisterInputEmail(EditText email) {
        if (email == null || email.getText().toString().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean validateRegisterInputPassword(EditText password) {
        if (password == null || password.getText().toString().length() == 0) {
            return false;
        }
        return true;
    }

    public static boolean validateAddLinkedUserInputEmail(EditText email) {
        if (email == null || email.getText().toString().length() == 0) {
            return false;
        }
        return true;
    }

    public static String constructInvalidLoginUserInputMessage(boolean validEmail,
                                                               boolean validPassword) {
        String msg;
        if (!validEmail && !validPassword) {
            msg = "Email and password must not be blank";

        } else if (!validEmail) {
            msg = "Email must not be blank";

        } else {
            msg = "Password must not be blank";
        }
        return msg;
    }

    public static String constructInvalidRegisterUserInputMessage(boolean validUsername,
                                                                  boolean validPhone,
                                                                  boolean validEmail,
                                                                  boolean validPassword) {
        String msg;
        if (!validUsername && !validPhone && !validEmail && !validPassword) {
            msg = "All fields must not be blank.";

        } else if (!validUsername && validPhone && validEmail && validPassword) {
            msg = "Username must not be blank.";

        } else if (validUsername && !validPhone && validEmail && validPassword) {
            msg = "Phone must not be blank.";

        } else if (validUsername && validPhone && !validEmail && validPassword) {
            msg = "Email must not be blank.";

        } else if (validUsername && validPhone && validEmail && !validPassword) {
            msg = "Password must not be blank.";

        } else if (!validUsername && !validPhone && validEmail && validPassword) {
            msg = "Username and phone must not be blank.";

        } else if (!validUsername && validPhone && !validEmail && validPassword) {
            msg = "Username and email must not be blank.";

        } else if (!validUsername && validPhone && validEmail && !validPassword) {
            msg = "Username and password must not be blank.";

        } else if (validUsername && !validPhone && !validEmail && validPassword) {
            msg = "Phone and email must not be blank.";

        } else if (validUsername && !validPhone && validEmail && !validPassword) {
            msg = "Phone and password must not be blank.";

        } else if (validUsername && validPhone && !validEmail && !validPassword) {
            msg = "Email and password must not be blank.";

        } else if (validUsername && !validPhone && !validEmail && !validPassword) {
            msg = "Phone, Email, and password must not be blank.";

        } else if (!validUsername && validPhone && !validEmail && !validPassword) {
            msg = "Username, Email, and password must not be blank.";

        } else if (!validUsername && !validPhone && validEmail && !validPassword) {
            msg = "Username, Phone, and password must not be blank.";

        } else {
            msg = "Username, Phone, and email must not be blank.";
        }
        return msg;
    }

    public static boolean validateCreateUserAccountResponse(String jsonResponse) {
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

    public static boolean validateUserLoginResponse(String jsonResponse) {
        if (jsonResponse.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                String message = jsonObj.getString("Message");

                if (message.equals("The request is invalid.")) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean validateUserLogoutResponse(String jsonResponse) {
        if (jsonResponse.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                String message = jsonObj.getString("Message");

                if (message.equals("The request is invalid.")) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean validateAddLinkedUserResponse(String jsonResponse) {
        if (jsonResponse.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                String message = jsonObj.getString("Message");

                if (message.equals("The request is invalid.")) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean validateRemoveLinkedUserResponse(String jsonResponse) {
        if (jsonResponse.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                String message = jsonObj.getString("Message");

                if (message.equals("The request is invalid.")) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean validateSetLinkedUserAlertResponse(String jsonResponse) {
        if (jsonResponse.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                String message = jsonObj.getString("Message");

                if (message.equals("The request is invalid.")) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean validateSetLinkedUserMuteResponse(String jsonResponse) {
        if (jsonResponse.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                String message = jsonObj.getString("Message");

                if (message.equals("The request is invalid.")) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean validateGetUserByEmailResponse(String jsonResponse) {
        if (jsonResponse.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                String message = jsonObj.getString("Message");

                if (message.equals("The request is invalid.") ||
                        message.equals("No user is found.")) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean validateTokenRefresh(String jsonResponse) {
        if (jsonResponse.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(jsonResponse);
                String message = jsonObj.getString("Message");

                if (message.equals("The request is invalid.")) {
                    return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
