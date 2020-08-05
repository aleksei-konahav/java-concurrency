package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._2;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._1_TokenHolder;

@RequiredArgsConstructor
public class ReadWriteLockTokenHolder implements _1_TokenHolder {
    private final Supplier<TokenResponse> tokenRetriever;
    private final Clock clock;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private TokenResponse currentToken;

    @Override
    public String getToken() {
        final Predicate<TokenResponse> tokenNotPresentOrExpired =
                token -> token == null || clock.instant().isAfter(token.getValidThru());
        return updateTokenIf(tokenNotPresentOrExpired, tokenRetriever).getToken();
    }

    @Override
    public void invalidateToken(String invalidToken) {
        updateTokenIf(token -> token != null && Objects.equals(token.getToken(), invalidToken), () -> null);
    }

    private TokenResponse updateTokenIf(Predicate<TokenResponse> condition, Supplier<TokenResponse> tokenRetriever) {
        lock.readLock().lock();
        try {
            if (!condition.test(currentToken)) {
                return currentToken;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            // Recheck state because another thread might have
            // acquired write lock and changed state before we did.
            if (condition.test(currentToken)) {
                currentToken = tokenRetriever.get();
            }
            return currentToken;
        } finally {
            lock.writeLock().unlock();
        }
    }
}
