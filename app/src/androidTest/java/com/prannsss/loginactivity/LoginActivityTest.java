
package com.prannsss.loginactivity;

import android.os.SystemClock;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private static final String PASSWORD = "Testing@123";
    private static final String WRONG_PASSWORD = "WrongPassword123!";
    private static final long TIMEOUT_MS = 30000;
    private static final long ACTION_DELAY_MS = 2500;

    // Keep the scenario reference; do not explicitly close it.
    private static ActivityScenario<LoginActivity> loginScenario;

    @Test
    public void register_thenInvalidLogin_thenCorrectLogin_andVerifyOtp()
            throws Exception {

        String email = createUniqueEmail();

        loginScenario = ActivityScenario.launch(LoginActivity.class);
        delay();

        // 1. Open Signup.
        onView(withId(R.id.signupButton)).perform(click());
        delay();

        // 2. Register a new account.
        onView(withId(R.id.emailInput)).perform(replaceText(email));
        delay();

        onView(withId(R.id.confirmEmailInput)).perform(replaceText(email));
        delay();

        onView(withId(R.id.passwordInput)).perform(replaceText(PASSWORD));
        delay();

        onView(withId(R.id.confirmPasswordInput))
                .perform(replaceText(PASSWORD), closeSoftKeyboard());
        delay();

        onView(withId(R.id.signupButton)).perform(click());
        delay();

        waitForView(R.id.usernameInput, 10000);
        delay();

        // 3. Attempt login with the wrong password.
        onView(withId(R.id.usernameInput)).perform(replaceText(email));
        delay();

        onView(withId(R.id.passwordInput))
                .perform(replaceText(WRONG_PASSWORD), closeSoftKeyboard());
        delay();

        onView(withId(R.id.loginButton)).perform(click());
        delay();

        // Confirm the login screen remains visible.
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        delay();

        // 4. Enter the correct password.
        onView(withId(R.id.passwordInput))
                .perform(replaceText(PASSWORD), closeSoftKeyboard());
        delay();

        onView(withId(R.id.loginButton)).perform(click());
        delay();

        // 5. Wait for the OTP screen.
        waitForView(R.id.otpInput1, TIMEOUT_MS);
        delay();

        // 6. Capture the OTP from Logcat.
        String otp = captureOtpFromLogcat(TIMEOUT_MS);
        delay();

        // 7. Enter the OTP.
        enterOtp(otp);
        delay();

        // 8. Wait for MainDashboardActivity.
        waitForView(R.id.dashboardTitle, 15000);
        delay();

        onView(withId(R.id.dashboardTitle)).check(matches(isDisplayed()));

        // Leave the app on MainDashboardActivity.
        // Do not call loginScenario.close() or finish().
    }

    private void delay() {
        SystemClock.sleep(ACTION_DELAY_MS);
    }

    private String createUniqueEmail() {
        String timestamp = new SimpleDateFormat(
                "yyyyMMddHHmmss", Locale.US
        ).format(new Date());

        return "espresso" + timestamp + "@example.com";
    }

    private void enterOtp(String otp) {
        if (otp == null || !otp.matches("\\d{4}")) {
            throw new AssertionError(
                    "Could not capture a valid four-digit OTP."
            );
        }

        onView(withId(R.id.otpInput1))
                .perform(replaceText(otp.substring(0, 1)));
        delay();

        onView(withId(R.id.otpInput2))
                .perform(replaceText(otp.substring(1, 2)));
        delay();

        onView(withId(R.id.otpInput3))
                .perform(replaceText(otp.substring(2, 3)));
        delay();

        onView(withId(R.id.otpInput4))
                .perform(replaceText(otp.substring(3, 4)),
                        closeSoftKeyboard());
        delay();

        onView(withId(R.id.verifyOtpButton)).perform(click());
        delay();
    }

    private String captureOtpFromLogcat(long timeoutMs) throws Exception {
        long deadline = SystemClock.elapsedRealtime() + timeoutMs;

        Pattern pattern = Pattern.compile(
                "Generated OTP:\\s*(\\d{4})"
        );

        while (SystemClock.elapsedRealtime() < deadline) {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "logcat", "-d", "-s", "OTP_DEBUG:D", "*:S"
            });

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            } finally {
                process.destroy();
            }

            Matcher matcher = pattern.matcher(output.toString());
            String otp = null;

            while (matcher.find()) {
                otp = matcher.group(1);
            }

            if (otp != null) {
                return otp;
            }

            SystemClock.sleep(250);
        }

        throw new AssertionError(
                "OTP not found in Logcat. Expected: Generated OTP: 1234"
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

        throw new AssertionError(
                "Timed out waiting for view ID: " + viewId
        );
    }
}
