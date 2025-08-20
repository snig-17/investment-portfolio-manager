package com.snig.investmentportfoliomanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets")

public class Asset {
    // database variables
    // Database variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Asset symbol is required")
    @Size(min = 1, max = 10, message = "Symbol must be between 1 and 10 characters")
    @Column(unique = true, nullable = false, length = 10)
    private String symbol; // AAPL, GOOGL, TSLA, etc.

    @NotBlank(message = "Asset name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Column(nullable = false, length = 200)
    private String name; // Apple Inc., Google LLC, Tesla Inc.

    @NotNull(message = "Asset type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType; // STOCK, BOND, ETF, CRYPTO

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "current_price", precision = 15, scale = 4, nullable = false)
    private BigDecimal currentPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Previous close cannot be negative")
    @Column(name = "previous_close", precision = 15, scale = 4)
    private BigDecimal previousClose;

    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap; // Total company value

    @Column(length = 10)
    private String sector; // Technology, Healthcare, Finance, etc.

    @Column(length = 50)
    private String exchange; // NASDAQ, NYSE, etc.

    @Column(name = "is_active")
    private Boolean isActive = true; // Can this asset be traded?

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated; // When was price last updated

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // database relationships

    // One asset can be in many positions
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Position> positions = new ArrayList<>();

    // One asset can have many transactions
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    //constructors for object creation
    public Asset() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.isActive = true;
    }

    public Asset(String symbol, String name, AssetType assetType, BigDecimal currentPrice) {
        this();
        this.symbol = symbol.toUpperCase(); // Always store symbols in uppercase
        this.name = name;
        this.assetType = assetType;
        this.currentPrice = currentPrice;
        this.previousClose = currentPrice; // Initially same as current price
    }

    //lifecycle callback - always keeps 'updated_at' current
    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    // business logic methods

    //update current price and set previous close

    public void updatePrice(BigDecimal newPrice) {
        if(newPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.previousClose = this.currentPrice;
            this.currentPrice = newPrice;
            this.lastUpdated = LocalDateTime.now();
        }
    }

    //calculate price change from previous close

    public BigDecimal getPriceChange(){
        if (previousClose != null && previousClose.compareTo(BigDecimal.ZERO) > 0) {
            return currentPrice.subtract(previousClose);
        }
        return BigDecimal.ZERO;
    }

    //calculate percentage change from previous close

    public BigDecimal getPriceChangePercent(){
        if (previousClose != null && previousClose.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = getPriceChange();
            return change.divide(previousClose, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    //check whether price is up
    public boolean isPriceUp(){
        return getPriceChange().compareTo(BigDecimal.ZERO) > 0;
    }
    //check whether price is down
    public boolean isPriceDown(){
        return getPriceChange().compareTo(BigDecimal.ZERO) < 0;
    }

    //format price string

    public String getFormattedPrice(){
        return "$" + currentPrice.toString();
    }

    //format price change string with +/-
    public String getFormattedPriceChange(){
        BigDecimal change = getPriceChange();
        String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "-";
        return sign + change;
    }

    //helper methods for relationships
    public void addPosition(Position position) {
        positions.add(position);
        position.setAsset(this);
    }

    public void removePosition(Position position) {
        positions.remove(position);
        position.setAsset(null);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setAsset(this);
    }
    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol != null ? symbol.toUpperCase() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", assetType=" + assetType +
                ", currentPrice=" + currentPrice +
                ", priceChange=" + getFormattedPriceChange() +
                '}';
    }
}
