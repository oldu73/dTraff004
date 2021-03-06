package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.Conn;

/**
 * Database description fields.
 *
 * @author Olivier Durand
 */
public class DbDescriptor {

    private final String key;
    private final String name;  // Conn name
    private final String denomination;  // End user representation, MySQL instead of key value, mysql
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String driver;

    private final String icon;

    // Constructors
    // #####################################################################

    /**
     * Default constructor.
     */
    public DbDescriptor() {
        this(null, null, null, null, 0, null, null, null, null);
    }

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
    public DbDescriptor(String key, String name, String denomination, String host, int port, String user, String password, String driver, String icon) {
        this.key = key;
        this.name = name;
        this.denomination = denomination;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.driver = driver;

        this.icon = icon;

        /* The most common approach to register a driver is to use Java's Class.forName() method,
        to dynamically load the driver's class file into memory, which automatically registers it.
        This method is preferable because it allows you to make the driver registration configurable and portable.

        SRC: https://www.tutorialspoint.com/jdbc/jdbc-db-connections.htm
        */
        try {
            Class.forName(driver);
        }
        catch(ClassNotFoundException ex) {
            System.out.println("Error: unable to load driver class!");
        }
    }

    // Method ###

    @Override
    public String toString() {
        return denomination;
    }

    /**
     * Return base URL in order to establish database
     * conn (default pattern is MySQL (MariaDB), c.f. BaseUrl "enum" class)
     *
     * @param conn
     */
    public String getBaseUrl(Conn conn) {
        return BaseUrl.INSTANCE.getBaseUrl(conn);
    }

    // Getter ###

    public String getIcon() {
        return icon;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDenomination() {
        return denomination;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getDriver() {
        return driver;
    }
}
