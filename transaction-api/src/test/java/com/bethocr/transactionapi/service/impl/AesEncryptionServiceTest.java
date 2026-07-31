package com.bethocr.transactionapi.service.impl;

import com.bethocr.transactionapi.exception.DecryptionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AesEncryptionServiceTest {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int AUTHENTICATION_TAG_LENGTH_BITS = 128;

    private static final byte[] SECRET_KEY = "12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8);
    private static final String ENCODED_SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY);

    @Test
    @DisplayName("Debe descifrar correctamente un secreto válido")
    void shouldDecryptValidEncryptedValue() throws Exception {
        AesEncryptionService encryptionService = new AesEncryptionService(ENCODED_SECRET_KEY);

        String originalValue = "secreto-prueba-123#";
        String encryptedValue = encrypt(originalValue, SECRET_KEY);

        String decryptedValue = encryptionService.decrypt(encryptedValue);

        assertThat(decryptedValue).isEqualTo(originalValue);
    }

    @Test
    @DisplayName("Debe conservar caracteres especiales al descifrar")
    void shouldDecryptValueWithSpecialCharacters() throws Exception {
        AesEncryptionService encryptionService = new AesEncryptionService(ENCODED_SECRET_KEY);

        String originalValue = "Contraseña-$#@ñáé-123";
        String encryptedValue = encrypt(originalValue, SECRET_KEY);

        String decryptedValue = encryptionService.decrypt(encryptedValue);

        assertThat(decryptedValue).isEqualTo(originalValue);
    }

    @Test
    @DisplayName("Debe rechazar una clave que no tenga formato Base64")
    void shouldRejectSecretKeyWithInvalidBase64Format() {
        assertThatThrownBy(
                () -> new AesEncryptionService("clave-no-valida%%%")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La clave de cifrado no tiene un formato Base64 válido")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe rechazar una clave que no tenga 32 bytes")
    void shouldRejectSecretKeyWithInvalidLength() {
        byte[] shortKey = "clave-demasiado-corta".getBytes(StandardCharsets.UTF_8);

        String encodedShortKey = Base64.getEncoder()
                .encodeToString(shortKey);

        assertThatThrownBy(
                () -> new AesEncryptionService(encodedShortKey)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La clave AES no tiene la longitud adecuda");
    }

    @Test
    @DisplayName("Debe rechazar un secreto cifrado que no esté en Base64")
    void shouldThrowDecryptionExceptionWhenEncryptedValueIsNotBase64() {
        AesEncryptionService encryptionService = new AesEncryptionService(ENCODED_SECRET_KEY);

        assertThatThrownBy(
                () -> encryptionService.decrypt("valor-invalido")
        )
                .isInstanceOf(DecryptionException.class)
                .hasMessage("El secreto cifrado no está codificado correctamente")
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debe rechazar un payload que solamente contenga el IV")
    void shouldRejectPayloadWithoutCiphertext() {
        AesEncryptionService encryptionService = new AesEncryptionService(ENCODED_SECRET_KEY);

        byte[] onlyInitializationVector = new byte[IV_LENGTH_BYTES];

        String invalidPayload = Base64.getEncoder()
                .encodeToString(onlyInitializationVector);

        assertThatThrownBy(
                () -> encryptionService.decrypt(invalidPayload)
        )
                .isInstanceOf(DecryptionException.class)
                .hasMessage("El secreto cifrado no tiene un formato válido");
    }

    @Test
    @DisplayName("Debe rechazar un payload menor que el IV requerido")
    void shouldRejectPayloadShorterThanInitializationVector() {
        AesEncryptionService encryptionService = new AesEncryptionService(ENCODED_SECRET_KEY);

        byte[] invalidPayloadBytes = new byte[5];

        String invalidPayload = Base64.getEncoder()
                .encodeToString(invalidPayloadBytes);

        assertThatThrownBy(
                () -> encryptionService.decrypt(invalidPayload)
        )
                .isInstanceOf(DecryptionException.class)
                .hasMessage("El secreto cifrado no tiene un formato válido");
    }

    @Test
    @DisplayName("Debe fallar cuando se intenta descifrar con otra clave")
    void shouldThrowDecryptionExceptionWhenSecretKeyIsIncorrect() throws Exception {
        byte[] differentKey = "abcdefghijklmnopqrstuvwxyz123456"
                        .getBytes(StandardCharsets.UTF_8);

        String encodedDifferentKey = Base64.getEncoder()
                .encodeToString(differentKey);

        AesEncryptionService encryptionService = new AesEncryptionService(encodedDifferentKey);

        String encryptedValue = encrypt("secreto-protegido", SECRET_KEY);

        assertThatThrownBy(
                () -> encryptionService.decrypt(encryptedValue)
        )
                .isInstanceOf(DecryptionException.class)
                .hasMessage("No fue posible verificar o descifrar el secreto");
    }

    @Test
    @DisplayName("Debe fallar cuando el contenido cifrado fue alterado")
    void shouldThrowDecryptionExceptionWhenPayloadIsTampered() throws Exception {
        AesEncryptionService encryptionService = new AesEncryptionService(ENCODED_SECRET_KEY);

        String encryptedValue = encrypt("secreto-original", SECRET_KEY);

        byte[] payload = Base64.getDecoder()
                .decode(encryptedValue);

        payload[payload.length - 1] ^= 1;

        String tamperedValue = Base64.getEncoder()
                .encodeToString(payload);

        assertThatThrownBy(
                () -> encryptionService.decrypt(tamperedValue)
        )
                .isInstanceOf(DecryptionException.class)
                .hasMessage("No fue posible verificar o descifrar el secreto");
    }

    @Test
    @DisplayName("Debe rechazar un secreto descifrado vacío")
    void shouldRejectBlankDecryptedValue() throws Exception {
        AesEncryptionService encryptionService = new AesEncryptionService(ENCODED_SECRET_KEY);

        String encryptedBlankValue = encrypt("   ", SECRET_KEY);

        assertThatThrownBy(
                () -> encryptionService.decrypt(encryptedBlankValue)
        )
                .isInstanceOf(DecryptionException.class)
                .hasMessage("El secreto descifrado está vacío");
    }

    private String encrypt(String plainText, byte[] key) throws Exception {
        byte[] initializationVector = new byte[IV_LENGTH_BYTES];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initializationVector);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);

        GCMParameterSpec parameterSpec =
                new GCMParameterSpec(
                        AUTHENTICATION_TAG_LENGTH_BITS,
                        initializationVector
                );

        cipher.init(
                Cipher.ENCRYPT_MODE,
                keySpec,
                parameterSpec
        );

        byte[] encryptedBytes = cipher.doFinal(
                plainText.getBytes(StandardCharsets.UTF_8)
        );

        byte[] payload = ByteBuffer
                .allocate(
                        initializationVector.length
                                + encryptedBytes.length
                )
                .put(initializationVector)
                .put(encryptedBytes)
                .array();

        return Base64.getEncoder()
                .encodeToString(payload);
    }
}