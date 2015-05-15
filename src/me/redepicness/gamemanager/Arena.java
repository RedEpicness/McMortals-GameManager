package me.redepicness.gamemanager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class Arena{

    public static Location NEXT_SPAWN_LOC = new Location(Bukkit.getWorld("world"), 100, 100, 100);

    public static Location getNextSpawn(){
        return NEXT_SPAWN_LOC.add(100, 0, 0);
    }

    private Location arenaSpawn;
    private ArrayList<String> players = new ArrayList<>();
    /*private Game game;*/
    private Block sign;

    public Arena(/*Location arenaSpawn, Game game, */Block sign){
        /*this.arenaSpawn = arenaSpawn;
        this.game = game;*/
        arenaSpawn = getNextSpawn();
        this.sign = sign;
        updateSign(0, getName());
    }

    public void broadcast(String message){
        for(String name : players){
            Bukkit.getPlayer(name).sendMessage(message);
        }
    }

    public int getPlayerCount(){
        return players.size();
    }

    public ArrayList<String> getPlayers() {
        return (ArrayList<String>) players.clone();
    }

    public boolean isJoinable(){
        return true;
    }

    public boolean isInArena(String name){
        return players.contains(name);
    }

    public void join(Player player){
        players.add(player.getName());
        updateSign(2, getPlayerCount()+"/"+getMaxPlayers());
    }

    public void leave(Player player){
        players.remove(player.getName());
        updateSign(2, getPlayerCount() + "/" + getMaxPlayers());
        player.teleport(GameManager.getInstance().getGameManager().getLobbyLocation());
    }

    public Sign getSign(){
        return (Sign) sign.getState();
    }

    public abstract int getMaxPlayers();

    public abstract String getName();

    public void updateSign(int line, String message){
        Sign s = getSign();
        s.setLine(line, message);
        System.out.println(message == null);
        s.update();
    }

    public Location getArenaSpawn() {
        return arenaSpawn;
    }
}
