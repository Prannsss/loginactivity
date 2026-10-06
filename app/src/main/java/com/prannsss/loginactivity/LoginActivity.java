package com.prannsss.loginactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends Activity {

    private static final String LOGIN_URL =
            "https://loginactivity.onrender.com/api/login";

    private FingerprintHelper fingerprintHelper;
    private FingerprintPreferences fingerprintPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText usernameInput =
                findViewById(R.id.usernameInput);

        EditText passwordInput =
                findViewById(R.id.passwordInput);

        Button loginButton =
                findViewById(R.id.loginButton);

        Button signupButton =
                findViewById(R.id.signupButton);

        fingerprintHelper =
                new FingerprintHelper(this);

        fingerprintPreferences =
                new FingerprintPreferences(this);

        setupUsernameIcon(usernameInput);
        setupPasswordToggle(passwordInput);

        loginButton.setOnClickListener(view ->
                login(usernameInput, passwordInput)
        );

        signupButton.setOnClickListener(view ->
                startActivity(
                        new Intent(
                                this,
                                SignupActivity.class
                        )
                )
        );
    }

    private void login(
            EditText usernameInput,
            EditText passwordInput
    ) {
        String username =
                usernameInput.getText()
                        .toString()
                        .trim();

        String password =
                passwordInput.getText()
                        .toString();

        if (username.isEmpty()
                || password.isEmpty()) {

            showMessage(
                    R.string.fill_login_fields
            );

            return;
        }

        validateLogin(
                username,
                password
        );
    }

    private void validateLogin(
            String username,
            String password
    ) {
        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url =
                        new URL(LOGIN_URL);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                connection.setDoOutput(true);

                connection.setConnectTimeout(10000);

                connection.setReadTimeout(10000);

                JSONObject requestBody =
                        new JSONObject();

                requestBody.put(
                        "username",
                        username
                );

                requestBody.put(
                        "password",
                        password
                );

                byte[] requestData =
                        requestBody
                                .toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                try (
                        OutputStream outputStream =
                                connection.getOutputStream()
                ) {

                    outputStream.write(
                            requestData
                    );

                    outputStream.flush();
                }

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (
                        responseCode >= 200
                                && responseCode < 300
                ) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();
                }

                String response =
                        readResponse(
                                inputStream
                        );

                runOnUiThread(() ->
                        handleLoginResponse(
                                responseCode,
                                response
                        )
                );

            } catch (Exception exception) {

                exception.printStackTrace();

                runOnUiThread(() ->
                        showMessage(
                                "Error: "
                                        + exception
                                        .getClass()
                                        .getSimpleName()
                        )
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private void handleLoginResponse(
            int responseCode,
            String response
    ) {

        try {

            JSONObject json =
                    new JSONObject(response);

            boolean success =
                    json.optBoolean(
                            "success",
                            false
                    );

            String message =
                    json.optString(
                            "message",
                            "Login failed"
                    );

            if (
                    success
                            && responseCode == 200
            ) {

                handleSuccessfulLogin();

            } else {

                showMessage(message);
            }

        } catch (Exception exception) {

            showMessage(
                    "Invalid server response"
            );
        }
    }

    private void handleSuccessfulLogin() {

        if (
                fingerprintPreferences
                        .isFingerprintEnabled()
                        && fingerprintHelper
                        .isAvailable()
        ) {

            showFingerprintLogin();

        } else {

            openOtpActivity();
        }
    }

    private void showFingerprintLogin() {

        new android.app.AlertDialog.Builder(this)
                .setTitle(
                        R.string.fingerprint_prompt_title
                )
                .setMessage(
                        R.string.fingerprint_prompt_message
                )
                .setNegativeButton(
                        R.string.use_otp,
                        (dialog, which) ->
                                openOtpActivity()
                )
                .setPositiveButton(
                        R.string.try_fingerprint_again,
                        null
                )
                .setCancelable(false)
                .show();

        authenticateFingerprint();
    }

    private void authenticateFingerprint() {

        fingerprintHelper.authenticate(
                new FingerprintHelper.AuthenticationCallback() {

                    @Override
                    public void onSuccess() {

                        openDonutActivity();
                    }

                    @Override
                    public void onFailed() {

                        showMessage(
                                R.string.fingerprint_failed
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        openOtpActivity();
                    }
                }
        );
    }

    private void openOtpActivity() {

        Intent intent =
                new Intent(
                        this,
                        OtpActivity.class
                );

        startActivity(intent);

        finish();
    }

    private void openDonutActivity() {

        Intent intent =
                new Intent(
                        this,
                        DonutActivity.class
                );

        startActivity(intent);

        finish();
    }

    private String readResponse(
            InputStream inputStream
    ) throws Exception {

        if (inputStream == null) {
            return "";
        }

        StringBuilder response =
                new StringBuilder();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                StandardCharsets.UTF_8
                        )
                );

        String line;

        while (
                (line = reader.readLine()) != null
        ) {

            response.append(line);
        }

        reader.close();

        return response.toString();
    }

    private void setupUsernameIcon(
            EditText usernameInput
    ) {

        usernameInput
                .setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_person,
                        0,
                        0,
                        0
                );
    }

    @SuppressLint(
            "ClickableViewAccessibility"
    )
    private void setupPasswordToggle(
            EditText passwordInput
    ) {

        passwordInput
                .setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_lock,
                        0,
                        R.drawable.ic_visibility,
                        0
                );

        passwordInput.setOnTouchListener(
                (view, event) -> {

                    if (
                            event.getAction()
                                    != MotionEvent.ACTION_UP
                    ) {

                        return false;
                    }

                    int drawableEndWidth =
                            passwordInput
                                    .getCompoundDrawablesRelative()[2]
                                    .getBounds()
                                    .width();

                    int touchAreaStart =
                            passwordInput.getWidth()
                                    - passwordInput
                                    .getPaddingEnd()
                                    - drawableEndWidth;

                    if (
                            event.getX()
                                    < touchAreaStart
                    ) {

                        return false;
                    }

                    boolean isPasswordHidden =
                            passwordInput
                                    .getTransformationMethod()
                                    instanceof
                                    PasswordTransformationMethod;

                    if (isPasswordHidden) {

                        passwordInput
                                .setTransformationMethod(
                                        HideReturnsTransformationMethod
                                                .getInstance()
                                );

                        passwordInput
                                .setCompoundDrawablesRelativeWithIntrinsicBounds(
                                        R.drawable.ic_lock,
                                        0,
                                        R.drawable.ic_visibility_off,
                                        0
                                );

                    } else {

                        passwordInput
                                .setTransformationMethod(
                                        PasswordTransformationMethod
                                                .getInstance()
                                );

                        passwordInput
                                .setCompoundDrawablesRelativeWithIntrinsicBounds(
                                        R.drawable.ic_lock,
                                        0,
                                        R.drawable.ic_visibility,
                                        0
                                );
                    }

                    passwordInput.setSelection(
                            passwordInput.length()
                    );

                    return true;
                }
        );
    }

    private void showMessage(
            int messageId
    ) {

        Toast.makeText(
                this,
                messageId,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void showMessage(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {

        if (fingerprintHelper != null) {
            fingerprintHelper.cancel();
        }

        super.onDestroy();
    }
}