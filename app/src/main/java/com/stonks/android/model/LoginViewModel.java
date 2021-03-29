package com.stonks.android.model;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.stonks.android.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    public LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        Result<LoggedInUser> result = loginRepository.login(username, password);

        if (result instanceof Result.Success) {
            loginResult.setValue(new LoginResult(new LoggedInUserView("Hello!")));
        } else {
            Integer error = ((Result.Error) result).getError();
            loginResult.setValue(new LoginResult(error));
        }
    }

    public void signup(String username, String password, boolean isBiometricsEnabled) {
        Result<LoggedInUser> result =
                loginRepository.signup(username, password, isBiometricsEnabled);

        if (result instanceof Result.Success) {
            loginResult.setValue(new LoginResult(new LoggedInUserView("Hello!")));
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
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
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
