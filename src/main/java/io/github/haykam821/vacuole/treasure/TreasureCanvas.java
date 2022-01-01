package io.github.haykam821.vacuole.treasure;

import java.util.List;

import com.google.common.base.Predicates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.map_templates.BlockBounds;

public class TreasureCanvas {
	private static final Logger LOGGER = LogManager.getLogger("TreasureCanvas");
	private static final BlockState AIR = Blocks.AIR.getDefaultState();

	private final ServerWorld world;
	private final BlockBounds bounds;
	private final BlockPos center;
	private final BlockPos bottomCenter;

	public TreasureCanvas(ServerWorld world, BlockBounds bounds) {
		this.world = world;
		this.bounds = bounds;

		this.center = new BlockPos(bounds.center());
		this.bottomCenter = new BlockPos(this.center.getX(), bounds.min().getY(), this.center.getZ());
	}

	public BlockPos getMin() {
		return this.bounds.min();
	}

	public BlockPos getMax() {
		return this.bounds.max();
	}

	public BlockPos getCenter() {
		return this.center;
	}

	public BlockPos getBottomCenter() {
		return this.bottomCenter;
	}

	public boolean contains(BlockPos pos) {
		return this.bounds.contains(pos);
	}

	public boolean setBlockState(BlockPos pos, BlockState state) {
		if (!this.bounds.contains(pos)) {
			LOGGER.warn("Attempted to modify block outside of canvas: {}", pos);
			return false;
		}

		return this.world.setBlockState(pos, state);
	}

	public BlockState getBlockState(BlockPos pos) {
		if (!this.bounds.contains(pos)) {
			return AIR;
		}
		return this.world.getBlockState(pos);
	}

	public BlockEntity getBlockEntity(BlockPos pos) {
		if (!this.bounds.contains(pos)) {
			return null;
		}
		return this.world.getBlockEntity(pos);
	}

	public <T extends BlockEntity> T getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
		if (!this.bounds.contains(pos)) {
			return null;
		}
		return this.world.getBlockEntity(pos, type).orElse(null);
	}

	public void clear() {
		for (BlockPos pos : this.bounds) {
			this.world.setBlockState(pos, AIR);
		}
	}

	public <T extends Entity> List<T> getEntitiesByType(EntityType<T> type) {
		return this.world.getEntitiesByType(type, this.bounds.asBox(), Predicates.alwaysTrue());
	}

	public <T extends Entity> List<T> getEntitiesByClass(Class<T> clazz) {
		return this.world.getEntitiesByClass(clazz, this.bounds.asBox(), Predicates.alwaysTrue());
	}

	public List<Entity> getEntities() {
		return this.getEntitiesByClass(Entity.class);
	}

	public PlayerEntity getClosestPlayer(BlockPos pos) {
		return this.world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, -1, player -> {
			return this.bounds.asBox().intersects(player.getBoundingBox());
		});
	}

	public FireworkRocketEntity spawnFirework(ItemStack stack, BlockPos pos) {
		FireworkRocketEntity firework = new FireworkRocketEntity(this.world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, stack);
		return this.world.spawnEntity(firework) ? firework : null;
	}
}
