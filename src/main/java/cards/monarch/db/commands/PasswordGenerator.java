package cards.monarch.db.commands;

import java.security.SecureRandom;

public class PasswordGenerator {

    /**
     * The length of the password.
     */
    public static final int PASSWORD_LEN = 15;
    /**
     * A secure random object to get the random chars from.
     */
    private static final SecureRandom random = new SecureRandom();
    /**
     * Chars that the password can compose of.
     */
    private static final char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    /**
     * This is a utils class
     */
    private PasswordGenerator() {
        System.out.println("you wot mate");
    }

    /**
     * Generates a secure password of length {@link #PASSWORD_LEN PASSWORD_LEN}.
     *
     * @return a secure alphanumeric password
     */
    public static String getPassword() {
        char[] out = new char[PASSWORD_LEN];
        for (int i = 0; i < PASSWORD_LEN; i++) {
            out[i] = chars[random.nextInt(chars.length)];
        }
        return new String(out);
    }

}
