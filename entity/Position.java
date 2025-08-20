package com.snig.investmentportfoliomanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "positions")
public class Postion {
    //database variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Quantity cannot be negative")
    @Column(precision = 15, scale = 4, nullable = false)
    private BigDecimal quantity; // How many shares we own

    @NotNull(message = "Average cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Average cost must be greater than 0")
    @Column(name = "average_cost", precision = 15, scale = 4, nullable = false)
    private BigDecimal averageCost; // Average price paid per share

    @Column(name = "total_cost", precision = 15, scale = 2)
    private BigDecimal totalCost; // Total amount invested (quantity × average_cost)

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue; // Current market value (quantity × current_price)

    @Column(name = "unrealized_gain_loss", precision = 15, scale = 2)
    private BigDecimal unrealizedGainLoss; // Profit/Loss (current_value - total_cost)

    @Column(name = "realized_gain_loss", precision = 15, scale = 2)
    private BigDecimal realizedGainLoss; // Profit/Loss from sales

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    //database relationships
    // many positions belong to one portoflio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    // many positions can reference one asset
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // one position can have many transactions
    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    //constructors for object creation

    public Postion() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.quantity = BigDecimal.ZERO;
        this.averageCost = BigDecimal.ZERO;
        this.currentValue = BigDecimal.ZERO;
        this.unrealizedGainLoss = BigDecimal.ZERO;
        this.realizedGainLoss = BigDecimal.ZERO;
    }

    public Position(Portfolio portfolio, Asset asset, BigDecimal quantity, BigDecimal price) {
        this();
        this.portfolio = portfolio;
        this.asset = asset;
        this.quantity = quantity;
        this.averageCost = price;
        calculateTotalCost();
        updateCurrentValue;
    }

    // lifecycle callback
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    //business logic methods

    //calculate total cost

    public void calculateTotalCost() {
        this.totalCost = this.quantity.multiply(this.averageCost).setScale(2, RoundingMode.HALF_UP);

    }

    //update current market values using asset's current price

    public void updateCurrentValue() {
        if (asset != null && asset.getCurrentPrice() != null){
            this.currentValue = this.quantity.multiply(asset.getCurrentPrice()).setScale(2, RoundingMode.HALF_UP);
            calculateUnrealizedGainLoss();
        }
    }
    // Calculate unrealized gain/loss

    public void calculateUnrealizedGainLoss() {
        this.unrealizedGainLoss = this.currentValue.subtract(this.totalCost);
    }

    //Get unrealized gain/loss percentage

    public BigDecimal getUnrealizedGainLossPercent() {
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            return unrealizedGainLoss.divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    // Buy more shares
    public void buyShares(BigDecimal additionalQuantity, BigDecimal pricePerShare) {
        if (additionalQuantity.compareTo(BigDecimal.ZERO) > 0) {
            // Calculate new average cost using weighted average
            BigDecimal currentTotal = this.quantity.multiply(this.averageCost);
            BigDecimal additionalTotal = additionalQuantity.multiply(pricePerShare);
            BigDecimal newTotalQuantity = this.quantity.add(additionalQuantity);

            if (newTotalQuantity.compareTo(BigDecimal.ZERO) > 0) {
                this.averageCost = currentTotal.add(additionalTotal)
                        .divide(newTotalQuantity, 4, RoundingMode.HALF_UP);
            }

            this.quantity = newTotalQuantity;
            calculateTotalCost();
            updateCurrentValue();
        }
    }

    // Sell shares
    public boolean sellShares(BigDecimal quantityToSell, BigDecimal pricePerShare) {
        if (quantityToSell.compareTo(BigDecimal.ZERO) > 0 &&
                this.quantity.compareTo(quantityToSell) >= 0) {


            BigDecimal soldCost = quantityToSell.multiply(this.averageCost);
            BigDecimal soldValue = quantityToSell.multiply(pricePerShare);
            BigDecimal gainLoss = soldValue.subtract(soldCost);

            this.realizedGainLoss = this.realizedGainLoss.add(gainLoss);
            this.quantity = this.quantity.subtract(quantityToSell);

            calculateTotalCost();
            updateCurrentValue();

            return true;
        }
        return false;
    }

    //Check if this position is profitable
    public boolean isProfitable() {
        return unrealizedGainLoss.compareTo(BigDecimal.ZERO) > 0;
    }

    // Check if this position is at a loss
    public boolean isAtLoss() {
        return unrealizedGainLoss.compareTo(BigDecimal.ZERO) < 0;
    }

    // Get total gain/loss (realized + unrealized)

    public BigDecimal getTotalGainLoss() {
        return realizedGainLoss.add(unrealizedGainLoss);
    }

    // what percentage of portfolio is this position
    public BigDecimal getPortfolioWeight(BigDecimal portfolioTotalValue) {
        if (portfolioTotalValue.compareTo(BigDecimal.ZERO) > 0) {
            return currentValue.divide(portfolioTotalValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    //Check if position exists (has shares)

    public boolean hasShares() {
        return quantity.compareTo(BigDecimal.ZERO) > 0;
    }

    //Get formatted current value as string

    public String getFormattedCurrentValue() {
        return "$" + currentValue.toString();
    }

    //Get formatted gain/loss with + or - sign

    public String getFormattedGainLoss() {
        String sign = unrealizedGainLoss.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + "$" + unrealizedGainLoss.toString();
    }

    //helper methods for relationships
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setPosition(this);
    }
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateTotalCost();
        updateCurrentValue();
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
        calculateTotalCost();
        updateCurrentValue();
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
        calculateUnrealizedGainLoss();
    }

    public BigDecimal getUnrealizedGainLoss() {
        return unrealizedGainLoss;
    }

    public void setUnrealizedGainLoss(BigDecimal unrealizedGainLoss) {
        this.unrealizedGainLoss = unrealizedGainLoss;
    }

    public BigDecimal getRealizedGainLoss() {
        return realizedGainLoss;
    }

    public void setRealizedGainLoss(BigDecimal realizedGainLoss) {
        this.realizedGainLoss = realizedGainLoss;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
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
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", asset=" + (asset != null ? asset.getSymbol() : "null") +
                ", quantity=" + quantity +
                ", averageCost=" + averageCost +
                ", currentValue=" + getFormattedCurrentValue() +
                ", gainLoss=" + getFormattedGainLoss() +
                '}';
    }
}

}