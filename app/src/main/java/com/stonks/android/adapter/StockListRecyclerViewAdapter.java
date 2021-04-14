package com.stonks.android.adapter;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import java.util.Locale;

public class StockListRecyclerViewAdapter
        extends RecyclerView.Adapter<StockListRecyclerViewAdapter.ViewHolder> {
    private final FragmentActivity parentActivity;
    private ArrayList<StockListItem> portfolioItems;
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
            // just showing stock symbol and company name if favourite stocks list
            holder.price.setText("");
            holder.priceChange.setText("");
            holder.arrowIndicator.setVisibility(View.GONE);
            return;
        }

        holder.price.setText(Formatters.formatStockQuantity(item.getPrice(), item.getQuantity()));
        holder.priceChange.setText(generateChangeString(item.getPriceChange(), item.getChangePercent()));
        holder.arrowIndicator.setImageDrawable(getIndicatorDrawable(item.getPriceChange()));
    }

    @Override
    public int getItemCount() {
        if (this.portfolioItems != null) {
            return this.portfolioItems.size();
        }

        return 0;
    }

    public void setNewStocks(ArrayList<StockListItem> newStocks) {
        this.portfolioItems = newStocks;
    }

    SpannableString generateChangeString(float change, float changePercentage) {
        String formattedPrice = Formatters.formatPrice(Math.abs(change));
        String changeString =
                String.format(
                        Locale.CANADA, "%s (%.2f%%)", formattedPrice, Math.abs(changePercentage));
        SpannableString text = new SpannableString(changeString);

        int color = change >= 0 ? R.color.green : R.color.red;

        text.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(parentActivity, color)),
                0,
                changeString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        return text;
    }

    Drawable getIndicatorDrawable(float change) {
        if (change >= 0) {
            return ContextCompat.getDrawable(
                    parentActivity, R.drawable.ic_baseline_arrow_drop_up_24);
        } else {
            return ContextCompat.getDrawable(
                    parentActivity, R.drawable.ic_baseline_arrow_drop_down_24);
        }
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
