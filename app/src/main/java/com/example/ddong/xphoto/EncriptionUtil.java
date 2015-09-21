package com.example.ddong.xphoto;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ddong on 2015-09-20.
 */
public class EncriptionUtil {
    private final static String TAG = "EncriptionUtil";
    private static byte[] mKey;

    public EncriptionUtil(String password) {
        try {
            mKey = generateKey(password);
        }
        catch (Exception e) {
            Log.w(TAG, "Exception when generate key");
        }
    }

    private static byte[] generateKey(String password) throws Exception
    {
        byte[] keyStart = password.getBytes("UTF-8");

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(keyStart);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    private static byte[] encodeFile(byte[] key, byte[] fileData, int offset, int length) throws Exception
    {

        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(fileData, offset, length);

        return encrypted;
    }

    private static byte[] decodeFile(byte[] key, byte[] fileData) throws Exception
    {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] decrypted = cipher.doFinal(fileData);

        return decrypted;
    }

    public void encriptBytes(byte[] bytes, int size, File dstFile) {
        if (!dstFile.exists())
        {
            try {
                dstFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dstFile));

            byte[] fileBytes = encodeFile(mKey, bytes, 0, size);
            bos.write(fileBytes);
            bos.flush();
            bos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encriptFile(String srcPath, String dstPath) {
        File sourceFile = new File(srcPath);
        File destFile  = new File(dstPath);
        if (!destFile.exists())
        {
            try {
                destFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            BufferedInputStream sinput = new BufferedInputStream(new FileInputStream(sourceFile));
            byte[] inBytes = new byte[(int)sourceFile.length()];
            sinput.read(inBytes);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));

            byte[] fileBytes = encodeFile(mKey, inBytes, 0, inBytes.length);
            bos.write(fileBytes);
            bos.flush();
            bos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] decriptFile(String path) {
        byte[] inputbytes;
        byte[] outputbytes = null;
        File file = new File(path);
        try {
            BufferedInputStream sinput = new BufferedInputStream(new FileInputStream(file));
            inputbytes = new byte[(int)file.length()];
            sinput.read(inputbytes);
            outputbytes = decodeFile(mKey, inputbytes);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return outputbytes;
    }
}
