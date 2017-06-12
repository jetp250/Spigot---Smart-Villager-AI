package me.jetp250.goapimpl.general;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import me.jetp250.goapimpl.objectives.Objective;
import me.jetp250.goapimpl.objectives.Objective.Priority;
import me.jetp250.goapimpl.objectives.Objectives.WeightedObjectiveList;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.IBlockData;

public class GOAPController {

	private static final Comparator<Objective> OBJECTIVE_PRIORITY_COMPARATOR;

	private final EntityCreature entity;
	private final Objective[] objectives;
	private final WeightedObjectiveList objectiveList;
	private final Equipment equipment;
	private Objective currentObjective;
	private boolean sorted;

	public GOAPController(final EntityCreature entity, final int maxObjectives, final WeightedObjectiveList objectives) {
		this.entity = entity;
		this.objectives = new Objective[maxObjectives];
		this.equipment = new Equipment(maxObjectives);
		this.objectiveList = objectives;
	}

	public boolean breakBlock(final int x, final int y, final int z, final boolean updatePhysics) {
		return breakBlock(new BlockPosition(x, y, z), updatePhysics);
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
			entity.world.methodProfiler.b();
			return true;
		}
		entity.world.methodProfiler.b();
		return false;
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
			if (objective.getPriority().isHigherOrEqualTo(toCompare.getPriority())) {
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
