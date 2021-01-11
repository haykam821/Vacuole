package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class DebugTreasure extends Treasure {
	public static final Codec<DebugTreasure> CODEC = Codec.unit(DebugTreasure::new);
	private static final BlockState STATE = Blocks.OBSIDIAN.getDefaultState();

	public DebugTreasure() {
		super(TreasureTypes.DEBUG, Items.DEBUG_STICK);
	}

	@Override
	public void build(ServerWorld world) {
		super.build(world);
		
		BlockPos pos = new BlockPos(this.getBounds().getCenter()).up();
		world.setBlockState(pos, STATE);
	}
}
