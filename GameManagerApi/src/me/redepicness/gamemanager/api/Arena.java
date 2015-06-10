package me.redepicness.gamemanager.api;

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

public abstract class Arena {

    private Location arenaSpawn;
    private ArrayList<String> players = new ArrayList<>();
    private Block sign;
    private int countdownTaskId = -1;
    private int countdownTime = -1;
    private Stage stage;

    /**
     * Constructor for Arena
     *
     * @param sign - The Block to be tied to this arena (must be a Sign)
     * @param arenaSpawn - The spawnpoint of this Arena
     */
    public Arena(Block sign, Location arenaSpawn){
        this.arenaSpawn = arenaSpawn;
        this.sign = sign;
        updateSign(0, getName());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(players.contains(e.getPlayer().getName()))
            leave(e.getPlayer());
    }

    /**
     * Broadcasts the message to all players in arena.
     *
     * @param message - Message to broadcast.
     */
    public void broadcast(String message){
        for(String name : players){
            Bukkit.getPlayer(name).sendMessage(message);
        }
    }

    /**
     * Starts a countdown with a custom duration, finish task and announcements on custom remaining times
     *
     * Example:
     *
     * startCountdown(60, new int[]{1,2,3,4,5,10, 30, 60}, "%TIME% %TIMEUNIT% remaining!", () -> broadcast("Testing finish task!"));
     *
     * Chat output (only to players in the arena):
     * 1 minute remaining!
     * 30 seconds remaining!
     * 10 seconds remaining!
     * 5 seconds remaining!
     * 4 seconds remaining!
     * 3 seconds remaining!
     * 2 seconds remaining!
     * 1 second remaining!
     * Testing finish task!
     *
     * @param seconds - Duration of the countdown
     * @param announce - Array of numbers on which it announces the time
     * @param message - Message to broadcast (replaces '%TIMEUNIT% with correct time unit and %TIME% with remaining time)
     * @param finish - The task to do when the countdown finishes
     */
    public void startCountdown(int seconds, Integer[] announce, String message, Runnable finish){
        List<Integer> toAnnounce = Arrays.asList(announce);
        if(countdownTaskId != -1)
            throw new RuntimeException("You can only have one countdown running at the same time!");
        countdownTime = seconds;
        countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Manager.getPlugin("GameManager"), () -> {
            if(countdownTime == 0){
                Bukkit.getScheduler().cancelTask(countdownTaskId);
                countdownTaskId = -1;
                this.countdownTime = -1;
                if(finish != null)
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

    /**
     * Gets the amount of players in arena.
     *
     * @return - Amount of players in arena.
     */
    public int getPlayerCount(){
        return players.size();
    }

    /**
     * Gets an ArrayList containing all names of players in the arena
     *
     * @return ArrayList of all player names in arena
     */
    public ArrayList<String> getPlayers() {
        return (ArrayList<String>) players.clone();
    }

    /**
     * Gets if the arena is joinable at this moment.
     *
     * @return If arena is joinable.
     */
    public boolean isJoinable(){
        return stage.equals(Stage.WAITING_FOR_PLAYERS);
    }

    /**
     * Check if the specified player is in this arena.
     *
     * @param name - Name of the player to check
     *
     * @return If player is in this arena
     */
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
        player.teleport(Manager.getGameManager().getLobbyLocation());
    }

    /**
     * Returns the sign this arena is tied to.
     *
     * @return The Sign of this arena
     */
    public Sign getSign(){
        return (Sign) sign.getState();
    }

    /**
     * Needs to be overridden, should return the max amount of players for this arena.
     *
     * @return Max amount of players in this arena
     */
    public abstract int getMaxPlayers();

    /**
     * Needs to be overridden, should return a custom name to display on the sign and other purposes
     *
     * @return A custom name
     */
    public abstract String getName();

    /**
     * Updates a line on the sign tied to this arena.
     *
     * @param line - The line number to update (0-3)
     * @param message - Message to display on the line
     */
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

    /**
     * Needs to be overridden, called when the GameManager disables the Arena, should be used to clean up.
     */
    public abstract void disable();

    /**
     * Gets the spawnpoint of this Arena.
     *
     * @return The spawnpoint of this Arena
     */
    public Location getArenaSpawn() {
        return arenaSpawn;
    }

    /**
     * Gets the current Stage of this Arena.
     *
     * @return The current Stage
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Sets the current Stage of this Arena.
     *
     * @param stage - The new Stage
     */
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

    /**
     * Represents the current Stage of this Arena
     */
    public enum Stage {

        STARTING_UP, CUSTOMIZING, GENERATING, WAITING_FOR_PLAYERS, COUNTDOWN, RUNNING, FINISHED, CLEANUP

    }

}
