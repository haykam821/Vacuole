package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class SierpinskiCarpetTreasure extends Treasure {
	private static final BlockStateProvider DEFAULT_STATE_PROVIDER = BlockStateProvider.of(Blocks.MOSS_CARPET);

	public static final Codec<SierpinskiCarpetTreasure> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("padding", 0).forGetter(treasure -> treasure.padding),
			BlockStateProvider.TYPE_CODEC.optionalFieldOf("state_provider", DEFAULT_STATE_PROVIDER).forGetter(treasure -> treasure.stateProvider)
		).apply(instance, SierpinskiCarpetTreasure::new);
	});

	private static final Random RANDOM = Random.create();

	private final int padding;
	private final BlockStateProvider stateProvider;

	public SierpinskiCarpetTreasure(int padding, BlockStateProvider stateProvider) {
		super(TreasureTypes.SIERPINSKI_CARPET);

		this.padding = padding;
		this.stateProvider = stateProvider;
	}

	public SierpinskiCarpetTreasure() {
		this(0, DEFAULT_STATE_PROVIDER);
	}

	@Override
	public void build() {
		super.build();

		int minX = this.canvas.getMin().getX();
		int maxX = this.canvas.getMax().getX();

		int minZ = this.canvas.getMin().getZ();
		int maxZ = this.canvas.getMax().getZ();
		
		int maxSize = Math.min(maxX - minX - padding * 2, maxZ - minZ - padding * 2);
		int order = (int) (Math.log(maxSize + 1) / Math.log(3));

		int size = (int) Math.pow(3, order);
		int paddingX = MathHelper.ceil((maxX - minX - size) / 2d);
		int paddingZ = MathHelper.ceil((maxZ - minZ - size) / 2d);

		BlockPos.Mutable pos = this.canvas.getMin().mutableCopy().move(Direction.UP);

		for (int x = 0; x < size; x++) {
			pos.setX(minX + paddingX + x);
			for (int z = 0; z < size; z++) {
				pos.setZ(minZ + paddingZ + z);
				if (this.isBlock(x, z)) {
					this.canvas.setBlockState(pos, this.stateProvider.get(RANDOM, pos));
				}
			}
		}
	}
	
	private boolean isBlock(int x, int z) {
		while (x != 0 && z != 0) {
			if (x % 3 == 1 && z % 3 == 1) {
				return false;
			}

			x /= 3;
			z /= 3;
		}

		return true;
	}
}
