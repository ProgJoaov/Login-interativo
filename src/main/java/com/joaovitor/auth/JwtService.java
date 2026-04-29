package com.joaovitor.auth;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private static final String SECRET = "chave-super-secreta";
    private static final long EXPIRATION = 3600; // 1h

    public String gerarToken(String email) {
        long exp = Instant.now().getEpochSecond() + EXPIRATION;

        String header = base64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64("{\"sub\":\"" + email + "\",\"exp\":" + exp + "}");

        String signature = sign(header + "." + payload);

        return header + "." + payload + "." + signature;
    }

    public String validar(String token) {
        try {
            String[] parts = token.split("\\.");

            String signature = sign(parts[0] + "." + parts[1]);

            if (!signature.equals(parts[2])) return null;

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            if (!payload.contains("\"sub\"")) return null;

            return payload.split("\"sub\":\"")[1].split("\"")[0];

        } catch (Exception e) {
            return null;
        }
    }

    private String base64(String str) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"));

            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes()));

        } catch (Exception e) {
            throw new RuntimeException("Erro JWT");
        }
    }
}