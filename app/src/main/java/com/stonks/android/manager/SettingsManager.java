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
        return userManager.getCurrentUsername();
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

    //
    //    public String getCurrentUser() {
    //        return this.user.getUserId();
    //    }
    //
    //    public Result<LoggedInUser> changeUsername(String newUsername) {
    //        Result<LoggedInUser> result = dataSource.changeUsername(this.user.getUserId(),
    // newUsername);
    //        if (result instanceof Result.Success) {
    //            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
    //        }
    //        return result;
    //    }
    //
    //    public boolean verifyCurrentPassword(String currentPassword) {
    //        return dataSource.verifyCurrentPassword(this.user.getUserId(), currentPassword);
    //    }
    //
    //    public Result<LoggedInUser> changePassword(String newPassword) {
    //        Result<LoggedInUser> result = dataSource.changePassword(this.user.getUserId(),
    // newPassword);
    //        if (result instanceof Result.Success) {
    //            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
    //        }
    //        return result;
    //    }
    //
    //    public boolean isCurrentUserBiometricsEnabled() {
    //        Result<LoggedInUser> biometricsUser = dataSource.getBiometricsUser();
    //        if (biometricsUser instanceof Result.Success) {
    //            return ((Result.Success<LoggedInUser>) biometricsUser)
    //                    .getData()
    //                    .getUserId()
    //                    .equals(this.user.getUserId());
    //        }
    //        return false;
    //    }
    //
    //    public boolean isBiometricsAvailableOnDevice() {
    //        Result<LoggedInUser> biometricsUser = dataSource.getBiometricsUser();
    //        return !(biometricsUser instanceof Result.Success);
    //    }
    //
    //    public boolean toggleBiometrics(boolean status) {
    //        Result<LoggedInUser> result = dataSource.toggleBiometrics(this.user.getUserId(),
    // status);
    //        return result instanceof Result.Success;
    //    }
    //
    //    public Result<LoggedInUser> changeTrainingAmount(float amount) {
    //        return dataSource.changeTrainingAmount(this.user.getUserId(), amount);
    //    }
}
