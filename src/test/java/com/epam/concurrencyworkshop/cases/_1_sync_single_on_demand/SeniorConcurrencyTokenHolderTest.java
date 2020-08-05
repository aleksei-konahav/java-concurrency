package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._4.StampedOptimisticLockTokenHolder;
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
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class SeniorConcurrencyTokenHolderTest {
    private _1_TokenHolder tokenHolder;

    @Mock
    private Clock clock;
    @Mock
    private Supplier<TokenResponse> tokenRetriever;

    @BeforeEach
    void beforeEach() {
        this.tokenHolder = new StampedOptimisticLockTokenHolder(tokenRetriever, clock);

        when(clock.instant()).thenAnswer(inv -> Instant.now());
    }

    @SneakyThrows
    @Test
    void shouldCreateOnlyOneTokenOnFirstAccess() {
        when(tokenRetriever.get()).thenAnswer(inv -> new TokenResponse("token", Instant.now().plusSeconds(10)));
        final var latch = new CountDownLatch(1);

        final Runnable runnable = () -> {
            try {
                latch.await();
                tokenHolder.getToken();
            } catch (InterruptedException ex) {
                throw new AssertionError(ex);
            }
        };

        final Thread[] threads = Stream.generate(() -> new Thread(runnable))
                .limit(4)
                .peek(Thread::start)
                .toArray(Thread[]::new);

        latch.countDown();

        for (Thread thread : threads) {
            thread.join();
        }

        verify(tokenRetriever).get();
    }
}
