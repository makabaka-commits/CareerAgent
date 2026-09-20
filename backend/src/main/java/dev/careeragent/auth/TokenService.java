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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private final byte[] secret;
    private final long accessSeconds;
    private final Set<String> revoked=ConcurrentHashMap.newKeySet();
    public TokenService(@Value("${app.token-secret}") String secret,@Value("${app.token.access-seconds:1800}") long accessSeconds) {
        if(secret.length()<32) throw new IllegalArgumentException("APP_TOKEN_SECRET 至少需要 32 个字符");
        this.secret=secret.getBytes(StandardCharsets.UTF_8);this.accessSeconds=accessSeconds;
    }

    public String create(long userId) {
        String payload = userId + ":" + Instant.now().plusSeconds(accessSeconds).getEpochSecond()+":"+UUID.randomUUID();
        return encode(payload) + "." + encode(sign(payload));
    }

    public long verify(String token) {
        try {
            if(revoked.contains(token))throw new IllegalArgumentException();String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            if (!constantTimeEquals(sign(payload), Base64.getUrlDecoder().decode(parts[1]))) throw new IllegalArgumentException();
            String[] values = payload.split(":");
            if (Instant.now().getEpochSecond() > Long.parseLong(values[1])) throw new IllegalArgumentException();
            return Long.parseLong(values[0]);
        } catch (Exception e) { throw new ApiException(HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录"); }
    }
    public void revoke(String token){if(token!=null&&!token.isBlank())revoked.add(token);}

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
