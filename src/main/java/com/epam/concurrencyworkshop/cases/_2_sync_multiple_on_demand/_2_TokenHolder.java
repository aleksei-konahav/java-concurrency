package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand;

public interface _2_TokenHolder {
    String getToken(String key);
    void invalidateToken(String key, String invalidToken);
}
