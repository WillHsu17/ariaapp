package com.ariat.app.resource;

import com.ariat.app.client.entity.StockResult;
import com.ariat.app.util.JwtUtil;
import com.example.api.MyTradingApi;
import com.ariat.app.entity.StockDetails;
import com.ariat.app.entity.WatchlistRequest;
import com.ariat.app.entity.WatchlistResponse;
import com.ariat.app.service.TradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/my_trading")
public class TradingResource {

    @Autowired
    private TradingService tradingService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<String>> getWatchlist(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        String username = jwtUtil.validateTokenAndGetUsername(token);
        List<String> watchlist = tradingService.getUserWatchlist(username);
        return ResponseEntity.ok(watchlist);
    }

    @PostMapping("/watchlist_details")
    public ResponseEntity<?> getWatchlistDetails(@RequestHeader("Authorization") String authHeader,
                                                 @RequestBody WatchlistRequest watchlistRequest) {
        String token = extractToken(authHeader);
        String username = jwtUtil.validateTokenAndGetUsername(token);
//        if (!tradingService.isStockInWatchlist(username, watchlistRequest.getStockName())) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body("Stock not found in your watchlist");
//        }
        StockResult response = tradingService.getStockBasis(watchlistRequest.getStockName());
//        StockDetails details = tradingService.getStockDetails(watchlistRequest.getStockName());
//        WatchlistResponse response = new WatchlistResponse(watchlistRequest.getStockName(), details);
        return ResponseEntity.ok(response);
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}