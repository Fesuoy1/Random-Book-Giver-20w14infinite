package org.mod.rng_book;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBookGiver implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("rng_book_giver-inf");
    private static final char[] RANDOM_CHARACTERS = {' ', ',', '.', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
            'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')',
            '+', '=', '[', ']', '{', '}', '|', '\\', '`', '~', ':', ';', '<', '>', '?', '/'};

    private static final String[] AUTHORS = {
            "Dwarf and the Stars",
            "Wizard and the North",
            "Warrior of a Land",
            "Crafter and the Mines",
            "Druid of the Sky",
            "Mage of the Wilds",
            "Universe itself"
    };

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static final int LENGTH = RANDOM_CHARACTERS.length;
    private static final int MAX_PAGES = 16;
    private static final int MIN_CHARS_PER_PAGE = 64;
    private static final int MAX_CHARS_PER_PAGE = 256;
    private static final int COORDINATE_RANGE = 6900;

    public static void giveRandomBook(World world, PlayerEntity player) {
        Direction direction = Direction.random(world.getRandom());
        int x = RANDOM.nextInt(1, COORDINATE_RANGE);
        int y = RANDOM.nextInt(1, COORDINATE_RANGE);
        int z = RANDOM.nextInt(1, COORDINATE_RANGE);
        BlockPos pos = new BlockPos(x, y, z);
        int i = pos.getY();
        int u;
        byte k;
        try {
            switch (direction) {
                case NORTH:
                    u = 15 - pos.getX() & 15;
                    k = 0;
                    break;
                case SOUTH:
                    u = pos.getX() & 15;
                    k = 2;
                    break;
                case EAST:
                    u = 15 - pos.getZ() & 15;
                    k = 1;
                    break;
                case WEST:
                default:
                    u = pos.getZ() & 15;
                    k = 3;
            }
        } catch (Exception ex) {
            u = RANDOM.nextInt(1, COORDINATE_RANGE);
            k = (byte) RANDOM.nextInt(1, 10);
        }
        while (u == 0 || i == 15) {
            i = RANDOM.nextInt(1, COORDINATE_RANGE);
            u = RANDOM.nextInt(1, COORDINATE_RANGE);
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        String string = chunkPos.x + "/" + chunkPos.z + "/" + k + "/" + u + "/" + i;
        Random random = new Random(chunkPos.x);
        Random random2 = new Random(chunkPos.z);
        Random random3 = new Random(((long) u << 8) + ((long) i << 4) + k);
        ItemStack itemStack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = new ListTag();

        for (int l = 0; l < MAX_PAGES; ++l) {
            StringBuilder stringBuilder = new StringBuilder();

            for (int m = 0; m < RANDOM.nextInt(MIN_CHARS_PER_PAGE, MAX_CHARS_PER_PAGE); ++m) {

                int n = random.nextInt() + random2.nextInt() - random3.nextInt();

                if (RANDOM.nextBoolean()) {
                    stringBuilder.append(Formatting.OBFUSCATED);
                }

                stringBuilder.append(RANDOM_CHARACTERS[n % LENGTH]);

                if (RANDOM.nextInt(10) < 3) {
                    stringBuilder.append(Formatting.RESET);
                }
            }

            listTag.add(StringTag.of(Text.Serializer.toJson(new LiteralText(stringBuilder.toString()))));
        }
        String author = AUTHORS[RANDOM.nextInt(AUTHORS.length)];

        compoundTag.put("pages", listTag);
        compoundTag.putString("author", Formatting.OBFUSCATED + author);
        compoundTag.putString("title", string);
        PlayerInventory inventory = player.inventory;
        int slot = inventory.getEmptySlot();
        if (slot == -1) {
            player.dropItem(itemStack, false);
            return;
        }
        inventory.insertStack(slot, itemStack);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Random Book Giver Initialized");
    }
}