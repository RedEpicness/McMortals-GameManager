package me.redepicness.gamemanager.utilities;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ExecItemStack {

    private ItemStack itemStack;
    private Consumer<Player> onExecute;

    public ExecItemStack(ItemStack itemStack, Consumer<Player> onExecute) {
        this.itemStack = itemStack;
        this.onExecute = onExecute;
    }

    public void onExecute(Player player){
        onExecute.accept(player);
    }

    public boolean isOrigin(ItemStack itemStack){
        return this.itemStack.getType().equals(itemStack.getType())&&
                ((!(this.itemStack.hasItemMeta() && itemStack.hasItemMeta()))
                        || this.itemStack.getItemMeta().getDisplayName().equals(itemStack.getItemMeta().getDisplayName())) ;
    }

    public ItemStack getStack() {
        return itemStack;
    }

    public void setStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
