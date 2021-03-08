package com.stonks.android;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.SearchView;

public class SearchableActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableSearch();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
    }

    protected void enableSearch() {
        // Enable search for SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
    }
}
