package io.github.haykam821.vacuole.command;

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
		context.getSource().sendFeedback(Text.translatable("command.vacuole.treasure.list.header", TreasureType.REGISTRY.keySet().size()), false);

		for (Identifier id : TreasureType.REGISTRY.keySet()) {
			TreasureType<?> treasure = TreasureType.REGISTRY.get(id);
			context.getSource().sendFeedback(Text.translatable("command.vacuole.treasure.list.entry", treasure.getName()), false);
		}

		return 0;
	}
}
