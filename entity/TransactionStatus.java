package com.snig.investmentportfoliomanager.entity;

public enum TransactionStatus {
    PENDING("Pending"),
    EXECUTED("Executed"),
    SETTLED("Settled"),
    CANCELLED("Cancelled"),
    FAILED("Failed");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
