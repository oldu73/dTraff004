package ch.stageconcept.dtraff.servcon.model;

import javafx.beans.property.IntegerProperty;
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

    //TODO SSL

    /**
     * Default constructor.
     */
    public Connection() {
        this.host = null;
        this.port = null;
        this.user = null;
        this.password = null;
    }

    /**
     * Constructor with some initial data.
     *
     * @param host
     * @param port
     * @param user
     * @param password
     */
    public Connection(StringProperty host, IntegerProperty port, StringProperty user, StringProperty password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
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
