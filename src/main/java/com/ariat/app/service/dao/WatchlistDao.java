package com.ariat.app.service.dao;

import com.ariat.app.entity.Stock;
import com.ariat.app.entity.WatchlistEntry;
import com.ariat.app.entity.WatchlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WatchlistDao extends JpaRepository<WatchlistEntry, WatchlistId> {

    @Query("SELECT w.stock FROM WatchlistEntry w WHERE w.user.id = :userId")
    List<Stock> findStocksByUserId(@Param("userId") Long userId);
}
