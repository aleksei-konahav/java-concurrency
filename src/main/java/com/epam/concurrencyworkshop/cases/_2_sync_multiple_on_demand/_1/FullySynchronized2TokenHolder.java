package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._1;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._0.Simple2TokenHolder;

import java.time.Clock;
import java.util.function.Function;

public class FullySynchronized2TokenHolder extends Simple2TokenHolder {
    public FullySynchronized2TokenHolder(Function<String, TokenResponse> tokenRetriever, Clock clock) {
        super(tokenRetriever, clock);
    }

    @Override
    public synchronized String getToken(String key) {
        return super.getToken(key);
    }

    @Override
    public synchronized void invalidateToken(String key, String invalidToken) {
        super.invalidateToken(key, invalidToken);
    }
}
