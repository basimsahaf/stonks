package com.stonks.android;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.manager.StockManager;
import com.stonks.android.model.PickerLiveDataModel;
import com.stonks.android.uicomponent.ChartMarker;
import com.stonks.android.uicomponent.HorizontalNumberPicker;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.Formatters;
import java.time.LocalTime;
import java.util.Locale;

public class HypotheticalFragment extends BaseFragment {
    static final String CURRENT_PRICE_ARG = "currentPrice";
    private final String TAG = getClass().getCanonicalName();
    private TextView estimatedCost;
    private TextView estimatedValue;
    private HorizontalNumberPicker numberPicker;
    private StockChart stockChart;
    private float currentPrice;
    private float scrubbedPrice;
    private PickerLiveDataModel viewModel;
    private SlidingUpPanelLayout slidingUpPanel;
    private StockManager stockManager;
    private TextView priceChange;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.slidingUpPanel = getMainActivity().getSlidingUpPanel();
        // TODO: Set custom sliding drawer height
        slidingUpPanel.post(
                () -> slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED));

        viewModel = ViewModelProviders.of(this).get(PickerLiveDataModel.class);
        final Observer<Integer> observer =
                newValue -> {
                    float newEstimatedCost = newValue * scrubbedPrice;
                    this.estimatedCost.setText(Formatters.formatPrice(newEstimatedCost));
                    float newEstimatedValue =
                            newValue * stockManager.getStockData().getCurrentPrice();
                    this.estimatedValue.setText(Formatters.formatPrice(newEstimatedValue));
                    priceChange.setText(this.generateChangeString(scrubbedPrice, newValue));
                };

        viewModel.getNumberOfStocks().observe(getViewLifecycleOwner(), observer);

        final TextView stockSymbol = view.findViewById(R.id.stock_symbol);
        final TextView stockName = view.findViewById(R.id.stock_name);
        final TextView time = view.findViewById(R.id.time);
        this.priceChange = view.findViewById(R.id.price_change);

        stockName.setText(stockManager.getStockData().getCompanyName());
        stockSymbol.setText(stockManager.getStockData().getSymbol());
        LocalTime now = LocalTime.now();
        time.setText(String.format("%02d:%02d", now.getHour(), now.getMinute()));

        final TextView costPerShare = view.findViewById(R.id.cost_per_share);
        this.estimatedCost = view.findViewById(R.id.estimated_cost);
        this.estimatedValue = view.findViewById(R.id.estimated_value);
        this.numberPicker = view.findViewById(R.id.number_picker);
        this.numberPicker.setModel(viewModel);
        // TODO: remove this when the picker defaults to 1
        this.numberPicker.setValue(1);

        stockChart = view.findViewById(R.id.chart);
        ScrollView scrollView = view.findViewById(R.id.scrollView);

        this.estimatedValue.setText(
                Formatters.formatPrice(stockManager.getStockData().getCurrentPrice()));
        this.estimatedCost.setText(
                Formatters.formatPrice(stockManager.getStockData().getCurrentPrice()));
        costPerShare.setText(Formatters.formatPrice(stockManager.getStockData().getCurrentPrice()));

        StockChart.CustomGestureListener lineChartGestureListener =
                new StockChart.CustomGestureListener(this.stockChart, scrollView);
        lineChartGestureListener.setHideHighlight(false);

        ChartMarker lineMarker =
                new ChartMarker(
                        getContext(),
                        R.layout.chart_marker,
                        this.stockManager.getHypotheticalLineMarker());
        lineMarker.setChartView(this.stockChart);
        stockChart.setMarker(lineMarker);

        stockChart.setOnChartGestureListener(lineChartGestureListener);
        stockChart.setOnScrub(
                (x, y) -> {
                    scrubbedPrice = y;
                    float numberOfShares = this.numberPicker.getValue();
                    float newEstimatedCost = numberOfShares * y;
                    costPerShare.setText(Formatters.formatPrice(y));
                    this.estimatedCost.setText(Formatters.formatPrice(newEstimatedCost));
                    priceChange.setText(this.generateChangeString(y, numberOfShares));
                });
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arg = getArguments();
        this.currentPrice = arg.getFloat(CURRENT_PRICE_ARG);
        this.scrubbedPrice = arg.getFloat(CURRENT_PRICE_ARG);
        this.stockManager = StockManager.getInstance(getMainActivity());
        this.stockManager
                .getStockData()
                .addOnPropertyChangedCallback(
                        new androidx.databinding.Observable.OnPropertyChangedCallback() {
                            @Override
                            public void onPropertyChanged(
                                    androidx.databinding.Observable observable, int i) {
                                stockChart.setData(stockManager.getHypotheticalLineData());
                                stockChart.invalidate();
                            }
                        });
        this.stockManager.loadHypotheticalData();

        View view = inflater.inflate(R.layout.fragment_hypothetical, container, false);
        return view;
    }

    SpannableString generateChangeString(Float price, float numShares) {
        Pair<Float, Float> changePair =
                new Pair<>(currentPrice - price, ((currentPrice - price) * 100 / price));
        float change = changePair.first;
        float changePercentage = changePair.second;

        String formattedPrice = Formatters.formatPrice(Math.abs(change * numShares));
        String changeString =
                String.format(
                        Locale.CANADA, "%s (%.2f%%)", formattedPrice, Math.abs(changePercentage));
        SpannableString text = new SpannableString(changeString);

        int color = change >= 0 ? R.color.green : R.color.red;

        text.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(getContext(), color)),
                0,
                changeString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return text;
    }
}
