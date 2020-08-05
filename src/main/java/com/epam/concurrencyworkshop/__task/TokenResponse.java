package com.epam.concurrencyworkshop.__task;

import lombok.Value;

import java.time.Instant;

@Value
public class TokenResponse {
    String token;
    Instant validThru;
}
