package me.redepicness.gamemanager.api;

import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface GuiInventory {

    void updateSize(int rows);

    void addItemStacks(int[] positions, ExecItemStack[] execItemStacks);

    void addItemStacks(ArrayList<ExecItemStack> execItemStacks);

    void updateStacks(Predicate<ExecItemStack> predicate, Consumer<ExecItemStack> update);

    void updateStacks(Consumer<ExecItemStack> update);

    Inventory getInventory();

}
