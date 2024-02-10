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
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
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
            "Dwarf and the Mountains",
            "Fey and the Stars",
            "Rogue and the Mountains",
            "Warrior of the Wilds",
            "Galaxy themselves",
            "Universe itself"
    };

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static final int LENGTH = RANDOM_CHARACTERS.length;
    private static final int MAX_PAGES = 16;
    private static final int MIN_CHARS_PER_PAGE = 64;
    private static final int MAX_CHARS_PER_PAGE = 256;
    private static final int COORDINATE_RANGE = 6900;

    private static final int SUCCESS = 1;
    private static final int FAILURE = 0;
    
    private static final ItemStack BOOK = new ItemStack(Items.WRITTEN_BOOK);
    private static final Random RANDOM_GENERATOR = new Random();

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
        RANDOM_GENERATOR.setSeed(chunkPos.x);
        Random random2 = new Random(chunkPos.z);
        RANDOM_GENERATOR.setSeed(((long) u << 8) + ((long) i << 4) + k);
        ItemStack itemStack = BOOK.copy();
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = new ListTag();

        for (int l = 0; l < MAX_PAGES; ++l) {
            StringBuilder stringBuilder = new StringBuilder();

            for (int m = 0; m < RANDOM.nextInt(MIN_CHARS_PER_PAGE, MAX_CHARS_PER_PAGE); ++m) {

                int n = RANDOM_GENERATOR.nextInt() + random2.nextInt() - RANDOM_GENERATOR.nextInt();

                if (RANDOM.nextBoolean()) {
                    stringBuilder.append(Formatting.OBFUSCATED);
                }

                stringBuilder.append(RANDOM_CHARACTERS[n % LENGTH]);

                if (RANDOM.nextInt(10) <= 3) {
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
        Vec3d pos = source.getPosition();

        PlayerEntity closestPlayer = null;
        if (players == null || players.isEmpty()) {
            closestPlayer = source.getWorld().getClosestPlayer((TargetPredicate) EntityPredicates.EXCEPT_SPECTATOR, pos.getX(), pos.getY(), pos.getZ());
        }
        String bookType = name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        if (closestPlayer != null) {
            for (int i = 0; i < amount; ++i) {
                giveBook(source.getDisplayName().getString(), name, bookType + " Book", closestPlayer);
            }
            return SUCCESS;
        } else if (players != null && !players.isEmpty()) {
            for (ServerPlayerEntity player : players) {
                for (int i = 0; i < amount; ++i) {
                    giveBook(source.getDisplayName().getString(), name, bookType + " Book", player);
                }
            }
            return SUCCESS;
        }
        return FAILURE;
    }

    public static void giveBook(String author, @NotNull String message, String title, PlayerEntity player) {

        ItemStack itemStack = BOOK.copy();
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        ListTag listTag = new ListTag();

        listTag.add(StringTag.of(Text.Serializer.toJson(new LiteralText(message))));

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
        LOGGER.info("Random Book Giver Initialized");

        CommandRegistry.INSTANCE.register(false, (RandomBookGiver::registerCommands));
    }
}

