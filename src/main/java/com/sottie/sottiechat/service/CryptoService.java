package com.sottie.sottiechat.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// TODO: AES 암호화 원리 및 이론 숙지
@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoService {

    @Value("${aes.secret-key}")
    private String key;
    private IvParameterSpec ivParameterSpec;
    private SecretKeySpec secretKeySpec;
    private Cipher cipher;

    @PostConstruct
    public void init() throws NoSuchPaddingException, NoSuchAlgorithmException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        ivParameterSpec = new IvParameterSpec(keyBytes);
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    public String encodeAES(String plainText) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encryptionByte = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(encryptionByte), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("CryptoService: 암호화 실패", e.getMessage());
            throw new IllegalStateException("메시지 암호화에 실패했습니다.");
        }
    }

    public String decodeAES(String encodedText) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decodeByte = Base64.getDecoder().decode(encodedText.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(decodeByte), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("CryptoService: 복호화 실패", e.getMessage());
            throw new IllegalArgumentException("메시지 복호화에 실패했습니다.");
        }
    }
}
