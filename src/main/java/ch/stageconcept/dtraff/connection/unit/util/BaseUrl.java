package ch.stageconcept.dtraff.connection.unit.util;

import ch.stageconcept.dtraff.connection.tree.model.Connection;

/**
 * Build default base URL in order to establish database
 * connection: - "jdbc:driver://host:port"
 * MySQL (MariaDB) base URL pattern is used as the default pattern
 * E.g. jdbc:mysql://127.0.0.1:port
 *
 * @author Olivier Durand
 */
public enum BaseUrl {

    INSTANCE;

    // Default value
    private static final String DRIVER_TYPE = "jdbc";

    /**
     * Return default connection base URL
     *
     * @param connection
     */
    public String getBaseUrl(Connection connection) {
        // Key value is also used as seed string in base URL (c.f. DbType class)
        return DRIVER_TYPE + ":" + connection.getKey() + "://" + connection.getHost() + ":" + connection.getPort();
    }

}
