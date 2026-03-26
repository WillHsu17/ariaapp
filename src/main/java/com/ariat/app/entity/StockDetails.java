package com.ariat.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDetails {
    private double currentPrice;
    private double dailyHigh;
    private double dailyLow;
    private long volume;
}