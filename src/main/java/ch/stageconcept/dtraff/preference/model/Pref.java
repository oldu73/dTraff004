package ch.stageconcept.dtraff.preference.model;

import java.util.prefs.Preferences;

/**
 * Manage user preferences
 *
 * @author Olivier Durand
 */
public enum Pref {

    INSTANCE;

    public static final String PREFS_PATH = "/ch/stageconcept/datatraffic/pref";
    private Preferences pref;

    // Preferences
    private boolean decryptConnFilePassAtStart;

    /**
     * Constructor.
     */
    Pref() {
        pref = Preferences.userRoot().node(PREFS_PATH);

        // Preferences
        decryptConnFilePassAtStart = pref.getBoolean("decryptConnFilePassAtStart", false);

        // #########################################################################

        // debug mode
        System.out.println("Pref instantiated..");
    }

    // Getters and Setters
    // #####################################################################

    public boolean isDecryptConnFilePassAtStart() {
        return decryptConnFilePassAtStart;
    }

    public void setDecryptConnFilePassAtStart(boolean decryptConnFilePassAtStart) {
        this.decryptConnFilePassAtStart = decryptConnFilePassAtStart;
        // Store value in preferences
        pref.putBoolean("decryptConnFilePassAtStart", decryptConnFilePassAtStart);
    }
}
