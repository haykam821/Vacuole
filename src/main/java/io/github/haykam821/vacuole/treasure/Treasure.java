package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class Treasure {
	public static final Codec<Treasure> TYPE_CODEC = TreasureType.REGISTRY.dispatch(Treasure::getType, TreasureType::getCodec);
	private static final BlockState BASE = Blocks.GRAY_TERRACOTTA.getDefaultState();

	private final TreasureType<?> type;
	protected TreasureCanvas canvas;

	public Treasure(TreasureType<?> type) {
		this.type = type;
	}

	// World modification
	private void buildBase() {
		BlockPos.Mutable pos = this.canvas.getMin().mutableCopy();
		for (int x = this.canvas.getMin().getX(); x <= this.canvas.getMax().getX(); x++) {
			pos.setX(x);
			for (int z = this.canvas.getMin().getZ(); z <= this.canvas.getMax().getZ(); z++) {
				pos.setZ(z);
				this.canvas.setBlockState(pos, BASE);
			}
		}
	}

	public void build() {
		this.buildBase();
	}

	public void clear() {
		this.canvas.clear();
	}

	// Getters
	public TreasureType<?> getType() {
		return this.type;
	}

	public Text getName() {
		return this.type.getName();
	}

	public void setCanvas(TreasureCanvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{type=" + this.type + ", canvas=" + this.canvas + "}";
	}
}