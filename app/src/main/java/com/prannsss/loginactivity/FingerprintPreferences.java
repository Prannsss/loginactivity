package com.prannsss.loginactivity;

import android.content.Context;
import android.content.SharedPreferences;

public class FingerprintPreferences {

    private static final String PREF_NAME =
            "login_preferences";

    private static final String KEY_FINGERPRINT_ENABLED =
            "fingerprint_enabled";

    private final SharedPreferences preferences;

    public FingerprintPreferences(
            Context context
    ) {

        preferences =
                context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );
    }

    public boolean isFingerprintEnabled() {

        return preferences.getBoolean(
                KEY_FINGERPRINT_ENABLED,
                false
        );
    }

    public void setFingerprintEnabled(
            boolean enabled
    ) {

        preferences.edit()
                .putBoolean(
                        KEY_FINGERPRINT_ENABLED,
                        enabled
                )
                .apply();
    }

    public void disableFingerprint() {

        setFingerprintEnabled(false);
    }
}