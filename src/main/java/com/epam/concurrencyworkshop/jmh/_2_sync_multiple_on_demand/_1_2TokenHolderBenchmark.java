package com.epam.concurrencyworkshop.jmh._2_sync_multiple_on_demand;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._1.FullySynchronized2TokenHolder;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2.Synchronized2TokenHolder;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._3.ConcurrentHashMap2TokenHolder;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._4.StampedLockHoldersMap2TokenHolder;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._5.Caffeine2TokenHolder;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._5.Guava2TokenHolder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
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
import java.util.function.Function;

public class _1_2TokenHolderBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        final Function<String, TokenResponse> tokenRetriever = key ->
                new TokenResponse(key, Clock.systemUTC().instant().plusMillis(100));

        private FullySynchronized2TokenHolder fullySynchronized2TokenHolder =
                new FullySynchronized2TokenHolder(tokenRetriever, Clock.systemUTC());
        private Synchronized2TokenHolder synchronized2TokenHolder =
                new Synchronized2TokenHolder(tokenRetriever, Clock.systemUTC());
        private ConcurrentHashMap2TokenHolder concurrentHashMap2TokenHolder =
                new ConcurrentHashMap2TokenHolder(tokenRetriever, Clock.systemUTC());
        private StampedLockHoldersMap2TokenHolder stampedLockHoldersMap2TokenHolder =
                new StampedLockHoldersMap2TokenHolder(tokenRetriever, Clock.systemUTC(), new String[] { "key1", "key2" });
        private Caffeine2TokenHolder caffeine2TokenHolder =
                new Caffeine2TokenHolder(tokenRetriever, Duration.of(100, ChronoUnit.MILLIS));
        private Guava2TokenHolder guava2TokenHolder =
                new Guava2TokenHolder(tokenRetriever, Duration.of(100, ChronoUnit.MILLIS));
    }

    @Benchmark
    @Group("fullySynchronized2TokenHolder")
    //@GroupThreads(4) use if you need to manually specify amount of threads
    public String fullySynchronized2TokenHolder1(BenchmarkState state) {
        return state.fullySynchronized2TokenHolder.getToken("key1");
    }

    @Benchmark
    @Group("fullySynchronized2TokenHolder")
    public String fullySynchronized2TokenHolder2(BenchmarkState state) {
        return state.fullySynchronized2TokenHolder.getToken("key2");
    }

    @Benchmark
    @Group("synchronized2TokenHolder")
    public String synchronized2TokenHolder1(BenchmarkState state) {
        return state.synchronized2TokenHolder.getToken("key1");
    }

    @Benchmark
    @Group("synchronized2TokenHolder")
    public String synchronized2TokenHolder2(BenchmarkState state) {
        return state.synchronized2TokenHolder.getToken("key2");
    }

    @Benchmark
    @Group("concurrentHashMap2TokenHolder")
    public String concurrentHashMap2TokenHolder1(BenchmarkState state) {
        return state.concurrentHashMap2TokenHolder.getToken("key1");
    }

    @Benchmark
    @Group("concurrentHashMap2TokenHolder")
    public String concurrentHashMap2TokenHolder2(BenchmarkState state) {
        return state.concurrentHashMap2TokenHolder.getToken("key2");
    }

    @Benchmark
    @Group("stampedLockHoldersMap2TokenHolder")
    public String stampedLockHoldersMap2TokenHolder1(BenchmarkState state) {
        return state.stampedLockHoldersMap2TokenHolder.getToken("key1");
    }

    @Benchmark
    @Group("stampedLockHoldersMap2TokenHolder")
    public String stampedLockHoldersMap2TokenHolder2(BenchmarkState state) {
        return state.stampedLockHoldersMap2TokenHolder.getToken("key2");
    }

    @Benchmark
    @Group("caffeine2TokenHolder")
    public String caffeine2TokenHolder1(BenchmarkState state) {
        return state.caffeine2TokenHolder.getToken("key1");
    }

    @Benchmark
    @Group("caffeine2TokenHolder")
    public String caffeine2TokenHolder2(BenchmarkState state) {
        return state.caffeine2TokenHolder.getToken("key2");
    }

    @Benchmark
    @Group("guava2TokenHolder")
    public String guava2TokenHolder1(BenchmarkState state) {
        return state.guava2TokenHolder.getToken("key1");
    }

    @Benchmark
    @Group("guava2TokenHolder")
    public String guava2TokenHolder2(BenchmarkState state) {
        return state.guava2TokenHolder.getToken("key2");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(_1_2TokenHolderBenchmark.class.getSimpleName())
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
