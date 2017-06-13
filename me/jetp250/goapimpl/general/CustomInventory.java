package me.jetp250.goapimpl.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryCustom;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.jetp250.goapimpl.utilities.Inventory;

public class CustomInventory extends CraftInventoryCustom implements me.jetp250.goapimpl.utilities.Inventory<ItemStack> {

	public CustomInventory(InventoryHolder owner, int size) {
		super(owner, size);
	}

	@Override
	public boolean addItem(ItemStack item) {
		super.addItem(item);
		return true;
	}

	@Override
	public Class<ItemStack> getGenericType() {
		return ItemStack.class;
	}

	@Override
	public boolean addAll(ItemStack... items) {
		boolean success = true;
		for (int i = 0; i < items.length; ++i) {
			if (!this.addItem(items[i])) {
				success = false;
			}
		}
		return success;
	}

	@Override
	public boolean addAll(Collection<ItemStack> collection) {
		boolean failed = false;
		if (collection.size() > 36 && collection instanceof RandomAccess) {
			final List<ItemStack> list = (List<ItemStack>) collection;
			for (int i = 0; i < collection.size(); ++i) {
				failed |= !this.addItem(list.get(i));
			}
		} else {
			for (final ItemStack stack : collection) {
				failed |= !this.addItem(stack);
			}
		}
		return !failed;
	}

	@Override
	public boolean replaceFirst(final @Nullable ItemStack type, final ItemStack stack) {
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (next == null && type == null || next != null && next.getType() == type.getType() && type.equals(next)) {
				this.setItem(i, stack);
				return true;
			}
		}
		return false;
	}

	@Override
	public int replaceAll(final ItemStack type, final @Nullable ItemStack replacement) {
		int matched = 0;
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (next != null && type.equals(next)) {
				this.setItem(i, replacement);
				matched++;
			}
		}
		return matched;
	}

	@Override
	public int count(final @Nullable ItemStack type, final boolean countAmounts) {
		int matched = 0;
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (next == null && type == null || next != null && next.equals(type)) {
				matched += countAmounts && next != null ? next.getAmount() : 1;
			}
		}
		return matched;
	}

	@Override
	public boolean isFull() {
		return this.firstEmpty() == -1;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack item = this.getItem(i);
			if (item != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int countAllItems(final boolean countAmounts) {
		int counted = 0;
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (next != null) {
				counted += countAmounts ? next.getAmount() : 1;
			}
		}
		return counted;
	}

	@Override
	public int emptySlots() {
		int empty = 0;
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack item = this.getItem(i);
			if (item == null) {
				empty++;
			}
		}
		return empty;
	}

	@Override
	public void remove(final int index) {
		if (index < 0 || index > this.getSize() - 1) {
			return;
		}
		this.setItem(index, null);
	}

	@Override
	public void remove(final ItemStack item) {
		this.replaceFirst(item, null);
	}

	@Override
	public void remove(final ItemStack type, int amount) {
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (next != null && next.equals(type)) {
				if (next.getAmount() < amount) {
					final int count = next.getAmount();
					amount -= count;
					this.setItem(i, null);
					if (amount == 0) {
						return;
					}
					continue;
				}
				next.setAmount(next.getAmount() - amount);
				return;
			}
		}
	}

	@Override
	public void remove(final int index, final int amount) {
		if (index < 0 || index > this.getSize() - 1) {
			return;
		}
		final ItemStack item = this.getItem(index);
		if (item == null) {
			return;
		}
		if (item.getAmount() < amount) {
			return;
		}
		item.setAmount(item.getAmount() - amount);
	}

	@Override
	public void removeAll(final ItemStack item) {
		this.replaceAll(item, null);
	}

	@Override
	public void removeIf(final Predicate<? super ItemStack> predicate) {
		for (int i = 0; i < this.getSize(); ++i) {
			if (predicate.test(this.getItem(i))) {
				this.setItem(i, null);
			}
		}
	}

	@Override
	public boolean contains(final Predicate<? super ItemStack> predicate) {
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (predicate.test(next)) {
				return true;
			}
		}
		return false;
	}

	@Override
	@Nullable
	public ItemStack getFirst(final Predicate<? super ItemStack> predicate) {
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (predicate.test(next)) {
				return next;
			}
		}
		return null;
	}

	@Override
	public List<ItemStack> getAll(final Predicate<? super ItemStack> predicate) {
		final List<ItemStack> list = new ArrayList<>();
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (predicate.test(next)) {
				list.add(next);
			}
		}
		return list;
	}

	@Override
	public void fill(final ItemStack toFillWith) {
		this.fill(toFillWith, 0, this.getSize());
	}

	@Override
	public void fill(final ItemStack toFillWith, final int start, final int end) {
		if (start < 0 || start > end || start > this.getSize()) {
			throw new IllegalArgumentException("illegal start index: " + start);
		}
		if (end > this.getSize()) {
			throw new IllegalArgumentException("end index cannot be bigger than inventory size");
		}
		for (int i = start; i < end; ++i) {
			this.setItem(i, toFillWith);
		}
	}

	@Override
	public ItemStack[] getItems(final int start, final int end) {
		final ItemStack[] array = new ItemStack[end - start];
		for (int i = start; i < end; ++i) {
			array[i - start] = this.getItem(i);
		}
		return array;
	}

	@Override
	public boolean containsAtLeast(final int amount, final ItemStack type) {
		int found = 0;
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (next != null && next.getType() == type.getType() && next.equals(type)) {
				found += next.getAmount();
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
		for (int i = 0; i < this.getSize(); ++i) {
			final ItemStack next = this.getItem(i);
			if (next != null && next.equals(type)) {
				list.add(next);
			}
		}
		return list;
	}

	@Override
	public void sort(final @Nullable Comparator<ItemStack> comparator) {
		final ItemStack[] contents = this.getContents();
		if (comparator == null) {
			Arrays.sort(contents);
		} else {
			Arrays.sort(contents, comparator);
		}
		this.setContents(contents);
	}

	@Override
	public Inventory<ItemStack> clone() {
		final CustomInventory clone = new CustomInventory(this.getHolder(), this.getSize());
		clone.setContents(this.getContents());
		return clone;
	}

	@Override
	public void copyTo(me.jetp250.goapimpl.utilities.Inventory.ResizeableInventory<ItemStack> other) {
		if (other.getSize() >= this.getSize()) {
			other.setContents(this.getContents());
		} else {
			final ItemStack[] cut = Arrays.copyOfRange(this.getContents(), 0, other.getSize());
			other.setContents(cut);
		}
	}

}
