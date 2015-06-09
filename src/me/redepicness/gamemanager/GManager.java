package me.redepicness.gamemanager;

import me.redepicness.gamemanager.GGameManager.GameManagerType;
import me.redepicness.gamemanager.api.Arena;
import me.redepicness.gamemanager.api.Rank;
import me.redepicness.gamemanager.api.ServerStatusManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GManager extends JavaPlugin implements Listener{

    private static GManager instance;

    @Override
    public void onEnable(){
        instance = this;
        Database.init();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new GameServerStatusManager());
        GameScoreboardManager.init();
        GameCustomPlayer.init();
        GameServerStatusManager.updateInit();
        ServerStatusManager.createInventory("serverMenu", ChatColor.RED + "Server Selector");
        //TODO MAKWE THOS LOBBY
        setGameManager(new GGameManager() {
            @Override
            public Location getLobbyLocation() {
                return GameUtility.makeLocation(-29.5, 88.5, 1.5);
            }

            @Override
            public void playerJoin(Player player) {
                player.sendMessage(ChatColor.GREEN+"Welcome to the Tesseract Server!");
                player.getInventory().setItem(0, GameUtility.makeItemStack(Material.ENDER_PEARL, "Server selector", "Right click to use"));
            }

            @Override
            public GameManagerType getType() {
                return GameManagerType.LOBBY;
            }
        });
    }

    @Override
    public void onDisable(){
        gameManager.cleanup();
        gameManager = null;
        Database.end();
    }

    public static GManager getInstance() {
        return instance;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void join(PlayerJoinEvent e){
        e.setJoinMessage(null);
        GameCustomPlayer player = GameCustomPlayer.get(e.getPlayer().getName());
        GameScoreboardManager.getManager().updateStaff();
        if(player.hasPermission(Rank.ADMIN)){
            player.getPlayer().setOp(true);
        }
        else{
            player.getPlayer().setOp(false);
        }
        e.getPlayer().teleport(gameManager.getLobbyLocation());
        if(gameManager.getType().equals(GameManagerType.SINGLE_GAME)){
            gameManager.getArena(null).join(e.getPlayer());
        }
        gameManager.playerJoin(player.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void leave(PlayerQuitEvent e){
        e.setQuitMessage(null);
        GameCustomPlayer.uncache(e.getPlayer().getName());
    }

    @EventHandler
    public void sign(SignChangeEvent e){
        if(!gameManager.getType().equals(GameManagerType.MULTI_GAME)) return;
        if(e.getLine(0).equals("ARENA")){
            gameManager.registerArena(e.getBlock());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        if(e.getAction().toString().toLowerCase().contains("right") && e.getItem() != null && e.getItem().getType().equals(Material.ENDER_PEARL)){
            e.setCancelled(true);
            e.getPlayer().openInventory(GameGuiInventory.forId("serverMenu").getInventory());
        }
        if(!gameManager.getType().equals(GameManagerType.MULTI_GAME)) return;
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
