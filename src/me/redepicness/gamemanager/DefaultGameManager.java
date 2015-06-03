package me.redepicness.gamemanager;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public abstract class DefaultGameManager implements Listener{

    public DefaultGameManager(){
        Bukkit.getServer().getPluginManager().registerEvents(this, GameManager.getInstance());
    }

    @EventHandler
    public void onUnload(ChunkUnloadEvent e){
        if(!canUnload(e.getChunk())) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockChange(BlockBreakEvent e){
        if(getArenaFromSign(e.getBlock()) != null) e.setCancelled(true);
    }

    public abstract boolean canUnload(Chunk chunk);

    public abstract void cleanup();

    public abstract Arena getArenaFromSign(Block sign);

    public Location getLobbyLocation(){
        return new Location(Bukkit.getWorld("world"), 0, 100, 0);
    }

    public abstract void registerArena(Block sign);

}
