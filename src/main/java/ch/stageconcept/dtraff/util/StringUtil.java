package ch.stageconcept.dtraff.util;

import ch.stageconcept.dtraff.connection.model.ConnFile;

/**
 * Helper enum class to provide
 * common String treatment.
 *
 * @author Olivier Durand
 */
public enum StringUtil {

    ;

    /**
     * Name entry (ConnFile instance treeView showed item),
     * String formatter Name - FileName (path + file name).
     *
     * @param connFile
     * @return formatted String
     */
    public static String nameFileNameToString(ConnFile connFile) {
        return connFile.getName() + " - " + connFile.getFileName();
    }

    /**
     * Name entry (ConnFile instance treeView showed item),
     * String formatter Name - FileName (path + file name).
     *
     * @param name
     * @param folder
     * @return formatted String
     */
    public static String nameFileNameToString(String name, String folder) {
        return name + " - " + folder + "\\" + name + ".xml";
    }

    /**
     * Check that the given String parameter is not null
     * and length greater than zero.
     *
     * @param string
     * @return true if string ok, false otherwise
     */
    public static boolean notNullAndLengthGreaterThanZero(String string) {
        return string != null && string.length() > 0;
    }

}
