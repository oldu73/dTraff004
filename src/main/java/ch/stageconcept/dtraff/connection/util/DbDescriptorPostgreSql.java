package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.Conn;

/**
 * PostgreSQL database description specificity.
 *
 * @author Olivier Durand
 */
public class DbDescriptorPostgreSql extends DbDescriptor {

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
     * @param icon
     */
    public DbDescriptorPostgreSql(String key, String name, String denomination, String host, int port, String user, String password, String driver, String icon) {
        super(key, name, denomination, host, port, user, password, driver, icon);
    }

    // Method ###

    /**
     * Build base URL in order to establish database
     * conn, PostgreSQL = "jdbc:postgresql://127.0.0.1:port/user"
     *
     * @param conn
     */
    @Override
    public String getBaseUrl(Conn conn) {
        return BaseUrl.INSTANCE.getBaseUrl(conn) + "/" + conn.getUser();
    }
}
