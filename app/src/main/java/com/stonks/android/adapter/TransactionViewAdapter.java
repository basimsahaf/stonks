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
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Transaction> transactions;

    public TransactionViewAdapter(ArrayList<Transaction> transactionList) {
        this.transactions = transactionList;
    }

    @Override
    public int getItemViewType(int position) {
        return transactions.get(position).getTransactionType().equalsIgnoreCase("DATE") ? 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;

        if (viewType == 0) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_transaction_list_item, parent, false);
            return new TransactionViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_transaction_date_item, parent, false);
            return new DateViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Transaction transaction = transactions.get(position);

        if (viewHolder.getItemViewType() == 0) {
            TransactionViewHolder holder = (TransactionViewHolder)viewHolder;

            holder.transactionType.setText("Market " + transaction.getTransactionType().toLowerCase());
            holder.symbol.setText(transaction.getSymbol());
            holder.priceAndShares.setText(
                    "$" + transaction.getPrice() + " (x" + transaction.getShares() + ")");
            holder.time.setText(transaction.getTransactionTimeString());
        } else {
            DateViewHolder holder = (DateViewHolder)viewHolder;
            holder.date.setText(transaction.getTransactionDateString());
        }
    }

    @Override
    public int getItemCount() {
        if (this.transactions != null) {
            return this.transactions.size();
        }

        return 0;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView time;
        public final TextView transactionType;
        public final TextView symbol;
        public final TextView priceAndShares;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            this.view = itemView;
            time = this.view.findViewById(R.id.transaction_time);
            transactionType = this.view.findViewById(R.id.transaction_type);
            symbol = this.view.findViewById(R.id.symbol);
            priceAndShares = this.view.findViewById(R.id.cost_shares);
        }
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView date;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);

            this.view = itemView;
            date = this.view.findViewById(R.id.date);
        }
    }
}
