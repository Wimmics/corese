package fr.inria.corese.compiler.eval;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import fr.cryptohash.*;

/**
 * Use cryptographic library from saphir2 project
 * 
 * http://www.saphir2.com/sphlib/
 * 
 */
public class Hash {
	private static Logger logger = LoggerFactory.getLogger(Hash.class);	

	//static String SHA224 = "SHA-224";
	String name;
	
	public Hash(String n){
		name = n;
	}
	
	public String hash(String str){
//		if (name.equals(SHA224)){
//			return sha224(str);
//		}
		
		byte[] uniqueKey = str.getBytes();
		byte[] hash      = null;

		try {
			hash = MessageDigest.getInstance(name).digest(uniqueKey);
		}
		catch (NoSuchAlgorithmException e){
			logger.error("No support in this VM: " + name);
			return null;
		}

		String res = toString(hash);
		return res;
	}
	
//	String sha224(String str){
//		SHA224 hash = new SHA224();
//		byte[] data = str.getBytes();
//		byte[] out = hash.digest(data);
//		if (out == null) return null;
//		String res = toString(out);
//		return res;
//	}

	
	String toString(byte[] hash){
		StringBuilder hashString = new StringBuilder();
		for (int i = 0; i < hash.length; i++){
			String hex = Integer.toHexString(hash[i]);
			if (hex.length() == 1){
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			}
			else {
				hashString.append(hex.substring(hex.length() - 2));
			}
		}

		String res = hashString.toString();
		return res;
	}
	

}
