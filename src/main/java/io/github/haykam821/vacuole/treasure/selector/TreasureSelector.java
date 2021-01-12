package io.github.haykam821.vacuole.treasure.selector;

import io.github.haykam821.vacuole.game.VacuoleGame;
import io.github.haykam821.vacuole.treasure.TreasureType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.shop.ShopUi;

public class TreasureSelector {
	public static ShopUi build(VacuoleGame game, int index) {
		Text title = new TranslatableText("text.vacuole.treasure_selector", index + 1);
		return ShopUi.create(title, builder -> {
			for (TreasureType<?> type : TreasureType.REGISTRY.values()) {
				builder.add(type.createShopEntry(game, index));
			}
		});
	}
}