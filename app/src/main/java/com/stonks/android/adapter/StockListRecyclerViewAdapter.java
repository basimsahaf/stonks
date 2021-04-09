package com.stonks.android.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.R;
import com.stonks.android.StockFragment;
import com.stonks.android.model.StockListItem;
import com.stonks.android.utility.Formatters;
import java.util.ArrayList;

public class StockListRecyclerViewAdapter
        extends RecyclerView.Adapter<StockListRecyclerViewAdapter.ViewHolder> {
    private final FragmentActivity parentActivity;
    private final ArrayList<StockListItem> portfolioItems;
    private final boolean isSavedStocksList;

    public StockListRecyclerViewAdapter(
            FragmentActivity parentActivity,
            ArrayList<StockListItem> portfolioItems,
            boolean isSavedStocksList) {
        this.parentActivity = parentActivity;
        this.portfolioItems = portfolioItems;
        this.isSavedStocksList = isSavedStocksList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.portfolio_list_item, parent, false);

        view.setOnClickListener(
                v -> {
                    TextView stockSymbolTextView = v.findViewById(R.id.stock_symbol);
                    Fragment stockFragment = new StockFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(
                            StockFragment.SYMBOL_ARG,
                            String.valueOf(stockSymbolTextView.getText()));
                    stockFragment.setArguments(bundle);

                    this.parentActivity
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, stockFragment)
                            .addToBackStack(null)
                            .commit();
                });

        return new StockListRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockListItem item = this.portfolioItems.get(position);

        holder.stockSymbol.setText(item.getStockSymbol());
        holder.companyName.setText(item.getCompanyName());

        if (this.isSavedStocksList) {
            holder.price.setText(Formatters.formatPrice(item.getPrice()));
        } else {
            holder.price.setText(
                    Formatters.formatStockQuantity(item.getPrice(), item.getQuantity()));
        }

        if (item.getPriceChange() < 0) {
            holder.priceChange.setText(
                    Formatters.formatPriceChange(
                            item.getPriceChange() * -1.0f, item.getChangePercent() * -1.0f));
            holder.priceChange.setTextColor(
                    ContextCompat.getColor(holder.priceChange.getContext(), R.color.red));
            holder.arrowIndicator.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
        } else {
            holder.priceChange.setText(
                    Formatters.formatPriceChange(item.getPriceChange(), item.getChangePercent()));
        }
    }

    @Override
    public int getItemCount() {
        if (this.portfolioItems != null) {
            return this.portfolioItems.size();
        }

        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView stockSymbol;
        public final TextView companyName;
        public final TextView price;
        public final TextView priceChange;
        public final ImageView arrowIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.view = itemView;
            stockSymbol = this.view.findViewById(R.id.stock_symbol);
            companyName = this.view.findViewById(R.id.company_name);
            price = this.view.findViewById(R.id.current_price);
            priceChange = this.view.findViewById(R.id.change_desc);
            arrowIndicator = this.view.findViewById(R.id.arrow_indicator);
        }
    }
}
