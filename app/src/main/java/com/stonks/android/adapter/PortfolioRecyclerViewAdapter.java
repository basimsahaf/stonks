package com.stonks.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stonks.android.R;
import com.stonks.android.model.PortfolioListItem;

import java.util.ArrayList;

public class PortfolioRecyclerViewAdapter
        extends RecyclerView.Adapter<PortfolioRecyclerViewAdapter.ViewHolder> {
    ArrayList<PortfolioListItem> portfolioItems;

    public PortfolioRecyclerViewAdapter(ArrayList<PortfolioListItem> portfolioItems) {
        this.portfolioItems = portfolioItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.portfolio_list_item, parent, false);

        return new PortfolioRecyclerViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PortfolioListItem item = this.portfolioItems.get(position);

        holder.stockSymbol.setText(item.getStockSymbol());
        holder.companyName.setText(item.getCompanyName());
        holder.price.setText("$" + item.getPrice() + " (x" + item.getMultiplier() + ")");
        holder.priceChange.setText("$" + item.getPriceChange() + "(% " + item.getChangePercent() + ")");
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.view = itemView;
            stockSymbol = this.view.findViewById(R.id.stock_symbol);
            companyName = this.view.findViewById(R.id.company_name);
            price = this.view.findViewById(R.id.current_price);
            priceChange = this.view.findViewById(R.id.change_desc);
        }
    }
}