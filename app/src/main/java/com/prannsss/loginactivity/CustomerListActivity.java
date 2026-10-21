package com.prannsss.loginactivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CustomerListActivity extends Activity {

    private static final String CUSTOMER_URL =
            "https://leytfpoyceaedfskdatm.supabase.co/rest/v1/customer";

    // Supabase publishable key (safe for client use; access is limited by RLS).
    private static final String SUPABASE_KEY =
            "sb_publishable_cTq82A9F1aqjcCWxUTCKmA_BUniFH3z";

    private TextView customerSummary;
    private ArrayAdapter<String> customerAdapter;

    private final List<String> displayedCustomers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerlist);

        ListView customerListView = findViewById(R.id.customerListView);
        customerSummary = findViewById(R.id.customerSummary);

        Button addCustomerButton = findViewById(R.id.addCustomerButton);
        Button refreshButton = findViewById(R.id.refreshCustomersButton);
        Button backButton = findViewById(R.id.backButton);

        customerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                displayedCustomers
        );
        customerListView.setAdapter(customerAdapter);

        addCustomerButton.setOnClickListener(v -> showAddCustomerDialog());
        refreshButton.setOnClickListener(v -> loadCustomers());
        backButton.setOnClickListener(v -> finish());

        loadCustomers();
    }

    private void loadCustomers() {
        customerSummary.setText(R.string.customer_loading);

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                connection = openConnection(
                        CUSTOMER_URL + "?select=*&order=customerid.asc"
                );
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();

                if (responseCode < 200 || responseCode >= 300) {
                    throw new Exception("HTTP " + responseCode);
                }

                JSONArray customers = new JSONArray(
                        readBody(connection.getInputStream())
                );

                List<String> rows = new ArrayList<>();

                for (int i = 0; i < customers.length(); i++) {
                    rows.add(formatCustomer(customers.getJSONObject(i)));
                }

                runOnUiThread(() -> displayCustomers(rows));

            } catch (Exception e) {
                runOnUiThread(() -> {
                    customerSummary.setText(R.string.customer_load_failed);
                    Toast.makeText(
                            CustomerListActivity.this,
                            getString(R.string.customer_load_failed)
                                    + " " + e.getMessage(),
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

    private void showAddCustomerDialog() {
        View form = getLayoutInflater().inflate(
                R.layout.dialog_add_customer,
                null
        );

        EditText lastNameInput = form.findViewById(R.id.lastNameInput);
        EditText firstNameInput = form.findViewById(R.id.firstNameInput);
        EditText mobileInput = form.findViewById(R.id.mobileInput);
        EditText addressInput = form.findViewById(R.id.addressInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_customer_title)
                .setView(form)
                .setNegativeButton(R.string.cancel_button, null)
                .setPositiveButton(R.string.save_customer_button, null)
                .create();

        // Override the positive button so the dialog stays open on validation errors.
        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            String lastName = lastNameInput.getText().toString().trim();
                            String firstName = firstNameInput.getText().toString().trim();
                            String mobile = mobileInput.getText().toString().trim();
                            String address = addressInput.getText().toString().trim();

                            if (lastName.isEmpty() || firstName.isEmpty()) {
                                Toast.makeText(
                                        this,
                                        R.string.customer_name_required,
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            dialog.dismiss();
                            insertCustomer(lastName, firstName, mobile, address);
                        })
        );

        dialog.show();
    }

    private void insertCustomer(
            String lastName,
            String firstName,
            String mobile,
            String address) {

        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                JSONObject body = new JSONObject();
                body.put("lastname", lastName);
                body.put("firstname", firstName);
                body.put("mobileno", mobile.isEmpty() ? JSONObject.NULL : mobile);
                body.put("address", address.isEmpty() ? JSONObject.NULL : address);

                connection = openConnection(CUSTOMER_URL);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Prefer", "return=representation");

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();

                if (responseCode < 200 || responseCode >= 300) {
                    String error = connection.getErrorStream() != null
                            ? readBody(connection.getErrorStream())
                            : "";
                    throw new Exception("HTTP " + responseCode + " " + error);
                }

                runOnUiThread(() -> {
                    Toast.makeText(
                            CustomerListActivity.this,
                            R.string.customer_added,
                            Toast.LENGTH_SHORT
                    ).show();
                    loadCustomers();
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                CustomerListActivity.this,
                                getString(R.string.customer_add_failed)
                                        + " " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private HttpURLConnection openConnection(String url) throws Exception {
        HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("apikey", SUPABASE_KEY);
        connection.setRequestProperty("Accept", "application/json");

        return connection;
    }

    private String readBody(InputStream input) throws Exception {
        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    private String formatCustomer(JSONObject customer) {
        StringBuilder row = new StringBuilder();

        row.append("#")
                .append(customer.optLong("customerid"))
                .append("  ")
                .append(customer.optString("lastname"))
                .append(", ")
                .append(customer.optString("firstname"));

        String mobile = customer.isNull("mobileno") ? "" : customer.optString("mobileno");
        String address = customer.isNull("address") ? "" : customer.optString("address");

        if (!mobile.isEmpty()) {
            row.append("\n").append(mobile);
        }

        if (!address.isEmpty()) {
            row.append("\n").append(address);
        }

        return row.toString();
    }

    private void displayCustomers(List<String> rows) {
        displayedCustomers.clear();
        displayedCustomers.addAll(rows);
        customerAdapter.notifyDataSetChanged();

        if (rows.isEmpty()) {
            customerSummary.setText(R.string.customer_empty);
        } else {
            customerSummary.setText(
                    getString(R.string.customer_loaded_count, rows.size())
            );
        }
    }
}
