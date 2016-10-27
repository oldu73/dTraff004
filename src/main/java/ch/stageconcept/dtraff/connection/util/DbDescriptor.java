package ch.stageconcept.dtraff.connection.util;

/**
 * Database description fields.
 *
 * @author Olivier Durand
 */
public class DbDescriptor {

    private final String name;  // Connection name
    private final String denomination;  // End user representation, MySQL instead of key value, mysql
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String driver;

    /**
     * Constructor.
     *
     * @param name
     * @param denomination
     * @param host
     * @param port
     * @param user
     * @param password
     * @param driver
     */
    public DbDescriptor(String name, String denomination, String host, int port, String user, String password, String driver) {
        this.name = name;
        this.denomination = denomination;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.driver = driver;
    }

    // Method ###

    @Override
    public String toString() {
        return denomination;
    }

    // Getter ###

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
