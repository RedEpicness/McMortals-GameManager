package me.redepicness.gamemanager.utilities;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.redepicness.gamemanager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerStatusManager implements PluginMessageListener{

    private static HashMap<String, Integer> serverList = null;

    public static void createInventory(String id, String title, String... filters){
        GuiInventory inventory = GuiInventory.generateNewInventory(id, title, 1);
        inventory.addItemStacks(
                new int[]{0},
                new ExecItemStack[]{
                    new ExecItemStack(Utility.makeItemStack(Material.REDSTONE_BLOCK, 0, ChatColor.RED+"Loading servers..."), Player::closeInventory)
                }
        );
        serverWait(id, filters);
    }

    public static void serverWait(String id, String... filters){
        Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance(), () -> {
            if (serverList != null) {
                GuiInventory inventory = GuiInventory.forId(id);
                ArrayList<String> servers = new ArrayList<>();
                if (filters.length > 0) {
                    for (String name : serverList.keySet()) {
                        for (String filter : filters) {
                            if (name.contains(filter) && !servers.contains(name))
                                servers.add(name);
                        }
                    }
                } else {
                    servers.addAll(serverList.keySet());
                }
                int[] positions = new int[servers.size()];
                ExecItemStack[] stacks = new ExecItemStack[servers.size()];
                int index = 0;
                for (String name : servers) {
                    stacks[index] = new ExecItemStack(Utility.makeItemStack(Material.ENDER_PEARL, 0, ChatColor.RED + name), p -> {
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Connect");
                        out.writeUTF(name);
                        p.sendPluginMessage(GameManager.getInstance(), "BungeeCord", out.toByteArray());
                    });
                    positions[index] = index;
                    index++;
                }
                inventory.addItemStacks(positions, stacks);
                inventoryUpdater(id);
            } else {
                serverWait(id, filters);
            }
        }, 10);

    }

    private static void inventoryUpdater(String id) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance(), () -> {
            GuiInventory inventory = GuiInventory.forId(id);
            inventory.updateStacks(stack -> {
                ItemStack item = stack.getStack();
                item.setAmount(serverList.get(ChatColor.stripColor(item.getItemMeta().getDisplayName())));
                stack.setStack(item);
            });
        }, 20, 20);
    }

    public static void updateInit(){
        Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance(), () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("GetServers");
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if(player != null){
                player.sendPluginMessage(GameManager.getInstance(), "BungeeCord", out.toByteArray());
            }
            else{
                updateInit();
            }
        }, 10);
    }

    public void serverUpdater(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance(), () -> {
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player != null)
                for (String name : serverList.keySet()) {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("PlayerCount");
                    out.writeUTF(name);
                    player.sendPluginMessage(GameManager.getInstance(), "BungeeCord", out.toByteArray());
                }
        }, 20, 20);
    }

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        if (!s.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subchannel = in.readUTF();
        if(subchannel.equals("GetServers")){
            String[] names = in.readUTF().split(", ");
            serverList = new HashMap<>();
            for(String name : names){
                serverList.put(name, 0);
            }
            serverUpdater();
        }
        else if(subchannel.equals("PlayerCount")){
            serverList.put(in.readUTF(), in.readInt());
        }
    }

}
