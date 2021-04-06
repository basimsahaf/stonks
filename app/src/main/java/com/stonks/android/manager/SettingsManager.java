package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.LoggedInUser;
import com.stonks.android.model.LoginRepository;
import com.stonks.android.model.Result;

public class SettingsManager {
    private static volatile SettingsManager settingsManager;
    private LoginRepository loginRepository;

    private SettingsManager(Context context) {
        loginRepository = LoginRepository.getInstance(context);
    }

    public static SettingsManager getInstance(Context context) {
        if (settingsManager == null) {
            settingsManager = new SettingsManager(context);
        }
        return settingsManager;
    }

    public Result<LoggedInUser> changeUsername(String newUsername) {
        return loginRepository.changeUsername(newUsername);
    }

    public String getCurrentUsername() {
        return loginRepository.getCurrentUser();
    }

    public boolean verifyCurrentPassword(String currentPassword) {
        return loginRepository.verifyCurrentPassword(currentPassword);
    }

    public Result<LoggedInUser> changePassword(String newPassword) {
        return loginRepository.changePassword(newPassword);
    }

    public boolean isCurrentUserBiometricsEnabled() {
        return loginRepository.isCurrentUserBiometricsEnabled();
    }

    //
    //    public boolean enableBiometrics() {
    //
    //    }
    //
    //    public boolean updateTrainingAmount(float amount) {
    //
    //    }
}
