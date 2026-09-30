package com.prannsss.loginactivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText emailInput = findViewById(R.id.emailInput);
        EditText confirmEmailInput = findViewById(R.id.confirmEmailInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        EditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        Button signupButton = findViewById(R.id.signupButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        setupPasswordToggle(passwordInput);
        setupPasswordToggle(confirmPasswordInput);

        signupButton.setOnClickListener(view ->
                signup(
                        emailInput,
                        confirmEmailInput,
                        passwordInput,
                        confirmPasswordInput
                )
        );

        cancelButton.setOnClickListener(view -> finish());
    }

    private void signup(
            EditText emailInput,
            EditText confirmEmailInput,
            EditText passwordInput,
            EditText confirmPasswordInput
    ) {

        String email = emailInput.getText().toString().trim();
        String confirmEmail = confirmEmailInput.getText().toString().trim();

        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (email.isEmpty()
                || confirmEmail.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            showMessage(R.string.fill_signup_fields);
            return;
        }

        if (!email.equals(confirmEmail)) {
            showMessage(R.string.emails_not_match);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showMessage(R.string.passwords_not_match);
            return;
        }

        showMessage(R.string.signup_success);
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
                    passwordInput.getCompoundDrawablesRelative()[2]
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
                    passwordInput.getTransformationMethod()
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

            passwordInput.setSelection(passwordInput.length());

            return true;
        });
    }

    private void showMessage(int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
    }
}