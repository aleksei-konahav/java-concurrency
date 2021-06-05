package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._5;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2_TokenHolder;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.time.Duration;
import java.util.function.Function;

public class Caffeine2TokenHolder implements _2_TokenHolder {
    private final LoadingCache<String, TokenResponse> cache;

    public Caffeine2TokenHolder(Function<String, TokenResponse> tokenRetriever, Duration ttl) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .build(tokenRetriever::apply);
    }

    @Override
    public String getToken(String key) {
        return this.cache.get(key).getToken();
    }

    @Override
    public void invalidateToken(String key, String invalidToken) {
        this.cache.invalidate(key);
    }
}
