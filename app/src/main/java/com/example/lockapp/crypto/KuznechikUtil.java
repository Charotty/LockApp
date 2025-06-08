package com.example.lockapp.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class KuznechikUtil {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("GOST3412-2015/CTR/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec keySpec = new SecretKeySpec(key, "GOST3412-2015");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(data);
    }
    public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("GOST3412-2015/CTR/NoPadding", BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec keySpec = new SecretKeySpec(key, "GOST3412-2015");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(data);
    }
}
