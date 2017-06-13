package me.jetp250.goapimpl.objectives;

import java.util.Random;

import me.jetp250.goapimpl.objectives.Objective.Priority;
import me.jetp250.goapimpl.objectives.lumberjack.ObjectiveCutTrees;
import net.minecraft.server.v1_12_R1.EntityCreature;

public enum Objectives {

	CUT_TREES(Priority.NORMAL) {
		@Override
		public Objective create(final EntityCreature creature) {
			return new ObjectiveCutTrees(this.getPriority(), creature);
		}
	};

	public abstract Objective create(final EntityCreature creature);

	private final Priority priority;

	private Objectives(final Priority priority) {
		this.priority = priority;
	}

	public Priority getPriority() {
		return this.priority;
	}

	public static class WeightedObjectiveList {

		private final int totalWeight;
		private final Objectives[] objectives;

		public WeightedObjectiveList(final Objectives... objectives) {
			int totalWeight = 0;
			for (final Objectives obj : objectives) {
				totalWeight += obj.priority.intValue() * 2;
			}
			this.totalWeight = totalWeight;
			this.objectives = objectives;
		}

		public int length() {
			return this.objectives.length;
		}

		public int totalWeight() {
			return this.totalWeight;
		}

		public Objectives get(final int index) {
			return this.objectives[index];
		}

		public Objective pickAndCreateRandom(final EntityCreature creature) {
			if (this.objectives.length == 0 || this.totalWeight <= 0) {
				return null;
			}
			if (this.objectives.length == 1) {
				return this.objectives[0].create(creature);
			}
			final Random source = creature.getRandom();
			int totalWeight = source.nextInt(this.totalWeight);
			this.shuffle(source);
			for (final Objectives obj : this.objectives) {
				if ((totalWeight -= obj.priority.intValue() * 2) <= 0) {
					return obj.create(creature);
				}
			}
			return this.objectives[source.nextInt(this.objectives.length)].create(creature);
		}

		private void shuffle(final Random source) {
			int index;
			Objectives temp;
			final Objectives[] array = this.objectives;
			for (int i = array.length - 1; i > 0; i--) {
				index = source.nextInt(i + 1);
				temp = array[index];
				array[index] = array[i];
				array[i] = temp;
			}
		}
	}

}
