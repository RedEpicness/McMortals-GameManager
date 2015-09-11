package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.ExecItemStack;
import me.redepicness.gamemanager.api.GuiInventoryManager;
import me.redepicness.gamemanager.api.Util;
import me.redepicness.gamemanager.api.Utility;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class GameInvManager implements GuiInventoryManager{

    private HashMap<String, GameGuiInventory> idToGui = new HashMap<>();

    public GameGuiInventory forId(String id){
        return idToGui.getOrDefault(id, null);
    }

    @Override
    public void removeById(String id) {
        HandlerList.unregisterAll(idToGui.get(id));
        idToGui.remove(id);
    }

    public GameGuiInventory generateNewInventory(String id, String name, int rows){
        idToGui.put(id, new GameGuiInventory(name, rows));
        return idToGui.get(id);
    }

    @Override
    public ExecItemStack getItemStack(ItemStack stack, Consumer<Player> onExecute) {
        return new GameExecItemStack(stack, -1, onExecute);
    }

    @Override
    public ExecItemStack getItemStack(ItemStack stack, int position, Consumer<Player> onExecute) {
        return new GameExecItemStack(stack, position, onExecute);
    }

}
