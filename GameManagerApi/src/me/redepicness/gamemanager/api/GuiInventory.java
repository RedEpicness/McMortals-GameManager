package me.redepicness.gamemanager.api;

import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface GuiInventory {

    public void updateSize(int rows);

    public void addItemStacks(int[] positions, ExecItemStack[] execItemStacks);

    public void updateStacks(Predicate<ExecItemStack> predicate, Consumer<ExecItemStack> update);

    public void updateStacks(Consumer<ExecItemStack> update);

    public Inventory getInventory();

}
