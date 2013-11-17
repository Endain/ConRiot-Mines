package net.conriot.sona.mines;

import java.util.HashMap;
import java.util.HashSet;

import net.conriot.sona.mysql.IOCallback;
import net.conriot.sona.mysql.MySQL;
import net.conriot.sona.mysql.Query;
import net.conriot.sona.mysql.Result;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

class MineManager implements Listener, IOCallback {
	private Plugin plugin;
	private HashMap<Location, RegenBlock> blocks;
	private HashMap<String, int[]> adders;
	private HashSet<String> removers;
	
	public MineManager(Mines mines) {
		this.plugin = mines;
		this.blocks = new HashMap<Location, RegenBlock>();
		this.adders = new HashMap<String, int[]>();
		this.removers = new HashSet<String>();
		
		// Load all regen-able blocks
		load();
		
		// Register all events
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	// BLOCK BREAK EVENTS
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDestroy(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			// Schedule a block regeneration if it is a regen-able block
			RegenBlock rb = this.blocks.get(event.getBlock().getLocation());
			if(rb != null)
				rb.destroy();
		}
	}
	
	// BLOCK INTERACT EVENTS
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(!event.isCancelled()) {
			// Only handle if a block was clicked
			if(event.getClickedBlock() != null) {
				if(this.adders.containsKey(event.getPlayer().getName()))
					add(event.getClickedBlock().getLocation(), this.adders.get(event.getPlayer().getName()));
				else if(this.removers.contains(event.getPlayer().getName()))
					remove(event.getClickedBlock().getLocation());
			}
		}
	}
	
	// PLAYER LOGOUT EVENTS
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogout(PlayerQuitEvent event) {
		// If a player logs out, remove them from adders/removers
		clearFromTasks(event.getPlayer().getName());
	}
	
	private void regenAll() {
		// Loop over every RegenBlock and force a regen() call
		for(RegenBlock rb : this.blocks.values()) {
			rb.regen();
		}
	}
	

	private void load() {
		// Create a query to load all regen-able blocks
		Query q = MySQL.makeQuery();
		q.setQuery("SELECT * FROM mines");
		
		// Execute query to asynchronously load block data
		MySQL.execute(this, "load", q);
	}
	
	public void setAdder(String name, int[] params) {
		this.removers.remove(name);
		this.adders.put(name, params);
	}
	
	public void setRemover(String name) {
		this.adders.remove(name);
		this.removers.add(name);
	}
	
	public void clearFromTasks(String name) {
		this.removers.remove(name);
		this.adders.remove(name);
	}
	
	public void add(Location loc, int[] params) {
		// Only add the block if it does not already exist
		if(!this.blocks.containsKey(loc)) {
			// Get the block x/y/z
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			
			// Add the block clicked to the list of regen-able blocks and force a regen
			RegenBlock rb = new RegenBlock(this.plugin, loc, params[0], (byte)params[1], params[2], params[3], (params[4] > 0 ? true : false));
			this.blocks.put(loc, rb);
			rb.regen();
			
			// Create a query to add the new block
			Query q = MySQL.makeQuery();
			q.setQuery("INSERT INTO mines VALUES (?,?,?,?,?,?,?,?)");
			q.add(x);
			q.add(y);
			q.add(z);
			q.add(params[0]);
			q.add(params[1]);
			q.add(params[2]);
			q.add(params[3]);
			q.add((params[4] > 0 ? true : false));
			
			// Execute query to asynchronously save new block
			MySQL.execute(this, null, q);
		}
	}
	
	public void remove(Location loc) {
		// Only remove this block is it exists
		if(this.blocks.containsKey(loc)) {
			// Cancel any pending regeneration
			RegenBlock rb = this.blocks.get(loc);
			rb.cancel();
			
			// Remove from list of tracked blocks
			this.blocks.remove(loc);
			
			// Set the block at the given location to air
			loc.getBlock().setType(Material.AIR);
			
			// Create a query to remove the old block
			Query q = MySQL.makeQuery();
			q.setQuery("DELETE FROM mines WHERE x=? AND y=? AND z=?");
			q.add(loc.getBlockX());
			q.add(loc.getBlockY());
			q.add(loc.getBlockZ());
			
			// Execute query to asynchronously delete old block
			MySQL.execute(this, null, q);
		}
	}
	
	@Override
	public void complete(boolean success, Object tag, Result result) {
		if(tag instanceof String && ((String)tag).equals("load")) {
			// Load up all regen-able blocks
			
			while(result.next()) {
				Location loc = new Location(Bukkit.getWorld("world"), (double)((int)result.get(0)), (double)((int)result.get(1)), (double)((int)result.get(2)));
				blocks.put(loc, new RegenBlock(this.plugin, loc, (int)result.get(3), (byte)((int)result.get(4)), (int)result.get(5), (int)result.get(6), (boolean)result.get(7)));
			}
			
			// Force a regeneration of every block
			regenAll();
		}
	}

}
