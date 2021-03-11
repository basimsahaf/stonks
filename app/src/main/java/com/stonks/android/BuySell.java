package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import org.w3c.dom.Text;

public class BuySell extends Fragment {

    HorizontalNumberPicker np;
    MaterialButton buyBtn, sellBtn, tradeBtn;
    TextView costValueLabel, availableLabel;

    enum Mode {
        BUY,
        SELL
    }

    Mode mode;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // force dark mode

        mode = Mode.SELL; // for now; we would get this info from the screen that triggers this

        return inflater.inflate(R.layout.fragment_buy_sell, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buyBtn = getView().findViewById(R.id.buy_mode_button);
        sellBtn = getView().findViewById(R.id.sell_mode_button);

        costValueLabel = getView().findViewById(R.id.cost_value_label);
        availableLabel = getView().findViewById(R.id.available);
        tradeBtn = getView().findViewById(R.id.trade_btn);

        switchView(mode);

        // we also need to eventually check if user is able to buy/sell so we can gray out the button accordingly

        buyBtn.setOnClickListener(myView -> {
            switchView(Mode.BUY);
        });
        sellBtn.setOnClickListener(myView -> {
            switchView(Mode.SELL);
        });

        final HorizontalNumberPicker np_channel_nr = (HorizontalNumberPicker) getView().findViewById(R.id.number_picker);

        int number = np_channel_nr.getValue();
    }

    // not sure what the visibility for this should be?
    void switchView(Mode mode) {
        if (mode == Mode.BUY) {
            buyBtn.setChecked(true);
            sellBtn.setChecked(false);
            costValueLabel.setText(getString(R.string.estimated_cost_label));
            availableLabel.setText(getString(R.string.available_to_trade_label));
            tradeBtn.setText(getString(R.string.buy_button_label));
        }
        else {
            sellBtn.setChecked(true);
            buyBtn.setChecked(false);
            costValueLabel.setText(getString(R.string.estimated_value_label));
            availableLabel.setText(getString(R.string.available_to_sell_label));
            tradeBtn.setText(getString(R.string.sell_button_label));
        }
    }
}
