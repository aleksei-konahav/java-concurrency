package com.epam.concurrencyworkshop.cases._3_sync_single_in_background;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._3_sync_single_in_background._1.ExecutorServiceBased3TokenHolder;
import io.vavr.CheckedFunction0;
import io.vavr.Function0;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ExecutorServiceBased3TokenHolderTest {

    @Test
    @Timeout(5)
    void shouldReturnNonNullTokenWhenCallGetToken() {
        // given
        final var tokenHolder = new ExecutorServiceBased3TokenHolder(
                () -> new TokenResponse("token", Instant.now().plusSeconds(1_000_000)),
                Clock.systemUTC(), Duration.ofDays(10));

        try {
            // when
            String token = tokenHolder.getToken();

            // then
            Assertions.assertThat(token).isEqualTo("token");
        } finally {
            tokenHolder.shutdown();
        }
    }

    @Test
    @Timeout(5)
    void shouldGenerateTokensInBackgroundWhenCallGetTokenSeveralTimes() throws BrokenBarrierException, InterruptedException {
        // given
        final var counter = new AtomicInteger();
        final var barrier = new CyclicBarrier(2);

        final var tokenHolder = new ExecutorServiceBased3TokenHolder(
                checkedSupplier(() -> {
                    barrier.await();
                    return new TokenResponse("token" + counter.incrementAndGet(), Instant.now());
                }),
                Clock.systemUTC(), Duration.ofDays(10));

        try {
            barrier.await();

            Awaitility.await()
                    .atMost(5, TimeUnit.SECONDS)
                    .pollDelay(50, TimeUnit.MILLISECONDS)
                    .untilAsserted(() -> {
                        // when
                        String token = tokenHolder.getToken();

                        // then
                        Assertions.assertThat(token).isEqualTo("token" + counter.get());
                    });

            barrier.await();

            Awaitility.await()
                    .atMost(5, TimeUnit.SECONDS)
                    .pollDelay(50, TimeUnit.MILLISECONDS)
                    .untilAsserted(() -> {
                        // when
                        String token = tokenHolder.getToken();

                        // then
                        Assertions.assertThat(token).isEqualTo("token" + counter.get());
                    });
        } finally {
            tokenHolder.shutdown();
        }
    }

    @Test
    @Timeout(5)
    void shouldGenerateNewTokenOnceWhenCallInvalidateTokenDuringTokenGeneration() throws InterruptedException {
        // given
        final var counter = new AtomicInteger();
        final var latchForMainThread = new CountDownLatch(1);
        final var latchInBackgroundThread = new CountDownLatch(1);

        final var tokenHolder = new ExecutorServiceBased3TokenHolder(
                checkedSupplier(() -> {
                    if (counter.get() == 0) {
                        return new TokenResponse("token" + counter.incrementAndGet(), Instant.now());
                    } else if (counter.get() == 1) {
                        latchInBackgroundThread.countDown();
                        latchForMainThread.await(5, TimeUnit.SECONDS);
                    }
                    return new TokenResponse("token" + counter.incrementAndGet(),
                            Instant.now().plusSeconds(1_000_000));
                }),
                Clock.systemUTC(), Duration.ofDays(10));

        try {
            // when
            String token = tokenHolder.getToken();

            // then
            String currentToken = "token" + counter.get();
            Assertions.assertThat(token).isEqualTo(currentToken);

            // when
            latchInBackgroundThread.await(5, TimeUnit.SECONDS);
            tokenHolder.invalidateToken(currentToken);
            latchForMainThread.countDown();

            // then
            Assertions.assertThat(tokenHolder.getToken()).isEqualTo("token" + counter.get());
            Assertions.assertThat(counter).hasValue(2);
        } finally {
            tokenHolder.shutdown();
        }
    }

    private static <R> Supplier<R> checkedSupplier(CheckedFunction0<R> f) {
        return () -> {
            try {
                return f.apply();
            } catch (Throwable tr) {
                throw new RuntimeException(tr);
            }
        };
    }
}
