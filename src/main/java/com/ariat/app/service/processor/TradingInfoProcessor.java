package com.ariat.app.service.processor;

import com.ariat.app.client.EulerpoolClient;
import com.ariat.app.client.entity.EulerStockSearchResponse;
import com.ariat.app.client.entity.InsiderSentimentResponse;
import com.ariat.app.client.entity.StockResult;
import com.ariat.app.entity.EarningCall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TradingInfoProcessor {

    @Autowired
    private EulerpoolClient eulerpoolClient;

    public StockResult getStockBasis(String stockName) {
        try {
            EulerStockSearchResponse response = eulerpoolClient.getStockBasis(stockName);
            Optional<StockResult> match = response.getResults()
                    .stream()
                    .filter(r -> r.getTicker().equalsIgnoreCase(stockName))
                    .findFirst();
            return match.orElse(null);
        } catch (Exception e) {
            log.error("Failed to fetch stock basis for {}", stockName, e);
            return null;
        }
    }

    public List<EarningCall> getEarningCalls(String stockSymbol) {
        return eulerpoolClient.getEarningCalls(stockSymbol);
    }

    public InsiderSentimentResponse getInsiderSentiment(String stockName) {
        try {
            StockResult stockResult = getStockBasis(stockName);
            if (stockResult != null && stockResult.getIsin() != null) {
                return eulerpoolClient.getInsiderSentiment(stockResult.getIsin());
            }
        } catch (Exception e) {
            log.error("Failed to fetch insider sentiment for {}", stockName, e);
        }
        return null;
    }
}
