package me.jetp250.goapimpl.general;

import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.Items;

public enum Craftables {
	LADDER() {
		@Override
		public boolean canCraft(final VillagerInventory inventory) {
			if (inventory.containsAtLeast(7, Items.STICK) || inventory.containsAtLeast(4, Item.getItemOf(Blocks.PLANKS))) {
				return true;
			}
			return inventory.containsAtLeast(1, Item.getItemOf(Blocks.LOG))
					|| inventory.containsAtLeast(1, Item.getItemOf(Blocks.LOG2));
		}

		@Override
		public void craft(final VillagerInventory inventory) {
			boolean removed = false;
			Item item;
			int amount;
			removed |= inventory.remove(item = Items.STICK, amount = 7);
			if (!removed) {
				removed |= inventory.remove(item = Item.getItemOf(Blocks.PLANKS), amount = 4);
			}
			if (!removed) {
				removed |= inventory.remove(item = Item.getItemOf(Blocks.LOG), amount = 1);
			}
			if (!removed) {
				removed |= inventory.remove(item = Item.getItemOf(Blocks.LOG2), amount = 1);
			}
			if (removed) {
				if (inventory.firstEmpty() == -1) {
					inventory.addItem(new ItemStack(item, amount));
				} else {
					inventory.addItem(new ItemStack(Item.getItemOf(Blocks.LADDER), 3));
				}
			}
		}
	};

	public abstract boolean canCraft(final VillagerInventory inventory);

	public abstract void craft(final VillagerInventory inventory);

}
