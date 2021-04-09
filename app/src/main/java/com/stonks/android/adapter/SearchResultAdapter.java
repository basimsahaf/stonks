package com.stonks.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.stonks.android.R;
import com.stonks.android.model.SearchResult;
import java.util.ArrayList;

public class SearchResultAdapter extends BaseAdapter {
    private ArrayList<SearchResult> searchResultList;
    Context context;
    LayoutInflater inflater;

    public SearchResultAdapter(Context context) {
        this.context = context;
        this.searchResultList = new ArrayList<>();
        inflater = LayoutInflater.from(context);
    }

    public class ViewHolder {
        public View view;
        public TextView companyName;
        public TextView symbol;

        public ViewHolder(@NonNull View itemView) {
            view = itemView;
            companyName = this.view.findViewById(R.id.company_name);
            symbol = this.view.findViewById(R.id.symbol);
        }
    }

    @Override
    public int getCount() {
        if (this.searchResultList == null) {
            return 0;
        }

        return this.searchResultList.size();
    }

    @Override
    public SearchResult getItem(int position) {
        return this.searchResultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.search_result_list_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        SearchResult searchResultItem = getItem(position);
        viewHolder.companyName.setText(searchResultItem.getCompanyName());
        viewHolder.symbol.setText(searchResultItem.getSymbol());

        return view;
    }

    public void updateSearchResultList(ArrayList<SearchResult> newSearchResultList) {
        this.searchResultList = newSearchResultList;
        notifyDataSetChanged();
    }
}
