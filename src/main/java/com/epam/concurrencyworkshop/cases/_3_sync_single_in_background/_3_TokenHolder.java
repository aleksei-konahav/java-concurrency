package com.epam.concurrencyworkshop.cases._3_sync_single_in_background;

public interface  _3_TokenHolder {
    String getToken();
    void invalidateToken(String invalidToken);
}
