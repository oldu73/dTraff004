package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.DbConnect;

/**
 * Database description fields.
 *
 * @author Olivier Durand
 */
public abstract class DbDescriptor {

    private final String key;
    private final String name;  // Connection name
    private final String denomination;  // End user representation, MySQL instead of key value, mysql
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String driver;

    // Constructors
    // #####################################################################

    /**
     * Default constructor.
     */
    public DbDescriptor() {
        this(null, null, null, null, 0, null, null, null);
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
     */
    public DbDescriptor(String key, String name, String denomination, String host, int port, String user, String password, String driver) {
        this.key = key;
        this.name = name;
        this.denomination = denomination;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.driver = driver;

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
     * Build base URL in order to establish database
     * connection (e.g. jdbc:mysql://127.0.0.1 for MySQL)
     *
     * @param dbConnect
     */
    public abstract String getBaseUrl(DbConnect dbConnect);

    // Getter ###

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
