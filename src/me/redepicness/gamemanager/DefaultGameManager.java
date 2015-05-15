package me.redepicness.gamemanager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class DefaultGameManager{

    public Arena getArenaFromSign(Block sign){
        throw new RuntimeException("Must be overridden!");
    }

    public Location getLobbyLocation(){
        return new Location(Bukkit.getWorld("world"), 0, 100, 0);
    }

    public void registerArena(Block sign){
        throw new RuntimeException("Must be overridden!");
    }

}
