package me.redepicness.gamemanager.api;

import me.redepicness.gamemanager.api.Infraction.InfractionType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public interface CustomPlayer {

    String getName();

    boolean isConsole();

    long getFirstLogin();

    long getLastLogin();

    String getFormattedName();

    String getColoredName();

    Rank getDominantRank();

    int getCubes();

    boolean hasEnoughCubes(int amount);

    void incrementCubes(int amount);

    void decrementCubes(int amount);

    void connectToServer(String serverName);

    Infraction getActiveInfraction(InfractionType type);

    Collection<Infraction> getInfractions();

    boolean hasFriend(String username);

    ArrayList<String> getFriends();

    ArrayList<String> getFriendRequests();

    boolean hasRank(Rank rank);

    ArrayList<Rank> getRanks();

    boolean hasPermission(Rank... rankList);

    boolean hasPermission(boolean inform, Rank... rankList);

    boolean isVanished();

    boolean isFlying();

    void message(String... message);

    Player getBukkitPlayer();

    String getUpgradeString(String game);

    void setUpgradeString(String game, String upgrade);

    String getSelectedGadget();

    void setSelectedGadget(String gadget);

    boolean isOnline();

    boolean exists();

}
