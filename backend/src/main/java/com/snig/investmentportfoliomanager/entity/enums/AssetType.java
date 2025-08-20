package com.snig.investmentportfoliomanager.entity.enums;


public enum AssetType {

    /**
     * Common stock equity securities
     * Represents ownership shares in publicly traded companies
     * Examples: AAPL, MSFT, GOOGL
     */
    STOCK("Stock", "Equity securities representing ownership in corporations"),

    /**
     * Government and corporate debt securities
     * Fixed income instruments with defined maturity and interest payments
     * Examples: US Treasury bonds, corporate bonds, municipal bonds
     */
    BOND("Bond", "Debt securities with fixed income payments"),

    /**
     * Exchange-Traded Funds
     * Investment funds that trade on stock exchanges like individual stocks
     * Examples: SPY, QQQ, VTI
     */
    ETF("ETF", "Exchange-traded funds providing diversified exposure"),

    /**
     * Mutual Funds
     * Pooled investment vehicles managed by professional fund managers
     * Examples: Vanguard funds, Fidelity funds, BlackRock funds
     */
    MUTUAL_FUND("Mutual Fund", "Professionally managed pooled investment funds"),

    /**
     * Real Estate Investment Trusts
     * Companies that own, operate, or finance income-generating real estate
     * Examples: REITs investing in commercial, residential, or specialized properties
     */
    REIT("REIT", "Real Estate Investment Trusts"),

    /**
     * Cryptocurrency and digital assets
     * Digital or virtual currencies secured by cryptography
     * Examples: Bitcoin, Ethereum, other altcoins
     */
    CRYPTOCURRENCY("Cryptocurrency", "Digital assets and cryptocurrencies"),

    /**
     * Commodity investments
     * Physical goods and raw materials used in commerce
     * Examples: Gold, silver, oil, agricultural products
     */
    COMMODITY("Commodity", "Physical goods and raw materials"),

    /**
     * Foreign exchange currency pairs
     * Trading pairs between different national currencies
     * Examples: EUR/USD, GBP/JPY, USD/CHF
     */
    FOREX("Forex", "Foreign exchange currency pairs"),

    /**
     * Derivative financial instruments
     * Financial contracts whose value derives from underlying assets
     * Examples: Options, futures, swaps, forwards
     */
    DERIVATIVE("Derivative", "Financial instruments derived from underlying assets"),

    /**
     * Alternative investment vehicles
     * Non-traditional investments including private equity, hedge funds, etc.
     * Examples: Private equity funds, hedge funds, venture capital
     */
    ALTERNATIVE("Alternative", "Alternative investment vehicles and strategies"),

    /**
     * Cash and cash equivalents
     * Highly liquid, short-term instruments
     * Examples: Money market funds, treasury bills, savings accounts
     */
    CASH("Cash", "Cash and cash equivalents"),

    /**
     * Preferred stock securities
     * Hybrid securities with characteristics of both stocks and bonds
     * Examples: Preferred shares with fixed dividend payments
     */
    PREFERRED_STOCK("Preferred Stock", "Preferred equity securities with fixed dividends"),

    /**
     * Convertible securities
     * Bonds or preferred stocks that can be converted to common stock
     * Examples: Convertible bonds, convertible preferred shares
     */
    CONVERTIBLE("Convertible", "Securities convertible to common stock"),

    /**
     * Structured products
     * Pre-packaged investment strategies based on derivatives
     * Examples: Market-linked CDs, equity-linked notes
     */
    STRUCTURED_PRODUCT("Structured Product", "Pre-packaged derivative-based investment products"),

    /**
     * Other or miscellaneous asset types
     * Assets that don't fit into standard categories
     * Used for unusual or emerging asset classes
     */
    OTHER("Other", "Miscellaneous or unclassified asset types");

    // ========================
    // ENUM PROPERTIES
    // ========================

    /**
     * Human-readable display name
     */
    private final String displayName;

    /**
     * Detailed description of the asset type
     */
    private final String description;

    // ========================
    // CONSTRUCTOR
    // ========================

    /**
     * Constructor for AssetType enum values
     * 
     * @param displayName User-friendly name for display
     * @param description Detailed explanation of the asset type
     */
    AssetType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    // ========================
    // GETTERS
    // ========================

    /**
     * Get the display name for this asset type
     * 
     * @return Human-readable asset type name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the description for this asset type
     * 
     * @return Detailed description of the asset type
     */
    public String getDescription() {
        return description;
    }

    // ========================
    // UTILITY METHODS
    // ========================

    /**
     * Check if this asset type represents an equity instrument
     * Includes stocks, ETFs, REITs, and preferred stocks
     * 
     * @return true if asset type is equity-based
     */
    public boolean isEquity() {
        return this == STOCK || this == ETF || this == REIT || 
               this == PREFERRED_STOCK || this == MUTUAL_FUND;
    }

    /**
     * Check if this asset type represents a fixed income instrument
     * Includes bonds and other debt securities
     * 
     * @return true if asset type is fixed income
     */
    public boolean isFixedIncome() {
        return this == BOND || this == CONVERTIBLE;
    }

    /**
     * Check if this asset type represents an alternative investment
     * Includes commodities, forex, derivatives, and alternatives
     * 
     * @return true if asset type is alternative
     */
    public boolean isAlternative() {
        return this == COMMODITY || this == FOREX || this == DERIVATIVE || 
               this == ALTERNATIVE || this == CRYPTOCURRENCY || 
               this == STRUCTURED_PRODUCT;
    }

    /**
     * Check if this asset type is liquid (easily tradeable)
     * Most exchange-traded instruments are considered liquid
     * 
     * @return true if asset type is typically liquid
     */
    public boolean isLiquid() {
        return this == STOCK || this == ETF || this == BOND || 
               this == FOREX || this == CASH || this == CRYPTOCURRENCY;
    }

    /**
     * Check if this asset type typically pays dividends or interest
     * 
     * @return true if asset type typically generates income
     */
    public boolean isIncomeGenerating() {
        return this == STOCK || this == BOND || this == REIT || 
               this == PREFERRED_STOCK || this == MUTUAL_FUND || 
               this == CONVERTIBLE || this == CASH;
    }

    /**
     * Get the risk category for this asset type
     * Provides general risk classification for portfolio analysis
     * 
     * @return Risk category (LOW, MEDIUM, HIGH)
     */
    public String getRiskCategory() {
        switch (this) {
            case CASH:
            case BOND:
                return "LOW";

            case ETF:
            case MUTUAL_FUND:
            case REIT:
            case PREFERRED_STOCK:
            case CONVERTIBLE:
                return "MEDIUM";

            case STOCK:
            case COMMODITY:
            case FOREX:
            case DERIVATIVE:
            case ALTERNATIVE:
            case CRYPTOCURRENCY:
            case STRUCTURED_PRODUCT:
            case OTHER:
                return "HIGH";

            default:
                return "MEDIUM";
        }
    }

    /**
     * Get typical allocation range for this asset type in a diversified portfolio
     * Returns suggested allocation percentage range
     * 
     * @return Allocation range as string (e.g., "50-70%")
     */
    public String getTypicalAllocationRange() {
        switch (this) {
            case STOCK:
            case ETF:
                return "50-70%";

            case BOND:
                return "20-40%";

            case CASH:
                return "5-10%";

            case REIT:
                return "5-15%";

            case COMMODITY:
            case ALTERNATIVE:
                return "0-10%";

            case CRYPTOCURRENCY:
                return "0-5%";

            case MUTUAL_FUND:
                return "10-50%";

            case PREFERRED_STOCK:
            case CONVERTIBLE:
                return "0-15%";

            case FOREX:
            case DERIVATIVE:
            case STRUCTURED_PRODUCT:
                return "0-5%";

            case OTHER:
            default:
                return "0-10%";
        }
    }

    /**
     * Get asset types suitable for conservative portfolios
     * 
     * @return Array of conservative asset types
     */
    public static AssetType[] getConservativeAssetTypes() {
        return new AssetType[]{CASH, BOND, PREFERRED_STOCK, CONVERTIBLE};
    }

    /**
     * Get asset types suitable for moderate portfolios
     * 
     * @return Array of moderate asset types
     */
    public static AssetType[] getModerateAssetTypes() {
        return new AssetType[]{STOCK, ETF, BOND, MUTUAL_FUND, REIT, CASH};
    }

    /**
     * Get asset types suitable for aggressive portfolios
     * 
     * @return Array of aggressive asset types
     */
    public static AssetType[] getAggressiveAssetTypes() {
        return new AssetType[]{STOCK, ETF, CRYPTOCURRENCY, COMMODITY, 
                               ALTERNATIVE, DERIVATIVE, FOREX};
    }

    /**
     * Find AssetType by display name (case-insensitive)
     * 
     * @param displayName The display name to search for
     * @return AssetType if found, null otherwise
     */
    public static AssetType findByDisplayName(String displayName) {
        for (AssetType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName + " (" + name() + ")";
    }
}