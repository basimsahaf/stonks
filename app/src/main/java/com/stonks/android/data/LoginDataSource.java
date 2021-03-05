package com.stonks.android.data;

import com.stonks.android.data.model.LoggedInUser;
import com.stonks.android.storage.tables.AuthTable;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    final private AuthTable authTable;

    public LoginDataSource(AuthTable authTable) {
        this.authTable = authTable;
    }

    public Result<LoggedInUser> login(String username, String password) {

        try {

            boolean exists = authTable.checkIfUserExists(username);
            if (!exists) {
                return new Result.Error(new IOException("Error, user does not exist"));
            }

            boolean correctPassword = authTable.checkUsernamePassword(username, password);

            if (!correctPassword) {
                return new Result.Error(new IOException("Error, wrong password"));
            }

            LoggedInUser user =
                    new LoggedInUser(
                            username);
            return new Result.Success<>(user);

        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}