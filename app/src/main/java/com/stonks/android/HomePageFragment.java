package com.stonks.android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.stonks.android.adapter.StockListRecyclerViewAdapter;
import com.stonks.android.manager.PortfolioManager;
import com.stonks.android.model.alpaca.DateRange;
import com.stonks.android.uicomponent.ChartMarker;
import com.stonks.android.uicomponent.StockChart;
import com.stonks.android.utility.Formatters;
import java.util.ArrayList;

public class HomePageFragment extends BaseFragment {
    private int currentInfoHeaderHeight = -1;

    private FrameLayout spinner;
    private ConstraintLayout homePage;

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
    private NestedScrollView scrollView;

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

        this.spinner = view.findViewById(R.id.spinner);
        this.homePage = view.findViewById(R.id.home_page);

        this.accountValue = view.findViewById(R.id.current_value_price);
        this.priceUpdate = view.findViewById(R.id.price_update);
        this.moneyLeft = view.findViewById(R.id.money_left);
        this.totalReturn = view.findViewById(R.id.total_return);
        this.noStocksMsg = view.findViewById(R.id.no_stocks_msg);
        this.stockChart = view.findViewById(R.id.new_stock_chart);
        this.priceUpdateArrow = view.findViewById(R.id.price_update_arrow);
        this.totalReturnArrow = view.findViewById(R.id.total_return_arrow);
        this.scrollView = view.findViewById(R.id.scroll_view);

        this.rangeDayButton = view.findViewById(R.id.range_day);
        this.rangeWeekButton = view.findViewById(R.id.range_week);
        this.rangeMonthButton = view.findViewById(R.id.range_month);
        this.rangeYearButton = view.findViewById(R.id.range_year);
        this.rangeAllButton = view.findViewById(R.id.range_all);
        disableAllButtons();

        view.findViewById(R.id.chart_toggle).setVisibility(View.GONE);

        RecyclerView.LayoutManager portfolioListManager =
                new LinearLayoutManager(this.getContext());
        RecyclerView portfolioList = view.findViewById(R.id.portfolio_list);
        portfolioList.setLayoutManager(portfolioListManager);
        portfolioListAdapter =
                new StockListRecyclerViewAdapter(this.getActivity(), new ArrayList<>(), false);
        portfolioList.setAdapter(portfolioListAdapter);

        ChartMarker lineMarker =
                new ChartMarker(
                        getContext(), R.layout.chart_marker, portfolioManager.getLineMarker());
        lineMarker.setChartView(this.stockChart);
        StockChart.CustomGestureListener lineChartGestureListener =
                new StockChart.CustomGestureListener(this.stockChart, this.scrollView);

        this.stockChart.setOnChartGestureListener(lineChartGestureListener);
        this.stockChart.setMarker(lineMarker);

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
                    disableAllButtons();
                    this.rangeDayButton.setChecked(true);
                    portfolioManager.setCurrentRange(DateRange.DAY);
                });
        rangeWeekButton.setOnClickListener(
                v -> {
                    disableAllButtons();
                    this.rangeWeekButton.setChecked(true);
                    portfolioManager.setCurrentRange(DateRange.WEEK);
                });
        rangeMonthButton.setOnClickListener(
                v -> {
                    disableAllButtons();
                    this.rangeMonthButton.setChecked(true);
                    portfolioManager.setCurrentRange(DateRange.MONTH);
                });
        rangeYearButton.setOnClickListener(
                v -> {
                    disableAllButtons();
                    this.rangeYearButton.setChecked(true);
                    portfolioManager.setCurrentRange(DateRange.YEAR);
                });
        rangeAllButton.setOnClickListener(
                v -> {
                    disableAllButtons();
                    this.rangeAllButton.setChecked(true);
                    portfolioManager.setCurrentRange(DateRange.THREE_YEARS);
                });

        portfolioManager.calculateData(false);
    }

    @Override
    public void onPause() {
        portfolioManager.unsubscribePortfolioItems();
        super.onPause();
    }

    public void disableAllButtons() {
        spinner.setVisibility(View.VISIBLE);
        rangeDayButton.setClickable(false);
        rangeWeekButton.setClickable(false);
        rangeMonthButton.setClickable(false);
        rangeYearButton.setClickable(false);
        rangeAllButton.setClickable(false);
    }

    public void enableAllButtons() {
        spinner.setVisibility(View.GONE);
        rangeDayButton.setClickable(true);
        rangeWeekButton.setClickable(true);
        rangeMonthButton.setClickable(true);
        rangeYearButton.setClickable(true);
        rangeAllButton.setClickable(true);
    }

    public void updateData() {
        if (!portfolioManager.getStocksList().isEmpty()) {
            noStocksMsg.setVisibility(View.GONE);
        }
        ((StockListRecyclerViewAdapter) portfolioListAdapter)
                .setNewStocks(portfolioManager.getStocksList());
        portfolioListAdapter.notifyDataSetChanged();

        getMainActivity().setPortfolioValue(portfolioManager.getAccountValue());
        this.moneyLeft.setText(Formatters.formatPrice(portfolioManager.getAccountBalance()));

        updateGraphData();
    }

    public void updateGraphData() {
        this.accountValue.setText(Formatters.formatPrice(portfolioManager.getAccountValue()));

        ArrayList<Float> barData = portfolioManager.getGraphData();
        float valueChange = portfolioManager.getGraphChange();
        float valueChangePercent;
        if (barData.isEmpty()) {
            valueChangePercent = 0.0f;
        } else {
            valueChangePercent =
                    barData.get(0) == 0.0f ? 0.0f : (valueChange * 100) / barData.get(0);
        }

        this.priceUpdate.setText(Formatters.formatPriceChange(valueChange, valueChangePercent));
        this.priceUpdate.setTextColor(
                ResourcesCompat.getColor(
                        getResources(), valueChange < 0 ? R.color.red : R.color.green, null));
        this.priceUpdateArrow.setImageDrawable(getIndicatorDrawable(valueChange));

        float totalReturn = portfolioManager.getTotalReturn();
        this.totalReturn.setText(
                Formatters.formatTotalReturn(totalReturn, portfolioManager.getStartDate()));
        this.totalReturn.setTextColor(
                ResourcesCompat.getColor(
                        getResources(), totalReturn < 0 ? R.color.red : R.color.green, null));
        this.totalReturnArrow.setImageDrawable(getIndicatorDrawable(totalReturn));

        if (!barData.isEmpty()) {
            this.stockChart.setData(portfolioManager.getStockChartData());
            this.stockChart.invalidate();
        }

        this.spinner.setVisibility(View.GONE);
        this.homePage.setVisibility(View.VISIBLE);
        enableAllButtons();
    }

    private Drawable getIndicatorDrawable(float change) {
        if (change >= 0) {
            return ContextCompat.getDrawable(
                    this.getContext(), R.drawable.ic_baseline_arrow_drop_up_24);
        } else {
            return ContextCompat.getDrawable(
                    this.getContext(), R.drawable.ic_baseline_arrow_drop_down_24);
        }
    }
}
