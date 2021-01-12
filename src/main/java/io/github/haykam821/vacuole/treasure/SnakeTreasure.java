package io.github.haykam821.vacuole.treasure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SnakeTreasure extends Treasure {
	public static final Codec<SnakeTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.INT.optionalFieldOf("max_size", 15).forGetter(treasure -> treasure.maxSize),
			Codec.INT.optionalFieldOf("max_ticks_until_move", 2).forGetter(treasure -> treasure.maxTicksUntilMove)
		).apply(instance, SnakeTreasure::new);
	});

	private static final Random RANDOM = new Random();
	private static final BlockState SNAKE = Blocks.LIME_TERRACOTTA.getDefaultState();
	private static final BlockState AIR = Blocks.AIR.getDefaultState();

	private final int maxSize;
	private final int maxTicksUntilMove;
	private final List<BlockPos> segments = new ArrayList<>();
	private int ticksUntilMove;

	public SnakeTreasure(int maxSize, int maxTicksUntilMove) {
		super(TreasureTypes.SNAKE);

		this.maxSize = maxSize;
		this.maxTicksUntilMove = maxTicksUntilMove;
	}

	public SnakeTreasure() {
		this(15, 3);
	}

	private void addDirectionIfViable(List<Direction> directions, BlockPos pos, Direction direction) {
		BlockPos newPos = pos.offset(direction);
		if (!this.segments.contains(newPos) && this.contains(newPos)) {
			directions.add(direction);
		}
	}

	private List<Direction> getPossibleDirections(BlockPos pos) {
		List<Direction> directions = new ArrayList<>();

		this.addDirectionIfViable(directions, pos, Direction.NORTH);
		this.addDirectionIfViable(directions, pos, Direction.EAST);
		this.addDirectionIfViable(directions, pos, Direction.SOUTH);
		this.addDirectionIfViable(directions, pos, Direction.WEST);

		return directions;
	}

	private void reset() {
		this.canvas.clear();
		this.build();
		
		this.segments.clear();
		this.segments.add(this.canvas.getBottomCenter().up());
	}

	@Override
	public void tick() {
		if (this.ticksUntilMove > 0) {
			this.ticksUntilMove -= 1;
			return;
		}

		// Find new direction
		if (this.segments.isEmpty()) {
			this.reset();
			return;
		}
		BlockPos previousHead = this.segments.get(0);
		List<Direction> directions = this.getPossibleDirections(previousHead);
		if (directions.isEmpty()) {
			this.reset();
			return;
		}

		// Place new head
		Direction direction = directions.get(RANDOM.nextInt(directions.size()));
		BlockPos head = previousHead.offset(direction);
		this.segments.add(0, head);
		this.canvas.setBlockState(head, SNAKE);

		if (this.segments.size() > this.maxSize) {
			this.canvas.setBlockState(this.segments.remove(this.maxSize), AIR);
		}

		this.ticksUntilMove = this.maxTicksUntilMove;
	}
}
