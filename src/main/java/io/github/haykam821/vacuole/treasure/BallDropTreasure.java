package io.github.haykam821.vacuole.treasure;

import java.util.Iterator;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StoneButtonBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import xyz.nucleoid.plasmid.util.ItemStackBuilder;

public class BallDropTreasure extends Treasure {
	private static final Random RANDOM = new Random();
	private static final DyeColor COLOR = DyeColor.ORANGE;

	private static final int SECONDS_PER_MINUTE = 60;
	private static final int MILLISECONDS_PER_SECOND = 1000;
	private static final long MILLISECONDS_PER_MINUTE = SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND;

	private static final BlockStateProvider DEFAULT_BALL_STATE_PROVIDER = BlockStateProvider.of(Blocks.SHROOMLIGHT);
	private static final BlockStateProvider DEFAULT_POLE_STATE_PROVIDER = BlockStateProvider.of(Blocks.IRON_BARS);
	private static final BlockStateProvider DEFAULT_POLE_BASE_STATE_PROVIDER = BlockStateProvider.of(Blocks.IRON_BLOCK);
	private static final BlockStateProvider DEFAULT_BUTTON_STATE_PROVIDER = BlockStateProvider.of(Blocks.STONE_BUTTON.getDefaultState().with(StoneButtonBlock.FACE, WallMountLocation.FLOOR));
	private static final BlockStateProvider DEFAULT_SIGN_STATE_PROVIDER = BlockStateProvider.of(Blocks.ACACIA_WALL_SIGN);
	private static final ItemStack DEFAULT_FIREWORK = ItemStackBuilder.firework(COLOR.getFireworkColor(), 2, FireworkRocketItem.Type.SMALL_BALL).build();
	private static final int DEFAULT_HEIGHT = 30;
	private static final int DEFAULT_DROP_TICKS = -1;

	public static final Codec<BallDropTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("ball_state_provider", DEFAULT_BALL_STATE_PROVIDER).forGetter(treasure -> treasure.ballStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("pole_state_provider", DEFAULT_POLE_STATE_PROVIDER).forGetter(treasure -> treasure.poleStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("pole_base_state_provider", DEFAULT_POLE_BASE_STATE_PROVIDER).forGetter(treasure -> treasure.poleBaseStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("button_state_provider", DEFAULT_BUTTON_STATE_PROVIDER).forGetter(treasure -> treasure.buttonStateProvider),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("sign_state_provider", DEFAULT_SIGN_STATE_PROVIDER).forGetter(treasure -> treasure.signStateProvider),
			ItemStack.CODEC.optionalFieldOf("firework", DEFAULT_FIREWORK).forGetter(treasure -> treasure.firework),
			Codec.INT.optionalFieldOf("height", DEFAULT_HEIGHT).forGetter(treasure -> treasure.height),
			Codec.INT.optionalFieldOf("drop_ticks", DEFAULT_DROP_TICKS).forGetter(treasure -> treasure.dropTicks)
		).apply(instance, BallDropTreasure::new);
	});

	private final BlockStateProvider ballStateProvider;
	private final BlockStateProvider poleStateProvider;
	private final BlockStateProvider poleBaseStateProvider;
	private final BlockStateProvider buttonStateProvider;
	private final BlockStateProvider signStateProvider;
	private final ItemStack firework;
	private final int height;
	private int dropTicks;

	public BallDropTreasure(BlockStateProvider ballStateProvider, BlockStateProvider poleStateProvider, BlockStateProvider baseStateProvider, BlockStateProvider buttonStateProvider, BlockStateProvider signStateProvider, ItemStack firework, int height, int dropTicks) {
		super(TreasureTypes.BALL_DROP);

		this.ballStateProvider = ballStateProvider;
		this.poleStateProvider = poleStateProvider;
		this.poleBaseStateProvider = baseStateProvider;
		this.buttonStateProvider = buttonStateProvider;
		this.signStateProvider = signStateProvider;
		this.firework = firework;
		this.height = height;
		this.dropTicks = dropTicks;
	}

	public BallDropTreasure() {
		this(DEFAULT_BALL_STATE_PROVIDER, DEFAULT_POLE_STATE_PROVIDER, DEFAULT_POLE_BASE_STATE_PROVIDER, DEFAULT_BUTTON_STATE_PROVIDER, DEFAULT_SIGN_STATE_PROVIDER, DEFAULT_FIREWORK, DEFAULT_HEIGHT, DEFAULT_DROP_TICKS);
	}

	private void buildPoleBase(BlockPos center) {
		int x = center.getX();
		int y = center.getY();
		int z = center.getZ();

		for (BlockPos pos : BlockPos.iterate(x - 1, y, z - 1, x + 1, y, z + 1)) {
			this.canvas.setBlockState(pos, this.poleBaseStateProvider.getBlockState(RANDOM, pos));
		}
	}

	private void updateSign(SignBlockEntity sign) {
		sign.markDirty();

		BlockState cachedState = sign.getCachedState();
		sign.getWorld().updateListeners(sign.getPos(), cachedState, cachedState, Block.NOTIFY_ALL);
	}

	private void buildSigns(BlockPos center, String line) {
		BlockPos.Mutable pos = new BlockPos.Mutable();

		Iterator<Direction> iterator = Direction.Type.HORIZONTAL.iterator();
		while (iterator.hasNext()) {
			Direction direction = iterator.next();

			pos.set(center);
			pos.move(direction);

			BlockState state = line == null ? Blocks.AIR.getDefaultState() : this.signStateProvider.getBlockState(RANDOM, pos)
				.with(WallSignBlock.FACING, direction);
			this.canvas.setBlockState(pos, state);

			if (line != null) {
				SignBlockEntity sign = this.canvas.getBlockEntity(pos, BlockEntityType.SIGN);
				if (sign != null) {
					sign.setGlowingText(true);
					sign.setTextColor(COLOR);

					sign.setTextOnRow(1, new LiteralText(line));

					this.updateSign(sign);
				}
			}
		}
	}

	private BlockState getPoleState(BlockPos pos, boolean ball) {
		if (ball) {
			return this.ballStateProvider.getBlockState(RANDOM, pos);
		} else {
			return this.poleStateProvider.getBlockState(RANDOM, pos);
		}
	}

	private void buildPole() {
		BlockPos.Mutable pos = this.canvas.getBottomCenter().mutableCopy();

		pos.move(Direction.UP);
		this.buildPoleBase(pos);

		for (int y = 0; y < this.height; y += 1) {
			pos.move(Direction.UP);

			int dropSeconds = this.dropTicks / SharedConstants.TICKS_PER_SECOND;
			int ballY = (int) (dropSeconds * (this.height / (double) SECONDS_PER_MINUTE));
			boolean ball = this.dropTicks >= 0 && y == ballY;

			this.buildSigns(pos, ball ? "" + dropSeconds : null);

			BlockState state = this.getPoleState(pos, ball);
			this.canvas.setBlockState(pos, state);
		}
	}

	private BlockPos getButtonPos() {
		return this.canvas.getBottomCenter().add(0, 1, 2);
	}

	private void buildButton() {
		BlockPos pos = this.getButtonPos();
		this.canvas.setBlockState(pos, this.buttonStateProvider.getBlockState(RANDOM, pos));
	}

	private void spawnFireworks() {
		if (this.firework.isEmpty()) {
			return;
		}

		BlockPos.Mutable pos = this.canvas.getBottomCenter().mutableCopy();
		pos.move(Direction.UP);

		int centerX = pos.getX();

		this.canvas.spawnFirework(this.firework, pos.setX(centerX - 5));
		this.canvas.spawnFirework(this.firework, pos.setX(centerX + 5));
	}

	@Override
	public void build() {
		super.build();
		this.buildPole();

		if (this.dropTicks == -1) {
			this.buildButton();
		}
	}

	@Override
	public void onUseBlock(ServerPlayerEntity player, BlockPos pos) {
		if (this.getButtonPos().equals(pos) && this.isModifiable(player)) {
			BlockState state = this.canvas.getBlockState(pos);
			this.canvas.setBlockState(pos, state.getFluidState().getBlockState());

			int secondsUntilNextMinute = (int) ((MILLISECONDS_PER_MINUTE - System.currentTimeMillis() % MILLISECONDS_PER_MINUTE) / MILLISECONDS_PER_SECOND);
			this.dropTicks = (secondsUntilNextMinute + SECONDS_PER_MINUTE + 1) * SharedConstants.TICKS_PER_SECOND;
		}
	}

	@Override
	public void tick() {
		if (this.dropTicks > 0) {
			this.dropTicks -= 1;
		} else if (this.dropTicks == 0) {
			this.dropTicks = 5;
			this.spawnFireworks();
		}

		if (this.dropTicks % 5 == 0) {
			this.buildPole();
		}
	}
}
