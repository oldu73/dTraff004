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
    private boolean splashScreen;
    private boolean decryptConnFilePassAtStart;

    /**
     * Constructor.
     */
    Pref() {
        pref = Preferences.userRoot().node(PREFS_PATH);

        // Preferences
        splashScreen = pref.getBoolean("splashScreen", true);
        decryptConnFilePassAtStart = pref.getBoolean("decryptConnFilePassAtStart", false);

        // #########################################################################

        // debug mode
        //System.out.println("Pref instantiated..");
    }

    // Getters and Setters
    // #####################################################################

    public boolean isSplashScreen() {
        return splashScreen;
    }

    public void setSplashScreen(boolean splashScreen) {
        this.splashScreen = splashScreen;
        // Store value in preferences
        pref.putBoolean("splashScreen", splashScreen);
    }

    public boolean isDecryptConnFilePassAtStart() {
        return decryptConnFilePassAtStart;
    }

    public void setDecryptConnFilePassAtStart(boolean decryptConnFilePassAtStart) {
        this.decryptConnFilePassAtStart = decryptConnFilePassAtStart;
        // Store value in preferences
        pref.putBoolean("decryptConnFilePassAtStart", decryptConnFilePassAtStart);
    }
}
