package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.Arena;
import me.redepicness.gamemanager.api.Utility;
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

import java.util.Collection;
import java.util.HashMap;

public class GGameManager<A extends Arena> implements Listener{

    public GGameManager(){
        if(getType().equals(GameManagerType.MULTI_GAME))
            Bukkit.getServer().getPluginManager().registerEvents(this, GManager.getInstance());
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

    void cleanup() {
        pluginCleanup();
        if(getType().equals(GameManagerType.MULTI_GAME)){
            for(Block block : signToArena.keySet()){
                signToArena.get(block).disable();
                block.setType(Material.AIR);
            }
            signToArena.clear();
        }
    }

    A getArenaFromSign(Block sign) {
        return signToArena.getOrDefault(sign, null);
    }

    void registerArena(Block sign) {
        if(getType().equals(GameManagerType.MULTI_GAME))
            signToArena.put(sign, getArena(sign));
        else
            throw new RuntimeException("Cannot register arena per sign if the manager is not MULTI_GAME");
    }

    //Public methods

    public Collection<A> getArenas(){
        return signToArena.values();
    }

    public Location getLobbyLocation(){
        return Utility.makeLocation(0, 100, 0);
    }

    public void playerJoin(Player player){}

    public void pluginCleanup(){}

    public A getArena(Block sign){
        return null;
    }

    public GameManagerType getType(){
        return null;
    }

    public enum GameManagerType{

        MULTI_GAME, SINGLE_GAME, LOBBY

    }

}
