package com.snig.investment_portfolio_manager.service;

import com.snig.investment_portfolio_manager.entity.User;
import com.snig.investment_portfolio_manager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Create new user account
    public User createUser(User user) {
        // Validate email doesn't already exist
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        // Set default values
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(true);

        return userRepository.save(user);
    }

    // Find user by ID
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    // Find user by email (login)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Update user profile
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // Get all active users
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    // Simple authentication (in real apps, use Spring Security)
    public boolean authenticateUser(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && user.get().getPassword().equals(password);
    }
}
