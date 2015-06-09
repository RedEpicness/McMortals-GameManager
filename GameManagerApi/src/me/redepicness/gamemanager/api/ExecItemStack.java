package me.redepicness.gamemanager.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ExecItemStack {

    void onExecute(Player player);

    boolean isOrigin(ItemStack itemStack);

    ItemStack getStack();

    void setStack(ItemStack itemStack);

}
