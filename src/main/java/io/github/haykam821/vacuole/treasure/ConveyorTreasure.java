package io.github.haykam821.vacuole.treasure;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ConveyorTreasure extends Treasure {
	private static final BlockState DEFAULT_STILL_STATE = Blocks.MAGENTA_CONCRETE.getDefaultState();
	private static final BiMap<BlockState, Direction> CONVEYOR_STATES = HashBiMap.create(4);

	public static final Codec<ConveyorTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			BlockState.CODEC.optionalFieldOf("still_state", DEFAULT_STILL_STATE).forGetter(treasure -> treasure.stillState),
			Codec.DOUBLE.optionalFieldOf("movement_scale", 0.4d).forGetter(treasure -> treasure.movementScale),
			Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("movement_interval", 2).forGetter(treasure -> treasure.movementInterval)
		).apply(instance, ConveyorTreasure::new);
	});

	private final BlockState stillState;
	private final double movementScale;
	private final int movementInterval;

	private int ticks = 0;

	public ConveyorTreasure(BlockState stillState, double movementScale, int movementInterval) {
		super(TreasureTypes.CONVEYOR);

		this.stillState = stillState;
		this.movementScale = movementScale;
		this.movementInterval = movementInterval;
	}

	public ConveyorTreasure() {
		this(DEFAULT_STILL_STATE, 0.4, 2);
	}

	@Override
	protected BlockState getBase(BlockPos pos) {
		if (pos.getX() == this.canvas.getMin().getX() && pos.getZ() != this.canvas.getMax().getZ()) {
			return CONVEYOR_STATES.inverse().get(Direction.SOUTH);
		} else if (pos.getZ() == this.canvas.getMin().getZ()) {
			return CONVEYOR_STATES.inverse().get(Direction.WEST);
		} else if (pos.getX() == this.canvas.getMax().getX()) {
			return CONVEYOR_STATES.inverse().get(Direction.NORTH);
		} else if (pos.getZ() == this.canvas.getMax().getZ()) {
			return CONVEYOR_STATES.inverse().get(Direction.EAST);
		}

		return this.stillState;
	}

	private boolean canBeChanged(BlockState state) {
		return state == this.stillState || CONVEYOR_STATES.containsKey(state);
	}

	private Direction getDirection(BlockState state) {
		return CONVEYOR_STATES.get(state);
	}

	@Override
	public void tick() {
		this.ticks += 1;
		if (this.ticks % this.movementInterval != 0) return;

		for (Entity entity : this.canvas.getEntities()) {
			if (entity.isSneaky()) continue;
			if (entity.getY() % 1 > 0.01) continue;

			BlockPos pos = entity.getBlockPos().down();
			BlockState state = this.canvas.getBlockState(pos);

			Direction direction = this.getDirection(state);
			if (direction == null) continue;

			double x = entity.getX() + (direction.getOffsetX() * this.movementScale);
			double y = entity.getY() + (direction.getOffsetY() * this.movementScale);
			double z = entity.getZ() + (direction.getOffsetZ() * this.movementScale);

			entity.requestTeleport(x, y, z);
		}
	}

	@Override
	public void onPunchBlock(ServerPlayerEntity player, BlockPos pos) {
		BlockState state = this.canvas.getBlockState(pos);
		if (this.canBeChanged(state)) {
			this.canvas.setBlockState(pos, this.stillState);
		}
	}

	@Override
	public void onUseBlock(ServerPlayerEntity player, BlockPos pos) {
		BlockState state = this.canvas.getBlockState(pos);
		if (this.canBeChanged(state)) {
			if (player.isSneaky()) {
				this.canvas.setBlockState(pos, this.stillState);
			} else {
				Direction facing = player.getHorizontalFacing();
				if (facing != null) {
					BlockState newState = CONVEYOR_STATES.inverse().get(facing);
					if (newState != null) {
						this.canvas.setBlockState(pos, newState);
					}
				}
			}
		}
	}

	static {
		BlockState state = Blocks.MAGENTA_GLAZED_TERRACOTTA.getDefaultState();
		for (Direction direction : Direction.values()) {
			if (Direction.Type.HORIZONTAL.test(direction)) {
				CONVEYOR_STATES.put(state.with(GlazedTerracottaBlock.FACING, direction.getOpposite()), direction);
			}
		}
	}
}
