package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.Result;
import com.stonks.android.model.UserModel;
import com.stonks.android.storage.UserTable;

public class UserManager {

    private static UserManager userManager;
    private UserModel currentUser;
    private final UserTable userTable;

    private UserManager(Context context) {
        userTable = UserTable.getInstance(context);
    }

    public static UserManager getInstance(Context context) {
        if (userManager == null) {
            userManager = new UserManager(context);
        }
        return userManager;
    }

    public UserModel getCurrentUser() {
        return this.currentUser;
    }

    public Result<UserModel> changeUsername(String newUsername) {
        Result<UserModel> result =
                userTable.changeUsername(this.currentUser.getUsername(), newUsername);
        if (result instanceof Result.Success) {
            setLoggedInUser((UserModel) ((Result.Success) result).getData());
        }
        return result;
    }

    public void setLoggedInUser(UserModel user) {
        this.currentUser = user;
    }

    public boolean verifyCurrentPassword(String currentPassword) {
        return this.currentUser.getPassword().equals(currentPassword);
    }

    public Result<UserModel> changePassword(String newPassword) {
        Result<UserModel> result =
                this.userTable.changePassword(this.currentUser.getUsername(), newPassword);
        if (result instanceof Result.Success) {
            setLoggedInUser((UserModel) ((Result.Success) result).getData());
        }
        return result;
    }

    public boolean isCurrentUserBiometricsEnabled() {
        Result<UserModel> biometricsUser = userTable.getBiometricsUser();
        if (biometricsUser instanceof Result.Success) {
            return ((Result.Success<UserModel>) biometricsUser)
                    .getData()
                    .getUsername()
                    .equals(this.currentUser.getUsername());
        }
        return false;
    }

    public boolean isBiometricsAvailableOnDevice() {
        Result<UserModel> biometricsUser = userTable.getBiometricsUser();
        return !(biometricsUser instanceof Result.Success);
    }

    public boolean toggleBiometrics(boolean status) {
        Result<UserModel> result =
                userTable.toggleBiometrics(this.currentUser.getUsername(), status);
        return result instanceof Result.Success;
    }

    public Result<UserModel> changeTrainingAmount(float amount) {
        return userTable.changeTrainingAmount(this.currentUser.getUsername(), amount);
    }
}
