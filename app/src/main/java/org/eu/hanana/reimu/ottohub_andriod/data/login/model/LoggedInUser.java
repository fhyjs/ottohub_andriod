package org.eu.hanana.reimu.ottohub_andriod.data.login.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private int userId;
    private String displayName;

    public LoggedInUser(int userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public int getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}