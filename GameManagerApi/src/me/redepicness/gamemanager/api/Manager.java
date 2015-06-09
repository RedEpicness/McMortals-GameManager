package me.redepicness.gamemanager.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Manager {

    private static GameManager gameManager;

    public static <T extends GameManager> void setGameManager(T manager){
        gameManager = manager;
    }

    public static GameManager getGameManager(){
        return gameManager;
    }

    public static Plugin getPlugin(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin);
    }
}
