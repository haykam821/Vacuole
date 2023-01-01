package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class WireworldTreasure extends CellularAutomatonTreasure {
	private static final byte EMPTY = 0x00;
	private static final byte ELECTRON_HEAD = 0x01;
	private static final byte ELECTRON_TAIL = 0x02;
	private static final byte CONDUCTOR = 0x03;

	private static final BlockState[] BLOCK_STATES = {
		Blocks.BLACK_CONCRETE.getDefaultState(),
		Blocks.BLUE_CONCRETE.getDefaultState(),
		Blocks.RED_CONCRETE.getDefaultState(),
		Blocks.YELLOW_CONCRETE.getDefaultState()
	};

	public static final Codec<WireworldTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.optionalFieldOf("max_ticks_until_step", DEFAULT_MAX_TICKS_UNTIL_STEP).forGetter(treasure -> treasure.maxTicksUntilStep)
		).apply(instance, WireworldTreasure::new);
	});

	public WireworldTreasure(int maxTicksUntilStep) {
		super(TreasureTypes.WIREWORLD, maxTicksUntilStep);
	}

	public WireworldTreasure() {
		this(DEFAULT_MAX_TICKS_UNTIL_STEP);
	}

	@Override
	protected byte update(int x, int z) {
		byte state = this.get(x, z);

		if (state == ELECTRON_HEAD) {
			return ELECTRON_TAIL;
		} else if (state == ELECTRON_TAIL) {
			return CONDUCTOR;
		} else if (state == CONDUCTOR) {
			int neighbors = 0;

			for (int offsetX = -1; offsetX <= 1; offsetX++) {
				for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
					// Ignore own state
					if (offsetX == 0 && offsetZ == 0) continue;

					byte neighborState = this.get(x + offsetX, z + offsetZ);
					if (neighborState == ELECTRON_HEAD) {
						neighbors++;

						if (neighbors > 2) {
							return state;
						}
					}
				}
			}

			if (neighbors == 1 || neighbors == 2) {
				return ELECTRON_HEAD;
			}
		}

		return state;
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

		if (state == EMPTY) {
			if (player.isSneaking()) {
				this.fillExcept(EMPTY, CONDUCTOR);
			} else {
				this.set(x, z, CONDUCTOR);
			}
		} else {
			this.set(x, z, player.isSneaking() ? ELECTRON_HEAD : EMPTY);
		}
	}

	@Override
	public void setCanvas(TreasureCanvas canvas) {
		super.setCanvas(canvas);

		BlockPos size = this.getSize();

		int centerX = size.getX() / 2;
		int z = size.getZ() / 2;

		for (int x = 0; x <= size.getX(); x++) {
			if (x == centerX || x == centerX + 1) {
				this.set(x, z - 1, CONDUCTOR);
				this.set(x, z + 1, CONDUCTOR);

				if (x == centerX) {
					continue;
				}
			}

			this.set(x, z, CONDUCTOR);
		}
	}
}
