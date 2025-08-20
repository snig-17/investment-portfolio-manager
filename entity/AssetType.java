package com.snig.investmentportfoliomanager.entity;

public enum AssetType {
    STOCK("Stock"),
    BOND("Bond"),
    ETF("Exchange-Traded Fund"),
    MUTUAL_FUND("Mutual Fund"),
    CRYPTO("Cryptocurrency"),
    COMMODITY("Commodity"),
    REIT("Real Estate Investment Trust");

    private final String displayName;

    AssetType(String displayName) {
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