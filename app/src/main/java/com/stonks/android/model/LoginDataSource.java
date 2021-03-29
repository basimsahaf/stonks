package com.stonks.android.model;

import com.stonks.android.storage.UserTable;
import java.io.IOException;

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
                return new Result.Error(new IOException("Error, user does not exist"));
            }

            boolean correctPassword = userTable.checkUsernamePassword(username, password);

            if (!correctPassword) {
                return new Result.Error(new IOException("Error, wrong password"));
            }

            LoggedInUser user = new LoggedInUser(username);
            return new Result.Success<>(user);

        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public Result<LoggedInUser> signup(String username, String password) {

        try {
            boolean exists = userTable.checkIfUserExists(username);
            if (exists) {
                return new Result.Error(new IOException("Error, user already exists"));
            }

            UserModel userModel = new UserModel(username, password);
            boolean userAdded = userTable.addUser(userModel);

            if (!userAdded) {
                return new Result.Error(new IOException("Signup failed"));
            }

            LoggedInUser user = new LoggedInUser(username);
            return new Result.Success<>(user);

        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
