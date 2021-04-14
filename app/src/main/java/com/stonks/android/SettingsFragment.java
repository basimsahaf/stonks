package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.stonks.android.manager.SettingsManager;
import com.stonks.android.model.LoggedInUser;
import com.stonks.android.model.Result;
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
    private TextInputEditText oldPassword;
    private TextInputEditText newPassword;
    private TextInputEditText trainingAmount;
    private TextView currentUsername;
    private MaterialCheckBox biometricsCheckBox;
    private TextView biometricsError;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        settingsManager = SettingsManager.getInstance(getContext());

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
        usernameField = view.findViewById(R.id.username_text_field);
        currentUsername = view.findViewById(R.id.current_username);
        oldPassword = view.findViewById(R.id.old_password_text);
        newPassword = view.findViewById(R.id.new_password_text);
        biometricsCheckBox = view.findViewById(R.id.biometrics_checkbox);
        biometricsError = view.findViewById(R.id.biometrics_error_message);
        trainingAmount = view.findViewById(R.id.amount_text);

        usernameField.setText(settingsManager.getCurrentUsername());
        currentUsername.setText(settingsManager.getCurrentUsername());
        biometricsError.setText(R.string.biometrics_in_use);

        if (settingsManager.isCurrentUserBiometricsEnabled()) {
            biometricsCheckBox.setChecked(true);
        } else if (settingsManager.isBiometricsAvailableOnDevice()) {
            biometricsCheckBox.setChecked(false);
        } else {
            biometricsCheckBox.setEnabled(false);
            biometricsError.setVisibility(View.VISIBLE);
        }

        setUpBiometricsListener();

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
                    if (changeSettings()) {
                        currentMode = SettingsMode.SETTINGS_HOME;
                        emailChangeScreen.setVisibility(View.GONE);
                        passwordChangeScreen.setVisibility(View.GONE);
                        amountChangeScreen.setVisibility(View.GONE);
                        submitButton.setVisibility(View.GONE);
                        settingsScreen.setVisibility(View.VISIBLE);
                        // reset back to blue + save text case returning from change training period
                        submitButton.setText(getString(R.string.submit));
                        submitButton.setBackgroundColor(
                                ResourcesCompat.getColor(
                                        getResources(), R.color.colorPrimary, null));
                    }
                });
    }

    public boolean shouldHandleBackPressed() {
        return isVisible() && settingsScreen.getVisibility() == View.GONE;
    }

    public void handleBackPressed() {
        emailChangeScreen.setVisibility(View.GONE);
        amountChangeScreen.setVisibility(View.GONE);
        passwordChangeScreen.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);

        settingsScreen.setVisibility(View.VISIBLE);
        currentMode = SettingsMode.SETTINGS_HOME;
    }

    private boolean changeSettings() {
        boolean status = false;
        switch (currentMode) {
            case USERNAME:
                status = changeUsername();
                break;

            case PASSWORD:
                status = changePassword();
                break;

            case TRAINING_PERIOD:
                status = changeTrainingAmount();
                break;
        }
        return status;
    }

    private boolean changeUsername() {
        boolean status = false;
        String newUsername = usernameField.getText().toString();
        // TODO: do username validation once LoginManager is written
        String toastText;
        Result<LoggedInUser> result = settingsManager.changeUsername(newUsername);
        if (result instanceof Result.Success) {
            toastText = getString(R.string.username_updated);
            status = true;
        } else {
            toastText = getString(((Result.Error) result).getError());
        }
        currentUsername.setText(settingsManager.getCurrentUsername());
        Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();
        return status;
    }

    private boolean changePassword() {
        boolean status = false;
        String currentPassword = oldPassword.getText().toString();
        String toastText = "";
        if (settingsManager.verifyCurrentPassword(currentPassword)) {
            status = true;
            String newPasswordText = newPassword.getText().toString();
            Result<LoggedInUser> result = settingsManager.changePassword(newPasswordText);

            // TODO: add password validation

            if (result instanceof Result.Success) {
                status = true;
                toastText = getString(R.string.password_updated);
            } else {
                toastText = getString(((Result.Error) result).getError());
            }

        } else {
            // show wrong pasword
            toastText = getString(R.string.invalid_current_password);
        }
        Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();
        return status;
    }

    private void setUpBiometricsListener() {
        biometricsCheckBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    settingsManager.toggleBiometrics(isChecked);
                });
    }

    private boolean changeTrainingAmount() {
        boolean status = false;
        String newAmount = trainingAmount.getText().toString();

        String toastText;
        Result<LoggedInUser> result;
        try {
            result = settingsManager.changeTrainingAmount(Float.parseFloat(newAmount));
            if (result instanceof Result.Success) {
                toastText = getString(R.string.training_period_updated);
                status = true;
            } else {
                toastText = getString(((Result.Error) result).getError());
            }
        } catch (Exception e) {
            toastText = getString(R.string.invalid_training_input);
        }

        Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();
        return status;
    }
}
