package ch.stageconcept.dtraff.main;

import ch.stageconcept.dtraff.preference.model.Pref;
import com.sun.javafx.application.LauncherImpl;

/**
 * Main class.
 * SRC: https://blog.codecentric.de/en/2015/09/javafx-how-to-easily-implement-application-preloader-2/
 *
 * @author Aron Sreder
 * Adapted by Olivier Durand
 */
public class Main {
    public static void main(String[] args) {
        if (Pref.INSTANCE.isSplashScreen()) LauncherImpl.launchApplication(MainApp.class, MainPreloader.class, args);
        else LauncherImpl.launchApplication(MainApp.class, args);
    }
}
