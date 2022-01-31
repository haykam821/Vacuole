package io.github.haykam821.vacuole.treasure;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class PyramidTreasure extends Treasure {
	private static final BlockStateProvider DEFAULT_STATE_PROVIDER = BlockStateProvider.of(Blocks.SANDSTONE);

	public static final Codec<PyramidTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.optionalFieldOf("step", 1).forGetter(treasure -> treasure.step),
			Codec.INT.optionalFieldOf("padding", 1).forGetter(treasure -> treasure.padding),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("state_provider", DEFAULT_STATE_PROVIDER).forGetter(treasure -> treasure.stateProvider)
		).apply(instance, PyramidTreasure::new);
	});

	private static final Random RANDOM = new Random();

	private final int step;
	private final int padding;
	private final BlockStateProvider stateProvider;

	public PyramidTreasure(int step, int padding, BlockStateProvider stateProvider) {
		super(TreasureTypes.PYRAMID);

		this.step = step;
		this.padding = padding;
		this.stateProvider = stateProvider;
	}

	public PyramidTreasure() {
		this(1, 1, DEFAULT_STATE_PROVIDER);
	}

	@Override
	public void build() {
		super.build();

		int minY = this.canvas.getMin().getY();
		int maxY = this.canvas.getMax().getY();

		int minX = this.canvas.getMin().getX();
		int maxX = this.canvas.getMax().getX();

		int minZ = this.canvas.getMin().getZ();
		int maxZ = this.canvas.getMax().getZ();

		BlockPos.Mutable pos = this.canvas.getMin().mutableCopy().move(Direction.UP);
		for (int y = minY + 1; y <= maxY; y++) {
			int relativeY = y - minY - 1 + this.padding;
			relativeY /= this.step;

			int minPyramidX = minX + relativeY;
			int maxPyramidX = maxX - relativeY;

			for (int x = minX; x <= maxX; x++) {
				if (x >= minPyramidX && x <= maxPyramidX) {
					for (int z = minZ; z <= maxZ; z++) {
						int minPyramidZ = minZ + relativeY;
						int maxPyramidZ = maxZ - relativeY;

						if (z >= minPyramidZ && z <= maxPyramidZ) {
							pos.set(x, y, z);
							this.canvas.setBlockState(pos, this.stateProvider.getBlockState(RANDOM, pos));
						}
					}
				}
			}
		}
	}
}
