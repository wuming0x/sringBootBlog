package com.wuming.blog.user.service;

import com.wuming.blog.user.entity.User;
import com.wuming.blog.user.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 轻量 JWT 服务，负责签发和解析 HS256 token。
 */
@Service
public class JwtService {

    /**
     * JWT 签名密钥。生产环境应在配置中设置 app.jwt.secret，避免使用开发默认值。
     */
    private final String secret;

    public JwtService(@Value("${app.jwt.secret:dev-only-change-this-secret}") String secret) {
        this.secret = secret;
    }

    /**
     * 根据用户信息生成 JWT。
     *
     * @param user 登录用户
     * @return JWT 字符串
     */
    public String generateToken(User user) {
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"userId\":" + user.getId() + ",\"username\":\"" + escapeJson(user.getUsername()) + "\"}");
        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    /**
     * 解析并校验 JWT。
     *
     * @param token JWT 字符串
     * @return token 中的用户信息
     */
    public JwtUser parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("未登录或登录已失效");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UnauthorizedException("未登录或登录已失效");
        }

        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!expectedSignature.equals(parts[2])) {
            throw new UnauthorizedException("未登录或登录已失效");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        Long userId = extractLong(payload, "userId");
        String username = extractString(payload, "username");
        if (userId == null || username == null || username.isBlank()) {
            throw new UnauthorizedException("未登录或登录已失效");
        }

        return new JwtUser(userId, username);
    }

    /**
     * 对待签名内容进行 HMAC-SHA256 签名。
     */
    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 签名失败", exception);
        }
    }

    /**
     * Base64Url 编码 JSON 文本。
     */
    private String base64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 转义 JSON 字符串中的特殊字符。
     */
    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 从简单 JSON 中提取 Long 字段。
     */
    private Long extractLong(String json, String fieldName) {
        String marker = "\"" + fieldName + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (end == start) {
            return null;
        }
        return Long.parseLong(json.substring(start, end));
    }

    /**
     * 从简单 JSON 中提取字符串字段。
     */
    private String extractString(String json, String fieldName) {
        String marker = "\"" + fieldName + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = json.indexOf("\"", start);
        if (end < 0) {
            return null;
        }
        return json.substring(start, end).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    /**
     * JWT 中保存的用户信息。
     *
     * @param userId 用户 ID
     * @param username 用户名
     */
    public record JwtUser(Long userId, String username) {
    }
}
