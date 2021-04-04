package com.stonks.android.model;

import java.util.List;

public class Symbols {
    private List<String> symbols;

    public Symbols() {}

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }

    @Override
    public String toString() {
        return String.join(",", this.symbols);
    }
}
