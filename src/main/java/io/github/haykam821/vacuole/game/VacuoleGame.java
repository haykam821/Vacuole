package io.github.haykam821.vacuole.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.haykam821.vacuole.game.map.VacuoleMap;
import io.github.haykam821.vacuole.game.map.VacuoleMapBuilder;
import io.github.haykam821.vacuole.treasure.Treasure;
import io.github.haykam821.vacuole.treasure.TreasureCanvas;
import io.github.haykam821.vacuole.treasure.selector.TreasureSelector;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec2f;
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
import xyz.nucleoid.plasmid.game.event.UseBlockListener;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;

public class VacuoleGame {
	private final GameSpace gameSpace;
	private final VacuoleMap map;
	private final VacuoleConfig config;
	private final List<Treasure> treasures;

	public VacuoleGame(GameSpace gameSpace, VacuoleMap map, VacuoleConfig config) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;
		this.treasures = new ArrayList<>(this.config.getTreasures());
	}

	public static void setRules(GameLogic game) {
		game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
		game.setRule(GameRule.BREAK_BLOCKS, RuleResult.DENY);
		game.setRule(GameRule.CRAFTING, RuleResult.DENY);
		game.setRule(GameRule.DISMOUNT_VEHICLE, RuleResult.DENY);
		game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
		game.setRule(GameRule.HUNGER, RuleResult.DENY);
		game.setRule(GameRule.PLACE_BLOCKS, RuleResult.DENY);
		game.setRule(GameRule.PORTALS, RuleResult.DENY);
		game.setRule(GameRule.PVP, RuleResult.ALLOW);
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
			game.on(UseBlockListener.EVENT, active::onUseBlock);
		});
	}

	private void open() {
		Iterator<TemplateRegion> iterator = this.map.getTreasureRegions();
		while (iterator.hasNext()) {
			TemplateRegion region = iterator.next();
			int index = region.getData().getInt("Index");

			Treasure treasure = this.getTreasure(index);
			if (treasure != null) {
				treasure.setCanvas(new TreasureCanvas(this.gameSpace.getWorld(), region.getBounds()));
				treasure.build();
			}
		}
	}

	private void tick() {
		for (ServerPlayerEntity player : this.gameSpace.getPlayers()) {
			this.respawnIfOutOfBounds(player);
		}
		for (Treasure treasure : this.treasures) {
			treasure.tick();
		}
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.spawn(player);
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float damage) {
		for (Treasure treasure : this.treasures) {
			if (treasure.contains(player.getBlockPos())) {
				if (treasure.isKnockbackEnabled()) {
					player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 0, 127, true, false));
					return ActionResult.SUCCESS;
				}
				break;
			}
		}
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		this.spawn(player);
		return ActionResult.FAIL;
	}

	private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		Iterator<TemplateRegion> iterator = this.map.getTreasureSelectorRegions();
		while (iterator.hasNext()) {
			TemplateRegion region = iterator.next();
			if (region.getBounds().contains(hitResult.getBlockPos())) {
				int index = region.getData().getInt("Index");
				player.openHandledScreen(TreasureSelector.build(this, index));

				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.FAIL;
	}

	// Utilities
	private Treasure getTreasure(int index) {
		if (index >= 0 && index < this.treasures.size()) {
			return this.treasures.get(index);
		}
		return null;
	}

	public boolean selectTreasure(int index, Treasure treasure) {
		if (index < 0 || index >= this.treasures.size()) {
			return false;
		}

		Iterator<TemplateRegion> iterator = this.map.getTreasureRegions();
		while (iterator.hasNext()) {
			TemplateRegion region = iterator.next();
			int regionIndex = region.getData().getInt("Index");

			if (regionIndex == index) {
				this.treasures.get(index).clear();
				treasure.setCanvas(new TreasureCanvas(this.gameSpace.getWorld(), region.getBounds()));
				this.treasures.set(index, treasure);
				treasure.build();
				return true;
			}
		}

		return false;
	}

	public void spawn(ServerPlayerEntity player) {
		Vec3d spawn = this.map.getSpawn();
		Vec2f rotation = this.map.getSpawnRotation();
		player.teleport(this.gameSpace.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), rotation.x, rotation.y);
	}

	public void respawnIfOutOfBounds(ServerPlayerEntity player) {
		if (!this.map.getBox().contains(player.getPos())) {
			this.spawn(player);
		}
	}
}
