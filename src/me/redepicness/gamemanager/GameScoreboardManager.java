package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.Rank;
import me.redepicness.gamemanager.api.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class GameScoreboardManager implements ScoreboardManager{

    /*private static GameScoreboardManager manager;


    public static void init(){
        manager = new GameScoreboardManager();
    }

    public static <T extends GameScoreboardManager> void setManager(T scoreboardManager){
        manager = scoreboardManager;
    }

    public static GameScoreboardManager getManager() {
        return manager;
    }*/

    //Object methods
    private volatile HashMap<String, Scoreboard> playerScoreboards;
    private boolean staffTab = true;
    private boolean local = false;
    private volatile Scoreboard staff;
    private volatile Scoreboard global;

    public GameScoreboardManager(){
        staff = Bukkit.getScoreboardManager().getNewScoreboard();
        if(!localScoreboards()) global = Bukkit.getScoreboardManager().getNewScoreboard();
        else playerScoreboards = new HashMap<>();
        if(staffInTab()){
            for(Rank rank : Rank.values()){
                if(rank.equals(Rank.DEFAULT)) continue;
                Team team = staff.registerNewTeam(rank.toString().toLowerCase());
                team.setPrefix(rank.asPrefix(true));
            }
        }
    }

    public void updateStaff(){
        if(!staffInTab()) return;
        for(Player player : Bukkit.getOnlinePlayers()){
            GameCustomPlayer p = GameCustomPlayer.get(player.getName());
            if(p.getDominantRank().equals(Rank.DEFAULT)) continue;
            Team team = staff.getTeam(p.getDominantRank().toString().toLowerCase());
            if(staff.getPlayerTeam(player) != null && staff.getPlayerTeam(player).equals(team)){
                continue;
            }
            team.addPlayer(player);
        }
        for(Player player : Bukkit.getOnlinePlayers()){
            if(localScoreboards()){
                player.setScoreboard(applyStaff(getScoreboard(player.getName())));
            }
            else{
                player.setScoreboard(applyStaff(getScoreboard()));
            }
        }
    }

    @Override
    public void setStaffInTab(boolean staffInTab) {
        staffTab = staffInTab;
    }

    public boolean staffInTab(){
        return staffTab;
    }

    @Override
    public void setLocalScoreboards(boolean localScoreboards) {
        local = localScoreboards;
    }

    public boolean localScoreboards(){
        return local;
    }

    public Scoreboard getScoreboard(){
        if (localScoreboards()){
            throw new RuntimeException("Cannot get global scoreboard with local scoreboards active, get scoreboard per player.");
        }
        return global;
    }

    public Scoreboard getScoreboard(String name){
        if (!localScoreboards()){
            throw new RuntimeException("Cannot get local scoreboard with local scoreboards inactive, get scoreboard globally.");
        }
        if(playerScoreboards.containsKey(name)){
            return playerScoreboards.get(name);
        }
        else{
            return Bukkit.getScoreboardManager().getNewScoreboard();
        }
    }

    public void updateScoreboard(Scoreboard scoreboard){
        if (localScoreboards()){
            throw new RuntimeException("Cannot update global scoreboard with local scoreboards active, update scoreboard per player.");
        }
        global = scoreboard;
        for(Player player : Bukkit.getOnlinePlayers()){
            player.setScoreboard(staffInTab() ? applyStaff(scoreboard) : scoreboard);
        }
    }

    public void updateScoreboard(String name, Scoreboard scoreboard){
        if (!localScoreboards()){
            throw new RuntimeException("Cannot update local scoreboard with local scoreboards inactive, update scoreboard globally.");
        }
        playerScoreboards.put(name, scoreboard);
        if(Bukkit.getPlayerExact(name) == null){
            playerScoreboards.remove(name);
        }
        else{
            Bukkit.getPlayerExact(name).setScoreboard(staffInTab()?applyStaff(scoreboard):scoreboard);
        }
    }

    Scoreboard applyStaff(Scoreboard scoreboard){
        for(Team team : staff.getTeams()){
            Team t = scoreboard.getTeam(team.getName());
            if(t == null){
                t = scoreboard.registerNewTeam(team.getName());
            }
            t.setPrefix(team.getPrefix());
            t.setDisplayName(team.getDisplayName());
            t.setSuffix(team.getSuffix());
            t.setAllowFriendlyFire(team.allowFriendlyFire());
            t.setCanSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles());
            t.setNameTagVisibility(team.getNameTagVisibility());
            team.getEntries().forEach(t::addEntry);
        }
        return scoreboard;
    }

}
