package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.ExecItemStack;
import me.redepicness.gamemanager.api.GuiInventory;
import me.redepicness.gamemanager.api.Manager;
import me.redepicness.gamemanager.api.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GameGuiInventory implements Listener, GuiInventory{

    private Inventory inventory;
    private HashMap<ExecItemStack, Integer> execItemStacks = new HashMap<>();

    GameGuiInventory(String name, int rows) {
        this.inventory = Bukkit.createInventory(null, rows*9, name);
        Bukkit.getPluginManager().registerEvents(this, Manager.getPlugin("GameManager"));
    }

    public void updateSize(int rows){
        Inventory inventory = Bukkit.createInventory(null, rows*9, this.inventory.getTitle());
        inventory.setContents(this.inventory.getContents());
        this.inventory = inventory;
    }

    public void addItemStacks(ArrayList<ExecItemStack> execItemStacks){
        for(ExecItemStack itemStack : execItemStacks){
            int position = itemStack.getPosition();
            if(position == -1) {
                inventory.addItem(itemStack.getStack());
                position = inventory.first(itemStack.getStack());
            }
            else{
                inventory.setItem(position, itemStack.getStack());
            }
            this.execItemStacks.put(itemStack, position);

        }
    }

    public void addItemStacks(int[] positions, ExecItemStack[] execItemStacks){
        addItemStacks(new ArrayList<ExecItemStack>(){{
            add(new GameExecItemStack(
                    Util.makeItemStack(
                        Material.REDSTONE_BLOCK,
                        ChatColor.RED+"Depracated API usage, use new add ItemStacks method!"),
                    -1,
                    p -> {}
            ));
        }});
    }

    public void updateStacks(Predicate<ExecItemStack> predicate, Consumer<ExecItemStack> update){
        execItemStacks.keySet().stream().filter(predicate).forEach(stack -> {
            inventory.remove(stack.getStack());
            update.accept(stack);
            inventory.setItem(execItemStacks.get(stack), stack.getStack());
        });
    }

    public void updateStacks(Consumer<ExecItemStack> update) {
        execItemStacks.keySet().forEach(stack -> {
            inventory.remove(stack.getStack());
            update.accept(stack);
            inventory.setItem(execItemStacks.get(stack), stack.getStack());
        });
    }

    public Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getInventory().equals(this.inventory)){
            e.setCancelled(true);
            if(e.getCurrentItem() != null) {
                for (ExecItemStack execItemStack : execItemStacks.keySet()) {
                    if (execItemStack.isOrigin(e.getCurrentItem())) {
                        execItemStack.onExecute(((Player) e.getWhoClicked()));
                        return;
                    }
                }
            }
        }
    }

}
