package me.jetp250.goapimpl.objectives;

import java.util.function.Predicate;

import me.jetp250.goapimpl.utilities.MathHelper;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.NavigationAbstract;

public class ObjectiveWalkToPoint extends Objective {

	private final Predicate<? super Block> breakIf;
	private final double x, y, z;
	private final double speed;
	private final double minDistance;
	private boolean completed;
	private boolean pathfinding;

	public ObjectiveWalkToPoint(final Priority priority, final EntityCreature entity, final BlockPosition target,
			final double speed, final double minDistance, final Predicate<? super Block> breakIf) {
		this(priority, entity, target, speed, minDistance, breakIf, (e) -> true);
	}

	public ObjectiveWalkToPoint(final Priority priority, final EntityCreature entity, final BlockPosition target,
			final double speed, final double minDistance, final Predicate<? super Block> breakIf,
			final Predicate<? super EntityCreature> startIf) {
		super(priority, entity, startIf);
		this.breakIf = breakIf;
		this.x = target.getX() + 0.5;
		this.y = target.getY() + 0.5;
		this.z = target.getZ() + 0.5;
		this.speed = speed;
		this.minDistance = minDistance;
	}

	@Override
	public boolean start() {
		final EntityCreature entity = this.getEntity();
		boolean success = false;
		success |= entity.getNavigation().a(this.x, this.y, this.z, this.speed);
		if (!success && this.breakBlocksInFront()) {
			success |= entity.getNavigation().a(this.x, this.y, this.z, this.speed);
		}
		this.completed = success;
		return true;
	}

	public boolean breakBlocksInFront() {
		final EntityCreature entity = this.getEntity();
		double dx = this.x - entity.locX;
		double dy = this.y - entity.locY;
		double dz = this.z - entity.locZ;
		final double n = Math.sqrt(dx * dx + dy * dy + dz * dz);
		dx /= n;
		dy /= n;
		dz /= n;
		BlockPosition front = new BlockPosition(entity.locX + dx, entity.locY + dy, entity.locZ + dz);
		Block type = entity.world.getType(front).getBlock();
		if (this.breakIf.test(type)) {
			entity.world.setTypeAndData(front, Blocks.AIR.getBlockData(), 2);
		} else if (this.breakIf.test(type = entity.world.getType(front = front.up()).getBlock())) {
			entity.world.setTypeAndData(front, Blocks.AIR.getBlockData(), 2);
		} else if (this.breakIf.test(type = entity.world.getType(front = front.up()).getBlock())) {
			entity.world.setTypeAndData(front, Blocks.AIR.getBlockData(), 2);
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean completed() {
		return this.completed;
	}

	@Override
	public void update() {
		final EntityCreature entity = this.getEntity();
		final double d = MathHelper.distSqr(entity.locX, entity.locZ, this.x, this.z);
		if (d < this.minDistance) {
			this.completed = true;
			return;
		}
		if (this.pathfinding && this.ticks % 6 == 0) {
			return;
		}
		final NavigationAbstract navigation = entity.getNavigation();
		this.pathfinding = navigation.a(this.x, this.y, this.z, this.speed);
		if (!this.pathfinding) {
			this.breakBlocksInFront();
			this.pathfinding = navigation.a(this.x, this.y, this.z, this.speed);
		}
	}

	@Override
	protected void stop() {
	}

}
