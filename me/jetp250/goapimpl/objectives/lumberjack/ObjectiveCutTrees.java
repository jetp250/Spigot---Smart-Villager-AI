package me.jetp250.goapimpl.objectives.lumberjack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.jetp250.goapimpl.entities.Human;
import me.jetp250.goapimpl.objectives.Objective;
import me.jetp250.goapimpl.objectives.common.ObjectiveBuildPath;
import me.jetp250.goapimpl.utilities.MathHelper;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.EnumDirection;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;

public class ObjectiveCutTrees extends Objective {

	private static final int SEARCH_RADIUS = 15;
	private static final EnumDirection[] LOOKUP_DIRECTIONS;
	private static final EnumDirection[] HORIZONTAL_DIRECTIONS;
	private boolean completed;
	private final BlockPosition start;
	private BlockPosition tree;
	private BlockPosition nearestAir;
	private boolean pathfinding;
	private int ticksOnTree;
	private int maxTicks;
	private long lastPlace;
	private BlockPosition[] treeBlocks;
	private List<BlockPosition> pillar;
	private Objective ordered;
	private final Human human;

	public ObjectiveCutTrees(final Priority priority, final EntityCreature entity) {
		super(priority, entity, (e) -> e instanceof Human);
		this.completed = false;
		this.start = new BlockPosition(entity);
		this.human = entity instanceof Human ? (Human) entity : null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void update() {
		if (ordered != null && ordered.completed()) {
			ordered = null;
		}
		if (this.tree == null) {
			return;
		}
		final Human entity = this.human;
		if (this.treeBlocks == null && this.pillar != null) {
			if (this.pillar.isEmpty()) {
				this.complete();
				return;
			}
			entity.getNavigation().p();
			entity.motX = 0;
			entity.motZ = 0;
			for (int i = this.pillar.size() - 1; i > -1; --i) {
				final BlockPosition next = this.pillar.get(i);
				Block block = entity.getWorld().getType(next).getBlock();
				if (block == Blocks.AIR || block.getBlockData().getMaterial().isReplaceable()) {
					this.pillar.remove(i);
					continue;
				}
				block = entity.world.getType(next.down()).getBlock();
				if (block == Blocks.AIR || !block.getBlockData().getMaterial().isSolid()
						|| block.getBlockData().getMaterial().isReplaceable()) {
					BlockPosition pos = next;
					for (final EnumDirection direction : ObjectiveCutTrees.HORIZONTAL_DIRECTIONS) {
						block = entity.world.getType(pos = pos.shift(direction)).getBlock();
						if (block != Blocks.AIR && block.getBlockData().getMaterial().isSolid()) {
							final boolean success = entity.getNavigation().a(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ()
									+ 0.5, 0.2);
							if (success) {
								return;
							}
						}
					}
				}
				entity.getController().breakBlock(next, true);
				this.pillar.remove(i);
				return;
			}
		} else if (this.ticksOnTree++ > this.maxTicks) {
			this.start();
			return;
		}
		final double d = MathHelper.distSqr(this.tree.getX(), this.tree.getZ(), entity.locX, entity.locZ);
		if (d < 2) {
			entity.getNavigation().p();
			if (this.treeBlocks != null) {
				if (this.ticks % 2 != 0) {
					return;
				}
				for (int i = 0; i < this.treeBlocks.length; ++i) {
					final BlockPosition next = this.treeBlocks[i];
					if (next != null) {
						final Block type = entity.world.getType(next).getBlock();
						if (type != Blocks.LOG && type != Blocks.LOG2) {
							this.treeBlocks[i] = null;
							continue;
						}
						final double distSqr = MathHelper.distSqr(next.getX(), next.getZ(), entity.locX, entity.locZ);
						if (distSqr > 6) {
							this.tryNavigateTo(next, this.nearestAir != null ? nearestAir : tree, 4);
						} else if (next.getY() > entity.getY() + 2) {
							if (MathHelper.distSqr(next.getX(), next.getZ(), entity.locX, entity.locZ) > 1) {
								this.tryNavigateTo(next, this.tree, 4);
							} else if (!this.pathfinding && this.pillar != null && !this.pillar.isEmpty()) {
								entity.motX = 0;
								entity.motZ = 0;
							}
							if (this.pillar == null) {
								this.pillar = new ArrayList<>();
							}
							BlockPosition eLoc = new BlockPosition(entity.locX, entity.locY - 1, entity.locZ);
							if (entity.world.getType(eLoc).getBlock() == Blocks.AIR) {
								entity.world.setTypeAndData(eLoc, Blocks.DIRT.getBlockData(), 3);
								this.pillar.add(eLoc);
								return;
							}
							eLoc = eLoc.up();
							this.pillar.add(eLoc);
							Block block;
							if ((block = entity.world.getType(eLoc).getBlock()) != Blocks.AIR
									&& block.getBlockData().getMaterial().isSolid()
									&& !block.getBlockData().getMaterial().isReplaceable()) {
								entity.setPosition(eLoc.getX() + 0.5D, eLoc.getY() + 1, eLoc.getZ() + 0.5D);
								return;
							}
							entity.motY = 0.4;
							entity.world.setTypeAndData(eLoc, Blocks.DIRT.getBlockData(), 3);
							return;
						}
						this.treeBlocks[i] = null;
						this.pathfinding = false;
						final IBlockData block = entity.world.getType(next);
						entity.getInventory().addItem(new ItemStack(Material.getMaterial(Block.getId(block.getBlock())), 1, (short) block.getBlock().toLegacyData(block)));
						entity.getController().breakBlock(next, true);
						final long currentTime = System.currentTimeMillis();
						this.maxTicks += (currentTime - this.lastPlace) / 50;
						this.lastPlace = currentTime;
						return;
					}
				}
				this.treeBlocks = null;
				this.completed = this.pillar == null || this.pillar.isEmpty();
				if (!this.completed) {
					Collections.sort(this.pillar, (o1,
							o2) -> o1 == null && o2 == null ? 0 : o1 == null ? 1 : o2 == null ? -1 : o1.getY() - o2.getY());
				} else {
					this.complete();
				}
				return;
			}
			final Set<BlockPosition> blocks = new HashSet<>();
			this.recursiveGetBlocks(entity.world, this.tree, blocks);
			if (blocks.isEmpty()) {
				return;
			}
			this.treeBlocks = blocks.toArray(new BlockPosition[blocks.size()]);
			Arrays.sort(this.treeBlocks, (o1, o2) -> o1.getY() - o2.getY());
			this.maxTicks = 10;
			this.lastPlace = System.currentTimeMillis();
		} else {
			if (d < 15 && this.ticks % 3 == 0) {
				this.breakBlocksInFront(this.tree.getX(), this.tree.getY(), this.tree.getZ());
			} else {
				if (d < 1 && entity.locY - this.tree.getY() > 2) {
					final BlockPosition ePos = new BlockPosition(entity).down();
					final Block block = entity.world.getType(ePos).getBlock();
					if (block == Blocks.LEAVES || block == Blocks.LOG || block == Blocks.LOG2) {
						entity.getController().breakBlock(ePos, true);
					}
				} else {
					if (d < 40 && this.pathfinding && !entity.getNavigation().o()) {
						return;
					}
					final boolean success = entity.getNavigation().a(this.tree.getX(), this.tree.getY(), this.tree.getZ(), 0.6);
					if (!success) {
						if (this.nearestAir != null) {
							this.pathfinding = entity.getNavigation().a(this.nearestAir.getX(), this.nearestAir.getY(), this.nearestAir.getZ(), 0.6);
						}
					}
				}
			}
		}
	}

	protected void tryNavigateTo(final BlockPosition target, final BlockPosition alternative, int attempts) {
		final Human entity = this.human;
		boolean success = this.pathfinding = entity.getNavigation().a(target.getX(), entity.getY(), target.getZ(), 0.45);
		if (!success) {
			this.breakBlocksInFront(target.getX(), entity.getY(), target.getZ());
			success |= this.pathfinding = entity.getNavigation().a(target.getX(), entity.getY(), target.getZ(), 0.45);
		}
		if (!success) {
			this.breakBlocksInFront(alternative.getX(), entity.getY(), alternative.getZ());
			success |= this.pathfinding = entity.getNavigation().a(alternative.getX(), entity.getY(), alternative.getZ(), 0.45);
		}
		if (!success && MathHelper.distSqr(target.getX(), target.getZ(), entity.locX, entity.locZ) > 1) {
			this.human.getController().addObjective(this.ordered = new ObjectiveBuildPath(Priority.HIGH, this.getEntity(), Blocks.DIRT, target));
		}
		if (!success && --attempts > -1) {
			this.tryNavigateTo(target, alternative, attempts);
		}
	}

	public void complete() {
		this.getEntity().getNavigation().a(this.start.getX(), this.start.getY(), this.start.getZ(), 0.8);
		this.reset();
		this.completed = true;
	}

	public void breakBlocksInFront(final double x, final double y, final double z) {
		final Human entity = this.human;
		double dx = this.tree.getX() - entity.locX;
		double dy = this.tree.getY() - entity.locY;
		double dz = this.tree.getZ() - entity.locZ;
		final double n = Math.sqrt(dx * dx + dy * dy + dz * dz);
		dx /= n;
		dy /= n;
		dz /= n;
		BlockPosition front = new BlockPosition(entity.locX + dx, entity.locY + dy, entity.locZ + dz);
		Block type = entity.world.getType(front).getBlock();
		if (type == Blocks.LEAVES || type == Blocks.LEAVES2) {
			entity.world.setTypeAndData(front, Blocks.AIR.getBlockData(), 2);
		} else if ((type = entity.world.getType(front = front.up()).getBlock()) == Blocks.LEAVES || type == Blocks.LEAVES2) {
			entity.world.setTypeAndData(front, Blocks.AIR.getBlockData(), 2);
		} else if ((type = entity.world.getType(front = front.up()).getBlock()) == Blocks.LEAVES || type == Blocks.LEAVES2) {
			entity.world.setTypeAndData(front, Blocks.AIR.getBlockData(), 2);
		}
	}

	public Set<BlockPosition> recursiveGetBlocks(final World world, final BlockPosition pos, final Set<BlockPosition> set) {
		for (final EnumDirection direction : ObjectiveCutTrees.LOOKUP_DIRECTIONS) {
			final BlockPosition shifted = pos.shift(direction);
			if (!set.contains(shifted) && world.getType(shifted).getBlock() == Blocks.LOG) {
				set.add(shifted);
				set.addAll(this.recursiveGetBlocks(world, shifted, set));
			}
		}
		return set;
	}

	@Override
	public String toString() {
		return "CutTrees";
	}

	@Override
	public boolean start() {
		this.reset();
		final Human entity = this.human;
		final WorldServer world = (WorldServer) entity.getWorld();
		final int radius = ObjectiveCutTrees.SEARCH_RADIUS;
		final int yRad = radius / 3;
		Label_001:
		{
			BlockPosition previousAir = null;
			for (int y = -yRad; y <= yRad; ++y) {
				for (int x = -radius; x <= radius; ++x) {
					for (int z = -radius; z <= radius; ++z) {
						final BlockPosition pos = new BlockPosition(entity.locX + x, entity.locY + y, entity.locZ + z);
						final IBlockData data = world.getType(pos);
						if (data.getBlock() == Blocks.LOG || data.getBlock() == Blocks.LOG2) {
							Block b = entity.world.getType(pos.up()).getBlock();
							if (b != Blocks.LOG && b != Blocks.LOG2) {
								continue;
							} else {
								final int highestY = entity.world.c(pos.getX(), pos.getZ()) - 1;
								final BlockPosition location = new BlockPosition(pos.getX(), highestY, pos.getZ());
								b = entity.world.getType(location).getBlock();
								if (b != Blocks.LEAVES && b != Blocks.LEAVES2) {
									continue;
								}
							}
							this.tree = pos;
							if (previousAir != null) {
								while (world.getType(previousAir).getBlock() == Blocks.AIR) {
									previousAir = previousAir.down();
								}
								this.nearestAir = previousAir.up();
							}
							break Label_001;
						}
						if (data.getBlock() == Blocks.AIR) {
							previousAir = pos;
						}
					}
				}
			}
		}
		if (this.tree == null) {
			return false;
		}
		final boolean b = entity.getNavigation().a(this.tree.getX(), this.tree.getY(), this.tree.getZ(), 0.6);
		if (!b && this.nearestAir != null) {
			entity.getNavigation().a(this.nearestAir.getX(), this.nearestAir.getY(), this.nearestAir.getZ(), 0.6);
		}
		return true;
	}

	private void reset() {
		this.tree = null;
		this.ticksOnTree = 0;
		this.pathfinding = false;
		this.pillar = null;
		this.treeBlocks = null;
		this.nearestAir = null;
		this.completed = false;
	}

	@Override
	public boolean completed() {
		return this.completed;
	}

	@Override
	public void stop() {
		this.reset();
	}

	static {
		LOOKUP_DIRECTIONS = EnumDirection.values();
		HORIZONTAL_DIRECTIONS = new EnumDirection[] { EnumDirection.EAST, EnumDirection.NORTH, EnumDirection.SOUTH,
				EnumDirection.WEST };
	}

}
