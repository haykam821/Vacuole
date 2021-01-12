package io.github.haykam821.vacuole.treasure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class DebugTreasure extends Treasure {
	private static final BlockState OBSIDIAN = Blocks.OBSIDIAN.getDefaultState();

	public DebugTreasure() {
		super(TreasureTypes.DEBUG);
	}

	@Override
	public void build() {
		super.build();
		this.canvas.setBlockState(this.canvas.getBottomCenter().up(), OBSIDIAN);
	}
}
