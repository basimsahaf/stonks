package com.stonks.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.stonks.android.FilterFragment;
import com.stonks.android.R;
import java.util.ArrayList;

public class CompanyFilterListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<String> companies;
    FilterFragment parentFragment;
    ArrayList<String> checkedCompanies;

    public CompanyFilterListAdapter(
            ArrayList<String> companies,
            FilterFragment fragment,
            ArrayList<String> checkedCompanies) {
        this.companies = companies;
        this.parentFragment = fragment;
        this.checkedCompanies = checkedCompanies;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.company_checkbox_item, parent, false);
        return new CompanyFilterListAdapter.CompanyFilterListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        String company = companies.get(position);
        CompanyFilterListAdapter.CompanyFilterListViewHolder holder =
                (CompanyFilterListAdapter.CompanyFilterListViewHolder) viewHolder;

        holder.checkBox.setText(company);
        holder.checkBox.setOnCheckedChangeListener(parentFragment);
        holder.checkBox.setChecked(checkedCompanies.contains(company));
    }

    @Override
    public int getItemCount() {
        if (this.companies != null) {
            return this.companies.size();
        }

        return 0;
    }

    public static class CompanyFilterListViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final MaterialCheckBox checkBox;

        public CompanyFilterListViewHolder(@NonNull View itemView) {
            super(itemView);

            this.view = itemView;
            checkBox = this.view.findViewById(R.id.checkbox);
        }
    }
}
