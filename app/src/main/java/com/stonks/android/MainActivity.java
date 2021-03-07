package com.stonks.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    final String TAG = this.getClass().getSimpleName();

    Button loginButton;
    TextInputLayout usernameField;
    TextInputLayout passwordField;
    TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        loginButton = findViewById(R.id.login_button);
        usernameField = findViewById(R.id.username_field);
        passwordField = findViewById(R.id.password_field);
        errorMessage = findViewById(R.id.error_message);

        loginButton.setText(getString(R.string.login));
        loginButton.setOnClickListener(view -> {
            String username = "";
            String password = "";

            if ( usernameField.getEditText().getText() != null ){
                username = usernameField.getEditText().getText().toString();
            }

            if ( passwordField.getEditText().getText() != null ) {
                password = passwordField.getEditText().getText().toString();
            }


            Log.d(TAG, "Username/password pair: " + username + " -> " + password);

            usernameField.getEditText().setText("");
            passwordField.getEditText().setText("");

            errorMessage.setVisibility(View.VISIBLE);
        });
    }
}