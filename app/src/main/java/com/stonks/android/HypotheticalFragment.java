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
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.model.HypotheticalViewModel;
import com.stonks.android.uicomponent.CustomSparkView;
import com.stonks.android.uicomponent.HorizontalNumberPicker;
import java.util.Locale;
import java.util.stream.Collectors;

public class HypotheticalFragment extends Fragment {

    TextView estimatedCost;
    TextView estimatedValue;
    HorizontalNumberPicker numberPicker;
    CustomSparkView sparkView;
    private float currentPrice;

    HypotheticalViewModel viewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(HypotheticalViewModel.class);
        final Observer<Integer> observer =
                newValue -> {
                    float newEstimatedCost = newValue * currentPrice;
                    estimatedCost.setText(
                            String.format(Locale.CANADA, "$%.2f", newEstimatedCost));
                };

        viewModel.getNumberOfStocks().observe(getViewLifecycleOwner(), observer);

        final TextView costPerShare = view.findViewById(R.id.cost_per_share);
        this.estimatedCost = view.findViewById(R.id.estimated_cost);
        this.estimatedValue = view.findViewById(R.id.estimated_value);
        this.numberPicker = view.findViewById(R.id.number_picker);
        this.numberPicker.setModel(viewModel);

        sparkView = view.findViewById(R.id.chart);
        sparkView.setBelongsToHypothetical();
        StockChartAdapter dataAdapter =
                new StockChartAdapter(
                        StockActivity.getFakeStockPrices().stream()
                                .map(p -> p.second)
                                .collect(Collectors.toList()));
        dataAdapter.setBaseline(121.08f);

        sparkView.setAdapter(dataAdapter);
        sparkView.setScrubListener(
                value -> {
                    if (value != null) {
                        currentPrice = Float.parseFloat(value.toString());
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
        this.currentPrice = Float.parseFloat(getArguments().getString("currentPrice"));
        View view = inflater.inflate(R.layout.fragment_hypothetical, container, false);
        return view;
    }
}
