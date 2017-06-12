package me.jetp250.goapimpl.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import me.jetp250.goapimpl.utilities.NMSUtils.Attributes;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;

public class ItemBuilder extends ItemStack {

	public static enum Slot {

		HELMET("head", 0),
		CHEST("chest", 1),
		LEGGINGS("legs", 2),
		BOOTS("feet", 3),
		NONE("", -1),
		MAINHAND("mainhand", 4),
		OFFHAND("offhand", 5);

		public static Slot fromMaterial(final Material input) {
			final ItemStack toCompare = new ItemStack(input);
			if (EnchantmentTarget.ARMOR_FEET.includes(toCompare)) {
				return Slot.BOOTS;
			} else if (EnchantmentTarget.ARMOR_HEAD.includes(toCompare)) {
				return Slot.HELMET;
			} else if (EnchantmentTarget.ARMOR_LEGS.includes(toCompare)) {
				return Slot.LEGGINGS;
			} else if (EnchantmentTarget.ARMOR_TORSO.includes(toCompare)) {
				return Slot.CHEST;
			}
			return Slot.MAINHAND;
		}

		private int id;

		private String name;

		Slot(final String name, final int uid) {
			this.name = name;
			this.id = uid;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}
	}

	private final Random random = new Random();

	private Recipe recipe;

	private final List<Recipe> recipes;

	public ItemBuilder(final ItemStack stack) {
		this(stack.getType(), stack.getAmount(), stack.getDurability());
		this.setData(stack.getData());
		if (stack.getItemMeta() != null) {
			this.setItemMeta(stack.getItemMeta());
		}
		if (this.getItemMeta() == null) {
			this.setItemMeta(Bukkit.getServer().getItemFactory().getItemMeta(this.getType()));
		}
	}

	public ItemBuilder(final Material m) {
		this(m, 1);
	}

	public ItemBuilder(final Material m, final int amount) {
		this(m, amount, 0);
	}

	public ItemBuilder(final Material m, final int amount, final int data) {
		super(m, amount, (byte) data);
		this.recipes = new ArrayList<>();
	}

	public ItemBuilder addEnchant(final Enchantment enchantment, final int level) {
		this.addEnchant(enchantment, level, false);
		return this;
	}

	public ItemBuilder addEnchant(final Enchantment enchantment, final int level, final boolean force) {
		final ItemMeta meta = this.getItemMeta();
		meta.addEnchant(enchantment, level, force);
		this.setItemMeta(meta);
		return this;
	}

	public ItemBuilder addItemFlags(final ItemFlag... flags) {
		final ItemMeta meta = this.getItemMeta();
		meta.addItemFlags(flags);
		this.setItemMeta(meta);
		return this;
	}

	public ItemBuilder addLore(final String... lines) {
		final List<String> lore = this.getLore();
		for (final String s : lines) {
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		final ItemMeta meta = this.getItemMeta();
		meta.setLore(lore);
		this.setItemMeta(meta);
		return this;
	}

	public ItemBuilder addRecipeIngredient(final char c, final Material data) {
		if (this.recipe == null) {
			this.createShapedRecipe();
		}
		if (this.recipe instanceof ShapedRecipe) {
			((ShapedRecipe) this.recipe).setIngredient(c, data);
		} else {
			((ShapelessRecipe) this.recipe).addIngredient(data);
		}
		return this;
	}

	public ItemBuilder addRecipeIngredient(final char c, final MaterialData data) {
		if (this.recipe == null) {
			this.createShapedRecipe();
		}
		if (this.recipe instanceof ShapedRecipe) {
			((ShapedRecipe) this.recipe).setIngredient(c, data);
		} else {
			((ShapelessRecipe) this.recipe).addIngredient(data);
		}
		return this;
	}

	public ItemBuilder addRecipeIngredient(final Material data) {
		if (this.recipe == null) {
			this.createShapelessRecipe();
		}
		if (this.recipe instanceof ShapelessRecipe) {
			((ShapelessRecipe) this.recipe).addIngredient(data);
		}
		return this;
	}

	public ItemBuilder addRecipeIngredient(final MaterialData data) {
		if (this.recipe instanceof ShapelessRecipe) {
			((ShapelessRecipe) this.recipe).addIngredient(data);
		}
		return this;
	}

	public ItemBuilder clearEnchantments() {
		final ItemMeta meta = this.getItemMeta();
		for (final Enchantment e : meta.getEnchants().keySet()) {
			meta.removeEnchant(e);
		}
		this.setItemMeta(meta);
		return this;
	}

	public ItemBuilder clearLore() {
		final ItemMeta meta = this.getItemMeta();
		meta.setLore(new ArrayList<String>());
		this.setItemMeta(meta);
		return this;
	}

	//FIXME
	@SuppressWarnings("deprecation")
	public ItemBuilder createShapedRecipe() {
		this.saveRecipe();
		this.recipe = new ShapedRecipe(this);
		return this;
	}

	//FIXME
	@SuppressWarnings("deprecation")
	public ItemBuilder createShapelessRecipe() {
		this.saveRecipe();
		this.recipe = new ShapelessRecipe(this);
		return this;
	}

	public List<String> getLore() {
		return this.getItemMeta().hasLore() ? this.getItemMeta().getLore() : new ArrayList<>();
	}

	public Recipe getRecipe() {
		return this.recipe == null ? this.recipes.size() == 0 ? null : this.recipes.get(0) : this.recipe;
	}

	public Recipe getRecipeAt(final int index) {
		return index >= this.recipes.size() - 1 ? this.recipes.get(this.recipes.size() - 1) : this.recipes.get(index);
	}

	public List<Recipe> getRecipes() {
		return this.recipes;
	}

	public ItemBuilder removeEnchant(final Enchantment enchantment) {
		if (this.getItemMeta().hasEnchants() && this.getItemMeta().getEnchants().containsKey(enchantment)) {
			return this;
		}
		final ItemMeta meta = this.getItemMeta();
		meta.removeEnchant(enchantment);
		this.setItemMeta(meta);
		return this;
	}

	public ItemBuilder saveRecipe() {
		if (this.recipe != null) {
			this.recipes.add(this.recipe);
		}
		return this;
	}

	public ItemBuilder setAttribute(final Attributes attr, final double value) {
		this.setAttribute(attr, value, Slot.fromMaterial(this.getType()));
		return this;
	}

	public ItemBuilder setAttribute(final Attributes attr, final double value, final Slot slot) {
		this.setAttribute(attr, value, slot, false);
		return this;
	}

	public ItemBuilder setAttribute(final Attributes attr, final double value, final Slot slot, final boolean operation) {
		this.setAttribute(attr, value, slot, operation, 20000 + this.random.nextInt(80000), this.random.nextInt(30000));
		return this;
	}

	public ItemBuilder setAttribute(final Attributes attr, final double value, final Slot slot, final boolean operation,
			final int uuidLeast, final int uuidMost) {
		final net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(this);
		final NBTTagCompound compound = nmsStack.hasTag() ? nmsStack.getTag() : new NBTTagCompound();
		final NBTTagList modifiers = compound.getList("AttributeModifiers", 10);
		final NBTTagCompound attribute = new NBTTagCompound();
		attribute.setString("AttributeName", attr.getName());
		attribute.setString("Slot", slot.getName());
		attribute.setString("Name", attr.getName());
		attribute.setDouble("Amount", value);
		attribute.setInt("Operation", operation ? 1 : 0);
		attribute.setInt("UUIDLeast", 894654 + uuidLeast);
		attribute.setInt("UUIDMost", 2872 + uuidMost);
		modifiers.add(attribute);
		compound.set("AttributeModifiers", modifiers);
		nmsStack.setTag(compound);

		final ItemMeta newMeta = CraftItemStack.getItemMeta(nmsStack);
		this.setItemMeta(newMeta);
		return this;
	}

	public ItemBuilder setDisplayname(final String displayname) {
		final ItemMeta meta = this.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayname));
		this.setItemMeta(meta);
		return this;
	}

	public ItemBuilder setLore(int line, final String text) {
		final List<String> lore = this.getLore();
		line = line + 1 > lore.size() ? lore.size() : line + 1;
		lore.set(line, ChatColor.translateAlternateColorCodes('&', text));
		final ItemMeta meta = this.getItemMeta();
		meta.setLore(lore);
		this.setItemMeta(meta);
		return this;
	}

	public ItemBuilder setLore(final String... lines) {
		final List<String> lore = new ArrayList<>();
		for (final String s : lines) {
			lore.add(ChatColor.translateAlternateColorCodes('&', s));
		}
		final ItemMeta meta = this.getItemMeta();
		meta.setLore(lore);
		this.setItemMeta(meta);
		return this;
	}

	public ItemBuilder setMaterial(final Material type) {
		this.setType(type);
		return this;
	}

	public ItemBuilder setRecipe(final Recipe recipe) {
		this.recipe = recipe;
		return this;
	}

	public ItemBuilder setRecipeShape(final String... shape) {
		if (this.recipe instanceof ShapedRecipe) {
			((ShapedRecipe) this.recipe).shape(shape);
		}
		return this;
	}
}
