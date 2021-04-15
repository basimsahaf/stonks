package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.Result;
import com.stonks.android.model.UserModel;
import com.stonks.android.storage.UserTable;

public class LoginManager {

    private static volatile LoginManager instance;
    private UserManager userManager;
    private UserTable userTable;

    // private constructor : singleton access
    private LoginManager(Context context) {
        this.userManager = UserManager.getInstance(context);
        this.userTable = UserTable.getInstance(context);
    }

    public static LoginManager getInstance(Context context) {
        if (instance == null) {
            instance = new LoginManager(context);
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
        return result;
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
