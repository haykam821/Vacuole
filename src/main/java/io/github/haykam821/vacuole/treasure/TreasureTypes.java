package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;

import io.github.haykam821.vacuole.Main;
import net.minecraft.util.Identifier;

public final class TreasureTypes {
	public static final TreasureType<EmptyTreasure> EMPTY = register("empty", EmptyTreasure.CODEC);
	public static final TreasureType<DebugTreasure> DEBUG = register("debug", DebugTreasure.CODEC);

	private static <T extends Treasure> TreasureType<T> register(String path, Codec<T> codec) {
		Identifier id = new Identifier(Main.MOD_ID, path);
		TreasureType<T> type = new TreasureType<>(codec);

		TreasureType.REGISTRY.register(id, type);
		return type;
	}

	public static void initialize() {
		return;
	}
}
