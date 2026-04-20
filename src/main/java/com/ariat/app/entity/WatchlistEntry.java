package com.ariat.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "watchlist")
public class WatchlistEntry {

    @EmbeddedId
    private WatchlistId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("stockId")
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public WatchlistEntry(User user, Stock stock) {
        this.id = new WatchlistId(user.getId(), stock.getId());
        this.user = user;
        this.stock = stock;
        this.createdAt = LocalDateTime.now();
    }
}
