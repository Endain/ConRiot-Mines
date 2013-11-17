package net.conriot.sona.mines;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

class RegenBlock {
	private Plugin plugin;
	private Location loc;
	private int id;
	private byte data;
	private int min;
	private int max;
	private boolean replace;
	private BukkitTask regen;
	
	public RegenBlock(Plugin plugin, Location loc, int id, byte data, int min, int max, boolean replace) {
		// Store block/regen configuration data
		this.plugin = plugin;
		this.loc = loc;
		this.id = id;
		this.data = data;
		this.min = min;
		this.max = max;
		this.replace = replace;
		this.regen = null;
	}
	
	@SuppressWarnings("deprecation")
	public void regen() {
		// Set the the block back to it's 'regenerated' state 
		this.loc.getBlock().setTypeIdAndData(this.id, this.data, false);
		
		// Set the regen task to null
		this.regen = null;
	}
	
	public void destroy() {
		// If the block should be replaced, replace with bedrock when destroyed
		if(this.replace)
			Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
				@Override
				public void run() {
					loc.getBlock().setType(Material.BEDROCK);
				}
			}, 0);
		
		// Get a random delay between the min and max values to regen the block after
		Random rand = new Random();
		this.regen = Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
			@Override
			public void run() {
				regen();
			}
		}, rand.nextInt(max - min) + min);
	}
	
	public void cancel() {
		// Cancel the regen task if there is one
		if(this.regen != null) {
			this.regen.cancel();
			this.regen = null;
		}
	}
	
	/*
	class RegenTask implements Runnable {
		@Override
		public void run() {
			// Simply call regen()
			regen();
		}
	}
	*/
}
