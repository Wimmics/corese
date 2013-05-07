package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.api.IDatatype;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage Node values in a table: key -> IDatatype key is MD5 hash of IDatatype
 * label
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class ValueResolver {

    HashMap<String, IDatatype> tvalues;
    MessageDigest hasher;
    private String NAME = "MD5";
    int count = 0;

    public ValueResolver() {
        tvalues = new HashMap<String, IDatatype>();
        try {
            hasher = MessageDigest.getInstance(NAME);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ValueResolver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    
    public int size() {
        return tvalues.size();
    }

    public IDatatype getValue(String key) {
        return tvalues.get(key);
    }

    public IDatatype setValue(String key, IDatatype dt) {
        return tvalues.put(key, dt);
    }
 
    public String getKey(IDatatype dt) {
        String str = dt.getID();
        String key = getKey(str);
        return key.intern();
    }
       
    synchronized public String getKey(String str) {
        byte[] hash = hasher.digest(str.getBytes());
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
        count++;  
        //System.out.println("VR: " + str + " " + res);
        return res;
    }
    
    
     
    
    
    
    
    
    public int getCount() {
        return count;
    }


    synchronized public byte[] hashByte(String str) {
        byte[] hash = hasher.digest(str.getBytes());
        return hash;
    }

}
