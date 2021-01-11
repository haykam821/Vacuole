package io.github.haykam821.vacuole.treasure;

import com.mojang.serialization.Codec;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.registry.TinyRegistry;

public class TreasureType<T extends Treasure> {
	public static final TinyRegistry<TreasureType<?>> REGISTRY = TinyRegistry.newStable();

	private final Codec<T> codec;
	private String translationKey;

	public TreasureType(Codec<T> codec) {
		this.codec = codec;
	}

	public Codec<T> getCodec() {
		return this.codec;
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
}