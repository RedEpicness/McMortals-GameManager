package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.ExecItemStack;
import me.redepicness.gamemanager.api.GuiInventoryManager;
import me.redepicness.gamemanager.api.Utility;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.function.Consumer;

public class GameInvManager implements GuiInventoryManager{

    private HashMap<String, GameGuiInventory> idToGui = new HashMap<>();

    public GameGuiInventory forId(String id){
        return idToGui.getOrDefault(id, null);
    }

    @Override
    public void removeById(String id) {
        idToGui.remove(id);
    }

    public GameGuiInventory generateNewInventory(String id, String name, int rows){
        idToGui.put(id, new GameGuiInventory(name, rows));
        return idToGui.get(id);
    }

    @Override
    public void createServerStatusInventory(String id, String title, String... filters) {
        GameGuiInventory inventory = generateNewInventory(id, title, 1);
        inventory.addItemStacks(
                new int[]{0},
                new GameExecItemStack[]{
                        new GameExecItemStack(Utility.makeItemStack(Material.REDSTONE_BLOCK, 0, ChatColor.RED + "Loading servers..."), Player::closeInventory)
                }
        );
        GameServerStatusManager.serverWait(id, filters);
    }

    @Override
    public ExecItemStack getItemStack(ItemStack stack, Consumer<Player> onExecute) {
        return new GameExecItemStack(stack, onExecute);
    }

}
