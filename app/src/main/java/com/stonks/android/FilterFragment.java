package com.stonks.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;
import com.stonks.android.adapter.CompanyFilterListAdapter;
import java.util.ArrayList;

public class FilterFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private RecyclerView.Adapter companiesFilterListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView companiesFilterList;

        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowTitleEnabled(false);
        getMainActivity().hideActionBarCustomViews();

        RecyclerView.LayoutManager companiesFilterListManager =
                new LinearLayoutManager(this.getContext());
        companiesFilterList = view.findViewById(R.id.companies_list);
        companiesFilterList.setLayoutManager(companiesFilterListManager);
        companiesFilterListAdapter = new CompanyFilterListAdapter(getCompanyList(), this);
        companiesFilterList.setAdapter(companiesFilterListAdapter);

        setupRadioListeners(view);
        setupResetButton(view);
        setupResetAllButton(view);
    }

    private void setupRadioListeners(View view) {
        RadioButton allRadio = view.findViewById(R.id.radio_all);
        RadioButton buyRadio = view.findViewById(R.id.radio_buy);
        RadioButton sellRadio = view.findViewById(R.id.radio_sell);

        allRadio.setOnClickListener(this);
        buyRadio.setOnClickListener(this);
        sellRadio.setOnClickListener(this);
    }

    private void setupResetButton(View view) {
        TextView reset = view.findViewById(R.id.reset_button);

        reset.setOnClickListener(v -> {
            TextInputLayout min = view.findViewById(R.id.min_field);
            min.getEditText().getText().clear();

            TextInputLayout max = view.findViewById(R.id.max_field);
            max.getEditText().getText().clear();
        });
    }

    private void setupResetAllButton(View view) {
        TextView resetAll = view.findViewById(R.id.reset_all_button);

        resetAll.setOnClickListener(v -> {
            RadioButton allRadio = view.findViewById(R.id.radio_all);
            allRadio.setChecked(true);

            TextView reset = view.findViewById(R.id.reset_button);
            reset.performClick();

            companiesFilterListAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();
    }

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        MaterialCheckBox checkBox = ((MaterialCheckBox) view);
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
