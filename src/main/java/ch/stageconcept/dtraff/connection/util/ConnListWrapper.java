package ch.stageconcept.dtraff.connection.util;

import ch.stageconcept.dtraff.connection.model.Conn;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Helper class to wrap a list of conns. This is used for saving the
 * list of conns to XML.
 *
 * @author Marco Jakob (adapted by Olivier Durand)
 */
@XmlRootElement(name = "conns")
public class ConnListWrapper {

    private List<Conn> conns;

    @XmlElement(name = "conn")
    public List<Conn> getConns() {
        return conns;
    }

    public void setConns(List<Conn> conns) {
        this.conns = conns;
    }
}
