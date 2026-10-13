
package com.prannsss.loginactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserListActivity extends Activity {

    private static final String USERS_URL =
            "https://loginactivity.onrender.com/api/users";

    private LocalUserStore localUserStore;
    private ListView userListView;
    private TextView userlistSummary;
    private ArrayAdapter<String> userAdapter;
    private Spinner activityDropdown;

    private final List<String> displayedUsers = new ArrayList<>();
    private boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist);

        localUserStore = new LocalUserStore(this);

        userListView = findViewById(R.id.userListView);
        userlistSummary = findViewById(R.id.userlistSummary);
        activityDropdown = findViewById(R.id.activityDropdown);

        Button refreshButton = findViewById(R.id.refreshUsersButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        setupActivityDropdown();

        userAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                displayedUsers
        );
        userListView.setAdapter(userAdapter);

        refreshButton.setOnClickListener(v -> loadUsers());
        logoutButton.setOnClickListener(v -> logout());

        loadUsers();
    }

    private void setupActivityDropdown() {
        String[] pages = {"User List", "Donut Animation"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                pages
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        activityDropdown.setAdapter(adapter);
        activityDropdown.setSelection(0, false);

        activityDropdown.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        if (position == 1 && !isNavigating) {
                            isNavigating = true;
                            startActivity(new Intent(
                                    UserListActivity.this,
                                    DonutActivity.class
                            ));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNavigating = false;

        if (activityDropdown != null
                && activityDropdown.getSelectedItemPosition() != 0) {
            activityDropdown.setSelection(0, false);
        }
    }

    private void loadUsers() {
        Map<String, String> users = new LinkedHashMap<>();

        for (String email : localUserStore.getEmails()) {
            users.put(
                    email.toLowerCase(Locale.ROOT),
                    email + " (LOCAL)"
            );
        }

        displayUsers(users);
        userlistSummary.setText("Loading server users...");

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection)
                        new URL(USERS_URL).openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception("HTTP " + responseCode);
                }

                StringBuilder response = new StringBuilder();

                try (InputStream input = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(input))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                JSONObject root = new JSONObject(response.toString());
                JSONArray serverUsers = root.optJSONArray("users");

                if (serverUsers == null) {
                    throw new Exception("Missing users array in server response");
                }

                for (int i = 0; i < serverUsers.length(); i++) {
                    Object item = serverUsers.opt(i);
                    String name = "";

                    if (item instanceof JSONObject) {
                        JSONObject user = (JSONObject) item;
                        name = user.optString("email");

                        if (name.isEmpty()) {
                            name = user.optString("username");
                        }
                    } else if (item instanceof String) {
                        name = (String) item;
                    }

                    if (!name.trim().isEmpty()) {
                        users.putIfAbsent(
                                name.toLowerCase(Locale.ROOT),
                                name + " (RENDER)"
                        );
                    }
                }

                runOnUiThread(() -> {
                    displayUsers(users);
                    userlistSummary.setText(
                            "Users found: " + users.size()
                    );
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    displayUsers(users);
                    userlistSummary.setText(
                            "Showing local users. Server unavailable."
                    );

                    Toast.makeText(
                            UserListActivity.this,
                            "Could not load server users.",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void displayUsers(Map<String, String> users) {
        displayedUsers.clear();
        displayedUsers.addAll(users.values());
        userAdapter.notifyDataSetChanged();

        if (users.isEmpty()) {
            userlistSummary.setText("No users found.");
        } else {
            userlistSummary.setText("Users found: " + users.size());
        }
    }

    private void logout() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.logout_cancel, null)
                .setPositiveButton(
                        R.string.logout_confirm,
                        (dialog, which) -> {
                            Intent intent = new Intent(
                                    UserListActivity.this,
                                    LoginActivity.class
                            );
                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            | Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );
                            startActivity(intent);
                            finish();
                        }
                )
                .show();
    }
}
