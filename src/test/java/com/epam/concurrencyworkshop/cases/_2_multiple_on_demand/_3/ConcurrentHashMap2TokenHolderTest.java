package com.epam.concurrencyworkshop.cases._2_multiple_on_demand._3;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2_TokenHolder;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._3.ConcurrentHashMap2TokenHolder;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class ConcurrentHashMap2TokenHolderTest {
    private _2_TokenHolder tokenHolder;

    @Mock
    private Clock clock;
    @Mock
    private Function<String, TokenResponse> tokenRetriever;

    @BeforeEach
    void beforeEach() {
        this.tokenHolder = new ConcurrentHashMap2TokenHolder(tokenRetriever, clock);

        when(clock.instant()).thenAnswer(inv -> Instant.now());
    }

    @Test
    void shouldGenerateNewTokenWhenInvalidatedExistingToken() {
        // given
        when(tokenRetriever.apply("key1"))
                .thenReturn(new TokenResponse("token1", Instant.MAX))
                .thenReturn(new TokenResponse("token2", Instant.MAX));
        tokenHolder.getToken("key1");

        // when
        tokenHolder.invalidateToken("key1", "token1");
        String token = tokenHolder.getToken("key1");

        // then
        Assertions.assertThat(token).isEqualTo("token2");
        verify(tokenRetriever, times(2)).apply(eq("key1"));
    }

    @Test
    void shouldGenerateTokenOnlyOnceWhenTwoThreadsRequestTokenForTheSameKey() throws InterruptedException {
        // given
        when(tokenRetriever.apply("key1")).thenAnswer(inv -> new TokenResponse("token", Instant.now().plusSeconds(10)));
        final var errors = new LinkedBlockingQueue<Exception>();
        final var tokens = new LinkedBlockingQueue<String>();
        final var threadsStartedLatch = new CountDownLatch(2);
        final var startExecutionLatch = new CountDownLatch(1);

        final Runnable task = () -> {
            try {
                threadsStartedLatch.countDown();
                startExecutionLatch.await(5, TimeUnit.SECONDS);

                tokens.add(tokenHolder.getToken("key1"));
            } catch (Exception ex) {
                errors.add(ex);
            }
        };

        // when
        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);
        thread1.start();
        thread2.start();

        // wait until threads will be ready
        threadsStartedLatch.await(5, TimeUnit.SECONDS);

        startExecutionLatch.countDown();

        thread1.join();
        thread2.join();

        // then
        Assertions.assertThat(errors).isEmpty();
        Assertions.assertThat(tokens).containsOnly("token");
        verify(tokenRetriever, times(1)).apply(eq("key1"));
    }

}
