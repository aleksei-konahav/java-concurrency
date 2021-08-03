package com.epam.concurrencyworkshop.jmh._3_sync_single_in_background;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._3_sync_single_in_background._1.ExecutorServiceBased3TokenHolder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class _3_3TokenHolderBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        final Supplier<TokenResponse> tokenRetriever = () ->
                new TokenResponse("token", Clock.systemUTC().instant().plusMillis(100));

        private ExecutorServiceBased3TokenHolder tokenHolder =
                new ExecutorServiceBased3TokenHolder(tokenRetriever, Clock.systemUTC(), Duration.of(5, ChronoUnit.SECONDS));
    }

    @Benchmark
    public String synchronizedTokenHolder(BenchmarkState state) {
        return state.tokenHolder.getToken();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(_3_3TokenHolderBenchmark.class.getSimpleName())
                .forks(1).warmupForks(1)
                .warmupIterations(10).warmupTime(TimeValue.seconds(1))
                .measurementIterations(5).measurementTime(TimeValue.seconds(1))
                .timeUnit(TimeUnit.MICROSECONDS)
                .mode(Mode.Throughput)
                .threads(Runtime.getRuntime().availableProcessors())
                .build();

        new Runner(opt).run();
    }
}
