package io.github.haykam821.vacuole.game.map;

import java.io.IOException;

import io.github.haykam821.vacuole.game.VacuoleConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;

public class VacuoleMapBuilder {
	private final VacuoleConfig config;

	public VacuoleMapBuilder(VacuoleConfig config) {
		this.config = config;
	}

	public VacuoleMap build(MinecraftServer server) {
		try {
			MapTemplate template = MapTemplateSerializer.loadFromResource(server, this.config.getMap());
			return new VacuoleMap(template);
		} catch (IOException exception) {
			throw new GameOpenException(Text.translatable("text.vacuole.template_load_failed"), exception);
		}
	}
}