package cards.monarch.db.database;

import net.dv8tion.jda.api.JDA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * represents a discord user in the database
 *
 * @author danny
 * @vesrion 1
 */
public class DiscordUser {

    /**
     * the id of the discord user
     *
     * @since 1
     */
    private final long discordID;

    /**
     * a cache of the name to make the database more readable to humans
     *
     * @since 1
     */
    private String nameCache;

    /**
     * Init this with a discord user from the database.
     *
     * @param discordID discord user's id
     * @param nameCache the name of the discord user (not from the guild)
     * @since 1
     */
    public DiscordUser(long discordID, String nameCache) {
        this.discordID = discordID;
        this.nameCache = nameCache;
    }

    /**
     * Called to add a new discord user to the database.
     *
     * @param discordID  the discord user's id
     * @param name       the current name of the account.
     * @param connection the database connection
     * @throws SQLException thrown when an sql exception occurs
     */
    public DiscordUser(long discordID, String name, Connection connection) throws SQLException {
        this.discordID = discordID;
        this.nameCache = name;

        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement("insert into DiscordUsers values (? ?);")) {
            statement.setLong(0, discordID);
            statement.setString(1, name);
            result = statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        if (!result)
            throw new SQLException("unable to insert discord user");
    }

    /**
     * Updates a discord user in the database.
     *
     * @param connection the database connection
     * @throws SQLException thrown on an sql exception
     */
    private void updateDiscordUser(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "update discordusers set NameCache = ?" +
                        "where DiscordID=?")) {
            statement.setString(1, this.nameCache);
            statement.setLong(2, this.discordID);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public long getDiscordID() {
        return discordID;
    }

    public String getNameCache() {
        return nameCache;
    }

    public void setNameCache(JDA jda, Connection connection) throws SQLException {
        String nameCache = null;
        try {
            nameCache = jda.getUserById(this.discordID).getAsTag();
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        if (nameCache != null) {
            this.nameCache = nameCache;
            this.updateDiscordUser(connection);
        } else {
            System.err.printf("[Error]: The name cache for %s could not be updated.\n", this.nameCache);
        }
    }

}
