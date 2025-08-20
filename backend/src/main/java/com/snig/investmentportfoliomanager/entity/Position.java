package com.snig.investmentportfoliomanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "positions",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"portfolio_id", "asset_id"})
       },
       indexes = {
           @Index(name = "idx_position_portfolio", columnList = "portfolio_id"),
           @Index(name = "idx_position_asset", columnList = "asset_id"),
           @Index(name = "idx_position_quantity", columnList = "quantity")
       })
public class Position {

    /**
     * Primary key - Auto-generated position ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Long id;

    /**
     * Current quantity of shares/units held
     * Can be fractional for assets that support it (ETFs, mutual funds)
     * Precision: 6 decimal places for fractional shares
     */
    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.000001", message = "Quantity must be positive")
    @Digits(integer = 13, fraction = 6, message = "Invalid quantity format")
    private BigDecimal quantity;

    /**
     * Average cost per share/unit
     * Calculated using weighted average cost method
     * Updates automatically when new shares are purchased
     */
    @Column(name = "average_cost", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Average cost is required")
    @DecimalMin(value = "0.0001", message = "Average cost must be positive")
    @Digits(integer = 15, fraction = 4, message = "Invalid average cost format")
    private BigDecimal averageCost;

    /**
     * Total cost basis of the position
     * quantity × averageCost = total investment amount
     * Used for accurate P&L calculations
     */
    @Column(name = "total_cost", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Total cost is required")
    @DecimalMin(value = "0.01", message = "Total cost must be positive")
    @Digits(integer = 17, fraction = 2, message = "Invalid total cost format")
    private BigDecimal totalCost;

    /**
     * Position opening timestamp
     * When the first shares were purchased
     */
    @CreationTimestamp
    @Column(name = "opened_at", nullable = false, updatable = false)
    private LocalDateTime openedAt;

    /**
     * Last modification timestamp
     * Updated when position quantity or cost basis changes
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Many-to-One relationship with Portfolio entity
     * Each position belongs to exactly one portfolio
     * 
     * FetchType.LAZY: Portfolio loaded only when accessed
     * JoinColumn: Foreign key column in positions table
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_position_portfolio"))
    @NotNull(message = "Position must belong to a portfolio")
    private Portfolio portfolio;

    /**
     * Many-to-One relationship with Asset entity
     * Each position holds exactly one type of asset
     * 
     * FetchType.EAGER: Asset data needed for most position operations
     * JoinColumn: Foreign key to assets table
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", nullable = false, foreignKey = @ForeignKey(name = "fk_position_asset"))
    @NotNull(message = "Position must reference an asset")
    private Asset asset;

    // ========================
    // CONSTRUCTORS
    // ========================

    /**
     * Default constructor required by JPA
     */
    public Position() {
    }

    /**
     * Constructor for creating new positions
     * 
     * @param quantity Initial quantity of shares/units
     * @param averageCost Initial cost per share/unit
     * @param portfolio Portfolio that owns this position
     * @param asset Asset being held in this position
     */
    public Position(BigDecimal quantity, BigDecimal averageCost, Portfolio portfolio, Asset asset) {
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.totalCost = quantity.multiply(averageCost);
        this.portfolio = portfolio;
        this.asset = asset;
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        // Recalculate total cost when quantity changes
        if (averageCost != null) {
            this.totalCost = quantity.multiply(averageCost);
        }
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
        // Recalculate total cost when average cost changes
        if (quantity != null) {
            this.totalCost = quantity.multiply(averageCost);
        }
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
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

    // ========================
    // BUSINESS LOGIC METHODS
    // ========================

    /**
     * Calculate current market value of the position
     * quantity × current_price = market value
     * 
     * @return Current market value in BigDecimal
     */
    public BigDecimal getMarketValue() {
        if (asset == null || asset.getCurrentPrice() == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(asset.getCurrentPrice());
    }

    /**
     * Calculate unrealized profit/loss
     * market_value - total_cost = unrealized P&L
     * 
     * @return Profit/loss amount (positive = profit, negative = loss)
     */
    public BigDecimal getUnrealizedProfitLoss() {
        return getMarketValue().subtract(totalCost);
    }

    /**
     * Calculate unrealized profit/loss percentage
     * (market_value - total_cost) / total_cost × 100
     * 
     * @return Percentage return as BigDecimal
     */
    public BigDecimal getUnrealizedProfitLossPercent() {
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal profitLoss = getUnrealizedProfitLoss();
        return profitLoss.divide(totalCost, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
    }

    /**
     * Get position allocation within portfolio
     * position_market_value / portfolio_total_value × 100
     * 
     * @return Position allocation percentage
     */
    public BigDecimal getPortfolioAllocationPercent() {
        if (portfolio == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal portfolioValue = portfolio.getTotalValue();
        if (portfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal positionValue = getMarketValue();
        return positionValue.divide(portfolioValue, 4, java.math.RoundingMode.HALF_UP)
                           .multiply(new BigDecimal("100"));
    }

    /**
     * Calculate daily change in position value
     * Based on asset's daily price change
     * 
     * @return Daily position value change
     */
    public BigDecimal getDailyPositionChange() {
        if (asset == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal dailyPriceChange = asset.getDailyChange();
        return quantity.multiply(dailyPriceChange);
    }

    /**
     * Add shares to position using weighted average cost
     * Updates quantity, average cost, and total cost
     * 
     * @param additionalQuantity Shares to add
     * @param pricePerShare Price paid per new share
     */
    public void addShares(BigDecimal additionalQuantity, BigDecimal pricePerShare) {
        if (additionalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }

        if (pricePerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price per share must be positive");
        }

        // Calculate new weighted average cost
        BigDecimal currentTotalValue = totalCost;
        BigDecimal additionalTotalValue = additionalQuantity.multiply(pricePerShare);
        BigDecimal newTotalValue = currentTotalValue.add(additionalTotalValue);

        BigDecimal newQuantity = quantity.add(additionalQuantity);
        BigDecimal newAverageCost = newTotalValue.divide(newQuantity, 4, java.math.RoundingMode.HALF_UP);

        // Update position
        this.quantity = newQuantity;
        this.averageCost = newAverageCost;
        this.totalCost = newTotalValue;
    }

    /**
     * Remove shares from position (partial or full sale)
     * Maintains average cost basis for remaining shares
     * 
     * @param quantityToRemove Shares to remove
     * @throws IllegalArgumentException if trying to remove more shares than held
     */
    public void removeShares(BigDecimal quantityToRemove) {
        if (quantityToRemove.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive");
        }

        if (quantityToRemove.compareTo(quantity) > 0) {
            throw new IllegalArgumentException("Cannot remove more shares than currently held");
        }

        // Calculate proportional cost reduction
        BigDecimal remainingQuantity = quantity.subtract(quantityToRemove);
        BigDecimal remainingCost = remainingQuantity.multiply(averageCost);

        // Update position
        this.quantity = remainingQuantity;
        this.totalCost = remainingCost;
        // Average cost remains the same for remaining shares
    }

    /**
     * Check if position is profitable
     * 
     * @return true if current market value exceeds cost basis
     */
    public boolean isProfitable() {
        return getUnrealizedProfitLoss().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Check if position size is significant within portfolio
     * Position represents more than 5% of portfolio value
     * 
     * @return true if position is considered significant
     */
    public boolean isSignificantPosition() {
        BigDecimal allocationPercent = getPortfolioAllocationPercent();
        return allocationPercent.compareTo(new BigDecimal("5.0")) > 0;
    }

    /**
     * Get position status description
     * 
     * @return Human-readable position status
     */
    public String getPositionStatus() {
        BigDecimal profitLossPercent = getUnrealizedProfitLossPercent();

        if (profitLossPercent.compareTo(new BigDecimal("10")) > 0) {
            return "Strong Gain";
        } else if (profitLossPercent.compareTo(BigDecimal.ZERO) > 0) {
            return "Gaining";
        } else if (profitLossPercent.compareTo(new BigDecimal("-10")) > 0) {
            return "Losing";
        } else {
            return "Significant Loss";
        }
    }

    /**
     * Calculate cost basis for specific quantity
     * Used for tax reporting and partial sales
     * 
     * @param shareCount Number of shares to calculate cost basis for
     * @return Cost basis for specified shares
     */
    public BigDecimal getCostBasisForShares(BigDecimal shareCount) {
        if (shareCount.compareTo(quantity) > 0) {
            throw new IllegalArgumentException("Cannot calculate cost basis for more shares than held");
        }

        return shareCount.multiply(averageCost);
    }

    // ========================
    // OBJECT METHODS
    // ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return id != null && id.equals(position.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", asset=" + (asset != null ? asset.getTickerSymbol() : "null") +
                ", quantity=" + quantity +
                ", averageCost=" + averageCost +
                ", totalCost=" + totalCost +
                ", marketValue=" + getMarketValue() +
                ", unrealizedPL=" + getUnrealizedProfitLoss() +
                ", unrealizedPLPercent=" + getUnrealizedProfitLossPercent() + "%" +
                ", portfolioAllocation=" + getPortfolioAllocationPercent() + "%" +
                ", status='" + getPositionStatus() + "'" +
                ", openedAt=" + openedAt +
                '}';
    }
}