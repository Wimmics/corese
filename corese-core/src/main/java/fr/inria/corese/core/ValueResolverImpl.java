package fr.inria.corese.core;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.core.api.ValueResolver;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
//import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 * Manage Node values in a table: key -> IDatatype key is MD5 hash of IDatatype
 * label
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class ValueResolverImpl implements ValueResolver {

    HashMap<String, IDatatype> tvalues;
    MessageDigest hasher;
    private String NAME = "MD5";
    int count = 0;

    public ValueResolverImpl() {
        tvalues = new HashMap<String, IDatatype>();
        try {
            hasher = MessageDigest.getInstance(NAME);
        } catch (NoSuchAlgorithmException ex) {
            LoggerFactory.getLogger(ValueResolverImpl.class.getName()).error("", ex);
        }
    }

    
    
    @Override
    public int size() {
        return tvalues.size();
    }

    @Override
    public IDatatype getValue(String key) {
        return tvalues.get(key);
    }

    @Override
    public void setValue(String key, IDatatype dt) {
        tvalues.put(key, dt);
    }
 
    @Override
    public String getKey(IDatatype dt) {
        String str = dt.getID();
        String key = getKey(str);
        return key.intern();
    }
       
    @Override
    synchronized public String getKey(String str) {
        byte[] hash = hasher.digest(str.getBytes());
        
       // String tmp = (new HexBinaryAdapter()).marshal(hash);
        
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
       // System.out.println("VR: " + res.equals(tmp) + " " + res + " " + tmp);
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
