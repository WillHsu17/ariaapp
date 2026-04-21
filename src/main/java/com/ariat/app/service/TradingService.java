package com.ariat.app.service;

import com.ariat.app.client.entity.InsiderSentimentResponse;
import com.ariat.app.client.entity.StockResult;
import com.ariat.app.entity.EarningCall;
import com.ariat.app.entity.WatchlistResponse;
import com.ariat.app.service.processor.TradingInfoProcessor;
import com.ariat.app.service.processor.WatchlistProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TradingService {

    @Autowired
    private WatchlistProcessor watchlistProcessor;

    @Autowired
    private TradingInfoProcessor tradingInfoProcessor;

    // --- Watchlist ---

    public WatchlistResponse getUserWatchlist(String username) {
        return watchlistProcessor.getUserWatchlist(username);
    }

    public WatchlistResponse addToWatchlist(String username, String stockName) {
        return watchlistProcessor.addToWatchlist(username, stockName);
    }

    public WatchlistResponse removeFromWatchlist(String username, String stockName) {
        return watchlistProcessor.removeFromWatchlist(username, stockName);
    }

    // --- Trading Info ---

    public StockResult getStockBasis(String stockName) {
        return tradingInfoProcessor.getStockBasis(stockName);
    }

    public List<EarningCall> getEarningCalls(String stockSymbol) {
        return tradingInfoProcessor.getEarningCalls(stockSymbol);
    }

    public InsiderSentimentResponse getInsiderSentiment(String stockName) {
        return tradingInfoProcessor.getInsiderSentiment(stockName);
    }
}
