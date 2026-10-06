package com.prannsss.loginactivity;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

public class FingerprintHelper {

    public interface AuthenticationCallback {

        void onSuccess();

        void onFailed();

        void onError(String message);
    }

    private final Activity activity;

    private CancellationSignal cancellationSignal;

    public FingerprintHelper(
            Activity activity
    ) {

        this.activity = activity;
    }

    public boolean isAvailable() {

        if (
                android.os.Build.VERSION.SDK_INT
                        < android.os.Build.VERSION_CODES.M
        ) {

            return false;
        }

        if (
                activity.checkSelfPermission(
                        Manifest.permission.USE_FINGERPRINT
                )
                        != PackageManager.PERMISSION_GRANTED
        ) {

            return false;
        }

        FingerprintManager fingerprintManager =
                (FingerprintManager)
                        activity.getSystemService(
                                Context.FINGERPRINT_SERVICE
                        );

        if (fingerprintManager == null) {
            return false;
        }

        if (
                !fingerprintManager
                        .isHardwareDetected()
        ) {

            return false;
        }

        if (
                !fingerprintManager
                        .hasEnrolledFingerprints()
        ) {

            return false;
        }

        KeyguardManager keyguardManager =
                (KeyguardManager)
                        activity.getSystemService(
                                Context.KEYGUARD_SERVICE
                        );

        return keyguardManager != null
                && keyguardManager
                .isKeyguardSecure();
    }

    public void authenticate(
            AuthenticationCallback callback
    ) {

        if (!isAvailable()) {

            callback.onError(
                    activity.getString(
                            R.string
                                    .fingerprint_unavailable
                    )
            );

            return;
        }

        FingerprintManager fingerprintManager =
                (FingerprintManager)
                        activity.getSystemService(
                                Context.FINGERPRINT_SERVICE
                        );

        cancellationSignal =
                new CancellationSignal();

        fingerprintManager.authenticate(
                null,
                cancellationSignal,
                0,
                new FingerprintManager
                        .AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            FingerprintManager
                                    .AuthenticationResult result
                    ) {

                        activity.runOnUiThread(
                                callback::onSuccess
                        );
                    }

                    @Override
                    public void onAuthenticationFailed() {

                        activity.runOnUiThread(
                                callback::onFailed
                        );
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            CharSequence errString
                    ) {

                        activity.runOnUiThread(
                                () ->
                                        callback.onError(
                                                errString
                                                        .toString()
                                        )
                        );
                    }
                },
                null
        );
    }

    public void cancel() {

        if (cancellationSignal != null) {

            cancellationSignal.cancel();

            cancellationSignal = null;
        }
    }
}