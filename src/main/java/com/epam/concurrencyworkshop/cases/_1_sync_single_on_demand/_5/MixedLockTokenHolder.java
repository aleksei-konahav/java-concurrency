package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._5;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._1_TokenHolder;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class MixedLockTokenHolder implements _1_TokenHolder {
    private final Supplier<TokenResponse> tokenRetriever;
    private final Clock clock;

    private volatile TokenResponse currentToken;

    @Override
    public String getToken() {
        final Predicate<TokenResponse> tokenNotPresentOrExpired = token ->
                token == null || clock.instant().isAfter(token.getValidThru());

        final var currentToken = this.currentToken;

        if (tokenNotPresentOrExpired.test(currentToken)) {
            return updateTokenIf(tokenNotPresentOrExpired, tokenRetriever).getToken();
        }
        return currentToken.getToken();
    }

    @Override
    public void invalidateToken(String invalidToken) {
        updateTokenIf(token -> token != null && Objects.equals(token.getToken(), invalidToken), () -> null);
    }

    private synchronized TokenResponse updateTokenIf(Predicate<TokenResponse> condition, Supplier<TokenResponse> tokenRetriever) {
        final var currentToken = this.currentToken;
        if (condition.test(currentToken)) {
            final var newToken = tokenRetriever.get();
            this.currentToken = newToken;
            return newToken;
        }
        return currentToken;
    }
}
