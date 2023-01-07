package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class GameOfLifeTreasure extends CellularAutomatonTreasure {
	private static final byte DEAD = 0x00;
	private static final byte LIVE = 0x01;

	private static final BlockState[] BLOCK_STATES = {
		Blocks.WHITE_CONCRETE.getDefaultState(),
		Blocks.BLACK_CONCRETE.getDefaultState()
	};

	public static final Codec<GameOfLifeTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.optionalFieldOf("max_ticks_until_step", DEFAULT_MAX_TICKS_UNTIL_STEP).forGetter(treasure -> treasure.maxTicksUntilStep)
		).apply(instance, GameOfLifeTreasure::new);
	});

	public GameOfLifeTreasure(int maxTicksUntilStep) {
		super(TreasureTypes.GAME_OF_LIFE, maxTicksUntilStep);
	}

	public GameOfLifeTreasure() {
		this(DEFAULT_MAX_TICKS_UNTIL_STEP);
	}

	@Override
	protected byte update(int x, int z) {
		byte state = this.get(x, z);

		int liveNeighbors = 0;

		for (int offsetX = -1; offsetX <= 1; offsetX++) {
			for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
				// Ignore own state
				if (offsetX == 0 && offsetZ == 0) continue;

				byte neighborState = this.get(x + offsetX, z + offsetZ);

				if (neighborState == LIVE) {
					liveNeighbors += 1;

					if (liveNeighbors > 3) {
						return DEAD;
					}
				}
			}
		}

		if ((state == LIVE && liveNeighbors == 2) || liveNeighbors == 3) {
			return LIVE;
		} else {
			return DEAD;
		}
	}

	@Override
	public BlockState[] getBlockStates() {
		return BLOCK_STATES;
	}

	@Override
	public void onUseBlock(ServerPlayerEntity player, BlockPos pos) {
		BlockPos min = this.canvas.getMin();

		int x = pos.getX() - min.getX();
		int z = pos.getZ() - min.getZ();

		byte state = this.get(x, z);

		this.set(x, z, state == LIVE ? DEAD : LIVE);
	}

	@Override
	public void setCanvas(TreasureCanvas canvas) {
		super.setCanvas(canvas);

		this.set(1, 0, LIVE);

		this.set(2, 1, LIVE);

		this.set(0, 2, LIVE);
		this.set(1, 2, LIVE);
		this.set(2, 2, LIVE);
	}
}
