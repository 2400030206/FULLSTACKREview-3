package com.student.portfolio.service;

import com.student.portfolio.entity.Portfolio;
import com.student.portfolio.entity.User;
import com.student.portfolio.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    public Portfolio getPortfolioByUser(User user) {
        // If not found, return null
        return portfolioRepository.findByUser(user).orElse(null);
    }

    public Portfolio savePortfolio(Portfolio portfolio) {
        return portfolioRepository.save(portfolio);
    }
}
