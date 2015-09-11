package me.redepicness.gamemanager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.redepicness.gamemanager.api.CustomPlayer;
import me.redepicness.gamemanager.api.Infraction;
import me.redepicness.gamemanager.api.Manager;
import me.redepicness.gamemanager.api.Rank;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GameCustomPlayer implements CustomPlayer{

    private String name;
    private ArrayList<Rank> ranks = null;
    private ArrayList<String> friends = null;
    private ArrayList<String> friendRequests = null;
    private Collection<Infraction> infractions = null;
    private long lastLogin = -1;
    private long firstLogin = -1;
    private int cubes = -1;

    GameCustomPlayer(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isConsole(){
        return name.equals("CONSOLE");
    }

    public long getFirstLogin() {
        if(isConsole()) return -1;
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(firstLogin != -1) return firstLogin;
        firstLogin = Database.getTable("PlayerData").getPropertyForName(name, "FirstLogin");
        return firstLogin;
    }

    public long getLastLogin() {
        if(isConsole()) return -1;
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(lastLogin != -1) return lastLogin;
        lastLogin = Database.getTable("PlayerData").getPropertyForName(name, "LastLogin");
        return lastLogin;
    }

    public String getFormattedName(){
        if(isConsole()) return ChatColor.GRAY+name;
        return getDominantRank().asPrefix(false)+name+ ChatColor.RESET;
    }

    public String getColoredName(){
        if(isConsole()) return ChatColor.GRAY+name;
        return getDominantRank().getColor()+name+ ChatColor.RESET;
    }

    public Rank getDominantRank(){
        if(isConsole()) return Rank.ADMIN;
        Rank dominant = Rank.DEFAULT;
        for(Rank rank : getRanks()){
            if(rank.getPriority() > dominant.getPriority()){
                dominant = rank;
            }
        }
        return dominant;
    }

    public void incrementCubes(int amount){
        int newAmount = getCubes() + amount;
        setCubes(newAmount);
    }

    public void decrementCubes(int amount){
        int newAmount = getCubes() - amount;
        setCubes(newAmount);
    }

    public void setCubes(int amount){
        if(amount < 0) amount = 0;
        Database.getTable("PlayerData").updatePropertyForName(name, "Cubes", amount);
        cubes = amount;
    }

    public int getCubes(){
        if(isConsole()) return Integer.MAX_VALUE;
        if(cubes != -1) return cubes;
        cubes = Database.getTable("PlayerData").getPropertyForName(name, "Cubes");
        return cubes;
    }

    public boolean hasEnoughCubes(int amount){
        return getCubes() > amount;
    }

    @Override
    public void connectToServer(String serverName) {
        if(isOnline()){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            getBukkitPlayer().sendPluginMessage(Manager.getPlugin("GameManager"), "BungeeCord", out.toByteArray());
        }
        else
            throw new RuntimeException("Tried to connect offline player!");
    }

    public Infraction getActiveInfraction(Infraction.InfractionType type){
        if(isConsole()) return null;
        if(infractions == null) getInfractions();
        assert infractions != null;
        ArrayList<Infraction> inf = new ArrayList<>();
        infractions.stream().filter(i -> !i.isExpired() && i.getType().equals(type)).forEach(inf::add);
        return inf.size() == 0 ? null : inf.get(0);
    }

    public Collection<Infraction> getInfractions(){
        if(isConsole()) return null;
        if(infractions != null) return infractions;
        infractions = Database.getInfractionsBulk(name);
        return infractions;
    }

    public boolean hasFriend(String username){
        if(isConsole()) return true;
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(friends == null) getFriends();
        assert friends != null;
        return friends.contains(username);
    }

    public ArrayList<String> getFriends(){
        if(isConsole()) return null;
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(friends != null) return friends;
        String result = Database.getTable("PlayerData").getPropertyForName(name, "Friends");
        friends = new ArrayList<>();
        if(result != null) Collections.addAll(friends, result.split(":"));
        return friends;
    }

    public ArrayList<String> getFriendRequests(){
        if(isConsole()) return null;
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(friendRequests != null) return friendRequests;
        String result = Database.getTable("PlayerData").getPropertyForName(name, "FRequests");
        friendRequests = new ArrayList<>();
        if(result != null) Collections.addAll(friendRequests, result.split(":"));
        return friendRequests;
    }

    public boolean hasRank(Rank rank) {
        if(isConsole()) return true;
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(ranks == null) getRanks();
        assert ranks != null;
        return ranks.contains(rank);
    }

    public ArrayList<Rank> getRanks(){
        if(isConsole()) {
            ArrayList<Rank> ranks = new ArrayList<>();
            ranks.add(Rank.ADMIN);
            return ranks;
        }
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(ranks != null) return ranks;
        String rank = Database.getTable("PlayerData").getPropertyForName(name, "Ranks");
        ranks = new ArrayList<>();
        if(rank == null) ranks.add(Rank.DEFAULT);
        else for(String r : rank.split(":")){
            ranks.add(Rank.valueOf(r.toUpperCase()));
        }
        return ranks;
    }

    private String getRankString(){
        String rankstring = "";
        for(Rank rank : ranks){
            rankstring += ":"+rank.toString();
        }
        return rankstring.substring(1);
    }

    public boolean hasPermission(Rank... rankList){
        return hasPermission(false, rankList);
    }

    public boolean hasPermission(boolean inform, Rank... rankList){
        if(isConsole()) return true;
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(ranks == null) getRanks();
        assert ranks != null;
        if(rankList.length > 1){
            boolean pass = false;
            for(Rank rank : rankList){
                if(hasPermission(false, rank)){
                    pass = true;
                }
            }
            if(inform && !pass) noPermission();
            return pass;
        }
        if(rankList.length == 0){
            if(inform) noPermission();
            return false;
        }
        if(ranks.contains(Rank.ADMIN)) return true;
        boolean pass = false;
        switch (rankList[0]){
            case DEFAULT:
                pass = true;
                break;
            case ACE:
                pass = ranks.contains(Rank.ACE) || ranks.contains(Rank.ALLSTAR) || ranks.contains(Rank.CUBER);
                break;
            case ALLSTAR:
                pass = ranks.contains(Rank.ALLSTAR) || ranks.contains(Rank.CUBER);
                break;
            case CUBER:
                pass = ranks.contains(Rank.CUBER);
                break;
            case BUILDER:
                pass = ranks.contains(Rank.BUILDER);
                break;
            case YOUTUBER:
                pass = ranks.contains(Rank.YOUTUBER);
                break;
            case JR_DEV:
                pass = ranks.contains(Rank.JR_DEV);
                break;
            case HELPER:
                pass = ranks.contains(Rank.HELPER) || ranks.contains(Rank.MODERATOR);
                break;
            case MODERATOR:
                pass = ranks.contains(Rank.MODERATOR);
                break;
        }
        if(inform && !pass) noPermission();
        return pass;
    }

    @Override
    public boolean isVanished() {
        return Database.getTable("PlayerData").getPropertyForName(name, "Vanished");
    }

    @Override
    public boolean isFlying() {
        return Database.getTable("PlayerData").getPropertyForName(name, "Flying");
    }

    public void message(String... message){
        if(isConsole()){
            Bukkit.getConsoleSender().sendMessage(message);
            return;
        }
        getBukkitPlayer().sendMessage(message);
    }

    public Player getBukkitPlayer(){
        if(isConsole()) return null;
        return Bukkit.getPlayerExact(name);
    }

    @Override
    public String getUpgradeString(String game) {
        return Database.getTable("PlayerUpgrades").getPropertyForName(name, game);
    }

    @Override
    public void setUpgradeString(String game, String upgrade) {
        Database.getTable("PlayerUpgrades").updatePropertyForName(name, game, upgrade);
    }

    @Override
    public String getSelectedGadget() {
        return Database.getTable("PlayerData").getPropertyForName(name, "Gadget");
    }

    @Override
    public void setSelectedGadget(String gadget) {
        Database.getTable("PlayerData").updatePropertyForName(name, "Gadget", gadget);
    }

    public boolean isOnline(){
        return getBukkitPlayer() != null;
    }

    public boolean exists() {
        if(isConsole()) return true;
        return Database.nameExistsInDatabase(name);
    }

    private void noPermission() {
        message(ChatColor.RED+"You do not have permission to do this!");
    }
}
