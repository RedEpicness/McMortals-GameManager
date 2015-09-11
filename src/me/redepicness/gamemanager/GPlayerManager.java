package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class GPlayerManager implements PlayerManager {

    private static Map<String, GameCustomPlayer> cachedData = new HashMap<>();

    GPlayerManager(){
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(Manager.getPlugin("GameManager"), this::checkCachedData, 5 * 60 * 20, 5 * 60 * 20);
    }

    public CustomPlayer get(String name){
        if(cachedData.containsKey(name)) return cachedData.get(name);
        GameCustomPlayer player = new GameCustomPlayer(name);
        if(!player.exists() || !player.isOnline()){
            return player;
        }
        cachedData.put(name, player);
        Util.log(ChatColor.GREEN + "Loaded data for " + player.getFormattedName() + ChatColor.GREEN + " Ranks: " + player.getRanks());
        return player;
    }

    public static void uncache(String name){
        cachedData.remove(name);
    }

    private void checkCachedData(){
        Util.log(ChatColor.RED + "Checking cached data!");
        cachedData.values().stream().forEach(player -> {
            if(!player.isOnline()){
                cachedData.remove(player.getName());
                Util.log(player.getFormattedName() + ChatColor.RED + " not online, removing data!");
            }
        });
        Bukkit.getOnlinePlayers().stream().filter(p -> !cachedData.containsKey(p.getName())).forEach(p -> {
            GameCustomPlayer player = new GameCustomPlayer(p.getName());
            cachedData.put(p.getName(), player);
        });
    }

}
