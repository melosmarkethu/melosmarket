package com.melosmarket.api.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;
    private final Duration tokenValidity;

    public JwtService(
            @Value("${melosmarket.auth.jwt-secret}") String secret,
            @Value("${melosmarket.auth.token-validity-hours}") long tokenValidityHours) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.tokenValidity = Duration.ofHours(tokenValidityHours);
    }

    public String createToken(Long userId) {
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        long expiresAt = Instant.now().plus(tokenValidity).getEpochSecond();
        String payload = encode("{\"sub\":" + userId + ",\"exp\":" + expiresAt + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public Long verifyAndReadUserId(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }

        String signedContent = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(signedContent), parts[2])) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        long expiresAt = readLongClaim(payload, "exp");
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new IllegalArgumentException("Token expired");
        }

        return readLongClaim(payload, "sub");
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign token", exception);
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private long readLongClaim(String json, String claim) {
        String marker = "\"" + claim + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Missing token claim");
        }
        start += marker.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }

    private boolean constantTimeEquals(String first, String second) {
        byte[] firstBytes = first.getBytes(StandardCharsets.UTF_8);
        byte[] secondBytes = second.getBytes(StandardCharsets.UTF_8);
        if (firstBytes.length != secondBytes.length) {
            return false;
        }

        int result = 0;
        for (int index = 0; index < firstBytes.length; index++) {
            result |= firstBytes[index] ^ secondBytes[index];
        }
        return result == 0;
    }
}
