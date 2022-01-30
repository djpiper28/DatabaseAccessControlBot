package cards.monarch.db.database;

import cards.monarch.db.BotManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Stores the information to connect and login to the databse. The user.txt file always contains the user information
 * and the other data for connections are read in BotConfig.
 *
 * @verison 1
 * @see BotManager
 */
public class DatabaseLogin {

    /**
     * The file name of the user.txt file.
     *
     * @since 1
     */
    private static final String USER_TXT = "user.txt";

    /**
     * The port of the database server.
     *
     * @since 1
     */
    private final int port;

    /**
     * Database hostname.
     *
     * @since 1
     */
    private final String host;
    /**
     * Database name.
     *
     * @since 1
     */
    private final String dataBaseName;
    /**
     * Database username.
     *
     * @since 1
     */
    private String username;
    /**
     * Database password.
     *
     * @since 1
     */
    private String password;

    /**
     * Reads the user.txt file and stores the username and password in this class.
     *
     * @throws IOException thrown when an error occurs reading the configuration file
     * @since 1
     */
    public DatabaseLogin(String host, int port, String dataBaseName) throws IOException {
        // Read username and password from file
        FileInputStream fis = new FileInputStream(new File(USER_TXT));
        Scanner scanner = new Scanner(fis);

        this.username = null;
        this.password = null;

        for (int i = 0; scanner.hasNext(); i++) {
            String line = scanner.nextLine();
            if (i == 0) {
                this.username = line;
            } else if (i == 1) {
                this.password = line;
            } else if (!line.equals("")) {
                System.err.printf("Error: line %s in %s is ignored.\n", line, USER_TXT);
            }
        }

        scanner.close();

        boolean valid = true;
        if (this.username == null) {
            System.err.println("Error: no username is defined.");
            valid = false;
        }
        if (this.password == null) {
            System.err.println("Error: no password is defined.");
            valid = false;
        }

        // Set the data passed to the constructor. This data comes from the config file.
        this.host = host;
        this.dataBaseName = dataBaseName;
        this.port = port;

        if (!valid) {
            throw new IOException("user.txt does not contain a username and password");
        }
    }

    /**
     * Combines the URL, port and database name to get the full URL for connecting to the database.
     *
     * @return the complete url for connecting to the database.
     */
    private String getDatabaseFullURL() {
        return String.format("jdbc://%s:%d/%s", this.host, this.port, this.dataBaseName);
    }

    /**
     * Connects to the database and returns the connection object.
     *
     * @return the connection to the database
     * @throws ClassNotFoundException thrown is the postgresql driver could not be found
     * @throws SQLException           thrown if there is an error connecting to the database
     */
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(this.getDatabaseFullURL(), this.username, this.password);
    }

    /**
     * Executes the code within the functional interface with resource management for the connection.
     *
     * @param databaseExec functional interface for the code to execute with the connection
     * @return returns true if no exception was thrown during execution
     * @throws SQLException           thrown on an SQL error with getting the connection
     * @throws ClassNotFoundException thrown on an error with getting the class driver for connection
     */
    public boolean connectAndExec(DatabaseExec databaseExec) throws SQLException, ClassNotFoundException {
        boolean res;
        try (Connection connection = this.getConnection()) {
            databaseExec.exec(connection);
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
            res = false;
        }

        return res;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }
}