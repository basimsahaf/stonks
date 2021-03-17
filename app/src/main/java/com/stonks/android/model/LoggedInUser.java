package com.stonks.android.model;

/** Data class that captures user information for logged in users retrieved from LoginRepository */
public class LoggedInUser {

    private String userId;

    public LoggedInUser(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}