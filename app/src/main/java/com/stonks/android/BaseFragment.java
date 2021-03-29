package com.stonks.android;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import java.util.Objects;

public class BaseFragment extends Fragment {

    public ActionBar getActionBar() {
        return ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
