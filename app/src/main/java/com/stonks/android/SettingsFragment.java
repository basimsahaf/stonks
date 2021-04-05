package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.stonks.android.manager.SettingsManager;
import com.stonks.android.model.SettingsMode;

public class SettingsFragment extends BaseFragment {

    private ConstraintLayout settingsScreen,
            usernameSetting,
            emailChangeScreen,
            passwordSetting,
            passwordChangeScreen,
            trainingPeriodSetting,
            amountChangeScreen;
    private Button submitButton;
    private SettingsMode currentMode;
    private SettingsManager settingsManager;
    private TextInputEditText usernameField;

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

        settingsManager = settingsManager.getInstance(getContext());

        // default settings mode
        currentMode = SettingsMode.SETTINGS_HOME;

        settingsScreen = view.findViewById(R.id.settings_main);
        usernameSetting = view.findViewById(R.id.username_setting);
        emailChangeScreen = view.findViewById(R.id.username_change);
        passwordSetting = view.findViewById(R.id.password_setting);
        passwordChangeScreen = view.findViewById(R.id.password_change);
        trainingPeriodSetting = view.findViewById(R.id.money_setting);
        amountChangeScreen = view.findViewById(R.id.training_period_change);
        submitButton = view.findViewById(R.id.submit_button);
        usernameField = view.findViewById(R.id.username_input);

        usernameSetting.setOnClickListener(
                v -> {
                    currentMode = SettingsMode.USERNAME;
                    settingsScreen.setVisibility(View.GONE);
                    emailChangeScreen.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                });

        passwordSetting.setOnClickListener(
                v -> {
                    currentMode = SettingsMode.PASSWORD;
                    settingsScreen.setVisibility(View.GONE);
                    passwordChangeScreen.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                });

        trainingPeriodSetting.setOnClickListener(
                v -> {
                    currentMode = SettingsMode.TRAINING_PERIOD;
                    settingsScreen.setVisibility(View.GONE);
                    amountChangeScreen.setVisibility(View.VISIBLE);
                    submitButton.setVisibility(View.VISIBLE);
                    // change text and save button to red since it's a destructive action
                    submitButton.setText(getString(R.string.reset_training_period));
                    submitButton.setBackgroundColor(
                            ResourcesCompat.getColor(getResources(), R.color.red, null));
                });

        submitButton.setOnClickListener(
                v -> {
                    changeSettings();
                    emailChangeScreen.setVisibility(View.GONE);
                    passwordChangeScreen.setVisibility(View.GONE);
                    amountChangeScreen.setVisibility(View.GONE);
                    submitButton.setVisibility(View.GONE);
                    settingsScreen.setVisibility(View.VISIBLE);
                    // reset back to blue + save text case returning from change training period
                    submitButton.setText(getString(R.string.submit));
                    submitButton.setBackgroundColor(
                            ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
                });
    }

    private void changeSettings() {
        switch (currentMode) {
            case USERNAME:
                String newUsername = usernameField.getText().toString();
                // TODO: do username validation once LoginManager is written
                String toastText;
                if (settingsManager.updateUsername(newUsername)) {
                    toastText = getString(R.string.username_updated);
                } else {
                    toastText = getString(R.string.username_update_failed);
                }
                Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
