package me.jetp250.goapimpl.general;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jetp250.goapimpl.utilities.NMSUtils.NBTTagType;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.Item.EnumToolMaterial;
import net.minecraft.server.v1_12_R1.ItemSword;
import net.minecraft.server.v1_12_R1.ItemTool;
import net.minecraft.server.v1_12_R1.Items;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;

public class Equipment {

	private static final Field WEAPON_DMG_FIELD;
	private static final Field TOOL_DMG_FIELD;

	private final ItemStack[] weapons;
	private final ItemStack[] toolbelt;
	private final double[] damageValues;
	private final Inventory inv;
	private final EnumToolMaterial[] toolTypes;
	private ItemStack itemInHand;

	public Equipment(final int maxWeapons, final @Nullable Inventory inventory) {
		this.toolbelt = new ItemStack[5];
		this.toolTypes = new EnumToolMaterial[toolbelt.length];
		this.weapons = new ItemStack[maxWeapons];
		this.inv = inventory;
		this.damageValues = new double[weapons.length + toolbelt.length];
	}

	public boolean addWeapon(final ItemStack weapon, final boolean dropOld) {
		if (weapon == null) {
			return false;
		}
		int index = -1;
		for (int i = 0; i < this.weapons.length; ++i) {
			final ItemStack next = this.weapons[i];
			if (next != null && next.equals(weapon)) {
				return false;
			}
			if (next == null) {
				weapons[i] = weapon;
				index = i;
				break;
			}
		}
		if (index == -1 && dropOld) {
			index = weapons.length - 1;
			while (index-- > -1 && ToolType.SWORD.compare(weapon.getType(), weapons[index].getType()) > -1) {
				weapons[index] = weapon;
				break;
			}
		}
		saveDamageOf(CraftItemStack.asNMSCopy(weapons[index]), index);
		return index != -1;
	}

	private void saveDamageOf(final net.minecraft.server.v1_12_R1.ItemStack stack, final int index) {
		if (stack == null || !stack.hasTag()) {
			damageValues[index] = 1;
			return;
		}
		double dmg = -1;
		if (Equipment.WEAPON_DMG_FIELD != null && stack.getItem() instanceof ItemSword) {
			try {
				Equipment.WEAPON_DMG_FIELD.setAccessible(true);
				dmg = Equipment.WEAPON_DMG_FIELD.getDouble(stack.getItem());
			} catch (IllegalAccessException | IllegalArgumentException ex) {
			}
		} else if (Equipment.TOOL_DMG_FIELD != null && stack.getItem() instanceof ItemTool) {
			try {
				Equipment.TOOL_DMG_FIELD.setAccessible(true);
				dmg = Equipment.TOOL_DMG_FIELD.getDouble(stack.getItem());
			} catch (IllegalAccessException | IllegalArgumentException ex) {
			}
		}
		if (dmg == -1) {
			final NBTTagList list = stack.getTag().getList("AttributeModifiers", NBTTagType.COMPOUND);
			for (int i = 0; i < list.size(); ++i) {
				final NBTTagCompound next = list.get(i);
				if ("generic.attackDamage".equals(next.getString("AttributeName"))) {
					dmg = next.getDouble("Amount");
				}
			}
		}
		damageValues[index] = dmg == -1 ? 1 : dmg;
	}

	private ItemStack setItemInHand(final ItemStack item) {
		if (item != null && this.inv != null) {
			final ItemStack[] contents = inv.getContents();
			for (int i = contents.length - 2; i > -1; --i) {
				contents[i - 1] = contents[i];
			}
			contents[0] = item;
			this.inv.setContents(contents);
		}
		return item;
	}

	public ItemStack getWeapon(final double distanceSquared, final @Nullable EntityLiving target) {
		if (this.itemInHand != null) {
			final ItemStack mainHand = this.itemInHand;
			this.setItemInHand(null);
			return mainHand;
		}
		if (distanceSquared >= 256) {
			final ItemStack bow = getWeaponIfExists(Material.BOW);
			if (bow != null) {
				return bow;
			}
		}
		if (target != null && target.getItemInOffHand().getItem() == Items.SHIELD) {
			final ItemStack axe = this.getWeaponIfExists(ToolType.AXE);
			if (axe != null) {
				return axe;
			}
		}
		final ItemStack preferred = weapons[0];
		if (preferred != null) {
			return preferred;
		}
		ItemStack tool = this.toolbelt[0];
		double dmg = -1;
		if (this.toolbelt != null) {
			for (int i = 0; i < this.toolbelt.length; ++i) {
				final double next = this.damageValues[i];
				if (toolbelt[i] != null && next > dmg) {
					dmg = next;
					tool = toolbelt[i];
				}
			}
		}
		for (int i = 0; i < this.weapons.length; ++i) {
			final double next = this.damageValues[i + this.toolbelt.length - 1];
			if (weapons[i] != null && next > dmg) {
				dmg = next;
				tool = weapons[i];
			}
		}
		return tool;
	}

	public ItemStack getWeaponIfExists(final Material type) {
		for (int i = 0; i < this.weapons.length; ++i) {
			final ItemStack next = weapons[i];
			if (next != null && next.getType() == type) {
				return next;
			}
		}
		return null;
	}

	public ItemStack getWeaponIfExists(final ToolType type) {
		for (int i = 0; i < this.weapons.length; ++i) {
			final ItemStack next = weapons[i];
			if (next != null && type.getType(next.getType()) != null) {
				return next;
			}
		}
		return null;
	}

	@Nullable
	public ItemStack getStrongestWeapon() {
		return this.setItemInHand(weapons[0]);
	}

	@Nullable
	public ItemStack getToolFor(final Block block) {
		final ToolType type = ToolType.getToolFor(block);
		if (type != null) {
			return this.setItemInHand(this.toolbelt[type.ordinal()]);
		}
		return this.setItemInHand(null);
	}

	@Nullable
	public ItemStack getItemInHand() {
		return this.itemInHand;
	}

	public void setTool(final ToolType type, final ItemStack item) {
		this.toolbelt[type.ordinal()] = item;
		if (item == null) {
			this.toolTypes[type.ordinal()] = null;
			return;
		}
		this.toolTypes[type.ordinal()] = type.getType(item.getType());
		saveDamageOf(CraftItemStack.asNMSCopy(item), toolbelt.length + type.ordinal());
	}

	public ItemStack getTool(final ToolType type) {
		return this.setItemInHand(this.toolbelt[type.ordinal()]);
	}

	public EnumToolMaterial getToolMaterial(final ToolType type) {
		return toolTypes[type.ordinal()];
	}

	public boolean canBreakBlock(final Block block) {
		final net.minecraft.server.v1_12_R1.Material type = block.getBlockData().getMaterial();
		if (type.isAlwaysDestroyable()) {
			return true;
		}
		return ToolType.PICKAXE.canBreak(block, this.getToolMaterial(ToolType.PICKAXE));
	}

	public int update() {
		int removed = 0;
		for (int i = 0; i < this.toolbelt.length; ++i) {
			final ItemStack next = this.toolbelt[i];
			if (next != null && next.getDurability() <= 0) {
				this.toolbelt[i] = null;
				for (final int j = i + 1; j < toolbelt.length; ++i) {
					final ItemStack shift = this.toolbelt[j];
					if (shift == null) {
						break;
					}
					this.toolbelt[j - 1] = shift;
				}
				removed++;
			}
		}
		for (int i = 0; i < this.weapons.length; ++i) {
			final ItemStack next = this.weapons[i];
			if (next != null && next.getDurability() <= 0) {
				this.weapons[i] = null;
				for (final int j = i + 1; j < weapons.length; ++i) {
					final ItemStack shift = this.weapons[j];
					if (shift == null) {
						break;
					}
					this.weapons[j - 1] = shift;
				}
				removed++;
			}
		}
		return removed;
	}

	public boolean containsTool(final ItemStack tool) {
		if (tool == null) {
			for (int i = 0; i < this.toolbelt.length; ++i) {
				if (toolbelt[i] == null) {
					return true;
				}
			}
			return false;
		}
		for (int i = 0; i < this.toolbelt.length; ++i) {
			final ItemStack next = this.toolbelt[i];
			if (tool.equals(next)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsWeapon(final ItemStack weapon) {
		if (weapon == null) {
			for (int i = 0; i < this.weapons.length; ++i) {
				if (weapons[i] == null) {
					return true;
				}
			}
			return false;
		}
		for (int i = 0; i < this.weapons.length; ++i) {
			final ItemStack next = this.weapons[i];
			if (weapon.equals(next)) {
				return true;
			}
		}
		return false;
	}

	public boolean contains(final ItemStack item) {
		if (item == null) {
			return false;
		}
		return ToolType.ofItem(item) == ToolType.SWORD ? containsWeapon(item) : containsTool(item);
	}

	public void checkInventory() {
		if (this.inv == null) {
			return;
		}
		final ItemStack[] contents = this.inv.getContents();
		for (int i = 0; i < contents.length; ++i) {
			final ItemStack next = contents[i];
			if (next != null) {
				final ToolType type = ToolType.ofItem(next);
				if (type == null) {
					continue;
				}
				if (type == ToolType.SWORD) {
					this.addWeapon(next, true);
					continue;
				}
				final ItemStack previous = this.getTool(type);
				if (previous == null) {
					this.setTool(type, next);
					continue;
				}
				final int comparison = type.compare(next.getType(), previous.getType());
				if (comparison == 1 || (comparison == 0 && next.getDurability() > previous.getDurability())) {
					this.setTool(type, next);
				}
			}
		}
	}

	static {
		Field wpd = null;
		Field td = null;
		try {
			for (final Field field : ItemSword.class.getDeclaredFields()) {
				if (field.getType() == Float.TYPE) {
					wpd = field;
					break;
				}
			}
			td = ItemTool.class.getDeclaredField("b");
			if (td == null) {
				int floatFields = 0;
				for (final Field field : ItemTool.class.getDeclaredFields()) {
					if (field.getType() == Float.TYPE) {
						if (floatFields++ == 1) {
							td = field;
							break;
						}
					}
				}
			}
		} catch (NoSuchFieldException | SecurityException ex) {
		}
		WEAPON_DMG_FIELD = wpd;
		TOOL_DMG_FIELD = td;
	}
}
