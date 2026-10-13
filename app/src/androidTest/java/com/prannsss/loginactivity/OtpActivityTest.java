package com.prannsss.loginactivity;

import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class OtpActivityTest {

    private static final String PASSWORD = "Testing@123";
    private static final long ACTION_DELAY_MS = 2500;
    private static final long OTP_ATTEMPT_DELAY_MS = 1500;
    private static final long TIMEOUT_MS = 30000;

    private static ActivityScenario<LoginActivity> loginScenario;

    @Test
    public void invalidOtp_thenValidOtp_shouldOpenUserList() throws Exception {
        String email = createUniqueEmail();

        loginScenario = ActivityScenario.launch(LoginActivity.class);
        delay(ACTION_DELAY_MS);

        // Register a fresh local account.
        onView(withId(R.id.signupButton)).perform(click());
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.emailInput)).perform(replaceText(email));
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.confirmEmailInput)).perform(replaceText(email));
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.passwordInput)).perform(replaceText(PASSWORD));
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.confirmPasswordInput))
                .perform(replaceText(PASSWORD), closeSoftKeyboard());
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.signupButton)).perform(click());
        delay(ACTION_DELAY_MS);

        waitForView(R.id.usernameInput, 10000);

        // Log in.
        onView(withId(R.id.usernameInput)).perform(replaceText(email));
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.passwordInput))
                .perform(replaceText(PASSWORD), closeSoftKeyboard());
        delay(ACTION_DELAY_MS);

        clearLogcat();

        onView(withId(R.id.loginButton)).perform(click());
        delay(ACTION_DELAY_MS);

        chooseOtpIfFingerprintDialogAppears();

        waitForView(R.id.otpInput1, TIMEOUT_MS);

        // Capture the OTP generated for this login.
        String validOtp = captureLatestOtp(TIMEOUT_MS);

        if (!validOtp.matches("\\d{4}")) {
            throw new AssertionError("Captured OTP is not four digits.");
        }

        // Submit a different, invalid OTP.
        String invalidOtp = createInvalidOtp(validOtp);
        enterOtp(invalidOtp);

        delay(OTP_ATTEMPT_DELAY_MS);

        // The OTP page should remain visible after invalid verification.
        onView(withId(R.id.otpInput1)).check(matches(isDisplayed()));

        // Submit the correct OTP.
        enterOtp(validOtp);

        delay(OTP_ATTEMPT_DELAY_MS);

        // Successful verification should open the User List page.
        waitForView(R.id.userlistSummary, 15000);
        onView(withId(R.id.userlistSummary)).check(matches(isDisplayed()));

        // Intentionally do not call loginScenario.close().
    }

    private void enterOtp(String otp) {
        onView(withId(R.id.otpInput1))
                .perform(replaceText(otp.substring(0, 1)));
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.otpInput2))
                .perform(replaceText(otp.substring(1, 2)));
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.otpInput3))
                .perform(replaceText(otp.substring(2, 3)));
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.otpInput4))
                .perform(replaceText(otp.substring(3, 4)), closeSoftKeyboard());
        delay(ACTION_DELAY_MS);

        onView(withId(R.id.verifyOtpButton)).perform(click());
    }

    private String createInvalidOtp(String validOtp) {
        int value = Integer.parseInt(validOtp);
        return String.format(Locale.US, "%04d", (value + 1) % 10000);
    }

    private String createUniqueEmail() {
        String timestamp = new SimpleDateFormat(
                "yyyyMMddHHmmssSSS", Locale.US
        ).format(new Date());

        return "otptest" + timestamp + "@example.com";
    }

    private void chooseOtpIfFingerprintDialogAppears() {
        try {
            onView(withText(R.string.use_otp)).check(matches(isDisplayed()));
            onView(withText(R.string.use_otp)).perform(click());
            delay(ACTION_DELAY_MS);
        } catch (NoMatchingViewException | AssertionError ignored) {
            // No fingerprint dialog: continue with the OTP page.
        }
    }

    private void clearLogcat() throws Exception {
        ParcelFileDescriptor pfd = InstrumentationRegistry
                .getInstrumentation()
                .getUiAutomation()
                .executeShellCommand("logcat -c");

        try {
            pfd.close();
        } catch (Exception ignored) {
            // Best effort.
        }
    }

    private String captureLatestOtp(long timeoutMs) throws Exception {
        long deadline = SystemClock.elapsedRealtime() + timeoutMs;
        Pattern pattern = Pattern.compile("Generated OTP:\\s*(\\d{4})");

        while (SystemClock.elapsedRealtime() < deadline) {
            ParcelFileDescriptor pfd = InstrumentationRegistry
                    .getInstrumentation()
                    .getUiAutomation()
                    .executeShellCommand("logcat -d -s OTP_DEBUG:D");

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new ParcelFileDescriptor.AutoCloseInputStream(pfd)))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }

            Matcher matcher = pattern.matcher(output.toString());
            String latestOtp = null;

            while (matcher.find()) {
                latestOtp = matcher.group(1);
            }

            if (latestOtp != null) {
                return latestOtp;
            }

            SystemClock.sleep(250);
        }

        throw new AssertionError(
                "OTP not found. Check that OtpActivity logs "
                        + "'Generated OTP: ####' using the OTP_DEBUG tag."
        );
    }

    private void waitForView(int viewId, long timeoutMs) {
        long deadline = SystemClock.elapsedRealtime() + timeoutMs;

        while (SystemClock.elapsedRealtime() < deadline) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return;
            } catch (NoMatchingViewException | AssertionError ignored) {
                SystemClock.sleep(250);
            }
        }

        throw new AssertionError("Timed out waiting for view ID: " + viewId);
    }

    private void delay(long milliseconds) {
        SystemClock.sleep(milliseconds);
    }
}
