package io.github.haykam821.vacuole.command;

import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import io.github.haykam821.vacuole.treasure.TreasureType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class VacuoleCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("vacuole")
			.then(CommandManager.literal("treasure")
			.then(CommandManager.literal("list")
			.executes(VacuoleCommand::list))));
	}

	private static int list(CommandContext<ServerCommandSource> context) {
		Set<Identifier> ids = TreasureType.REGISTRY.keySet();

		context.getSource().sendFeedback(() -> {
			return Text.translatable("command.vacuole.treasure.list.header", ids.size());
		}, false);

		for (Identifier id : ids) {
			context.getSource().sendFeedback(() -> {
				TreasureType<?> treasure = TreasureType.REGISTRY.get(id);
				return Text.translatable("command.vacuole.treasure.list.entry", treasure.getName());
			}, false);
		}

		return 0;
	}
}
