package com.ariat.app.resource;

import com.ariat.app.client.entity.StockResult;
import com.ariat.app.util.JwtUtil;
import com.ariat.app.service.TradingService;
import com.example.api.MyTradingApi;
import com.example.model.WatchlistRequest;
import com.example.model.WatchlistResponse;
import com.example.model.WatchlistStockItem;
import com.example.model.YearlyBound;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/my_trading")
public class TradingResource implements MyTradingApi {

    @Autowired
    private TradingService tradingService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @GetMapping("/watchlist")
    public ResponseEntity<WatchlistResponse> getWatchlist(
            @RequestHeader("Authorization") String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(toModel(tradingService.getUserWatchlist(username)));
    }

    @Override
    @PostMapping("/watchlist_details")
    public ResponseEntity<WatchlistResponse> getWatchlistDetails(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody WatchlistRequest watchlistRequest) {
        String username = extractUsername(authHeader);
        StockResult result = tradingService.getStockBasis(watchlistRequest.getStockName());
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(toModel(tradingService.getUserWatchlist(username)));
    }

    @Override
    @PostMapping("/addToWatchList")
    public ResponseEntity<WatchlistResponse> addToWatchList(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody WatchlistRequest watchlistRequest) {
        try {
            String username = extractUsername(authHeader);
            com.ariat.app.entity.WatchlistResponse result =
                    tradingService.addToWatchlist(username, watchlistRequest.getStockName());
            return ResponseEntity.ok(toModel(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Override
    @PostMapping("/removeFromWatchList")
    public ResponseEntity<WatchlistResponse> removeFromWatchList(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody WatchlistRequest watchlistRequest) {
        try {
            String username = extractUsername(authHeader);
            com.ariat.app.entity.WatchlistResponse result =
                    tradingService.removeFromWatchlist(username, watchlistRequest.getStockName());
            return ResponseEntity.ok(toModel(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private String extractUsername(String authHeader) {
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return jwtUtil.validateTokenAndGetUsername(token);
    }

    private WatchlistResponse toModel(com.ariat.app.entity.WatchlistResponse entity) {
        List<WatchlistStockItem> items = entity.getStocks().stream()
                .map(s -> {
                    YearlyBound bound = new YearlyBound()
                            .upperBound(s.getYearlyBound().getUpperBound())
                            .lowerBound(s.getYearlyBound().getLowerBound());
                    return new WatchlistStockItem()
                            .stockName(s.getStockName())
                            .currentPrice(s.getCurrentPrice())
                            .yearlyBound(bound)
                            .sentimentIdx(s.getSentimentIdx())
                            .lastClosedPrice(s.getLastClosedPrice());
                })
                .toList();
        return new WatchlistResponse().stocks(items).total(entity.getTotal());
    }
}
