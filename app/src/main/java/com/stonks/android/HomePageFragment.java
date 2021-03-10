package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stonks.android.adapter.PortfolioRecyclerViewAdapter;
import com.stonks.android.model.PortfolioListItem;

import java.util.ArrayList;

public class HomePageFragment extends Fragment {
    public HomePageFragment() {}

    public static HomePageFragment newInstance() {
        HomePageFragment fragment = new HomePageFragment();
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
        return inflater.inflate(R.layout.fragment_home_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView.LayoutManager portfolioListManager = new LinearLayoutManager(this.getContext());
        RecyclerView portfolioList = view.findViewById(R.id.profolio_list);
        portfolioList.setLayoutManager(portfolioListManager);
        RecyclerView.Adapter portfolioListAdapter = new PortfolioRecyclerViewAdapter(this.getMockItems());
        portfolioList.setAdapter(portfolioListAdapter);
    }

    private ArrayList<PortfolioListItem> getMockItems() {
        ArrayList<PortfolioListItem> list = new ArrayList<>();
        PortfolioListItem item = new PortfolioListItem("SYM", "Company Name", 19.80f, 9, 2.29f, 4.85f);

        for (int i = 0; i < 15; i++) {
            list.add(item);
        }

        return list;
    }
}