package me.jetp250.goapimpl.general;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.Item.EnumToolMaterial;

public enum ToolType {
	SWORD {
		@Override
		EnumToolMaterial getType(final Material item) {
			switch (item) {
				case WOOD_SWORD:
					return EnumToolMaterial.WOOD;
				case STONE_SWORD:
					return EnumToolMaterial.STONE;
				case GOLD_SWORD:
					return EnumToolMaterial.STONE;
				case IRON_SWORD:
					return EnumToolMaterial.IRON;
				case DIAMOND_SWORD:
					return EnumToolMaterial.DIAMOND;
				default:
					return null;
			}
		}

		@Override
		protected boolean canBreak0(final Block type, final EnumToolMaterial etm) {
			return type == Blocks.WEB;
		}

		/**
		 * Returns 1 if a is better b, 0 if a is equal to b and -1 if a is worse than b.
		 */
		@Override
		int compare(final Material a, final Material b) {
			final EnumToolMaterial am = getType(a);
			final EnumToolMaterial bm = getType(b);
			switch (am) {
				case GOLD:
				case WOOD: {
					if (bm == EnumToolMaterial.WOOD || bm == EnumToolMaterial.WOOD) {
						return 0;
					}
					return 1;
				}
				case STONE: {
					if (bm == EnumToolMaterial.GOLD || bm == EnumToolMaterial.WOOD) {
						return -1;
					}
					return bm == EnumToolMaterial.STONE ? 0 : 1;
				}
				case IRON: {
					if (bm == EnumToolMaterial.GOLD || bm == EnumToolMaterial.WOOD || bm == EnumToolMaterial.STONE) {
						return -1;
					}
					return bm == EnumToolMaterial.IRON ? 0 : 1;
				}
				default: {
					if (bm == EnumToolMaterial.GOLD || bm == EnumToolMaterial.WOOD || bm == EnumToolMaterial.STONE
							|| bm == EnumToolMaterial.IRON) {
						return -1;
					}
					return 0;
				}
			}
		}
	},
	AXE {
		@Override
		EnumToolMaterial getType(final Material item) {
			switch (item) {
				case WOOD_AXE:
					return EnumToolMaterial.WOOD;
				case STONE_AXE:
					return EnumToolMaterial.STONE;
				case GOLD_AXE:
					return EnumToolMaterial.STONE;
				case IRON_AXE:
					return EnumToolMaterial.IRON;
				case DIAMOND_AXE:
					return EnumToolMaterial.DIAMOND;
				default:
					return null;
			}
		}

		@Override
		protected boolean canBreak0(final Block type, final EnumToolMaterial etm) {
			return false;
		}

		@Override
		int compare(final Material a, final Material b) {
			// TODO Auto-generated method stub
			return 0;
		}
	},
	PICKAXE {
		@Override
		EnumToolMaterial getType(final Material item) {
			switch (item) {
				case WOOD_PICKAXE:
					return EnumToolMaterial.WOOD;
				case STONE_PICKAXE:
					return EnumToolMaterial.STONE;
				case GOLD_PICKAXE:
					return EnumToolMaterial.STONE;
				case IRON_PICKAXE:
					return EnumToolMaterial.IRON;
				case DIAMOND_PICKAXE:
					return EnumToolMaterial.DIAMOND;
				default:
					return null;
			}
		}

		@Override
		protected boolean canBreak0(final Block type, final EnumToolMaterial etm) {
			if (type == Blocks.OBSIDIAN) {
				return etm == EnumToolMaterial.DIAMOND;
			}
			if (type == Blocks.DIAMOND_BLOCK || type == Blocks.EMERALD_BLOCK || type == Blocks.GOLD_BLOCK
					|| type == Blocks.DIAMOND_ORE || type == Blocks.EMERALD_ORE || type == Blocks.GOLD_ORE
					|| type == Blocks.REDSTONE_ORE) {
				return etm == EnumToolMaterial.IRON || etm == EnumToolMaterial.DIAMOND;
			}
			if (type == Blocks.IRON_BLOCK || type == Blocks.IRON_ORE || type == Blocks.LAPIS_BLOCK || type == Blocks.LAPIS_ORE) {
				return etm == EnumToolMaterial.STONE || etm == EnumToolMaterial.IRON || etm == EnumToolMaterial.DIAMOND;
			}
			if (type == Blocks.STONE || type == Blocks.ENDER_CHEST || type == Blocks.ANVIL || type == Blocks.COAL_BLOCK
					|| type == Blocks.REDSTONE_BLOCK || type == Blocks.ENCHANTING_TABLE || type == Blocks.IRON_BARS
					|| type == Blocks.IRON_DOOR || type == Blocks.MOB_SPAWNER || type == Blocks.DISPENSER
					|| type == Blocks.DROPPER || type == Blocks.FURNACE || type == Blocks.COAL_ORE || type == Blocks.END_STONE
					|| type == Blocks.HOPPER || type == Blocks.IRON_TRAPDOOR || type == Blocks.QUARTZ_ORE
					|| type == Blocks.BRICK_STAIRS || type == Blocks.BRICK_BLOCK || type == Blocks.cauldron
					|| type == Blocks.COBBLESTONE || type == Blocks.STONE_STAIRS || type == Blocks.COBBLESTONE_WALL
					|| type == Blocks.MOSSY_COBBLESTONE || type == Blocks.NETHER_BRICK || type == Blocks.NETHER_BRICK_FENCE
					|| type == Blocks.NETHER_BRICK_STAIRS || type == Blocks.STONE_SLAB || type == Blocks.PRISMARINE
					|| type == Blocks.HARDENED_CLAY || type == Blocks.STAINED_HARDENED_CLAY || type == Blocks.QUARTZ_BLOCK
					|| type == Blocks.QUARTZ_STAIRS || type == Blocks.SANDSTONE || type == Blocks.SANDSTONE_STAIRS
					|| type == Blocks.BREWING_STAND || type == Blocks.NETHERRACK) {
				return true;
			}
			return false;
		}

		// TODO
		@Override
		int compare(final Material a, final Material b) {
			return 0;
		}
	},
	SHOVEL {
		@Override
		EnumToolMaterial getType(final Material item) {
			switch (item) {
				case WOOD_SPADE:
					return EnumToolMaterial.WOOD;
				case STONE_SPADE:
					return EnumToolMaterial.STONE;
				case GOLD_SPADE:
					return EnumToolMaterial.STONE;
				case IRON_SPADE:
					return EnumToolMaterial.IRON;
				case DIAMOND_SPADE:
					return EnumToolMaterial.DIAMOND;
				default:
					return null;
			}
		}

		@Override
		protected boolean canBreak0(final Block type, final EnumToolMaterial etm) {
			return false;
		}

		@Override
		int compare(final Material a, final Material b) {
			// TODO Auto-generated method stub
			return 0;
		}
	},
	HOE {
		@Override
		EnumToolMaterial getType(final Material item) {
			switch (item) {
				case WOOD_HOE:
					return EnumToolMaterial.WOOD;
				case STONE_HOE:
					return EnumToolMaterial.STONE;
				case GOLD_HOE:
					return EnumToolMaterial.STONE;
				case IRON_HOE:
					return EnumToolMaterial.IRON;
				case DIAMOND_HOE:
					return EnumToolMaterial.DIAMOND;
				default:
					return null;
			}
		}

		@Override
		protected boolean canBreak0(final Block type, final EnumToolMaterial etm) {
			return false;
		}

		@Override
		int compare(final Material a, final Material b) {
			return 0;
		}
	};

	abstract EnumToolMaterial getType(final Material item);

	protected abstract boolean canBreak0(final Block type, final EnumToolMaterial etm);

	abstract int compare(final Material a, final Material b);

	public boolean canBreak(final Block type, final EnumToolMaterial etm) {
		final net.minecraft.server.v1_12_R1.Material material = type.getBlockData().getMaterial();
		if (material.isAlwaysDestroyable()) {
			return true;
		}
		return !material.isLiquid() && canBreak0(type, etm);
	}

	public static ToolType ofItem(final ItemStack item) {
		if (item == null) {
			return null;
		}
		return ToolType.ofMaterial(item.getType());
	}

	public static ToolType ofMaterial(final Material type) {
		if (type == null) {
			return null;
		}
		final ToolType[] values = ToolType.values();
		for (final ToolType tt : values) {
			if (tt.getType(type) != null) {
				return tt;
			}
		}
		return null;
	}

	public static ToolType getToolFor(final Block block) {
		final net.minecraft.server.v1_12_R1.Material mat = block.getBlockData().getMaterial();
		if (block == Blocks.LOG || block == Blocks.LOG2 || mat == net.minecraft.server.v1_12_R1.Material.WOOD
				|| mat == net.minecraft.server.v1_12_R1.Material.PUMPKIN
				|| mat == net.minecraft.server.v1_12_R1.Material.BANNER) {
			return ToolType.AXE;
		}
		if (mat == net.minecraft.server.v1_12_R1.Material.STONE || mat == net.minecraft.server.v1_12_R1.Material.HEAVY
				|| mat == net.minecraft.server.v1_12_R1.Material.ORE) {
			return ToolType.AXE;
		}
		if (mat == net.minecraft.server.v1_12_R1.Material.EARTH || mat == net.minecraft.server.v1_12_R1.Material.GRASS
				|| mat == net.minecraft.server.v1_12_R1.Material.CLAY || mat == net.minecraft.server.v1_12_R1.Material.SAND) {
			return ToolType.SHOVEL;
		}
		return block == Blocks.FARMLAND || mat == net.minecraft.server.v1_12_R1.Material.PLANT ? ToolType.HOE : null;
	}

}
