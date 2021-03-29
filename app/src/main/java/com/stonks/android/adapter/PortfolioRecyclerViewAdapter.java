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
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.MainActivity;
import com.stonks.android.R;
import com.stonks.android.StockFragment;
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
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.portfolio_list_item, parent, false);

        view.setOnClickListener(
                v -> {
                    TextView stockSymbolTextView = v.findViewById(R.id.stock_symbol);
                    Fragment stockFragment = new StockFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(
                            view.getContext().getString(R.string.intent_extra_symbol),
                            String.valueOf(stockSymbolTextView.getText()));
                    stockFragment.setArguments(bundle);

                    // this feels dangerous
                    ((MainActivity) v.getContext())
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, stockFragment)
                            .addToBackStack(null)
                            .commit();
                });

        return new PortfolioRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PortfolioListItem item = this.portfolioItems.get(position);

        holder.stockSymbol.setText(item.getStockSymbol());
        holder.companyName.setText(item.getCompanyName());
        holder.price.setText(String.format("$%.2f (x%d)", item.getPrice(), item.getMultiplier()));

        if (item.getPriceChange() < 0) {
            holder.priceChange.setText(
                    String.format(
                            "$%.2f (%.2f%%)",
                            item.getPriceChange() * -1.0, item.getChangePercent() * -1.0));
            holder.priceChange.setTextColor(
                    ContextCompat.getColor(holder.priceChange.getContext(), R.color.red));
            holder.arrowIndicator.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
        } else {
            holder.priceChange.setText(
                    String.format(
                            "$%.2f (%.2f%%)", item.getPriceChange(), item.getChangePercent()));
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
