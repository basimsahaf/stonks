package com.stonks.android.manager;

import android.content.Context;
import com.stonks.android.model.FavouriteStock;
import com.stonks.android.model.StockListItem;
import com.stonks.android.storage.FavouritesTable;
import java.util.ArrayList;

public class FavouriteStocksManager {
    private static FavouriteStocksManager favouriteStocksManager = null;
    private static FavouritesTable favouritesTable;
    private String username;

    private FavouriteStocksManager(Context context) {
        this.favouritesTable = FavouritesTable.getInstance(context);
        this.username = UserManager.getInstance(context).getCurrentUser().getUsername();
    }

    public static FavouriteStocksManager getInstance(Context context) {
        if (favouriteStocksManager == null) {
            favouriteStocksManager = new FavouriteStocksManager(context);
        }
        return favouriteStocksManager;
    }

    public Boolean isStockFavourited(String symbol) {
        return favouritesTable.doesFavouriteStockExist(this.username, symbol);
    }

    public void addFavouriteStock(String symbol) {
        favouritesTable.addFavouritesRow(new FavouriteStock(this.username, symbol));
    }

    public void removeFavouriteStock(String symbol) {
        favouritesTable.deleteFavouritesRow(this.username, symbol);
    }

    public ArrayList<StockListItem> getAllFavouriteStocks() {
        ArrayList<FavouriteStock> faveStocks = favouritesTable.getUserFavourites(this.username);
        ArrayList<StockListItem> faveStocksList = new ArrayList<>();

        // necessary bc the saved stocks page expects a list of StockListItems
        faveStocks.forEach(
                (faveStock) ->
                        faveStocksList.add(
                                new StockListItem(
                                        faveStock.getSymbol(), "CompanyName", 0, 0, 0, 0)));
        // ^^ TODO: use getcompanyname here
        return faveStocksList;
    }
}
