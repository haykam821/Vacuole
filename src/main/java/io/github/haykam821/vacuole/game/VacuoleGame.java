package io.github.haykam821.vacuole.game;

import java.util.Iterator;

import io.github.haykam821.vacuole.game.map.VacuoleMap;
import io.github.haykam821.vacuole.game.map.VacuoleMapBuilder;
import io.github.haykam821.vacuole.treasure.Treasure;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.GameLogic;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameOpenListener;
import xyz.nucleoid.plasmid.game.event.GameTickListener;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;

public class VacuoleGame {
	private final GameSpace gameSpace;
	private final VacuoleMap map;
	private final VacuoleConfig config;

	public VacuoleGame(GameSpace gameSpace, VacuoleMap map, VacuoleConfig config) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
	}

	public static void setRules(GameLogic game) {
		game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
		game.setRule(GameRule.BREAK_BLOCKS, RuleResult.DENY);
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.DISMOUNT_VEHICLE, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
		game.setRule(GameRule.INTERACTION, RuleResult.DENY);
		game.setRule(GameRule.PLACE_BLOCKS, RuleResult.DENY);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.PVP, RuleResult.DENY);
		game.setRule(GameRule.TEAM_CHAT, RuleResult.DENY);
		game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
		game.setRule(GameRule.UNSTABLE_TNT, RuleResult.DENY);
	}

	public static GameOpenProcedure open(GameOpenContext<VacuoleConfig> context) {
		VacuoleConfig config = context.getConfig();
		VacuoleMap map = new VacuoleMapBuilder(config).build();

		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
			.setGenerator(map.createGenerator(context.getServer()))
			.setDefaultGameMode(GameMode.ADVENTURE);

		return context.createOpenProcedure(worldConfig, game -> {
			VacuoleGame active = new VacuoleGame(game.getSpace(), map, config);
			VacuoleGame.setRules(game);

			// Listeners
			game.on(GameOpenListener.EVENT, active::open);
			game.on(GameTickListener.EVENT, active::tick);
			game.on(PlayerAddListener.EVENT, active::addPlayer);
			game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
			game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
		});
	}

	private void open() {
		Iterator<TemplateRegion> iterator = this.map.getTreasureRegions();
		while (iterator.hasNext()) {
			TemplateRegion region = iterator.next();
			int index = region.getData().getInt("Index");

			Treasure treasure = this.config.getTreasure(index);
			if (treasure != null) {
				treasure.setBounds(region.getBounds());
				treasure.build(this.gameSpace.getWorld());
			}
		}
	}

	private void tick() {
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			this.respawnIfOutOfBounds(player);
		}
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.spawn(player);
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity damagedPlayer, DamageSource source, float damage) {
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		this.spawn(player);
		return ActionResult.FAIL;
	}

	public void spawn(ServerPlayerEntity player) {
		Vec3d spawn = this.map.getSpawn();
		player.teleport(this.gameSpace.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0);
	}

	public void respawnIfOutOfBounds(ServerPlayerEntity player) {
		if (!this.map.getBox().contains(player.getPos())) {
			this.spawn(player);
		}
	}
}
