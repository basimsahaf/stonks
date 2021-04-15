package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.google.android.material.button.MaterialButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.databinding.FragmentBuySellBinding;
import com.stonks.android.manager.BuySellManager;
import com.stonks.android.manager.StockManager;
import com.stonks.android.model.LoginRepository;
import com.stonks.android.model.PickerLiveDataModel;
import com.stonks.android.model.StockData;
import com.stonks.android.model.TransactionMode;
import com.stonks.android.uicomponent.HorizontalNumberPicker;
import com.stonks.android.utility.Formatters;
import java.time.LocalDateTime;
import java.util.Locale;

// Todos:
// - check if user is able to buy/sell so we can gray out the button accordingly
// - cancel button onclick (close drawer/back)

public class BuySellFragment extends Fragment {
    static final String TRANSACTION_MODE_ARG = "transactionMode";
    static final String STOCK_DATA_ARG = "stockData";
    private final int PICKER_MAX = 10000;
    private final String TAG = getClass().getCanonicalName();
    private HorizontalNumberPicker numberPicker;
    private MaterialButton buyBtn, sellBtn, tradeBtn;
    private TextView stock_symbol,
            companyName,
            costValueLabel,
            availableLabel,
            costPrice,
            price,
            available,
            errorMessage;
    private TransactionMode mode;
    private float currentPrice;
    private MutableLiveData<Float> availableToTrade;
    private MutableLiveData<Integer> numSharesOwned;
    private PickerLiveDataModel viewModel;
    private SlidingUpPanelLayout slidingUpPanel;
    private LoginRepository repo;
    private StockData stockData;
    private BuySellManager buySellManager;
    private StockManager stockManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // force dark mode

        FragmentBuySellBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_buy_sell, container, false);

        Bundle arg = getArguments();
        mode = (TransactionMode) arg.getSerializable(TRANSACTION_MODE_ARG);
        stockData = (StockData) arg.getSerializable(STOCK_DATA_ARG);
        currentPrice = stockData.getCurrentPrice();
        stockManager = StockManager.getInstance(getContext());
        binding.setStock(this.stockManager.getStockData());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();

        slidingUpPanel = mainActivity.getSlidingUpPanel();
        // TODO: Set custom sliding drawer height
        slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

        buyBtn = getView().findViewById(R.id.buy_mode_button);
        sellBtn = getView().findViewById(R.id.sell_mode_button);
        MaterialButton cancelBtn = getView().findViewById(R.id.cancel_btn);
        buyBtn.setOnClickListener(
                myView -> {
                    switchView(TransactionMode.BUY);
                });
        sellBtn.setOnClickListener(
                myView -> {
                    switchView(TransactionMode.SELL);
                });

        stock_symbol = getView().findViewById(R.id.stock_symbol);
        companyName = getView().findViewById(R.id.company_name);
        costValueLabel = getView().findViewById(R.id.cost_value_label);
        availableLabel = getView().findViewById(R.id.available_label);
        tradeBtn = getView().findViewById(R.id.trade_btn);
        available = getView().findViewById(R.id.available);
        costPrice = getView().findViewById(R.id.cost_price);
        price = getView().findViewById(R.id.price);
        numberPicker = getView().findViewById(R.id.number_picker);
        errorMessage = getView().findViewById(R.id.buy_sell_error);

        // initializing fields retrieved from stock page
        stock_symbol.setText(stockData.getSymbol());
        companyName.setText(stockData.getCompanyName());
        price.setText(Formatters.formatPrice(currentPrice));
        repo = LoginRepository.getInstance(getContext());
        buySellManager = BuySellManager.getInstance(getContext());
        availableToTrade = new MutableLiveData<>(repo.getTotalAmountAvailable());
        numSharesOwned =
                new MutableLiveData<>(
                        buySellManager.getStocksOwnedBySymbol(
                                repo.getCurrentUser(), stockData.getSymbol()));

        viewModel = ViewModelProviders.of(this).get(PickerLiveDataModel.class);
        final Observer<Integer> numStocksPicked =
                newValue -> {
                    float newEstimatedCost = newValue * currentPrice;
                    costPrice.setText(String.format(Locale.CANADA, "$%.2f", newEstimatedCost));
                    if (newEstimatedCost > availableToTrade.getValue()) {
                        disableTradeButton();
                        errorMessage.setVisibility(View.VISIBLE);
                    } else {
                        enableTradeButton();
                        errorMessage.setVisibility(View.GONE);
                    }
                };

        viewModel.getNumberOfStocks().observe(getViewLifecycleOwner(), numStocksPicked);
        this.numberPicker.setModel(viewModel);

        // setting observers for the number of shares owned and total amount available as its not in
        // data binding
        final Observer<Float> totalAmountAvailable =
                newValue -> {
                    if (mode == TransactionMode.BUY) {
                        available.setText(Formatters.formatPrice(newValue));
                    }
                };

        availableToTrade.observe(getViewLifecycleOwner(), totalAmountAvailable);

        final Observer<Integer> numStocksAvailable =
                newValue -> {
                    if (mode == TransactionMode.SELL) {
                        available.setText(String.format("%s", newValue));
                    }
                };

        numSharesOwned.observe(getViewLifecycleOwner(), numStocksAvailable);

        tradeBtn.setOnClickListener(
                myView -> {
                    handleTransaction();
                });

        cancelBtn.setOnClickListener(
                myView -> {
                    this.slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                });

        switchView(mode);
    }

    private void enableTradeButton() {
        tradeBtn.setEnabled(true);
        tradeBtn.setBackgroundColor(
                getResources().getColor(R.color.colorPrimary, getContext().getTheme()));
    }

    private void disableTradeButton() {
        tradeBtn.setEnabled(false);
        tradeBtn.setBackgroundColor(getResources().getColor(R.color.grey, getContext().getTheme()));
    }

    private void switchView(TransactionMode mode) {
        // in case there was error on the previous view, we need to reset the page once its changed
        errorMessage.setVisibility(View.GONE);
        numberPicker.setValue(0);
        enableTradeButton();

        if (mode == TransactionMode.BUY) {
            buyBtn.setChecked(true);
            sellBtn.setChecked(false);
            costValueLabel.setText(getString(R.string.estimated_cost_label));
            availableLabel.setText(getString(R.string.available_to_trade_label));
            available.setText(Formatters.formatPrice(availableToTrade.getValue()));
            tradeBtn.setText(getString(R.string.buy_button_label));

            // reset picker max here
            numberPicker.setMax(PICKER_MAX);
        } else {
            sellBtn.setChecked(true);
            buyBtn.setChecked(false);
            costValueLabel.setText(getString(R.string.estimated_value_label));
            availableLabel.setText(getString(R.string.available_to_sell_label));
            available.setText(String.format("%s", numSharesOwned.getValue()));
            tradeBtn.setText(getString(R.string.sell_button_label));

            // can't sell more than owned
            numberPicker.setMax(numSharesOwned.getValue());
        }
    }

    private void handleTransaction() {
        String username = repo.getCurrentUser();
        String stockSymbol = stockData.getSymbol();
        int numberOfShares = numberPicker.getValue();
        float price = stockData.getCurrentPrice();
        final LocalDateTime createdAt = LocalDateTime.now();
        String toastText;
        if (buySellManager.isTransactionValid(
                username,
                stockSymbol,
                repo.getTotalAmountAvailable(),
                numberOfShares,
                price,
                mode)) {
            boolean result =
                    buySellManager.commitTransaction(
                            username,
                            stockSymbol,
                            numberOfShares,
                            price,
                            mode,
                            createdAt,
                            availableToTrade.getValue());
            if (result) {
                if (mode == TransactionMode.BUY) {
                    availableToTrade.setValue(availableToTrade.getValue() - numberOfShares * price);
                    toastText = getString(R.string.buy_successful);
                } else {
                    numSharesOwned.setValue(numSharesOwned.getValue() + numberOfShares);
                    toastText = getString(R.string.sell_successful);
                }
                slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            } else {
                toastText =
                        getString(
                                mode == TransactionMode.BUY
                                        ? R.string.buy_unsuccessful
                                        : R.string.sell_unsuccessful);
            }
        } else {
            toastText =
                    getString(
                            mode == TransactionMode.BUY
                                    ? R.string.insufficient_funds
                                    : R.string.insufficent_stocks);
        }

        Toast.makeText(getContext(), toastText, Toast.LENGTH_LONG).show();
    }
}
