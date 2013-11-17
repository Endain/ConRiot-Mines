package net.conriot.sona.mines;

import org.bukkit.plugin.java.JavaPlugin;

public class Mines extends JavaPlugin {
	private MineManager mines;
	
	@Override
	public void onEnable() {
		// Instantiate the only map
		this.mines = new MineManager(this);
		// Register commands for this map
		getCommand("mine").setExecutor(new Commands(this.mines));
	}
	
	@Override
	public void onDisable() {
		// Nothing to do here
	}
}
