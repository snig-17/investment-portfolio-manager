package com.snig.investment_portfolio_manager.repository;

import com.snig.investment_portfolio_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {

    //find user by email
    Optional<User> findByEmail(String email);

    //check if email exists
    boolean existsByEmail(String email);

    //find users by status
    List<User> findByIsActiveTrue();

    //find users with portfolios over 'x'
    @Query("SELECT u FROM User u JOIN u.portfolios p WHERE p.totalValue > :minValue")
    List<User> findUsersWithPortfolioValueGreaterThan(@Param("minValue") Double minValue);

    //find users created within date range
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByDateRange(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );
}