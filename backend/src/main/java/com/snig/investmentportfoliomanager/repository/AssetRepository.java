package com.snig.investment_portfolio_manager.repository;

import com.snig.investment_portfolio_manager.entity.Asset;
import com.snig.investment_portfolio_manager.entity.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    // Find asset by symbol (ticker)
    Optional<Asset> findBySymbol(String symbol);

    // Find assets by type
    List<Asset> findByAssetType(AssetType assetType);

    // Find assets by sector
    List<Asset> findBySector(String sector);

    // Search assets by name or symbol
    @Query("SELECT a FROM Asset a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(a.symbol) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Asset> searchAssets(@Param("searchTerm") String searchTerm);

    // Find top gainers
    @Query("SELECT a FROM Asset a WHERE a.dailyChange > 0 ORDER BY a.dailyChangePercent DESC")
    List<Asset> findTopGainers();

    // Find top losers
    @Query("SELECT a FROM Asset a WHERE a.dailyChange < 0 ORDER BY a.dailyChangePercent ASC")
    List<Asset> findTopLosers();

    // Find assets with high volume
    @Query("SELECT a FROM Asset a WHERE a.volume > :minVolume ORDER BY a.volume DESC")
    List<Asset> findHighVolumeAssets(@Param("minVolume") Long minVolume);

    // Find assets in price range
    @Query("SELECT a FROM Asset a WHERE a.currentPrice BETWEEN :minPrice AND :maxPrice")
    List<Asset> findAssetsByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
}
