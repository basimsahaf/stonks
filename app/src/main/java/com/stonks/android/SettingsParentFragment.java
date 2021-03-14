package com.stonks.android;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsParentFragment extends Fragment {

    public SettingsParentFragment() {
        // Required empty public constructor
    }

    public static SettingsParentFragment newInstance(String param1, String param2) {
        SettingsParentFragment fragment = new SettingsParentFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_parent, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        /*this.getParentFragmentManager().beginTransaction()
                .replace(R.id.settings_screen, new SettingsFragment())
                .commit();*/
    }
}

