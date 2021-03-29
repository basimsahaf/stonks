package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import com.stonks.android.adapter.SearchResultAdapter;
import com.stonks.android.model.SearchResult;
import java.util.ArrayList;

public class SavedStocksFragment extends BaseFragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_stocks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ListView savedStocksListView;
        SearchResultAdapter savedStocksAdapter;

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        ArrayList<SearchResult> savedStocks = SearchableFragment.getFakeSearchResults();
        if (!savedStocks.isEmpty()) {
            savedStocksListView = view.findViewById(R.id.saved_list);
            savedStocksListView.setVisibility(View.VISIBLE);
            savedStocksAdapter = new SearchResultAdapter(this.getContext(), savedStocks, true);
            savedStocksListView.setAdapter(savedStocksAdapter);

            savedStocksListView.setOnItemClickListener(
                    (adapterView, view1, position, l) -> {
                        SearchResult item = savedStocksAdapter.getItem(position);

                        Fragment stockFragment = new StockFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(getString(R.string.intent_extra_symbol), item.getSymbol());
                        stockFragment.setArguments(bundle);
                        getActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, stockFragment)
                                .addToBackStack(null)
                                .commit();
                    });

            ConstraintLayout noSavedMsgGroup = view.findViewById(R.id.no_saved_msg_group);
            noSavedMsgGroup.setVisibility(View.GONE);
        }
    }
}
