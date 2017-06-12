package me.jetp250.goapimpl.general;

import java.lang.reflect.Field;
import java.util.Arrays;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
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
	private final EnumToolMaterial[] toolTypes;
	private ItemStack itemInHand;
	private boolean sortWeapons;

	public Equipment(final int maxWeapons) {
		this.toolbelt = new ItemStack[4];
		this.toolTypes = new EnumToolMaterial[4];
		this.weapons = new ItemStack[maxWeapons];
		this.damageValues = new double[weapons.length + toolbelt.length];
	}

	public boolean addWeapon(final ItemStack weapon, final boolean dropOld) {
		int index = -1;
		for (int i = 0; i < this.weapons.length; ++i) {
			if (weapons[i] == null) {
				weapons[i] = weapon;
				index = i;
				break;
			}
		}
		if (index != -1 || sortWeapons) {
			Arrays.sort(weapons, (o1, o2) -> ToolType.SWORD.compare(o1.getType(), o2.getType()));
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

	public ItemStack getWeapon(final double distanceSquared, final @Nullable EntityLiving target) {
		if (this.itemInHand != null) {
			final ItemStack mainHand = this.itemInHand;
			this.itemInHand = null;
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
		if (this.toolbelt.length == 0) {
			return null;
		}
		ItemStack tool = this.toolbelt[0];
		double dmg = -1;
		for (int i = 0; i < this.toolbelt.length; ++i) {
			final double next = this.damageValues[i];
			if (next > dmg) {
				dmg = next;
				tool = toolbelt[i];
			}
		}
		for (int i = 0; i < this.weapons.length; ++i) {
			final double next = this.damageValues[i + this.weapons.length];
			if (next > dmg) {
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
		return this.itemInHand = weapons[0];
	}

	@Nullable
	public ItemStack getToolFor(final Block block) {
		final ToolType type = ToolType.getToolFor(block);
		if (type != null) {
			return this.itemInHand = this.toolbelt[type.ordinal()];
		}
		return this.itemInHand = null;
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
		return this.itemInHand = this.toolbelt[type.ordinal()];
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
