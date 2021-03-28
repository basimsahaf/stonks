package com.stonks.android.model;

import android.util.Log;
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
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            Log.d("Debug", "Returning success from model");
            loginResult.setValue(new LoginResult(new LoggedInUserView("Hello!")));
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void usernameChanged(String username) {
        Log.d("Debug", "Changing");
        if (!isUserNameValid(username)) {
            Log.d("Debug", "InValid");
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else {
            Log.d("Debug", "Valid");
            loginFormState.setValue(new LoginFormState(true));
            Log.d("Debug", "" + loginFormState.getValue().getPasswordError());
        }
    }

    public void passwordChanged(String password) {
        if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username) && !isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, R.string.invalid_password));
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
