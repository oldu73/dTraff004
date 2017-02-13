package ch.stageconcept.dtraff.connection.util;

/**
 * ConnRoot object state with corresponding icon.
 *
 * @author Olivier Durand
 */
public enum ConnRootState {

    QUITE("network001.png"),
    OPENING_FILE("loadingGray001.gif");

    private String iconFileName ;

    ConnRootState(String iconFileName) {
        this.iconFileName = iconFileName ;
    }

    public String getIconFileName() {
        return  this.iconFileName ;
    }

}
