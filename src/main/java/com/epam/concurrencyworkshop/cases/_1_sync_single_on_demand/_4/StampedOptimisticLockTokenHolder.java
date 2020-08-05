package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._4;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._1_TokenHolder;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class StampedOptimisticLockTokenHolder implements _1_TokenHolder {
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
        long stamp = lock.tryOptimisticRead();
        try {
            while (true) {
                final TokenResponse token = currentToken;
                if (lock.validate(stamp)) {
                    if (condition.test(token)) {
                        if ((stamp = lock.tryConvertToWriteLock(stamp)) != 0) {
                            currentToken = tokenRetriever.get();
                            return currentToken;
                        }
                    } else {
                        return token;
                    }
                }
                stamp = lock.writeLock();
            }
        } finally {
            if (StampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }
}
