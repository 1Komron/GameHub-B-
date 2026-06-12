package com.gamehubbot.auth.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.gamehubbot.auth.dto.MessageDigestSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long ttlSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret:change-this-development-secret-at-least-32-bytes}") String secret,
            @Value("${app.jwt.ttl-seconds:86400}") long ttlSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
    }

    public String createToken(UUID userId) {
        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            Instant now = Instant.now();
            String payload = encodeJson(Map.of(
                    "sub", userId.toString(),
                    "iat", now.getEpochSecond(),
                    "exp", now.plusSeconds(ttlSeconds).getEpochSecond()
            ));
            String unsigned = header + "." + payload;
            return unsigned + "." + base64Url(hmac(unsigned.getBytes(StandardCharsets.UTF_8), secret));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create JWT", exception);
        }
    }

    public UUID validateAndGetUserId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            String unsigned = parts[0] + "." + parts[1];
            String expectedSignature = base64Url(hmac(unsigned.getBytes(StandardCharsets.UTF_8), secret));
            if (!constantTimeEquals(expectedSignature, parts[2])) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }
            JsonNode payload = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
            long exp = payload.path("exp").asLong(0);
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("JWT expired");
            }
            return UUID.fromString(payload.path("sub").asText());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid JWT", exception);
        }
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return base64Url(objectMapper.writeValueAsBytes(value));
    }

    private byte[] hmac(byte[] value, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(value);
    }

    private String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigestSupport.constantTimeEquals(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
