package com.stonks.android.model;

import android.content.Context;
import android.util.Log;
import com.stonks.android.storage.UserTable;

/**
 * Class that requests authentication and user information from the remote data source and maintains
 * an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static volatile LoginRepository instance;
    private LoginDataSource dataSource;
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(Context context) {
        if (instance == null) {
            UserTable userTable = UserTable.getInstance(context);
            instance = new LoginRepository(new LoginDataSource(userTable));
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        Log.d("Tag", "Current user: " + user.getUserId());
        this.user = user;
    }

    public Result<LoggedInUser> login(String username, String password) {
        Result<LoggedInUser> result = dataSource.login(username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }

    public Result<LoggedInUser> signup(
            String username, String password, boolean isBiometricsEnabled) {
        Result<LoggedInUser> result = dataSource.signup(username, password, isBiometricsEnabled);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }

    public boolean initializeBiometricsUser() {
        Result<LoggedInUser> biometricsUser = dataSource.getBiometricsUser();
        if (biometricsUser instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) biometricsUser).getData());
            this.user.setBiometricsEnabled(true);
            return true;
        }
        return false;
    }

    // TODO: everything below this goes into the new UserManager

    public String getCurrentUser() {
        return this.user.getUserId();
    }

    public float getTotalAmountAvailable() {
        return dataSource.getTotalAmountAvailable(this.user.getUserId());
    }

    public Result<LoggedInUser> changeUsername(String newUsername) {
        Result<LoggedInUser> result = dataSource.changeUsername(this.user.getUserId(), newUsername);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }

    public boolean verifyCurrentPassword(String currentPassword) {
        return dataSource.verifyCurrentPassword(this.user.getUserId(), currentPassword);
    }

    public Result<LoggedInUser> changePassword(String newPassword) {
        Result<LoggedInUser> result = dataSource.changePassword(this.user.getUserId(), newPassword);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;
    }

    public boolean isCurrentUserBiometricsEnabled() {
        Result<LoggedInUser> biometricsUser = dataSource.getBiometricsUser();
        if (biometricsUser instanceof Result.Success) {
            return ((Result.Success<LoggedInUser>) biometricsUser)
                    .getData()
                    .getUserId()
                    .equals(this.user.getUserId());
        }
        return false;
    }

    public boolean isBiometricsAvailableOnDevice() {
        Result<LoggedInUser> biometricsUser = dataSource.getBiometricsUser();
        return !(biometricsUser instanceof Result.Success);
    }

    public boolean toggleBiometrics(boolean status) {
        Result<LoggedInUser> result = dataSource.toggleBiometrics(this.user.getUserId(), status);
        return result instanceof Result.Success;
    }

    public Result<LoggedInUser> changeTrainingAmount(float amount) {
        return dataSource.changeTrainingAmount(this.user.getUserId(), amount);
    }
}
