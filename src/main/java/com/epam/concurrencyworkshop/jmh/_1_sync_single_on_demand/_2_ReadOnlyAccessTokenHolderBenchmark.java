package com.epam.concurrencyworkshop.jmh._1_sync_single_on_demand;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._1.SynchronizedTokenHolder;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._2.ReadWriteLockTokenHolder;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._3.StampedLockTokenHolder;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._4.StampedOptimisticLockTokenHolder;

public class _2_ReadOnlyAccessTokenHolderBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        final Supplier<TokenResponse> tokenRetriever = () -> new TokenResponse("token", Instant.MAX);

        private SynchronizedTokenHolder synchronizedTokenHolder =
                new SynchronizedTokenHolder(tokenRetriever, Clock.systemUTC());;
        private ReadWriteLockTokenHolder readWriteLockTokenHolder =
                new ReadWriteLockTokenHolder(tokenRetriever, Clock.systemUTC());
        private StampedLockTokenHolder stampedLockTokenHolder =
                new StampedLockTokenHolder(tokenRetriever, Clock.systemUTC());
        private StampedOptimisticLockTokenHolder stampedOptimisticLockTokenHolder =
                new StampedOptimisticLockTokenHolder(tokenRetriever, Clock.systemUTC());

        @Setup(Level.Iteration)
        public void setup() {
            readWriteLockTokenHolder.getToken();
        }
    }

    @Benchmark
    public String synchronizedTokenHolder(BenchmarkState state) {
        return state.synchronizedTokenHolder.getToken();
    }

    @Benchmark
    public String readWriteLockTokenHolder(BenchmarkState state) {
        return state.readWriteLockTokenHolder.getToken();
    }

    @Benchmark
    public String stampedLockTokenHolder(BenchmarkState state) {
        return state.stampedLockTokenHolder.getToken();
    }

    @Benchmark
    public String stampedOptimisticLockTokenHolder(BenchmarkState state) {
        return state.stampedOptimisticLockTokenHolder.getToken();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(_2_ReadOnlyAccessTokenHolderBenchmark.class.getSimpleName())
                .forks(1).warmupForks(1)
                .warmupIterations(3).warmupTime(TimeValue.seconds(1))
                .measurementIterations(5).measurementTime(TimeValue.seconds(1))
                .timeUnit(TimeUnit.MICROSECONDS)
                .mode(Mode.Throughput)
                .threads(Runtime.getRuntime().availableProcessors())
                .build();

        new Runner(opt).run();
    }
}
