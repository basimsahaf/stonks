package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.CompanyModel;
import com.stonks.android.model.SearchResult;
import com.stonks.android.storage.CompanyTable;
import java.util.ArrayList;
import java.util.Collections;

public class SearchManager {
    private static SearchManager searchManager = null;
    private static CompanyTable companyTable;

    private SearchManager(Context context) {
        companyTable = CompanyTable.getInstance(context);
    }

    public static SearchManager getInstance(Context context) {
        if (searchManager == null) {
            searchManager = new SearchManager(context);
        }

        return searchManager;
    }

    public ArrayList<SearchResult> getOrderedSearchResults(String searchInput) {
        ArrayList<SearchResult> searchResults = getSearchResults(searchInput);
        Collections.sort(searchResults);

        return searchResults;
    }

    private ArrayList<SearchResult> getSearchResults(String searchInput) {
        ArrayList<CompanyModel> companies = companyTable.getMatchingCompanies(searchInput);
        ArrayList<SearchResult> searchResults = new ArrayList<>();
        for (CompanyModel company : companies) {
            SearchResult searchResult =
                    new SearchResult(company.getSymbol(), company.getName(), searchInput);
            searchResults.add(searchResult);
        }

        return searchResults;
    }
}
