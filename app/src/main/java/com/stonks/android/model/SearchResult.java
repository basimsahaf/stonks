package com.stonks.android.model;

public class SearchResult implements Comparable<SearchResult> {
    private String companyName;
    private String symbol;
    private String searchInput;

    public SearchResult(String symbol, String companyName, String searchInput) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.searchInput = searchInput.toLowerCase();
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSymbolLowercase() {
        return symbol.toLowerCase();
    }

    public String getCompanyNameLowercase() {
        return companyName.toLowerCase();
    }

    public String getSearchInput() {
        return searchInput;
    }

    public int compareTo(SearchResult searchResult) {
        int position1 = this.searchInput.indexOf(getSymbolLowercase());
        int position2 = this.searchInput.indexOf(searchResult.getSymbolLowercase());

        if (position1 == position2) {
            position1 = this.searchInput.indexOf(getCompanyNameLowercase());
            position2 = this.searchInput.indexOf(searchResult.getCompanyNameLowercase());
        }

        if (position1 == -1 && position2 == -1) {
            return 0;
        } else if (position1 == -1) {
            return 1;
        } else if (position2 == -1) {
            return -1;
        } else {
            return position1 - position2;
        }
    }
}
