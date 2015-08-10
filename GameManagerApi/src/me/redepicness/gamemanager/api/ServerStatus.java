package me.redepicness.gamemanager.api;

public interface ServerStatus {

    void registerStatusListener(String forwardTarget, String filter);

}
