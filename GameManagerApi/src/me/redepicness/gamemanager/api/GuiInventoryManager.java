package me.redepicness.gamemanager.api;

public interface GuiInventoryManager {

    public GuiInventory forId(String id);

    public GuiInventory generateNewInventory(String id, String name, int rows);

    void createInventory(String id, String title, String... filters);

}
