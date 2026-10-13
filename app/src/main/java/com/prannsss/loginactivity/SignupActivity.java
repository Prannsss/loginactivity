
package com.prannsss.loginactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends Activity {

    private LocalUserStore localUserStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        localUserStore = new LocalUserStore(this);

        EditText emailInput = findViewById(R.id.emailInput);
        EditText confirmEmailInput =
                findViewById(R.id.confirmEmailInput);
        EditText passwordInput =
                findViewById(R.id.passwordInput);
        EditText confirmPasswordInput =
                findViewById(R.id.confirmPasswordInput);

        Button signupButton = findViewById(R.id.signupButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        setupPasswordToggle(passwordInput);
        setupPasswordToggle(confirmPasswordInput);

        signupButton.setOnClickListener(view -> {
            String email = emailInput.getText()
                    .toString().trim();
            String confirmEmail = confirmEmailInput.getText()
                    .toString().trim();
            String password = passwordInput.getText()
                    .toString();
            String confirmPassword =
                    confirmPasswordInput.getText().toString();

            if (email.isEmpty() || confirmEmail.isEmpty()
                    || password.isEmpty()
                    || confirmPassword.isEmpty()) {
                showMessage(R.string.fill_signup_fields);
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email).matches()) {
                showMessage(R.string.invalid_email);
                return;
            }

            if (!email.equalsIgnoreCase(confirmEmail)) {
                showMessage(R.string.email_mismatch);
                return;
            }

            if (!password.equals(confirmPassword)) {
                showMessage(R.string.password_mismatch);
                return;
            }

            boolean registered =
                    localUserStore.registerUser(email, password);

            if (!registered) {
                showMessage(R.string.email_already_registered);
                return;
            }

            Toast.makeText(
                    this,
                    R.string.signup_success,
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );
            startActivity(intent);
            finish();
        });

        cancelButton.setOnClickListener(view -> finish());
    }

    private void setupPasswordToggle(EditText passwordInput) {
        passwordInput.setOnTouchListener((view, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            android.graphics.drawable.Drawable endDrawable =
                    passwordInput.getCompoundDrawablesRelative()[2];

            if (endDrawable == null) {
                return false;
            }

            int start = passwordInput.getWidth()
                    - passwordInput.getPaddingEnd()
                    - endDrawable.getBounds().width();

            if (event.getX() < start) {
                return false;
            }

            boolean hidden = passwordInput.getTransformationMethod()
                    instanceof PasswordTransformationMethod;

            if (hidden) {
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
