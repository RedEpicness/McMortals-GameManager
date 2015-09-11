package me.redepicness.gamemanager.api;

import org.bukkit.ChatColor;

public enum Rank {

    DEFAULT, ACE, ALLSTAR, CUBER, SURREAL, BUILDER, YOUTUBER, JR_DEV, HELPER, MODERATOR, ADMIN;

    public boolean isStaffRank() {
        return this == HELPER || this == MODERATOR || this == ADMIN;
    }

    public boolean isPaidRank() {
        return this == ACE || this == ALLSTAR || this == CUBER;
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
            case DEFAULT:
                return ChatColor.WHITE;
            case ACE:
                return ChatColor.GOLD;
            case ALLSTAR:
                return ChatColor.AQUA;
            case CUBER:
                return ChatColor.DARK_PURPLE;
            case SURREAL:
                return ChatColor.YELLOW;
            case BUILDER:
                return ChatColor.DARK_AQUA;
            case YOUTUBER:
                return ChatColor.LIGHT_PURPLE;
            case JR_DEV:
                return ChatColor.GREEN;
            case HELPER:
                return ChatColor.BLUE;
            case MODERATOR:
                return ChatColor.DARK_GREEN;
            case ADMIN:
                return ChatColor.RED;
            default:
                throw new RuntimeException("unhandled enum!");
        }
    }

    public String getName(){
        switch (this){
            case DEFAULT:
                return "";
            case ACE:
                return "Ace";
            case ALLSTAR:
                return "AllStar";
            case CUBER:
                return "Cuber";
            case SURREAL:
                return "Surreal";
            case BUILDER:
                return "Builder";
            case YOUTUBER:
                return "Youtuber";
            case JR_DEV:
                return "Jr Dev";
            case HELPER:
                return "Helper";
            case MODERATOR:
                return "Mod";
            case ADMIN:
                return "Admin";
            default:
                throw new RuntimeException("unhandled enum!");
        }
    }

    public String asPrefix(boolean resetColor){
        if(this.equals(DEFAULT)) return "";
        return getColor()+getName()+" "+(resetColor?ChatColor.RESET+"":"");
    }

    public String withColors(){
        return getColor()+this.toString()+ ChatColor.RESET;
    }

    public String getColoredName(){
        return getColor()+getName()+ChatColor.RESET;
    }

    public int getPriority(){
        switch (this){
            case DEFAULT:
                return 0;
            case ACE:
                return 1;
            case ALLSTAR:
                return 2;
            case CUBER:
                return 3;
            case SURREAL:
                return 4;
            case BUILDER:
                return 5;
            case YOUTUBER:
                return 7;
            case JR_DEV:
                return 6;
            case HELPER:
                return 8;
            case MODERATOR:
                return 9;
            case ADMIN:
                return 10;
            default:
                throw new RuntimeException("unhandled enum!");
        }
    }

}
