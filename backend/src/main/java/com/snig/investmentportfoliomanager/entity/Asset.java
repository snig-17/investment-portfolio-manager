package com.snig.investmentportfoliomanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.snig.investmentportfoliomanager.entity.enums.AssetType;


@Entity
@Table(name = "assets",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "ticker_symbol"),
           @UniqueConstraint(columnNames = "isin")
       },
       indexes = {
           @Index(name = "idx_asset_ticker", columnList = "ticker_symbol"),
           @Index(name = "idx_asset_name", columnList = "name"),
           @Index(name = "idx_asset_type", columnList = "asset_type"),
           @Index(name = "idx_asset_sector", columnList = "sector")
       })
public class Asset {

    /**
     * Primary key - Auto-generated asset ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long id;

    /**
     * Ticker symbol (e.g., AAPL, MSFT, BTC-USD)
     * Primary identifier for trading and market data
     */
    @Column(name = "ticker_symbol", nullable = false, unique = true, length = 20)
    @NotBlank(message = "Ticker symbol is required")
    @Size(max = 20, message = "Ticker symbol must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9.-]+$", message = "Ticker symbol must contain only uppercase letters, numbers, dots, and hyphens")
    private String tickerSymbol;

    /**
     * Full asset name (e.g., "Apple Inc.", "Microsoft Corporation")
     */
    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "Asset name is required")
    @Size(max = 200, message = "Asset name must not exceed 200 characters")
    private String name;

    /**
     * Asset classification (STOCK, BOND, ETF, CRYPTO, etc.)
     * Uses enum for type safety and consistency
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    /**
     * Market sector (Technology, Healthcare, Financial Services, etc.)
     * Used for portfolio diversification analysis
     */
    @Column(name = "sector", length = 50)
    @Size(max = 50, message = "Sector must not exceed 50 characters")
    private String sector;

    /**
     * Industry classification within sector
     * Provides more granular categorization
     */
    @Column(name = "industry", length = 100)
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    /**
     * Current market price per share/unit
     * Updated through market data feeds
     * Precision: 4 decimal places for accurate pricing
     */
    @Column(name = "current_price", nullable = false, precision = 19, scale = 4)
    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.0001", message = "Current price must be positive")
    @Digits(integer = 15, fraction = 4, message = "Invalid price format")
    private BigDecimal currentPrice;

    /**
     * Previous day's closing price
     * Used for daily change calculations
     */
    @Column(name = "previous_close", precision = 19, scale = 4)
    @DecimalMin(value = "0.0001", message = "Previous close must be positive")
    @Digits(integer = 15, fraction = 4, message = "Invalid previous close format")
    private BigDecimal previousClose;

    /**
     * 52-week high price
     * Important for technical analysis and risk assessment
     */
    @Column(name = "week_52_high", precision = 19, scale = 4)
    @DecimalMin(value = "0.0001", message = "52-week high must be positive")
    @Digits(integer = 15, fraction = 4, message = "Invalid 52-week high format")
    private BigDecimal week52High;

    /**
     * 52-week low price
     * Used for volatility and risk analysis
     */
    @Column(name = "week_52_low", precision = 19, scale = 4)
    @DecimalMin(value = "0.0001", message = "52-week low must be positive")
    @Digits(integer = 15, fraction = 4, message = "Invalid 52-week low format")
    private BigDecimal week52Low;

    /**
     * Annual dividend yield percentage
     * Important for income-focused portfolios
     */
    @Column(name = "dividend_yield", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Dividend yield must be non-negative")
    @DecimalMax(value = "1.0000", message = "Dividend yield cannot exceed 100%")
    @Digits(integer = 1, fraction = 4, message = "Invalid dividend yield format")
    private BigDecimal dividendYield;

    /**
     * Market capitalization in USD
     * Used for portfolio size allocation and risk analysis
     */
    @Column(name = "market_cap", precision = 19, scale = 2)
    @DecimalMin(value = "0.00", message = "Market cap must be non-negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid market cap format")
    private BigDecimal marketCap;

    /**
     * Average daily trading volume (shares)
     * Indicates liquidity and ease of trading
     */
    @Column(name = "avg_volume")
    @Min(value = 0, message = "Average volume must be non-negative")
    private Long avgVolume;

    /**
     * ISIN (International Securities Identification Number)
     * Global standard for security identification
     */
    @Column(name = "isin", unique = true, length = 12)
    @Size(min = 12, max = 12, message = "ISIN must be exactly 12 characters")
    @Pattern(regexp = "^[A-Z]{2}[A-Z0-9]{9}[0-9]$", message = "Invalid ISIN format")
    private String isin;

    /**
     * CUSIP (Committee on Uniform Securities Identification Procedures)
     * North American security identification standard
     */
    @Column(name = "cusip", length = 9)
    @Size(min = 9, max = 9, message = "CUSIP must be exactly 9 characters")
    @Pattern(regexp = "^[A-Z0-9]{9}$", message = "Invalid CUSIP format")
    private String cusip;

    /**
     * Primary exchange where asset is traded
     * (NYSE, NASDAQ, LSE, etc.)
     */
    @Column(name = "exchange", length = 10)
    @Size(max = 10, message = "Exchange must not exceed 10 characters")
    private String exchange;

    /**
     * Base currency for pricing (USD, EUR, GBP, etc.)
     */
    @Column(name = "currency", nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    private String currency;

    /**
     * Asset status flag
     * Inactive assets cannot be traded (delisted, suspended, etc.)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Last price update timestamp
     * Tracks when market data was last refreshed
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /**
     * Asset creation timestamp
     * When asset was added to the system
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     * When asset data was last modified
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * One-to-Many relationship with Position entity
     * An asset can be held in multiple portfolio positions
     * 
     * FetchType.LAZY: Positions loaded only when accessed
     * mappedBy: Position entity owns the relationship
     */
    @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    private List<Position> positions = new ArrayList<>();

    /**
     * One-to-Many relationship with Transaction entity
     * Asset transaction history across all portfolios
     */
    @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    // ========================
    // CONSTRUCTORS
    // ========================

    /**
     * Default constructor required by JPA
     */
    public Asset() {
    }

    /**
     * Constructor for creating new assets
     * 
     * @param tickerSymbol Asset ticker symbol
     * @param name Asset full name
     * @param assetType Asset classification
     * @param currentPrice Current market price
     * @param currency Base currency
     */
    public Asset(String tickerSymbol, String name, AssetType assetType, 
                 BigDecimal currentPrice, String currency) {
        this.tickerSymbol = tickerSymbol;
        this.name = name;
        this.assetType = assetType;
        this.currentPrice = currentPrice;
        this.currency = currency;
        this.isActive = true;
        this.lastUpdated = LocalDateTime.now();
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

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
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

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
        this.lastUpdated = LocalDateTime.now();
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(BigDecimal previousClose) {
        this.previousClose = previousClose;
    }

    public BigDecimal getWeek52High() {
        return week52High;
    }

    public void setWeek52High(BigDecimal week52High) {
        this.week52High = week52High;
    }

    public BigDecimal getWeek52Low() {
        return week52Low;
    }

    public void setWeek52Low(BigDecimal week52Low) {
        this.week52Low = week52Low;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public Long getAvgVolume() {
        return avgVolume;
    }

    public void setAvgVolume(Long avgVolume) {
        this.avgVolume = avgVolume;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getCusip() {
        return cusip;
    }

    public void setCusip(String cusip) {
        this.cusip = cusip;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    // ========================
    // BUSINESS LOGIC METHODS
    // ========================

    /**
     * Calculate daily price change amount
     * 
     * @return Price change from previous close
     */
    public BigDecimal getDailyChange() {
        if (previousClose == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.subtract(previousClose);
    }

    /**
     * Calculate daily price change percentage
     * 
     * @return Percentage change from previous close
     */
    public BigDecimal getDailyChangePercent() {
        if (previousClose == null || previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal change = getDailyChange();
        return change.divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
    }

    /**
     * Check if asset is near 52-week high
     * Within 5% of 52-week high
     * 
     * @return true if near 52-week high
     */
    public boolean isNear52WeekHigh() {
        if (week52High == null) {
            return false;
        }

        BigDecimal threshold = week52High.multiply(new BigDecimal("0.95"));
        return currentPrice.compareTo(threshold) >= 0;
    }

    /**
     * Check if asset is near 52-week low
     * Within 5% of 52-week low
     * 
     * @return true if near 52-week low
     */
    public boolean isNear52WeekLow() {
        if (week52Low == null) {
            return false;
        }

        BigDecimal threshold = week52Low.multiply(new BigDecimal("1.05"));
        return currentPrice.compareTo(threshold) <= 0;
    }

    /**
     * Get market cap category
     * 
     * @return Market cap classification
     */
    public String getMarketCapCategory() {
        if (marketCap == null) {
            return "Unknown";
        }

        BigDecimal billion = new BigDecimal("1000000000");
        BigDecimal trillion = new BigDecimal("1000000000000");

        if (marketCap.compareTo(trillion) >= 0) {
            return "Mega Cap";
        } else if (marketCap.compareTo(billion.multiply(new BigDecimal("200"))) >= 0) {
            return "Large Cap";
        } else if (marketCap.compareTo(billion.multiply(new BigDecimal("10"))) >= 0) {
            return "Mid Cap";
        } else if (marketCap.compareTo(billion.multiply(new BigDecimal("2"))) >= 0) {
            return "Small Cap";
        } else {
            return "Micro Cap";
        }
    }

    /**
     * Check if asset is liquid (high trading volume)
     * 
     * @return true if average volume indicates good liquidity
     */
    public boolean isLiquid() {
        if (avgVolume == null) {
            return false;
        }

        // Consider liquid if average volume > 100,000 shares/day
        return avgVolume > 100000L;
    }

    /**
     * Update price data
     * Helper method for batch price updates
     * 
     * @param newPrice New current price
     * @param newPreviousClose New previous close
     */
    public void updatePriceData(BigDecimal newPrice, BigDecimal newPreviousClose) {
        this.previousClose = this.currentPrice; // Current becomes previous
        this.currentPrice = newPrice;
        this.lastUpdated = LocalDateTime.now();

        // Update 52-week high/low if necessary
        if (week52High == null || newPrice.compareTo(week52High) > 0) {
            this.week52High = newPrice;
        }
        if (week52Low == null || newPrice.compareTo(week52Low) < 0) {
            this.week52Low = newPrice;
        }
    }

    // ========================
    // OBJECT METHODS
    // ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asset)) return false;
        Asset asset = (Asset) o;
        return id != null && id.equals(asset.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Asset{" +
                "id=" + id +
                ", tickerSymbol='" + tickerSymbol + "'" +
                ", name='" + name + "'" +
                ", assetType=" + assetType +
                ", currentPrice=" + currentPrice +
                ", currency='" + currency + "'" +
                ", dailyChange=" + getDailyChange() +
                ", dailyChangePercent=" + getDailyChangePercent() + "%" +
                ", marketCapCategory='" + getMarketCapCategory() + "'" +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
    
}
    