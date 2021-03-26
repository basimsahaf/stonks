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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.textfield.TextInputLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        UserTable userTable = new UserTable(this);
        loginViewModel = new LoginViewModel(new LoginRepository(new LoginDataSource(userTable)));

        loginButton = findViewById(R.id.login_button);
        usernameField = findViewById(R.id.username_field);
        passwordField = findViewById(R.id.password_field);
        errorMessage = findViewById(R.id.error_message);

        setLoginViewModelListeners();
        setTextWatcher(usernameField);
        setTextWatcher(passwordField);

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
    }

    private void setTextWatcher(TextInputLayout field) {

        TextWatcher textWatcher = new TextWatcher() {
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
                    loginViewModel.usernameChanged(
                            field.getEditText().getText().toString());
                } else {
                    loginViewModel.passwordChanged(
                            field.getEditText().getText().toString());
                }
            }




        };

        field.getEditText().addTextChangedListener(textWatcher);
    }

    private void updateUiWithUser(LoggedInUserView model) {
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
                            if (loginFormState.getUsernameError() != null) {
                                Log.d(TAG, "Error username");
                                usernameField.setError(
                                        getString(loginFormState.getUsernameError()));
                                loginButton.setClickable(false);
                            } else {
                                usernameField.setError(null);
//                                loginButton.setClickable(true);
                            }
                            if (loginFormState.getPasswordError() != null) {
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
                                updateUiWithUser(loginResult.getSuccess());
                            }
                            setResult(Activity.RESULT_OK);
                        });
    }
}
