package me.jetp250.goapimpl;

import org.bukkit.plugin.java.JavaPlugin;

import me.jetp250.goapimpl.commands.VillageCommand;
import me.jetp250.goapimpl.entities.Human;
import me.jetp250.goapimpl.utilities.NMSUtils;
import me.jetp250.goapimpl.utilities.NMSUtils.Biome;
import me.jetp250.goapimpl.utilities.NMSUtils.SpawnData;
import me.jetp250.goapimpl.utilities.NMSUtils.Type;

public class GOAPImplMain extends JavaPlugin {

	@Override
	public void onLoad() {
		NMSUtils.registerEntity("human", Type.VILLAGER, Human.class, false);
		NMSUtils.addRandomSpawn(Type.VILLAGER, new SpawnData(Human.class, 70, 2, 5), Biome.COLLECTION_TAIGA, Biome.COLLECTION_FORESTS_ALL, Biome.COLLECTION_JUNGLE);
	}

	@Override
	public void onEnable() {
		this.getCommand("village").setExecutor(new VillageCommand());
	}

}
