package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.LoginRepository;
import com.stonks.android.model.Result;
import com.stonks.android.model.UserModel;

public class SettingsManager {
    private static volatile SettingsManager settingsManager;
    private final LoginRepository loginRepository;
    private final UserManager userManager;

    private SettingsManager(Context context) {
        loginRepository = LoginRepository.getInstance(context);
        userManager = UserManager.getInstance(context);
    }

    public static SettingsManager getInstance(Context context) {
        if (settingsManager == null) {
            settingsManager = new SettingsManager(context);
        }
        return settingsManager;
    }

    public Result<UserModel> changeUsername(String newUsername) {
        return userManager.changeUsername(newUsername);
    }

    public String getCurrentUsername() {
        return userManager.getCurrentUser().getUsername();
    }

    public boolean verifyCurrentPassword(String currentPassword) {
        return userManager.verifyCurrentPassword(currentPassword);
    }

    public Result<UserModel> changePassword(String newPassword) {
        return userManager.changePassword(newPassword);
    }

    public boolean isCurrentUserBiometricsEnabled() {
        return userManager.isCurrentUserBiometricsEnabled();
    }

    public boolean isBiometricsAvailableOnDevice() {
        return userManager.isBiometricsAvailableOnDevice();
    }

    public boolean toggleBiometrics(boolean status) {
        return userManager.toggleBiometrics(status);
    }

    public Result<UserModel> changeTrainingAmount(float amount) {
        return userManager.changeTrainingAmount(amount);
    }
}
