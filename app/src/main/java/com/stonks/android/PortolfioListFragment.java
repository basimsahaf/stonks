package com.stonks.android;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stonks.android.adapter.PortfolioRecyclerViewAdapter;
import com.stonks.android.adapter.TransactionViewAdapter;
import com.stonks.android.model.PortfolioListItem;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PortolfioListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortolfioListFragment extends Fragment {
    private RecyclerView portfolioList;
    private RecyclerView.Adapter portfolioListAdapter;

    public PortolfioListFragment() {
        // Required empty public constructor
    }

    public static PortolfioListFragment newInstance(String param1, String param2) {
        PortolfioListFragment fragment = new PortolfioListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_portolfio_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView.LayoutManager portfolioListManager = new LinearLayoutManager(this.getContext());
        this.portfolioList = view.findViewById(R.id.profolio_list);
        this.portfolioList.setLayoutManager(portfolioListManager);
        this.portfolioListAdapter = new PortfolioRecyclerViewAdapter(this.getMockItems());
        this.portfolioList.setAdapter(this.portfolioListAdapter);
    }

    private ArrayList<PortfolioListItem> getMockItems() {
        ArrayList<PortfolioListItem> list = new ArrayList<>();
        PortfolioListItem item = new PortfolioListItem("SYM", "Company Name", 19.80f, 9, 2.29f, 4.85f);

        for (int i = 0; i <= 10; i++) {
            list.add(item);
        }

        return list;
    }
}