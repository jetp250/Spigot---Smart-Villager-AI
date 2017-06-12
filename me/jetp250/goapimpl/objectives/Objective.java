package me.jetp250.goapimpl.objectives;

import java.util.function.Predicate;

import net.minecraft.server.v1_12_R1.EntityCreature;

public abstract class Objective implements Comparable<Objective> {

	private Priority priority;
	private final Predicate<? super EntityCreature> predicate;
	private final EntityCreature entity;
	private boolean running;
	public int ticks;

	public Objective(final Priority priority, final EntityCreature entity, final Predicate<? super EntityCreature> predicate) {
		this.priority = priority;
		this.predicate = predicate;
		this.entity = entity;
		this.running = false;
	}

	public EntityCreature getEntity() {
		return this.entity;
	}

	public boolean isPossibleFor() {
		return this.predicate.test(this.entity);
	}

	public Priority setPriority(final Priority newPriority) {
		if (newPriority == this.priority) {
			return newPriority;
		}
		final Priority old = this.priority;
		this.priority = newPriority;
		return old;
	}

	public void cancel() {
		this.stop();
		this.ticks = 0;
		this.running = false;
	}

	public boolean checkAndDo() {
		if (!this.isPossibleFor()) {
			this.running = false;
			return false;
		}
		this.running = this.start();
		return true;
	}

	public abstract boolean start();

	public abstract boolean completed();

	public abstract void update();

	protected abstract void stop();

	public boolean active() {
		return this.running;
	}

	public Priority getPriority() {
		return this.priority;
	}

	public Predicate<? super EntityCreature> getCondition() {
		return this.predicate;
	}

	@Override
	public String toString() {
		return "Unknown Objective";
	}

	@Override
	public int compareTo(final Objective o) {
		return this.priority.intVal - o.priority.intVal;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Objective)) {
			return false;
		}
		final Objective other = (Objective) obj;
		return this.priority == other.priority && this.predicate.equals(other.predicate);
	}

	@Override
	public int hashCode() {
		return 31 * super.hashCode() ^ this.predicate.hashCode();
	}

	public static Objective getHighestPriorityObjective(final Objective... objectives) {
		int priority = 0;
		Objective res = null;
		for (final Objective next : objectives) {
			if (next.priority.intVal > priority) {
				res = next;
				priority = next.priority.intVal;
			}
		}
		return res;
	}

	public static enum Priority {
		LOWEST(1), LOW(2), NORMAL(3), HIGH(4), HIGHEST(5);

		private final int intVal;

		private Priority(final int intValue) {
			this.intVal = intValue;
		}

		public boolean isHigherThan(final Priority other) {
			return this.intVal > other.intVal;
		}

		public boolean isHigherOrEqualTo(final Priority other) {
			return this.intVal >= other.intVal;
		}

		public boolean isLowerThan(final Priority other) {
			return this.intVal < other.intVal;
		}

		public boolean isLowerOrEqualTo(final Priority other) {
			return this.intVal <= other.intVal;
		}

		public boolean isEqualTo(final Priority other) {
			return this.intVal == other.intVal;
		}

		public int intValue() {
			return this.intVal;
		}

	}

}