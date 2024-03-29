package com.stonks.android.model;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.stonks.android.R;
import com.stonks.android.manager.LoginManager;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginManager loginManager;

    public LoginViewModel(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        Result<UserModel> result = loginManager.login(username, password);

        if (result instanceof Result.Success) {
            loginResult.setValue(new LoginResult(true));
        } else {
            Integer error = ((Result.Error) result).getError();
            loginResult.setValue(new LoginResult(error));
        }
    }

    public void signup(String username, String password) {
        Result<UserModel> result = loginManager.signUp(username, password);

        if (result instanceof Result.Success) {
            loginResult.setValue(new LoginResult(true));
        } else {
            Integer error = ((Result.Error) result).getError();
            loginResult.setValue(new LoginResult(error));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username) && !isPasswordValid(password)) {
            loginFormState.setValue(
                    new LoginFormState(R.string.invalid_username, R.string.invalid_password));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password_format));
        } else if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
