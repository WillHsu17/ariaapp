package com.ariat.app.service;

import com.ariat.app.client.EulerpoolClient;
import com.ariat.app.client.entity.EulerStockSearchResponse;
import com.ariat.app.client.entity.StockResult;
import com.ariat.app.entity.EarningCall;
import com.ariat.app.entity.StockDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TradingService {

    @Autowired
    private EulerpoolClient eulerpoolClient;

    // Mock user watchlists
    private final Map<String, List<String>> userWatchlists = new HashMap<>();

    public TradingService() {
    }

    public List<String> getUserWatchlist(String username) {
        return userWatchlists.getOrDefault(username, Collections.emptyList());
    }

    public boolean isStockInWatchlist(String username, String stockName) {
        return getUserWatchlist(username).contains(stockName);
    }

    public StockResult getStockBasis(String stockName) {
        try {
            EulerStockSearchResponse response = eulerpoolClient.getStockBasis(stockName);
            Optional<StockResult> match = response.getResults()
                    .stream()
                    .filter(r -> r.getTicker().equalsIgnoreCase(stockName))
                    .findFirst();

            return match.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public List<EarningCall> getEarningCalls(String stockSymbol) {
        return eulerpoolClient.getEarningCalls(stockSymbol);
    }
}
