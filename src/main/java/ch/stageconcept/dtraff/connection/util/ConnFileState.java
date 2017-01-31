package ch.stageconcept.dtraff.connection.util;

/**
 * ConnFile object state with corresponding icon.
 *
 * @author Olivier Durand
 */
public enum ConnFileState {

    BROKEN("fileBroken001.png"),
    EMPTY_CLEAR("fileEmptyClear001.png"),
    CLEAR("fileClear001.png"),
    ENCRYPTED("fileLock001.png"),
    EMPTY_DECRYPTED("fileEmptyUnLock001.png"),
    DECRYPTED("fileUnLock001.png") ;

    private String iconFileName ;

    ConnFileState(String iconFileName) {
        this.iconFileName = iconFileName ;
    }

    public String getIconFileName() {
        return  this.iconFileName ;
    }

}
