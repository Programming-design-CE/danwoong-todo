package com.danwoog.todo.global.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtProvider {

    private static final String SECRET_KEY = "danwoog-todo-secret-key-must-be-long-enough-123456";
    private static final long ACCESS_TOKEN_EXPIRE_SECONDS = 60 * 60; // 1시간
    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 60 * 60 * 24 * 14; // 14일

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createAccessToken(Long userId) {
        return createToken(userId, "access", ACCESS_TOKEN_EXPIRE_SECONDS);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, "refresh", REFRESH_TOKEN_EXPIRE_SECONDS);
    }

    private String createToken(Long userId, String type, long expireSeconds) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", String.valueOf(userId));
            payload.put("type", type);
            payload.put("exp", Instant.now().getEpochSecond() + expireSeconds);

            String encodedHeader = base64Url(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64Url(objectMapper.writeValueAsBytes(payload));

            String unsignedToken = encodedHeader + "." + encodedPayload;
            String signature = sign(unsignedToken);

            return unsignedToken + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("JWT 생성 실패", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                return false;
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);

            boolean signatureValid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8)
            );

            if (!signatureValid) {
                return false;
            }

            Map<String, Object> payload = getPayload(token);
            long exp = ((Number) payload.get("exp")).longValue();

            return exp > Instant.now().getEpochSecond();
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        try {
            Map<String, Object> payload = getPayload(token);
            return Long.valueOf((String) payload.get("sub"));
        } catch (Exception e) {
            throw new RuntimeException("JWT에서 userId 추출 실패", e);
        }
    }

    private Map<String, Object> getPayload(String token) throws Exception {
        String[] parts = token.split("\\.");
        byte[] decodedPayload = Base64.getUrlDecoder().decode(parts[1]);

        return objectMapper.readValue(
                decodedPayload,
                new TypeReference<Map<String, Object>>() {}
        );
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(keySpec);

        return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
