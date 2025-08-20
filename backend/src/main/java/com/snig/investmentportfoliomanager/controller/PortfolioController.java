package com.snig.investmentportfoliomanager.controller;

import com.snig.investmentportfoliomanager.entity.Portfolio;
import com.snig.investmentportfoliomanager.service.PortfolioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {
    
    @Autowired
    private PortfolioService portfolioService;

   
    @PostMapping("/create")
    public ResponseEntity<Portfolio> createPortfolio(
            @RequestParam Long userId,           // ← Clean parameter
            @RequestParam String name,           // ← Clean parameter  
            @RequestParam BigDecimal initialCash) {
        
        // Call your service method
        Portfolio portfolio = portfolioService.createPortfolio(userId, name, initialCash);
        return ResponseEntity.ok(portfolio);
    }

   
    @GetMapping("/{id}/value")
    public ResponseEntity<BigDecimal> getPortfolioValue(@PathVariable Long id) {
        BigDecimal value = portfolioService.calculatePortfolioValue(id);
        return ResponseEntity.ok(value);
    }

    
    @GetMapping("/{id}/performance")
    public ResponseEntity<Map<String, BigDecimal>> getPerformance(@PathVariable Long id) {
        Map<String, BigDecimal> performance = portfolioService.getPortfolioPerformance(id);
        return ResponseEntity.ok(performance);
    }

    
    @PutMapping("/{id}/cash")
    public ResponseEntity<Portfolio> updateCash(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        
        Portfolio portfolio = portfolioService.updateCashBalance(id, amount, reason);
        return ResponseEntity.ok(portfolio);
    }
}
