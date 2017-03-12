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

}
