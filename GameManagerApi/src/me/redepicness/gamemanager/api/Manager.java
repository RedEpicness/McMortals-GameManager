package me.redepicness.gamemanager.api;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

public class Manager {

    private static GameManager gameManager = null;
    private static boolean noManagerMode = false;

    public static <T extends GameManager> void setGameManager(T manager){
        if(noManagerMode)
            throw new RuntimeException("This server is running in no GameManager mode!");
        if(gameManager != null){
            HandlerList.unregisterAll(gameManager);
        }
        gameManager = manager;
        Bukkit.getPluginManager().registerEvents(gameManager, Manager.getPlugin("GameManager"));
        System.out.println("Accepted new GameManager - "+manager.getClass().getName());
    }

    public static GameManager getGameManager(){
        if(noManagerMode)
            throw new RuntimeException("This server is running in no GameManager mode!");
        return gameManager;
    }

    public static Plugin getPlugin(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin);
    }

    public static boolean isNoManagerMode(){
        return noManagerMode;
    }

    public static void startNoManagerMode(){
        if (gameManager != null)
            throw new RuntimeException("GameManager was already provided, cannot start no GameManager mode!");
        noManagerMode = true;
    }

}
