package io.github.haykam821.vacuole.treasure;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import io.github.haykam821.vacuole.game.VacuoleGame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;
import xyz.nucleoid.plasmid.shop.Cost;
import xyz.nucleoid.plasmid.shop.ShopEntry;

public class TreasureType<T extends Treasure> {
	public static final TinyRegistry<TreasureType<?>> REGISTRY = TinyRegistry.create();

	private final Codec<T> codec;
	private final Supplier<T> creator;
	private final Item icon;
	private String translationKey;

	public TreasureType(Codec<T> codec, Supplier<T> creator, ItemConvertible icon) {
		this.codec = codec;
		this.creator = creator;
		this.icon = icon.asItem();
	}

	public Codec<T> getCodec() {
		return this.codec;
	}

	private T create() {
		return this.creator.get();
	}

	private String getTranslationKey() {
		if (this.translationKey == null) {
			Identifier id = REGISTRY.getIdentifier(this);
			this.translationKey = "treasure." + id.getNamespace() + "." + id.getPath();
		}
		return this.translationKey;
	}

	public Text getName() {
		return new TranslatableText(this.getTranslationKey());
	}

	public ShopEntry createShopEntry(VacuoleGame game, int index) {
		return ShopEntry.ofIcon(this.icon).withName(this.getName()).withCost(Cost.free()).onBuy(player -> {
			game.selectTreasure(index, this.create(), player);
		});
	}
}