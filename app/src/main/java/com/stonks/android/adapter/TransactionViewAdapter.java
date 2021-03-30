package com.stonks.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.R;
import com.stonks.android.model.Transaction;
import com.stonks.android.model.TransactionsListRow;
import com.stonks.android.utility.Formatters;
import java.util.ArrayList;

public class TransactionViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<TransactionsListRow> transactions;

    public TransactionViewAdapter(ArrayList<TransactionsListRow> transactionList) {
        this.transactions = transactionList;
    }

    @Override
    public int getItemViewType(int position) {
        return transactions.get(position).getDate() != null ? 1 : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;

        if (viewType == 0) {
            v =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.stock_transaction_list_item, parent, false);
            return new TransactionViewHolder(v);
        } else {
            v =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.stock_transaction_date_item, parent, false);
            return new DateViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        TransactionsListRow rowItem = transactions.get(position);

        if (viewHolder.getItemViewType() == 0) {
            Transaction transaction = rowItem.getTransaction();
            TransactionViewHolder holder = (TransactionViewHolder) viewHolder;

            holder.transactionType.setText(
                    "Market " + transaction.getTransactionType().toLowerCase());
            holder.symbol.setText(transaction.getSymbol());
            holder.price.setText(Formatters.formatPrice(transaction.getTotalPrice()));
            holder.time.setText(transaction.getTransactionTimeString());
            holder.pricePerShare.setText(
                    Formatters.formatPricePerShare(
                            transaction.getShares(), transaction.getPrice()));
        } else {
            DateViewHolder holder = (DateViewHolder) viewHolder;
            holder.date.setText(rowItem.getDateString());
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
        public final TextView price;
        public final TextView pricePerShare;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);

            this.view = itemView;
            time = this.view.findViewById(R.id.transaction_time);
            transactionType = this.view.findViewById(R.id.transaction_type);
            symbol = this.view.findViewById(R.id.symbol);
            price = this.view.findViewById(R.id.cost_shares);
            pricePerShare = this.view.findViewById(R.id.price_per_share);
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
