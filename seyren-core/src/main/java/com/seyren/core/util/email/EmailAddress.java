package com.seyren.core.util.email;

public class EmailAddress {
    public String getAddress() {
        return address;
    }

    private final String address;
    public EmailAddress(String address) {
        if (!address.contains("@")) {
            throw new IllegalArgumentException("Email address must have an @ in it.");
        }

        this.address = address;
    }
}
