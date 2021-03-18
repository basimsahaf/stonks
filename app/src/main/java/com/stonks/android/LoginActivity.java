package com.stonks.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.textfield.TextInputLayout;
import com.stonks.android.model.AuthMode;
import com.stonks.android.model.TransactionMode;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoginActivity extends BaseActivity {

    final String TAG = this.getClass().getSimpleName();

    Button loginButton;
    TextInputLayout usernameField;
    TextInputLayout passwordField;
    TextView errorMessage;
    private Button loginModeButton;
    private Button signUpModeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

<<<<<<< HEAD
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
=======
        loginModeButton = findViewById(R.id.login_mode_button);
        signUpModeButton = findViewById(R.id.signup_mode_button);



        loginModeButton.setOnClickListener(
                myView -> {
                    switchView(AuthMode.LOGIN);
                });
        signUpModeButton.setOnClickListener(
                myView -> {
                    switchView(AuthMode.SIGNUP);
                });
>>>>>>> Adding Signup tab

        loginButton = findViewById(R.id.login_button);
        usernameField = findViewById(R.id.username_field);
        passwordField = findViewById(R.id.password_field);
        errorMessage = findViewById(R.id.error_message);

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

                    // errorMessage.setVisibility(View.VISIBLE);

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                });
    }

    private void switchView(AuthMode login) {
        if (login == AuthMode.LOGIN) {
            // change layout
        } else {
            // change layout
        }
    }
}
