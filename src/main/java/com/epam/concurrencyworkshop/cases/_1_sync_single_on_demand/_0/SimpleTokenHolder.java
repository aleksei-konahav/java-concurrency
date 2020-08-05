package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._0;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.function.Supplier;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._1_TokenHolder;

@RequiredArgsConstructor
public class SimpleTokenHolder implements _1_TokenHolder {
    private final Supplier<TokenResponse> tokenRetriever;
    private final Clock clock;

    private TokenResponse currentToken;

    @Override
    public String getToken() {
        if (currentToken == null || clock.instant().isAfter(currentToken.getValidThru())) {
            currentToken = tokenRetriever.get();
        }

        return currentToken.getToken();
    }

    @Override
    public void invalidateToken(String invalidToken) {
        if (currentToken != null && Objects.equals(currentToken.getToken(), invalidToken)) {
            currentToken = null;
        }
    }
}
