package me.jetp250.goapimpl.utilities;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityLiving;

public final class MobUtility {

	public static boolean a(final Entity entity, @Nullable final EntityLiving living, final ReversedEntitySenses senses,
			final boolean flag, final boolean flag1) {
		if (living == null || entity == living || !living.isAlive()) {
			return false;
		}
		if (entity.r(living)) {
			return false;
		} else if (living instanceof EntityHuman && !flag && ((EntityHuman) living).abilities.isInvulnerable) {
			return false;
		}
		return !flag1 || senses.a(living);
	}

	public static class ReversedEntitySenses {
		private final Entity a;
		private final List<EntityLiving> b;
		private final List<EntityLiving> c;

		public ReversedEntitySenses(final Entity a) {
			this.b = Lists.newArrayList();
			this.c = Lists.newArrayList();
			this.a = a;
		}

		public void a() {
			this.b.clear();
			this.c.clear();
		}

		public boolean a(final EntityLiving entity) {
			if (this.b.contains(entity)) {
				return true;
			}
			if (this.c.contains(entity)) {
				return false;
			}
			this.a.world.methodProfiler.a("canSee");
			final boolean hasLineOfSight = entity.hasLineOfSight(this.a);
			this.a.world.methodProfiler.b();
			if (hasLineOfSight) {
				this.b.add(entity);
			} else {
				this.c.add(entity);
			}
			return hasLineOfSight;
		}
	}
}
