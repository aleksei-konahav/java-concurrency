package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._4;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._1_TokenHolder;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._4.StampedOptimisticLockTokenHolder;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2_TokenHolder;

import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StampedLockHoldersMap2TokenHolder implements _2_TokenHolder {
    private final Map<String, _1_TokenHolder> tokens;

    public StampedLockHoldersMap2TokenHolder(Map<String, Supplier<TokenResponse>> tokenRetrievers, Clock clock) {
        this.tokens = tokenRetrievers.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                        entry -> new StampedOptimisticLockTokenHolder(entry.getValue(), clock)));
    }

    public StampedLockHoldersMap2TokenHolder(Function<String, TokenResponse> tokenRetriever, Clock clock, String[] keys) {
        this.tokens = Arrays.stream(keys)
                .collect(Collectors.toUnmodifiableMap(Function.identity(),
                        key -> new StampedOptimisticLockTokenHolder(() -> tokenRetriever.apply(key), clock)));
    }

    @Override
    public String getToken(String key) {
        return tokens.get(key).getToken();
    }

    @Override
    public void invalidateToken(String key, String invalidToken) {
        tokens.get(key).invalidateToken(invalidToken);
    }
}
