package me.redepicness.gamemanager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Connector {

    public static void connect(Player p, String server){
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        p.sendPluginMessage(GManager.getInstance(), "BungeeCord", out.toByteArray());
    }

}
