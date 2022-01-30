package cards.monarch.db.database;

import cards.monarch.db.BotManager;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * configuration for a guild. See README.md for field definitions.
 *
 * @author danny
 * @version 1
 */
public class GuildConfig {

    private final boolean allowedAccess;
    private final long guildID;
    private final long databaseStatusCategoryID;
    private final long userChangeLogChannelID;
    private final long activeUserChannelID;
    private final long administratorRoleID;

    public GuildConfig(long guildID, Connection connection) throws SQLException {
        this.guildID = guildID;
        this.allowedAccess = false;
        this.databaseStatusCategoryID = -1;
        this.userChangeLogChannelID = -1;
        this.activeUserChannelID = -1;
        this.administratorRoleID = -1;

        // Try to insert guild configuration into table
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(
                "insert into GuildSettings values (? ? ? ? ? ?);")) {
            statement.setLong(0, this.guildID);
            statement.setBoolean(1, this.allowedAccess);
            statement.setLong(2, this.databaseStatusCategoryID);
            statement.setLong(3, this.userChangeLogChannelID);
            statement.setLong(4, this.activeUserChannelID);
            statement.setLong(5, this.administratorRoleID);
            result = statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        if (!result)
            throw new SQLException("unable to insert guild config");
    }

    /**
     * sets the guild configuration up with the specified data from the database.
     *
     * @param guildID                  the id of the guild this config is for
     * @param allowedAccess            whether the guild is allowed access for the database
     * @param databaseStatusCategoryID the id of the category for the database status. -1 for no category.
     * @param userChangeLogChannelID   the id of the channel to log user changes to. -1 for no category.
     * @param activeUserChannelID      the id of the channel to show the active users in. -1 for no channel;
     * @param administratorRoleID      the id of the role for administrators of the database accounts. -1 for no role.
     * @since 1
     */
    public GuildConfig(long guildID, boolean allowedAccess, long databaseStatusCategoryID, long userChangeLogChannelID,
                       long activeUserChannelID, long administratorRoleID) {
        this.guildID = guildID;
        this.allowedAccess = allowedAccess;
        this.databaseStatusCategoryID = databaseStatusCategoryID;
        this.userChangeLogChannelID = userChangeLogChannelID;
        this.activeUserChannelID = activeUserChannelID;
        this.administratorRoleID = administratorRoleID;
    }

    /**
     * Updates a modified guild configuration.
     *
     * @param connection connection
     * @throws SQLException thrown when an error with the SQl command occurs
     * @since 1
     */
    private void updateGuildConfig(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "update GuildSettings set GuildID = ?, AllowedAccess = ?, DatabaseStatusCategoryID = ?, " +
                        "UserChangeLogChannelID = ?, ActiveUserChannelID = ?, AdministratorRoleID = ? " +
                        "where GuildId=?;")) {
            statement.setLong(0, this.guildID);
            statement.setLong(1, this.databaseStatusCategoryID);
            statement.setLong(2, this.userChangeLogChannelID);
            statement.setLong(3, this.activeUserChannelID);
            statement.setLong(4, this.administratorRoleID);
            statement.setLong(5, this.guildID);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public long getGuildID() {
        return guildID;
    }

    public boolean isAllowedAccess() {
        return allowedAccess;
    }

    public long getDatabaseStatusCategoryID() {
        return databaseStatusCategoryID;
    }

    public long getUserChangeLogChannelID() {
        return userChangeLogChannelID;
    }

    public long getActiveUserChannelID() {
        return activeUserChannelID;
    }

    public long getAdministratorRoleID() {
        return administratorRoleID;
    }

    /**
     * Tests if a user has permissions to modify the database users.
     *
     * @param user the user to test the permissions of
     * @return whether the user has permissions to modify users
     * @since 1
     */
    public boolean hasPermissions(User user) {
        if (user.getIdLong() == BotManager.DATABASE_ADMIN_DISCORD_ID) return true;
        for (Role role : user.getJDA().getRoles()) {
            if (role.getIdLong() == this.administratorRoleID) return true;
        }

        return false;
    }

}
