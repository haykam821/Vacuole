package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class BounceTreasure extends Treasure {
	private static final Random RANDOM = Random.create();

	private static final BlockStateProvider DEFAULT_BALL_STATE_PROVIDER = BlockStateProvider.of(Blocks.SLIME_BLOCK);
	private static final BlockStateProvider DEFAULT_TRAIL_STATE_PROVIDER = BlockStateProvider.of(Blocks.LIME_CARPET);

	public static final Codec<BounceTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("ball_state_provider", DEFAULT_BALL_STATE_PROVIDER).forGetter(treasure -> treasure.ballStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("trail_state_provider", DEFAULT_TRAIL_STATE_PROVIDER).forGetter(treasure -> treasure.trailStateProvider),
			Codec.INT.optionalFieldOf("max_ticks_until_move", 3).forGetter(treasure -> treasure.maxTicksUntilMove)
		).apply(instance, BounceTreasure::new);
	});

	private final BlockStateProvider ballStateProvider;
	private final BlockStateProvider trailStateProvider;
	private final int maxTicksUntilMove;

	private BlockPos pos;
	private boolean up = RANDOM.nextBoolean();
	private boolean right = RANDOM.nextBoolean();
	private int ticksUntilMove = 0;

	public BounceTreasure(BlockStateProvider ballStateProvider, BlockStateProvider trailStateProvider, int maxTicksUntilMove) {
		super(TreasureTypes.BOUNCE);

		this.ballStateProvider = ballStateProvider;
		this.trailStateProvider = trailStateProvider;
		this.maxTicksUntilMove = maxTicksUntilMove;
	}

	public BounceTreasure() {
		this(DEFAULT_BALL_STATE_PROVIDER, DEFAULT_TRAIL_STATE_PROVIDER, 3);
	}

	private boolean isOnEdgeX() {
		BlockPos min = this.canvas.getMin();
		BlockPos max = this.canvas.getMax();

		int x = this.pos.getX();
		return x == min.getX() || x == max.getX();
	}

	private boolean isOnEdgeZ() {
		BlockPos min = this.canvas.getMin();
		BlockPos max = this.canvas.getMax();

		int z = this.pos.getZ();
		return z == min.getZ() || z == max.getZ();
	}

	@Override
	public void tick() {
		if (this.ticksUntilMove > 0) {
			this.ticksUntilMove -= 1;
			return;
		}

		if (this.pos == null) {
			BlockPos min = this.canvas.getMin();
			BlockPos max = this.canvas.getMax();

			int x = MathHelper.nextBetween(RANDOM, min.getX() + 1, max.getX() - 1);
			int z = MathHelper.nextBetween(RANDOM, min.getZ() + 1, max.getZ() - 1);
			this.pos = new BlockPos(x, min.getY() + 1, z);
		} else {
			this.canvas.setBlockState(this.pos, this.trailStateProvider.get(RANDOM, this.pos));
		}

		if (this.isOnEdgeX()) {
			this.up = !this.up;
		}
		if (this.isOnEdgeZ()) {
			this.right = !this.right;
		}

		this.pos = this.pos.add(up ? 1 : -1, 0, right ? 1 : -1);
		this.canvas.setBlockState(this.pos, this.ballStateProvider.get(RANDOM, this.pos));

		this.ticksUntilMove = this.maxTicksUntilMove;
	}
}
