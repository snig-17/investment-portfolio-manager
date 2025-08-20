package com.snig.investmentportfoliomanager.repository;

import com.snig.investmentportfoliomanager.entity.Position;
import com.snig.investmentportfoliomanager.entity.Portfolio;
import com.snig.investmentportfoliomanager.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    // Find all positions in a portfolio
    List<Position> findByPortfolio(Portfolio portfolio);

    // Find positions by portfolio ID
    List<Position> findByPortfolioId(Long portfolioId);

    // Find specific position by portfolio and asset
    Optional<Position> findByPortfolioAndAsset(Portfolio portfolio, Asset asset);

    // Find positions for a specific asset across all portfolios
    List<Position> findByAsset(Asset asset);

    // Find positions with quantity greater than specified amount
    @Query("SELECT p FROM Position p WHERE p.quantity > :minQuantity")
    List<Position> findPositionsWithQuantityGreaterThan(@Param("minQuantity") Double minQuantity);

    // Find profitable positions (current value > total cost)
    @Query("SELECT p FROM Position p WHERE (p.quantity * p.asset.currentPrice) > p.totalCost")
    List<Position> findProfitablePositions();

    // Find losing positions (current value < total cost)
    @Query("SELECT p FROM Position p WHERE (p.quantity * p.asset.currentPrice) < p.totalCost")
    List<Position> findLosingPositions();

    // Calculate total portfolio value by summing all positions
    @Query("SELECT SUM(p.quantity * p.asset.currentPrice) FROM Position p WHERE p.portfolio.id = :portfolioId")
    Double calculatePortfolioCurrentValue(@Param("portfolioId") Long portfolioId);

    // Calculate total cost basis for a portfolio
    @Query("SELECT SUM(p.totalCost) FROM Position p WHERE p.portfolio.id = :portfolioId")
    Double calculatePortfolioTotalCost(@Param("portfolioId") Long portfolioId);

    // Find positions by asset type (stocks, bonds, ETFs)
    @Query("SELECT p FROM Position p WHERE p.asset.assetType = :assetType")
    List<Position> findPositionsByAssetType(@Param("assetType") com.snig.investmentportfoliomanager.entity.enums.AssetType assetType);

    // Find positions in a specific sector
    @Query("SELECT p FROM Position p WHERE p.asset.sector = :sector")
    List<Position> findPositionsBySector(@Param("sector") String sector);

    // Find top positions by current value
    @Query("SELECT p FROM Position p ORDER BY (p.quantity * p.asset.currentPrice) DESC")
    List<Position> findTopPositionsByValue();

    // Find positions with unrealized gain/loss above threshold
    @Query("SELECT p FROM Position p WHERE ABS((p.quantity * p.asset.currentPrice) - p.totalCost) > :threshold")
    List<Position> findPositionsWithUnrealizedGainLossAbove(@Param("threshold") Double threshold);

    // Get position allocation percentage within portfolio
    @Query("SELECT p, (p.quantity * p.asset.currentPrice) / " +
            "(SELECT SUM(pos.quantity * pos.asset.currentPrice) FROM Position pos WHERE pos.portfolio = p.portfolio) * 100 " +
            "FROM Position p WHERE p.portfolio.id = :portfolioId")
    List<Object[]> findPositionAllocations(@Param("portfolioId") Long portfolioId);

    // Find concentrated positions (allocation > specified percentage)
    @Query("SELECT p FROM Position p WHERE " +
            "(p.quantity * p.asset.currentPrice) / " +
            "(SELECT SUM(pos.quantity * pos.asset.currentPrice) FROM Position pos WHERE pos.portfolio = p.portfolio) > :allocationThreshold")
    List<Position> findConcentratedPositions(@Param("allocationThreshold") Double allocationThreshold);
}
