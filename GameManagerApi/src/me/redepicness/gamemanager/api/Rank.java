package me.redepicness.gamemanager.api;

import org.bukkit.ChatColor;

/**
 * Created by Miha on 6/7/2015.
 */
public enum Rank {

    DEFAULT, BUILDER, HELPER, MODERATOR, JR_DEV, ADMIN;

    public boolean isStaffRank() {
        return this == HELPER || this == MODERATOR || this == JR_DEV || this == ADMIN;
    }

    public boolean isPaidRank() {
        return false;
    }

    public static boolean isValid(String name){
        try {
            Rank.valueOf(name);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public ChatColor getColor(){
        switch (this){
            case BUILDER:
                return ChatColor.DARK_AQUA;
            case HELPER:
                return ChatColor.BLUE;
            case MODERATOR:
                return ChatColor.DARK_GREEN;
            case JR_DEV:
                return ChatColor.GREEN;
            case ADMIN:
                return ChatColor.RED;
            default:
                return ChatColor.WHITE;
        }
    }

    public String asPrefix(boolean resetColor){
        switch (this){
            default:
                return ChatColor.RESET+"";
            case BUILDER:
                return ChatColor.DARK_AQUA+"Builder "+(resetColor?ChatColor.RESET+"":"");
            case HELPER:
                return ChatColor.BLUE+"Helper "+(resetColor?ChatColor.RESET+"":"");
            case MODERATOR:
                return ChatColor.DARK_GREEN+"Mod "+(resetColor?ChatColor.RESET+"":"");
            case JR_DEV:
                return ChatColor.GREEN+"Jr Dev "+(resetColor?ChatColor.RESET+"":"");
            case ADMIN:
                return ChatColor.RED+"Admin "+(resetColor?ChatColor.RESET+"":"");
        }
    }

    public String withColors(){
        switch (this){
            case DEFAULT:
                return ChatColor.WHITE+this.toString()+ChatColor.RESET;
            case BUILDER:
                return ChatColor.DARK_AQUA+this.toString()+ChatColor.RESET;
            case HELPER:
                return ChatColor.BLUE+this.toString()+ChatColor.RESET;
            case MODERATOR:
                return ChatColor.DARK_GREEN+this.toString()+ChatColor.RESET;
            case JR_DEV:
                return ChatColor.GREEN+this.toString()+ChatColor.RESET;
            case ADMIN:
                return ChatColor.RED+this.toString()+ChatColor.RESET;
            default:
                return this.toString();
        }
    }

}
