package me.redepicness.gamemanager;

import me.redepicness.gamemanager.GameInfraction.InfractionType;
import me.redepicness.gamemanager.api.CustomPlayer;
import me.redepicness.gamemanager.api.Infraction;
import me.redepicness.gamemanager.api.Rank;
import me.redepicness.gamemanager.api.Utility;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class GameCustomPlayer implements CustomPlayer{

    private static Map<String, GameCustomPlayer> cachedData = new HashMap<>();

    public static void init(){
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(GManager.getInstance(), GameCustomPlayer::checkCachedData, 5 * 60 * 20, 5 * 60 * 20);
    }

    public static GameCustomPlayer get(String name){
        if(cachedData.containsKey(name)) return cachedData.get(name);
        GameCustomPlayer player = new GameCustomPlayer(name);
        if(!player.exists() || !player.isOnline()){
            return player;
        }
        cachedData.put(name, player);
        Utility.log(ChatColor.GREEN + "Loaded data for " + player.getFormattedName() + ChatColor.GREEN + " Ranks: " + player.getRanks());
        return player;
    }

    public static void uncache(String name){
        cachedData.remove(name);
    }

    private static void checkCachedData(){
        Utility.log(ChatColor.RED + "Checking cached data!");
        cachedData.values().stream().forEach(player -> {
            if(!player.isOnline()){
                cachedData.remove(player.getName());
                Utility.log(player.getFormattedName() + ChatColor.RED + " not online, removing data!");
            }
        });
        Bukkit.getOnlinePlayers().stream().filter(p -> !cachedData.containsKey(p.getName())).forEach(p -> {
            GameCustomPlayer player = new GameCustomPlayer(p.getName());
            cachedData.put(p.getName(), player);
        });
    }

    private String lastMessage = null;
    private String name;
    private ArrayList<Rank> ranks = null;
    private ArrayList<String> friends = null;
    private ArrayList<String> friendRequests = null;
    private Collection<Infraction> infractions = null;
    private long lastLogin = -1;
    private long firstLogin = -1;

    private GameCustomPlayer(String name){
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

    public void updateLastLogin(){
        Database.getTable("PlayerData").updatePropertyForName(name, "LastLogin", Calendar.getInstance().getTimeInMillis());
    }

    public String getFormattedName(){
        if(isConsole()) return ChatColor.GRAY+name;
        if(hasRank(Rank.ADMIN))
            return ChatColor.RED+"Admin "+name+ ChatColor.RESET;
        if(hasRank(Rank.MODERATOR))
            return ChatColor.DARK_GREEN+"Mod "+name+ ChatColor.RESET;
        if(hasRank(Rank.HELPER))
            return ChatColor.BLUE+"Helper "+name+ ChatColor.RESET;
        if(hasRank(Rank.JR_DEV))
            return ChatColor.GREEN+"Jr Dev "+name+ ChatColor.RESET;
        if(hasRank(Rank.BUILDER))
            return ChatColor.DARK_AQUA+"Builder "+name+ ChatColor.RESET;
        return name;
    }

    public String getColoredName(){
        if(isConsole()) return ChatColor.GRAY+name;
        if(hasRank(Rank.ADMIN))
            return ChatColor.RED+name+ ChatColor.RESET;
        if(hasRank(Rank.MODERATOR))
            return ChatColor.DARK_GREEN+name+ ChatColor.RESET;
        if(hasRank(Rank.HELPER))
            return ChatColor.BLUE+name+ ChatColor.RESET;
        if(hasRank(Rank.JR_DEV))
            return ChatColor.GREEN+name+ ChatColor.RESET;
        if(hasRank(Rank.BUILDER))
            return ChatColor.DARK_AQUA+name+ ChatColor.RESET;
        return name;
    }

    public Rank getDominantRank(){
        if(isConsole()) return Rank.ADMIN;
        if(hasRank(Rank.ADMIN))
            return Rank.ADMIN;
        if(hasRank(Rank.MODERATOR))
            return Rank.MODERATOR;
        if(hasRank(Rank.HELPER))
            return Rank.HELPER;
        if(hasRank(Rank.JR_DEV))
            return Rank.JR_DEV;
        if(hasRank(Rank.BUILDER))
            return Rank.BUILDER;
        return Rank.DEFAULT;
    }

    public void updateInfractions(){
        getInfractions();
    }

    public Infraction getActiveInfraction(Infraction.InfractionType type){
        if(isConsole()) return null;
        if(infractions == null) getInfractions();
        assert infractions != null;
        ArrayList<GameInfraction> inf = new ArrayList<>();
        infractions.stream().filter(i -> !i.isExpired() && i.getType().equals(type)).forEach(inf::add);
        return inf.size() == 0 ? null : inf.get(0);
    }

    public Collection<Infraction> getInfractions(){
        if(isConsole()) return null;
        if(infractions != null) return infractions;
        infractions = Database.getInfractionsBulk(name);
        return infractions;
    }

    public void removeFriend(String username){
        if(friends == null) getFriends();
        assert friends != null;
        friends.remove(username);
        if(friends.isEmpty()){
            Database.getTable("PlayerData").updatePropertyForName(name, "Friends", null);
            return;
        }
        String fString = "";
        for(String s : friends){
            fString += ":"+s;
        }
        Database.getTable("PlayerData").updatePropertyForName(name, "Friends", fString.substring(1));
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

    public void requestFriend(String username){
        if(friendRequests == null) getFriendRequests();
        assert friendRequests != null;
        friendRequests.add(username);
        String fString = "";
        for(String s : friendRequests){
            fString += ":"+s;
        }
        Database.getTable("PlayerData").updatePropertyForName(name, "FRequests", fString.substring(1));
    }

    public void acceptRequest(String username){
        if(friends == null) getFriends();
        assert friends != null;
        friends.add(username);
        String fString = "";
        for(String s : friends){
            fString += ":"+s;
        }
        Database.getTable("PlayerData").updatePropertyForName(name, "Friends", fString.substring(1));

        //Very hack, much legit, such genius (it's an ugly hack but it should work :D)
        GameCustomPlayer player = new GameCustomPlayer(username);
        if(!player.hasFriend(name)){
            player.requestFriend(name);
            player.acceptRequest(name);
        }
        removeRequest(username);
    }

    public void removeRequest(String username){
        if(friendRequests == null) getFriendRequests();
        assert friendRequests != null;
        friendRequests.remove(username);
        if(friendRequests.isEmpty()){
            Database.getTable("PlayerData").updatePropertyForName(name, "FRequests", null);
            return;
        }
        String fString = "";
        for(String s : friendRequests){
            fString += ":"+s;
        }
        Database.getTable("PlayerData").updatePropertyForName(name, "FRequests", fString.substring(1));
    }

    public boolean hasFRequestFrom(String username){
        if(isConsole()) return true;
        if(!exists()) throw new RuntimeException("User does not exist, proxy didnt create one??!");
        if(friendRequests == null) getFriendRequests();
        assert friendRequests != null;
        return friendRequests.contains(username);
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

    public void addRank(Rank rank){
        ranks.add(rank);
        if(ranks.contains(Rank.DEFAULT)) ranks.remove(Rank.DEFAULT);
        Database.getTable("PlayerData").updatePropertyForName(name, "Ranks", getRankString());
    }

    public void removeRank(Rank rank){
        ranks.remove(rank);
        if(ranks.isEmpty()){
            ranks.add(Rank.DEFAULT);
            Database.getTable("PlayerData").updatePropertyForName(name, "Ranks", null);
            return;
        }
        Database.getTable("PlayerData").updatePropertyForName(name, "Ranks", getRankString());
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
            case BUILDER:
                pass = ranks.contains(Rank.BUILDER);
                break;
            case HELPER:
                pass = ranks.contains(Rank.HELPER) || ranks.contains(Rank.MODERATOR) || ranks.contains(Rank.JR_DEV);
                break;
            case MODERATOR:
                pass = ranks.contains(Rank.MODERATOR) || ranks.contains(Rank.JR_DEV);
                break;
            case JR_DEV:
                pass = ranks.contains(Rank.JR_DEV);
                break;
        }
        if(inform && !pass) noPermission();
        return pass;
    }

    public void message(String... message){
        if(isConsole()){
            Bukkit.getConsoleSender().sendMessage(message);
            return;
        }
        getPlayer().sendMessage(message);
    }

    public void setLastMessage(String username){
        lastMessage = username;
    }

    public String getLastMessage(){
        return lastMessage;
    }

    public boolean hasLastMessage(){
        return lastMessage != null;
    }

    public Player getPlayer(){
        if(isConsole()) return null;
        return Bukkit.getPlayerExact(name);
    }

    public boolean isOnline(){
        return getPlayer() != null;
    }

    public boolean exists() {
        if(isConsole()) return true;
        try{
            Database.getTable("PlayerData").getPropertyForName(name, "Name");
        }
        catch(IllegalArgumentException e){
            return false;
        }
        return true;
    }

    private void noPermission() {
        message(ChatColor.RED+"You do not have permission to do this!");
    }
}
