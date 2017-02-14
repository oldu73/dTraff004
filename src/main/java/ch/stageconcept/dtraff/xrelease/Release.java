package ch.stageconcept.dtraff.xrelease;

/**
 * Application release number provider.
 *
 * @author Olivier Durand
 */
public enum Release {

    // updated: 20170214-1347

    NUMBER(" r0.4.028");    // ! Don't miss first space character !

    private String value ;

    Release(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
