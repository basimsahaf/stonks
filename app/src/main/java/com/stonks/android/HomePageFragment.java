package com.stonks.android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.adapter.StockListRecyclerViewAdapter;
import com.stonks.android.manager.PortfolioManager;
import com.stonks.android.model.StockListItem;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.uicomponent.CustomSparkView;
import com.stonks.android.utility.Formatters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class HomePageFragment extends BaseFragment {
    private int currentInfoHeaderHeight = -1;
    private TextView accountValue;
    private TextView moneyLeft;
    private TextView priceUpdate;
    private TextView totalReturn;
    private TextView noStocksMsg;
    private ImageView priceUpdateArrow;
    private ImageView totalReturnArrow;
    private static PortfolioManager portfolioManager;
    private static RecyclerView.Adapter portfolioListAdapter;
    private static StockChartAdapter dataAdapter;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        portfolioManager = PortfolioManager.getInstance(this.getContext(), this);

        this.accountValue = view.findViewById(R.id.current_value_price);
        this.priceUpdate = view.findViewById(R.id.price_update);
        this.moneyLeft = view.findViewById(R.id.money_left);
        this.totalReturn = view.findViewById(R.id.total_return);

        this.priceUpdateArrow = view.findViewById(R.id.price_update_arrow);
        this.totalReturnArrow = view.findViewById(R.id.total_return_arrow);

        RecyclerView.LayoutManager portfolioListManager =
                new LinearLayoutManager(this.getContext());
        RecyclerView portfolioList = view.findViewById(R.id.portfolio_list);
        portfolioList.setLayoutManager(portfolioListManager);
        portfolioListAdapter =
                new StockListRecyclerViewAdapter(this.getActivity(), new ArrayList<>(), false);
        portfolioList.setAdapter(portfolioListAdapter);

        NestedScrollView scrollView = view.findViewById(R.id.scroll_view);
        ConstraintLayout currentInfoHeader = view.findViewById(R.id.current_info_header);

        // get the height of the header
        currentInfoHeader.post(() -> currentInfoHeaderHeight = currentInfoHeader.getHeight());

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getMainActivity().setActionBarCustomViewAlpha(0);
        getMainActivity().setPortfolioValue(0.0f);

        scrollView.setOnScrollChangeListener(
                (View.OnScrollChangeListener)
                        (view1, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                            float offset = currentInfoHeaderHeight - scrollY;
                            float alpha = (1f - (Math.max(0f, offset) / currentInfoHeaderHeight));

                            getMainActivity().setActionBarCustomViewAlpha(alpha);
                        });

        CustomSparkView sparkView = view.findViewById(R.id.stock_chart);
        sparkView.setScrubListener(
                value -> scrollView.requestDisallowInterceptTouchEvent(value != null));
        dataAdapter =
                new StockChartAdapter(
                        StockFragment.getFakeStockPrices().stream()
                                .map(p -> p.second)
                                .collect(Collectors.toList()));
        dataAdapter.setBaseline(121.08f);

        sparkView.setAdapter(dataAdapter);

        portfolioManager.fetchInitialData();
    }

    public static ArrayList<StockListItem> getMockItems() {
        ArrayList<StockListItem> list = new ArrayList<>();

        list.add(new StockListItem("SHOP", 19.34f, 103, -2.15f, -1.0f));
        list.add(new StockListItem("UBER", 9.22f, 3, 2.23f, 2.85f));
        list.add(new StockListItem("AMZN", 20.99f, 1, -8.90f, -4.0f));
        list.add(new StockListItem("GOOG", 30.81f, 22, 1.11f, 3.33f));
        list.add(new StockListItem("SPY", 30.81f, 22, 1.11f, 3.33f));

        return list;
    }

    public void updateData() {
        if (!portfolioManager.getStocks().isEmpty()) {
            noStocksMsg.setVisibility(View.GONE);
        }
        ((StockListRecyclerViewAdapter) portfolioListAdapter).setNewStocks(portfolioManager.getStocks());
        portfolioListAdapter.notifyDataSetChanged();

        ArrayList<Float> barData = portfolioManager.getBarData();
        dataAdapter.setData(
                barData.stream()
                        .collect(Collectors.toList()));
        dataAdapter.setBaseline(barData.get(0));
        dataAdapter.notifyDataSetChanged();

        getMainActivity().setPortfolioValue(portfolioManager.getAccountValue());
        this.accountValue.setText(Formatters.formatPrice(portfolioManager.getAccountValue()));
        this.moneyLeft.setText(Formatters.formatPrice(portfolioManager.getAccountBalance()));

        float valueChange = portfolioManager.graphChange();
        float valueChangePercent = barData.get(0) == 0.0f ? 0.0f : valueChange / barData.get(0);
        this.priceUpdate.setText(Formatters.formatPriceChange(valueChange, valueChangePercent));
        this.priceUpdateArrow.setImageDrawable(getIndicatorDrawable(valueChange));

        float soldProfit = portfolioManager.calculateProfit();
        this.totalReturn.setText(Formatters.formatTotalReturn(soldProfit)); // TODO: get training period start form user table
        this.totalReturnArrow.setImageDrawable(getIndicatorDrawable(soldProfit));
    }

    Drawable getIndicatorDrawable(float change) {
        if (change >= 0) {
            return ContextCompat.getDrawable(
                    this.getContext(), R.drawable.ic_baseline_arrow_drop_up_24);
        } else {
            return ContextCompat.getDrawable(
                    this.getContext(), R.drawable.ic_baseline_arrow_drop_down_24);
        }
    }
}
