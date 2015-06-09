package me.redepicness.gamemanager;

import me.redepicness.gamemanager.api.Infraction;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Miha on 6/3/2015.
 */
public class Database {

    private static Connection connection = null;

    public static void init(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/battlerealms?autoReconnect=true", "root", "cocksteelers");
        } catch (Exception ex) {
            throw new RuntimeException("Error connecting to database, aborting startup!", ex);
        }
    }

    private static void checkConnection(){
        try {
            if(!connection.isValid(0)){
                System.out.println("Connection check failed! Connection invalid! Trying to refresh!");
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://localhost/battlerealms?autoReconnect=true", "root", "cocksteelers");
                if (!connection.isValid(0)){
                    new RuntimeException("Connection invalid after refresh! Retrying!").printStackTrace();
                    checkConnection();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error checking database!!!", e);
        }
    }

    public static void end(){
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new RuntimeException("Error connecting to database, aborting startup!", ex);
        }
    }

    public static void insertInfraction(GameInfraction infraction){
        try{
            checkConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO Infractions (Time, Offender, Issuer, Type, Reason, Duration) " +
                    "VALUES (?, ?, ?, ?, ?, ?)");
            statement.setLong(1, infraction.getWhen());
            statement.setString(2, infraction.getOffender());
            statement.setString(3, infraction.getIssuer());
            statement.setString(4, infraction.getType().toString());
            statement.setString(5, infraction.getReason());
            statement.setInt(6, infraction.getDuration());
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e){
            throw new RuntimeException("Could not update infraction!", e);
        }
    }

    public static void expireInfraction(GameInfraction infraction){
        try{
            checkConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE Infractions SET Expired=1, WhoExpired=?, WhenExpired=? WHERE ID=?");
            statement.setString(1, infraction.getWhoExpired());
            statement.setLong(2, infraction.getWhenExpired());
            statement.setInt(3, infraction.getID());
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e){
            throw new RuntimeException("Could not update expire infraction!", e);
        }
    }

    public static Collection<Infraction> getInfractionsBulk(String offender){
        try{
            checkConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Infractions WHERE Offender=?");
            statement.setString(1, offender);
            ResultSet resultSet = statement.executeQuery();
            Collection<Infraction> infractions = new ArrayList<>();
            if(!resultSet.isBeforeFirst()){
                return infractions;
            }
            resultSet.first();
            do {
                GameInfraction infraction;
                if(resultSet.getBoolean("Expired")){
                    infraction = new GameInfraction(resultSet.getString("Issuer"), offender, resultSet.getLong("Time"), resultSet.getInt("Duration"),
                            resultSet.getString("Type"), resultSet.getString("Reason"), resultSet.getInt("ID"), resultSet.getString("WhoExpired"),
                            resultSet.getLong("WhenExpired"));
                }
                else{
                    infraction = new GameInfraction(resultSet.getString("Issuer"), offender, resultSet.getLong("Time"), resultSet.getInt("Duration"),
                            resultSet.getString("Type"), resultSet.getString("Reason"), resultSet.getInt("ID"));
                }
                infractions.add(infraction);
            }
            while (resultSet.next());
            statement.close();
            resultSet.close();
            return infractions;
        }
        catch (SQLException e){
            throw new RuntimeException("Could not obtain infractions for "+offender+"!", e);
        }
    }

    public static Database getTable(String name){
        return new Database(name);
    }

    private String tableName;

    private Database(String tableName){
        this.tableName = tableName;
    }

    public <T> T getPropertyForName(String username, String property){
        try{
            checkConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM "+tableName+" WHERE Name=?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.isBeforeFirst()){
                throw new IllegalArgumentException(username + " could not be found in the database!");
            }
            resultSet.first();
            T object = (T) resultSet.getObject(property);
            statement.close();
            resultSet.close();
            return object;
        }
        catch (SQLException e){
            throw new RuntimeException("Could not obtain "+property+" for "+username+"!", e);
        }
    }

    public <T> void updatePropertyForName(String username, String propertyName, T property){
        try{
            checkConnection();
            PreparedStatement statement = connection.prepareStatement("UPDATE "+tableName+" SET "+propertyName+"=? WHERE Name=?");
            statement.setObject(1, property);
            statement.setString(2, username);
            statement.executeUpdate();
            statement.close();
        }
        catch (SQLException e){
            throw new RuntimeException("Could not update "+propertyName+" to "+property+" for "+username+"!", e);
        }
    }
}
