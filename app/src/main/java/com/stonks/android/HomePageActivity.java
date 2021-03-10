package com.stonks.android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stonks.android.adapter.PortfolioRecyclerViewAdapter;
import com.stonks.android.model.PortfolioListItem;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {
    private RecyclerView portfolioList;
    private RecyclerView.Adapter portfolioListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        RecyclerView.LayoutManager portfolioListManager = new LinearLayoutManager(this);
        this.portfolioList = findViewById(R.id.profolio_list);
        this.portfolioList.setLayoutManager(portfolioListManager);
        this.portfolioListAdapter = new PortfolioRecyclerViewAdapter(this.getMockItems());
        this.portfolioList.setAdapter(this.portfolioListAdapter);
    }

    private ArrayList<PortfolioListItem> getMockItems() {
        ArrayList<PortfolioListItem> list = new ArrayList<>();
        PortfolioListItem item = new PortfolioListItem("SYM", "Company Name", 19.80f, 9, 2.29f, 4.85f);

        for (int i = 0; i <= 3; i++) {
            list.add(item);
        }

        return list;
    }
}