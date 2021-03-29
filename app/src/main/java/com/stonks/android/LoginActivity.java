package com.stonks.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.stonks.android.model.AuthMode;
import com.stonks.android.model.LoggedInUserView;
import com.stonks.android.model.LoginDataSource;
import com.stonks.android.model.LoginRepository;
import com.stonks.android.model.LoginViewModel;
import com.stonks.android.storage.UserTable;

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
    private boolean usernameChanged;
    private boolean passwordChanged;
    private AuthMode currentAuthMode;
    private CheckBox biometricCheckbox;
    private boolean biometricsEnabled;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        UserTable userTable = new UserTable(this);
        LoginRepository repo = LoginRepository.getInstance(new LoginDataSource(userTable));
        loginViewModel = new LoginViewModel(repo);

        loginModeButton = findViewById(R.id.login_mode_button);
        signUpModeButton = findViewById(R.id.signup_mode_button);
        usernameField = findViewById(R.id.username_field);
        passwordField = findViewById(R.id.password_field);
        loginButton = findViewById(R.id.login_button);
        biometricCheckbox = findViewById(R.id.biometric_checkbox);
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
        setLoginViewModelListeners();

        passwordField
                .getEditText()
                .setOnEditorActionListener(
                        (v, actionId, event) -> {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                loadingProgressBar.setVisibility(View.VISIBLE);
                                switch (currentAuthMode) {
                                    case LOGIN:
                                        loginViewModel.login(
                                                usernameField.getEditText().getText().toString(),
                                                passwordField.getEditText().getText().toString());
                                        break;

                                    case SIGNUP:
                                        loginViewModel.signup(
                                                usernameField.getEditText().getText().toString(),
                                                passwordField.getEditText().getText().toString(),
                                                biometricsEnabled);
                                        break;
                                }
                            }
                            return false;
                        });

        loginButton.setText(getString(R.string.login));
        loginButton.setOnClickListener(
                view -> {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    String username = "";
                    String password = "";

                    if (usernameField.getEditText().getText() != null) {
                        username = usernameField.getEditText().getText().toString();
                    }

                    if (passwordField.getEditText().getText() != null) {
                        password = passwordField.getEditText().getText().toString();
                    }

                    Log.d(TAG, "Username/password pair: " + username + " -> " + password);

                    usernameChanged = false;
                    passwordChanged = false;

                    switch (currentAuthMode) {
                        case LOGIN:
                            loginViewModel.login(username, password);
                            break;

                        case SIGNUP:
                            loginViewModel.signup(username, password, biometricsEnabled);
                            break;
                    }
                });

        biometricCheckbox.setOnClickListener(
                v -> {
                    biometricsEnabled = biometricCheckbox.isChecked();
                });

        // set progress bar visibility to gone by default
        usernameErrorMessage.setVisibility(View.GONE);
        passwordErrorMessage.setVisibility(View.GONE);

        // disable the back button on the login page
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // toggle login mode by default
        switchView(AuthMode.LOGIN);
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
        startActivity(intent);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
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
                            if (usernameChanged && loginFormState.getUsernameError() != null) {
                                usernameErrorMessage.setText(loginFormState.getUsernameError());
                                usernameErrorMessage.setTextColor(
                                        getResources().getColor(R.color.red, getTheme()));
                                usernameErrorMessage.setVisibility(View.VISIBLE);
                            } else {
                                usernameErrorMessage.setVisibility(View.GONE);
                            }
                            if (passwordChanged && loginFormState.getPasswordError() != null) {
                                passwordErrorMessage.setText(loginFormState.getPasswordError());
                                passwordErrorMessage.setTextColor(
                                        getResources().getColor(R.color.red, getTheme()));
                                passwordErrorMessage.setVisibility(View.VISIBLE);
                            } else {
                                passwordErrorMessage.setVisibility(View.GONE);
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
                            loadingProgressBar.setVisibility(View.GONE);
                            if (loginResult.getError() != null) {
                                showLoginFailed(loginResult.getError());
                            }
                            if (loginResult.getSuccess() != null) {
                                showLoginSucceeded(loginResult.getSuccess());
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                            }
                            setResult(Activity.RESULT_OK);
                        });
    }

    private void switchView(AuthMode login) {
        CheckBox biometricsCheckbox = findViewById(R.id.biometric_checkbox);
        if (login == AuthMode.LOGIN) {
            currentAuthMode = AuthMode.LOGIN;
            loginModeButton.setChecked(true);
            loginButton.setText(getString(R.string.login));
            biometricsCheckbox.setVisibility(View.GONE);
        } else {
            currentAuthMode = AuthMode.SIGNUP;
            signUpModeButton.setChecked(true);
            loginButton.setText(getString(R.string.create_account));
            biometricsCheckbox.setVisibility(View.VISIBLE);
        }
    }
}
