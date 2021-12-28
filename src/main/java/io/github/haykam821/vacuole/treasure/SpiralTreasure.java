package io.github.haykam821.vacuole.treasure;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public class SpiralTreasure extends Treasure {
	private static final Random RANDOM = new Random();

	private static final BlockStateProvider DEFAULT_PRIMARY_STATE_PROVIDER = new SimpleBlockStateProvider(Blocks.PURPUR_BLOCK.getDefaultState());
	private static final BlockStateProvider DEFAULT_SECONDARY_STATE_PROVIDER = new SimpleBlockStateProvider(Blocks.END_STONE_BRICKS.getDefaultState());

	public static final Codec<SpiralTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("primary_state_provider", DEFAULT_PRIMARY_STATE_PROVIDER).forGetter(treasure -> treasure.primaryStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("secondary_state_provider", DEFAULT_SECONDARY_STATE_PROVIDER).forGetter(treasure -> treasure.secondaryStateProvider)
		).apply(instance, SpiralTreasure::new);
	});

	private final BlockStateProvider primaryStateProvider;
	private final BlockStateProvider secondaryStateProvider;

	public SpiralTreasure(BlockStateProvider primaryStateProvider, BlockStateProvider secondaryStateProvider) {
		super(TreasureTypes.SPIRAL);

		this.primaryStateProvider = primaryStateProvider;
		this.secondaryStateProvider = secondaryStateProvider;
	}

	public SpiralTreasure() {
		this(DEFAULT_PRIMARY_STATE_PROVIDER, DEFAULT_SECONDARY_STATE_PROVIDER);
	}
	
	@Override
	protected BlockState getBase(BlockPos pos) {
		return this.secondaryStateProvider.getBlockState(RANDOM, pos);
	}

	@Override
	public void build() {
		super.build();

		BlockPos min = this.canvas.getMin();
		BlockPos max = this.canvas.getMax();

		BlockPos.Mutable pos = new BlockPos.Mutable(min.getX(), min.getY(), min.getZ() + 1);

		int sizeX = max.getX() - min.getX() - 1;
		int sizeZ = max.getZ() - min.getZ() - 2;

		Direction direction = Direction.EAST;

		while (sizeX > 0 || sizeZ > 0) {
			boolean parallel = direction.getAxis() == Direction.Axis.X;

			for (int index = 0; index < (parallel ? sizeX : sizeZ); index++) {
				BlockState state = this.primaryStateProvider.getBlockState(RANDOM, pos);
				this.canvas.setBlockState(pos, state);

				pos.move(direction);
			}

			if (parallel) {
				sizeX -= sizeZ == 1 ? 1 : 2;
			} else {
				sizeZ -= sizeX == 1 ? 1 : 2;
			}
			direction = direction.rotateYClockwise();
		}
	}
}
