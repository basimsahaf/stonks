package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import com.stonks.android.adapter.SearchResultAdapter;
import com.stonks.android.model.SearchResult;
import java.util.ArrayList;

public class SearchableFragment extends Fragment implements SearchView.OnQueryTextListener {
    private ListView searchResultList;
    private SearchResultAdapter searchResultAdapter;
    private SearchView searchView;

    public SearchableFragment() {
        // Required empty public constructor
    }

    public static SearchableFragment newInstance() {
        SearchableFragment fragment = new SearchableFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_searchable, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchResultList = view.findViewById(R.id.list_view);
        searchResultAdapter = new SearchResultAdapter(getActivity(), getFakeSearchResults());
        searchResultList.setAdapter(searchResultAdapter);

        searchView = view.findViewById(R.id.search_view);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Let SearchView handle the query
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String query = newText;
        searchResultAdapter.filterQuery(query);
        // Let SearchView handle showing suggestions
        return false;
    }

    private ArrayList<SearchResult> getFakeSearchResults() {
        ArrayList<SearchResult> list = new ArrayList<>();
        list.add(new SearchResult("Shopify", "SHOP"));
        list.add(new SearchResult("Uber", "UBER"));
        list.add(new SearchResult("Survey Monkey", "SVMK"));
        list.add(new SearchResult("Google", "GOOGL"));
        list.add(new SearchResult("Facebook", "FB"));
        list.add(new SearchResult("Instacart", "ICART"));
        list.add(new SearchResult("Salesforce", "CRM"));

        list.add(new SearchResult("The Weather Network", "WNET"));

        return list;
    }
}
