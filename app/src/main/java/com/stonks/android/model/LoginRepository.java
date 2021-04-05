package com.stonks.android.model;

import android.content.Context;
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
            UserTable userTable = new UserTable(context);
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

    public boolean isBiometricsEnabled() {
        Result<LoggedInUser> biometricsUser = dataSource.getBiometricsUser();
        if (biometricsUser instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) biometricsUser).getData());
            return true;
        }
        return false;
    }

    public String getCurrentUser() {
        return this.user.getUserId();
    }
}
