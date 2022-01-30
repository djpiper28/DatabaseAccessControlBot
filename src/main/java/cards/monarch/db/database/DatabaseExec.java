package cards.monarch.db.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The interface for execution of code with a connection. It is designed like this to have resource management done by
 * the DatabaseLogin class.
 *
 * @see DatabaseLogin
 */
public interface DatabaseExec {
    void exec(Connection connection) throws SQLException;
}
