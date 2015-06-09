package me.redepicness.gamemanager.api;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Map;

public interface BlockGenerator {

    BlockGenerator newGenerator();

    void addBlock(Location location, Material material);

    void postGenerate(Runnable runnable);

    void generate(Runnable postTask);

    void scheduleBlocks(Map<Location, Material> map, int delay);

}
