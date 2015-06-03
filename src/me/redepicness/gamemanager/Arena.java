package me.redepicness.gamemanager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Arena{

    private Location arenaSpawn;
    private ArrayList<String> players = new ArrayList<>();
    /*private Game game;*/
    private Block sign;
    private int countdownTaskId = -1;
    private int countdownTime = -1;
    private Stage stage;

    public Arena(/*Location arenaSpawn, Game game, */Block sign, Location arenaSpawn){
        /*this.arenaSpawn = arenaSpawn;
        this.game = game;*/
        this.arenaSpawn = arenaSpawn;
        this.sign = sign;
        updateSign(0, getName());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(players.contains(e.getPlayer().getName()))
            leave(e.getPlayer());
    }

    public void broadcast(String message){
        for(String name : players){
            Bukkit.getPlayer(name).sendMessage(message);
        }
    }

    public void startCountdown(int seconds, Integer[] announce, String message, Runnable finish){
        List<Integer> toAnnounce = Arrays.asList(announce);
        if(countdownTaskId != -1)
            throw new RuntimeException("You can only have one countdown running at the same time!");
        countdownTime = seconds;
        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameManager.getInstance(), () -> {
            if(countdownTime == 0){
                Bukkit.getScheduler().cancelTask(countdownTaskId);
                countdownTaskId = -1;
                this.countdownTime = -1;
                finish.run();
                return;
            }
            if(toAnnounce.contains(countdownTime)){
                String msg = message;
                if(countdownTime%60 == 0){
                    if(countdownTime > 60){
                        msg = msg.replace("%TIMEUNIT%", "minutes");
                    }
                    else{
                        msg = msg.replace("%TIMEUNIT%", "minute");
                    }
                    msg = msg.replace("%TIME%", countdownTime/60+"");
                }
                else{
                    if(countdownTime > 1){
                        msg = msg.replace("%TIMEUNIT%", "seconds");
                    }
                    else{
                        msg = msg.replace("%TIMEUNIT%", "second");
                    }
                    msg = msg.replace("%TIME%", countdownTime+"");
                }
                broadcast(msg);
            }
            updateSign(3, countdownTime+"");
            countdownTime--;
        }, 20, 20);
    }

    public int getPlayerCount(){
        return players.size();
    }

    public ArrayList<String> getPlayers() {
        return (ArrayList<String>) players.clone();
    }

    public boolean isJoinable(){
        return stage.equals(Stage.WAITING_FOR_PLAYERS);
    }

    public boolean isInArena(String name){
        return players.contains(name);
    }

    public void join(Player player){
        broadcast(player.getName()+" joined the game!");
        players.add(player.getName());
        updateSign(2, getPlayerCount() + "/" + getMaxPlayers());
    }

    public void leave(Player player){
        players.remove(player.getName());
        broadcast(player.getName()+" left the game!");
        updateSign(2, getPlayerCount() + "/" + getMaxPlayers());
        player.teleport(GameManager.getInstance().getGameManager().getLobbyLocation());
    }

    public Sign getSign(){
        return (Sign) sign.getState();
    }

    public abstract int getMaxPlayers();

    public abstract String getName();

    public void updateSign(int line, String message){
        Sign s = getSign();
        s.setLine(line, message);
        try{
            s.update();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Location getArenaSpawn() {
        return arenaSpawn;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        switch(stage){
            case STARTING_UP:
                updateSign(1, "Starting up");
                break;
            case GENERATING:
                updateSign(1, "Generating");
                break;
            case WAITING_FOR_PLAYERS:
                updateSign(1, "Join");
                break;
            case COUNTDOWN:
                updateSign(1, "Started");
                break;
            case RUNNING:
                updateSign(1, "Running");
                break;
            case FINISHED:
                updateSign(1, "Finished");
                break;
            case CLEANUP:
                updateSign(1, "Cleaning up");
                break;
        }
    }

    public enum Stage {

        STARTING_UP, CUSTOMIZING, GENERATING, WAITING_FOR_PLAYERS, COUNTDOWN, RUNNING, FINISHED, CLEANUP

    }

}
