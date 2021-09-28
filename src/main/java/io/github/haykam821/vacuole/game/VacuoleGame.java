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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.game.GameActivity;
import xyz.nucleoid.plasmid.game.GameOpenContext;
import xyz.nucleoid.plasmid.game.GameOpenProcedure;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.stimuli.event.block.BlockPunchEvent;
import xyz.nucleoid.stimuli.event.block.BlockUseEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

public class VacuoleGame {
	private final GameSpace gameSpace;
	private final ServerWorld world;
	private final VacuoleMap map;
	private final VacuoleConfig config;
	private final List<Treasure> treasures;

	public VacuoleGame(GameSpace gameSpace, ServerWorld world, VacuoleMap map, VacuoleConfig config) {
		this.gameSpace = gameSpace;
		this.world = world;
		this.map = map;
		this.config = config;
		this.treasures = new ArrayList<>(this.config.getTreasures());
	}

	public static void setRules(GameActivity activity) {
		activity.deny(GameRuleType.BLOCK_DROPS);
		activity.deny(GameRuleType.BREAK_BLOCKS);
		activity.deny(GameRuleType.CRAFTING);
		activity.deny(GameRuleType.DISMOUNT_VEHICLE);
		activity.deny(GameRuleType.FALL_DAMAGE);
		activity.deny(GameRuleType.HUNGER);
		activity.deny(GameRuleType.PLACE_BLOCKS);
		activity.deny(GameRuleType.PORTALS);
		activity.allow(GameRuleType.PVP);
		activity.deny(GameRuleType.THROW_ITEMS);
		activity.deny(GameRuleType.UNSTABLE_TNT);
	}

	public static GameOpenProcedure open(GameOpenContext<VacuoleConfig> context) {
		VacuoleConfig config = context.config();
		VacuoleMap map = new VacuoleMapBuilder(config).build(context.server());

		RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
			.setGenerator(map.createGenerator(context.server()));

		return context.openWithWorld(worldConfig, (activity, world) -> {
			VacuoleGame active = new VacuoleGame(activity.getGameSpace(), world, map, config);
			VacuoleGame.setRules(activity);

			// Listeners
			activity.listen(GameActivityEvents.ENABLE, active::enable);
			activity.listen(GameActivityEvents.TICK, active::tick);
			activity.listen(GamePlayerEvents.ADD, active::addPlayer);
			activity.listen(GamePlayerEvents.OFFER, active::offerPlayer);
			activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
			activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
			activity.listen(BlockPunchEvent.EVENT, active::onPunchBlock);
			activity.listen(BlockUseEvent.EVENT, active::onUseBlock);
		});
	}

	private void enable() {
		Iterator<TemplateRegion> iterator = this.map.getTreasureRegions();
		while (iterator.hasNext()) {
			TemplateRegion region = iterator.next();
			int index = region.getData().getInt("Index");

			Treasure treasure = this.getTreasure(index);
			if (treasure != null) {
				treasure.setCanvas(new TreasureCanvas(this.world, region.getBounds()));
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

	private PlayerOfferResult offerPlayer(PlayerOffer offer) {
		return offer.accept(this.world, this.map.getSpawn()).and(() -> {
			offer.player().changeGameMode(GameMode.ADVENTURE);
		});
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

	private ActionResult onPunchBlock(ServerPlayerEntity player, Direction direction, BlockPos pos) {
		for (Treasure treasure : this.treasures) {
			if (treasure.contains(pos)) {
				treasure.onPunchBlock(player, pos);
				break;
			}
		}

		return ActionResult.FAIL;
	}

	private ActionResult onUseBlock(ServerPlayerEntity player, Hand hand, BlockHitResult hitResult) {
		Iterator<TemplateRegion> iterator = this.map.getTreasureSelectorRegions();
		while (iterator.hasNext()) {
			TemplateRegion region = iterator.next();
			if (region.getBounds().contains(hitResult.getBlockPos())) {
				int index = region.getData().getInt("Index");
				TreasureSelector.build(player, this, index).open();

				return ActionResult.SUCCESS;
			}
		}

		for (Treasure treasure : this.treasures) {
			if (treasure.contains(hitResult.getBlockPos())) {
				treasure.onUseBlock(player, hitResult.getBlockPos());
				break;
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
				treasure.setCanvas(new TreasureCanvas(this.world, region.getBounds()));
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
		player.teleport(this.world, spawn.getX(), spawn.getY(), spawn.getZ(), rotation.x, rotation.y);
	}

	public void respawnIfOutOfBounds(ServerPlayerEntity player) {
		if (!this.map.getBox().contains(player.getPos())) {
			this.spawn(player);
		}
	}
}
