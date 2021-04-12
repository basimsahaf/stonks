package com.stonks.android.model;

import android.content.Context;
import com.stonks.android.manager.UserManager;
import com.stonks.android.storage.UserTable;

public class LoginRepository {

    private static volatile LoginRepository instance;
    private UserManager userManager;
    private UserTable userTable;

    // private constructor : singleton access
    private LoginRepository(Context context) {
        this.userManager = UserManager.getInstance(context);
        this.userTable = UserTable.getInstance(context);
    }

    public static LoginRepository getInstance(Context context) {
        if (instance == null) {
            instance = new LoginRepository(context);
        }
        return instance;
    }

    private void setLoggedInUser(UserModel user) {
        userManager.setLoggedInUser(user);
    }

    public Result<UserModel> login(String username, String password) {
        Result<UserModel> result = userTable.login(username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<UserModel>) result).getData());
        }
        return userTable.login(username, password);
    }

    public Result<UserModel> signUp(String username, String password) {
        Result<UserModel> result = userTable.signUp(username, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<UserModel>) result).getData());
        }
        return result;
    }

    public boolean initializeBiometricsUser() {
        Result<UserModel> biometricsUser = userTable.getBiometricsUser();
        if (biometricsUser instanceof Result.Success) {
            setLoggedInUser(((Result.Success<UserModel>) biometricsUser).getData());
            return true;
        }
        return false;
    }
}
