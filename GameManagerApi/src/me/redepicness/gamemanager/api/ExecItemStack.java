package me.redepicness.gamemanager.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ExecItemStack {

    /**
     * Called when a player clicks on the item in an inventory
     *
     * @param player - Player who clicked
     */
    void onExecute(Player player);

    /**
     * Checks if the itemstack passed is the origin of this ExecItemStack.
     * Compares the type of the itemstack and the display name.
     *
     * @param itemStack - ItemStack to check
     *
     * @return - If the ItemStack is the origin
     */
    boolean isOrigin(ItemStack itemStack);

    /**
     * Gets the origin ItemStack of this ExecItemStack
     *
     * @return Origin ItemStack
     */
    ItemStack getStack();

    /**
     * Sets a new origin ItemStack for this ExecItemStack
     *
     * @param itemStack - New origin ItemStack
     */
    void setStack(ItemStack itemStack);

    int getPosition();

}
