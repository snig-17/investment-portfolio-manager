package com.snig.investmentportfoliomanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "portfolios",
       indexes = {
           @Index(name = "idx_portfolio_user", columnList = "user_id"),
           @Index(name = "idx_portfolio_name", columnList = "name"),
           @Index(name = "idx_portfolio_created", columnList = "created_at")
       })
public class Portfolio {

    /**
     * Primary key - Auto-generated portfolio ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long id;

    /**
     * Portfolio name/title for identification
     * Must be unique per user to avoid confusion
     */
    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Portfolio name is required")
    @Size(max = 100, message = "Portfolio name must not exceed 100 characters")
    private String name;

    /**
     * Optional portfolio description
     * Helps users identify portfolio purpose and strategy
     */
    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Initial cash investment amount
     * Precision: 2 decimal places for currency calculations
     * Scale: 19 digits total (supports up to $999,999,999,999,999.99)
     */
    @Column(name = "initial_cash", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Initial cash amount is required")
    @DecimalMin(value = "0.00", message = "Initial cash must be non-negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid cash amount format")
    private BigDecimal initialCash;

    /**
     * Current available cash balance
     * Updated automatically through transactions
     * Precision maintained for accurate financial calculations
     */
    @Column(name = "current_cash", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Current cash balance is required")
    @DecimalMin(value = "0.00", message = "Current cash must be non-negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid cash balance format")
    private BigDecimal currentCash;

    /**
     * Portfolio creation timestamp
     * Used for performance calculations and audit trails
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     * Updated when portfolio metadata changes
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Many-to-One relationship with User entity
     * Each portfolio belongs to exactly one user
     * 
     * FetchType.LAZY: User loaded only when accessed
     * JoinColumn: Foreign key column in portfolios table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_portfolio_user"))
    @NotNull(message = "Portfolio must belong to a user")
    private User user;

    /**
     * One-to-Many relationship with Position entity
     * A portfolio contains multiple asset positions
     * 
     * CascadeType.ALL: Position lifecycle managed with portfolio
     * FetchType.LAZY: Positions loaded only when needed
     * orphanRemoval: Positions removed from portfolio are deleted
     */
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Position> positions = new ArrayList<>();

    /**
     * One-to-Many relationship with Transaction entity
     * Portfolio maintains history of all transactions
     * 
     * Transactions are never deleted (audit compliance)
     * Ordered by transaction date for historical analysis
     */
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    // ========================
    // CONSTRUCTORS
    // ========================

    /**
     * Default constructor required by JPA
     */
    public Portfolio() {
    }

    /**
     * Constructor for creating new portfolios
     * 
     * @param name Portfolio name
     * @param description Portfolio description
     * @param initialCash Starting cash amount
     * @param user Portfolio owner
     */
    public Portfolio(String name, String description, BigDecimal initialCash, User user) {
        this.name = name;
        this.description = description;
        this.initialCash = initialCash;
        this.currentCash = initialCash; // Initially, current cash equals initial cash
        this.user = user;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getInitialCash() {
        return initialCash;
    }

    public void setInitialCash(BigDecimal initialCash) {
        this.initialCash = initialCash;
    }

    public BigDecimal getCurrentCash() {
        return currentCash;
    }

    public void setCurrentCash(BigDecimal currentCash) {
        this.currentCash = currentCash;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // ========================
    // BUSINESS LOGIC METHODS
    // ========================

    /**
     * Calculate total portfolio value
     * Includes current cash + market value of all positions
     * 
     * @return Total portfolio value in BigDecimal
     */
    public BigDecimal getTotalValue() {
        BigDecimal positionsValue = positions.stream()
            .map(Position::getMarketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return currentCash.add(positionsValue);
    }

    /**
     * Calculate total profit/loss since inception
     * Compares current total value to initial cash investment
     * 
     * @return Profit/loss amount (positive = profit, negative = loss)
     */
    public BigDecimal getTotalProfitLoss() {
        return getTotalValue().subtract(initialCash);
    }

    /**
     * Calculate percentage return since inception
     * Formula: (Current Value - Initial Cash) / Initial Cash * 100
     * 
     * @return Percentage return as BigDecimal
     */
    public BigDecimal getReturnPercentage() {
        if (initialCash.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return getTotalProfitLoss().divide(initialCash, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
    }

    /**
     * Get portfolio cash allocation percentage
     * Shows what percentage of portfolio is in cash vs investments
     * 
     * @return Cash percentage of total portfolio
     */
    public BigDecimal getCashAllocationPercentage() {
        BigDecimal totalValue = getTotalValue();
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentCash.divide(totalValue, 4, RoundingMode.HALF_UP)
                         .multiply(new BigDecimal("100"));
    }

    /**
     * Get number of positions in portfolio
     * 
     * @return Count of distinct asset positions
     */
    public int getPositionCount() {
        return positions != null ? positions.size() : 0;
    }

    /**
     * Check if portfolio has sufficient cash for transaction
     * 
     * @param amount Required cash amount
     * @return true if sufficient cash available
     */
    public boolean hasSufficientCash(BigDecimal amount) {
        return currentCash.compareTo(amount) >= 0;
    }

    /**
     * Add cash to portfolio (e.g., from deposits)
     * 
     * @param amount Cash amount to add
     */
    public void addCash(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            this.currentCash = this.currentCash.add(amount);
        }
    }

    /**
     * Subtract cash from portfolio (e.g., for purchases, withdrawals)
     * 
     * @param amount Cash amount to subtract
     * @throws IllegalArgumentException if insufficient funds
     */
    public void subtractCash(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (!hasSufficientCash(amount)) {
            throw new IllegalArgumentException("Insufficient cash balance");
        }

        this.currentCash = this.currentCash.subtract(amount);
    }

    /**
     * Add a position to this portfolio
     * 
     * @param position The position to add
     */
    public void addPosition(Position position) {
        positions.add(position);
        position.setPortfolio(this);
    }

    /**
     * Remove a position from this portfolio
     * 
     * @param position The position to remove
     */
    public void removePosition(Position position) {
        positions.remove(position);
        position.setPortfolio(null);
    }

    /**
     * Add a transaction to this portfolio
     * 
     * @param transaction The transaction to add
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setPortfolio(this);
    }

    /**
     * Find position by asset
     * 
     * @param asset The asset to find position for
     * @return Position if found, null otherwise
     */
    public Position findPositionByAsset(Asset asset) {
        return positions.stream()
            .filter(position -> position.getAsset().equals(asset))
            .findFirst()
            .orElse(null);
    }

    // ========================
    // OBJECT METHODS
    // ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Portfolio)) return false;
        Portfolio portfolio = (Portfolio) o;
        return id != null && id.equals(portfolio.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Portfolio{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", initialCash=" + initialCash +
                ", currentCash=" + currentCash +
                ", totalValue=" + getTotalValue() +
                ", returnPercentage=" + getReturnPercentage() + "%" +
                ", positionCount=" + getPositionCount() +
                ", createdAt=" + createdAt +
                '}';
    }
}