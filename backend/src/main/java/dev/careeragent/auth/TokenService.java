package dev.careeragent.auth;

import dev.careeragent.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {
    private final byte[] secret;
    public TokenService(@Value("${app.token-secret}") String secret) { this.secret = secret.getBytes(StandardCharsets.UTF_8); }

    public String create(long userId) {
        String payload = userId + ":" + Instant.now().plusSeconds(86400).getEpochSecond();
        return encode(payload) + "." + encode(sign(payload));
    }

    public long verify(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            if (!constantTimeEquals(sign(payload), Base64.getUrlDecoder().decode(parts[1]))) throw new IllegalArgumentException();
            String[] values = payload.split(":");
            if (Instant.now().getEpochSecond() > Long.parseLong(values[1])) throw new IllegalArgumentException();
            return Long.parseLong(values[0]);
        } catch (Exception e) { throw new ApiException(HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录"); }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { throw new IllegalStateException("无法创建访问令牌", e); }
    }
    private String encode(String value) { return encode(value.getBytes(StandardCharsets.UTF_8)); }
    private String encode(byte[] value) { return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
    private boolean constantTimeEquals(byte[] a, byte[] b) { return java.security.MessageDigest.isEqual(a, b); }
}
