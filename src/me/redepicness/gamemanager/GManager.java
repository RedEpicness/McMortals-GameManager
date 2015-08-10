package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.*;
import me.redepicness.gamemanager.api.GameManager.GameManagerType;
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

    private static GManager instance;
    private boolean initialized = false;

    public static GManager getInstance() {
        return instance;
    }

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
        instance = this;
        Database.init();
        Bukkit.getMessenger().registerOutgoingPluginChannel(GManager.getInstance(), "BungeeCord");
        getServer().getPluginManager().registerEvents(this, this);
        Utility.setManagers(new GPlayerManager(), new GameInvManager(), new GameBlockGenerator(), new GameScoreboardManager(),
                new GameServerStatus.ServerStatusApi(), new GCubeManager());
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
        if(!initialized){
            e.getPlayer().kickPlayer(ChatColor.RED+"The server is initializing!");
            return;
        }
        e.setJoinMessage(null);
        CustomPlayer player = Utility.getPlayer(e.getPlayer().getName());
        Utility.getScoreboardManager().updateStaff();
        if (player.hasPermission(Rank.ADMIN)) {
            player.getBukkitPlayer().setOp(true);
        } else {
            player.getBukkitPlayer().setOp(false);
        }
        if(Manager.isNoManagerMode()) return;
        e.getPlayer().teleport(Manager.getGameManager().getLobbyLocation());
        if(player.isVanished()){
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
                    p.hidePlayer(player.getBukkitPlayer());
                }
            }
            player.message(ChatColor.GREEN + "You are vanished!");
            return;
        } else {
            for(Player p : Bukkit.getOnlinePlayers()){
                CustomPlayer player1 = Utility.getPlayer(p);
                if(player1.isVanished()){
                    player.getBukkitPlayer().hidePlayer(p);
                }
            }
        }
        if (Manager.getGameManager().getType().equals(GameManagerType.GAME)) {
            Game game = Manager.getGameManager().getGame();
            if(!game.isJoinable()){
                e.getPlayer().kickPlayer(ChatColor.RED+game.cannotJoinReason());
            }
            else if(game.getPlayerCount() >= game.getMaxPlayers()){
                e.getPlayer().kickPlayer(ChatColor.RED+"The game is full!");
            }
            else{
                game.gameJoin(e.getPlayer());
            }
        }
        else{
            if(player.isFlying()){
                player.getBukkitPlayer().setAllowFlight(true);
            }
        }
        Manager.getGameManager().playerJoin(player.getBukkitPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void leave(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        if(Manager.isNoManagerMode()){
            if (Manager.getGameManager().getType().equals(GameManagerType.GAME) && !Utility.getPlayer(e.getPlayer()).isVanished()) {
                Manager.getGameManager().getGame().gameLeave(e.getPlayer());
            }
        }
        GPlayerManager.uncache(e.getPlayer().getName());
    }

    @EventHandler
    public void sign(SignChangeEvent e) {
        if(e.getLine(0).equals("HUB")){
            e.setLine(0, "");
            e.setLine(1, ChatColor.GREEN + "Click here to");
            e.setLine(2, ChatColor.GREEN + "return to hub");
            e.setLine(3, "");
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().toString().toLowerCase().contains("sign")) {
            if(((Sign) e.getClickedBlock().getState()).getLine(2).equals(ChatColor.GREEN+"return to hub")){
                if(!Manager.isNoManagerMode()){
                    Connector.connect(e.getPlayer(), Manager.getGameManager().hubServerName());
                }
                else{
                    Connector.connect(e.getPlayer(), "LOBBY_1");
                }
            }
            if(((Sign) e.getClickedBlock().getState()).getLine(3).equals(ChatColor.GREEN + "Click to join")){
                Connector.connect(e.getPlayer(), ((Sign) e.getClickedBlock().getState()).getLine(0));
            }
        }
    }

    @EventHandler
    public void onPing(ServerListPingEvent e){
        if(!initialized){
            e.setMotd("NULL::NULL::NULL::NULL:NULL");
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
        if(Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) join = false;
        if(Manager.getGameManager().getType().equals(GameManagerType.GAME)){
            Game game = Manager.getGameManager().getGame();
            if(!game.isJoinable()){
                join = false;
                reason = Manager.getGameManager().getGame().cannotJoinReason();
                sign = game.getPlayerCount() + "/" + game.getMaxPlayers()+":"+
                        game.getStage().toText();
            }
        }
        joinable = join+"";
        e.setMotd(type+"::"+joinable+"::"+reason+"::"+sign);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e){
        if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreak(BlockPlaceEvent e){
        if(!e.getPlayer().getGameMode().equals(GameMode.CREATIVE)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent e){
        if(Manager.getGameManager().getType().equals(GameManagerType.LOBBY)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        CustomPlayer p = Utility.getPlayer(e.getPlayer());
        e.setCancelled(true);
        Bukkit.broadcastMessage(p.getFormattedName() + ChatColor.GRAY + ": " + ChatColor.RESET + e.getMessage());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("fly")){
            if(!Manager.getGameManager().getType().equals(GameManagerType.LOBBY)) return false;
            CustomPlayer player = Utility.getPlayer(sender.getName());
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
        CustomPlayer player = Utility.getPlayer(sender.getName());
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
                    p.hidePlayer(player.getBukkitPlayer());
                }
            }
            player.message(ChatColor.GREEN + "You have vanished!");
        }
        Database.getTable("PlayerData").updatePropertyForName(player.getName(), "Vanished", !vanished);
        return true;
    }
}
