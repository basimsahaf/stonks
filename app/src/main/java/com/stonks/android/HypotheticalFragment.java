package com.stonks.android;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.uicomponent.CustomSparkView;

import java.util.Locale;
import java.util.stream.Collectors;

public class HypotheticalFragment extends Fragment {

    TextView costPerShare;
    TextView estimatedCost;
    TextView estimatedValue;
    HorizontalNumberPicker numberPicker;
    CustomSparkView sparkView;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView costPerShare = view.findViewById(R.id.cost_per_share);
        this.estimatedCost = view.findViewById(R.id.estimated_cost);
        this.estimatedValue = view.findViewById(R.id.estimated_value);
        this.numberPicker = view.findViewById(R.id.number_picker);

//        this.numberPicker.

        sparkView = view.findViewById(R.id.chart);
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
                        float numberOfShares = this.numberPicker.getValue();
                        float newEstimatedCost = numberOfShares*Float.parseFloat(value.toString());
                        Log.d("Debug", value.toString());
                        Log.d("Debug", "" + costPerShare.getId());
                        costPerShare.setText(String.format(Locale.CANADA, "$%.2f", value));
                        this.estimatedCost.setText(String.format(Locale.CANADA, "$%.2f", newEstimatedCost));
                    }
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hypothetical, container, false);
        return view;
    }
}