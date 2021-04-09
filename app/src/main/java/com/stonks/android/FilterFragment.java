package com.stonks.android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.stonks.android.adapter.CompanyFilterListAdapter;
import com.stonks.android.manager.RecentTransactionsManager;
import com.stonks.android.model.TransactionMode;
import com.stonks.android.storage.CompanyTable;

public class FilterFragment extends BaseFragment
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private CompanyFilterListAdapter companiesFilterListAdapter;
    private RecentTransactionsManager recentTransactionsManager;
    private String TAG = CompanyTable.class.getCanonicalName();

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

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getMainActivity().setGlobalTitle(getString(R.string.filters));

        RecyclerView.LayoutManager companiesFilterListManager =
                new LinearLayoutManager(this.getContext());
        companiesFilterList = view.findViewById(R.id.companies_list);
        companiesFilterList.setLayoutManager(companiesFilterListManager);
        companiesFilterListAdapter =
                new CompanyFilterListAdapter(
                        recentTransactionsManager.getSymbols(),
                        this,
                        recentTransactionsManager.filters.getSymbols());
        companiesFilterList.setAdapter(companiesFilterListAdapter);

        setupRadioListeners(view);
        setupResetMinMaxAmountButton(view);
        setupResetAllButton(view);
        setupMinAmountInputEdit(view);
        setupMaxAmountInputEdit(view);

        MaterialButton button = view.findViewById(R.id.apply_button);
        button.setOnClickListener(
                v -> {
                    super.getActivity().onBackPressed();
                });
    }

    public void setRecentTransactionsManager(RecentTransactionsManager recentTransactionsManager) {
        this.recentTransactionsManager = recentTransactionsManager;
    }

    private void setupRadioListeners(View view) {
        RadioButton allRadio = view.findViewById(R.id.radio_all);
        RadioButton buyRadio = view.findViewById(R.id.radio_buy);
        RadioButton sellRadio = view.findViewById(R.id.radio_sell);

        if (recentTransactionsManager.filters.isModeFilterApplied()) {
            TransactionMode modeFilter = recentTransactionsManager.filters.getMode();
            if (modeFilter == TransactionMode.BUY) {
                buyRadio.setChecked(true);
            } else if (modeFilter == TransactionMode.SELL) {
                sellRadio.setChecked(true);
            }
        }

        allRadio.setOnClickListener(
                v -> {
                    recentTransactionsManager.resetModeFilter();
                });

        buyRadio.setOnClickListener(
                v -> {
                    recentTransactionsManager.applyModeFilter(TransactionMode.BUY);
                });

        sellRadio.setOnClickListener(
                v -> {
                    recentTransactionsManager.applyModeFilter(TransactionMode.SELL);
                });
    }

    private void setupResetMinMaxAmountButton(View view) {
        TextView reset = view.findViewById(R.id.reset_button);

        reset.setOnClickListener(
                v -> {
                    TextInputLayout min = view.findViewById(R.id.min_field);
                    min.getEditText().getText().clear();

                    TextInputLayout max = view.findViewById(R.id.max_field);
                    max.getEditText().getText().clear();

                    recentTransactionsManager.resetMinMaxAmount();
                });
    }

    private void setupMinAmountInputEdit(View view) {
        TextInputEditText min = view.findViewById(R.id.min_input);
        min.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String minAmount = min.getText().toString();
                        if (minAmount.isEmpty()) {
                            return;
                        }
                        recentTransactionsManager.applyMinAmountFilter(Integer.parseInt(minAmount));
                    }
                });

        if (recentTransactionsManager.filters.isMinAmountFilterApplied()) {
            int minAmount = recentTransactionsManager.filters.getMinAmount();
            min.setText(Integer.toString(minAmount));
        }
    }

    private void setupMaxAmountInputEdit(View view) {
        TextInputEditText max = view.findViewById(R.id.max_input);
        max.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String maxAmount = max.getText().toString();
                        if (maxAmount.isEmpty()) {
                            return;
                        }
                        recentTransactionsManager.applyMaxAmountFilter(Integer.parseInt(maxAmount));
                    }
                });

        if (recentTransactionsManager.filters.isMaxAmountFilterApplied()) {
            int maxAmount = recentTransactionsManager.filters.getMaxAmount();
            max.setText(Integer.toString(maxAmount));
        }
    }

    private void setupResetAllButton(View view) {
        TextView resetAll = view.findViewById(R.id.reset_all_button);

        resetAll.setOnClickListener(
                v -> {
                    RadioButton allRadio = view.findViewById(R.id.radio_all);
                    allRadio.setChecked(true);

                    TextView reset = view.findViewById(R.id.reset_button);
                    reset.performClick();

                    recentTransactionsManager.resetFilters();
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
        checkBox.setChecked(isChecked);
        String symbol = checkBox.getText().toString();
        if (isChecked) {
            recentTransactionsManager.applySymbolFilter(symbol);
        } else {
            recentTransactionsManager.unapplySymbolFilter(symbol);
        }
    }
}
