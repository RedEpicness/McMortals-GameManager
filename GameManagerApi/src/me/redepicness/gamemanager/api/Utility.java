package me.redepicness.gamemanager.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Utility {

    private static boolean set = false;
    private static GuiInventoryManager guiInventoryManager = null;
    private static BlockGenerator blockGenerator = null;
    private static ScoreboardManager scoreboardManager = null;
    private static PlayerManager playerManager = null;
    private static ServerStatus serverStatus = null;
    private static CubeManager cubeManager = null;

    public static void setManagers(PlayerManager pmanager, GuiInventoryManager guimanager, BlockGenerator generator, ScoreboardManager smanager, ServerStatus status, CubeManager cmanager){
        if(set)
            throw new UnsupportedOperationException("Cannot re-set the managers!");
        playerManager = pmanager;
        guiInventoryManager = guimanager;
        blockGenerator = generator;
        scoreboardManager = smanager;
        serverStatus = status;
        cubeManager = cmanager;
    }

    public static void registerServerStatusListener(String forwardTarget, String filter){
        serverStatus.registerStatusListener(forwardTarget, filter);
    }

    public static CubeManager getCubeManager() {
        return cubeManager.getNewInstance();
    }

    public static BlockGenerator getBlockGenerator(){
        return blockGenerator.newGenerator();
    }

    public static GuiInventoryManager getGuiInvManager(){
        return guiInventoryManager;
    }

    public static ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public static CustomPlayer getPlayer(Player p){
        return playerManager.get(p.getName());
    }

    public static CustomPlayer getPlayer(String name){
        return playerManager.get(name);
    }

    public static ItemStack makeItemStack(Material material, String displayName, String... lore){
        return makeItemStack(material, 1, displayName, lore);
    }

    public static ItemStack makeItemStack(Material material, int amount, String displayName, String... lore){
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(displayName);
        if(lore.length > 0){
            meta.setLore(Arrays.asList(lore));
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public static void log(String s) {
        Bukkit.getConsoleSender().sendMessage(s);
    }

    public static World getWorld(){
        return Bukkit.getWorld("world");
    }

    public static Location makeLocation(double x, double y, double z){
        return new Location(getWorld(), x, y, z);
    }

    public static Location makeLocation(double x, double y, double z, float yaw, float pitch){
        return new Location(getWorld(), x, y, z, yaw, pitch);
    }

}
