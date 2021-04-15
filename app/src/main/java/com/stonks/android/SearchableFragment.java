package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import com.stonks.android.adapter.SearchResultAdapter;
import com.stonks.android.manager.SearchManager;
import com.stonks.android.model.SearchResult;
import java.util.ArrayList;

public class SearchableFragment extends BaseFragment implements SearchView.OnQueryTextListener {
    private ListView searchResultList;
    private SearchResultAdapter searchResultAdapter;
    private SearchView searchView;
    private SearchManager searchManager;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_searchable, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        searchManager = SearchManager.getInstance(this.getContext());
        super.onViewCreated(view, savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        searchResultList = view.findViewById(R.id.list_view);
        searchResultAdapter = new SearchResultAdapter(getActivity());
        searchResultList.setAdapter(searchResultAdapter);

        searchView = view.findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);

        searchResultList.setOnItemClickListener(
                (adapterView, view1, position, l) -> {
                    SearchResult item = searchResultAdapter.getItem(position);

                    Fragment stockFragment = new StockFragment();
                    String stockFragmentTag = stockFragment.getClass().getCanonicalName();
                    Bundle bundle = new Bundle();
                    bundle.putString(StockFragment.SYMBOL_ARG, item.getSymbol());
                    stockFragment.setArguments(bundle);
                    getActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, stockFragment, stockFragmentTag)
                            .addToBackStack(stockFragmentTag)
                            .commit();
                });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Let SearchView handle the query
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String query = newText;
        ArrayList<SearchResult> searchResults = searchManager.getOrderedSearchResults(query);
        searchResultAdapter.updateSearchResultList(searchResults);
        // Let SearchView handle showing suggestions
        return false;
    }
}
