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
@Table(name= "portfolios")
public class Portfolio {
    // database variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Portfolio name is required")
    @Size(min = 2, max = 100, message = "Portfolio name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Initial cash amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Cash amount cannot be negative")
    @Column(name = "cash_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal cashBalance;

    @DecimalMin(value = "0.0", inclusive = true, message = "Total value cannot be negative")
    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // database relationships

    // Many portfolios belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One portfolio has many positions (stock holdings)
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Position> positions = new ArrayList<>();

    // One portfolio has many transactions
    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    // constructors for object creation
    public Portfolio() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.cashBalance = BigDecimal.ZERO;
        this.totalValue = BigDecimal.ZERO;
    }

    public Portfolio(String name, BigDecimal initialCash, User user) {
        this();
        this.name = name;
        this.cashBalance = initialCash;
        this.user = user;
        this.totalValue = initialCash; // Initially, total value equals cash
    }

    //lifecycle callback - always keeps 'updated_at' current
    @PreUpdate
    public void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    // business logic methods

    //calculate total portfolio value
    public void updateTotalValue() {
        BigDecimal positionsValue = positions.stream()
                .map(Position::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalValue = this.cashBalance.add(positionsValue);
    }

    //deposit cash into portfolio

    public void addCash(BigDecimal amount) {
        if(amount.compareTo(BigDecimal.ZERO) > 0){
            this.cashBalance = this.cashBalance.add(amount);
            updateTotalValue();
        }
    }

    //withdraw cash from portfolio

    public boolean withdrawCash(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) > 0 && this.cashBalance.compareTo(amount) >= 0) {
            this.cashBalance = this.cashBalance.subtract(amount);
            updateTotalValue();
            return true;
        }
        return false;
    }

    // calculate portfolio performace percentage

    public BigDecimal getPerformancePercentage() {
        if(totalValue.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        return BigDecimal.ZERO; //replace with actual transaction history later
    }

    // helper methods for database relationships

    public void addPosition(Position position) {
        positions.add(position);
        position.setPortfolio(this);
        updateTotalValue();
    }
    public void removePosition(Position position) {
        positions.remove(position);
        position.setPortfolio(null);
        updateTotalValue();
    }
    public void addTransaction(Transaction transaction){
        transactions.add(transaction);
        transaction.setPortfolio(this);
    }

    //getters and setters
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

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
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

    @Override
    public String toString() {
        return "Portfolio{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cashBalance=" + cashBalance +
                ", totalValue=" + totalValue +
                ", positionsCount=" + (positions != null ? positions.size() : 0) +
                '}';
    }
}