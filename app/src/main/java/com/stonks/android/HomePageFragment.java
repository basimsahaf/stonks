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

import com.google.android.material.button.MaterialButton;
import com.stonks.android.adapter.StockListRecyclerViewAdapter;
import com.stonks.android.manager.PortfolioManager;
import com.stonks.android.model.StockListItem;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.Formatters;

import java.util.ArrayList;

public class HomePageFragment extends BaseFragment {
    private int currentInfoHeaderHeight = -1;

    private TextView accountValue;
    private TextView moneyLeft;
    private TextView priceUpdate;
    private TextView totalReturn;
    private TextView noStocksMsg;
    private ImageView priceUpdateArrow;
    private ImageView totalReturnArrow;
    private StockChart stockChart;
    private MaterialButton rangeDayButton;
    private MaterialButton rangeWeekButton;
    private MaterialButton rangeMonthButton;
    private MaterialButton rangeYearButton;
    private MaterialButton rangeAllButton;

    private static PortfolioManager portfolioManager;
    private static RecyclerView.Adapter portfolioListAdapter;

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
        this.noStocksMsg = view.findViewById(R.id.no_stocks_msg);
        this.stockChart = view.findViewById(R.id.new_stock_chart);
        this.priceUpdateArrow = view.findViewById(R.id.price_update_arrow);
        this.totalReturnArrow = view.findViewById(R.id.total_return_arrow);

        this.rangeDayButton = view.findViewById(R.id.range_day);
        this.rangeWeekButton = view.findViewById(R.id.range_week);
        this.rangeMonthButton = view.findViewById(R.id.range_month);
        this.rangeYearButton = view.findViewById(R.id.range_year);
        this.rangeAllButton = view.findViewById(R.id.range_all);

        view.findViewById(R.id.chart_toggle).setVisibility(View.GONE);

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

        rangeDayButton.setChecked(true);
        rangeDayButton.setOnClickListener(
                v -> {
                    portfolioManager.setCurrentRange(DateRange.DAY);
                    this.rangeDayButton.setChecked(true);
                });
        rangeWeekButton.setOnClickListener(
                v -> {
                    portfolioManager.setCurrentRange(DateRange.WEEK);
                    this.rangeWeekButton.setChecked(true);
                });
        rangeMonthButton.setOnClickListener(
                v -> {
                    portfolioManager.setCurrentRange(DateRange.MONTH);
                    this.rangeMonthButton.setChecked(true);
                });
        rangeYearButton.setOnClickListener(
                v -> {
                    portfolioManager.setCurrentRange(DateRange.YEAR);
                    this.rangeYearButton.setChecked(true);
                });
        rangeAllButton.setOnClickListener(
                v -> {
                    this.rangeAllButton.setChecked(true);
                    portfolioManager.setCurrentRange(DateRange.THREE_YEARS);
                });

        portfolioManager.calculateData(false);
    }

    public static ArrayList<StockListItem> getMockItems() {
        ArrayList<StockListItem> list = new ArrayList<>();

        list.add(new StockListItem("SHOP", "Shopify Inc.", 19.34f, 103, -2.15f, -1.0f));
        list.add(new StockListItem("UBER", "Uber Technologies Inc.", 9.22f, 3, 2.23f, 2.85f));
        list.add(new StockListItem("AMZN", "Amazon.com  Inc.", 20.99f, 1, -8.90f, -4.0f));
        list.add(new StockListItem("GOOG", "Google", 30.81f, 22, 1.11f, 3.33f));
        list.add(new StockListItem("SPY", "SPY ETF", 30.81f, 22, 1.11f, 3.33f));

        return list;
    }

    public void updateData() {
        if (!portfolioManager.getStocksList().isEmpty()) {
            noStocksMsg.setVisibility(View.GONE);
        }
        ((StockListRecyclerViewAdapter) portfolioListAdapter).setNewStocks(portfolioManager.getStocksList());
        portfolioListAdapter.notifyDataSetChanged();

        getMainActivity().setPortfolioValue(portfolioManager.getAccountValue());
        this.moneyLeft.setText(Formatters.formatPrice(portfolioManager.getAccountBalance()));

        float soldProfit = portfolioManager.getTransactionProfits();
        this.totalReturn.setText(Formatters.formatTotalReturn(soldProfit)); // TODO: get training period start form user table
        this.totalReturnArrow.setImageDrawable(getIndicatorDrawable(soldProfit));

        updateGraph();
    }

    public void updateGraph() {
        this.accountValue.setText(Formatters.formatPrice(portfolioManager.getAccountValue()));

        ArrayList<Float> barData = portfolioManager.getGraphData();
        float valueChange = portfolioManager.getGraphChange();
        float valueChangePercent = barData.get(0) == 0.0f ? 0.0f : valueChange / barData.get(0);
        this.priceUpdate.setText(Formatters.formatPriceChange(valueChange, valueChangePercent));
        this.priceUpdateArrow.setImageDrawable(getIndicatorDrawable(valueChange));

        this.stockChart.setData(portfolioManager.getStockChartData());
        this.stockChart.invalidate();
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
