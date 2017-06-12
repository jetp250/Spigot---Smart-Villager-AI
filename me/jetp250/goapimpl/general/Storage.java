package me.jetp250.goapimpl.general;

import org.bukkit.Material;

import net.minecraft.server.v1_12_R1.TileEntityChest;

public class Storage<T> extends org.bukkit.craftbukkit.v1_12_R1.block.CraftChest {

	public Storage(final Material material, final TileEntityChest te) {
		super(material, te);
	}

}
