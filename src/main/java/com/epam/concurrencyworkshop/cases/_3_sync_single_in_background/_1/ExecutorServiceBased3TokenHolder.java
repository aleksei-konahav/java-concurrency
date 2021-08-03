package com.epam.concurrencyworkshop.cases._3_sync_single_in_background._1;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._3_sync_single_in_background._3_TokenHolder;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ExecutorServiceBased3TokenHolder implements _3_TokenHolder {
    private final Supplier<TokenResponse> tokenRetriever;
    private final Clock clock;

    private final long errorRefreshDelayNanos;
    private final ScheduledExecutorService executorService;

    private final AtomicReference<Tuple2<Either<Exception, TokenResponse>, ScheduledFuture<?>>>
            currentTokenAndRefreshFuture = new AtomicReference<>();

    public ExecutorServiceBased3TokenHolder(Supplier<TokenResponse> tokenRetriever,
                                            Clock clock,
                                            Duration errorRefreshDelay) {
        this.tokenRetriever = tokenRetriever;
        this.clock = clock;

        this.errorRefreshDelayNanos = errorRefreshDelay.toNanos();
        final AtomicInteger counter = new AtomicInteger();
        this.executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName("token-holder-refresher-" + counter.incrementAndGet());
            return thread;
        });
        this.executorService.execute(() -> this.refreshToken(false));
    }

    public void shutdown() {
        this.executorService.shutdownNow();
    }

    @Override
    public String getToken() {
        Tuple2<Either<Exception, TokenResponse>, ScheduledFuture<?>> currentToken;
        // кстати, решить проблему того, что токена изначально может не быть, можно тем, чтобы генерировать
        // токен ещё в конструкторе, при создании объекта, но тогда нужно подумать как правильно реализовать
        // метод invalidateToken
        while ((currentToken = this.currentTokenAndRefreshFuture.get()) == null) {
            Thread.onSpinWait();
        }

        return currentToken._1
                .fold(ex -> { throw new RuntimeException(ex); }, TokenResponse::getToken);
    }

    @Override
    public void invalidateToken(String invalidToken) {
        final var currentTokenAndFuture = this.currentTokenAndRefreshFuture.get();

        if (currentTokenAndFuture != null
                && currentTokenAndFuture._1.exists(t -> Objects.equals(t.getToken(), invalidToken))) {
            if (this.currentTokenAndRefreshFuture.compareAndSet(currentTokenAndFuture, null)) {
                currentTokenAndFuture._2.cancel(false);

                this.executorService.execute(() -> this.refreshToken(true));
            }
        }
    }

    private void refreshToken(boolean checkTokenNullability) {
        final var currentTokenAndFuture = this.currentTokenAndRefreshFuture.get();
        if (!checkTokenNullability || currentTokenAndFuture == null) {
            try {
                final var tokenResponse = this.tokenRetriever.get();

                final long delay = Duration.between(clock.instant(), tokenResponse.getValidThru()).toNanos();
                final var refreshFuture = executorService
                        .schedule(() -> this.refreshToken(false), delay, TimeUnit.NANOSECONDS);
                this.currentTokenAndRefreshFuture.set(Tuple.of(Either.right(tokenResponse), refreshFuture));
            } catch (Exception ex) {
                final var refreshFuture = executorService
                        .schedule(() -> this.refreshToken(false), errorRefreshDelayNanos, TimeUnit.NANOSECONDS);
                this.currentTokenAndRefreshFuture.set(Tuple.of(Either.left(ex), refreshFuture));
            }
        }
    }
}
