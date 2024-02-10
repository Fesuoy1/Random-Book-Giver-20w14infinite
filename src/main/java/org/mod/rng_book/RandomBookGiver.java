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
import net.minecraft.util.ActionResult;
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
	private static final char[] RANDOM_CHARACTERS = new char[]{' ', ',', '.', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i',
			'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E',
			'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
	private static final int LENGTH = RANDOM_CHARACTERS.length;
	private static final int MAX_PAGES = 16;
	private static final int MIN_CHARS_PER_PAGE = 64;
	private static final int MAX_CHARS_PER_PAGE = 256;
	private static final int COORDINATE_RANGE = 6900;

	@Override
	public void onInitialize() {
		LOGGER.info("Random Book Giver Initialized");
	}

	public static ActionResult giveRandomBook(World world, PlayerEntity player) {
		Direction direction = Direction.random(world.getRandom());
		int x = RANDOM.nextInt(COORDINATE_RANGE) + 1;
		int y = RANDOM.nextInt(COORDINATE_RANGE) + 1;
		int z = RANDOM.nextInt(COORDINATE_RANGE) + 1;
        BlockPos pos = new BlockPos(x, y, z);
		int i = pos.getY();
		int j;
		byte k;
		switch (direction) {
			case NORTH:
				j = 15 - pos.getX() & 15;
				k = 0;
				break;
			case SOUTH:
				j = pos.getX() & 15;
				k = 2;
				break;
			case EAST:
				j = 15 - pos.getZ() & 15;
				k = 1;
				break;
			case WEST:
			default:
				j = pos.getZ() & 15;
				k = 3;
		}

		if (j == 0 || j == 15) {
			return ActionResult.FAIL;
		}
			ChunkPos chunkPos = new ChunkPos(pos);
			String string = chunkPos.x + "/" + chunkPos.z + "/" + k + "/" + j + "/" + i;
			Random random = new Random(chunkPos.x);
			Random random2 = new Random(chunkPos.z);
			Random random3 = new Random((j << 8) + ((long) i << 4) + k);
			ItemStack itemStack = new ItemStack(Items.WRITTEN_BOOK);
			CompoundTag compoundTag = itemStack.getOrCreateTag();
			ListTag listTag = new ListTag();

			for(int l = 0; l < MAX_PAGES; ++l) {
				StringBuilder stringBuilder = new StringBuilder();

				for(int m = 0; m < RANDOM.nextInt(MIN_CHARS_PER_PAGE, MAX_CHARS_PER_PAGE); ++m) {
					int n = random.nextInt() + random2.nextInt() - random3.nextInt();
					stringBuilder.append(RANDOM_CHARACTERS[Math.floorMod(n, LENGTH)]);
				}

				listTag.add(StringTag.of(Text.Serializer.toJson(new LiteralText(stringBuilder.toString()))));
			}

			String author;

			// Pre=set names so it feels random
			switch (RANDOM.nextInt(6)) { 
				case 0:
					author = "Dwarf and the Stars";
					break;
				case 1:
					author = "Wizard and the North";
					break;
				case 2:
					author = "Warrior of a Land";
					break;
				case 3:
					author = "Crafter and the Mines";
					break;
				case 4:
					author = "Druid of the Sky";
					break;
				case 5:
					author = "Mage of the Wilds";
					break;
				default:
					author = "Universe itself";
			}

			compoundTag.put("pages", listTag);
			compoundTag.putString("author", Formatting.OBFUSCATED + author);
			compoundTag.putString("title", string);
			PlayerInventory inventory = player.inventory;
			int slot = inventory.getEmptySlot();
			if (slot == -1) {
				player.dropItem(itemStack, false);
				return ActionResult.SUCCESS;
			}
			inventory.insertStack(slot, itemStack);
			return ActionResult.SUCCESS;
	}
}