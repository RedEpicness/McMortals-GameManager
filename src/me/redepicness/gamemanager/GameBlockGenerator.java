package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.BlockGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GameBlockGenerator implements BlockGenerator{

    public static final int BLOCKS_PER_TICK = 200;

    private Map<Location, Material> blocks;
    private boolean isGenerated = false;
    private Runnable runnable = null;

    public GameBlockGenerator(){
        blocks = new HashMap<>();
    }

    public BlockGenerator newGenerator() {
        return new GameBlockGenerator();
    }

    public void addBlock(Location location, Material material){
        if(isGenerated)
            throw new RuntimeException("Tried to add blocks after generation!");
        blocks.put(location, material);
    }

    public void postGenerate(Runnable runnable){
        if(isGenerated)
            throw new RuntimeException("Tried to add post task after generation!");
        this.runnable = runnable;
    }

    public void generate(Runnable postTask){
        if(isGenerated)
            throw new RuntimeException("Cannot generate twice!");
        isGenerated = true;
        int amount = BLOCKS_PER_TICK;
        int delay = 1;
        Map<Location, Material> toPlace = new HashMap<>();
        for (Entry<Location, Material> entry : blocks.entrySet()){
            if(amount == 0){
                scheduleBlocks(toPlace, delay);
                toPlace.clear();
                delay++;
                amount = BLOCKS_PER_TICK;
            }
            toPlace.put(entry.getKey(), entry.getValue());
            amount--;
        }
        scheduleBlocks(toPlace, delay);
        if(runnable != null){
            Bukkit.getScheduler().scheduleSyncDelayedTask(GManager.getInstance(), runnable::run, delay+20);
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(GManager.getInstance(), postTask::run, delay+40);
    }

    private void scheduleBlocks(Map<Location, Material> map, int delay){
        Map<Location, Material> fin = new HashMap<>();
        for (Entry<Location, Material> e : map.entrySet()) {
            fin.put(e.getKey(), e.getValue());
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(GManager.getInstance(), () -> {
            for (Entry<Location, Material> e : fin.entrySet()) {
                e.getKey().getBlock().setType(e.getValue());
            }
        }, delay);
    }

}
