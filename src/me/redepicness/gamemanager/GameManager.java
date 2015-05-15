package me.redepicness.gamemanager;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GameManager <T extends DefaultGameManager> extends JavaPlugin implements Listener{

    private T gameManager;
    private static GameManager instance;

    @Override
    public void onEnable(){
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);
    }

    public static GameManager getInstance() {
        return instance;
    }

    public void setGameManager(T manager){
        gameManager = manager;
    }

    public T getGameManager(){
        return gameManager;
    }

    @EventHandler
    public void join(PlayerJoinEvent e){
        e.getPlayer().teleport(gameManager.getLobbyLocation());
    }

    @EventHandler
    public void sign(SignChangeEvent e){
        if(e.getLine(0).equals("ARENA")){
            gameManager.registerArena(e.getBlock());
            e.setLine(1, "Setting up...");
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().toString().toLowerCase().contains("sign")){
            Arena arena = gameManager.getArenaFromSign(e.getClickedBlock());
            if(arena == null) return;
            if(arena.isJoinable()){
                if(arena.getPlayerCount() < arena.getMaxPlayers()){
                    arena.join(e.getPlayer());
                }
                else{
                    e.getPlayer().sendMessage(ChatColor.RED+"The game is full!");
                }
            }
            else{
                e.getPlayer().sendMessage(ChatColor.RED+"The game is currently "+arena.getSign().getLine(1).toLowerCase()+"!");
            }
        }
    }

}
