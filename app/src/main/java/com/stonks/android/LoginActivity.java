package com.stonks.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.stonks.android.model.AuthMode;
import com.stonks.android.model.LoggedInUserView;
import com.stonks.android.model.LoginRepository;
import com.stonks.android.model.LoginViewModel;

public class LoginActivity extends BaseActivity {

    private final String TAG = this.getClass().getSimpleName();

    private Button loginButton;
    private TextInputLayout usernameField;
    private TextInputLayout passwordField;
    private TextView usernameErrorMessage;
    private TextView passwordErrorMessage;
    private LoginViewModel loginViewModel;
    private MaterialButton loginModeButton;
    private MaterialButton signUpModeButton;
    private TextView authErrorMessage;
    private boolean usernameChanged;
    private boolean passwordChanged;
    private AuthMode currentAuthMode;
    private boolean biometricsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        LoginRepository repo = LoginRepository.getInstance(getApplicationContext());
        loginViewModel = new LoginViewModel(repo);

        loginModeButton = findViewById(R.id.login_mode_button);
        signUpModeButton = findViewById(R.id.signup_mode_button);
        authErrorMessage = findViewById(R.id.auth_failed_error_message);
        usernameField = findViewById(R.id.username_field);
        passwordField = findViewById(R.id.password_field);
        loginButton = findViewById(R.id.login_button);
        usernameErrorMessage = findViewById(R.id.username_error_message);
        passwordErrorMessage = findViewById(R.id.password_error_message);

        loginModeButton.setOnClickListener(
                myView -> {
                    switchView(AuthMode.LOGIN);
                });
        signUpModeButton.setOnClickListener(
                myView -> {
                    switchView(AuthMode.SIGNUP);
                });

        setTextWatcher(usernameField);
        setTextWatcher(passwordField);
        // TODO: enable this later
                setLoginViewModelListeners();

        // auth triggers
        passwordField
                .getEditText()
                .setOnEditorActionListener(
                        (v, actionId, event) -> {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                authorize();
                            }
                            return false;
                        });

        loginButton.setOnClickListener(
                view -> {
                    // TODO: use this after testing is done
                                        authorize();

                    // this is just for testing purposes
//                    authorizeTestLogin();

                    usernameChanged = false;
                    passwordChanged = false;
                });

        // disable login button initially as no data is entered
        // TODO: change to disable once testing is done
        loginButton.setEnabled(false);

        // set error messages visibility to gone by default
        usernameErrorMessage.setVisibility(View.GONE);
        passwordErrorMessage.setVisibility(View.GONE);
        authErrorMessage.setVisibility(View.GONE);

        // disable the back button on the login page
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // TODO: do biometrics here

        // toggle login mode by default
        switchView(AuthMode.LOGIN);
    }

    private String getFieldText(TextInputLayout field) {
        if (field.getEditText().getText() != null) {
            return field.getEditText().getText().toString();
        }
        return "";
    }

    private void authorize() {
        String username = getFieldText(usernameField);
        String password = getFieldText(passwordField);
        authErrorMessage.setVisibility(View.GONE);

        // only login on non-empty username and password, otherwise show error
        if (!username.equals("") && !password.equals("")) {
            switch (currentAuthMode) {
                case LOGIN:
                    loginViewModel.login(username, password);
                    break;

                case SIGNUP:
                    loginViewModel.signup(username, password, biometricsEnabled);
                    break;
            }
        } else {
            showLoginFailed(R.string.login_incomplete);
        }
    }

    private void setTextWatcher(TextInputLayout field) {
        TextWatcher textWatcher =
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // ignore
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // ignore
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        authErrorMessage.setVisibility(View.GONE);
                        if (field == usernameField) {
                            usernameChanged = true;
                        } else {
                            passwordChanged = true;
                        }
                        loginViewModel.loginDataChanged(
                                usernameField.getEditText().getText().toString(),
                                passwordField.getEditText().getText().toString());
                    }
                };

        field.getEditText().addTextChangedListener(textWatcher);
    }

    private void showLoginSucceeded(LoggedInUserView model) {
        String welcome = getString(R.string.welcome);
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        authErrorMessage.setText(errorString);
        authErrorMessage.setVisibility(View.VISIBLE);
        Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(R.string.try_again),
                        Toast.LENGTH_SHORT)
                .show();
    }

    private void setLoginViewModelListeners() {
        loginViewModel
                .getLoginFormState()
                .observe(
                        this,
                        loginFormState -> {
                            if (loginFormState == null) {
                                return;
                            }
                            loginButton.setEnabled(loginFormState.isDataValid());

                            // display error if invalid username or password
                            boolean usernameState =
                                    setFieldState(
                                            usernameChanged,
                                            usernameErrorMessage,
                                            loginFormState.getUsernameError());

                            boolean passwordState =
                                    setFieldState(
                                            passwordChanged,
                                            passwordErrorMessage,
                                            loginFormState.getPasswordError());

                            // disable login button in case either of the fields is incorrect
                            if (usernameState && passwordState) {
                                loginButton.setEnabled(true);
                            } else {
                                loginButton.setEnabled(false);
                            }
                        });

        loginViewModel
                .getLoginResult()
                .observe(
                        this,
                        loginResult -> {
                            if (loginResult == null) {
                                return;
                            }
                            if (loginResult.getError() != null) {
                                showLoginFailed(loginResult.getError());
                            }
                            if (loginResult.getSuccess() != null) {
                                showLoginSucceeded(loginResult.getSuccess());
                            }
                            setResult(Activity.RESULT_OK);
                        });
    }

    private boolean setFieldState(boolean fieldChanged, TextView errorView, Integer error) {
        if (fieldChanged && error != null) {
            errorView.setText(error);
            errorView.setTextColor(getResources().getColor(R.color.red, getTheme()));
            errorView.setVisibility(View.VISIBLE);
            return false;
        } else {
            errorView.setVisibility(View.GONE);
            return true;
        }
    }

    private void switchView(AuthMode login) {
        authErrorMessage.setVisibility(View.GONE);
        if (login == AuthMode.LOGIN) {
            currentAuthMode = AuthMode.LOGIN;
            loginModeButton.setChecked(true);
            loginButton.setText(getString(R.string.login));
        } else {
            currentAuthMode = AuthMode.SIGNUP;
            signUpModeButton.setChecked(true);
            loginButton.setText(getString(R.string.create_account));
        }
    }

    // TODO: remove after testing
    private void authorizeTestLogin() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
