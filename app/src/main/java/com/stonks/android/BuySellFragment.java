package com.stonks.android;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.button.MaterialButton;
import com.stonks.android.model.TransactionMode;

// Todos:
// - check if user is able to buy/sell so we can gray out the button accordingly
// - cancel button onclick (close drawer/back)
// - small thing but styling the +/- buttons to be circular
// - calculate estimated cost/price

public class BuySellFragment extends Fragment {

    HorizontalNumberPicker numberPicker;
    MaterialButton buyBtn, sellBtn, tradeBtn;
    TextView costValueLabel, availableLabel;
    TransactionMode mode;

    public BuySellFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // force dark mode

        // for now; we would get this info from the screen that triggers this
        mode = TransactionMode.SELL;

        return inflater.inflate(R.layout.fragment_buy_sell, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buyBtn = getView().findViewById(R.id.buy_mode_button);
        sellBtn = getView().findViewById(R.id.sell_mode_button);
        numberPicker = getView().findViewById(R.id.number_picker);

        costValueLabel = getView().findViewById(R.id.cost_value_label);
        availableLabel = getView().findViewById(R.id.available_label);
        tradeBtn = getView().findViewById(R.id.trade_btn);

        switchView(mode);

        buyBtn.setOnClickListener(
                myView -> {
                    switchView(TransactionMode.BUY);
                });
        sellBtn.setOnClickListener(
                myView -> {
                    switchView(TransactionMode.SELL);
                });
    }

    private void switchView(TransactionMode mode) {
        if (mode == TransactionMode.BUY) {
            buyBtn.setChecked(true);
            sellBtn.setChecked(false);
            costValueLabel.setText(getString(R.string.estimated_cost_label));
            availableLabel.setText(getString(R.string.available_to_trade_label));
            tradeBtn.setText(getString(R.string.buy_button_label));
        } else {
            sellBtn.setChecked(true);
            buyBtn.setChecked(false);
            costValueLabel.setText(getString(R.string.estimated_value_label));
            availableLabel.setText(getString(R.string.available_to_sell_label));
            tradeBtn.setText(getString(R.string.sell_button_label));
        }
    }
}
