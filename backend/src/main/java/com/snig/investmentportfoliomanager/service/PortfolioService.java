package com.snig.investmentportfoliomanager.service;

import org.springframework.stereotype.Service;

import com.snig.investmentportfoliomanager.entity.Portfolio;
import com.snig.investmentportfoliomanager.entity.User;
import com.snig.investmentportfoliomanager.repository.PortfolioRepository;
import com.snig.investmentportfoliomanager.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;


@Service
@Transactional
public class PortfolioService {
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private UserRepository userRepository;

    
    public Portfolio createPortfolio(Long userId, String portfolioName, BigDecimal initialCash) {
        
        // Validation
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
       
        Portfolio portfolio = new Portfolio(portfolioName, null, initialCash, user);
        
        return portfolioRepository.save(portfolio);
    }

   
    @Transactional(readOnly = true)
    public BigDecimal calculatePortfolioValue(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
       
        return portfolio.getTotalValue();
    }

   
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getPortfolioPerformance(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        Map<String, BigDecimal> performance = new HashMap<>();
        
        
        performance.put("currentValue", portfolio.getTotalValue());
        performance.put("initialCash", portfolio.getInitialCash());
        performance.put("totalReturn", portfolio.getTotalProfitLoss());
        performance.put("returnPercentage", portfolio.getReturnPercentage());
        performance.put("cashAllocation", portfolio.getCashAllocationPercentage());
        performance.put("positionCount", BigDecimal.valueOf(portfolio.getPositionCount()));
        
        return performance;
    }

    
    public Portfolio updateCashBalance(Long portfolioId, BigDecimal amount, String reason) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        
        
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            portfolio.addCash(amount);  // Your method!
        } else {
            portfolio.subtractCash(amount.abs());  // Your method with built-in validation!
        }
        
        return portfolioRepository.save(portfolio);
    }
}
