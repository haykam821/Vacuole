package io.github.haykam821.vacuole.treasure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class TreasureCanvas {
	private static final Logger LOGGER = LogManager.getLogger("TreasureCanvas");
	private static final BlockState AIR = Blocks.AIR.getDefaultState();

	private final ServerWorld world;
	private final BlockBounds bounds;
	private final BlockPos center;
	private final BlockPos bottomCenter;

	public TreasureCanvas(ServerWorld world, BlockBounds bounds) {
		this.world = world;
		this.bounds = bounds;

		this.center = new BlockPos(bounds.getCenter());
		this.bottomCenter = new BlockPos(this.center.getX(), bounds.getMin().getY(), this.center.getZ());
	}

	public BlockPos getMin() {
		return this.bounds.getMin();
	}

	public BlockPos getMax() {
		return this.bounds.getMax();
	}

	public BlockPos getCenter() {
		return this.center;
	}

	public BlockPos getBottomCenter() {
		return this.bottomCenter;
	}

	public boolean setBlockState(BlockPos pos, BlockState state) {
		if (!this.bounds.contains(pos)) {
			LOGGER.warn("Attempted to modify block outside of canvas: {}", pos);
			return false;
		}

		return this.world.setBlockState(pos, state);
	}

	public BlockState getBlockState(BlockPos pos) {
		if (!this.bounds.contains(pos)) {
			return AIR;
		}
		return this.world.getBlockState(pos);
	}

	public void clear() {
		for (BlockPos pos : this.bounds) {
			this.world.setBlockState(pos, AIR);
		}
	}
}
