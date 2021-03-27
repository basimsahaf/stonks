package com.stonks.android;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.adapter.PortfolioRecyclerViewAdapter;
import com.stonks.android.adapter.StockChartAdapter;
import com.stonks.android.model.PortfolioListItem;
import com.stonks.android.uicomponent.CustomSparkView;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HomePageFragment extends BaseFragment {
    private int currentInfoHeaderHeight = -1;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView.LayoutManager portfolioListManager =
                new LinearLayoutManager(this.getContext());
        RecyclerView portfolioList = view.findViewById(R.id.portfolio_list);
        portfolioList.setLayoutManager(portfolioListManager);
        RecyclerView.Adapter portfolioListAdapter =
                new PortfolioRecyclerViewAdapter(this.getMockItems());
        portfolioList.setAdapter(portfolioListAdapter);

        NestedScrollView scrollView = view.findViewById(R.id.scroll_view);
        ConstraintLayout currentInfoHeader = view.findViewById(R.id.current_info_header);
        int[] locWindow = {-1, -1};

        currentInfoHeader
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                currentInfoHeader
                                        .getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);
                                currentInfoHeaderHeight = currentInfoHeader.getHeight();
                                currentInfoHeader.getLocationInWindow(locWindow);
                            }
                        });

        MainActivity activity = (MainActivity) getActivity();

        getActionBar().setDisplayHomeAsUpEnabled(false);

        activity.setActionBarCustomViewAlpha(0);
        activity.setPortfolioValue(129.32f);

        scrollView.setOnScrollChangeListener(
                (View.OnScrollChangeListener)
                        (view1, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                            currentInfoHeader.getLocationInWindow(locWindow);
                            float alpha =
                                    (1f
                                            - (Math.max(
                                                            0f,
                                                            (float) currentInfoHeaderHeight
                                                                    - scrollY)
                                                    / currentInfoHeaderHeight));

                            activity.setActionBarCustomViewAlpha(alpha);
                        });

        CustomSparkView sparkView = view.findViewById(R.id.stock_chart);
        sparkView.setScrubListener(
                value -> scrollView.requestDisallowInterceptTouchEvent(value != null));
        StockChartAdapter dataAdapter =
                new StockChartAdapter(
                        StockFragment.getFakeStockPrices().stream()
                                .map(p -> p.second)
                                .collect(Collectors.toList()));
        dataAdapter.setBaseline(121.08f);

        sparkView.setAdapter(dataAdapter);
    }

    private ArrayList<PortfolioListItem> getMockItems() {
        ArrayList<PortfolioListItem> list = new ArrayList<>();

        list.add(new PortfolioListItem("SHOP", "Shopify Inc.", 19.34f, 103, 2.15f, 1.0f));
        list.add(new PortfolioListItem("UBER", "Uber Technologies Inc.", 9.22f, 3, 2.23f, 2.85f));
        list.add(new PortfolioListItem("AMZN", "Amazon.com  Inc.", 20.99f, 1, 8.90f, 4.0f));
        list.add(new PortfolioListItem("GOOG", "Google", 30.81f, 22, 1.11f, 3.33f));

        // TODO: Fix fragment scroll issue
        // Padding for bug
        list.add(new PortfolioListItem("GOOG", "Google", 30.81f, 22, 1.11f, 3.33f));

        return list;
    }

    public static class AlphaForegroundColorSpan extends ForegroundColorSpan {
        private float mAlpha = 1;

        public AlphaForegroundColorSpan(int color) {
            super(color);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(mAlpha);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(getAlphaColor());
        }

        public void setAlpha(float alpha) {
            mAlpha = alpha;
        }

        public float getAlpha() {
            return mAlpha;
        }

        private int getAlphaColor() {
            int foregroundColor = getForegroundColor();
            return Color.argb(
                    (int) (mAlpha * 255),
                    Color.red(foregroundColor),
                    Color.green(foregroundColor),
                    Color.blue(foregroundColor));
        }
    }
}
