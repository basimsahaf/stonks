package com.stonks.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricPrompt;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.stonks.android.manager.LoginManager;
import com.stonks.android.manager.UserManager;
import com.stonks.android.model.AuthMode;
import com.stonks.android.model.LoginViewModel;
import com.stonks.android.model.UserModel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends BaseActivity {

    private final String TAG = this.getClass().getSimpleName();

    private Button loginButton;
    private Button biometricsButton;
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
    private BiometricPrompt.PromptInfo promptInfo;
    private BiometricPrompt biometricPrompt;
    private LoginManager loginManager;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        loginManager = LoginManager.getInstance(getApplicationContext());
        loginViewModel = new LoginViewModel(loginManager);

        loginModeButton = findViewById(R.id.login_mode_button);
        signUpModeButton = findViewById(R.id.signup_mode_button);
        authErrorMessage = findViewById(R.id.auth_failed_error_message);
        usernameField = findViewById(R.id.username_field);
        passwordField = findViewById(R.id.password_field);
        loginButton = findViewById(R.id.login_button);
        biometricsButton = findViewById(R.id.biometrics_button);
        usernameErrorMessage = findViewById(R.id.username_error_message);
        passwordErrorMessage = findViewById(R.id.password_error_message);

        setButtonListeners();
        setTextWatcher(usernameField);
        setTextWatcher(passwordField);
        setLoginViewModelListeners();

        // disable login button initially as no data is entered
        loginButton.setEnabled(false);

        // set error messages visibility to gone by default
        usernameErrorMessage.setVisibility(View.GONE);
        passwordErrorMessage.setVisibility(View.GONE);
        authErrorMessage.setVisibility(View.GONE);

        // disable the back button on the login page
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // toggle login mode by default
        switchView(AuthMode.LOGIN);

        if (loginManager.initializeBiometricsUser()) {
            biometricsButton.setVisibility(View.VISIBLE);
            authorizeViaBiometrics();
        } else {
            biometricsButton.setVisibility(View.GONE);
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

    private void authorizeViaForm() {
        String username = getFieldText(usernameField);
        String password = getFieldText(passwordField);
        authErrorMessage.setVisibility(View.GONE);

        Log.d(TAG, "Form login");
        // only login on non-empty username and password, otherwise show error
        if (!username.equals("") && !password.equals("")) {
            switch (currentAuthMode) {
                case LOGIN:
                    loginViewModel.login(username, password);
                    break;

                case SIGNUP:
                    loginViewModel.signup(username, password);
                    break;
            }
        } else {
            showLoginFailed(R.string.login_incomplete);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void authorizeViaBiometrics() {
        UserModel currentUser = UserManager.getInstance(getApplicationContext()).getCurrentUser();
        setupBiometrics(currentUser.getUsername());
        biometricPrompt.authenticate(promptInfo);
        usernameField.getEditText().setText(currentUser.getUsername());
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void setupBiometrics(String username) {
        promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle(String.format("Welcome %s", username))
                        .setSubtitle("Sign in to Stonks")
                        .setDescription("Sign in via biometrics")
                        .setNegativeButtonText("Cancel")
                        .build();

        Executor executor = Executors.newSingleThreadExecutor();
        biometricPrompt =
                new BiometricPrompt(
                        LoginActivity.this,
                        executor,
                        new BiometricPrompt.AuthenticationCallback() {
                            @Override
                            public void onAuthenticationError(
                                    int errorCode, @NonNull CharSequence errString) {
                                super.onAuthenticationError(errorCode, errString);
                                Log.d(TAG, "An unrecoverable error occurred");
                            }

                            @Override
                            public void onAuthenticationSucceeded(
                                    @NonNull BiometricPrompt.AuthenticationResult result) {
                                super.onAuthenticationSucceeded(result);
                                showLoginSucceeded();
                                Log.d(TAG, "Fingerprint recognised successfully");
                            }

                            @Override
                            public void onAuthenticationFailed() {
                                super.onAuthenticationFailed();
                                Log.d(TAG, "Fingerprint not recognised");
                            }
                        });
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

    private void showLoginSucceeded() {
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
                            if (loginResult.getSuccess()) {
                                Toast.makeText(
                                                getApplicationContext(),
                                                R.string.welcome,
                                                Toast.LENGTH_LONG)
                                        .show();
                                showLoginSucceeded();
                            }
                            setResult(Activity.RESULT_OK);
                        });
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void setButtonListeners() {
        loginModeButton.setOnClickListener(
                myView -> {
                    switchView(AuthMode.LOGIN);
                });
        signUpModeButton.setOnClickListener(
                myView -> {
                    switchView(AuthMode.SIGNUP);
                });

        loginButton.setOnClickListener(
                view -> {
                    authorizeViaForm();
                    usernameChanged = false;
                    passwordChanged = false;
                });

        biometricsButton.setOnClickListener(v -> authorizeViaBiometrics());
    }

    private String getFieldText(TextInputLayout field) {
        if (field.getEditText().getText() != null) {
            return field.getEditText().getText().toString();
        }
        return "";
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

    // TODO: remove after testing
    private void authorizeTestLogin() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
