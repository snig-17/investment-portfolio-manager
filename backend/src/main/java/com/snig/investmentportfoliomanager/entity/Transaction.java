package com.snig.investmentportfoliomanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.snig.investmentportfoliomanager.entity.enums.TransactionType;
import com.snig.investmentportfoliomanager.entity.enums.TransactionStatus;


@Entity
@Table(name = "transactions",
       indexes = {
           @Index(name = "idx_transaction_portfolio", columnList = "portfolio_id"),
           @Index(name = "idx_transaction_asset", columnList = "asset_id"),
           @Index(name = "idx_transaction_date", columnList = "transaction_date"),
           @Index(name = "idx_transaction_type", columnList = "transaction_type"),
           @Index(name = "idx_transaction_status", columnList = "status")
       })
public class Transaction {

    /**
     * Primary key - Auto-generated transaction ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    /**
     * Transaction type (BUY, SELL, DIVIDEND, etc.)
     * Uses enum for type safety and consistency
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    /**
     * Number of shares/units involved in transaction
     * Can be fractional for certain asset types
     * Precision: 6 decimal places for fractional shares
     */
    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.000001", message = "Quantity must be positive")
    @Digits(integer = 13, fraction = 6, message = "Invalid quantity format")
    private BigDecimal quantity;

    /**
     * Price per share/unit at time of transaction
     * Historical price for accurate record keeping
     */
    @Column(name = "price_per_share", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Price per share is required")
    @DecimalMin(value = "0.0001", message = "Price per share must be positive")
    @Digits(integer = 15, fraction = 4, message = "Invalid price format")
    private BigDecimal pricePerShare;

    /**
     * Total transaction amount (quantity × price)
     * Before fees and commissions
     */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be positive")
    @Digits(integer = 17, fraction = 2, message = "Invalid total amount format")
    private BigDecimal totalAmount;

    /**
     * Trading fees and commissions
     * Important for accurate cost basis calculations
     */
    @Column(name = "fees", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Fees are required (use 0.00 if no fees)")
    @DecimalMin(value = "0.00", message = "Fees must be non-negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid fees format")
    private BigDecimal fees = BigDecimal.ZERO;

    /**
     * Net amount after fees
     * For BUY: total_amount + fees (cash outflow)
     * For SELL: total_amount - fees (cash inflow)
     */
    @Column(name = "net_amount", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Net amount is required")
    @Digits(integer = 17, fraction = 2, message = "Invalid net amount format")
    private BigDecimal netAmount;

    /**
     * Transaction execution date and time
     * When the trade was actually executed
     */
    @Column(name = "transaction_date", nullable = false)
    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

    /**
     * Settlement date
     * When cash and securities actually change hands (T+2 for most equities)
     */
    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;

    /**
     * Transaction status tracking
     * PENDING, COMPLETED, FAILED, CANCELLED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Transaction status is required")
    private TransactionStatus status;

    /**
     * Optional notes or description
     * For manual entries or special circumstances
     */
    @Column(name = "notes", length = 500)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    /**
     * External reference ID
     * For integration with brokers or trading systems
     */
    @Column(name = "external_reference_id", length = 100)
    @Size(max = 100, message = "External reference ID must not exceed 100 characters")
    private String externalReferenceId;

    /**
     * Record creation timestamp
     * When transaction was entered into the system
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Many-to-One relationship with Portfolio entity
     * Each transaction belongs to exactly one portfolio
     * 
     * FetchType.LAZY: Portfolio loaded only when accessed
     * JoinColumn: Foreign key to portfolios table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_portfolio"))
    @NotNull(message = "Transaction must belong to a portfolio")
    private Portfolio portfolio;

    /**
     * Many-to-One relationship with Asset entity
     * Each transaction involves exactly one asset type
     * 
     * FetchType.EAGER: Asset data commonly needed for transaction processing
     * JoinColumn: Foreign key to assets table
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_asset"))
    @NotNull(message = "Transaction must reference an asset")
    private Asset asset;

    // ========================
    // CONSTRUCTORS
    // ========================

    /**
     * Default constructor required by JPA
     */
    public Transaction() {
    }

    /**
     * Constructor for creating new transactions
     * 
     * @param transactionType Type of transaction (BUY, SELL, etc.)
     * @param quantity Number of shares/units
     * @param pricePerShare Execution price per unit
     * @param fees Trading fees and commissions
     * @param transactionDate When transaction occurred
     * @param portfolio Portfolio involved in transaction
     * @param asset Asset being traded
     */
    public Transaction(TransactionType transactionType, BigDecimal quantity, 
                      BigDecimal pricePerShare, BigDecimal fees, 
                      LocalDateTime transactionDate, Portfolio portfolio, Asset asset) {
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.fees = fees != null ? fees : BigDecimal.ZERO;
        this.transactionDate = transactionDate;
        this.portfolio = portfolio;
        this.asset = asset;

        // Calculate derived fields
        this.totalAmount = quantity.multiply(pricePerShare);
        this.netAmount = calculateNetAmount();
        this.status = TransactionStatus.PENDING;

        // Calculate settlement date (T+2 for most securities)
        this.settlementDate = transactionDate.plusDays(2);
    }

    // ========================
    // GETTERS AND SETTERS
    // ========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        // Recalculate net amount when transaction type changes
        this.netAmount = calculateNetAmount();
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        // Recalculate amounts when quantity changes
        if (pricePerShare != null) {
            this.totalAmount = quantity.multiply(pricePerShare);
            this.netAmount = calculateNetAmount();
        }
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(BigDecimal pricePerShare) {
        this.pricePerShare = pricePerShare;
        // Recalculate amounts when price changes
        if (quantity != null) {
            this.totalAmount = quantity.multiply(pricePerShare);
            this.netAmount = calculateNetAmount();
        }
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
        this.netAmount = calculateNetAmount();
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDateTime settlementDate) {
        this.settlementDate = settlementDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    // ========================
    // BUSINESS LOGIC METHODS
    // ========================

    /**
     * Calculate net amount based on transaction type
     * 
     * @return Net cash flow amount
     */
    private BigDecimal calculateNetAmount() {
        if (totalAmount == null || fees == null) {
            return BigDecimal.ZERO;
        }

        switch (transactionType) {
            case BUY:
                // For purchases: negative cash flow (outgoing)
                return totalAmount.add(fees).negate();

            case SELL:
                // For sales: positive cash flow (incoming)
                return totalAmount.subtract(fees);

            case DIVIDEND:
                // For dividends: positive cash flow (incoming)
                return totalAmount.subtract(fees);

            case SPLIT:
            case MERGER:
                // Corporate actions typically don't involve cash
                return BigDecimal.ZERO;

            default:
                return totalAmount.subtract(fees);
        }
    }

    /**
     * Check if transaction affects cash balance
     * 
     * @return true if transaction involves cash movement
     */
    public boolean affectsCash() {
        return transactionType == TransactionType.BUY || 
               transactionType == TransactionType.SELL || 
               transactionType == TransactionType.DIVIDEND;
    }

    /**
     * Check if transaction affects position quantity
     * 
     * @return true if transaction changes share count
     */
    public boolean affectsPosition() {
        return transactionType == TransactionType.BUY || 
               transactionType == TransactionType.SELL || 
               transactionType == TransactionType.SPLIT;
    }

    /**
     * Get effective cost per share (including fees)
     * Used for accurate cost basis calculations
     * 
     * @return Cost per share including proportional fees
     */
    public BigDecimal getEffectiveCostPerShare() {
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalCostWithFees = totalAmount.add(fees);
        return totalCostWithFees.divide(quantity, 4, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Get transaction description for reporting
     * 
     * @return Human-readable transaction description
     */
    public String getTransactionDescription() {
        String assetSymbol = asset != null ? asset.getTickerSymbol() : "Unknown";

        switch (transactionType) {
            case BUY:
                return String.format("Bought %.2f shares of %s at $%.2f", 
                                   quantity, assetSymbol, pricePerShare);

            case SELL:
                return String.format("Sold %.2f shares of %s at $%.2f", 
                                   quantity, assetSymbol, pricePerShare);

            case DIVIDEND:
                return String.format("Dividend payment from %s: $%.2f", 
                                   assetSymbol, totalAmount);

            case SPLIT:
                return String.format("Stock split for %s: %.2f shares", 
                                   assetSymbol, quantity);

            case MERGER:
                return String.format("Merger transaction for %s: %.2f shares", 
                                   assetSymbol, quantity);

            default:
                return String.format("Transaction for %s: %.2f shares at $%.2f", 
                                   assetSymbol, quantity, pricePerShare);
        }
    }

    /**
     * Check if transaction is settled
     * 
     * @return true if settlement date has passed and status is completed
     */
    public boolean isSettled() {
        return status == TransactionStatus.COMPLETED && 
               settlementDate != null && 
               LocalDateTime.now().isAfter(settlementDate);
    }

    /**
     * Get transaction age in days
     * 
     * @return Number of days since transaction date
     */
    public long getTransactionAgeInDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(
            transactionDate.toLocalDate(), 
            LocalDateTime.now().toLocalDate()
        );
    }

    /**
     * Check if transaction is recent (within 30 days)
     * 
     * @return true if transaction occurred within last 30 days
     */
    public boolean isRecentTransaction() {
        return getTransactionAgeInDays() <= 30;
    }

    /**
     * Complete the transaction
     * Changes status to COMPLETED and finalizes all calculations
     */
    public void complete() {
        if (status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.COMPLETED;

            // Ensure net amount is calculated
            if (netAmount == null) {
                this.netAmount = calculateNetAmount();
            }
        }
    }

    /**
     * Cancel the transaction
     * Changes status to CANCELLED
     */
    public void cancel(String reason) {
        if (status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.CANCELLED;
            this.notes = (notes != null ? notes + "; " : "") + "Cancelled: " + reason;
        }
    }

    /**
     * Fail the transaction
     * Changes status to FAILED with reason
     */
    public void fail(String reason) {
        if (status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.FAILED;
            this.notes = (notes != null ? notes + "; " : "") + "Failed: " + reason;
        }
    }

    // ========================
    // OBJECT METHODS
    // ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction transaction = (Transaction) o;
        return id != null && id.equals(transaction.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type=" + transactionType +
                ", asset=" + (asset != null ? asset.getTickerSymbol() : "null") +
                ", quantity=" + quantity +
                ", pricePerShare=" + pricePerShare +
                ", totalAmount=" + totalAmount +
                ", fees=" + fees +
                ", netAmount=" + netAmount +
                ", status=" + status +
                ", transactionDate=" + transactionDate +
                ", settlementDate=" + settlementDate +
                ", description='" + getTransactionDescription() + "'" +
                '}';
    }
}