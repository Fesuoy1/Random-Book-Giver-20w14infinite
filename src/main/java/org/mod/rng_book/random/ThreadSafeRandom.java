package org.mod.rng_book.random;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A random that can be shared by multiple threads safely.
 */
@SuppressWarnings("unused")
@Deprecated
public class ThreadSafeRandom
implements BaseRandom {
    
    private static final int INT_BITS = 48;
    private static final long SEED_MASK = 0xFFFFFFFFFFFFL;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private final AtomicLong seed = new AtomicLong();
    private final GaussianGenerator gaussianGenerator = new GaussianGenerator(this);

    public ThreadSafeRandom(long seed) {
        this.setSeed(seed);
    }

    @Override
    public Random split() {
        return new ThreadSafeRandom(this.nextLong());
    }

    @Override
    public RandomSplitter nextSplitter() {
        return new CheckedRandom.Splitter(this.nextLong());
    }

    @Override
    public void setSeed(long seed) {
        this.seed.set((seed ^ 0x5DEECE66DL) & 0xFFFFFFFFFFFFL);
    }

    @Override
    public int next(int bits) {
        long m;
        long l;
        while (!this.seed.compareAndSet(l = this.seed.get(), m = l * 25214903917L + 11L & 0xFFFFFFFFFFFFL)) {
        }
        return (int)(m >>> 48 - bits);
    }

    @Override
    public double nextGaussian() {
        return this.gaussianGenerator.next();
    }
}

