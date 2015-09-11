package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.*;
import me.redepicness.gamemanager.api.GameManager.GameManagerType;
import me.redepicness.gamemanager.api.Util;
import me.redepicness.gamemanager.api.Utility;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GManager extends JavaPlugin implements Listener{

    private boolean initialized = false;

    @Override
    public void onDisable() {
        GCubeManager.forcePush();
        Database.end();
        for(Player p : Bukkit.getOnlinePlayers()){
            p.kickPlayer(ChatColor.RED+"Server shutting down!");
        }
    }

    @Override
    public void onEnable() {
        Database.init();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getPluginManager().registerEvents(this, this);
        Util.setManagers(new GPlayerManager(), new GameInvManager(), new GameBlockGenerator(), new GameScoreboardManager(), new GCubeManager());
        Utility.setManagers(new GPlayerManager(), new GameInvManager(), new GameBlockGenerator(), new GameScoreboardManager(), new GCubeManager());
        Bukkit.getScheduler().scheduleAsyncDelayedTask(this, () -> {
            System.out.println("Initializing GameManager!");
            if (Manager.getGameManager() == null) {
                System.out.println("No GameManager provided, attempting to run in no-manager mode!");
                Manager.startNoManagerMode();
            } else {
                Manager.getGameManager().init();
            }
            initialized = true;
            System.out.println("Done!");
        }, 1);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void join(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if(!initialized){
            p.kickPlayer(ChatColor.RED + "The server is initializing!");
            return;
        }
        e.setJoinMessage(null);
        CustomPlayer player = Util.getPlayer(p.getName());
        Util.getScoreboardManager().updateStaff();
        if (player.hasPermission(Rank.ADMIN)) {
            p.setOp(true);
        } else {
            p.setOp(false);
        }
        if(Manager.isNoManagerMode()) return;
        p.teleport(Manager.getLobbyLoc());
        if(player.isVanished()){
            p.setGameMode(GameMode.SPECTATOR);
            for(Player p1 : Bukkit.getOnlinePlayers()){
                CustomPlayer player1 = Util.getPlayer(p1);
                if(!player1.hasPermission(Rank.MODERATOR)){
                    p1.hidePlayer(p);
                }
            }
            player.message(ChatColor.GREEN + "You are vanished!");
            return;
        } else {
            for(Player p1 : Bukkit.getOnlinePlayers()){
                CustomPlayer player1 = Util.getPlayer(p1);
                if(player1.isVanished() && !player.hasPermission(Rank.MODERATOR)){
                    p.hidePlayer(p1);
                }
            }
        }
        if (Manager.isType(GameManagerType.GAME)) {
            Game game = Manager.getGame();
            if(!game.isJoinable()){
                p.kickPlayer(ChatColor.RED + game.cannotJoinReason());
            }
            else if(game.getPlayerCount() >= game.getMaxPlayers()){
                p.kickPlayer(ChatColor.RED + "The game is full!");
            }
            else{
                game.gameJoin(p);
            }
        }
        else{
            if(player.isFlying()){
                player.message(ChatColor.GREEN+"You are flying!");
                p.setAllowFlight(true);
            }
        }
        Manager.getGameManager().playerJoin(p);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void leave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.setQuitMessage(null);
        GPlayerManager.uncache(p.getName());
        if(Manager.isNoManagerMode()) return;
        if (Manager.isType(GameManagerType.GAME) && Manager.getGame().isInArena(p.getName())) {
            Manager.getGame().gameLeave(p);
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent e){
        if(!initialized){
            e.setMotd("NULL::false::The server is still Initializing!::NULL:NULL");
            return;
        }
        if(Manager.isNoManagerMode()) {
            e.setMotd("NULL::true::NULL::NULL:NULL");
            return;
        }
        String type = ""+Manager.getGameManager().getType();
        String joinable;
        String reason = "NULL";
        String sign = "";
        boolean join = true;
        if(Manager.isType(GameManagerType.GAME)){
            Game game = Manager.getGameManager().getGame();
            if(!game.isJoinable()){
                join = false;
                reason = Manager.getGame().cannotJoinReason();
                sign = game.getPlayerCount() + "/" + game.getMaxPlayers()+":"+
                        game.getStage().toText();
            }
        }
        joinable = join+"";
        e.setMotd(type+"::"+joinable+"::"+reason+"::"+sign);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e){
        if(Manager.isNoManagerMode()) return;
        if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && Manager.isType(GameManagerType.LOBBY)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreak(BlockPlaceEvent e){
        if(Manager.isNoManagerMode()) return;
        if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE) && Manager.isType(GameManagerType.LOBBY)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent e){
        if(Manager.isNoManagerMode()) return;
        if(Manager.isType(GameManagerType.LOBBY)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        CustomPlayer p = Util.getPlayer(e.getPlayer());
        e.setCancelled(true);
        Bukkit.broadcastMessage(p.getFormattedName() + ChatColor.GRAY + ": " + ChatColor.RESET + e.getMessage());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("fly")){
            if(!Manager.isType(GameManagerType.LOBBY)) return false;
            CustomPlayer player = Util.getPlayer(sender.getName());
            if(!player.hasPermission(true, Rank.ACE)) return true;
            boolean flying = player.isFlying();
            if(flying){
                player.getBukkitPlayer().setAllowFlight(false);
                player.message(ChatColor.RED+"Disabled flying!");
            }
            else {
                player.getBukkitPlayer().setAllowFlight(true);
                player.message(ChatColor.GREEN+"Enabled flying!");
            }
            Database.getTable("PlayerData").updatePropertyForName(player.getName(), "Flying", !flying);
            return true;
        }
        if(!command.getName().equals("v")) return false;
        CustomPlayer player = Util.getPlayer(sender.getName());
        if(!player.hasPermission(true, Rank.MODERATOR)) return true;
        boolean vanished = player.isVanished();
        if(vanished){
            player.getBukkitPlayer().setGameMode(GameMode.SURVIVAL);
            if (Manager.getGameManager().getType().equals(GameManagerType.GAME)) {
                for(String p : Manager.getGameManager().getGame().getPlayers()){
                    Bukkit.getPlayerExact(p).showPlayer(player.getBukkitPlayer());
                }
                Game game = Manager.getGameManager().getGame();
                if(!game.isJoinable()){
                    player.getBukkitPlayer().kickPlayer(ChatColor.RED + game.cannotJoinReason());
                }
                else if(game.getPlayerCount() >= game.getMaxPlayers()){
                    player.getBukkitPlayer().getPlayer().kickPlayer(ChatColor.RED+"The game is full!");
                }
                else{
                    game.gameJoin(player.getBukkitPlayer().getPlayer());
                }
            }
            else{
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.showPlayer(player.getBukkitPlayer());
                }
            }
            if(player.isFlying()){
                player.getBukkitPlayer().setAllowFlight(true);
            }
            player.message(ChatColor.GREEN+"You have unvanished!");
        }
        else{
            player.getBukkitPlayer().setGameMode(GameMode.SPECTATOR);
            if (Manager.getGameManager().getType().equals(GameManagerType.GAME)) {
                for(String p : Manager.getGameManager().getGame().getPlayers()){
                    Bukkit.getPlayerExact(p).hidePlayer(player.getBukkitPlayer());
                }
                Game game = Manager.getGameManager().getGame();
                game.gameLeave(player.getBukkitPlayer().getPlayer());
            }
            else{
                for(Player p : Bukkit.getOnlinePlayers()){
                    CustomPlayer player1 = Util.getPlayer(p);
                    if(!player1.hasPermission(Rank.MODERATOR)){
                        p.hidePlayer(player.getBukkitPlayer());
                    }
                }
            }
            player.message(ChatColor.GREEN + "You have vanished!");
        }
        Database.getTable("PlayerData").updatePropertyForName(player.getName(), "Vanished", !vanished);
        return true;
    }
}
