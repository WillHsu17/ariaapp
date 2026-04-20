package com.ariat.app.service.dao;

import com.ariat.app.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockDao extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);
}
