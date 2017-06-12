package me.jetp250.goapimpl.utilities;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public interface Inventory<E> extends Iterable<E> {

	public Class<?> getGenericType();

	public E getItem(final int index);

	public boolean addItem(final E item);

	public boolean addItem(final E item, final boolean resize);

	public boolean addAll(@SuppressWarnings("unchecked") final E... items);

	public boolean addAll(final Collection<E> collection);

	public boolean setItem(final int index, final E stack);

	public int size();

	public boolean replaceFirst(final @Nullable E type, final E replacement);

	public int replaceAll(final E type, final @Nullable E replacement);

	public void clear();

	public int count(final @Nullable E type, final boolean countAmounts);

	public int firstEmpty();

	public boolean isFull();

	public boolean isEmpty();

	public int countAllItems(final boolean countAmounts);

	public int emptySlots();

	public boolean remove(final int index);

	public boolean remove(final E item);

	public boolean remove(final E type, final int amount);

	public boolean remove(final int index, final int amount);

	public boolean removeAll(final E item);

	public void removeIf(final Predicate<? super E> predicate);

	public boolean contains(final Predicate<? super E> predicate);

	public E getFirst(final Predicate<? super E> predicate);

	public Collection<E> getAll(final Predicate<? super E> predicate);

	public void fill(final E item);

	public void fill(final E item, final int start, final int end);

	public E[] getItems(final int start, final int end);

	public boolean containsAtLeast(final int amount, final E type);

	public List<E> getAllOfType(final E type);

	public void resize(final int rows);

	public void resize(final int rows, final boolean keepContents);

	public void sort(final @Nullable Comparator<E> comparator);

	public E[] getContents();

	public Inventory<E> clone();

	public void copyTo(final ResizeableInventory<E> other);

	public static interface ResizeableInventory<E> extends Inventory<E> {

		public void setContents(final E[] contents);

		public void copyFrom(final Inventory<E> other);

	}

}
