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
