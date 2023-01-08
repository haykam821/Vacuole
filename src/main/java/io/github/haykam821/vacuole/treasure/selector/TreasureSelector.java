package io.github.haykam821.vacuole.treasure.selector;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SimpleGuiBuilder;
import io.github.haykam821.vacuole.game.VacuoleGame;
import io.github.haykam821.vacuole.treasure.TreasureType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TreasureSelector {
	public static SimpleGui build(ServerPlayerEntity player, VacuoleGame game, int index) {
		SimpleGuiBuilder builder = new SimpleGuiBuilder(ScreenHandlerType.GENERIC_9X5, false);

		builder.setTitle(Text.translatable("text.vacuole.treasure_selector", index + 1));

		for (TreasureType<?> type : TreasureType.REGISTRY.values()) {
			builder.addSlot(type.createShopEntry(game, index));
		}

		return builder.build(player);
	}
}