package io.github.haykam821.vacuole.game.map;

import java.util.Iterator;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;

public class VacuoleMap {
	private final MapTemplate template;
	private final Box box;

	public VacuoleMap(MapTemplate template) {
		this.template = template;
		this.box = this.template.getBounds().toBox().expand(10);
	}

	public MapTemplate getTemplate() {
		return this.template;
	}

	public Box getBox() {
		return this.box;
	}

	public Vec3d getSpawn() {
		TemplateRegion spawn = this.template.getMetadata().getFirstRegion("spawn");
		if (spawn != null) {
			return VacuoleMap.getBottomCenter(spawn.getBounds());
		}

		return VacuoleMap.getBottomCenter(this.template.getBounds());
	}

	public Vec2f getSpawnRotation() {
		TemplateRegion spawn = this.template.getMetadata().getFirstRegion("spawn");
		if (spawn != null) {
			NbtList tag = spawn.getData().getList("Rotation", NbtType.FLOAT);
			return  new Vec2f(tag.getFloat(0), tag.getFloat(1));
		}

		return new Vec2f(0, 0);
	}

	public Iterator<TemplateRegion> getTreasureRegions() {
		return this.template.getMetadata().getRegions("treasure").iterator();
	}

	public Iterator<TemplateRegion> getTreasureSelectorRegions() {
		return this.template.getMetadata().getRegions("treasure_selector").iterator();
	}

	public ChunkGenerator createGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}

	private static Vec3d getBottomCenter(BlockBounds bounds) {
		Vec3d center = bounds.getCenter();
		return new Vec3d(center.getX(), bounds.getMin().getY(), center.getZ());
	}
}
