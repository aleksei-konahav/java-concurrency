package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2_TokenHolder;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
public class Synchronized2TokenHolder implements _2_TokenHolder {
    private final Function<String, TokenResponse> tokenRetriever;
    private final Clock clock;

    private final Map<String, TokenResponse> tokens = Collections.synchronizedMap(new HashMap<>());

    @Override
    public String getToken(String key) {
        return tokens.compute(key, (k, currentToken) -> {
            if (currentToken == null || clock.instant().isAfter(currentToken.getValidThru())) {
                return tokenRetriever.apply(key);
            } else {
                return currentToken;
            }
        }).getToken();
    }

    @Override
    public void invalidateToken(String key, String invalidToken) {
        tokens.computeIfPresent(key, (k, currentToken) ->
                Objects.equals(currentToken.getToken(), invalidToken) ? null : currentToken);
    }
}
