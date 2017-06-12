package me.jetp250.goapimpl.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.InventoryHolder;

import me.jetp250.goapimpl.utilities.Inventory;
import me.jetp250.goapimpl.utilities.Inventory.ResizeableInventory;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemStack;

public class VillagerInventory implements ResizeableInventory<ItemStack>, Iterable<ItemStack> {
	private ItemStack[] items;

	public VillagerInventory(final int rows) {
		this.items = new ItemStack[rows * 9];
	}

	private VillagerInventory(final ItemStack[] items) {
		this.items = items;
	}

	@Override
	public Class<ItemStack> getGenericType() {
		return ItemStack.class;
	}

	@Override
	public ItemStack getItem(final int index) {
		return index < 0 || index > this.items.length - 1 ? null : this.items[index];
	}

	@Override
	public boolean setItem(final int index, final ItemStack item) {
		if (index < 0 || index > this.items.length - 1) {
			return false;
		}
		this.items[index] = item;
		return true;
	}

	@Override
	public boolean addItem(final ItemStack item) {
		for (int i = 0; i < this.items.length; ++i) {
			final ItemStack next = this.items[i];
			if (next == null) {
				this.items[i] = item;
				return true;
			} else if (next.getItem() == item.getItem()) {
				if (next.getCount() < next.getMaxStackSize() && ItemStack.equals(next, item)) {
					final int free = next.getMaxStackSize() - next.getCount();
					if (free < item.getCount()) {
						next.setCount(next.getCount() + item.getCount());
						return true;
					} else {
						next.setCount(next.getMaxStackSize());
						item.setCount(item.getCount() - free);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean addItem(final ItemStack item, final boolean resizeIfNeeded) {
		for (int i = 0; i < this.items.length; ++i) {
			final ItemStack next = this.items[i];
			if (next == null) {
				this.items[i] = item;
				return true;
			} else if (next.getItem() == item.getItem()) {
				if (next.getCount() < next.getMaxStackSize() && ItemStack.equals(next, item)) {
					final int free = next.getMaxStackSize() - next.getCount();
					if (free < item.getCount()) {
						next.setCount(next.getCount() + item.getCount());
						return true;
					} else {
						next.setCount(next.getMaxStackSize());
						item.setCount(item.getCount() - free);
					}
				}
			}
		}
		final int size = this.size();
		this.resize(Math.max(this.size() / 9 + 1, 6), true);
		if (size == this.size()) {
			return false;
		}
		return this.setItem(this.firstEmpty(), item);
	}

	@Override
	public boolean addAll(final ItemStack... items) {
		boolean success = true;
		for (final ItemStack item : items) {
			success |= this.addItem(item);
		}
		return success;
	}

	@Override
	public boolean addAll(final Collection<ItemStack> collection) {
		boolean success = true;
		if (collection.size() > 36 && collection instanceof RandomAccess) {
			final List<ItemStack> list = (List<ItemStack>) collection;
			for (int i = 0; i < collection.size(); ++i) {
				success |= this.addItem(list.get(i));
			}
		} else {
			for (final ItemStack stack : collection) {
				success |= this.addAll(stack);
			}
		}
		return success;
	}

	public ItemStack getRandom(final Random random, final boolean nonnull) {
		return this.getRandom(random, nonnull, 0, this.size());
	}

	public ItemStack getRandom(final Random random, final boolean nonnull, final int min, final int max) {
		int index = random.nextInt(max - min) + min;
		ItemStack stack = index < 0 || index > this.items.length ? null : this.items[index];
		if (stack == null && nonnull) {
			while (stack == null) {
				index = random.nextInt(max - min) + min;
				stack = index < 0 || index > this.items.length ? null : this.items[index];
			}
		}
		return stack;
	}

	public ItemStack getRandomAndRemove(final Random random) {
		return this.getRandomAndRemove(random, 0, this.size());
	}

	public ItemStack getRandomAndRemove(final Random random, final int min, final int max) {
		int index = random.nextInt(max - min) + min;
		ItemStack stack = index < 0 || index > this.items.length ? null : this.items[index];
		while (stack == null) {
			index = random.nextInt(max - min) + min;
			stack = index < 0 || index > this.items.length ? null : this.items[index];
		}
		this.items[index] = null;
		return stack;
	}

	@Override
	public int size() {
		return this.items.length;
	}

	@Override
	public boolean replaceFirst(final @Nullable ItemStack type, final ItemStack stack) {
		for (int i = 0; i < this.items.length; ++i) {
			final ItemStack next = this.items[i];
			if (next == null && type == null
					|| next != null && next.getItem() == type.getItem() && ItemStack.equals(type, next)) {
				this.items[i] = stack;
				return true;
			}
		}
		return false;
	}

	public boolean replaceFirst(final @Nullable Item type, final ItemStack stack) {
		for (int i = 0; i < this.items.length; ++i) {
			final ItemStack next = this.items[i];
			if (next == null && type == null || next != null && type == next.getItem()) {
				this.items[i] = stack;
				return true;
			}
		}
		return false;
	}

	@Override
	public int replaceAll(final ItemStack type, final @Nullable ItemStack replacement) {
		int matched = 0;
		for (int i = 0; i < this.items.length; ++i) {
			final ItemStack next = this.items[i];
			if (next != null && ItemStack.equals(type, next)) {
				this.items[i] = replacement;
				matched++;
			}
		}
		return matched;
	}

	public int replaceAll(final Item type, final @Nullable ItemStack replacement) {
		int matched = 0;
		for (int i = 0; i < this.items.length; ++i) {
			final ItemStack next = this.items[i];
			if (next != null && next.getItem() == type) {
				this.items[i] = replacement;
				matched++;
			}
		}
		return matched;
	}

	@Override
	public void clear() {
		if (this.size() > 18) {
			this.items = new ItemStack[this.size()];
		} else {
			for (int i = 0; i < this.items.length; ++i) {
				this.items[i] = null;
			}
		}
	}

	@Override
	public int count(final @Nullable ItemStack type, final boolean countAmounts) {
		int matched = 0;
		for (final ItemStack next : this.items) {
			if (next == null && type == null || next != null && ItemStack.equals(next, type)) {
				matched += countAmounts && next != null ? next.getCount() : 1;
			}
		}
		return matched;
	}

	@Override
	public int firstEmpty() {
		for (int i = 0; i < this.items.length; ++i) {
			if (this.items[i] == null) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean isFull() {
		return this.firstEmpty() == -1;
	}

	@Override
	public boolean isEmpty() {
		for (final ItemStack item : this.items) {
			if (item != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int countAllItems(final boolean countAmounts) {
		int counted = 0;
		for (final ItemStack next : this.items) {
			if (next != null) {
				counted += countAmounts ? next.getCount() : 1;
			}
		}
		return counted;
	}

	@Override
	public int emptySlots() {
		int empty = 0;
		for (final ItemStack item : this.items) {
			if (item == null) {
				empty++;
			}
		}
		return empty;
	}

	@Override
	public boolean remove(final int index) {
		if (index < 0 || index > this.items.length - 1) {
			return false;
		}
		this.items[index] = null;
		return true;
	}

	public boolean remove(final Item item) {
		return this.replaceFirst(item, null);
	}

	public boolean removeAll(final Item item) {
		return this.replaceAll(item, null) > 0;
	}

	@Override
	public boolean remove(final ItemStack item) {
		return this.replaceFirst(item, null);
	}

	@Override
	public boolean remove(final int index, final int amount) {
		if (index < 0 || index > this.items.length - 1) {
			return false;
		}
		final ItemStack item = this.items[index];
		if (item == null) {
			return false;
		}
		if (item.getCount() < amount) {
			return false;
		}
		item.setCount(item.getCount() - amount);
		return true;
	}

	public boolean remove(final Item type, int amount) {
		for (int i = 0; i < this.items.length; ++i) {
			final ItemStack next = this.items[i];
			if (next != null && next.getItem() == type) {
				if (next.getCount() < amount) {
					final int count = next.getCount();
					amount -= count;
					this.items[i] = null;
					if (amount == 0) {
						return true;
					}
					continue;
				}
				next.setCount(next.getCount() - amount);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean remove(final ItemStack type, int amount) {
		for (int i = 0; i < this.items.length; ++i) {
			final ItemStack next = this.items[i];
			if (next != null && ItemStack.equals(next, type)) {
				if (next.getCount() < amount) {
					final int count = next.getCount();
					amount -= count;
					this.items[i] = null;
					if (amount == 0) {
						return true;
					}
					continue;
				}
				next.setCount(next.getCount() - amount);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(final ItemStack item) {
		return this.replaceAll(item, null) > 0;
	}

	@Override
	public void removeIf(final Predicate<? super ItemStack> predicate) {
		for (int i = 0; i < this.items.length; ++i) {
			if (predicate.test(this.items[i])) {
				this.items[i] = null;
			}
		}
	}

	@Override
	public boolean contains(final Predicate<? super ItemStack> predicate) {
		for (final ItemStack next : this.items) {
			if (predicate.test(next)) {
				return true;
			}
		}
		return false;
	}

	@Override
	@Nullable
	public ItemStack getFirst(final Predicate<? super ItemStack> predicate) {
		for (final ItemStack next : this.items) {
			if (predicate.test(next)) {
				return next;
			}
		}
		return null;
	}

	@Override
	public List<ItemStack> getAll(final Predicate<? super ItemStack> predicate) {
		final List<ItemStack> list = new ArrayList<>();
		for (final ItemStack next : this.items) {
			if (predicate.test(next)) {
				list.add(next);
			}
		}
		return list;
	}

	@Override
	public void fill(final ItemStack toFillWith) {
		this.fill(toFillWith, 0, this.size());
	}

	@Override
	public void fill(final ItemStack toFillWith, final int start, final int end) {
		if (start < 0 || start > end || start > this.items.length) {
			throw new IllegalArgumentException("illegal start index: " + start);
		}
		if (end > this.items.length) {
			throw new IllegalArgumentException("end index cannot be bigger than inventory size");
		}
		for (int i = start; i < end; ++i) {
			this.items[i] = toFillWith;
		}
	}

	@Override
	public ItemStack[] getItems(final int start, final int end) {
		final ItemStack[] array = new ItemStack[end - start];
		for (int i = start; i < end; ++i) {
			array[i - start] = this.items[i];
		}
		return array;
	}

	public int count(final @Nullable Item type, final boolean countAmounts) {
		int matched = 0;
		for (final ItemStack next : this.items) {
			if (next == null && type == null || next != null && type == next.getItem()) {
				matched += countAmounts && next != null ? next.getCount() : 1;
			}
		}
		return matched;
	}

	@Override
	public boolean containsAtLeast(final int amount, final ItemStack type) {
		int found = 0;
		for (final ItemStack next : this.items) {
			if (next != null && next.getItem() == type.getItem() && ItemStack.equals(next, type)) {
				found += next.getCount();
				if (found >= amount) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsAtLeast(final int amount, final Item type) {
		int found = 0;
		for (final ItemStack next : this.items) {
			if (next != null && next.getItem() == type) {
				found += next.getCount();
				if (found >= amount) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<ItemStack> getAllOfType(final ItemStack type) {
		final List<ItemStack> list = new ArrayList<>();
		for (final ItemStack next : this.items) {
			if (next != null && ItemStack.equals(next, type)) {
				list.add(next);
			}
		}
		return list;
	}

	public List<ItemStack> getAllOfType(final Item type) {
		final List<ItemStack> list = new ArrayList<>();
		for (final ItemStack next : this.items) {
			if (next != null && next.getItem() == type) {
				list.add(next);
			}
		}
		return list;
	}

	@Override
	public void resize(final int rows) {
		this.resize(rows, true);
	}

	@Override
	public void resize(final int rows, final boolean keepContents) {
		if (rows > 6) {
			throw new IllegalArgumentException("inventories can't have more than 6 rows (given: " + rows + ")");
		}
		final ItemStack[] resized = new ItemStack[rows * 9];
		if (keepContents) {
			System.arraycopy(this.items, 0, resized, 0, resized.length < this.size() ? resized.length : this.size());
		}
		this.items = resized;
	}

	@Override
	public void sort(final @Nullable Comparator<ItemStack> comparator) {
		if (comparator == null) {
			Arrays.sort(this.items);
		} else {
			Arrays.sort(this.items, comparator);
		}
	}

	@Override
	public ItemStack[] getContents() {
		return this.items;
	}

	@Override
	public void setContents(final ItemStack[] items) {
		this.items = items;
	}

	@Override
	public VillagerInventory clone() {
		final ItemStack[] cloned = new ItemStack[this.items.length];
		System.arraycopy(this.items, 0, cloned, 0, this.items.length);
		return new VillagerInventory(cloned);
	}

	@Override
	public void copyTo(final ResizeableInventory<ItemStack> other) {
		this.copyTo(other, false);
	}

	public void copyTo(final ResizeableInventory<ItemStack> other, final boolean resize) {
		if (other.size() == this.size() || resize) {
			other.setContents(this.getContents());
		} else {
			final ItemStack[] cut = Arrays.copyOfRange(this.items, 0, other.size());
			other.setContents(cut);
		}
	}

	public org.bukkit.inventory.Inventory convertToBukkit(final @Nullable InventoryHolder holder, final String title) {
		final org.bukkit.inventory.Inventory bukkit = Bukkit.createInventory(holder, this.size(), title);
		final org.bukkit.inventory.ItemStack[] bukkitStackArray = new org.bukkit.inventory.ItemStack[this.size()];
		for (int i = 0; i < this.size(); ++i) {
			final ItemStack next = this.items[i];
			if (next != null) {
				bukkitStackArray[i] = CraftItemStack.asBukkitCopy(this.items[i]);
			}
		}
		bukkit.setContents(bukkitStackArray);
		return bukkit;
	}

	public VillagerInventory copyOf(final org.bukkit.inventory.Inventory inventory) {
		final org.bukkit.inventory.ItemStack[] bContents = inventory.getContents();
		if (bContents.length != this.size()) {
			this.items = new ItemStack[bContents.length];
		}
		for (int i = 0; i < bContents.length; ++i) {
			this.items[i] = CraftItemStack.asNMSCopy(bContents[i]);
		}
		return this;
	}

	@Override
	public void copyFrom(final Inventory<ItemStack> other) {
		this.setContents(other.getContents());
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof VillagerInventory)) {
			return false;
		}
		return Arrays.equals(this.items, ((VillagerInventory) obj).items);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.items);
	}

	@Override
	public void forEach(final Consumer<? super ItemStack> action) {
		for (final ItemStack item : this.items) {
			action.accept(item);
		}
	}

	@Override
	public String toString() {
		return "VillagerInventory-Contents(x" + this.size() + "){" + Arrays.toString(this.items) + "}";
	}

	@Override
	public Iterator<ItemStack> iterator() {
		return new Itr(this.items);
	}

	class Itr implements Iterator<ItemStack> {

		private final ItemStack[] array;
		private int index;

		public Itr(final ItemStack[] array) {
			this.array = array;
			this.index = -1;
		}

		@Override
		public boolean hasNext() {
			return this.index < this.array.length;
		}

		@Override
		public ItemStack next() {
			return this.array[++this.index];
		}

		@Override
		public void remove() {
			this.array[this.index] = null;
		}

		@Override
		public void forEachRemaining(final Consumer<? super ItemStack> action) {
			for (; this.index < this.array.length; ++this.index) {
				action.accept(this.array[this.index]);
			}
		}

	}
}