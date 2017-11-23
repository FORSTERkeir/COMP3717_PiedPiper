package ca.bcit.comp3717.guardian.api;

import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import ca.bcit.comp3717.guardian.model.User;
import ca.bcit.comp3717.guardian.util.UserValidation;

public class HttpHandler {
    private static String TAG = HttpHandler.class.getSimpleName();
    private static final String createUserURL = "http://guardiannewwestapi.azurewebsites.net/user/create";
    private static final String getUserURL = "http://guardiannewwestapi.azurewebsites.net/user/get";
    private static final String deleteUserURL = "http://guardiannewwestapi.azurewebsites.net/user/delete";
    private static final String userLoginURL = "http://guardiannewwestapi.azurewebsites.net/user/login";
    private static final String userLogoutURL = "http://guardiannewwestapi.azurewebsites.net/user/logout";

    public HttpHandler() {}

    public static User createUser(String userName, String password, String email, String phone) {
        User user = null;

        try {
            HttpURLConnection conn = openConnection(createUserURL);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("userName", userName);
            conn.setRequestProperty("password", password);
            conn.setRequestProperty("email", email);
            conn.setRequestProperty("phone", phone);
            conn.setRequestMethod("POST");

            if (conn.getResponseCode() != 200) {
                Log.e(TAG, conn.getResponseCode() + "");

            } else {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = HttpHandler.convertStreamToString(in);

                if (UserValidation.createUserAccountValidation(response)) {
                    user = new User();
                    user.setUserName(userName);
                    user.setEmail(email);
                }
            }

        } catch (ProtocolException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static User getUserByEmail(String email, String password) {
        User user = null;

        try {
            HttpURLConnection conn = openConnection(getUserURL);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", getB64Auth(email, password));
            conn.setRequestProperty("email", email);
            conn.setRequestMethod("POST");

            if (conn.getResponseCode() != 200) {
                Log.e(TAG, "getUser() response code: " + conn.getResponseCode());

            } else {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = HttpHandler.convertStreamToString(in);
                user = HttpHandler.convertStringToUser(response);
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in getUser(): " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in getUser(): " + e.getMessage());
        }
        return user;
    }

    public static User userLogin(String email, String password) {
        User user = null;

        try {
            HttpURLConnection conn = openConnection(userLoginURL);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", getB64Auth(email, password));
            conn.setRequestProperty("email", email);
            conn.setRequestMethod("POST");

            if (conn.getResponseCode() != 200) {
                Log.e(TAG, "getUser() response code: " + conn.getResponseCode());

            } else {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = HttpHandler.convertStreamToString(in);
                user = HttpHandler.convertStringToUser(response);
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in userLogin(): " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in userLogin(): " + e.getMessage());
        }
        return user;
    }

    public static User userLogout(String email, String password) {
        User user = null;

        try {
            HttpURLConnection conn = openConnection(userLogoutURL);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", getB64Auth(email, password));
            conn.setRequestProperty("email", email);
            conn.setRequestMethod("POST");

            if (conn.getResponseCode() != 200) {
                Log.e(TAG, "getUser() response code: " + conn.getResponseCode());

            } else {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                String response = HttpHandler.convertStreamToString(in);
                user = HttpHandler.convertStringToUser(response);
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in userLogout(): " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in userLogout(): " + e.getMessage());
        }
        return user;
    }

    public static String deleteUser(String email, String password) {
        String response = "";

        try {
            HttpURLConnection conn = openConnection(deleteUserURL);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", getB64Auth(email, password));
            conn.setRequestProperty("email", email);
            conn.setRequestMethod("POST");

            if (conn.getResponseCode() != 200) {
                Log.e(TAG, conn.getResponseCode() + "");
            }

            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = HttpHandler.convertStreamToString(in);

        } catch (ProtocolException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in deleteUser(): " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in deleteUser(): " + e.getMessage());
        }
        return response;
    }

    private static HttpURLConnection openConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return conn;
    }

    private static String getB64Auth(String login, String pass) {
        String source = login + ":" + pass;
        String formatted = "Basic " + Base64.encodeToString(source.getBytes(),
                Base64.URL_SAFE | Base64.NO_WRAP);
        return formatted;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static User convertStringToUser(String response) {
        User user = null;

        try {
            JSONObject obj = new JSONObject(response);
            JSONObject userObj = obj.getJSONObject("user");
            user = new User();

            user.setId(userObj.getInt("ID"));
            user.setUserName(userObj.getString("UserName"));
            user.setEmail(userObj.getString("Email"));
            user.setPassword(userObj.getString("Password"));
            user.setPhone(userObj.getString("Phone"));
            user.setLogin(userObj.getBoolean("Login"));
            user.setLastLogin(userObj.getString("LastLogin"));
            user.setStatus(userObj.getInt("Status"));
            user.setDeleted(userObj.getBoolean("Deleted"));

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR in convertStringToUser(): " + e.getMessage());
            return null;
        }
        return user;
    }
    public String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }
}
