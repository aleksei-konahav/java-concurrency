package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand;

public interface _1_TokenHolder {
    String getToken();
    void invalidateToken(String invalidToken);
}
