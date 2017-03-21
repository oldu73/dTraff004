package ch.stageconcept.dtraff.xrelease;

/**
 * Application release number provider.
 *
 * @author Olivier Durand
 */
public enum Release {

    // updated: 20170321-2036

    NUMBER(" r0.4.057");    // ! Don't miss first space character !

    private String value ;

    Release(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
