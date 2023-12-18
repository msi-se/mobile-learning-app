package de.htwg_konstanz.mobilelearning.helper;

import java.security.MessageDigest;

public class Hasher {
    
    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();
            
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }

    }
}
