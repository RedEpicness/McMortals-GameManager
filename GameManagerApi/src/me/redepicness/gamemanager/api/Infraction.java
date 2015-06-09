package me.redepicness.gamemanager.api;

public interface Infraction {

    boolean isExpired();

    String getIssuer();

    String getOffender();

    long getWhen();

    int getDuration();

    InfractionType getType();

    String getReason();

    int getID();

    String getWhoExpired();

    long getWhenExpired();

    enum InfractionType {

        BAN, TEMP_BAN, MUTE, TEMP_MUTE, KICK;

        public static boolean isValid(String name){
            try{
                InfractionType.valueOf(name);
            }
            catch (IllegalArgumentException e){
                return false;
            }
            return true;
        }

    }

}
