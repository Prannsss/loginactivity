package com.prannsss.loginactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainDashboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard);

        Button customersButton = findViewById(R.id.customersButton);
        Button usersButton = findViewById(R.id.usersButton);
        Button logoutButton = findViewById(R.id.logoutButton);

        customersButton.setOnClickListener(v ->
                startActivity(new Intent(this, CustomerListActivity.class))
        );
        usersButton.setOnClickListener(v ->
                startActivity(new Intent(this, UserListActivity.class))
        );
        logoutButton.setOnClickListener(v -> logout());
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
                                    MainDashboardActivity.this,
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
