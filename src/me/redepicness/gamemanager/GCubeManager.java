package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class GCubeManager implements CubeManager{

    private static ArrayList<CubeManager> managers = new ArrayList<>();

    static void forcePush(){
        managers.stream().filter(manager -> !manager.isFinished()).forEach(me.redepicness.gamemanager.api.CubeManager::pushToDatabase);
    }

    public CubeManager getNewInstance(){
        CubeManager manager = new GCubeManager();
        managers.add(manager);
        ArrayList<CubeManager> toRemove = new ArrayList<>();
        managers.stream().filter(CubeManager::isFinished).forEach(toRemove::add);
        toRemove.forEach(managers::remove);
        return manager;
    }

    private ArrayList<CubeTransaction> transactions = new ArrayList<>();
    private boolean finished = false;

    @Override
    public int getCubes(Player player) {
        int cubes = 0;
        for(CubeTransaction transaction : transactions){
            if(transaction.getUuid().equals(player.getName())){
                cubes += transaction.getAmount();
            }
        }
        return cubes;
    }

    @Override
    public void addCubes(Player player, int amount, String reason) {
        if(finished) throw new RuntimeException("cannot add coins after finish!");
        int multiplier = 1;
        String name = "";
        for(String p : Manager.getGameManager().getGame().getPlayers()){
            CustomPlayer pl = Utility.getPlayer(p);
            if(pl.hasPermission(Rank.CUBER)){
                name = p;
                multiplier = 4;
                break;
            }
            else if(multiplier < 3 && pl.hasPermission(Rank.ALLSTAR)){
                name = p;
                multiplier = 3;
            }
            else if(multiplier < 2 && pl.hasPermission(Rank.ACE)){
                name = p;
                multiplier = 2;
            }
        }
        amount *= multiplier;
        transactions.add(new CubeTransaction(player.getUniqueId().toString(), amount, reason));
        player.sendMessage(ChatColor.DARK_PURPLE+"You recieved "+amount+" cubes!"+(name.equals("")?"":" ("+name+"'s multiplier ("+multiplier+"x))"));
        //TODO check for multiplier
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void pushToDatabase() {
        if(finished) throw new RuntimeException("cannot push after finish!");
        finished = true;
        for(CubeTransaction transaction : transactions){
            Database.insertCubeTransaction(transaction.getTime(), transaction.getUuid(), transaction.getAmount(), transaction.getReason());
            GameCustomPlayer player = (GameCustomPlayer)Utility.getPlayer(Bukkit.getPlayer(UUID.fromString(transaction.getUuid())));
            player.incrementCubes(transaction.getAmount());
        }
        //TODO LOG ALL TRANSACTIONS
        //TODO AND PUSH TO DB!
    }

    private class CubeTransaction {

        private String uuid;
        private int amount;
        private String reason;
        private long time;

        private CubeTransaction(String uuid, int amount, String reason){
            this.uuid = uuid;
            this.amount = amount;
            this.reason = reason;
            time = System.currentTimeMillis();
        }

        public int getAmount() {
            return amount;
        }

        public String getUuid() {
            return uuid;
        }

        public String getReason() {
            return reason;
        }

        public long getTime() {
            return time;
        }

    }

}
