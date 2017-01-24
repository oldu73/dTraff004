package ch.stageconcept.dtraff.preference.model;

import java.util.prefs.Preferences;

/**
 * Manage user preferences
 *
 * @author Olivier Durand
 */
public enum Pref {

    //TODO Check other enum if INSTANCE (let semicolon) is really necessary!
    ;

    public static final String PREFS_PATH = "/ch/stageconcept/datatraffic/pref";
    private static Preferences pref = Preferences.userRoot().node(PREFS_PATH);

    // Preferences
    private static boolean splashScreen = pref.getBoolean("splashScreen", true);
    private static boolean errorLoadingFilePopUpAtStartOrOnOpen = pref.getBoolean("errorLoadingFilePopUpAtStartOrOnOpen", true);
    private static boolean decryptFilePassPopUpAtStartOrOnOpen = pref.getBoolean("decryptFilePassPopUpAtStartOrOnOpen", false);

    /**
     * Constructor.
     */
    Pref() {
        // debug mode
        //System.out.println("Pref instantiated..");
    }

    // #####################################################################

    // Getters and Setters
    // #####################################################################

    public static boolean isSplashScreen() {
        return splashScreen;
    }

    public static void setSplashScreen(boolean splashScreenParam) {
        splashScreen = splashScreenParam;
        // Store value in preferences
        pref.putBoolean("splashScreen", splashScreen);
    }

    public static boolean isErrorLoadingFilePopUpAtStartOrOnOpen() {
        return errorLoadingFilePopUpAtStartOrOnOpen;
    }

    public static void setErrorLoadingFilePopUpAtStartOrOnOpen(boolean errorLoadingDataFromFilePopUpAtStartOrOnOpenParam) {
        errorLoadingFilePopUpAtStartOrOnOpen = errorLoadingDataFromFilePopUpAtStartOrOnOpenParam;
        // Store value in preferences
        pref.putBoolean("errorLoadingFilePopUpAtStartOrOnOpen", errorLoadingFilePopUpAtStartOrOnOpen);
    }

    public static boolean isDecryptFilePassPopUpAtStartOrOnOpen() {
        return decryptFilePassPopUpAtStartOrOnOpen;
    }

    public static void setDecryptFilePassPopUpAtStartOrOnOpen(boolean decryptConnFilePassAtStartOrOnOpenParam) {
        decryptFilePassPopUpAtStartOrOnOpen = decryptConnFilePassAtStartOrOnOpenParam;
        // Store value in preferences
        pref.putBoolean("decryptFilePassPopUpAtStartOrOnOpen", decryptFilePassPopUpAtStartOrOnOpen);
    }

    // #####################################################################

}
