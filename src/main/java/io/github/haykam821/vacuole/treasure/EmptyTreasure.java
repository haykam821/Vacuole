package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;

import net.minecraft.item.Items;

public class EmptyTreasure extends Treasure {
	public static final Codec<EmptyTreasure> CODEC = Codec.unit(EmptyTreasure::new);

	public EmptyTreasure() {
		super(TreasureTypes.EMPTY, Items.BARRIER);
	}
}
