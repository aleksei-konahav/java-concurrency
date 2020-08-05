package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._0.SimpleTokenHolder;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Supplier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class JuniorConcurrencyTokenHolderTest {
    private _1_TokenHolder tokenHolder;

    @Mock
    private Clock clock;
    @Mock
    private Supplier<TokenResponse> tokenRetriever;

    @BeforeEach
    void beforeEach() {
        this.tokenHolder = new SimpleTokenHolder(tokenRetriever, clock);

        when(clock.instant()).thenAnswer(inv -> Instant.now());
    }

    @SneakyThrows
    @Test
    void shouldCreateOnlyOneTokenOnFirstAccess() {
        when(tokenRetriever.get()).thenAnswer(inv -> new TokenResponse("token", Instant.now().plusSeconds(10)));

        final var token = tokenHolder.getToken();
        final var token2 = tokenHolder.getToken();

        Assertions.assertThat(token).isEqualTo("token");
        Assertions.assertThat(token2).isEqualTo("token");

        verify(tokenRetriever).get();
    }
}

