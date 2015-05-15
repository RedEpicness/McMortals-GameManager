package me.redepicness.gamemanager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public abstract class Game<T extends Arena> implements Listener{

    private Location lobbySpawn;
    private ArrayList<T> arenas = new ArrayList<>();

    public Game(Location lobbySpawn){
        this.lobbySpawn = lobbySpawn;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("GameManager"));
    }

    public ArrayList<T> getArenas() {
        return arenas;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public abstract T getNewArenaObject(Block var1);

    @EventHandler
    public void interact(PlayerInteractEvent e){
        if(e.getPlayer().isSneaking() && e.getClickedBlock().getType().equals(Material.WALL_SIGN)){
            Sign sign = ((Sign) e.getClickedBlock().getState());
            if(sign.getLine(0).equals("GM") && sign.getLine(1).equals("NEW")){
                arenas.add(getNewArenaObject(e.getClickedBlock()));
                //TODO CREATE ARENA
            }
        }
    }

}
