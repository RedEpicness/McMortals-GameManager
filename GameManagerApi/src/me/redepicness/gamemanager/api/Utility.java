package me.redepicness.gamemanager.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Utility {

    private static ServerStatusManager serverStatusManager = null;
    private static BlockGenerator blockGenerator = null;

    public static void setServerStatusManager(ServerStatusManager server) {
        if(serverStatusManager != null) {
            throw new UnsupportedOperationException("Cannot re-set the status manager!");
        } else {
            serverStatusManager = server;
        }
    }

    public static void setBlockGenerator(BlockGenerator generator) {
        if(blockGenerator != null) {
            throw new UnsupportedOperationException("Cannot re-set the status manager!");
        } else {
            blockGenerator = generator;
        }
    }

    public static BlockGenerator getBlockGenerator(){
        return blockGenerator.newGenerator();
    }

    public static void createInventory(String id, String title, String... filters){
        serverStatusManager.createInventory(id, title, filters);
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

    public static Location makeLocation(double x, double y, double z){
        return new Location(Bukkit.getWorld("world"), x, y, z);
    }

}
