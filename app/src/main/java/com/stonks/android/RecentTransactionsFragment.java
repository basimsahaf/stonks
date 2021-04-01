package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stonks.android.adapter.TransactionViewAdapter;

public class RecentTransactionsFragment extends BaseFragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recent_transactions, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView transactionList;
        RecyclerView.Adapter transactionListAdapter;
        FloatingActionButton filterButton;

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        RecyclerView.LayoutManager transactionListManager =
                new LinearLayoutManager(this.getContext());
        transactionList = view.findViewById(R.id.history_list);
        transactionList.setLayoutManager(transactionListManager);
        transactionListAdapter = new TransactionViewAdapter(StockFragment.getFakeTransactions());
        transactionList.setAdapter(transactionListAdapter);

        filterButton = view.findViewById(R.id.filter_button);
        filterButton.setOnClickListener(
                v -> {
                    FragmentManager fm = this.getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setCustomAnimations(
                            R.anim.slide_in, // enter
                            0,
                            0,
                            R.anim.slide_out);
                    ft.replace(R.id.fragment_container, new FilterFragment());
                    ft.addToBackStack(null);
                    ft.commit();
                });
    }
}
