package com.ariat.app.client;

import com.ariat.app.entity.EarningCall;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
}