package me.redepicness.gamemanager.utilities;

import me.redepicness.gamemanager.GameManager;
import org.bukkit.Bukkit;
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

public class GuiInventory implements Listener{

    private static HashMap<String, GuiInventory> idToGui = new HashMap<>();

    public static GuiInventory forId(String id){
        return idToGui.getOrDefault(id, null);
    }

    public static GuiInventory generateNewInventory(String id, String name, int rows){
        idToGui.put(id, new GuiInventory(name, rows));
        return idToGui.get(id);
    }

    private Inventory inventory;
    private HashMap<ExecItemStack, Integer> execItemStacks = new HashMap<>();

    private GuiInventory(String name, int rows) {
        this.inventory = Bukkit.createInventory(null, rows*9, name);
        Bukkit.getPluginManager().registerEvents(this, GameManager.getInstance());
    }

    public void updateSize(int rows){
        Inventory inventory = Bukkit.createInventory(null, rows*9, this.inventory.getTitle());
        inventory.setContents(this.inventory.getContents());
        this.inventory = inventory;
    }

    public void addItemStacks(int[] positions, ExecItemStack[] execItemStacks){
        if(positions != null){
            int a = 0;
            for(ExecItemStack itemStack : execItemStacks){
                this.execItemStacks.put(itemStack, positions[a]);
                if(inventory.getItem(positions[a]) != null){
                    if(!inventory.getItem(positions[a]).getType().equals(Material.AIR)){
                        ArrayList<ExecItemStack> remove = new ArrayList<>();
                        for(ExecItemStack stack : this.execItemStacks.keySet()){
                            if(stack.isOrigin(inventory.getItem(positions[a]))){
                                remove.add(stack);
                            }
                        }
                        remove.forEach(this.execItemStacks::remove);
                    }
                }
                inventory.setItem(positions[a], itemStack.getStack());
                a++;
            }
        }
        else {
            for(ExecItemStack itemStack : execItemStacks){
                inventory.addItem(itemStack.getStack());
            }
        }

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
