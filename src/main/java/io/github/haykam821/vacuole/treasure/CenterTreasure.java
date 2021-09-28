package io.github.haykam821.vacuole.treasure;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;

public class CenterTreasure extends Treasure {
	private static final Random RANDOM = new Random();

	private static final BlockStateProvider DEFAULT_CENTER_STATE_PROVIDER = new SimpleBlockStateProvider(Blocks.DIAMOND_BLOCK.getDefaultState());
	private static final BlockStateProvider DEFAULT_X_AXIS_STATE_PROVIDER = new SimpleBlockStateProvider(Blocks.RED_STAINED_GLASS.getDefaultState());
	private static final BlockStateProvider DEFAULT_Y_AXIS_STATE_PROVIDER = new SimpleBlockStateProvider(Blocks.LIME_STAINED_GLASS.getDefaultState());
	private static final BlockStateProvider DEFAULT_Z_AXIS_STATE_PROVIDER = new SimpleBlockStateProvider(Blocks.BLUE_STAINED_GLASS.getDefaultState());

	public static final Codec<CenterTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("center_state_provider", DEFAULT_CENTER_STATE_PROVIDER).forGetter(treasure -> treasure.centerStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("x_axis_state_provider", DEFAULT_X_AXIS_STATE_PROVIDER).forGetter(treasure -> treasure.xAxisStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("y_axis_state_provider", DEFAULT_Y_AXIS_STATE_PROVIDER).forGetter(treasure -> treasure.yAxisStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("z_axis_state_provider", DEFAULT_Z_AXIS_STATE_PROVIDER).forGetter(treasure -> treasure.zAxisStateProvider),
			Codec.INT.optionalFieldOf("axis_length", 4).forGetter(treasure -> treasure.axisLength)
		).apply(instance, CenterTreasure::new);
	});

	private final BlockStateProvider centerStateProvider;
	private final BlockStateProvider xAxisStateProvider;
	private final BlockStateProvider yAxisStateProvider;
	private final BlockStateProvider zAxisStateProvider;
	private final int axisLength;

	public CenterTreasure(BlockStateProvider centerStateProvider, BlockStateProvider xAxisStateProvider, BlockStateProvider yAxisStateProvider, BlockStateProvider zAxisStateProvider, int axisLength) {
		super(TreasureTypes.CENTER);

		this.centerStateProvider = centerStateProvider;
		this.xAxisStateProvider = xAxisStateProvider;
		this.yAxisStateProvider = yAxisStateProvider;
		this.zAxisStateProvider = zAxisStateProvider;
		this.axisLength = axisLength;
	}

	public CenterTreasure() {
		this(DEFAULT_CENTER_STATE_PROVIDER, DEFAULT_X_AXIS_STATE_PROVIDER, DEFAULT_Y_AXIS_STATE_PROVIDER, DEFAULT_Z_AXIS_STATE_PROVIDER, 4);
	}

	@Override
	public void build() {
		super.build();

		BlockPos.Mutable pos = this.canvas.getCenter().mutableCopy();
		this.canvas.setBlockState(pos, this.centerStateProvider.getBlockState(RANDOM, pos));

		if (this.axisLength != 0) {
			this.buildAxis(pos, this.xAxisStateProvider, Direction.Axis.X);
			this.buildAxis(pos, this.yAxisStateProvider, Direction.Axis.Y);
			this.buildAxis(pos, this.zAxisStateProvider, Direction.Axis.Z);
		}
	}

	public void buildAxis(BlockPos.Mutable pos, BlockStateProvider stateProvider, Direction.Axis axis) {
		pos.set(this.canvas.getCenter());

		Direction.AxisDirection axisDirection = this.axisLength > 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
		Direction direction = Direction.get(axisDirection, axis);

		for (int offset = 1; offset <= Math.abs(this.axisLength); offset++) {
			pos.move(direction);
			this.canvas.setBlockState(pos, stateProvider.getBlockState(RANDOM, pos));
		}
	}
}
