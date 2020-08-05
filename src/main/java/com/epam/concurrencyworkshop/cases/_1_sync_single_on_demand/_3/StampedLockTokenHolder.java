package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._3;

import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._1_TokenHolder;

@RequiredArgsConstructor
public class StampedLockTokenHolder implements _1_TokenHolder {
    private final Supplier<TokenResponse> tokenRetriever;
    private final Clock clock;

    private final StampedLock lock = new StampedLock();

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
        long stamp = lock.readLock();
        try {
            // check condition
            while (condition.test(currentToken)) {
                // condition is true, we should try to acquire write lock and update token
                final long ws = lock.tryConvertToWriteLock(stamp);
                if (ws != 0) {
                    // write lock successfully acquired, we can do what we want (and it will be exclusively)
                    stamp = ws;
                    currentToken = tokenRetriever.get();
                    return currentToken;
                } else {
                    // our attempt for write lock acquiring failed
                    // we unlock our read and will wait for write lock acquiring
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();
                }
            }
            return currentToken;
        } finally {
            lock.unlock(stamp);
        }
    }
}
