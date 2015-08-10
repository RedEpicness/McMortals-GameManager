package me.redepicness.gamemanager.api;

import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface GuiInventoryManager {

    GuiInventory forId(String id);

    void removeById(String id);

    GuiInventory generateNewInventory(String id, String name, int rows);

    void createServerStatusInventory(String id, String title, String... filters);

    ExecItemStack getItemStack(ItemStack stack, Consumer<Player> onExecute);

}
