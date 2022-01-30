package cards.monarch.db;

import cards.monarch.db.database.DatabaseLogin;
import cards.monarch.db.database.DatabaseUser;
import cards.monarch.db.database.DiscordUser;
import cards.monarch.db.database.GuildConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * starts the discord bot and, rads the configs, inits the data sources and, adds commands
 *
 * @author danny
 * @version 1
 */
public class Main {

    /**
     * Database name for monarch cards data.
     *
     * @since 1
     */
    public static final String MONARCH_DB = "monarchdb";

    /**
     * The url of the database.
     *
     * @since 1
     */
    private static final String HOST = "127.0.0.1";

    /**
     * The port of the database.
     *
     * @since 1
     */
    private static final int PORT = 6446;

    /**
     * utils class
     */
    private Main() {
    }

    public static void main(String[] args) {
        // Get token
        if (args.length != 1) {
            System.out.println("Usage: java -jar bot.jar <token>");
            System.err.println("Error: No token.");
            System.exit(13);
        }
        String token = args[0];

        // Load database configuration
        DatabaseLogin databaseLogin = null;

        try {
            databaseLogin = new DatabaseLogin(HOST, PORT, MONARCH_DB);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to load database config - exiting.");
            System.exit(1);
        }

        // Load bot configuration from database
        BotManager botManager = new BotManager(databaseLogin, token);

        // Start bot
        try {
            final JDA jda = JDABuilder.createDefault("token")
                    .addEventListeners(new EventListener(botManager))
                    .build();
            try {
                jda.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Updates the cache and deletes expired accounts
            Thread pollingThread = new Thread(() -> {
                while (true) {
                    // Update database cache if needed
                    if (botManager.needsUpdate()) {
                        if (botManager.refreshDatabaseCache()) {
                            // Update the name cache on success
                            try {
                                botManager.getDatabaseLogin().connectAndExec(connection -> {
                                    botManager.updateNameCache(jda, connection);
                                });
                            } catch (SQLException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.err.println("Failed to update cache, will try again soon.");
                        }
                    }

                    // Delete old accounts

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Polling thread.");
            pollingThread.setDaemon(true);
            pollingThread.start();
        } catch (LoginException e) {
            System.out.println("Unable to login.");
            e.printStackTrace();
            System.exit(1);
        }
    }

}

/**
 * Parses the on ready event.
 *
 * @author danny
 * @version 1
 */
class EventListener implements net.dv8tion.jda.api.hooks.EventListener {

    private final BotManager botManager;

    public EventListener(BotManager botManager) {
        this.botManager = botManager;
    }

    @Override
    public void onEvent(GenericEvent event) {
        // Yucky instanceof statements
        if (event instanceof ReadyEvent) {
            System.out.println("API is ready!");
            this.botManager.refreshDatabaseCache();

            // Update the name cache in the database and the name cache to make manual database lookups easier
            try {
                this.botManager.getDatabaseLogin().connectAndExec(connection -> {
                    this.botManager.updateNameCache(event.getJDA(), connection);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println("Unable to find the postgresql driver. Exiting.");
                event.getJDA().shutdownNow();
            }

            // Add all guilds that the bot is in but has no record of.
            for (Guild guild : event.getJDA().getGuilds()) {
                if (!this.botManager.getGuildConfigs().containsKey(guild.getIdLong())) {
                    try {
                        this.botManager.getDatabaseLogin().connectAndExec((Connection connection) -> {
                            GuildConfig guildConfig = new GuildConfig(guild.getIdLong(), connection);
                            this.botManager.getGuildConfigs().put(guild.getIdLong(), guildConfig);
                        });
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Update name cache and add missing users
            for (User user : event.getJDA().getUsers()) {
                if (this.botManager.getDiscordUsers().containsKey(user.getIdLong())) {
                    // Update name caches
                    try {
                        this.botManager.getDatabaseLogin().connectAndExec((Connection connection) -> {
                            this.botManager.getDiscordUsers().get(user.getIdLong()).setNameCache(event.getJDA(), connection);
                        });
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Add all discord users to the database that the bot has no record of
                    try {
                        this.botManager.getDatabaseLogin().connectAndExec((Connection connection) -> {
                            DiscordUser discordUser = new DiscordUser(user.getIdLong(), user.getAsTag(), connection);
                            this.botManager.getDiscordUsers().put(user.getIdLong(), discordUser);
                        });
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Delete database accounts for users which are no longer in the guild/deleted
            for (DiscordUser user : this.botManager.getDiscordUsers().values()) {
                if (event.getJDA().getUserById(user.getDiscordID()) == null) {
                    try {
                        botManager.getDatabaseLogin().connectAndExec((Connection connection) -> {
                            for (DatabaseUser databaseUser : this.botManager.getDatabaseUsers()) {
                                if (databaseUser.getDiscordID() == user.getDiscordID()) {
                                    databaseUser.deleteUser(connection);
                                    break;
                                }
                            }
                        });
                    } catch (SQLException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
