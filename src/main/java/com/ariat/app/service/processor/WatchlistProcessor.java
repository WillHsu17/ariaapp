package com.ariat.app.service.processor;

import com.ariat.app.entity.*;
import com.ariat.app.service.dao.StockDao;
import com.ariat.app.service.dao.UserDao;
import com.ariat.app.service.dao.WatchlistDao;
import com.ariat.app.client.entity.StockResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class WatchlistProcessor {

    private static final String CACHE_PREFIX = "watchlist:";

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

    @Autowired
    private TradingInfoProcessor tradingInfoProcessor;

    @Value("${watchlist.cache.ttl-minutes:30}")
    private long cacheTtlMinutes;

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
            StockResult result = tradingInfoProcessor.getStockBasis(symbol);
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

    private WatchlistResponse fetchWatchlistFromDb(String username) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        List<Stock> stocks = watchlistDao.findStocksByUserId(user.getId());
        List<WatchlistStockItem> items = stocks.stream()
                .map(s -> new WatchlistStockItem(s.getSymbol(), 0.0, new YearlyBound(0.0, 0.0), null, 0.0))
                .toList();
        return new WatchlistResponse(items, items.size());
    }

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
