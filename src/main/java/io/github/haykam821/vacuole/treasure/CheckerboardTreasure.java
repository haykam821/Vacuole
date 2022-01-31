package io.github.haykam821.vacuole.treasure;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class CheckerboardTreasure extends Treasure {
	private static final Random RANDOM = new Random();

	private static final BlockStateProvider DEFAULT_PRIMARY_STATE_PROVIDER = BlockStateProvider.of(Blocks.BLACK_CONCRETE);
	private static final BlockStateProvider DEFAULT_SECONDARY_STATE_PROVIDER = BlockStateProvider.of(Blocks.WHITE_CONCRETE);

	public static final Codec<CheckerboardTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("primary_state_provider", DEFAULT_PRIMARY_STATE_PROVIDER).forGetter(treasure -> treasure.primaryStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("secondary_state_provider", DEFAULT_SECONDARY_STATE_PROVIDER).forGetter(treasure -> treasure.secondaryStateProvider),
			Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("scale", 2).forGetter(treasure -> treasure.scale)
		).apply(instance, CheckerboardTreasure::new);
	});

	private final BlockStateProvider primaryStateProvider;
	private final BlockStateProvider secondaryStateProvider;
	private final int scale;

	public CheckerboardTreasure(BlockStateProvider primaryStateProvider, BlockStateProvider secondaryStateProvider, int scale) {
		super(TreasureTypes.CHECKERBOARD);

		this.primaryStateProvider = primaryStateProvider;
		this.secondaryStateProvider = secondaryStateProvider;
		this.scale = scale;
	}

	public CheckerboardTreasure() {
		this(DEFAULT_PRIMARY_STATE_PROVIDER, DEFAULT_SECONDARY_STATE_PROVIDER, 2);
	}
	
	@Override
	protected BlockState getBase(BlockPos pos) {
		BlockPos min = this.canvas.getMin();

		int x = (pos.getX() - min.getX()) / this.scale;
		int z = (pos.getZ() - min.getZ()) / this.scale;

		if ((x + z) % 2 == 0) {
			return this.primaryStateProvider.getBlockState(RANDOM, pos);
		} else {
			return this.secondaryStateProvider.getBlockState(RANDOM, pos);
		}
	}
}
