package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._0;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2_TokenHolder;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
public class Simple2TokenHolder implements _2_TokenHolder {
    private final Function<String, TokenResponse> tokenRetriever;
    private final Clock clock;

    private final Map<String, TokenResponse> tokens = new HashMap<>();

    @Override
    public String getToken(String key) {
        var currentToken = tokens.get(key);
        if (currentToken == null || clock.instant().isAfter(currentToken.getValidThru())) {
            currentToken = tokenRetriever.apply(key);
            tokens.put(key, currentToken);
        }

        return currentToken.getToken();
    }

    @Override
    public void invalidateToken(String key, String invalidToken) {
        final var currentToken = tokens.get(key);
        if (currentToken != null && Objects.equals(currentToken.getToken(), invalidToken)) {
            tokens.remove(key);
        }
    }
}
