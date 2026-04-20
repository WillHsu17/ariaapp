package com.ariat.app.service;

import com.ariat.app.client.EulerpoolClient;
import com.ariat.app.client.entity.EulerStockSearchResponse;
import com.ariat.app.client.entity.InsiderSentimentResponse;
import com.ariat.app.client.entity.StockResult;
import com.ariat.app.entity.*;
import com.ariat.app.service.dao.StockDao;
import com.ariat.app.service.dao.UserDao;
import com.ariat.app.service.dao.WatchlistDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TradingService {

    private static final String CACHE_PREFIX = "watchlist:";

    @Autowired
    private EulerpoolClient eulerpoolClient;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StockDao stockDao;

    @Autowired
    private WatchlistDao watchlistDao;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${watchlist.cache.ttl-minutes:30}")
    private long cacheTtlMinutes;

    // --- Public API ---

    public WatchlistResponse getUserWatchlist(String username) {
        String cacheKey = CACHE_PREFIX + username;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, WatchlistResponse.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize watchlist cache for {}, falling back to DB", username);
            }
        }
        WatchlistResponse result = fetchWatchlistFromDb(username);
        putCache(cacheKey, result);
        return result;
    }

    public WatchlistResponse addToWatchlist(String username, String stockName) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String symbol = stockName.toUpperCase();
        Stock stock = stockDao.findBySymbol(symbol).orElseGet(() -> {
            StockResult result = getStockBasis(symbol);
            return stockDao.save(new Stock(symbol, result != null ? result.getName() : null));
        });

        WatchlistId entryId = new WatchlistId(user.getId(), stock.getId());
        if (!watchlistDao.existsById(entryId)) {
            watchlistDao.save(new WatchlistEntry(user, stock));
        }

        return refreshCache(username);
    }

    public WatchlistResponse removeFromWatchlist(String username, String stockName) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        String symbol = stockName.toUpperCase();
        Stock stock = stockDao.findBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not in watchlist: " + symbol));

        WatchlistId entryId = new WatchlistId(user.getId(), stock.getId());
        if (!watchlistDao.existsById(entryId)) {
            throw new RuntimeException("Stock not in watchlist: " + symbol);
        }

        watchlistDao.deleteById(entryId);
        return refreshCache(username);
    }

    public InsiderSentimentResponse getInsiderSentiment(String stockName) {
        try {
            StockResult stockResult = getStockBasis(stockName);
            if (stockResult != null && stockResult.getIsin() != null) {
                return eulerpoolClient.getInsiderSentiment(stockResult.getIsin());
            }
        } catch (Exception e) {
            return null;
        }
        return null;
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

    // --- Private helpers ---

    private WatchlistResponse fetchWatchlistFromDb(String username) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        List<Stock> stocks = watchlistDao.findStocksByUserId(user.getId());
        List<WatchlistStockItem> items = stocks.stream()
                .map(s -> new WatchlistStockItem(s.getSymbol(), 0.0, new YearlyBound(0.0, 0.0), null, 0.0))
                .toList();
        return new WatchlistResponse(items, items.size());
    }

    // Invalidate stale cache → fetch fresh from DB → re-populate cache.
    // Called after any mutation (add/remove) so stale memory is freed immediately.
    private WatchlistResponse refreshCache(String username) {
        String cacheKey = CACHE_PREFIX + username;
        redisTemplate.delete(cacheKey);
        WatchlistResponse fresh = fetchWatchlistFromDb(username);
        putCache(cacheKey, fresh);
        return fresh;
    }

    private void putCache(String key, WatchlistResponse value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value),
                    Duration.ofMinutes(cacheTtlMinutes));
        } catch (Exception e) {
            log.warn("Failed to write watchlist cache for key {}", key);
        }
    }
}
