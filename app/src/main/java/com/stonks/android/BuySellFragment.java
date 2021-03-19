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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.google.android.material.button.MaterialButton;
import com.stonks.android.model.PickerLiveDataModel;
import com.stonks.android.model.TransactionMode;
import com.stonks.android.uicomponent.CustomSlideUpDrawer;
import com.stonks.android.uicomponent.HorizontalNumberPicker;
import java.util.Locale;

// Todos:
// - check if user is able to buy/sell so we can gray out the button accordingly
// - cancel button onclick (close drawer/back)

public class BuySellFragment extends Fragment {

    private HorizontalNumberPicker numberPicker;
    private MaterialButton buyBtn, sellBtn, tradeBtn;
    private TextView costValueLabel, availableLabel, costPrice, price, available;
    private TransactionMode mode;
    private float currentPrice, availableToTrade;
    private PickerLiveDataModel viewModel;
    private int numSharesOwned;
    private CustomSlideUpDrawer customSlideUpDrawer;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // force dark mode

        // for now; we would get this info from the screen that triggers this
        mode = TransactionMode.SELL;
        currentPrice = (float) 232.0;
        numSharesOwned = 103;
        availableToTrade = (float) 19032.23;

        return inflater.inflate(R.layout.fragment_buy_sell, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buyBtn = getView().findViewById(R.id.buy_mode_button);
        sellBtn = getView().findViewById(R.id.sell_mode_button);
        buyBtn.setOnClickListener(
                myView -> {
                    switchView(TransactionMode.BUY);
                });
        sellBtn.setOnClickListener(
                myView -> {
                    switchView(TransactionMode.SELL);
                });

        costValueLabel = getView().findViewById(R.id.cost_value_label);
        availableLabel = getView().findViewById(R.id.available_label);
        tradeBtn = getView().findViewById(R.id.trade_btn);
        available = getView().findViewById(R.id.available);

        // things dealing with the viewmodel
        price = getView().findViewById(R.id.price);
        price.setText(String.format(Locale.CANADA, "$%.2f", currentPrice));

        costPrice = getView().findViewById(R.id.cost_price);
        numberPicker = getView().findViewById(R.id.number_picker);
        viewModel = ViewModelProviders.of(this).get(PickerLiveDataModel.class);
        final Observer<Integer> observer =
                newValue -> {
                    float newEstimatedCost = newValue * currentPrice;
                    costPrice.setText(String.format(Locale.CANADA, "$%.2f", newEstimatedCost));
                };

        viewModel.getNumberOfStocks().observe(getViewLifecycleOwner(), observer);
        this.numberPicker.setModel(viewModel);

        this.customSlideUpDrawer = getActivity().findViewById(R.id.sliding_layout);

        tradeBtn.setOnClickListener(myView -> {
            this.customSlideUpDrawer.closeDrawer();
        });

        switchView(mode);
    }

    private void switchView(TransactionMode mode) {
        if (mode == TransactionMode.BUY) {
            buyBtn.setChecked(true);
            sellBtn.setChecked(false);
            costValueLabel.setText(getString(R.string.estimated_cost_label));
            availableLabel.setText(getString(R.string.available_to_trade_label));
            available.setText(String.format(Locale.CANADA, "$%.2f", availableToTrade));
            tradeBtn.setText(getString(R.string.buy_button_label));
        } else {
            sellBtn.setChecked(true);
            buyBtn.setChecked(false);
            costValueLabel.setText(getString(R.string.estimated_value_label));
            availableLabel.setText(getString(R.string.available_to_sell_label));
            available.setText(Integer.toString(numSharesOwned));
            tradeBtn.setText(getString(R.string.sell_button_label));
        }
    }
}
