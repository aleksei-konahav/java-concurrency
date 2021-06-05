package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._5;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2_TokenHolder;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.function.Function;

public class Guava2TokenHolder implements _2_TokenHolder {
    private final LoadingCache<String, TokenResponse> cache;

    public Guava2TokenHolder(Function<String, TokenResponse> tokenRetriever, Duration ttl) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(ttl)
                .build(new CacheLoader<>() {
                    @Override
                    public TokenResponse load(String key) throws Exception {
                        return tokenRetriever.apply(key);
                    }
                });
    }

    @Override
    @SneakyThrows
    public String getToken(String key) {
        return this.cache.get(key).getToken();
    }

    @Override
    public void invalidateToken(String key, String invalidToken) {
        this.cache.invalidate(key);
    }
}
