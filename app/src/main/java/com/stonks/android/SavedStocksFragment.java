package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.adapter.PortfolioRecyclerViewAdapter;
import com.stonks.android.model.PortfolioListItem;
import java.util.ArrayList;

public class SavedStocksFragment extends BaseFragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_stocks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        RecyclerView savedStocksListView;
        RecyclerView.LayoutManager savedStocksListManager =
                new LinearLayoutManager(this.getContext());
        ArrayList<PortfolioListItem> savedStocks = HomePageFragment.getMockItems();

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        if (!savedStocks.isEmpty()) {
            savedStocksListView = view.findViewById(R.id.saved_list);
            savedStocksListView.setVisibility(View.VISIBLE);
            savedStocksListView.setLayoutManager(savedStocksListManager);
            PortfolioRecyclerViewAdapter savedStocksAdapter =
                    new PortfolioRecyclerViewAdapter(this.getActivity(), savedStocks);
            savedStocksListView.setAdapter(savedStocksAdapter);

            ConstraintLayout noSavedMsgGroup = view.findViewById(R.id.no_saved_msg_group);
            noSavedMsgGroup.setVisibility(View.GONE);
        }
    }
}
