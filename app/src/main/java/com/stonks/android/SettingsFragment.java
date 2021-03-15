package com.stonks.android;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SettingsFragment extends Fragment {

    ConstraintLayout settingsScreen;
    ConstraintLayout emailSetting;
    ConstraintLayout emailChangeScreen;
    ConstraintLayout passwordSetting;
    ConstraintLayout passwordChangeScreen;
    ConstraintLayout amountSetting;
    ConstraintLayout amountChangeScreen;
    Button submitButton;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_parent, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        settingsScreen = view.findViewById(R.id.settings_main);
        emailSetting = view.findViewById(R.id.email_setting);
        emailChangeScreen = view.findViewById(R.id.email_change);
        passwordSetting = view.findViewById(R.id.password_setting);
        passwordChangeScreen = view.findViewById(R.id.password_change);
        amountSetting = view.findViewById(R.id.money_setting);
        amountChangeScreen = view.findViewById(R.id.starting_amount_change);
        submitButton = view.findViewById(R.id.submit_button);

        emailSetting.setOnClickListener(v -> {
            settingsScreen.setVisibility(View.GONE);
            emailChangeScreen.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
        });

        passwordSetting.setOnClickListener(v -> {
            settingsScreen.setVisibility(View.GONE);
            passwordChangeScreen.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
        });

        amountSetting.setOnClickListener(v -> {
            settingsScreen.setVisibility(View.GONE);
            amountChangeScreen.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
        });

        submitButton.setOnClickListener(v -> {
            emailChangeScreen.setVisibility(View.GONE);
            passwordChangeScreen.setVisibility(View.GONE);
            amountChangeScreen.setVisibility(View.GONE);
            submitButton.setVisibility(View.GONE);
            settingsScreen.setVisibility(View.VISIBLE);
        });
    }
}

