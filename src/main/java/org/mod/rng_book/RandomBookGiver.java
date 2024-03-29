package org.mod.rng_book;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mod.rng_book.random.Random;

import java.util.Collection;
import java.util.Locale;


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
            "Dwarf and the Mountains",
            "Fey and the Stars",
            "Rogue and the Mountains",
            "Warrior of the Wilds",
            "Galaxy themselves",
            "Universe itself"
    };

    private static final Random RANDOM = Random.create();
    private static final int LENGTH = RANDOM_CHARACTERS.length;
    private static final int MAX_PAGES = 16;
    private static final int MIN_CHARS_PER_PAGE = 100;
    private static final int MAX_CHARS_PER_PAGE = 128;
    private static final int COORDINATE_RANGE = 69000;

    private static final int SUCCESS = 1;
    private static final int FAILURE = 0;

    public static void giveRandomBook(World world, PlayerEntity player) {
        Direction direction = Direction.random(world.getRandom());
        BlockPos pos = getRandomBlockPos(world);
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
            u = getRandomCoordinate();
            k = getRandomByte();
        }
        while (u == 0 || i == 15) {
            i = getRandomCoordinate();
            u = getRandomCoordinate();
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        String string = getChunkString(chunkPos, k, u, i);
        Random random = Random.create(chunkPos.x);
        Random random2 = Random.create(chunkPos.z);
        Random random3 = Random.create(((long) u << 8) + ((long) i << 4) + k);
        ItemStack itemStack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = generateRandomPages(random, random2, random3);

        String author = getRandomAuthor();

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

    private static BlockPos getRandomBlockPos(World world) {
        int x = RANDOM.nextBetween(1, COORDINATE_RANGE);
        int y = RANDOM.nextBetween(1, COORDINATE_RANGE);
        int z = RANDOM.nextBetween(1, COORDINATE_RANGE);
        return new BlockPos(x, y, z);
    }

    private static int getRandomCoordinate() {
        return RANDOM.nextBetween(1, COORDINATE_RANGE);
    }

    private static byte getRandomByte() {
        return (byte) RANDOM.nextBetween(1, 10);
    }

    private static String getChunkString(ChunkPos chunkPos, byte k, int u, int i) {
        return chunkPos.x + "/" + chunkPos.z + "/" + k + "/" + u + "/" + i;
    }

    private static ListTag generateRandomPages(Random random, Random random2, Random random3) {
        ListTag listTag = new ListTag();
        for (int l = 0; l < MAX_PAGES; ++l) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int m = 0; m < RANDOM.nextBetween(MIN_CHARS_PER_PAGE, MAX_CHARS_PER_PAGE); ++m) {
                int n = random.nextInt() + random2.nextInt() - random3.nextInt();
                if (RANDOM.nextBoolean()) {
                    stringBuilder.append(Formatting.OBFUSCATED);
                }
                stringBuilder.append(RANDOM_CHARACTERS[Math.floorMod(n, LENGTH)]);
                if (RANDOM.nextInt(10) <= 3) {
                    stringBuilder.append(Formatting.RESET);
                }
            }
            listTag.add(StringTag.of(Text.Serializer.toJson(new LiteralText(stringBuilder.toString()))));
        }
        return listTag;
    }

    private static String getRandomAuthor() {
        return AUTHORS[RANDOM.nextInt(AUTHORS.length)];
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        for (String name : BookType.ALL) {
            dispatcher.register(CommandManager.literal("giveBook_" + name)
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> giveBookToPlayer(context.getSource(), name, IntegerArgumentType.getInteger(context, "amount"), null))));

            dispatcher.register(CommandManager.literal("giveBook_" + name)
                    .requires(source -> source.hasPermissionLevel(3))
                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
                            .then(CommandManager.argument("players", EntityArgumentType.players())
                                    .executes(context -> giveBookToPlayer(context.getSource(), name, IntegerArgumentType.getInteger(context, "amount"), EntityArgumentType.getPlayers(context, "players"))))));

            dispatcher.register(CommandManager.literal("giveBook_" + name)
                    .requires(source -> source.hasPermissionLevel(3))
                    .executes(context -> giveBookToPlayer(context.getSource(), name, 1, null)));
        }
    }

    private static int giveBookToPlayer(@NotNull ServerCommandSource source, String name, int amount, @Nullable Collection<ServerPlayerEntity> players) {
        BlockPos pos = new BlockPos(source.getPosition());

        PlayerEntity closestPlayer = null;
        if (players == null || players.isEmpty()) {
            closestPlayer = source.getWorld().getClosestPlayer(TargetPredicate.DEFAULT, pos.getX(), pos.getY(), pos.getZ());
        }
        String bookType = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        if (closestPlayer != null) {
            for (int i = 0; i < amount; ++i) {
                giveBook(closestPlayer.getDisplayName().getString(), name, bookType + " Book", closestPlayer);
            }
            return SUCCESS;
        } else if (players != null && !players.isEmpty()) {
            for (ServerPlayerEntity player : players) {
                for (int i = 0; i < amount; ++i) {
                    giveBook(player.getDisplayName().getString(), name, bookType + " Book", player);
                }
            }
            return SUCCESS;
        }
        return FAILURE;
    }

    public static void giveBook(String author, @NotNull String message, String title, PlayerEntity player) {
        ItemStack itemStack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = new ListTag();

        StringBuilder pageText = new StringBuilder();

        // Append the entire message to a single page
        for (int i = 0; i < message.length(); ++i) {
            pageText.append(message.charAt(i));

            // Each page can only contain 128 characters
            if (pageText.length() >= MAX_CHARS_PER_PAGE) {
                listTag.add(StringTag.of(Text.Serializer.toJson(new LiteralText(pageText.toString()))));
                // Reset pageText for the next page
                pageText = new StringBuilder();
            }
        }
        // Add the remaining text to the last page
        if (pageText.length() > 0) {
            listTag.add(StringTag.of(Text.Serializer.toJson(new LiteralText(pageText.toString()))));
        }

        compoundTag.put("pages", listTag);
        compoundTag.putString("author", author);
        compoundTag.putString("title", title);

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
        CommandRegistry.INSTANCE.register(false, (RandomBookGiver::registerCommands));
        LOGGER.info("Random Book Giver Initialized");
    }
}

