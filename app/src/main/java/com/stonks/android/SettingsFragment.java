package com.stonks.android;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SettingsFragment extends BaseFragment {

    private ConstraintLayout settingsScreen,
            emailSetting,
            emailChangeScreen,
            passwordSetting,
            passwordChangeScreen,
            trainingPeriodSetting,
            amountChangeScreen;
    private Button submitButton;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        settingsScreen = view.findViewById(R.id.settings_main);
        emailSetting = view.findViewById(R.id.email_setting);
        emailChangeScreen = view.findViewById(R.id.email_change);
        passwordSetting = view.findViewById(R.id.password_setting);
        passwordChangeScreen = view.findViewById(R.id.password_change);
        trainingPeriodSetting = view.findViewById(R.id.money_setting);
        amountChangeScreen = view.findViewById(R.id.training_period_change);
        submitButton = view.findViewById(R.id.submit_button);

        emailSetting.setOnClickListener(
                v -> {
                    settingsScreen.setVisibility(View.GONE);
                    emailChangeScreen.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                });

        passwordSetting.setOnClickListener(
                v -> {
                    settingsScreen.setVisibility(View.GONE);
                    passwordChangeScreen.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                });

        trainingPeriodSetting.setOnClickListener(
                v -> {
                    settingsScreen.setVisibility(View.GONE);
                    amountChangeScreen.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                    // change text and save button to red since it's a destructive action
                    submitButton.setText(getString(R.string.reset_training_period));
                    submitButton.setBackgroundColor(getResources().getColor(R.color.red));
                });

        submitButton.setOnClickListener(
                v -> {
                    emailChangeScreen.setVisibility(View.GONE);
                    passwordChangeScreen.setVisibility(View.GONE);
                    amountChangeScreen.setVisibility(View.GONE);
                    submitButton.setVisibility(View.GONE);
                    settingsScreen.setVisibility(View.VISIBLE);
                    // reset back to blue + save text case returning from change training period
                    submitButton.setText(getString(R.string.submit));
                    submitButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                });
    }
}
