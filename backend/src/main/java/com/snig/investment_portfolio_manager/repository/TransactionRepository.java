package com.snig.investment_portfolio_manager.repository;

import com.snig.investment_portfolio_manager.entity.Transaction;
import com.snig.investment_portfolio_manager.entity.Portfolio;
import com.snig.investment_portfolio_manager.entity.Asset;
import com.snig.investment_portfolio_manager.entity.TransactionType;
import com.snig.investment_portfolio_manager.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions for a specific portfolio
    List<Transaction> findByPortfolio(Portfolio portfolio);

    // Find transactions by portfolio ID
    List<Transaction> findByPortfolioId(Long portfolioId);

    // Find transactions for a specific asset
    List<Transaction> findByAsset(Asset asset);

    // Find transactions by type (BUY, SELL, DIVIDEND)
    List<Transaction> findByTransactionType(TransactionType transactionType);

    // Find transactions by status
    List<Transaction> findByStatus(TransactionStatus status);

    // Find transactions within date range
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findTransactionsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find transactions for portfolio within date range
    @Query("SELECT t FROM Transaction t WHERE t.portfolio.id = :portfolioId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    List<Transaction> findPortfolioTransactionsByDateRange(
            @Param("portfolioId") Long portfolioId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find buy transactions for an asset in a portfolio
    @Query("SELECT t FROM Transaction t WHERE t.portfolio = :portfolio " +
            "AND t.asset = :asset AND t.transactionType = 'BUY'")
    List<Transaction> findBuyTransactions(@Param("portfolio") Portfolio portfolio, @Param("asset") Asset asset);

    // Find sell transactions for an asset in a portfolio
    @Query("SELECT t FROM Transaction t WHERE t.portfolio = :portfolio " +
            "AND t.asset = :asset AND t.transactionType = 'SELL'")
    List<Transaction> findSellTransactions(@Param("portfolio") Portfolio portfolio, @Param("asset") Asset asset);

    // Calculate total buy amount for an asset in portfolio
    @Query("SELECT SUM(t.totalAmount) FROM Transaction t WHERE t.portfolio = :portfolio " +
            "AND t.asset = :asset AND t.transactionType = 'BUY' AND t.status = 'COMPLETED'")
    Double calculateTotalBuyAmount(@Param("portfolio") Portfolio portfolio, @Param("asset") Asset asset);

    // Calculate total sell amount for an asset in portfolio
    @Query("SELECT SUM(t.totalAmount) FROM Transaction t WHERE t.portfolio = :portfolio " +
            "AND t.asset = :asset AND t.transactionType = 'SELL' AND t.status = 'COMPLETED'")
    Double calculateTotalSellAmount(@Param("portfolio") Portfolio portfolio, @Param("asset") Asset asset);

    // Find large transactions above specified amount
    @Query("SELECT t FROM Transaction t WHERE t.totalAmount > :minAmount ORDER BY t.totalAmount DESC")
    List<Transaction> findLargeTransactions(@Param("minAmount") Double minAmount);

    // Find recent transactions (last N days)
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate >= :cutoffDate ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Find pending transactions
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' ORDER BY t.transactionDate ASC")
    List<Transaction> findPendingTransactions();

    // Find failed transactions
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' ORDER BY t.transactionDate DESC")
    List<Transaction> findFailedTransactions();

    // Calculate total trading volume for a portfolio
    @Query("SELECT SUM(t.totalAmount) FROM Transaction t WHERE t.portfolio.id = :portfolioId " +
            "AND t.status = 'COMPLETED'")
    Double calculateTotalTradingVolume(@Param("portfolioId") Long portfolioId);

    // Calculate total fees paid by portfolio
    @Query("SELECT SUM(t.fees) FROM Transaction t WHERE t.portfolio.id = :portfolioId " +
            "AND t.status = 'COMPLETED'")
    Double calculateTotalFees(@Param("portfolioId") Long portfolioId);

    // Find dividend transactions
    @Query("SELECT t FROM Transaction t WHERE t.transactionType = 'DIVIDEND' " +
            "AND t.status = 'COMPLETED' ORDER BY t.transactionDate DESC")
    List<Transaction> findDividendTransactions();

    // Calculate total dividends received for portfolio
    @Query("SELECT SUM(t.totalAmount) FROM Transaction t WHERE t.portfolio.id = :portfolioId " +
            "AND t.transactionType = 'DIVIDEND' AND t.status = 'COMPLETED'")
    Double calculateTotalDividends(@Param("portfolioId") Long portfolioId);

    // Find transactions by asset symbol
    @Query("SELECT t FROM Transaction t WHERE t.asset.symbol = :symbol ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAssetSymbol(@Param("symbol") String symbol);

    // Monthly transaction summary
    @Query("SELECT EXTRACT(YEAR FROM t.transactionDate) as year, " +
            "EXTRACT(MONTH FROM t.transactionDate) as month, " +
            "COUNT(t) as transactionCount, " +
            "SUM(t.totalAmount) as totalAmount " +
            "FROM Transaction t WHERE t.portfolio.id = :portfolioId " +
            "AND t.status = 'COMPLETED' " +
            "GROUP BY EXTRACT(YEAR FROM t.transactionDate), EXTRACT(MONTH FROM t.transactionDate) " +
            "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyTransactionSummary(@Param("portfolioId") Long portfolioId);
}
