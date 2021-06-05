package com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._3;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2.Synchronized2TokenHolder;
import com.epam.concurrencyworkshop.cases._2_sync_multiple_on_demand._2_TokenHolder;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Token should be initialized only twice (for different keys)")
@Outcome(expect = Expect.FORBIDDEN, desc = "Token was initialized not only twice")
@State
public class ConcurrentHashMap2TokenHolderJcStressTest {
    private AtomicInteger count = new AtomicInteger();

    private _2_TokenHolder tokenHolder = new ConcurrentHashMap2TokenHolder(key -> {
        count.incrementAndGet();
        return new TokenResponse(key, Instant.MAX);
    }, Clock.systemUTC());

    @Actor
    public String actor1() {
        return tokenHolder.getToken("key1");
    }

    @Actor
    public String actor2() {
        return tokenHolder.getToken("key1");
    }

    @Actor
    public String actor3() {
        return tokenHolder.getToken("key2");
    }

    @Actor
    public String actor4() {
        return tokenHolder.getToken("key2");
    }

    @Arbiter
    public void arbiter(I_Result result) {
        result.r1 = count.get();
    }
}
