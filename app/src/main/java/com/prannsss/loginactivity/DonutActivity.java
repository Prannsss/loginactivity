package com.prannsss.loginactivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class DonutActivity extends Activity {

    private TextView donutView;
    private SeekBar speedSlider;
    private Spinner animationSpinner;

    private final Handler handler = new Handler();

    private final DonutRenderer donutRenderer =
            new DonutRenderer();

    private final MobiusRenderer mobiusRenderer =
            new MobiusRenderer();

    private double rotationA = 0;
    private double rotationB = 0;

    private double rotationSpeed = 0.07;

    private int selectedAnimation = 0;

    private boolean animationRunning = false;

    private final Runnable animation = new Runnable() {

        @Override
        public void run() {

            if (!animationRunning) {
                return;
            }

            if (selectedAnimation == 0) {

                donutView.setText(
                        donutRenderer.render(
                                rotationA,
                                rotationB
                        )
                );

            } else {

                donutView.setText(
                        mobiusRenderer.render(
                                rotationA,
                                rotationB
                        )
                );
            }

            rotationA += rotationSpeed;
            rotationB += rotationSpeed * 0.43;

            handler.postDelayed(this, 50);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donut);

        donutView = findViewById(R.id.donutView);
        speedSlider = findViewById(R.id.speedSlider);
        animationSpinner = findViewById(R.id.animationSpinner);

        Button logoutButton =
                findViewById(R.id.logoutButton);

        setupAnimationSpinner();
        setupSpeedSlider();

        logoutButton.setOnClickListener(
                view -> logout()
        );
    }

    private void setupAnimationSpinner() {

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        this,
                        R.array.animation_options,
                        android.R.layout.simple_spinner_item
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        animationSpinner.setAdapter(adapter);

        animationSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {

                        selectedAnimation = position;

                        rotationA = 0;
                        rotationB = 0;
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent
                    ) {
                    }
                }
        );
    }

    private void setupSpeedSlider() {

        speedSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {

                        rotationSpeed =
                                0.01
                                        + (progress / 100.0) * 0.19;
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }
                }
        );
    }

    private void startAnimation() {

        if (animationRunning) {
            return;
        }

        animationRunning = true;

        handler.post(animation);
    }

    private void stopAnimation() {

        animationRunning = false;

        handler.removeCallbacks(animation);
    }

    private void logout() {

        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(
                        R.string.logout_cancel,
                        null
                )
                .setPositiveButton(
                        R.string.logout_confirm,
                        (dialog, which) -> performLogout()
                )
                .show();
    }
    private void performLogout() {

        stopAnimation();

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
    @Override
    protected void onResume() {

        super.onResume();

        startAnimation();
    }

    @Override
    protected void onPause() {

        super.onPause();

        stopAnimation();
    }
}