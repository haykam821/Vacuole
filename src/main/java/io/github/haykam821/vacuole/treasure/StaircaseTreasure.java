package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

public class StaircaseTreasure extends Treasure {
	private static final Block DEFAULT_STAIR_BLOCK = Blocks.BRICK_STAIRS;

	public static final Codec<StaircaseTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Registry.BLOCK.getCodec().optionalFieldOf("stair_block", DEFAULT_STAIR_BLOCK).forGetter(treasure -> treasure.stairBlock),
			Codec.BOOL.optionalFieldOf("bottom_stairs", true).forGetter(treasure -> treasure.bottomStairs)
		).apply(instance, StaircaseTreasure::new);
	});

	private final Block stairBlock;
	private final boolean bottomStairs;

	public StaircaseTreasure(Block stairBlock, boolean bottomStairs) {
		super(TreasureTypes.STAIRCASE);

		this.stairBlock = stairBlock;
		this.bottomStairs = bottomStairs;
	}

	public StaircaseTreasure() {
		this(DEFAULT_STAIR_BLOCK, true);
	}
	
	@Override
	public void build() {
		super.build();

		BlockPos max = this.canvas.getMax();
		BlockPos min = this.canvas.getMin();
		BlockPos.Mutable pos = new BlockPos.Mutable(min.getX(), min.getY() + 1, max.getZ());

		Direction towards = Direction.NORTH;
		BlockState state = this.stairBlock.getDefaultState();

		while (pos.getY() < this.canvas.getMax().getY()) {
			this.canvas.setBlockState(pos, state);

			int touchingEdges = 0;
			if (pos.getX() == min.getX()) touchingEdges += 1;
			if (pos.getX() == max.getX()) touchingEdges += 1;
			if (pos.getZ() == min.getZ()) touchingEdges += 1;
			if (pos.getZ() == max.getZ()) touchingEdges += 1;
			
			if (pos.getY() - 1 != min.getY() && touchingEdges == 2) {
				towards = towards.rotateYClockwise();
				state = state.with(StairsBlock.FACING, towards);
			}

			pos.move(towards);
			if (this.bottomStairs) {
				BlockState bottomState = state
					.with(StairsBlock.HALF, BlockHalf.TOP)
					.with(StairsBlock.FACING, towards.getOpposite());

				this.canvas.setBlockState(pos, bottomState);
			}

			pos.move(Direction.UP);
		}
	}
}
