package com.exceptions;

public class MACAddressError extends Error{
    public MACAddressError() {
        super("Failed to retrieve MAC address, potentially there is an issue in your network.\n" +
                "If problem persists after resolving network issues.\n" +
                "Please contact aki@akizhou.com");
    }
}