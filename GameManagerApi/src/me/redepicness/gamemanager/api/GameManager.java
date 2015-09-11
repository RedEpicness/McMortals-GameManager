package me.redepicness.gamemanager.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class GameManager<A extends Game> implements Listener {

    private Game game;

    public void init(){
        if(getType().equals(GameManagerType.GAME)){
            game = registerGame();
        }
    }

    public A getGame(){
        if(getType().equals(GameManagerType.GAME)){
            return (A)game;
        }
        else
            throw new RuntimeException("cannot get game in lobby mode!");
    }

    public abstract Location getLobbyLocation();

    public abstract void playerJoin(Player player);

    public abstract A registerGame();

    public abstract GameManagerType getType();

    public enum GameManagerType{

        GAME, LOBBY

    }

}
