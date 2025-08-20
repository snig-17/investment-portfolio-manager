package com.snig.investmentportfoliomanager.entity;

public enum TransactionType {
    BUY("Buy"),
    SELL("Sell"),
    DIVIDEND("Dividend"),
    STOCK_SPLIT("Stock Split"),
    TRANSFER_IN("Transfer In"),
    TRANSFER_OUT("Transfer Out"),
    INTEREST("Interest"),
    FEE("Fee");

    private final String displayName;

    TransactionType(String displayName) {
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
