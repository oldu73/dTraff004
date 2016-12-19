package ch.stageconcept.dtraff.connection.util;

import org.jasypt.util.text.StrongTextEncryptor;

/**
 * Class to encrypt/decrypt a String with a password using Jasypt library (http://www.jasypt.org/)
 *
 * @author Olivier Durand
 */

public class Crypto {

    /**
     * Instance of StrongTextEncryptor which is a utility class for easily performing high-strength encryption of texts
     */
    private final StrongTextEncryptor strongTextEncryptor;

    public Crypto(String password) {
        strongTextEncryptor = new StrongTextEncryptor();
        strongTextEncryptor.setPassword(password);
    }

    /**
     * Encrypt a String using the StrongTextEncryptor instance
     *
     * @param stringToEncrypt a String that will be encrypted with the StrongTextEncryptor instance
     * @return an encrypted String
     */
    public String getEncrypted(String stringToEncrypt) {
        return strongTextEncryptor.encrypt(stringToEncrypt);
    }

    /**
     * Decrypt a String using the StrongTextEncryptor instance
     *
     * @param stringToDecrypt a String that will be decrypted with the StrongTextEncryptor instance
     * @return a decrypted String
     */
    public String getDecrypted(String stringToDecrypt) {
        return strongTextEncryptor.decrypt(stringToDecrypt);
    }

}
