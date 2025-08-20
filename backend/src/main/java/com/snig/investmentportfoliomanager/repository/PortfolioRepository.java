package com.snig.investment_portfolio_manager.repository;

import com.snig.investment_portfolio_manager.entity.Portfolio;
import com.snig.investment_portfolio_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // Find all portfolios for a specific user
    List<Portfolio> findByUser(User user);

    // Find portfolios by user ID
    List<Portfolio> findByUserId(Long userId);

    // Find portfolio by name and user
    Optional<Portfolio> findByNameAndUser(String name, User user);

    // Find top performing portfolios
    @Query("SELECT p FROM Portfolio p WHERE p.totalReturn > :minReturn ORDER BY p.totalReturn DESC")
    List<Portfolio> findTopPerformingPortfolios(@Param("minReturn") Double minReturn);

    // Calculate total portfolio value for a user
    @Query("SELECT SUM(p.totalValue) FROM Portfolio p WHERE p.user.id = :userId")
    Double calculateTotalUserPortfolioValue(@Param("userId") Long userId);

    // Find portfolios with high risk (beta > threshold)
    @Query("SELECT p FROM Portfolio p WHERE p.beta > :betaThreshold")
    List<Portfolio> findHighRiskPortfolios(@Param("betaThreshold") Double betaThreshold);

    // Get portfolio performance metrics
    @Query("SELECT p FROM Portfolio p WHERE p.sharpeRatio > :minSharpe ORDER BY p.sharpeRatio DESC")
    List<Portfolio> findPortfoliosBySharpeRatio(@Param("minSharpe") Double minSharpe);
}
