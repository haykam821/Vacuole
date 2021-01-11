package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class Treasure {
	public static final Codec<Treasure> TYPE_CODEC = TreasureType.REGISTRY.dispatch(Treasure::getType, TreasureType::getCodec);
	private static final BlockState BASE = Blocks.GRAY_TERRACOTTA.getDefaultState();

	private final TreasureType<?> type;
	private final Item displayItem;
	private BlockBounds bounds = new BlockBounds(BlockPos.ORIGIN, BlockPos.ORIGIN);

	public Treasure(TreasureType<?> type, Item displayItem) {
		this.type = type;
		this.displayItem = displayItem;
	}

	// World modification
	private void buildBase(ServerWorld world) {
		BlockPos.Mutable pos = this.bounds.getMin().mutableCopy();
		for (int x = this.bounds.getMin().getX(); x <= this.bounds.getMax().getX(); x++) {
			pos.setX(x);
			for (int z = this.bounds.getMin().getZ(); z <= this.bounds.getMax().getZ(); z++) {
				pos.setZ(z);
				world.setBlockState(pos, BASE);
			}
		}
	}

	public void build(ServerWorld world) {
		this.buildBase(world);
	}

	// Getters
	public TreasureType<?> getType() {
		return this.type;
	}

	public Item getDisplayItem() {
		return this.displayItem;
	}

	public Text getName() {
		return this.type.getName();
	}

	public BlockBounds getBounds() {
		return this.bounds;
	}

	public void setBounds(BlockBounds bounds) {
		this.bounds = bounds;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{displayItem=" + this.displayItem + "}";
	}
}