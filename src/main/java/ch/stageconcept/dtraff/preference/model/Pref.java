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
    private boolean errorLoadingDataFromFilePopUpAtStartOrOnOpen;
    private boolean decryptConnFilePassAtStartOrOnOpen;

    /**
     * Constructor.
     */
    Pref() {
        pref = Preferences.userRoot().node(PREFS_PATH);

        // Preferences
        splashScreen = pref.getBoolean("splashScreen", true);
        errorLoadingDataFromFilePopUpAtStartOrOnOpen = pref.getBoolean("errorLoadingDataFromFilePopUpAtStartOrOnOpen", true);
        decryptConnFilePassAtStartOrOnOpen = pref.getBoolean("decryptConnFilePassAtStartOrOnOpen", false);

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

    public boolean isErrorLoadingDataFromFilePopUpAtStartOrOnOpen() {
        return errorLoadingDataFromFilePopUpAtStartOrOnOpen;
    }

    public void setErrorLoadingDataFromFilePopUpAtStartOrOnOpen(boolean errorLoadingDataFromFilePopUpAtStartOrOnOpen) {
        this.errorLoadingDataFromFilePopUpAtStartOrOnOpen = errorLoadingDataFromFilePopUpAtStartOrOnOpen;
        // Store value in preferences
        pref.putBoolean("errorLoadingDataFromFilePopUpAtStartOrOnOpen", errorLoadingDataFromFilePopUpAtStartOrOnOpen);
    }

    public boolean isDecryptConnFilePassAtStartOrOnOpen() {
        return decryptConnFilePassAtStartOrOnOpen;
    }

    public void setDecryptConnFilePassAtStartOrOnOpen(boolean decryptConnFilePassAtStartOrOnOpen) {
        this.decryptConnFilePassAtStartOrOnOpen = decryptConnFilePassAtStartOrOnOpen;
        // Store value in preferences
        pref.putBoolean("decryptConnFilePassAtStartOrOnOpen", decryptConnFilePassAtStartOrOnOpen);
    }
}
