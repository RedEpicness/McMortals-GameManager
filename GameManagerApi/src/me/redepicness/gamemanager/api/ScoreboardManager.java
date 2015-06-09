package me.redepicness.gamemanager.api;

import org.bukkit.scoreboard.Scoreboard;

public interface ScoreboardManager {

    void setStaffInTab(boolean staffInTab);

    boolean staffInTab();

    void setLocalScoreboards();

    boolean localScoreboards();

    Scoreboard getScoreboard();

    Scoreboard getScoreboard(String name);

    void updateScoreboard(Scoreboard scoreboard);

    void updateScoreboard(String name, Scoreboard scoreboard);

}
