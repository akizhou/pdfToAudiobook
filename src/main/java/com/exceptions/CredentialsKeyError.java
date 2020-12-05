package com.exceptions;

public class CredentialsKeyError extends Error{
    public CredentialsKeyError() {
        super("Failed to authenticate.\nEnsure stable internet connection and then restart the program.\n" +
                "If problem persists, it may be due to invalid credentials or service account has been inactivated by project admin.\n" +
                "Please contact aki@akizhou.com");
    }
}
