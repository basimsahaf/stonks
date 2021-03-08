package com.stonks.android;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HypotheticalModeActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HypotheticalModeActivity extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ImageButton cancelButton;
    TextView stockSymbol;
    TextView stockCompanyName;
    TextView lastPriceLabel;
    TextView lastPrice;
    TextView estimatedCostLabel;
    TextView estimatedCost;
    TextView estimatedValueLabel;
    TextView estimatedValue;



    public HypotheticalModeActivity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HypotheticalMode.
     */
    // TODO: Rename and change types and number of parameters
    public static HypotheticalModeActivity newInstance(String param1, String param2) {
        HypotheticalModeActivity fragment = new HypotheticalModeActivity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hypothetical_mode, null);
        this.cancelButton = view.findViewById(R.id.cancel_button_image);

        return inflater.inflate(R.layout.fragment_hypothetical_mode, container, false);
    }
}