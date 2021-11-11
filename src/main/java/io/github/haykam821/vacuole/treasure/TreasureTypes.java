package io.github.haykam821.vacuole.treasure;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import io.github.haykam821.vacuole.Main;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public final class TreasureTypes {
	public static final TreasureType<EmptyTreasure> EMPTY = register("empty", EmptyTreasure::new, Items.BARRIER);
	public static final TreasureType<DebugTreasure> DEBUG = register("debug", DebugTreasure::new, Items.DEBUG_STICK);
	public static final TreasureType<DownpourShelterTreasure> DOWNPOUR_SHELTER = register("downpour_shelter", DownpourShelterTreasure.CODEC, DownpourShelterTreasure::new, Items.RED_STAINED_GLASS);
	public static final TreasureType<MinefieldTreasure> MINEFIELD = register("minefield", MinefieldTreasure.CODEC, MinefieldTreasure::new, Items.STONE_PRESSURE_PLATE);
	public static final TreasureType<SnakeTreasure> SNAKE = register("snake", SnakeTreasure.CODEC, SnakeTreasure::new, Items.LIME_TERRACOTTA);
	public static final TreasureType<PyramidTreasure> PYRAMID = register("pyramid", PyramidTreasure.CODEC, PyramidTreasure::new, Items.SANDSTONE_STAIRS);
	public static final TreasureType<CheckerboardTreasure> CHECKERBOARD = register("checkerboard", CheckerboardTreasure.CODEC, CheckerboardTreasure::new, Items.WAXED_OXIDIZED_CUT_COPPER);
	public static final TreasureType<StaircaseTreasure> STAIRCASE = register("staircase", StaircaseTreasure.CODEC, StaircaseTreasure::new, Items.BRICK_STAIRS);
	public static final TreasureType<ConveyorTreasure> CONVEYOR = register("conveyor", ConveyorTreasure.CODEC, ConveyorTreasure::new, Items.MAGENTA_GLAZED_TERRACOTTA);
	public static final TreasureType<CenterTreasure> CENTER = register("center", CenterTreasure.CODEC, CenterTreasure::new, Items.END_ROD);
	public static final TreasureType<SierpinskiCarpetTreasure> SIERPINSKI_CARPET = register("sierpinski_carpet", SierpinskiCarpetTreasure.CODEC, SierpinskiCarpetTreasure::new, Items.MOSS_CARPET);
	public static final TreasureType<BinaryTimerTreasure> BINARY_TIMER = register("binary_timer", BinaryTimerTreasure.CODEC, BinaryTimerTreasure::new, Items.CLOCK);

	private static <T extends Treasure> TreasureType<T> register(String path, Codec<T> codec, Supplier<T> creator, Item icon) {
		Identifier id = new Identifier(Main.MOD_ID, path);
		TreasureType<T> type = new TreasureType<>(codec, creator, icon);

		TreasureType.REGISTRY.register(id, type);
		return type;
	}

	private static <T extends Treasure> TreasureType<T> register(String path, Supplier<T> creator, Item icon) {
		return register(path, Codec.unit(creator), creator, icon);
	}

	public static void initialize() {
		return;
	}
}
