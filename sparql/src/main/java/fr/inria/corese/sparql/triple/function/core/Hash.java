package fr.inria.corese.sparql.triple.function.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hash {

    private static Logger logger = LoggerFactory.getLogger(Hash.class);
    String name;

    public Hash(String n) {
        name = n;
    }

    public String hash(String str) {
        byte[] uniqueKey = str.getBytes();
        byte[] hash = null;

        try {
            hash = MessageDigest.getInstance(name).digest(uniqueKey);
        } catch (NoSuchAlgorithmException e) {
            logger.error("No support in this VM: " + name);
            return null;
        }

        String res = toString(hash);
        return res;
    }

    String toString(byte[] hash) {
        StringBuilder hashString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }

        String res = hashString.toString();
        return res;
    }
}
