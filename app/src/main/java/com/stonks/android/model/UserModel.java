package com.stonks.android.model;

public class UserModel {

    String username;
    String password;
    boolean biometricsEnabled;

    public UserModel(String username, String password, boolean biometricsEnabled) {
        this.username = username;
        this.password = password;
        this.biometricsEnabled = biometricsEnabled;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean getBiometricsEnabled() {
        return biometricsEnabled;
    }
}
