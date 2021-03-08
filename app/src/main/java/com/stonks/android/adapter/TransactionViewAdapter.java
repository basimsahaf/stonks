package com.stonks.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.R;
import com.stonks.android.model.Transaction;
import java.util.ArrayList;

public class TransactionViewAdapter
        extends RecyclerView.Adapter<TransactionViewAdapter.ViewHolder> {
    ArrayList<Transaction> transactions;

    public TransactionViewAdapter(ArrayList<Transaction> transactionList) {
        this.transactions = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.stock_transaction_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = this.transactions.get(position);

        holder.transactionType.setText("Market " + transaction.getTransactionType().toLowerCase());
        holder.symbol.setText(transaction.getSymbol());
        holder.priceAndShares.setText(
                "$" + transaction.getPrice() + " (x" + transaction.getShares() + ")");
        holder.date.setText(transaction.getTransactionDateString());
        holder.time.setText(transaction.getTransactionTimeString());
    }

    @Override
    public int getItemCount() {
        if (this.transactions != null) {
            return this.transactions.size();
        }

        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView date;
        public final TextView time;
        public final TextView transactionType;
        public final TextView symbol;
        public final TextView priceAndShares;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.view = itemView;
            date = this.view.findViewById(R.id.transaction_date);
            time = this.view.findViewById(R.id.transaction_time);
            transactionType = this.view.findViewById(R.id.transaction_type);
            symbol = this.view.findViewById(R.id.symbol);
            priceAndShares = this.view.findViewById(R.id.cost_shares);
        }
    }
}
