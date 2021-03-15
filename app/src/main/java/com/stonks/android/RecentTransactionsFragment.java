package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.stonks.android.adapter.TransactionViewAdapter;

/**
 * A simple {@link Fragment} subclass. Use the {@link RecentTransactionsFragment#newInstance}
 * factory method to create an instance of this fragment.
 */
public class RecentTransactionsFragment extends Fragment {
    public RecentTransactionsFragment() {}

    public static RecentTransactionsFragment newInstance(String param1, String param2) {
        RecentTransactionsFragment fragment = new RecentTransactionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recent_transactions, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView transactionList;
        RecyclerView.Adapter transactionListAdapter;

        RecyclerView.LayoutManager transactionListManager =
                new LinearLayoutManager(this.getContext());
        transactionList = view.findViewById(R.id.history_list);
        transactionList.setLayoutManager(transactionListManager);
        transactionListAdapter = new TransactionViewAdapter(StockActivity.getFakeTransactions());
        transactionList.setAdapter(transactionListAdapter);
    }
}
