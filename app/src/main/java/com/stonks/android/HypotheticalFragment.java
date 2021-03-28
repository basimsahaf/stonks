package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.model.PickerLiveDataModel;
import com.stonks.android.uicomponent.CustomSparkView;
import com.stonks.android.uicomponent.HorizontalNumberPicker;
import com.stonks.android.utility.Formatters;
import java.util.Locale;
import java.util.stream.Collectors;

public class HypotheticalFragment extends Fragment {
    static final String CURRENT_PRICE_ARG = "currentPrice";
    private final String TAG = getClass().getCanonicalName();
    private TextView estimatedCost;
    private TextView estimatedValue;
    private HorizontalNumberPicker numberPicker;
    private CustomSparkView sparkView;
    private float currentPrice;
    private float scrubbedPrice;
    private PickerLiveDataModel viewModel;
    private SlidingUpPanelLayout slidingUpPanel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();
        this.slidingUpPanel = mainActivity.getSlidingUpPanel();
        // TODO: Set custom sliding drawer height
        slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

        viewModel = ViewModelProviders.of(this).get(PickerLiveDataModel.class);
        final Observer<Integer> observer =
                newValue -> {
                    float newEstimatedCost = newValue * scrubbedPrice;
                    this.estimatedCost.setText(Formatters.formatPrice(newEstimatedCost));
                    float newEstimatedValue = newValue * currentPrice;
                    this.estimatedValue.setText(Formatters.formatPrice(newEstimatedValue));
                };

        viewModel.getNumberOfStocks().observe(getViewLifecycleOwner(), observer);

        final TextView costPerShare = view.findViewById(R.id.cost_per_share);
        this.estimatedCost = view.findViewById(R.id.estimated_cost);
        this.estimatedValue = view.findViewById(R.id.estimated_value);
        this.numberPicker = view.findViewById(R.id.number_picker);
        this.numberPicker.setModel(viewModel);
        // TODO: remove this when the picker defaults to 1
        this.numberPicker.setValue(1);

        sparkView = view.findViewById(R.id.chart);
        sparkView.keepScrubLineOnRelease();

        this.estimatedValue.setText(Formatters.formatPrice(this.currentPrice));
        this.estimatedCost.setText(Formatters.formatPrice(this.currentPrice));

        StockChartAdapter dataAdapter =
                new StockChartAdapter(
                        StockFragment.getFakeStockPrices().stream()
                                .map(p -> p.second)
                                .collect(Collectors.toList()));
        dataAdapter.setBaseline(121.08f);

        sparkView.setAdapter(dataAdapter);
        sparkView.setScrubListener(
                value -> {
                    if (value != null) {
                        scrubbedPrice = Float.parseFloat(value.toString());
                        float numberOfShares = this.numberPicker.getValue();
                        float newEstimatedCost =
                                numberOfShares * Float.parseFloat(value.toString());
                        costPerShare.setText(String.format(Locale.CANADA, "$%.2f", value));
                        this.estimatedCost.setText(
                                String.format(Locale.CANADA, "$%.2f", newEstimatedCost));
                    }
                });
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arg = getArguments();
        this.currentPrice = arg.getFloat(CURRENT_PRICE_ARG);
        this.scrubbedPrice = arg.getFloat(CURRENT_PRICE_ARG);
        View view = inflater.inflate(R.layout.fragment_hypothetical, container, false);
        return view;
    }
}
