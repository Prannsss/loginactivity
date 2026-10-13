
package com.prannsss.loginactivity;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocalUserStore {

    private static final String PREF_NAME = "local_users";
    private static final String KEY_USERS = "users";

    private final SharedPreferences preferences;

    public LocalUserStore(Context context) {
        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public synchronized boolean registerUser(
            String email,
            String password
    ) {
        JSONArray users = getUsersArray();

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.optJSONObject(i);

            if (user != null && email.equalsIgnoreCase(
                    user.optString("email"))) {
                return false;
            }
        }

        try {
            JSONObject newUser = new JSONObject();
            newUser.put("email", email.trim());
            newUser.put("password", password);

            users.put(newUser);

            preferences.edit()
                    .putString(KEY_USERS, users.toString())
                    .apply();

            return true;

        } catch (Exception exception) {
            return false;
        }
    }

    public synchronized boolean authenticate(
            String email,
            String password
    ) {
        JSONArray users = getUsersArray();

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.optJSONObject(i);

            if (user == null) {
                continue;
            }

            if (email.equalsIgnoreCase(
                    user.optString("email"))
                    && password.equals(
                    user.optString("password"))) {
                return true;
            }
        }

        return false;
    }

    public synchronized List<String> getEmails() {
        List<String> emails = new ArrayList<>();
        JSONArray users = getUsersArray();

        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.optJSONObject(i);

            if (user != null) {
                String email = user.optString("email");

                if (!email.isEmpty()) {
                    emails.add(email);
                }
            }
        }

        return emails;
    }

    private JSONArray getUsersArray() {
        String stored = preferences.getString(
                KEY_USERS,
                "[]"
        );

        try {
            return new JSONArray(stored);
        } catch (Exception exception) {
            return new JSONArray();
        }
    }
}
