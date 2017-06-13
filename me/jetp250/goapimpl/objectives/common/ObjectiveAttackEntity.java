package me.jetp250.goapimpl.objectives.common;

import java.util.List;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import me.jetp250.goapimpl.entities.Human;
import me.jetp250.goapimpl.objectives.Objective;
import me.jetp250.goapimpl.utilities.Debug;
import me.jetp250.goapimpl.utilities.MathHelper;
import net.minecraft.server.v1_12_R1.EntityCreeper;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityMonster;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumHand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutWorldParticles;

public class ObjectiveAttackEntity extends Objective {

	private final Human entity;
	private EntityLiving target;
	private boolean completed;
	private int ticksOnTarget;

	public ObjectiveAttackEntity(Priority priority, Human entity, final EntityMonster target) {
		super(priority, entity, (e) -> true);
		this.target = target;
		this.entity = entity;
	}

	@Override
	public boolean start() {
		if (this.target == null) {
			this.target = this.entity.getGoalTarget();
		}
		if (target == null) {
			return false;
		}
		if (target.dead || (target.getHealth() < 1 && target.isBurning())) {
			return false;
		}
		return true;
	}

	@Override
	public boolean completed() {
		return completed;
	}

	@Override
	public void update() {
		Debug.b("Updating");
		if (this.entity.getGoalTarget() != null) {
			final EntityLiving target = entity.getGoalTarget();
			if (!target.equals(this.target)) {
				this.target = target;
				this.ticksOnTarget = 0;
			}
		}
		final double dSqr;
		if (target.dead || ticksOnTarget++ > 500
				|| (dSqr = MathHelper.distSqr(target.locX, target.locZ, entity.locX, entity.locZ)) > 1500
				|| !this.entity.getEntitySenses().a(target)) {
			this.completed = true;
			return;
		}
		if (target instanceof EntityCreeper) {
			final EntityCreeper creeper = (EntityCreeper) target;
			if (creeper.isIgnited()) {
				double tx = target.locX - entity.locX;
				double ty = target.locY - entity.locY;
				double tz = target.locZ - entity.locZ;
				final double dsqrt = 1 / MathHelper.sqrt((float) (tx * tx + ty * ty + tz * tz));
				tx *= dsqrt * 10;
				ty *= dsqrt * 10;
				tz *= dsqrt * 10;
				this.entity.getNavigation().a(tx, ty, tz, 0.8);
				return;
			}
		}
		final ItemStack weapon = this.entity.getController().getEquipment().getWeapon(dSqr, target);
		if (weapon == null && ticks % 4 == 0) {
			this.entity.getController().getEquipment().checkInventory();
		}
		if (weapon == null) {
			// TODO Run to home!
			final double tx = entity.locX - (target.locX - entity.locX);
			final double ty = entity.locY - (target.locY - entity.locY);
			final double tz = entity.locZ - (target.locZ - entity.locZ);
			this.entity.getNavigation().a(tx, ty, tz, 0.8);
			if (this.ticks % 4 == 0) {
				final PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(EnumParticle.VILLAGER_ANGRY, false, (float) entity.locX, (float) entity.locY, (float) entity.locZ, 0.1f, 0.1f, 0.1f, 0.01f, 10, new int[0]);
				final List<EntityHuman> players = this.entity.world.players;
				for (int i = 0; i < players.size(); ++i) {
					final EntityPlayer player = (EntityPlayer) players.get(i);
					if (this.entity.h(player) < 16384) {
						player.playerConnection.sendPacket(particles);
					}
				}
			}
			return;
		}
		if (dSqr < 2) {
			double tx = target.locX - entity.locX;
			double ty = target.locY - entity.locY;
			double tz = target.locZ - entity.locZ;
			final double dsqrt = 1 / MathHelper.sqrt((float) (tx * tx + ty * ty + tz * tz));
			tx *= dsqrt;
			ty *= dsqrt;
			tz *= dsqrt;
			this.entity.getNavigation().a(entity.locX - tx * 2, entity.locY - ty * 2, entity.locZ - tz * 2, 0.4);
		} else if (dSqr < 5) {
			entity.lastPitch = -80F;
			entity.pitch = entity.lastPitch;
			entity.getControllerLook().a(target.locX, entity.locX - 4, target.locZ, 30, 30);
			this.entity.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
			this.entity.b(EnumHand.MAIN_HAND);
			this.entity.getController().attack(target, weapon);
		} else {
			final boolean b = this.entity.getNavigation().a(target.locX, target.locY, target.locZ, speed(dSqr));
			if (!b) {
				this.completed = true;
				return;
			}
		}
		this.entity.getControllerLook().a(target, 30.0f, 30.0f);
	}

	public double speed(double dSqr) {
		dSqr *= 0.09;
		return dSqr < 0.4 ? 0.4 : dSqr > 0.9 ? 0.9 : dSqr;
	}

	@Override
	protected void stop() {
		// TODO Auto-generated method stub

	}

}
