package com.prj.chatgpt.infrastructure.util.sdk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author zhoumin
 * @create 2018-07-10 10:50
 */
public class SignUtil {

    private static String token = "6b6b";//Token in wechat official account setting

    /**
     * Convert the byte array into a hexadecimal string
     * @param byteArrays character array
     * @return string
     */
    private static String byteToStr(byte[] byteArrays){
        String str = "";
        for (int i = 0; i < byteArrays.length; i++) {
            str += byteToHexStr(byteArrays[i]);
        }
        return str;
    }

    /**
     * Convert bytes to hexadecimal string
     * @param myByte byte
     * @return string
     */
    private static String byteToHexStr(byte myByte) {
        char[] Digit = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] tampArr = new char[2];
        tampArr[0] = Digit[(myByte >>> 4) & 0X0F];
        tampArr[1] = Digit[myByte & 0X0F];
        String str = new String(tampArr);
        return str;
    }

    /**
     * Verify signature
     * @param signature signature
     * @param timestamp timestamp
     * @param nonce random number
     * @return Boolean value
     */
    public static boolean checkSignature(String signature,String timestamp,String nonce){
        String checktext = null;
        if (null != signature) {
            // Sort ToKen, timestamp, nonce dictionary
            String[] paramArr = new String[]{token,timestamp,nonce};
            Arrays.sort(paramArr);
            // Concatenate the sorted results into a string
            String content = paramArr[0].concat(paramArr[1]).concat(paramArr[2]);

            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                //The connected string is encrypted with SHA1
                byte[] digest = md.digest(content.toString().getBytes());
                checktext = byteToStr(digest);
            } catch (NoSuchAlgorithmException e){
                e.printStackTrace();
            }
        }
        //Compare the encrypted string with signature
        return checktext !=null ? checktext.equals(signature.toUpperCase()) : false;
    }
}
