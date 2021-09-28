package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class DownpourShelterTreasure extends Treasure {
	public static final Codec<DownpourShelterTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.BOOL.optionalFieldOf("locked", false).forGetter(treasure -> treasure.locked),
			Codec.INT.optionalFieldOf("size", 2).forGetter(treasure -> treasure.size)
		).apply(instance, DownpourShelterTreasure::new);
	});

	private static final BlockState FLOOR = Blocks.RED_TERRACOTTA.getDefaultState();
	private static final BlockState ROOF = Blocks.SPRUCE_SLAB.getDefaultState();
	private static final BlockState GLASS = Blocks.RED_STAINED_GLASS.getDefaultState();

	private final boolean locked;
	private final int size;

	public DownpourShelterTreasure(boolean locked, int size) {
		super(TreasureTypes.DOWNPOUR_SHELTER);

		this.locked = locked;
		this.size = size;
	}

	public DownpourShelterTreasure() {
		this(false, 2);
	}

	@Override
	public void build() {
		super.build();

		int upperRadius = (int) Math.ceil(size / (float) 2);
		int lowerRadius = (int) Math.floor(size / (float) 2);

		BlockBox box = BlockBox.create(this.canvas.getBottomCenter().add(-lowerRadius, 1, -lowerRadius), this.canvas.getBottomCenter().add(upperRadius, 7, upperRadius));
		BlockBox outerBox = new BlockBox(box.getMinX() - 1, box.getMinY(), box.getMinZ() - 1, box.getMaxX() + 1, box.getMaxY(), box.getMaxZ() + 1);

		for (BlockPos pos : BlockPos.iterate(outerBox.getMinX(), outerBox.getMinY(), outerBox.getMinZ(), outerBox.getMaxX(), outerBox.getMaxY(), outerBox.getMaxZ())) {
			if (this.locked && !box.contains(pos)) {
				this.canvas.setBlockState(pos, GLASS);
			} else if (pos.getY() == box.getMinY()) {
				this.canvas.setBlockState(pos, FLOOR);
			} else if (pos.getY() == box.getMaxY()) {
				this.canvas.setBlockState(pos, ROOF);
			}
		}
	}

	@Override
	public boolean isKnockbackEnabled() {
		return true;
	}
}
