package com.prannsss.loginactivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class OtpActivity extends Activity {

    private static final String TAG =
            "OTP_DEBUG";

    private static final long OTP_DURATION =
            30_000;

    private EditText otpInput1;
    private EditText otpInput2;
    private EditText otpInput3;
    private EditText otpInput4;

    private TextView otpTimer;

    private CountDownTimer countDownTimer;

    private String currentOtp;

    private boolean otpExpired = false;

    private FingerprintHelper fingerprintHelper;

    private FingerprintPreferences fingerprintPreferences;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_otp
        );

        otpInput1 =
                findViewById(
                        R.id.otpInput1
                );

        otpInput2 =
                findViewById(
                        R.id.otpInput2
                );

        otpInput3 =
                findViewById(
                        R.id.otpInput3
                );

        otpInput4 =
                findViewById(
                        R.id.otpInput4
                );

        otpTimer =
                findViewById(
                        R.id.otpTimer
                );

        Button verifyOtpButton =
                findViewById(
                        R.id.verifyOtpButton
                );

        Button resendOtpButton =
                findViewById(
                        R.id.resendOtpButton
                );

        Button backToLoginButton =
                findViewById(
                        R.id.backToLoginButton
                );

        fingerprintHelper =
                new FingerprintHelper(this);

        fingerprintPreferences =
                new FingerprintPreferences(this);

        setupOtpInputs();

        generateOtp();

        verifyOtpButton.setOnClickListener(
                view ->
                        verifyOtp()
        );

        resendOtpButton.setOnClickListener(
                view ->
                        generateOtp()
        );

        backToLoginButton.setOnClickListener(
                view ->
                        backToLogin()
        );
    }

    private void generateOtp() {

        stopTimer();

        Random random =
                new Random();

        int otpNumber =
                1000
                        + random.nextInt(
                        9000
                );

        currentOtp =
                String.valueOf(
                        otpNumber
                );

        otpExpired = false;

        clearOtpInputs();

        Log.d(
                TAG,
                getString(
                        R.string.otp_generated_log,
                        currentOtp
                )
        );

        startTimer();

        otpInput1.requestFocus();

        InputMethodManager keyboard =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (keyboard != null) {

            keyboard.showSoftInput(
                    otpInput1,
                    InputMethodManager
                            .SHOW_IMPLICIT
            );
        }
    }

    private void startTimer() {

        countDownTimer =
                new CountDownTimer(
                        OTP_DURATION,
                        1000
                ) {

                    @Override
                    public void onTick(
                            long millisUntilFinished
                    ) {

                        long remainingSeconds =
                                (
                                        millisUntilFinished
                                                + 999
                                ) / 1000;

                        otpTimer.setText(
                                getString(
                                        R.string
                                                .otp_timer_seconds,
                                        remainingSeconds
                                )
                        );
                    }

                    @Override
                    public void onFinish() {

                        otpExpired = true;

                        currentOtp = null;

                        otpTimer.setText(
                                R.string.otp_expired
                        );

                        Log.d(
                                TAG,
                                "OTP expired"
                        );
                    }
                };

        countDownTimer.start();
    }

    private void verifyOtp() {

        String enteredOtp =
                otpInput1.getText()
                        .toString()
                        + otpInput2.getText()
                        .toString()
                        + otpInput3.getText()
                        .toString()
                        + otpInput4.getText()
                        .toString();

        if (enteredOtp.length() != 4) {

            showMessage(
                    R.string.otp_empty
            );

            return;
        }

        if (
                otpExpired
                        || currentOtp == null
        ) {

            showMessage(
                    R.string.otp_expired
            );

            return;
        }

        if (
                enteredOtp.equals(
                        currentOtp
                )
        ) {

            stopTimer();

            showFingerprintRegistration();

        } else {

            showMessage(
                    R.string.otp_invalid
            );
        }
    }

    private void showFingerprintRegistration() {

        if (
                !fingerprintHelper
                        .isAvailable()
        ) {

            openDonutActivity();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        R.string.fingerprint_title
                )
                .setMessage(
                        R.string.fingerprint_message
                )
                .setNegativeButton(
                        R.string.fingerprint_skip,
                        (dialog, which) ->
                                openDonutActivity()
                )
                .setPositiveButton(
                        R.string.fingerprint_enable,
                        (dialog, which) ->
                                registerFingerprint()
                )
                .setCancelable(false)
                .show();
    }

    private void registerFingerprint() {

        fingerprintHelper.authenticate(
                new FingerprintHelper.AuthenticationCallback() {

                    @Override
                    public void onSuccess() {

                        fingerprintPreferences
                                .setFingerprintEnabled(
                                        true
                                );

                        showMessage(
                                R.string.fingerprint_success
                        );

                        openDonutActivity();
                    }

                    @Override
                    public void onFailed() {

                        showMessage(
                                R.string
                                        .fingerprint_failed
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        showMessage(message);

                        openDonutActivity();
                    }
                }
        );
    }

    private void setupOtpInputs() {

        setupInput(
                otpInput1,
                otpInput2
        );

        setupInput(
                otpInput2,
                otpInput3
        );

        setupInput(
                otpInput3,
                otpInput4
        );

        otpInput1.setOnKeyListener(
                (view, keyCode, event) -> {

                    return false;
                }
        );

        otpInput2.setOnKeyListener(
                (view, keyCode, event) -> {

                    if (
                            keyCode
                                    == KeyEvent
                                    .KEYCODE_DEL
                                    && event.getAction()
                                    == KeyEvent
                                    .ACTION_DOWN
                                    && otpInput2
                                    .getText()
                                    .length()
                                    == 0
                    ) {

                        otpInput1
                                .requestFocus();

                        return true;
                    }

                    return false;
                }
        );

        otpInput3.setOnKeyListener(
                (view, keyCode, event) -> {

                    if (
                            keyCode
                                    == KeyEvent
                                    .KEYCODE_DEL
                                    && event.getAction()
                                    == KeyEvent
                                    .ACTION_DOWN
                                    && otpInput3
                                    .getText()
                                    .length()
                                    == 0
                    ) {

                        otpInput2
                                .requestFocus();

                        return true;
                    }

                    return false;
                }
        );

        otpInput4.setOnKeyListener(
                (view, keyCode, event) -> {

                    if (
                            keyCode
                                    == KeyEvent
                                    .KEYCODE_DEL
                                    && event.getAction()
                                    == KeyEvent
                                    .ACTION_DOWN
                                    && otpInput4
                                    .getText()
                                    .length()
                                    == 0
                    ) {

                        otpInput3
                                .requestFocus();

                        return true;
                    }

                    return false;
                }
        );
    }

    private void setupInput(
            EditText current,
            EditText next
    ) {

        current.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {
                        current.selectAll();
                    }
                }
        );

        current.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        if (
                                s.length() == 1
                        ) {

                            next.requestFocus();
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s
                    ) {
                    }
                }
        );
    }

    private void clearOtpInputs() {

        otpInput1.setText("");
        otpInput2.setText("");
        otpInput3.setText("");
        otpInput4.setText("");
    }

    private void stopTimer() {

        if (countDownTimer != null) {

            countDownTimer.cancel();

            countDownTimer = null;
        }
    }

    private void backToLogin() {

        stopTimer();

        currentOtp = null;

        otpExpired = true;

        Intent intent =
                new Intent(
                        this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
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

        stopTimer();

        currentOtp = null;

        if (fingerprintHelper != null) {
            fingerprintHelper.cancel();
        }

        super.onDestroy();
    }
}