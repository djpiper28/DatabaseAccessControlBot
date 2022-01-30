package cards.monarch.db.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a user account in the database.
 *
 * @author danny
 * @version 1
 */
public class DatabaseUser {

    /**
     * Usenames that are system reserved.
     *
     * @since 1
     */
    private static final String[] FORBIDDEN_USENAMES = {"danny", "admin"};
    private final UUID databaseUserID;
    private final long discordID;
    private final long guildID;
    private final String userName;
    private Date creationTime;
    private Date deletionTime;

    /**
     * Creates a new database user then puts it into the database. The UUID for the user is generated here.
     *
     * @param userName   the usename of the account
     * @param password   the password of the account (not stored)
     * @param discordID  the discord id of the account owner
     * @param guildID    the guild if of where it was issued
     * @param connection the database connection
     * @throws SQLException       thrown on an sql error
     * @throws IllegalAccessError thrown if the user already exists or, if the username is forbidden
     * @see #FORBIDDEN_USENAMES
     * @since 1
     */
    public DatabaseUser(String userName, String password, long discordID, long guildID, Connection connection) throws SQLException, IllegalAccessError {
        this.databaseUserID = UUID.randomUUID();
        this.discordID = discordID;
        this.guildID = guildID;
        this.userName = userName;
        this.creationTime = null;
        this.deletionTime = null;

        // Check to see if it is an allowed username
        for (String forbiddenName : FORBIDDEN_USENAMES) {
            if (userName.equals(forbiddenName))
                throw new IllegalAccessError("This is a system reserved name.");
        }

        try {
            this.createUser(connection, password);
        } catch (SQLException | IllegalAccessError e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Constructor for use when reading data from the database
     *
     * @param databaseUserID the uuid as read from database
     * @param discordID      the discord user id as read from the database
     * @param guildID        the discord guild id as read from the database
     * @param userName       the username as read from the database
     * @param creationTime   the creation time as read from the database
     * @param deletionTime   the deletion time as read from the database
     * @since 1
     */
    public DatabaseUser(UUID databaseUserID, long discordID, long guildID, String userName, Date creationTime, Date deletionTime) {
        this.databaseUserID = databaseUserID;
        this.discordID = discordID;
        this.guildID = guildID;
        this.userName = userName;
        this.creationTime = creationTime;
        this.deletionTime = deletionTime;
    }

    /**
     * Get the database account'owner's discord id.
     *
     * @return the discord id of the owner of the database account
     * @since 1
     */
    public long getDiscordID() {
        return this.getDiscordID();
    }

    /**
     * Checks if a user exists in the database.
     *
     * @param connection the database connection
     * @return a boolean of whether the user exists
     * @throws SQLException thrown when an error reading the database occurs
     * @since 1
     */
    private boolean userExists(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select usename from pg_catalog.pg_user where usename=?");
        statement.setString(0, this.userName);
        ResultSet res = statement.executeQuery("");

        int size = 0;
        if (res.last()) {
            size = res.getRow();
            res.beforeFirst();
        }

        statement.close();
        return size > 0;
    }

    /**
     * Creates the user if it does not exist already.
     *
     * @param connection database connection
     * @param password   user's password (this is never stored)
     * @throws SQLException       thrown when an SQL error occurs
     * @throws IllegalAccessError thrown when the user already exists
     * @since 1
     */
    private void createUser(Connection connection, String password) throws SQLException, IllegalAccessError {
        if (userExists(connection))
            throw new IllegalAccessError("User already exists.");

        PreparedStatement statement = connection.prepareStatement("create user ? with password ?;");
        statement.setString(0, this.userName);
        statement.setString(1, password);

        boolean result = statement.execute();
        statement.close();
        statement = connection.prepareStatement("grant select on players, tournaments, tournamentplayers, matches, decks, " +
                "tournamentdecks, deckcards, matchplayers, cards to ?;");
        statement.setString(0, this.userName);

        boolean result2 = statement.execute();
        statement.close();

        statement = connection.prepareStatement("insert into databaseusers values (? ? ? ? ? ? false);");
        //TODO: set values

        boolean result3 = statement.execute();
        statement.close();

        if (!(result3 || result2 || result)) {
            System.err.printf("[ERROR]: Cannot create user result %b result2 %b\n", result, result2);
            throw new SQLException("PiPi in my pampers");
        } else {
            this.creationTime = Date.from(Instant.now());
        }
    }

    public void deleteUser(Connection connection) throws SQLException, IllegalAccessError {
        if (!userExists(connection))
            throw new IllegalAccessError("User does not exist");

        PreparedStatement statement = connection.prepareStatement("revoke * on players, tournaments, tournamentplayers, matches, decks, " +
                "tournamentdecks, deckcards, matchplayers, cards to ?;");
        statement.setString(0, this.userName);

        boolean result2 = statement.execute();
        statement.close();

        statement = connection.prepareStatement("drop user ?;");
        statement.setString(0, this.userName);

        boolean result = statement.execute();
        statement.close();

        statement = connection.prepareStatement("update DatabaseUsers set active=false where DatabaseUserID = ?;");
        statement.setString(0, this.databaseUserID.toString());
        boolean result3 = statement.execute();
        statement.close();

        if (!(result3 || result2 || result)) {
            System.err.printf("[ERROR]: Cannot drop user result %b result2 %b\n", result, result2);
            throw new SQLException("PiPi in my pampers");
        } else {
            this.deletionTime = Date.from(Instant.now());
        }
    }

}
