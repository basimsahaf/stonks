package com.stonks.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.widget.Toast;

import com.stonks.android.models.UserModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // following lines just provide an example of how the auth table works
        UserModel userModel = new UserModel("testuser3", "test123", "test", "user");
        AuthTable authTable = new AuthTable(this);
        boolean success = authTable.addUser(userModel);

        Toast.makeText(MainActivity.this, "User inserted " + success, Toast.LENGTH_LONG).show();

    }
}