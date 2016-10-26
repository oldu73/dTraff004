package ch.stageconcept.dtraff.servcon.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for a server connection.
 *
 * @author Olivier Durand
 */
public class Connection {

    private final StringProperty host;
    private final IntegerProperty port;
    private final StringProperty user;
    private final StringProperty password;

    //TODO Server Type (MySQL, MariaDB, PostgreSQL)
    //TODO Save password option
    //TODO SSL

    /**
     * Default constructor.
     */
    public Connection() {
        this(null, 0, null, null);
    }

    /**
     * Constructor.
     *
     * @param host
     * @param port
     * @param user
     * @param password
     */
    public Connection(String host, Integer port, String user, String password) {
        this.host = new SimpleStringProperty(host);
        this.port = new SimpleIntegerProperty(port);
        this.user = new SimpleStringProperty(user);
        this.password = new SimpleStringProperty(password);
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    public int getPort() {
        return port.get();
    }

    public IntegerProperty portProperty() {
        return port;
    }

    public void setPort(int port) {
        this.port.set(port);
    }

    public String getUser() {
        return user.get();
    }

    public StringProperty userProperty() {
        return user;
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }
}
