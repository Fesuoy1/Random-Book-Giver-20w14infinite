package org.mod.rng_book.random;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;

public class RandomSequencesState
extends PersistentState {
    private long seed;
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<Identifier, RandomSequence> sequences = new Object2ObjectOpenHashMap<Identifier, RandomSequence>();

    public RandomSequencesState(long seed) {
        super(null);
        this.seed = seed;
    }



    public Random getOrCreate(Identifier id) {
        Random random = this.sequences.computeIfAbsent(id, this::createSequence).getSource();
        return new WrappedRandom(random);
    }

    private RandomSequence createSequence(Identifier id) {
        return this.createSequence(id, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(Identifier id, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        long l = (includeWorldSeed ? this.seed : 0L) ^ (long)salt;
        return new RandomSequence(l, includeSequenceId ? Optional.of(id) : Optional.empty());
    }

    public void forEachSequence(BiConsumer<Identifier, RandomSequence> consumer) {
        this.sequences.forEach(consumer);
    }

    public void setDefaultParameters(int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        this.salt = salt;
        this.includeWorldSeed = includeWorldSeed;
        this.includeSequenceId = includeSequenceId;
    }

    public int resetAll() {
        int i = this.sequences.size();
        this.sequences.clear();
        return i;
    }

    public void reset(Identifier id) {
        this.sequences.put(id, this.createSequence(id));
    }

    public void reset(Identifier id, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        this.sequences.put(id, this.createSequence(id, salt, includeWorldSeed, includeSequenceId));
    }

    class WrappedRandom
    implements Random {
        private final Random random;

        WrappedRandom(Random random) {
            this.random = random;
        }

        @Override
        public Random split() {
            RandomSequencesState.this.markDirty();
            return this.random.split();
        }

        @Override
        public RandomSplitter nextSplitter() {
            RandomSequencesState.this.markDirty();
            return this.random.nextSplitter();
        }

        @Override
        public void setSeed(long seed) {
            RandomSequencesState.this.markDirty();
            this.random.setSeed(seed);
        }

        @Override
        public int nextInt() {
            RandomSequencesState.this.markDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            RandomSequencesState.this.markDirty();
            return this.random.nextInt(bound);
        }

        @Override
        public long nextLong() {
            RandomSequencesState.this.markDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequencesState.this.markDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequencesState.this.markDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequencesState.this.markDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequencesState.this.markDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof WrappedRandom) {
                WrappedRandom wrappedRandom = (WrappedRandom)o;
                return this.random.equals(wrappedRandom.random);
            }
            return false;
        }
    }

    @Override
    public void fromTag(CompoundTag tag) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromTag'");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toTag'");
    }
}

