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

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);

        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);

        setupUsernameIcon(usernameInput);
        setupPasswordToggle(passwordInput);

        loginButton.setOnClickListener(view ->
                login(usernameInput, passwordInput)
        );

        signupButton.setOnClickListener(view ->
                startActivity(new Intent(this, SignupActivity.class))
        );
    }

    private void login(
            EditText usernameInput,
            EditText passwordInput
    ) {

        String username =
                usernameInput.getText().toString().trim();

        String password =
                passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage(R.string.fill_login_fields);
            return;
        }

        // Successful login
        Intent intent =
                new Intent(this, DonutActivity.class);

        startActivity(intent);

        finish();
    }

    private void setupUsernameIcon(EditText usernameInput) {

        usernameInput.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_person,
                0,
                0,
                0
        );
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordToggle(EditText passwordInput) {

        passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_lock,
                0,
                R.drawable.ic_visibility,
                0
        );

        passwordInput.setOnTouchListener((view, event) -> {

            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            int drawableEndWidth =
                    passwordInput
                            .getCompoundDrawablesRelative()[2]
                            .getBounds()
                            .width();

            int touchAreaStart =
                    passwordInput.getWidth()
                            - passwordInput.getPaddingEnd()
                            - drawableEndWidth;

            if (event.getX() < touchAreaStart) {
                return false;
            }

            boolean isPasswordHidden =
                    passwordInput
                            .getTransformationMethod()
                            instanceof PasswordTransformationMethod;

            if (isPasswordHidden) {

                passwordInput.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );

                passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_lock,
                        0,
                        R.drawable.ic_visibility_off,
                        0
                );

            } else {

                passwordInput.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );

                passwordInput.setCompoundDrawablesRelativeWithIntrinsicBounds(
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
        });
    }

    private void showMessage(int messageId) {

        Toast.makeText(
                this,
                messageId,
                Toast.LENGTH_SHORT
        ).show();
    }
}