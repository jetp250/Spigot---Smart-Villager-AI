package me.jetp250.goapimpl.objectives;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.jetp250.goapimpl.entities.Human;
import me.jetp250.goapimpl.utilities.Debug;
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
	private boolean completed;
	private BlockPosition tree;
	private BlockPosition nearestAir;
	private boolean pathfinding;
	private BlockPosition[] treeBlocks;
	private List<BlockPosition> pillar;
	private final Human human;
	final EnumDirection[] directions = EnumDirection.values();

	public ObjectiveCutTrees(final Priority priority, final EntityCreature entity) {
		super(priority, entity, (e) -> e instanceof Human);
		this.completed = false;
		this.human = entity instanceof Human ? (Human) entity : null;
	}

	@Override
	public void update() {
		final Human entity = this.human;
		final double d = MathHelper.distSqr(this.tree.getX(), this.tree.getZ(), entity.locX, entity.locZ);
		if (d < 2) {
			entity.getNavigation().p();
			if (this.pillar != null && this.treeBlocks == null) {
				entity.getNavigation().p();
				entity.lastPitch = -80F;
				entity.pitch = entity.lastPitch;
				entity.motX = 0;
				entity.motZ = 0;
				if (this.ticks % 3 != 0) {
					return;
				}
				for (int i = this.pillar.size() - 1; i > -1; --i) {
					final BlockPosition next = this.pillar.get(i);
					if (next != null) {
						entity.world.setTypeAndData(next, Blocks.AIR.getBlockData(), 2);
						this.pillar.remove(i);
						return;
					}
				}
				this.completed = true;
				this.pillar = null;
			} else if (this.treeBlocks != null) {
				if (this.ticks % 2 != 0) {
					return;
				}
				for (int i = 0; i < this.treeBlocks.length; ++i) {
					final BlockPosition next = this.treeBlocks[i];
					if (next != null) {
						if (next.getY() > entity.getY() + 2) {
							if (MathHelper.distSqr(next.getX(), next.getZ(), entity.locX, entity.locZ) > 1) {
								boolean success = this.pathfinding = entity.getNavigation().a(next.getX(), entity.getY(), next.getZ(), 0.45);
								if (!success) {
									this.breakBlocksInFront();
									success |= this.pathfinding = entity.getNavigation().a(next.getX(), entity.getY(), next.getZ(), 0.45);
									if (!success) {
										if (MathHelper.distSqr(next.getX(), next.getZ(), entity.locX, entity.locZ) > 1) {
											entity.setPosition(next.getX() + 0.5, entity.locY, next.getZ() + 0.5);
										}
									}
								}
							}
							final BlockPosition eLoc = new BlockPosition(entity);
							if (entity.world.getType(eLoc).getBlock() != Blocks.AIR) {
								entity.setPosition(eLoc.getX() + 0.5D, eLoc.getY() + 1, eLoc.getZ() + 0.5D);
							}
							entity.motY = 0.5;
							if (this.pillar == null) {
								this.pillar = new ArrayList<>(this.treeBlocks[this.treeBlocks.length - 1].getY() - eLoc.getY()
										- 1);
							}
							this.pillar.add(new BlockPosition(entity));
							entity.world.setTypeAndData(eLoc, Blocks.DIRT.getBlockData(), 3);
							return;
						}
						entity.lastPitch = 80;
						entity.pitch = 80;
						this.treeBlocks[i] = null;
						this.pathfinding = false;
						final IBlockData block = entity.world.getType(next);
						entity.getInventory().addItem(new ItemStack(Material.LOG, 1, (short) block.getBlock().toLegacyData(block)));
						entity.world.setTypeAndData(next, Blocks.AIR.getBlockData(), 3);
						return;
					}
				}
				this.treeBlocks = null;
				this.completed = this.pillar == null;
				if (!this.completed) {
					Collections.sort(this.pillar, (o1,
							o2) -> o1 == null && o2 == null ? 0 : o1 == null ? 1 : o2 == null ? -1 : o1.getY() - o2.getY());
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
		} else {
			if (d < 15 && this.ticks % 3 == 0) {
				this.breakBlocksInFront();
			} else {
				final double hd = MathHelper.distSqr(this.tree.getX(), this.tree.getZ(), entity.locX, entity.locZ);
				if (hd < 2 && entity.locX - this.tree.getX() > 2) {
					final BlockPosition ePos = new BlockPosition(entity).down();
					final Block block = entity.world.getType(ePos).getBlock();
					if (block == Blocks.LEAVES || block == Blocks.LOG || block == Blocks.LOG2) {
						entity.world.setTypeAndData(ePos, Blocks.AIR.getBlockData(), 3);
						entity.pitch = -80F;
						entity.lastPitch = -80F;
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

	public void breakBlocksInFront() {
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
		for (final EnumDirection direction : this.directions) {
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
		final int radius = ObjectiveCutTrees.SEARCH_RADIUS / 2;
		final int yRad = radius / 2;
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
								Debug.b("Above wasn't log");
								continue;
							} else {
								final int highestY = entity.world.c(pos.getX(), pos.getZ()) - 1;
								final BlockPosition location = new BlockPosition(pos.getX(), highestY, pos.getZ());
								b = entity.world.getType(location).getBlock();
								if (b != Blocks.LEAVES && b != Blocks.LEAVES2) {
									Debug.b("Expected leaves, got: " + b.getName() + " at "
											+ String.format("X: %d, Y: %d, Z: %d", location.getX(), location.getY(), location.getZ()));
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
		if (!b) {
			entity.getNavigation().a(this.nearestAir.getX(), this.nearestAir.getY(), this.nearestAir.getZ(), 0.6);
		}
		return true;
	}

	private void reset() {
		this.tree = null;
		this.completed = false;
	}

	@Override
	public boolean completed() {
		return this.completed;
	}

	@Override
	public void stop() {

	}

}