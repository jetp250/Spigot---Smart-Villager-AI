package me.jetp250.goapimpl.objectives;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EntityCreature;

public class ObjectiveBuildPath extends Objective {

	public ObjectiveBuildPath(final Objective.Priority priority, final EntityCreature entity, final Block pathType,
			final BlockPosition to) {
		super(priority, entity, (e) -> true);
		new ObjectiveBuildPath(Priority.HIGH, entity, pathType, to);
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean completed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void stop() {
		// TODO Auto-generated method stub

	}

}
