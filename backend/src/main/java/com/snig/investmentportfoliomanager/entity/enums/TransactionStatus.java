package com.snig.investmentportfoliomanager.entity.enums;


public enum TransactionStatus {

    /**
     * Transaction has been created but not yet processed
     * Initial state for all new transactions
     * Transaction can still be modified or cancelled
     */
    PENDING("Pending", "Transaction created but not yet processed", true, false),

    /**
     * Transaction is currently being processed
     * Intermediate state during execution
     * Transaction cannot be modified
     */
    PROCESSING("Processing", "Transaction is being executed", false, false),

    /**
     * Transaction has been successfully completed
     * Final state for successful transactions
     * All portfolio updates have been applied
     */
    COMPLETED("Completed", "Transaction successfully executed", false, true),

    /**
     * Transaction execution failed
     * Final state for unsuccessful transactions
     * Portfolio remains unchanged, reason should be documented
     */
    FAILED("Failed", "Transaction execution failed", false, true),

    /**
     * Transaction was cancelled before execution
     * Final state for cancelled transactions
     * Portfolio remains unchanged
     */
    CANCELLED("Cancelled", "Transaction was cancelled", false, true),

    /**
     * Transaction is awaiting settlement
     * Trade executed but cash/securities not yet settled
     * Common state during T+2 settlement period
     */
    SETTLING("Settling", "Transaction executed, awaiting settlement", false, false),

    /**
     * Transaction settlement failed
     * Trade was executed but settlement failed
     * May require manual intervention
     */
    SETTLEMENT_FAILED("Settlement Failed", "Transaction settlement failed", false, true),

    /**
     * Transaction is being reviewed
     * May be flagged for compliance or risk review
     * Temporary hold state
     */
    UNDER_REVIEW("Under Review", "Transaction is being reviewed", false, false),

    /**
     * Transaction was rejected by the system
     * Failed validation or risk checks
     * Final state for rejected transactions
     */
    REJECTED("Rejected", "Transaction was rejected", false, true),

    /**
     * Transaction is partially filled
     * Some quantity executed, remainder still pending
     * Common for large orders or illiquid securities
     */
    PARTIALLY_FILLED("Partially Filled", "Transaction partially executed", true, false),

    /**
     * Transaction expired without execution
     * Time-limited orders that were not filled
     * Final state for expired transactions
     */
    EXPIRED("Expired", "Transaction expired without execution", false, true);

    // ========================
    // ENUM PROPERTIES
    // ========================

    /**
     * Human-readable display name
     */
    private final String displayName;

    /**
     * Detailed description of the status
     */
    private final String description;

    /**
     * Whether transaction can be modified in this status
     */
    private final boolean canModify;

    /**
     * Whether this is a final status (no further transitions)
     */
    private final boolean isFinal;

    // ========================
    // CONSTRUCTOR
    // ========================

    /**
     * Constructor for TransactionStatus enum values
     * 
     * @param displayName User-friendly name for display
     * @param description Detailed explanation of the status
     * @param canModify Whether transaction can be modified in this state
     * @param isFinal Whether this is a terminal state
     */
    TransactionStatus(String displayName, String description, boolean canModify, boolean isFinal) {
        this.displayName = displayName;
        this.description = description;
        this.canModify = canModify;
        this.isFinal = isFinal;
    }

    // ========================
    // GETTERS
    // ========================

    /**
     * Get the display name for this transaction status
     * 
     * @return Human-readable status name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the description for this transaction status
     * 
     * @return Detailed description of the status
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if transaction can be modified in this status
     * 
     * @return true if transaction can be modified
     */
    public boolean canModify() {
        return canModify;
    }

    /**
     * Check if this is a final status
     * 
     * @return true if this is a terminal state
     */
    public boolean isFinal() {
        return isFinal;
    }

    // ========================
    // UTILITY METHODS
    // ========================

    /**
     * Check if this is a successful status
     * 
     * @return true if status indicates successful transaction
     */
    public boolean isSuccessful() {
        return this == COMPLETED || this == SETTLING;
    }

    /**
     * Check if this is a failure status
     * 
     * @return true if status indicates failed transaction
     */
    public boolean isFailure() {
        return this == FAILED || this == REJECTED || this == SETTLEMENT_FAILED || this == EXPIRED;
    }

    /**
     * Check if this is an active status
     * Active means transaction is still being processed
     * 
     * @return true if transaction is actively being processed
     */
    public boolean isActive() {
        return this == PENDING || this == PROCESSING || this == SETTLING || 
               this == UNDER_REVIEW || this == PARTIALLY_FILLED;
    }

    /**
     * Check if transaction requires attention
     * Statuses that may need manual intervention
     * 
     * @return true if status requires attention
     */
    public boolean requiresAttention() {
        return this == FAILED || this == SETTLEMENT_FAILED || this == UNDER_REVIEW || 
               this == REJECTED || this == PARTIALLY_FILLED;
    }

    /**
     * Get the color code for UI display
     * Provides standard color coding for status display
     * 
     * @return Color code (GREEN, YELLOW, RED, BLUE, GRAY)
     */
    public String getColorCode() {
        switch (this) {
            case COMPLETED:
                return "GREEN";

            case PENDING:
            case PROCESSING:
            case SETTLING:
                return "BLUE";

            case UNDER_REVIEW:
            case PARTIALLY_FILLED:
                return "YELLOW";

            case FAILED:
            case REJECTED:
            case SETTLEMENT_FAILED:
            case EXPIRED:
                return "RED";

            case CANCELLED:
            default:
                return "GRAY";
        }
    }

    /**
     * Get valid next statuses from current status
     * Defines allowed status transitions
     * 
     * @return Array of valid next statuses
     */
    public TransactionStatus[] getValidNextStatuses() {
        switch (this) {
            case PENDING:
                return new TransactionStatus[]{PROCESSING, CANCELLED, REJECTED, UNDER_REVIEW};

            case PROCESSING:
                return new TransactionStatus[]{COMPLETED, FAILED, PARTIALLY_FILLED, SETTLING};

            case SETTLING:
                return new TransactionStatus[]{COMPLETED, SETTLEMENT_FAILED};

            case UNDER_REVIEW:
                return new TransactionStatus[]{PROCESSING, REJECTED, CANCELLED};

            case PARTIALLY_FILLED:
                return new TransactionStatus[]{COMPLETED, CANCELLED, EXPIRED};

            case COMPLETED:
            case FAILED:
            case CANCELLED:
            case REJECTED:
            case SETTLEMENT_FAILED:
            case EXPIRED:
            default:
                return new TransactionStatus[]{};  // Final states have no valid transitions
        }
    }

    /**
     * Check if transition to another status is valid
     * 
     * @param nextStatus The status to transition to
     * @return true if transition is allowed
     */
    public boolean canTransitionTo(TransactionStatus nextStatus) {
        TransactionStatus[] validNext = getValidNextStatuses();
        for (TransactionStatus status : validNext) {
            if (status == nextStatus) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get statuses that indicate transaction completion
     * Either successful or unsuccessful completion
     * 
     * @return Array of completion statuses
     */
    public static TransactionStatus[] getCompletionStatuses() {
        return new TransactionStatus[]{
            COMPLETED, FAILED, CANCELLED, REJECTED, 
            SETTLEMENT_FAILED, EXPIRED
        };
    }

    /**
     * Get statuses that indicate transaction is in progress
     * 
     * @return Array of in-progress statuses
     */
    public static TransactionStatus[] getInProgressStatuses() {
        return new TransactionStatus[]{
            PENDING, PROCESSING, SETTLING, UNDER_REVIEW, PARTIALLY_FILLED
        };
    }

    /**
     * Get statuses that indicate successful transaction
     * 
     * @return Array of success statuses
     */
    public static TransactionStatus[] getSuccessStatuses() {
        return new TransactionStatus[]{COMPLETED, SETTLING};
    }

    /**
     * Get statuses that indicate failed transaction
     * 
     * @return Array of failure statuses
     */
    public static TransactionStatus[] getFailureStatuses() {
        return new TransactionStatus[]{
            FAILED, REJECTED, SETTLEMENT_FAILED, EXPIRED
        };
    }

    /**
     * Find TransactionStatus by display name (case-insensitive)
     * 
     * @param displayName The display name to search for
     * @return TransactionStatus if found, null otherwise
     */
    public static TransactionStatus findByDisplayName(String displayName) {
        for (TransactionStatus status : values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Get transaction processing priority based on status
     * Higher values indicate higher priority
     * 
     * @return Priority level (1-5, where 5 is highest priority)
     */
    public int getProcessingPriority() {
        switch (this) {
            case SETTLEMENT_FAILED:
            case UNDER_REVIEW:
                return 5;  // Highest priority - needs immediate attention

            case PARTIALLY_FILLED:
            case PROCESSING:
                return 4;  // High priority - active processing

            case SETTLING:
                return 3;  // Medium priority - awaiting settlement

            case PENDING:
                return 2;  // Low priority - waiting to start

            case COMPLETED:
            case FAILED:
            case CANCELLED:
            case REJECTED:
            case EXPIRED:
            default:
                return 1;  // Lowest priority - completed states
        }
    }

    @Override
    public String toString() {
        return displayName + " (" + name() + ")";
    }
}