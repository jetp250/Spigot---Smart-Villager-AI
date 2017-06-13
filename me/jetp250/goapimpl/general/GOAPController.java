package me.jetp250.goapimpl.general;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.inventory.ItemStack;

import me.jetp250.goapimpl.entities.Human;
import me.jetp250.goapimpl.objectives.Objective;
import me.jetp250.goapimpl.objectives.Objective.Priority;
import me.jetp250.goapimpl.objectives.Objectives.WeightedObjectiveList;
import me.jetp250.goapimpl.utilities.MathHelper;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.EnchantmentManager;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.ItemAxe;
import net.minecraft.server.v1_12_R1.Items;

public class GOAPController {

	private static final Comparator<Objective> OBJECTIVE_PRIORITY_COMPARATOR;

	private final Human entity;
	private final Objective[] objectives;
	private final WeightedObjectiveList objectiveList;
	private final Equipment equipment;
	private Objective currentObjective;
	private boolean sorted;

	public GOAPController(final Human entity, final int maxObjectives, final WeightedObjectiveList objectives) {
		this.entity = entity;
		this.objectives = new Objective[maxObjectives];
		this.equipment = new Equipment(maxObjectives, entity.getInventory());
		this.objectiveList = objectives;
	}

	public boolean breakBlock(final int x, final int y, final int z, final boolean updatePhysics) {
		return breakBlock(new BlockPosition(x, y, z), updatePhysics);
	}

	public Equipment getEquipment() {
		return this.equipment;
	}

	public boolean breakBlock(final BlockPosition pos, final boolean updatePhysics) {
		entity.world.methodProfiler.a("break block");
		final IBlockData type = entity.world.getType(pos);
		final boolean canBreak = this.equipment.canBreakBlock(type.getBlock());
		if (canBreak) {
			entity.world.setTypeAndData(pos, Blocks.AIR.getBlockData(), updatePhysics ? 3 : 2);
			entity.lastPitch = -80F;
			entity.pitch = entity.lastPitch;
			entity.getControllerLook().a(pos.getX(), entity.locX - 4, pos.getZ(), 30, 30);
			final ToolType toolType = ToolType.getToolFor(type.getBlock());
			if (toolType != null) {
				final ItemStack tool = this.equipment.getTool(toolType);
				if (tool != null) {
					tool.setDurability((short) (tool.getDurability() + 1));
					if (tool.getDurability() <= 0) {
						this.equipment.update();
					}
				}
			}
			entity.world.methodProfiler.b();
			return true;
		}
		entity.world.methodProfiler.b();
		return false;
	}

	public void attack(final EntityLiving target) {
		if (target == null) {
			return;
		}
		final ItemStack weapon = this.equipment.getWeapon(entity.h(target), target);
		if (weapon == null) {
			attack(target, 1);
			return;
		}
		this.entity.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		attack(target, this.entity.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue());
	}

	public void attack(final Entity target, final ItemStack weapon) {
		if (weapon == null) {
			attack(target, 1);
			return;
		}
		weapon.setDurability((short) (weapon.getDurability() + 1));
		this.entity.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		attack(target, this.entity.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue());
	}

	// TODO Clean up directly copied NMS source
	public void attack(final Entity target, final double damage) {
		float f = (float) damage;
		int i = 0;
		if (target instanceof EntityLiving) {
			f += EnchantmentManager.a(entity.getItemInMainHand(), ((EntityLiving) target).getMonsterType());
			i += EnchantmentManager.b(entity);
		}
		final boolean flag = target.damageEntity(DamageSource.mobAttack(entity), f);
		if (flag) {
			if (i > 0 && target instanceof EntityLiving) {
				((EntityLiving) target).a(entity, i
						* 0.5f, MathHelper.sin(entity.yaw * 0.017453292f), -MathHelper.cos(entity.yaw * 0.017453292f));
				entity.motX *= 0.6;
				entity.motZ *= 0.6;
			}
			final int j = EnchantmentManager.getFireAspectEnchantmentLevel(entity);
			if (j > 0) {
				final EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(entity.getBukkitEntity(), target.getBukkitEntity(), j
						* 4);
				Bukkit.getPluginManager().callEvent(combustEvent);
				if (!combustEvent.isCancelled()) {
					target.setOnFire(combustEvent.getDuration());
				}
			}
			if (target instanceof EntityHuman) {
				final EntityHuman targethuman = (EntityHuman) target;
				final net.minecraft.server.v1_12_R1.ItemStack itemstack = entity.getItemInMainHand();
				final net.minecraft.server.v1_12_R1.ItemStack itemstack2 = targethuman.isHandRaised() ? targethuman.cJ() : net.minecraft.server.v1_12_R1.ItemStack.a;
				if (!itemstack.isEmpty() && !itemstack2.isEmpty() && itemstack.getItem() instanceof ItemAxe
						&& itemstack2.getItem() == Items.SHIELD) {
					final float f2 = 0.25f + EnchantmentManager.getDigSpeedEnchantmentLevel(entity) * 0.05f;
					if (entity.getRandom().nextFloat() < f2) {
						targethuman.getCooldownTracker().a(Items.SHIELD, 100);
						entity.world.broadcastEntityEffect(targethuman, (byte) 30);
					}
				}
			}
			//			            entity.a(entity, target);
		}
	}

	public void update() {
		entity.world.methodProfiler.a("controller update");
		if (this.currentObjective == null) {
			this.pickNewCurrentObjective();
			return;
		}
		if (!this.currentObjective.active()) {
			this.currentObjective.checkAndDo();
			entity.world.methodProfiler.b();
			return;
		}
		this.currentObjective.update();
		this.currentObjective.ticks++;
		if (this.currentObjective.completed()) {
			this.abandonCurrentObjective();
		}
		entity.world.methodProfiler.b();
	}

	public void pickNewCurrentObjective() {
		if (this.currentObjective != null) {
			return;
		}
		Objective objective = this.getObjective();
		if (objective == null) {
			objective = this.objectiveList.pickAndCreateRandom(this.entity);
		} else {
			this.removeObjective(objective);
		}
		this.currentObjective = objective;
	}

	@Nullable
	public Objective getObjective() {
		if (this.objectives.length != 0 && (!this.sorted || this.objectives[0] == null)) {
			Arrays.sort(this.objectives, GOAPController.OBJECTIVE_PRIORITY_COMPARATOR);
			this.sorted = true;
		}
		return this.objectives[0];
	}

	public boolean addObjective(final Objective objective) {
		if (objective.getPriority().isHigherThan(this.currentObjective.getPriority())) {
			final Objective prevCurrent = this.currentObjective();
			this.currentObjective = objective;
			return this.addObjective(prevCurrent);
		}
		for (int i = 0; i < this.objectives.length; ++i) {
			final Objective toCompare = this.objectives[i];
			if (toCompare == null || objective.getPriority().isHigherOrEqualTo(toCompare.getPriority())) {
				final int index = i;
				for (i = this.objectives.length - 1; i > index; --i) {
					this.objectives[i] = this.objectives[i - 1];
				}
				this.objectives[index] = objective;
				return true;
			}
		}
		return false;
	}

	public void setCurrentObjective(final Objective objective) {
		final Objective current = this.currentObjective;
		if (current == null) {
			return;
		}
		current.setPriority(Priority.HIGHEST);
		this.currentObjective = objective;
		for (int i = 1; i < this.objectives.length; ++i) {
			this.objectives[i] = this.objectives[i - 1];
		}
		this.objectives[0] = current;
	}

	public boolean addObjective(final Objective objective, final Priority priority) {
		objective.setPriority(priority);
		return this.addObjective(objective);
	}

	public void abandonCurrentObjective() {
		this.currentObjective = null;
	}

	public void removeObjective(final Objective objective) {
		for (int i = 0; i < this.objectives.length; ++i) {
			final Objective next = this.objectives[i];
			if (objective.equals(next)) {
				this.objectives[i] = null;
				for (; i < this.objectives.length - 1; ++i) {
					this.objectives[i] = this.objectives[i + 1];
				}
				return;
			}
		}
	}

	public int removeIf(final Predicate<? super Objective> predicate) {
		int matched = 0;
		for (int i = 0; i < this.objectives.length; ++i) {
			if (predicate.test(this.objectives[i])) {
				this.objectives[i] = null;
				matched++;
			}
		}
		return matched;
	}

	@Nullable
	public Objective currentObjective() {
		return this.currentObjective;
	}

	static {
		OBJECTIVE_PRIORITY_COMPARATOR = (o1, o2) -> {
			if (o1 == null && o2 == null) {
				return 0;
			}
			if (o1 == null) {
				return 1;
			}
			if (o2 == null) {
				return -1;
			}
			return o1.compareTo(o2);
		};
	}

}
