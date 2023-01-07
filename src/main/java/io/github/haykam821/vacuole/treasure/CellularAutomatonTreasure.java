package io.github.haykam821.vacuole.treasure;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public abstract class CellularAutomatonTreasure extends Treasure {
	protected static final int DEFAULT_MAX_TICKS_UNTIL_STEP = 2;

	private BlockPos size;
	private byte[][] grid;

	protected final int maxTicksUntilStep;
	private int ticksUntilStep;

	public CellularAutomatonTreasure(TreasureType<? extends CellularAutomatonTreasure> type, int maxTicksUntilStep) {
		super(type);

		this.maxTicksUntilStep = 2;
	}

	protected byte get(int x, int z) {
		if (x < 0 || x > this.size.getX()) return 0;
		if (z < 0 || z > this.size.getZ()) return 0;

		return this.grid[x][z];
	}

	protected void set(int x, int z, byte state) {
		if (x < 0 || x > this.size.getX()) return;
		if (z < 0 || z > this.size.getZ()) return;

		this.grid[x][z] = state;

		BlockPos pos = this.canvas.getMin().add(x, 0, z);
		BlockState blockState = this.getBlockStates()[state];

		this.canvas.setBlockState(pos, blockState);
	}

	protected void fillExcept(byte except, byte state) {
		for (int x = 0; x <= this.size.getX(); x++) {
			for (int z = 0; z <= this.size.getZ(); z++) {
				if (this.grid[x][z] != except) {
					this.grid[x][z] = state;
				}
			}
		}
	}

	protected abstract byte update(int x, int z);

	protected abstract BlockState[] getBlockStates();

	protected BlockPos getSize() {
		return this.size;
	}

	@Override
	public void setCanvas(TreasureCanvas canvas) {
		super.setCanvas(canvas);

		this.size = canvas.getSize();
		this.grid = new byte[this.size.getX() + 1][this.size.getZ() + 1];
	}

	@Override
	public void tick() {
		if (this.ticksUntilStep > 0) {
			this.ticksUntilStep -= 1;
			return;
		}

		// Pause if all entities are sneaking
		List<Entity> entities = this.canvas.getEntities();

		if (!entities.isEmpty()) {
			boolean sneaking = true;

			for (Entity entity : this.canvas.getEntities()) {
				if (!entity.isSneaking()) {
					sneaking = false;
				}
			}

			if (sneaking) {
				return;
			}
		}

		byte[][] nextGrid = new byte[this.size.getX() + 1][this.size.getZ() + 1];

		BlockPos min = this.canvas.getMin();
		BlockPos.Mutable pos = new BlockPos.Mutable();

		// Iterate once to get new states
		for (int x = 0; x <= this.size.getX(); x++) {
			for (int z = 0; z <= this.size.getZ(); z++) {
				nextGrid[x][z] = this.update(x, z);
			}
		}

		// Iterate again to apply new states
		for (int x = 0; x <= this.size.getX(); x++) {
			for (int z = 0; z <= this.size.getZ(); z++) {
				pos.set(min, x, 0, z);

				byte state = this.get(x, z);
				BlockState blockState = this.getBlockStates()[state];

				this.canvas.setBlockState(pos, blockState);
			}
		}

		this.grid = nextGrid;
		this.ticksUntilStep = this.maxTicksUntilStep;
	}
}
