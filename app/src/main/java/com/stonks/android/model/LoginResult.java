package com.stonks.android.model;

import androidx.annotation.Nullable;

/** Authentication result : success (user details) or error message. */
public class LoginResult {
    private boolean status;
    @Nullable private Integer error;

    public LoginResult(@Nullable Integer error) {
        this.error = error;
    }

    public LoginResult(boolean status) {
        this.status = status;
    }

    public boolean getSuccess() {
        return this.status;
    }

    @Nullable
    public Integer getError() {
        return error;
    }
}
