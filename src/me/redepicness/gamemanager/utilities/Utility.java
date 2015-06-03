package me.redepicness.gamemanager.utilities;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Utility {

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

}
