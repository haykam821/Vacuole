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

public class BinaryTimerTreasure extends Treasure {
	private static final Random RANDOM = new Random();

	private static final BlockStateProvider DEFAULT_ON_STATE_PROVIDER = new SimpleBlockStateProvider(Blocks.BLACK_CONCRETE.getDefaultState());
	private static final BlockStateProvider DEFAULT_OFF_STATE_PROVIDER = new SimpleBlockStateProvider(Blocks.WHITE_CONCRETE.getDefaultState());

	public static final Codec<BinaryTimerTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("on_state_provider", DEFAULT_ON_STATE_PROVIDER).forGetter(treasure -> treasure.onStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("off_state_provider", DEFAULT_OFF_STATE_PROVIDER).forGetter(treasure -> treasure.offStateProvider),
			Codec.LONG.optionalFieldOf("time", 0l).forGetter(treasure -> treasure.time)
		).apply(instance, BinaryTimerTreasure::new);
	});

	private final BlockStateProvider onStateProvider;
	private final BlockStateProvider offStateProvider;
	private long time;

	public BinaryTimerTreasure(BlockStateProvider onStateProvider, BlockStateProvider offStateProvider, long time) {
		super(TreasureTypes.BINARY_TIMER);

		this.onStateProvider = onStateProvider;
		this.offStateProvider = offStateProvider;
		this.time = time;
	}

	public BinaryTimerTreasure() {
		this(DEFAULT_ON_STATE_PROVIDER, DEFAULT_OFF_STATE_PROVIDER, 0);
	}

	@Override
	public void tick() {
		this.time += 1;

		if (this.time % 20 == 0) {
			BlockPos.Mutable pos = this.canvas.getMin().mutableCopy();
			long seconds = this.time / 20;

			for (int bit = 0; bit < 64; bit++) {
				boolean on = ((seconds >> bit) & 1) == 1;

				BlockState state = this.getBlockState(on, pos);
				this.canvas.setBlockState(pos, state);

				pos.move(Direction.EAST);
				if (pos.getX() > this.canvas.getMax().getX()) {
					pos.setX(this.canvas.getMin().getX());
					pos.setZ(pos.getZ() + 1);
				}
			}
		}
	}

	private BlockState getBlockState(boolean on, BlockPos pos) {
		if (on) {
			return this.onStateProvider.getBlockState(RANDOM, pos);
		} else {
			return this.offStateProvider.getBlockState(RANDOM, pos);
		}
	}
}
