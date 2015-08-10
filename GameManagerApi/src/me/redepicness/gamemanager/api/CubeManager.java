package me.redepicness.gamemanager.api;

import org.bukkit.entity.Player;

public interface CubeManager {

    CubeManager getNewInstance();

    int getCubes(Player player);

    void addCubes(Player player, int amount, String reason);

    boolean isFinished();

    void pushToDatabase();

}
