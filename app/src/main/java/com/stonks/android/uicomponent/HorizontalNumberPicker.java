package com.stonks.android.uicomponent;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.stonks.android.R;
import com.stonks.android.model.PickerLiveDataModel;

public class HorizontalNumberPicker extends LinearLayout {
    private final EditText etNumber;
    private int max;
    PickerLiveDataModel model;

    public HorizontalNumberPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.numberpicker_horizontal, this);

        max = 10000; // init here for now
        etNumber = findViewById(R.id.et_number);

        final MaterialButton btnLess = findViewById(R.id.btn_less);
        btnLess.setOnClickListener(new AddHandler(-1));

        final MaterialButton btnMore = findViewById(R.id.btn_more);
        btnMore.setOnClickListener(new AddHandler(1));
    }

    /** * HANDLERS */
    private class AddHandler implements OnClickListener {
        final int diff;

        public AddHandler(int diff) {
            this.diff = diff;
        }

        @Override
        public void onClick(View v) {
            int newValue = getValue() + diff;
            if (newValue < 0) {
                newValue = 0;
            } else if (newValue > max) {
                newValue = max;
            }
            etNumber.setText(String.valueOf(newValue));
            model.getNumberOfStocks().setValue(newValue);
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

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setModel(PickerLiveDataModel hModel) {
        this.model = hModel;
    }
}
