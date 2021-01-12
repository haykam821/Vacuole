package io.github.haykam821.vacuole.game;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.haykam821.vacuole.treasure.Treasure;
import net.minecraft.util.Identifier;

public class VacuoleConfig {
	public static final Codec<VacuoleConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
			Identifier.CODEC.fieldOf("map").forGetter(VacuoleConfig::getMap),
			Treasure.TYPE_CODEC.listOf().fieldOf("treasures").forGetter(VacuoleConfig::getTreasures)
		).apply(instance, VacuoleConfig::new);
	});

	private final Identifier map;
	private final List<Treasure> treasures;

	public VacuoleConfig(Identifier map, List<Treasure> treasures) {
		this.map = map;
		this.treasures = treasures;
	}

	public Identifier getMap() {
		return this.map;
	}

	public List<Treasure> getTreasures() {
		return this.treasures;
	}
}
