package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.DbConnect;

/**
 * MySQL database description fields.
 *
 * @author Olivier Durand
 */
public class DbDescriptorMySql extends DbDescriptor {

    /**
     * Constructor.
     *
     * @param key
     * @param name
     * @param denomination
     * @param host
     * @param port
     * @param user
     * @param password
     * @param driver
     */
    public DbDescriptorMySql(String key, String name, String denomination, String host, int port, String user, String password, String driver) {
        super(key, name, denomination, host, port, user, password, driver);
    }

    // Method ###

    /**
     * Build base URL in order to establish database
     * connection, MySQL = "jdbc:mysql://127.0.0.1:port"
     *
     * @param dbConnect
     */
    @Override
    public String getBaseUrl(DbConnect dbConnect) {

        return null;
    }
}
