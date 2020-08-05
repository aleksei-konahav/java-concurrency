package com.epam.concurrencyworkshop.jmh._1_sync_single_on_demand;

import lombok.RequiredArgsConstructor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class _3_RWLockVsSynchronizedBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private List<Integer> ints = ThreadLocalRandom.current().ints()
                .limit(1_000)
                .boxed()
                .collect(Collectors.toList());

        private SynchronizedListSum synchronizedListSum = new SynchronizedListSum(ints);
        private ReadWriteLockListSum readWriteLockListSum = new ReadWriteLockListSum(ints);
    }

    @Benchmark
    public int synchronizedSum(BenchmarkState state) {
        return state.synchronizedListSum.sum();
    }

    @Benchmark
    public int readWriteLockSum(BenchmarkState state) {
        return state.readWriteLockListSum.sum();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(_3_RWLockVsSynchronizedBenchmark.class.getSimpleName())
                .forks(1).warmupForks(1)
                .warmupIterations(3).warmupTime(TimeValue.seconds(1))
                .measurementIterations(5).measurementTime(TimeValue.seconds(1))
                .timeUnit(TimeUnit.MILLISECONDS)
                .mode(Mode.Throughput)
                .threads(Runtime.getRuntime().availableProcessors())
                .build();

        new Runner(opt).run();
    }

    @RequiredArgsConstructor
    public static class SynchronizedListSum {
        private final List<Integer> ints;

        public synchronized int sum() {
            int sum = 0;
            for (Integer i : ints) {
                sum += i;
            }
            return sum;
        }

        public synchronized void add(int i) {
            ints.add(i);
        }
    }

    @RequiredArgsConstructor
    public static class ReadWriteLockListSum {
        private final List<Integer> ints;
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        public int sum() {
            lock.readLock().lock();
            try {
                int sum = 0;
                for (Integer i : ints) {
                    sum += i;
                }
                return sum;
            } finally {
                lock.readLock().unlock();
            }
        }

        public void add(int i) {
            lock.writeLock().lock();
            try {
                ints.add(i);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
