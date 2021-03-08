package com.stonks.android;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import com.stonks.android.adapter.SearchResultAdapter;
import com.stonks.android.model.SearchResult;
import java.util.ArrayList;

public class SearchableActivity extends BaseActivity implements SearchView.OnQueryTextListener {
    private ListView searchResultList;
    private SearchResultAdapter searchResultAdapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        searchResultList = (ListView) findViewById(R.id.list_view);
        searchResultAdapter = new SearchResultAdapter(this, getFakeSearchResults());
        searchResultList.setAdapter(searchResultAdapter);

        searchView = (SearchView) findViewById(R.id.search_view);
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
