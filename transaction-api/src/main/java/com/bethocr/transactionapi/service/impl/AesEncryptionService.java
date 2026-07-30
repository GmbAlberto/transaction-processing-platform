package com.bethocr.transactionapi.service.impl;

import com.bethocr.transactionapi.exception.DecryptionException;
import com.bethocr.transactionapi.service.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AesEncryptionService implements EncryptionService {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int AUTHENTICATION_TAG_LENGTH_BITS = 128;
    private static final int AES_256_KEY_LENGTH_BYTES = 32;

    private final byte[] secretKey;

    public AesEncryptionService(@Value("${security.encryption.key}") String encodedSecretKey) {
        try {
            this.secretKey = Base64.getDecoder().decode(encodedSecretKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("La clave de cifrado no tiene un formato Base64 válido", exception);
        }

        if (secretKey.length != AES_256_KEY_LENGTH_BYTES) {
            throw new IllegalStateException("La clave AES no tiene la longitud adecuda");
        }
    }

    @Override
    public String decrypt(String encryptedValue) {
        try {
            byte[] encryptedPayload = Base64.getDecoder()
                    .decode(encryptedValue);

            if (encryptedPayload.length <= IV_LENGTH_BYTES) {
                throw new DecryptionException("El secreto cifrado no tiene un formato válido");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedPayload);

            byte[] initializationVector = new byte[IV_LENGTH_BYTES];
            byteBuffer.get(initializationVector);

            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            SecretKeySpec keySpec = new SecretKeySpec(secretKey, ALGORITHM);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(AUTHENTICATION_TAG_LENGTH_BITS, initializationVector);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(ciphertext);

            String decryptedValue = new String(decryptedBytes, StandardCharsets.UTF_8);

            if (decryptedValue.isBlank()) {
                throw new DecryptionException("El secreto descifrado está vacío");
            }

            return decryptedValue;

        } catch (DecryptionException exception) {
            throw exception;

        } catch (IllegalArgumentException exception) {
            throw new DecryptionException("El secreto cifrado no está codificado correctamente", exception);

        } catch (AEADBadTagException exception) {
            throw new DecryptionException("No fue posible verificar o descifrar el secreto", exception);

        } catch (Exception exception) {
            throw new DecryptionException("No fue posible descifrar el secreto", exception);
        }
    }
}