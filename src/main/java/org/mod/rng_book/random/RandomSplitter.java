package org.mod.rng_book.random;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * A random splitter represents necessary bits from a random that can be
 * combined with other seeds to split a new random from the old one.
 */
public interface RandomSplitter {
    default public Random split(BlockPos pos) {
        return this.split(pos.getX(), pos.getY(), pos.getZ());
    }

    default public Random split(Identifier seed) {
        return this.split(seed.toString());
    }

    public Random split(String var1);

    public Random split(int var1, int var2, int var3);

    @VisibleForTesting
    public void addDebugInfo(StringBuilder var1);
}

