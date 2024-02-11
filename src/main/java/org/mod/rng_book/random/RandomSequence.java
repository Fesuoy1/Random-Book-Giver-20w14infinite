package org.mod.rng_book.random;

import java.util.Optional;
import net.minecraft.util.Identifier;

public class RandomSequence {
    private final Xoroshiro128PlusPlusRandom source;

    public RandomSequence(Xoroshiro128PlusPlusRandom source) {
        this.source = source;
    }

    public RandomSequence(long seed, Identifier id) {
        this(RandomSequence.createSource(seed, Optional.of(id)));
    }

    public RandomSequence(long seed, Optional<Identifier> id) {
        this(RandomSequence.createSource(seed, id));
    }

    private static Xoroshiro128PlusPlusRandom createSource(long seed, Optional<Identifier> id) {
        RandomSeed.XoroshiroSeed xoroshiroSeed = RandomSeed.createUnmixedXoroshiroSeed(seed);
        if (id.isPresent()) {
            xoroshiroSeed = xoroshiroSeed.split(RandomSequence.createSeed(id.get()));
        }
        return new Xoroshiro128PlusPlusRandom(xoroshiroSeed.mix());
    }

    public static RandomSeed.XoroshiroSeed createSeed(Identifier id) {
        return RandomSeed.createXoroshiroSeed(id.toString());
    }

    public Random getSource() {
        return this.source;
    }
}

