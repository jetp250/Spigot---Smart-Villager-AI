package me.jetp250.goapimpl.entities;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftVillager;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;

import me.jetp250.goapimpl.general.GOAPController;
import me.jetp250.goapimpl.general.Village;
import me.jetp250.goapimpl.objectives.Objectives;
import me.jetp250.goapimpl.objectives.Objectives.WeightedObjectiveList;
import me.jetp250.goapimpl.utilities.Debug;
import me.jetp250.goapimpl.utilities.MathHelper;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.DifficultyDamageScaler;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EntityEvoker;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityMonster;
import net.minecraft.server.v1_12_R1.EntityVex;
import net.minecraft.server.v1_12_R1.EntityVillager;
import net.minecraft.server.v1_12_R1.EntityVindicator;
import net.minecraft.server.v1_12_R1.EnumHand;
import net.minecraft.server.v1_12_R1.GroupDataEntity;
import net.minecraft.server.v1_12_R1.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_12_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_12_R1.PathfinderGoalInteract;
import net.minecraft.server.v1_12_R1.PathfinderGoalInteractVillagers;
import net.minecraft.server.v1_12_R1.PathfinderGoalLookAtTradingPlayer;
import net.minecraft.server.v1_12_R1.PathfinderGoalMoveIndoors;
import net.minecraft.server.v1_12_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_12_R1.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_12_R1.PathfinderGoalOpenDoor;
import net.minecraft.server.v1_12_R1.PathfinderGoalRestrictOpenDoor;
import net.minecraft.server.v1_12_R1.PathfinderGoalTradeWithPlayer;
import net.minecraft.server.v1_12_R1.World;

public class Human extends EntityVillager {

	private Village village;
	private final Inventory inventory;
	private BlockPosition interest;
	private final VillagerType type;
	private final GOAPController controller;
	private final CraftHuman entity;

	public Human(final World world) {
		super(world);
		this.aP();
		this.type = VillagerType.LUMBERJACK; //VillagerType.pickRandom(this.random);
		final WeightedObjectiveList objectives = this.type.getObjectives();
		this.controller = new GOAPController(this, this.type.maxObjectives, objectives);
		this.entity = new CraftHuman(this.world.getServer(), this);
		this.inventory = Bukkit.createInventory(this.entity, 36, this.getName());
		if (objectives.length() != 0) {
			int amount = this.random.nextInt(objectives.length());
			if (amount == 0) {
				return;
			}
			while (amount-- > 0) {
				this.controller.addObjective(objectives.get(this.random.nextInt(objectives.length())).create(this));
			}
		}
	}

	public GOAPController getController() {
		return this.controller;
	}

	@Override
	public void B_() {
		super.B_();
		if (this.ticksLived % 5 == 0) {
			this.world.methodProfiler.a("human update tick");
			this.controller.update();
			if (this.ticksLived % 40 == 0) {
				this.aP();
			}
			this.world.methodProfiler.b();
		}
	}

	protected void aP() {
		if (this.interest != null) {
			final boolean success = this.getNavigation().a(this.interest.getX(), this.interest.getY(), this.interest.getZ(), 1);
			if (success) {
				this.interest = null;
			}
		}
		if (this.village == null) {
			this.world.methodProfiler.a("human village search");
			final Village nearest = Village.getNearestVillage(this.locX, this.locZ);
			if (nearest != null) {
				final double dSqr = MathHelper.distSqr(nearest.getX(), nearest.getZ(), this.locX, this.locZ);
				if (dSqr < 2000) { // 10000
					this.village = nearest;
					final boolean success = this.getNavigation().a(nearest.getX(), this.world.c(nearest.getX(), nearest.getZ()), nearest.getZ(), 1F);
					if (!success) {
						this.interest = new BlockPosition(this.village.getX(), this.world.c(this.village.getX(), this.village.getZ()), this.village.getZ());
					}
					this.world.methodProfiler.b();
					return;
				}
			}
			final List<Entity> nearby = this.world.getEntities(this, this.getBoundingBox().grow(40, 20, 40), e -> e instanceof Human);
			if (!nearby.isEmpty()) {
				for (final Entity entity : nearby) {
					final Human villager = (Human) entity;
					if (villager.getVillage() != null) {
						this.village = villager.getVillage();
						entity.setCustomName("VILLAGE#" + this.village.getId());
						final boolean success = this.getNavigation().a(nearest.getX(), this.world.c(nearest.getX(), nearest.getZ()), nearest.getZ(), 1F);
						if (!success) {
							this.interest = new BlockPosition(this.village.getX(), this.world.c(this.village.getX(), this.village.getZ()), this.village.getZ());
						}
						this.world.methodProfiler.b();
						return;
					}
				}
				if (this.village == null && nearby.size() > 4) {
					double cx = this.locX;
					double cz = this.locZ;
					for (final Entity entity : nearby) {
						cx += entity.locX;
						cz += entity.locZ;
					}
					cx /= nearby.size() + 1;
					cz /= nearby.size() + 1;
					final int y = this.world.c((int) cx, (int) cz);
					final BlockPosition c = new BlockPosition(cx, y, cz);
					this.village = new Village(c, this.world);
					for (final Entity entity : nearby) {
						((Human) entity).village = this.village;
						entity.setCustomName("VILLAGE#" + this.village.getId());
						((Human) entity).getNavigation().a(cx, y, cz, 1);
					}
					final EntityArmorStand marker = new EntityArmorStand(this.world);
					marker.setPosition(cx, y + 10, cz);
					marker.setNoGravity(true);
					marker.setCustomName("VILLAGE#" + this.village.getId());
					marker.setCustomNameVisible(true);
					this.world.addEntity(marker, SpawnReason.CUSTOM);
				}
			}
			this.world.methodProfiler.b();
		}
	}

	@Override
	public GroupDataEntity prepare(final DifficultyDamageScaler difficultydamagescaler, final GroupDataEntity groupdataentity) {
		this.setCustomName((this.village != null ? "V#" + this.village.getId() + " - " : "") + this.type.name().toLowerCase());
		this.setCustomNameVisible(true);
		return super.prepare(difficultydamagescaler, groupdataentity);
	}

	public BlockPosition getInterest() {
		return this.interest;
	}

	public Village getVillage() {
		return this.village;
	}

	@Override
	public CraftHuman getBukkitEntity() {
		return this.entity;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	@Override
	protected void r() {
		this.goalSelector.a(0, new PathfinderGoalFloat(this));
		this.goalSelector.a(1, new PathfinderGoalAvoidTarget<>(this, EntityEvoker.class, 12.0f, 0.8, 0.8));
		this.goalSelector.a(1, new PathfinderGoalAvoidTarget<>(this, EntityVindicator.class, 8.0f, 0.8, 0.8));
		this.goalSelector.a(1, new PathfinderGoalAvoidTarget<>(this, EntityVex.class, 8.0f, 0.6, 0.6));
		this.goalSelector.a(1, new PathfinderGoalTradeWithPlayer(this));
		this.goalSelector.a(1, new PathfinderGoalLookAtTradingPlayer(this));
		this.goalSelector.a(2, new PathfinderGoalMoveIndoors(this));
		this.goalSelector.a(3, new PathfinderGoalRestrictOpenDoor(this));
		this.goalSelector.a(4, new PathfinderGoalOpenDoor(this, true));
		this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 0.6));
		//		this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
		//		this.goalSelector.a(6, new PathfinderGoalMakeLove(this));
		//		this.goalSelector.a(8, new PathfinderGoalVillagerFarm(this, 0.6));
		//		this.goalSelector.a(9, new PathfinderGoalInteract(this, EntityHuman.class, 3.0f, 1.0f));
		this.goalSelector.a(9, new PathfinderGoalInteract(this, Human.class, 3.0f, 1.0f));
		this.goalSelector.a(9, new PathfinderGoalInteractVillagers(this));
		//		this.goalSelector.a(9, new PathfinderGoalRandomStrollLand(this, 0.6));
		//		this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0f));
		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityMonster.class, true));
	}

	@Override
	public boolean a(final EntityHuman entityhuman, final EnumHand enumhand) {
		//		if (this.getInventory().firstEmpty() > 1) {
		//			this.getInventory().sort((o1, o2) -> o1 == null && o2 == null ? 0 : o1 == null ? 1 : -1);
		//		}
		entityhuman.getBukkitEntity().openInventory(this.getInventory());
		//		entityhuman.getBukkitEntity().openInventory(this.getInventory().convertToBukkit(entityhuman.getBukkitEntity(), this.getName()));
		return false;
	}

	public static enum VillagerType {
		FARMER(14, 3),
		LUMBERJACK(16, 3, Objectives.CUT_TREES),
		MINER(14, 3),
		BUILDER(8, 3),
		GUARD(12, 2),
		MERCHANT(7, 2),
		MAGE(10, 2),
		ARCHER(10, 2);

		private static final int TOTAL_WEIGHT;

		private final int maxObjectives;
		private final int weight;
		private final WeightedObjectiveList objectives;

		private VillagerType(final int maxObjectives, final int weight, final Objectives... objectives) {
			this.maxObjectives = maxObjectives;
			this.weight = weight;
			this.objectives = new WeightedObjectiveList(objectives);
		}

		public WeightedObjectiveList getObjectives() {
			return this.objectives;
		}

		public int getMaxObjectiveCount() {
			return this.maxObjectives;
		}

		public static VillagerType pickRandom(final Random source) {
			int total = source.nextInt(VillagerType.TOTAL_WEIGHT);
			final VillagerType[] types = VillagerType.values();
			for (final VillagerType type : types) {
				total -= type.weight;
				if (total <= 0) {
					return type;
				}
			}
			return types[source.nextInt(types.length)];
		}

		static {
			int weight = 0;
			final VillagerType[] values = VillagerType.values();
			for (final VillagerType type : values) {
				weight += type.weight;
			}
			TOTAL_WEIGHT = weight;
		}
	}

	public class CraftHuman extends CraftVillager {

		private final Human human;

		public CraftHuman(final CraftServer server, final Human entity) {
			super(server, entity);
			this.human = entity;
		}

		@Override
		public Inventory getInventory() {
			Debug.a("Called getInventory()");
			return this.human.getInventory();//.convertToBukkit(this, human.getName());
		}

	}

}
