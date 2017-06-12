package me.jetp250.goapimpl.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;

import me.jetp250.goapimpl.GOAPImplMain;
import me.jetp250.goapimpl.entities.Human;
import me.jetp250.goapimpl.utilities.MathHelper;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;

public class Village {

	private static int LAST_ID;
	private static final HashMap<Integer, Village> VILLAGES;

	private final List<Human> villagers;
	private final int locX, locZ;
	private final WorldServer world;
	private final int id;

	public Village(final double locX, final double locZ, final World world) {
		this.villagers = new ArrayList<>();
		this.locX = (int) locX;
		this.locZ = (int) locZ;
		this.world = (WorldServer) world;
		this.id = Village.LAST_ID++ + 1;
		Village.VILLAGES.put(this.id, this);
		Bukkit.getScheduler().runTask(GOAPImplMain.getPlugin(), () -> {
			Bukkit.broadcastMessage("\u00a7aVillage formed! Residents: " + this.villagers.size() + ", #" + this.id);
			Bukkit.broadcastMessage("\u00a7aVillage location: " + locX + ", " + world.c(this.locX, this.locZ) + ", " + locZ);
		});
	}

	public Village(final BlockPosition location, final World world) {
		this(location.getX(), location.getZ(), world);
	}

	public int getId() {
		return this.id;
	}

	public WorldServer getWorld() {
		return this.world;
	}

	public int getX() {
		return this.locX;
	}

	public int getZ() {
		return this.locZ;
	}

	public List<Human> getVillagers() {
		return this.villagers;
	}

	public static Collection<Village> getVillages() {
		return Village.VILLAGES.values();
	}

	public static Village getById(final int id) {
		return Village.VILLAGES.get(id);
	}

	public static Village getNearestVillage(final double x, final double z) {
		Village nearest = null;
		double distance = Double.MAX_VALUE;
		final Collection<Village> villages = Village.VILLAGES.values();
		for (final Village village : villages) {
			final double dSqr = MathHelper.distSqr(x, z, village.getX(), village.getZ());
			if (dSqr < distance) {
				nearest = village;
				distance = dSqr;
			}
		}
		return nearest;
	}

	static {
		VILLAGES = new HashMap<>();
	}

}