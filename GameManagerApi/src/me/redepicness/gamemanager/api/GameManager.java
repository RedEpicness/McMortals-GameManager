package me.redepicness.gamemanager.api;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;

public abstract class GameManager<A extends Arena> implements Listener {

    public GameManager(){
        if(getType().equals(GameManagerType.MULTI_GAME))
            Bukkit.getServer().getPluginManager().registerEvents(this, Manager.getPlugin("GameManager"));
    }

    //Private methods

    @EventHandler
    public void onUnload(ChunkUnloadEvent e){
        if(!canUnload(e.getChunk())) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockChange(BlockBreakEvent e){
        if(getArenaFromSign(e.getBlock()) != null) e.setCancelled(true);
    }

    private HashMap<Block, A> signToArena = new HashMap<>();

    boolean canUnload(Chunk chunk) {
        for(Block block : signToArena.keySet()){
            if(block.getChunk().equals(chunk)){
                return false;
            }
        }
        return true;
    }

    public void cleanup() {
        if(getType().equals(GameManagerType.MULTI_GAME)){
            for(Block block : signToArena.keySet()){
                signToArena.get(block).disable();
                block.setType(Material.AIR);
            }
            signToArena.clear();
        }
    }

    public A getArenaFromSign(Block sign) {
        return signToArena.getOrDefault(sign, null);
    }

    public void registerArena(Block sign) {
        if(getType().equals(GameManagerType.MULTI_GAME))
            signToArena.put(sign, getArena(sign));
        else
            throw new RuntimeException("Cannot register arena per sign if the manager is not MULTI_GAME");
    }

   /* Collection<A> getArenas(){
        return signToArena.values();
    }*/

    //Public methods (to override)

    public abstract Location getLobbyLocation();

    public abstract void playerJoin(Player player);

    public abstract A getArena(Block sign);

    public abstract GameManagerType getType();

    public enum GameManagerType{

        MULTI_GAME, SINGLE_GAME, LOBBY

    }

}
