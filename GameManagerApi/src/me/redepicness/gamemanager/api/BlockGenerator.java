package me.redepicness.gamemanager.api;

import org.bukkit.Location;
import org.bukkit.Material;

public interface BlockGenerator {

    /**
     * Gets a new BlockGenerator
     *
     * @return new BlockGenerator
     */
    BlockGenerator newGenerator();

    /**
     * Adds a block (Material) to generate at a Location
     *
     * @param location - Where to generate
     * @param material - What material to generate
     */
    void addBlock(Location location, Material material);

    /**
     * Adds a task that will be ran after the generation is complete, but before the postTask.
     *
     * @param runnable - The task to run
     */
    void postGenerate(Runnable runnable);

    /**
     * Generates the blocks added by addBlock() and calls a task after it finishes.
     *
     * @param postTask - The task to call when finished
     */
    void generate(Runnable postTask);

}
