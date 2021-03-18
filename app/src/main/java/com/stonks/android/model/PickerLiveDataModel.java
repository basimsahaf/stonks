package com.stonks.android.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PickerLiveDataModel extends ViewModel {

    private MutableLiveData<Integer> numberOfStocks;
    private MutableLiveData<Integer> currentPrice;

    public MutableLiveData<Integer> getNumberOfStocks() {
        if (numberOfStocks == null) {
            numberOfStocks = new MutableLiveData<Integer>();
        }
        return numberOfStocks;
    }

    public MutableLiveData<Integer> getCurrentPrice() {
        if (currentPrice == null) {
            currentPrice = new MutableLiveData<Integer>();
        }
        return currentPrice;
    }
}
