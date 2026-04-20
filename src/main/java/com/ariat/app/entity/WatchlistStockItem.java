package com.ariat.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistStockItem {
    private String stockName;
    private double currentPrice;
    private YearlyBound yearlyBound;
    private String sentimentIdx;
    private double lastClosedPrice;
}
