package com.stonks.android.model;

import com.stonks.android.R;
import com.stonks.android.storage.UserTable;

/** Class that handles authentication w/ login credentials and retrieves user information. */
public class LoginDataSource {
    private final UserTable userTable;

    public LoginDataSource(UserTable userTable) {
        this.userTable = userTable;
    }

    public Result<LoggedInUser> login(String username, String password) {

        try {
            boolean exists = userTable.checkIfUserExists(username);
            if (!exists) {
                return new Result.Error(R.string.user_does_not_exist);
            }

            boolean correctPassword = userTable.checkUsernamePassword(username, password);

            if (!correctPassword) {
                return new Result.Error(R.string.invalid_password);
            }

            LoggedInUser user = new LoggedInUser(username);
            return new Result.Success<>(user);

        } catch (Exception e) {
            return new Result.Error(R.string.internal_server_error);
        }
    }

    public Result<LoggedInUser> signup(
            String username, String password, boolean isBiometricsEnabled) {

        try {
            boolean exists = userTable.checkIfUserExists(username);
            if (exists) {
                return new Result.Error(R.string.user_exists);
            }

            UserModel userModel = new UserModel(username, password, isBiometricsEnabled);
            boolean userAdded = userTable.addUser(userModel);

            if (!userAdded) {
                return new Result.Error(R.string.internal_server_error);
            }

            LoggedInUser user = new LoggedInUser(username);
            return new Result.Success<>(user);

        } catch (Exception e) {
            return new Result.Error(R.string.internal_server_error);
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }

    public float getTotalAmountAvailable(String username) {
        return userTable.getTotalAmountAvailable(username);
    }

    public Result<LoggedInUser> changeUsername(String oldUsername, String newUsername) {
        return userTable.changeUsername(oldUsername, newUsername);
    }

    public boolean verifyCurrentPassword(String currentUsername, String currentPassword) {
        return userTable.verifyCurrentPassword(currentUsername, currentPassword);
    }

    public Result<LoggedInUser> changePassword(String username, String newPassword) {
        return userTable.changePassword(username, newPassword);
    }

    public Result<LoggedInUser> getBiometricsUser() {
        try {
            return userTable.getBiometricsUser();
        } catch (Exception e) {
            return new Result.Error(R.string.internal_server_error);
        }
    }

    public Result<LoggedInUser> toggleBiometrics(String username, boolean status) {
        try {
            return userTable.toggleBiometrics(username, status);
        } catch (Exception e) {
            return new Result.Error(R.string.internal_server_error);
        }
    }

    public Result<LoggedInUser> changeTrainingAmount(String username, float amount) {
        try {
            return userTable.changeTrainingBalance(username, amount);
        } catch (Exception e) {
            return new Result.Error(R.string.internal_server_error);
        }
    }
}
