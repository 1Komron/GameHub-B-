package com.gamehubbot.auth.application;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.gamehubbot.auth.dto.AuthResponse;
import com.gamehubbot.auth.dto.MessageDigestSupport;
import com.gamehubbot.auth.dto.UserView;
import com.gamehubbot.auth.infrastructure.JwtGenerateService;
import com.gamehubbot.user.domain.entity.User;
import com.gamehubbot.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramAuthService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final JwtGenerateService jwtGenerateService;

    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.skip-validation:false}")
    private boolean skipValidation;

    @Transactional
    public AuthResponse authenticate(String initData) {
        log.info(initData);
        if (skipValidation && initData.startsWith("dev_mock_")) {
            String userNum = initData.replace("dev_mock_", "");
            long telegramId = "1".equals(userNum) ? 111111111L : 222222222L;
            String username = "devuser" + userNum;
            String firstName = "Dev " + ("1".equals(userNum) ? "One" : "Two");
//
//            userRepository.findByTelegramId(telegramId)
//                    .ifPresent(userRepository::delete);

            User devUser = new User(telegramId, username, firstName);
            return new AuthResponse(jwtGenerateService.generateToken(devUser), UserView.from(devUser));
        }

        Map<String, String> values = parseInitData(initData);
        if (!skipValidation) {
            verify(initData);
        }

        JsonNode telegramUser = parseTelegramUser(values.get("user"));
        long telegramId = telegramUser.path("id").asLong(0);
        if (telegramId <= 0) {
            throw new IllegalArgumentException("Telegram user id is missing");
        }

        User user = userRepository.findByTelegramId(telegramId)
                .map(existing -> updateUser(existing, telegramUser))
                .orElseGet(() -> new User(
                        telegramId,
                        nullableText(telegramUser, "username"),
                        nullableText(telegramUser, "first_name")
                ));
        User saved = userRepository.save(user);

        return new AuthResponse(jwtGenerateService.generateToken(user), UserView.from(saved));
    }

    private User updateUser(User user, JsonNode telegramUser) {
        user.setUsername(nullableText(telegramUser, "username"));
        user.setFirstName(nullableText(telegramUser, "first_name"));
        return user;
    }

    public boolean verify(String initData) {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            for (String pair : initData.split("&")) {
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    params.put(
                            pair.substring(0, eq),
                            pair.substring(eq + 1)
                    );
                }
            }

            String receivedHash = params.remove("hash");
            if (receivedHash == null || receivedHash.isBlank()) return false;

            String dataCheckString = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + safeDecode(e.getValue()))
                    .collect(Collectors.joining("\n"));

            byte[] secretKey = hmac(
                    botToken.trim().getBytes(StandardCharsets.UTF_8),
                    "WebAppData".getBytes(StandardCharsets.UTF_8)
            );

            byte[] computedHash = hmac(
                    dataCheckString.getBytes(StandardCharsets.UTF_8),
                    secretKey
            );

            String computed = toHex(computedHash);

            return computed.equals(receivedHash);

        } catch (Exception e) {
            log.error("verify error", e);
            return false;
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void validate(Map<String, String> values) {
        log.info("Keys in values: {}", values.keySet());
        log.info("Has signature: {}", values.containsKey("signature"));

        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("Telegram bot token is not configured");
        }
        // работаем с копией чтобы не менять оригинал
        Map<String, String> params = new LinkedHashMap<>(values);

        String receivedHash = params.remove("hash");
        if (receivedHash == null || receivedHash.isBlank()) {
            throw new IllegalArgumentException("Telegram initData hash is missing");
        }
        params.remove("signature"); // убираем signature если есть

        String dataCheckString = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + safeDecode(e.getValue()))
                .collect(Collectors.joining("\n"));

        log.info("dataCheckString:\n{}", dataCheckString);
        log.info("receivedHash: {}", receivedHash);

        String expectedHash = telegramHash(dataCheckString);

        log.info("expectedHash: {}", expectedHash);

        if (!MessageDigestSupport.constantTimeEquals(
                expectedHash.getBytes(StandardCharsets.UTF_8),
                receivedHash.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Telegram initData signature is invalid");
        }
    }

    private String safeDecode(String value) {
        if (!value.contains("%")) return value;
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return value;
        }
    }

    private String telegramHash(String dataCheckString) {
        try {
            byte[] secret = hmac(botToken.getBytes(StandardCharsets.UTF_8), "WebAppData".getBytes(StandardCharsets.UTF_8));
            byte[] hash = hmac(dataCheckString.getBytes(StandardCharsets.UTF_8), secret);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Could not validate Telegram initData", exception);
        }
    }

    private byte[] hmac(byte[] value, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(value);
    }

    private JsonNode parseTelegramUser(String userJson) {
        if (userJson == null || userJson.isBlank()) {
            throw new IllegalArgumentException("Telegram initData user is missing");
        }
        try {
            return objectMapper.readTree(userJson);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Telegram user payload is invalid", exception);
        }
    }

    private Map<String, String> parseInitData(String initData) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String pair : initData.split("&")) {
            int separator = pair.indexOf('=');
            if (separator > 0) {
                String key = decode(pair.substring(0, separator));
                String value = decode(pair.substring(separator + 1)); // декодируем!
                values.put(key, value);
            }
        }
        return values;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String nullableText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
