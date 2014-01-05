package jmemproxy.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * MD5Hash.java
 * Description:
 * A very simple md5 hash implements, may be too simple?
 * 
 * Author: Leon
 * Email : lleon.21.t@gmail.com
 */

public class MD5Hash implements HashFunction {

	public int hash(Object key) {
		int result = 0;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.reset();
			md5.update(key.toString().getBytes());
			byte[] bKey = md5.digest();
			
			//Why?
			result = ((int)(bKey[3] & 0xFF) << 24) |
					 ((int)(bKey[2] & 0xFF) << 16) |
					 ((int)(bKey[1] & 0xFF) << 8 ) |
					 ((int)(bKey[0] & 0xFF));

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
