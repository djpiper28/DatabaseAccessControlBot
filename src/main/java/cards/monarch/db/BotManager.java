package cards.monarch.db;

import cards.monarch.db.database.DatabaseLogin;
import cards.monarch.db.database.DatabaseUser;
import cards.monarch.db.database.DiscordUser;
import cards.monarch.db.database.GuildConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * stores the configuration for the bot, the database objects for the bot and, the refresh database methods.
 *
 * @author danny
 * @version 1
 */
public class BotManager {

    /**
     * The database administrator's discord id. They are the only one who can use the init commands and always have
     * admin permissions. This is currently set to Danny P.#6969 's
     *
     * @since 1
     */
    public static final long DATABASE_ADMIN_DISCORD_ID = 219813528566104064L;
    /**
     * Frequency to refresh the database.
     *
     * @since 1
     */
    private static final long UPDATE_DATABASE_CACHE_LIFE_MS = 10L * 1000L;
    /**
     * Database login details.
     *
     * @since 1
     */
    private final DatabaseLogin databaseLogin;
    /**
     * Maps guild ids to guild configs.
     *
     * @since 1
     */
    private final Map<Long, GuildConfig> guildConfigs;
    /**
     * Maps discord ids to discord users.
     *
     * @since 1
     */
    private final Map<Long, DiscordUser> discordUsers;
    /**
     * The bot's discord auth token
     *
     * @since 1
     */
    private final String token;
    /**
     * Stores all discord users in an unsorted list.
     *
     * @since 1
     */
    private List<DatabaseUser> databaseUsers;
    /**
     * Last time the database was updated (stored as values from System.currentMillis()).
     *
     * @since 1
     */
    private long databaseLastUpdateTime;

    /**
     * Sets up the bot configuration and loads the guild settings from the database
     *
     * @param databaseLogin login details for the databases (monarchdb and userbotdb)
     * @param token         discord bot's token
     * @since 1
     */
    public BotManager(DatabaseLogin databaseLogin, String token) {
        this.databaseLogin = databaseLogin;
        this.guildConfigs = new HashMap<>();
        this.discordUsers = new HashMap<>();
        this.token = token;
        this.databaseLastUpdateTime = System.currentTimeMillis();
        this.refreshDatabaseCache();
    }

    /**
     * Get the database admin for SlashCommands enabledUsers field/
     *
     * @return an array of one element which is the database admin.
     * @since 1
     */
    public String[] getEnabledUser() {
        return new String[]{String.valueOf(DATABASE_ADMIN_DISCORD_ID)};
    }

    /**
     * Get the admin roles for the server for the slash commands permissions.
     *
     * @param jda
     * @return an array of string ids of admin roles for the server
     * @since 1
     */
    public String[] getAdminRoles(JDA jda) {
        List<String> adminRoles = new LinkedList<>();
        for (Guild guild : jda.getGuilds()) {
            for (Role role : guild.getRoles()) {
                if (role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                    adminRoles.add(role.getId());
                }
            }
        }

        return adminRoles.toArray(new String[0]);
    }

    public DatabaseLogin getDatabaseLogin() {
        return databaseLogin;
    }

    public Map<Long, GuildConfig> getGuildConfigs() {
        return guildConfigs;
    }

    public Map<Long, DiscordUser> getDiscordUsers() {
        return discordUsers;
    }

    public List<DatabaseUser> getDatabaseUsers() {
        return databaseUsers;
    }

    public String getToken() {
        return token;
    }

    /**
     * Gets the data from the database and loads it into the cache.
     *
     * @return whether the database cachge was updated successfully
     * @since 1
     */
    public boolean refreshDatabaseCache() {
        try {
            boolean result = this.databaseLogin.connectAndExec(connection -> {
                this.refreshGuildConfigs(connection);
                this.refreshDiscordUsers(connection);
                this.refreshDatabaseUsers(connection);
            });

            if (result) {
                this.databaseLastUpdateTime = System.currentTimeMillis();
            } else {
                System.err.println("[ERROR]: Unable to update database cache.");
            }
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Refreshes the guild config cache from the database.
     *
     * @param connection SQL database connection.
     * @throws SQLException thrown on SQL database error.
     */
    private void refreshGuildConfigs(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("select * from GuildSettings;");

        while (result.next()) {
            long guildId = result.getLong("GuildID");
            boolean allowedAccess = result.getBoolean("AllowedAccess");
            long databaseStatusCategoryID = result.getLong("DatabaseStatusCategoryID");
            long userChangeLogChannelID = result.getLong("UserChangeLogChannelID");
            long activeUserChannelID = result.getLong("ActiveUserChannelID");
            long administratorRoleID = result.getLong("AdministratorRoleID");

            GuildConfig guildConfig = new GuildConfig(guildId, allowedAccess, databaseStatusCategoryID, userChangeLogChannelID,
                    activeUserChannelID, administratorRoleID);

            this.guildConfigs.put(guildId, guildConfig);
        }

        statement.close();
    }

    /**
     * Refreshes the discord user cache.
     *
     * @param connection SQL database connection.
     * @throws SQLException thrown on SQL database error.
     */
    private void refreshDiscordUsers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("select * from DiscordUsers;");

        while (result.next()) {
            long id = result.getLong("DiscordID");
            String name = result.getString("NameCache");
            DiscordUser discordUser = new DiscordUser(id, name);

            this.discordUsers.put(id, discordUser);
        }

        statement.close();
    }

    /**
     * Refreshes the database user cache.
     *
     * @param connection SQL database connection.
     * @throws SQLException thrown on SQL database error.
     * @since 1
     */
    private void refreshDatabaseUsers(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("select * from DatabaseUsers;");

        while (result.next()) {
            String UUID = result.getString("DatabaseUserID");
            long guildId = result.getLong("GuildID");
            long discordId = result.getLong("DiscordID");
            String name = result.getString("UserName");
            Date creationTime = Date.from(result.getDate("CreationTime").toInstant());
            Date deletionTime = Date.from(result.getDate("DeletionTime").toInstant());

            DatabaseUser databaseUser = new DatabaseUser(java.util.UUID.fromString(UUID), guildId, discordId, name, creationTime, deletionTime);
            this.databaseUsers.add(databaseUser);
        }

        statement.close();
    }

    /**
     * Updates the name cache of all users in the database.
     *
     * @param jda the jda of the discord bot
     * @param connection the database connection
     * @throws SQLException thrown when an error occurs updating the value of the name cache
     * @since 1
     */
    public void updateNameCache(JDA jda, Connection connection) throws SQLException {
        for (DiscordUser user: this.discordUsers.values()) {
            user.setNameCache(jda, connection);
        }
    }

    /**
     * Check if the database cache needs to be updated.
     *
     * @return whether the cache exceeds the maximum cache life length
     * @see #UPDATE_DATABASE_CACHE_LIFE_MS
     * @since 1
     */
    public boolean needsUpdate() {
        return System.currentTimeMillis() - this.databaseLastUpdateTime >= UPDATE_DATABASE_CACHE_LIFE_MS;
    }

}
