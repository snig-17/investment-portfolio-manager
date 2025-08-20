package com.snig.investmentportfoliomanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "username")
       },
       indexes = {
           @Index(name = "idx_user_email", columnList = "email"),
           @Index(name = "idx_user_username", columnList = "username")
       })
public class User {

    /**
     * Primary key - Auto-generated user ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * Unique username for login
     * Requirements: 3-50 characters, alphanumeric and underscore only
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    /**
     * User's email address - used for communications and notifications
     * Must be unique across the system
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /**
     * Encrypted password for authentication
     * Note: Should be hashed before storing (e.g., using BCrypt)
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String passwordHash;

    /**
     * User's first name
     */
    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    /**
     * User's last name
     */
    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    /**
     * User's risk tolerance level
     * Scale: 1-10 (1 = Very Conservative, 10 = Very Aggressive)
     * Used for portfolio recommendations and risk analysis
     */
    @Column(name = "risk_tolerance", nullable = false)
    @NotNull(message = "Risk tolerance is required")
    @Min(value = 1, message = "Risk tolerance must be at least 1")
    @Max(value = 10, message = "Risk tolerance must not exceed 10")
    private Integer riskTolerance;

    /**
     * Account status flag
     * Active users can perform transactions, inactive users are blocked
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Account creation timestamp - automatically set
     * Used for audit trails and compliance reporting
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp - automatically updated
     * Tracks when user profile was last modified
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * One-to-Many relationship with Portfolio entity
     * A user can have multiple investment portfolios
     * 
     * CascadeType.ALL: When user is deleted, all portfolios are deleted
     * FetchType.LAZY: Portfolios loaded only when accessed (performance optimization)
     * orphanRemoval: If portfolio is removed from user, delete it from database
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Portfolio> portfolios = new ArrayList<>();

    // ========================
    // CONSTRUCTORS
    // ========================

    /**
     * Default constructor required by JPA
     */
    public User() {
    }

    /**
     * Constructor for creating new users
     * 
     * @param username User's unique username
     * @param email User's email address
     * @param passwordHash Encrypted password
     * @param firstName User's first name
     * @param lastName User's last name
     * @param riskTolerance Risk tolerance level (1-10)
     */
    public User(String username, String email, String passwordHash, 
                String firstName, String lastName, Integer riskTolerance) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.riskTolerance = riskTolerance;
        this.isActive = true;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(Integer riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }

    // ========================
    // HELPER METHODS
    // ========================

    /**
     * Get user's full name
     * 
     * @return Concatenated first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get risk tolerance description
     * 
     * @return Human-readable risk tolerance level
     */
    public String getRiskToleranceDescription() {
        if (riskTolerance == null) return "Not Set";

        if (riskTolerance <= 2) return "Conservative";
        else if (riskTolerance <= 4) return "Moderately Conservative";
        else if (riskTolerance <= 6) return "Moderate";
        else if (riskTolerance <= 8) return "Moderately Aggressive";
        else return "Aggressive";
    }

    /**
     * Add a portfolio to this user
     * 
     * @param portfolio The portfolio to add
     */
    public void addPortfolio(Portfolio portfolio) {
        portfolios.add(portfolio);
        portfolio.setUser(this);
    }

    /**
     * Remove a portfolio from this user
     * 
     * @param portfolio The portfolio to remove
     */
    public void removePortfolio(Portfolio portfolio) {
        portfolios.remove(portfolio);
        portfolio.setUser(null);
    }

    // ========================
    // OBJECT METHODS
    // ========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + "'" +
                ", email='" + email + "'" +
                ", firstName='" + firstName + "'" +
                ", lastName='" + lastName + "'" +
                ", riskTolerance=" + riskTolerance +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}