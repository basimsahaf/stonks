package com.stonks.android;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.stonks.android.model.HypotheticalViewModel;

import java.util.Observable;

public class HorizontalNumberPicker extends LinearLayout {
    private EditText etNumber;
    private int min, max;
    HypotheticalViewModel model;

    public HorizontalNumberPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.numberpicker_horizontal, this);

        min = 0; // should probably let user know that max is 10000
        max = 10000;

        etNumber = findViewById(R.id.et_number);

        final Button btnLess = findViewById(R.id.btn_less);
        btnLess.setOnClickListener(new AddHandler(-1));

        final Button btnMore = findViewById(R.id.btn_more);
        btnMore.setOnClickListener(new AddHandler(1));
    }

    // addObserver(TextView)

    /** * HANDLERS */
    private class AddHandler implements OnClickListener {
        final int diff;

        public AddHandler(int diff) {
            this.diff = diff;
        }

        @Override
        public void onClick(View v) {
            int newValue = getValue() + diff;
            if (newValue < min) {
                newValue = min;
            } else if (newValue > max) {
                newValue = max;
            }
            model.getNumberOfStocks().setValue(newValue);
            etNumber.setText(String.valueOf(newValue));
        }
    }

    /** * GETTERS & SETTERS */
    public int getValue() {
        if (etNumber != null) {
            try {
                final String value = etNumber.getText().toString();
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                Log.e("HorizontalNumberPicker", ex.toString());
            }
        }
        return 0;
    }

    public void setValue(final int value) {
        if (etNumber != null) {
            etNumber.setText(String.valueOf(value));
        }
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setModel(HypotheticalViewModel hModel) {
        this.model = hModel;
    }
}
