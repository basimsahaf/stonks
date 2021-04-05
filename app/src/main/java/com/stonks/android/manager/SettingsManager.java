package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.LoginRepository;

public class SettingsManager {
    SettingsManager settingsManager;
    LoginRepository loginRepository;

    private SettingsManager(Context context) {
        loginRepository = LoginRepository.getInstance(context);
    }

    public SettingsManager getInstance(Context context) {
        if (settingsManager == null) {
            settingsManager = new SettingsManager(context);
        }
        return settingsManager;
    }

    public boolean updateUsername(String newUsername) {
        return loginRepository.updateUsername(newUsername);
    }

    //    public boolean updatePassword(String newPassword) {
    //
    //    }
    //
    //    public boolean enableBiometrics() {
    //
    //    }
    //
    //    public boolean updateTrainingAmount(float amount) {
    //
    //    }
}
