package com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._2;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;

import com.epam.concurrencyworkshop.__task.TokenResponse;
import com.epam.concurrencyworkshop.cases._1_sync_single_on_demand._1_TokenHolder;

@JCStressTest
@Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "Token should be initialized only ones")
@Outcome(id = "", expect = Expect.FORBIDDEN, desc = "Token was initialized not only ones")
@State
public class ReadWriteLockTokenHolderJcStressTest {
    private AtomicInteger count = new AtomicInteger();

    private _1_TokenHolder tokenHolder = new ReadWriteLockTokenHolder(() -> {
        count.incrementAndGet();
        return new TokenResponse("token", Instant.MAX);
    }, Clock.systemUTC());

    @Actor
    public String actor1() {
        return tokenHolder.getToken();
    }

    @Actor
    public String actor2() {
        return tokenHolder.getToken();
    }

    @Actor
    public String actor3() {
        return tokenHolder.getToken();
    }

    @Actor
    public String actor4() {
        return tokenHolder.getToken();
    }

    @Arbiter
    public void arbiter(I_Result result) {
        result.r1 = count.get();
    }
}
