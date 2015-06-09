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

    Infraction getActiveInfraction(InfractionType type);

    Collection<Infraction> getInfractions();

    boolean hasFriend(String username);

    ArrayList<String> getFriends();

    ArrayList<String> getFriendRequests();

    boolean hasRank(Rank rank);

    ArrayList<Rank> getRanks();

    boolean hasPermission(Rank... rankList);

    boolean hasPermission(boolean inform, Rank... rankList);

    void message(String... message);

    String getLastMessage();

    boolean hasLastMessage();

    Player getPlayer();

    boolean isOnline();

    boolean exists();

}
