package io.github.haykam821.vacuole.treasure;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class SkullsTreasure extends Treasure {
	private static final Random RANDOM = new Random();

	private static final Block DEFAULT_SKULL_BLOCK = Blocks.SKELETON_SKULL;
	private static final BlockStateProvider DEFAULT_POST_STATE_PROVIDER = BlockStateProvider.of(Blocks.DEEPSLATE_TILE_WALL);

	public static final Codec<SkullsTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Registry.BLOCK.getCodec().optionalFieldOf("skull_block", DEFAULT_SKULL_BLOCK).forGetter(treasure -> treasure.skullBlock),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("post_state_provider", DEFAULT_POST_STATE_PROVIDER).forGetter(treasure -> treasure.postStateProvider),
			Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("height", 1).forGetter(treasure -> treasure.height),
			Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("count", 8).forGetter(treasure -> treasure.count)
		).apply(instance, SkullsTreasure::new);
	});

	private final Block skullBlock;
	private final BlockStateProvider postStateProvider;
	private final int height;
	private final int count;

	public SkullsTreasure(Block skullBlock, BlockStateProvider postStateProvider, int height, int count) {
		super(TreasureTypes.SKULLS);

		this.skullBlock = skullBlock;
		this.postStateProvider = postStateProvider;
		this.height = height;
		this.count = count;
	}

	public SkullsTreasure() {
		this(DEFAULT_SKULL_BLOCK, DEFAULT_POST_STATE_PROVIDER, 1, 8);
	}

	@Override
	public void build() {
		super.build();

		BlockPos.Mutable pos = new BlockPos.Mutable();

		int minX = this.canvas.getMin().getX();
		int maxX = this.canvas.getMax().getX();

		int minZ = this.canvas.getMin().getZ();
		int maxZ = this.canvas.getMax().getZ();

		int minY = this.canvas.getMin().getY();

		LongSet positions = new LongOpenHashSet();
		for (int index = 0; index < this.count; index++) {
			int x = MathHelper.nextInt(RANDOM, minX, maxX);
			int z = MathHelper.nextInt(RANDOM, minZ, maxZ);
			pos.set(x, minY + 1 + this.height, z);
			positions.add(pos.asLong());

			this.canvas.setBlockState(pos, this.skullBlock.getDefaultState());
			
			pos.move(Direction.DOWN);
			while (pos.getY() > minY) {
				this.canvas.setBlockState(pos, this.postStateProvider.getBlockState(RANDOM, pos));
				pos.move(Direction.DOWN);
			}
		}
	}

	@Override
	public void tick() {
		BlockPos min = this.canvas.getMin();
		BlockPos max = this.canvas.getMax();

		BlockPos.Mutable pos = min.mutableCopy();
		pos.move(Direction.UP, height + 1);

		for (int x = min.getX(); x <= max.getX(); x++) {
			pos.setX(x);
			for (int z = min.getZ(); z <= max.getZ(); z++) {
				pos.setZ(z);

				BlockState state = this.canvas.getBlockState(pos);
				if (state.isOf(this.skullBlock) && state.contains(SkullBlock.ROTATION)) {
					PlayerEntity player = this.canvas.getClosestPlayer(pos);
					if (player != null) {
						double angle;
						if (player.isSneaking()) {
							angle = player.getYaw() + 180;
						} else {
							double relativeX = player.getX() - pos.getX() - 0.5;
							double relativeZ = player.getZ() - pos.getZ() - 0.5;

							angle = MathHelper.atan2(relativeZ, relativeX) * MathHelper.DEGREES_PER_RADIAN + 90;
						}

						int rotation = MathHelper.floor((angle * 16d / 360d) + 0.5d) & 15;
						this.canvas.setBlockState(pos, state.with(SkullBlock.ROTATION, rotation));
					}
					
				}
			}
		}
	}
}
