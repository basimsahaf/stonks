package com.stonks.android.model;

public class UserModel {

    String username;
    String password;
    int biometricsEnabled;
    float trainingAmount;
    String trainingStartDate;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBiometricsEnabled() {
        return biometricsEnabled;
    }

    public UserModel(
            String username,
            String password,
            int biometricsEnabled,
            float trainingAmount,
            String date) {
        this.username = username;
        this.password = password;
        this.biometricsEnabled = biometricsEnabled;
        this.trainingAmount = trainingAmount;
        this.trainingStartDate = date;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int isBiometricsEnabled() {
        return biometricsEnabled;
    }

    public void setBiometricsEnabled(int biometricsEnabled) {
        this.biometricsEnabled = biometricsEnabled;
    }

    public float getTrainingAmount() {
        return trainingAmount;
    }

    public void setTrainingAmount(float trainingAmount) {
        this.trainingAmount = trainingAmount;
    }

    public String getTrainingStartDate() {
        return trainingStartDate;
    }

    public void setTrainingStartDate(String trainingStartDate) {
        this.trainingStartDate = trainingStartDate;
    }
}
