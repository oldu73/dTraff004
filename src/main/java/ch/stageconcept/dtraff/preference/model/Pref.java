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
    private static String language = pref.get("language", "en");
    private static boolean warnRemoveEmptyFileOnClose = pref.getBoolean("warnRemoveEmptyFileOnClose", true);
    private static boolean warnExitingOnClose = pref.getBoolean("warnExitingOnClose", true);

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
        Pref.splashScreen = splashScreenParam;
        // Store value in preferences
        pref.putBoolean("splashScreen", splashScreen);
    }

    public static boolean isErrorLoadingFilePopUpAtStartOrOnOpen() {
        return errorLoadingFilePopUpAtStartOrOnOpen;
    }

    public static void setErrorLoadingFilePopUpAtStartOrOnOpen(boolean errorLoadingDataFromFilePopUpAtStartOrOnOpenParam) {
        Pref.errorLoadingFilePopUpAtStartOrOnOpen = errorLoadingDataFromFilePopUpAtStartOrOnOpenParam;
        // Store value in preferences
        pref.putBoolean("errorLoadingFilePopUpAtStartOrOnOpen", errorLoadingFilePopUpAtStartOrOnOpen);
    }

    public static boolean isDecryptFilePassPopUpAtStartOrOnOpen() {
        return decryptFilePassPopUpAtStartOrOnOpen;
    }

    public static void setDecryptFilePassPopUpAtStartOrOnOpen(boolean decryptConnFilePassAtStartOrOnOpenParam) {
        Pref.decryptFilePassPopUpAtStartOrOnOpen = decryptConnFilePassAtStartOrOnOpenParam;
        // Store value in preferences
        pref.putBoolean("decryptFilePassPopUpAtStartOrOnOpen", decryptFilePassPopUpAtStartOrOnOpen);
    }

    public static String getLanguage() {
        return language;
    }

    public static void setLanguage(String language) {
        Pref.language = language;
        // Store value in preferences
        pref.put("language", language);
    }

    public static boolean isWarnRemoveEmptyFileOnClose() {
        return warnRemoveEmptyFileOnClose;
    }

    public static void setWarnRemoveEmptyFileOnClose(boolean warnRemoveEmptyFileOnClose) {
        Pref.warnRemoveEmptyFileOnClose = warnRemoveEmptyFileOnClose;
        // Store value in preferences
        pref.putBoolean("warnRemoveEmptyFileOnClose", warnRemoveEmptyFileOnClose);
    }

    public static boolean isWarnExitingOnClose() {
        return warnExitingOnClose;
    }

    public static void setWarnExitingOnClose(boolean warnExitingOnClose) {
        Pref.warnExitingOnClose = warnExitingOnClose;
        // Store value in preferences
        pref.putBoolean("warnExitingOnClose", warnExitingOnClose);
    }

    // #####################################################################

}
