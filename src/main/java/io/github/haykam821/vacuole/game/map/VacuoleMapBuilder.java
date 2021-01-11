package io.github.haykam821.vacuole.game.map;

import java.io.IOException;

import io.github.haykam821.vacuole.game.VacuoleConfig;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;

public class VacuoleMapBuilder {
	private final VacuoleConfig config;

	public VacuoleMapBuilder(VacuoleConfig config) {
		this.config = config;
	}

	public VacuoleMap build() {
		try {
			MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.getMap());
			return new VacuoleMap(template);
		} catch (IOException exception) {
			throw new GameOpenException(new TranslatableText("text.vacuole.template_load_failed"), exception);
		}
	}
}