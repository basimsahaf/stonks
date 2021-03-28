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

    final String TAG = this.getClass().getSimpleName();

    private Button loginButton;
    private TextInputLayout usernameField;
    private TextInputLayout passwordField;
    private TextView errorMessage;
    private LoginViewModel loginViewModel;
    private MaterialButton loginModeButton;
    private MaterialButton signUpModeButton;
    private boolean usernameChanged;
    private boolean passwordChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        UserTable userTable = new UserTable(this);
        Log.d(TAG, userTable.getDatabaseName());
        loginViewModel = new LoginViewModel(new LoginRepository(new LoginDataSource(userTable)));

        loginModeButton = findViewById(R.id.login_mode_button);
        signUpModeButton = findViewById(R.id.signup_mode_button);
        usernameField = findViewById(R.id.username_field);
        passwordField = findViewById(R.id.password_field);
        loginButton = findViewById(R.id.login_button);
        errorMessage = findViewById(R.id.error_message);

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
                                loginViewModel.login(
                                        usernameField.getEditText().getText().toString(),
                                        passwordField.getEditText().getText().toString());
                            }
                            return false;
                        });

        loginButton.setText(getString(R.string.login));
        loginButton.setOnClickListener(
                view -> {
                    String username = "";
                    String password = "";

                    if (usernameField.getEditText().getText() != null) {
                        username = usernameField.getEditText().getText().toString();
                    }

                    if (passwordField.getEditText().getText() != null) {
                        password = passwordField.getEditText().getText().toString();
                    }

                    Log.d(TAG, "Username/password pair: " + username + " -> " + password);

                    usernameField.getEditText().setText("");
                    passwordField.getEditText().setText("");
                    errorMessage.setVisibility(View.VISIBLE);
                    loginViewModel.login(username, password);
                });

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
        String welcome = getString(R.string.welcome) + model.getDisplayName();
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
                                Log.d(TAG, "Error username");
                                usernameField.setError(
                                        getString(loginFormState.getUsernameError()));
                            } else {
                                Log.d(TAG, "Valid username");
                                usernameField.setError(null);
                            }
                            if (passwordChanged && loginFormState.getPasswordError() != null) {
                                passwordField.setError(
                                        getString(loginFormState.getPasswordError()));
                            } else {
                                passwordField.setError(null);
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
                                Intent intent = new Intent(this, MainActivity.class);
                                startActivity(intent);
                            }
                            setResult(Activity.RESULT_OK);
                        });
        // errorMessage.setVisibility(View.VISIBLE);

    }

    private void switchView(AuthMode login) {
        CheckBox biometricsCheckbox = findViewById(R.id.biometric_checkbox);
        if (login == AuthMode.LOGIN) {
            loginModeButton.setChecked(true);
            loginButton.setText(getString(R.string.login));
            biometricsCheckbox.setVisibility(View.GONE);
        } else {
            signUpModeButton.setChecked(true);
            loginButton.setText(getString(R.string.create_account));
            biometricsCheckbox.setVisibility(View.VISIBLE);
        }
    }
}
