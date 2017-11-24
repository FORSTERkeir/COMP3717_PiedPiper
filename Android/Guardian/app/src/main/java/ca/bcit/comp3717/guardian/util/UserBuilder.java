package ca.bcit.comp3717.guardian.util;

import android.content.Intent;
import android.os.Bundle;

import ca.bcit.comp3717.guardian.model.User;

public class UserBuilder {

    public static User constructUserFromIntent(Intent i) {
        User user = new User();
        user.setId(i.getIntExtra("userId", -1));
        user.setUserName(i.getStringExtra("userName"));
        user.setEmail(i.getStringExtra("email"));
        user.setPassword(i.getStringExtra("password"));
        user.setPhone(i.getStringExtra("phoneNumber"));
        return user;
    }

    public static User constructUserFromSavedInstanceState(Bundle savedInstanceState) {
        User user = new User();
        user.setId(savedInstanceState.getInt("userId", -1));
        user.setUserName(savedInstanceState.getString("userName"));
        user.setEmail(savedInstanceState.getString("email"));
        user.setPassword(savedInstanceState.getString("password"));
        user.setPhone(savedInstanceState.getString("phoneNumber"));
        return user;
    }
}
