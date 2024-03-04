package fr.inria.corese.core.print.rdfc10;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing functionalities.
 */
public class HashingUtility {

    /**
     * Represents the hash algorithm to use.
     */
    public static enum HashAlgorithm {
        /**
         * Represents the SHA-256 hash algorithm.
         */
        SHA_256("SHA-256"),

        /**
         * Represents the SHA-384 hash algorithm.
         */
        SHA_384("SHA-384");

        private final String algorithm;

        private HashAlgorithm(String algorithm) {
            this.algorithm = algorithm.replace("-", "");
        }

        /**
         * Gets the algorithm name.
         *
         * @return the algorithm name
         */
        public String getAlgorithm() {
            return algorithm;
        }
    }

    /**
     * Hashes a string using the specified algorithm.
     * 
     * @param input     the string to hash
     * @param algorithm the algorithm to use
     * @return the hash of the input string
     */
    public static String hash(String input, HashAlgorithm algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getAlgorithm());
            byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return toHexString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm.getAlgorithm() + " algorithm not found", e);
        }
    }

    /**
     * Converts a byte array to a hexadecimal string.
     * 
     * @param hash the byte array to convert
     * @return the hexadecimal string
     */
    private static String toHexString(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
