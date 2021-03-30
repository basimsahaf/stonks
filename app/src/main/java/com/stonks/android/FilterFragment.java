package com.stonks.android;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stonks.android.adapter.CompanyFilterListAdapter;
import com.stonks.android.adapter.TransactionViewAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FilterFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView companiesFilterList;
        RecyclerView.Adapter companiesFilterListAdapter;

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        RecyclerView.LayoutManager companiesFilterListManager =
                new LinearLayoutManager(this.getContext());
        companiesFilterList = view.findViewById(R.id.companies_list);
        companiesFilterList.setLayoutManager(companiesFilterListManager);
        companiesFilterListAdapter = new CompanyFilterListAdapter(getCompanyList());
        companiesFilterList.setAdapter(companiesFilterListAdapter);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
    }

    public void onCompanyCheckboxClicked(View view) {
        boolean checked = ((MaterialCheckBox) view).isChecked();
    }

    private ArrayList<String> getCompanyList() {
        ArrayList<String> list = new ArrayList<>();

        list.add("Blizzard");
        list.add("Nintendo");
        list.add("Shopify");
        list.add("Google");

        return list;
    }
}