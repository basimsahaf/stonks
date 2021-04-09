package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.adapter.StockListRecyclerViewAdapter;
import com.stonks.android.manager.FavouriteStocksManager;
import com.stonks.android.model.StockListItem;
import java.util.ArrayList;

public class SavedStocksFragment extends BaseFragment {
    private FavouriteStocksManager favouriteStocksManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_stocks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        favouriteStocksManager = FavouriteStocksManager.getInstance(getContext());

        RecyclerView savedStocksListView;
        RecyclerView.LayoutManager savedStocksListManager =
                new LinearLayoutManager(this.getContext());
        ArrayList<StockListItem> savedStocks = favouriteStocksManager.getAllFavouriteStocks();

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        if (!savedStocks.isEmpty()) {
            savedStocksListView = view.findViewById(R.id.saved_list);
            savedStocksListView.setVisibility(View.VISIBLE);
            savedStocksListView.setLayoutManager(savedStocksListManager);
            StockListRecyclerViewAdapter savedStocksAdapter =
                    new StockListRecyclerViewAdapter(this.getActivity(), savedStocks, true);
            savedStocksListView.setAdapter(savedStocksAdapter);

            ConstraintLayout noSavedMsgGroup = view.findViewById(R.id.no_saved_msg_group);
            noSavedMsgGroup.setVisibility(View.GONE);
        }
    }
}
