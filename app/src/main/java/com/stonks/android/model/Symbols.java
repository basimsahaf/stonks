package com.stonks.android.model;

import java.util.List;

public class Symbols {
    private final List<String> symbols;

    public Symbols(List<String> symbols) {
        this.symbols = symbols;
    }

    @Override
    public String toString() {
        return String.join(",", this.symbols);
    }
}
