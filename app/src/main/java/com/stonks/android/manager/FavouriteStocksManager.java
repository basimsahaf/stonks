package com.stonks.android.manager;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.stonks.android.model.FavouriteStock;
import com.stonks.android.model.LoginRepository;
import com.stonks.android.model.StockListItem;
import com.stonks.android.storage.FavouritesTable;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FavouriteStocksManager {
    private static FavouriteStocksManager favouriteStocksManager = null;
    private static FavouritesTable favouritesTable;
    private String username;

    private FavouriteStocksManager(Context context) {
        this.favouritesTable = new FavouritesTable(context);
        this.username = "ce";
    }

    public static FavouriteStocksManager getInstance(Context context) {
        if (favouriteStocksManager == null) {
            favouriteStocksManager = new FavouriteStocksManager(context);
        }
        return favouriteStocksManager;
    }

    public boolean isStockFavourited(String symbol) {
        return favouritesTable.checkIfFavouriteExists(this.username, symbol);
    }

    public void addFavouriteStock(String symbol) {
        favouritesTable.addFavouritesRow(new FavouriteStock(this.username, symbol));
    }

    public void removeFavouriteStock(String symbol) {
        favouritesTable.deleteFavouritesRow(this.username, symbol);
    }

    public ArrayList<StockListItem> getAllFavouriteStocks() {
        ArrayList<FavouriteStock> faveStocks = favouritesTable.getUserFavourites(this.username);
        ArrayList<StockListItem> faveStocksList = new ArrayList<StockListItem>();

        faveStocks.forEach((faveStock) ->
                faveStocksList.add(new StockListItem(faveStock.getSymbol(), "CompanyName", 0, 0, 0, 0)));
        return faveStocksList;
    }

}
