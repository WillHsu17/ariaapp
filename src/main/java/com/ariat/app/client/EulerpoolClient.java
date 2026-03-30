package com.ariat.app.client;

import com.ariat.app.client.entity.EulerStockSearchResponse;
import com.ariat.app.client.entity.InsiderSentimentResponse;
import com.ariat.app.client.entity.StockResult;
import com.ariat.app.entity.EarningCall;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EulerpoolClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${eulerpool.api.host}")
    private String apiHost;

    @Value("${eulerpool.api.token}")
    private String apiToken;

    private final String SEARCH_API = "/api/1/equity/search";
    private final String INSIDER_SENTIMENT_API = "/api/1/sentiment/insider-sentiment/";

    /** Public API methods using centralized get() */
    public InsiderSentimentResponse getInsiderSentiment(String isin) {
        return get(INSIDER_SENTIMENT_API + isin, null, InsiderSentimentResponse.class);
    }

    public EulerStockSearchResponse getStockBasis(String stockSymbol) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("q", stockSymbol);
        return get(SEARCH_API, params, EulerStockSearchResponse.class);
    }
    /**
     * Fetch earning calls for a given stock symbol
     */
    public List<EarningCall> getEarningCalls(String stockSymbol) {
        String url = UriComponentsBuilder
                .fromUriString(apiHost)
                .path("/api/1/earning-calls/list/{symbol}")
                .queryParam("token", apiToken)
                .buildAndExpand(stockSymbol)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<EarningCall[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    EarningCall[].class
            );

            // Only accept 2xx responses
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            } else {
                log.warn("Eulerpool API returned non-success status: {}", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (HttpStatusCodeException ex) {
            log.error("Eulerpool API error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception ex) {
            log.error("Unexpected error calling Eulerpool API", ex);
            return Collections.emptyList();
        }
    }

    /**
     * Centralized GET request handler
     */
    private <T> T get(String path, MultiValueMap<String, String> queryParams, Class<T> responseType) {
        try {
            WebClient.RequestHeadersSpec<?> request = getClient().get().uri(uriBuilder -> {
                var builder = uriBuilder.path(path);
                if (queryParams != null) queryParams.forEach(builder::queryParam);
                builder.queryParam("token", apiToken);
                return builder.build();
            });

            return request
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response ->
                            response.bodyToMono(String.class).flatMap(body -> {
                                log.error("Client error {}: {}", response.statusCode(), body);
                                return Mono.error(new RuntimeException("Euler API 4xx error: " + response.statusCode()));
                            })
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.bodyToMono(String.class).flatMap(body -> {
                                log.error("Server error {}: {}", response.statusCode(), body);
                                return Mono.error(new RuntimeException("Euler API 5xx error: " + response.statusCode()));
                            })
                    )
                    .bodyToMono(responseType)
                    .block();

        } catch (Exception ex) {
            log.error("Unexpected error calling Euler API", ex);
            return null;
        }
    }

    private WebClient getClient() {
        return WebClient.builder()
                .baseUrl(apiHost)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}