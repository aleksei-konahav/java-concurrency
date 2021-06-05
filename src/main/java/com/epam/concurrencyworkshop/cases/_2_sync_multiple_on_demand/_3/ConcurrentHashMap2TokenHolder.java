package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._3;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2_TokenHolder;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

@RequiredArgsConstructor
public class ConcurrentHashMap2TokenHolder implements _2_TokenHolder {
    private final Function<String, TokenResponse> tokenRetriever;
    private final Clock clock;

    private final ConcurrentMap<String, TokenResponse> tokens = new ConcurrentHashMap<>();

    @Override
    public String getToken(String key) {
        final var currentToken = tokens.get(key);
        if (tokenNotPresentOrExpired(currentToken)) {
            return tokens.compute(key, (k, token) ->
                    tokenNotPresentOrExpired(token) ? tokenRetriever.apply(key) : token).getToken();
        }

        return currentToken.getToken();
    }

    @Override
    public void invalidateToken(String key, String invalidToken) {
        tokens.computeIfPresent(key, (k, currentToken) ->
                Objects.equals(currentToken.getToken(), invalidToken) ? null : currentToken);
    }

    private boolean tokenNotPresentOrExpired(TokenResponse token) {
        return token == null || clock.instant().isAfter(token.getValidThru());
    }
}
