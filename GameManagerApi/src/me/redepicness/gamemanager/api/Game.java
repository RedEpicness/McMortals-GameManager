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

public abstract class Game {

    private Location arenaSpawn;
    private ArrayList<String> players = new ArrayList<>();
    private int countdownTaskId = -1;
    private int countdownTime = -1;
    private Stage stage;

    /**
     * Constructor for Arena
     *
     * @param arenaSpawn - The spawnpoint of this Arena
     */
    public Game(Location arenaSpawn){
        if(Manager.isNoManagerMode())
            throw new RuntimeException("Cannot make Arena object in no GameManager mode!");
        this.arenaSpawn = arenaSpawn;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(players.contains(e.getPlayer().getName()))
            gameLeave(e.getPlayer());
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

    public String cannotJoinReason(){
        return "The game is currently Running!";
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

    public abstract void join(Player player);

    public void gameJoin(Player player){
        players.add(player.getName());
        join(player);

    }

    public abstract void leave(Player player);

    public void gameLeave(Player player){
        players.remove(player.getName());
        leave(player);
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
    }

    /**
     * Represents the current Stage of this Arena
     */
    public enum Stage {

        STARTING_UP, CUSTOMIZING, GENERATING, WAITING_FOR_PLAYERS, COUNTDOWN, RUNNING, FINISHED, CLEANUP;

        public String toText(){
            switch(this){
                case STARTING_UP:
                    return "Starting up";
                case CUSTOMIZING:
                    return "Customizing";
                case GENERATING:
                    return "Generating";
                case WAITING_FOR_PLAYERS:
                    return "Join";
                case COUNTDOWN:
                    return "Started";
                case RUNNING:
                    return "Running";
                case FINISHED:
                    return "Finished";
                case CLEANUP:
                    return "Cleaning up";
                default:
                    return null;
            }
        }

    }

}
