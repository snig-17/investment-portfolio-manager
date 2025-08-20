package com.snig.investmentportfoliomanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    // Database variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType; // BUY, SELL, DIVIDEND, SPLIT

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    @Column(precision = 15, scale = 4, nullable = false)
    private BigDecimal quantity; // Number of shares

    @NotNull(message = "Price per share is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price_per_share", precision = 15, scale = 4, nullable = false)
    private BigDecimal pricePerShare; // Price paid/received per share

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount; // Total transaction value (quantity × price)

    @DecimalMin(value = "0.0", inclusive = true, message = "Commission cannot be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO; // Trading fees

    @DecimalMin(value = "0.0", inclusive = true, message = "Fees cannot be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO; // Other fees (SEC, etc.)

    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount; // Total amount after fees

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "settlement_date")
    private LocalDateTime settlementDate; // When trade actually settles (T+2)

    @NotNull(message = "Transaction status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 500)
    private String notes; // Optional notes about the transaction

    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId; // ID from broker or external system

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //database relationships
    // Many transactions belong to one portfolio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    // Many transactions reference one asset
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // Many transactions can belong to one position (buying/selling same stock over time)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    //constructors for object creation
    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.transactionDate = LocalDateTime.now();
        this.status = TransactionStatus.PENDING;
        this.commission = BigDecimal.ZERO;
        this.fees = BigDecimal.ZERO;
    }

    public Transaction(TransactionType type, Portfolio portfolio, Asset asset,
                       BigDecimal quantity, BigDecimal pricePerShare) {
        this();
        this.transactionType = type;
        this.portfolio = portfolio;
        this.asset = asset;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        calculateTotalAmount();
        calculateNetAmount();
        calculateSettlementDate();
    }

    //lifecycle callback
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    //business logic methods

    //calculate total transaction amount
    public void calculateTotalAmount() {
        this.totalAmount = this.quantity.multiply(this.pricePerShare)
                .setScale(2, RoundingMode.HALF_UP);
    }

    // Business logic methods (Financial calculations!)

    //Calculate total transaction amount (quantity × price)

    public void calculateTotalAmount() {
        this.totalAmount = this.quantity.multiply(this.pricePerShare)
                .setScale(2, RoundingMode.HALF_UP);
    }

    //Calculate net amount after fees and commissions

    public void calculateNetAmount() {
        if (transactionType == TransactionType.BUY) {
            // For buys: add fees to total cost
            this.netAmount = this.totalAmount.add(this.commission).add(this.fees);
        } else if (transactionType == TransactionType.SELL) {
            // For sells: subtract fees from proceeds
            this.netAmount = this.totalAmount.subtract(this.commission).subtract(this.fees);
        } else {
            this.netAmount = this.totalAmount;
        }
    }

    //Calculate settlement date (T+2 for stocks)

    public void calculateSettlementDate() {
        if (asset != null) {
            switch (asset.getAssetType()) {
                case STOCK:
                case ETF:
                    // Stocks settle in 2 business days
                    this.settlementDate = transactionDate.plusDays(2);
                    break;
                case BOND:
                    // Bonds settle in 1 business day
                    this.settlementDate = transactionDate.plusDays(1);
                    break;
                case CRYPTO:
                    // Crypto settles immediately
                    this.settlementDate = transactionDate;
                    break;
                default:
                    this.settlementDate = transactionDate.plusDays(1);
            }
        }
    }

    //Execute the transaction (update position and portfolio)

    public void execute() {
        if (status == TransactionStatus.PENDING) {
            if (transactionType == TransactionType.BUY) {
                executeBuy();
            } else if (transactionType == TransactionType.SELL) {
                executeSell();
            }
            this.status = TransactionStatus.EXECUTED;
        }
    }

    //Execute a buy transaction

    private void executeBuy() {
        // Find or create position for this asset in the portfolio
        Position existingPosition = findOrCreatePosition();

        // Add shares to position
        existingPosition.buyShares(quantity, pricePerShare);

        // Deduct cash from portfolio
        if (portfolio.getCashBalance().compareTo(netAmount) >= 0) {
            portfolio.setCashBalance(portfolio.getCashBalance().subtract(netAmount));
            portfolio.updateTotalValue();
        }

        this.position = existingPosition;
    }

    //Execute a sell transaction

    private void executeSell() {
        // Find existing position
        Position existingPosition = findOrCreatePosition();

        // Sell shares from position
        if (existingPosition.sellShares(quantity, pricePerShare)) {
            // Add cash to portfolio
            portfolio.setCashBalance(portfolio.getCashBalance().add(netAmount));
            portfolio.updateTotalValue();

            this.position = existingPosition;
        }
    }

    //Find existing position or create new one

    private Position findOrCreatePosition() {
        // In a real application, this would query the database
        // For now, we'll create a new position
        if (position == null) {
            position = new Position(portfolio, asset, BigDecimal.ZERO, pricePerShare);
        }
        return position;
    }

    //Cancel the transaction

    public void cancel() {
        if (status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.CANCELLED;
        }
    }

    //Fail the transaction

    public void fail(String reason) {
        if (status == TransactionStatus.PENDING) {
            this.status = TransactionStatus.FAILED;
            this.notes = reason;
        }
    }

    //Check if transaction is a buy

    public boolean isBuy() {
        return transactionType == TransactionType.BUY;
    }

    //Check if transaction is a sell

    public boolean isSell() {
        return transactionType == TransactionType.SELL;
    }

    //Check if transaction is executed

    public boolean isExecuted() {
        return status == TransactionStatus.EXECUTED;
    }

    //Check if transaction is settled

    public boolean isSettled() {
        return settlementDate != null && LocalDateTime.now().isAfter(settlementDate);
    }

    //Get total fees (commission + other fees)

    public BigDecimal getTotalFees() {
        return commission.add(fees);
    }

    //Get formatted total amount

    public String getFormattedTotalAmount() {
        return "$" + totalAmount.toString();
    }

    //Get formatted net amount

    public String getFormattedNetAmount() {
        return "$" + netAmount.toString();
    }

    //Get transaction description

    public String getDescription() {
        String action = transactionType.name().toLowerCase();
        return String.format("%s %s shares of %s at %s",
                action, quantity.toString(),
                asset != null ? asset.getSymbol() : "Unknown",
                "$" + pricePerShare.toString());
    }

    // Getters and Setters
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
        calculateNetAmount(); // Recalculate when type changes
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateTotalAmount();
        calculateNetAmount();
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(BigDecimal pricePerShare) {
        this.pricePerShare = pricePerShare;
        calculateTotalAmount();
        calculateNetAmount();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
        calculateNetAmount();
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
        calculateNetAmount();
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
        calculateSettlementDate();
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

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
        calculateSettlementDate();
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type=" + transactionType +
                ", asset=" + (asset != null ? asset.getSymbol() : "null") +
                ", quantity=" + quantity +
                ", price=" + pricePerShare +
                ", total=" + getFormattedTotalAmount() +
                ", status=" + status +
                '}';
    }

}