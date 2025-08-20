package com.snig.investmentportfoliomanager.entity.enums;


public enum TransactionType {

    /**
     * Purchase of securities
     * Increases position quantity and decreases cash balance
     * Most common transaction type for building positions
     */
    BUY("Buy", "Purchase of securities", true, false, true),

    /**
     * Sale of securities
     * Decreases position quantity and increases cash balance
     * Used for position reduction, rebalancing, or profit-taking
     */
    SELL("Sell", "Sale of securities", false, true, true),

    /**
     * Dividend payments received
     * Cash payment from stock holdings, increases cash balance
     * No impact on position quantity
     */
    DIVIDEND("Dividend", "Dividend payment received", false, true, false),

    /**
     * Interest payments received
     * Cash payment from bond holdings or cash positions
     * Common with fixed income securities
     */
    INTEREST("Interest", "Interest payment received", false, true, false),

    /**
     * Stock split transactions
     * Increases position quantity proportionally, no cash impact
     * Adjusts cost basis to maintain total position value
     */
    SPLIT("Split", "Stock split adjustment", true, false, false),

    /**
     * Stock dividend (additional shares)
     * Receives additional shares instead of cash dividend
     * Increases position quantity without cash impact
     */
    STOCK_DIVIDEND("Stock Dividend", "Stock dividend received", true, false, false),

    /**
     * Spin-off transactions
     * Receives shares in new company from existing holdings
     * Creates new position while maintaining original
     */
    SPINOFF("Spin-off", "Corporate spin-off transaction", true, false, false),

    /**
     * Merger and acquisition transactions
     * Position converted due to corporate merger
     * May involve cash, stock, or combination
     */
    MERGER("Merger", "Merger or acquisition transaction", false, false, false),

    /**
     * Cash deposit into portfolio
     * Increases available cash balance for investments
     * External funding of portfolio
     */
    DEPOSIT("Deposit", "Cash deposit into portfolio", false, true, false),

    /**
     * Cash withdrawal from portfolio
     * Decreases available cash balance
     * Distribution from portfolio to owner
     */
    WITHDRAWAL("Withdrawal", "Cash withdrawal from portfolio", false, false, false),

    /**
     * Transfer of securities in
     * Securities moved into portfolio from external account
     * Increases position without cash impact
     */
    TRANSFER_IN("Transfer In", "Securities transferred into portfolio", true, false, false),

    /**
     * Transfer of securities out
     * Securities moved out of portfolio to external account
     * Decreases position without cash impact
     */
    TRANSFER_OUT("Transfer Out", "Securities transferred out of portfolio", false, false, false),

    /**
     * Rights offering transactions
     * Opportunity to purchase additional shares at discount
     * Usually time-limited corporate action
     */
    RIGHTS("Rights", "Rights offering transaction", true, false, true),

    /**
     * Warrant exercise
     * Exercise of warrant to purchase underlying security
     * Converts warrant into common stock
     */
    WARRANT("Warrant", "Warrant exercise transaction", true, false, true),

    /**
     * Return of capital
     * Partial return of invested capital from company
     * Reduces cost basis rather than generating income
     */
    RETURN_OF_CAPITAL("Return of Capital", "Return of invested capital", false, true, false),

    /**
     * Fee or commission charges
     * Explicit fees charged to portfolio
     * Reduces cash balance, no position impact
     */
    FEE("Fee", "Fee or commission charge", false, false, false),

    /**
     * Tax withholding
     * Taxes withheld from dividends or sales
     * Reduces net cash received
     */
    TAX_WITHHOLDING("Tax Withholding", "Tax withheld from transaction", false, false, false),

    /**
     * Currency exchange
     * Conversion between different currencies
     * For international portfolios
     */
    CURRENCY_EXCHANGE("Currency Exchange", "Currency conversion transaction", false, false, true),

    /**
     * Other miscellaneous transactions
     * Transactions that don't fit standard categories
     * Flexible category for unusual situations
     */
    OTHER("Other", "Miscellaneous transaction", false, false, false);

    // ========================
    // ENUM PROPERTIES
    // ========================

    /**
     * Human-readable display name
     */
    private final String displayName;

    /**
     * Detailed description of the transaction type
     */
    private final String description;

    /**
     * Whether this transaction type increases position quantity
     */
    private final boolean increasesPosition;

    /**
     * Whether this transaction type increases cash balance
     */
    private final boolean increasesCash;

    /**
     * Whether this transaction type typically involves trading fees
     */
    private final boolean typicallyHasFees;

    // ========================
    // CONSTRUCTOR
    // ========================

    /**
     * Constructor for TransactionType enum values
     * 
     * @param displayName User-friendly name for display
     * @param description Detailed explanation of the transaction type
     * @param increasesPosition Whether transaction increases position quantity
     * @param increasesCash Whether transaction increases cash balance
     * @param typicallyHasFees Whether transaction typically incurs fees
     */
    TransactionType(String displayName, String description, boolean increasesPosition, 
                   boolean increasesCash, boolean typicallyHasFees) {
        this.displayName = displayName;
        this.description = description;
        this.increasesPosition = increasesPosition;
        this.increasesCash = increasesCash;
        this.typicallyHasFees = typicallyHasFees;
    }

    // ========================
    // GETTERS
    // ========================

    /**
     * Get the display name for this transaction type
     * 
     * @return Human-readable transaction type name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the description for this transaction type
     * 
     * @return Detailed description of the transaction type
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if this transaction type increases position quantity
     * 
     * @return true if transaction adds to position
     */
    public boolean increasesPosition() {
        return increasesPosition;
    }

    /**
     * Check if this transaction type increases cash balance
     * 
     * @return true if transaction adds cash
     */
    public boolean increasesCash() {
        return increasesCash;
    }

    /**
     * Check if this transaction type typically has fees
     * 
     * @return true if fees are commonly associated with this transaction
     */
    public boolean typicallyHasFees() {
        return typicallyHasFees;
    }

    // ========================
    // UTILITY METHODS
    // ========================

    /**
     * Check if this is a trading transaction (buy/sell)
     * 
     * @return true if transaction involves active trading
     */
    public boolean isTradingTransaction() {
        return this == BUY || this == SELL;
    }

    /**
     * Check if this is an income transaction
     * Transactions that generate income without reducing positions
     * 
     * @return true if transaction represents income
     */
    public boolean isIncomeTransaction() {
        return this == DIVIDEND || this == INTEREST || this == RETURN_OF_CAPITAL;
    }

    /**
     * Check if this is a corporate action
     * Transactions initiated by the company, not the investor
     * 
     * @return true if transaction is a corporate action
     */
    public boolean isCorporateAction() {
        return this == SPLIT || this == STOCK_DIVIDEND || this == SPINOFF || 
               this == MERGER || this == RIGHTS || this == RETURN_OF_CAPITAL;
    }

    /**
     * Check if this is a cash flow transaction
     * Transactions that primarily affect cash balance
     * 
     * @return true if transaction is primarily about cash movement
     */
    public boolean isCashFlowTransaction() {
        return this == DEPOSIT || this == WITHDRAWAL || this == FEE || 
               this == TAX_WITHHOLDING || this == CURRENCY_EXCHANGE;
    }

    /**
     * Check if this is an asset transfer transaction
     * Moving securities without buying or selling
     * 
     * @return true if transaction is an asset transfer
     */
    public boolean isTransferTransaction() {
        return this == TRANSFER_IN || this == TRANSFER_OUT;
    }

    /**
     * Check if this transaction requires price information
     * 
     * @return true if transaction needs price per share data
     */
    public boolean requiresPrice() {
        return this == BUY || this == SELL || this == RIGHTS || 
               this == WARRANT || this == CURRENCY_EXCHANGE;
    }

    /**
     * Check if this transaction affects cost basis
     * 
     * @return true if transaction changes position cost basis
     */
    public boolean affectsCostBasis() {
        return this == BUY || this == SPLIT || this == STOCK_DIVIDEND || 
               this == TRANSFER_IN || this == RETURN_OF_CAPITAL;
    }

    /**
     * Get the cash flow direction for this transaction type
     * 
     * @return "IN" for cash inflows, "OUT" for cash outflows, "NONE" for no cash impact
     */
    public String getCashFlowDirection() {
        if (this == BUY || this == WITHDRAWAL || this == FEE || this == TAX_WITHHOLDING) {
            return "OUT";
        } else if (this == SELL || this == DIVIDEND || this == INTEREST || 
                   this == DEPOSIT || this == RETURN_OF_CAPITAL) {
            return "IN";
        } else {
            return "NONE";
        }
    }

    /**
     * Get transaction types that are suitable for manual entry
     * Excludes complex corporate actions that are typically automated
     * 
     * @return Array of transaction types suitable for manual entry
     */
    public static TransactionType[] getManualEntryTypes() {
        return new TransactionType[]{
            BUY, SELL, DIVIDEND, INTEREST, DEPOSIT, WITHDRAWAL, 
            TRANSFER_IN, TRANSFER_OUT, FEE, OTHER
        };
    }

    /**
     * Get transaction types that represent corporate actions
     * 
     * @return Array of corporate action transaction types
     */
    public static TransactionType[] getCorporateActionTypes() {
        return new TransactionType[]{
            SPLIT, STOCK_DIVIDEND, SPINOFF, MERGER, RIGHTS, 
            WARRANT, RETURN_OF_CAPITAL
        };
    }

    /**
     * Get transaction types that affect portfolio performance
     * Excludes transfers and deposits/withdrawals
     * 
     * @return Array of performance-affecting transaction types
     */
    public static TransactionType[] getPerformanceAffectingTypes() {
        return new TransactionType[]{
            BUY, SELL, DIVIDEND, INTEREST, SPLIT, STOCK_DIVIDEND, 
            SPINOFF, MERGER, RETURN_OF_CAPITAL, FEE
        };
    }

    /**
     * Find TransactionType by display name (case-insensitive)
     * 
     * @param displayName The display name to search for
     * @return TransactionType if found, null otherwise
     */
    public static TransactionType findByDisplayName(String displayName) {
        for (TransactionType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get recommended transaction types for a given asset type
     * 
     * @param assetType The asset type to get transaction types for
     * @return Array of recommended transaction types
     */
    public static TransactionType[] getRecommendedForAssetType(AssetType assetType) {
        switch (assetType) {
            case STOCK:
                return new TransactionType[]{BUY, SELL, DIVIDEND, SPLIT, 
                                           STOCK_DIVIDEND, SPINOFF, MERGER};

            case BOND:
                return new TransactionType[]{BUY, SELL, INTEREST, MERGER};

            case ETF:
            case MUTUAL_FUND:
                return new TransactionType[]{BUY, SELL, DIVIDEND, SPLIT};

            case CASH:
                return new TransactionType[]{DEPOSIT, WITHDRAWAL, INTEREST};

            case CRYPTOCURRENCY:
                return new TransactionType[]{BUY, SELL};

            default:
                return new TransactionType[]{BUY, SELL, DIVIDEND, OTHER};
        }
    }

    @Override
    public String toString() {
        return displayName + " (" + name() + ")";
    }
}