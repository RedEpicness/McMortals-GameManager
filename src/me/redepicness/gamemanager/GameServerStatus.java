package me.redepicness.gamemanager;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.redepicness.gamemanager.api.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashMap;

public class GameServerStatus implements Listener, PluginMessageListener{

    public static class ServerStatusApi implements ServerStatus {
        @Override
        public void registerStatusListener(String forwardTarget, String filter) {
            GameServerStatus.createStatusListener(forwardTarget, filter);
        }
    }

    private static boolean created = false;

    private HashMap<String, Block> signs = new HashMap<>();
    private String forwardTarget;
    private String filter;


    public static void createStatusListener(String forwardTarget, String filter){
        if(created)
            throw new RuntimeException("Cannot create multiple Status Listeners, 1 already created!");
        GameServerStatus status = new GameServerStatus(forwardTarget, filter);
        Bukkit.getPluginManager().registerEvents(status, GManager.getInstance());
        Bukkit.getMessenger().registerIncomingPluginChannel(GManager.getInstance(), "Messenger", status);
        Bukkit.getMessenger().registerOutgoingPluginChannel(GManager.getInstance(), "Messenger");
        status.startGameStatusPing();
    }

    GameServerStatus(String forwardTarget, String filter) {
        this.forwardTarget = forwardTarget;
        this.filter = filter;
    }

    @EventHandler
    public void onSign(SignChangeEvent e){
        if(e.getLine(0).equals("UPDATE")){
            signs.put(e.getLine(1), e.getBlock());
        }
    }

    void startGameStatusPing(){
        Bukkit.getScheduler().scheduleSyncDelayedTask(GManager.getInstance(), () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("GameStatusPingStart");
            out.writeUTF(forwardTarget);
            out.writeUTF(filter);
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if(player != null){
                System.out.println("Sent message to "+player.getName());
                player.sendPluginMessage(GManager.getInstance(), "Messenger", out.toByteArray());
            }
            else{
                startGameStatusPing();
            }
        }, 10);
    }

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        if(s.equals("Messenger")){
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String subchannel = in.readUTF();
            if(subchannel.equals("GameStatusPingData")){
                String server = in.readUTF();
                String[] sign = in.readUTF().split(":");
                if(signs.get(server) != null && signs.get(server).getType().toString().toLowerCase().contains("sign")){
                    Sign signblock = (Sign) signs.get(server).getState();
                    signblock.setLine(0, server);
                    signblock.setLine(1, sign[0]);
                    signblock.setLine(2, sign[1]);
                    signblock.setLine(3, ChatColor.GREEN+"Click to join");
                    signblock.update();
                }
            }
            else if(subchannel.equals("GameStatusEndListen")){
                Bukkit.getScheduler().scheduleSyncDelayedTask(GManager.getInstance(), this::startGameStatusPing,  20);
            }
        }
    }
}
