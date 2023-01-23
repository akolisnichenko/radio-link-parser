package com.kain.radio.tools;

import com.kain.radio.model.js.CipherData;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
public class LinkDecoder {
    private static final String AES_ALGORITHM = "AES/CFB/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";

    public static String decodeLink(CipherData data, String key) {
        return decodeLink(data.getCipher(), data.getIv(), key);
    }
    public static String decodeLink(String cipherText, String iv, String key) {
        try {
            return decrypt(cipherText, new SecretKeySpec(key.getBytes(), KEY_ALGORITHM), new IvParameterSpec(HexFormat.of().parseHex(iv)));
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            log.error("Invalid string or parameters for decrypt", e);
        }
        return null;
    }

    private static String decrypt(String cipherText, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder()
                .decode(cipherText));
        return new String(plainText, StandardCharsets.UTF_8);
    }
}
