package com.hr.presence.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hr.presence.config.PresenceProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Jeton QR opaque {@code p1.<payloadB64Url>.<sigB64Url>} (HMAC-SHA256).
 */
@Service
public class QrTokenService {

	public static final String SCHEMA_PREFIX = "p1";

	private final PresenceProperties properties;
	private final ObjectMapper objectMapper;
	private final Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
	private final Base64.Decoder urlDecoder = Base64.getUrlDecoder();

	public QrTokenService(PresenceProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	public String buildToken(UUID siteId, int qrVersion, UUID jti, Instant iat, Instant exp) {
		Map<String, Object> claims = new LinkedHashMap<>();
		claims.put("siteId", siteId.toString());
		claims.put("qrVersion", qrVersion);
		claims.put("jti", jti.toString());
		claims.put("iat", iat.getEpochSecond());
		claims.put("exp", exp.getEpochSecond());
		String payloadJson;
		try {
			payloadJson = objectMapper.writeValueAsString(claims);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Impossible de sérialiser le payload QR", e);
		}
		String payloadB64 = urlEncoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
		String sig = sign(payloadB64);
		return SCHEMA_PREFIX + "." + payloadB64 + "." + sig;
	}

	/**
	 * @return claims si signature OK, sinon empty
	 */
	public java.util.Optional<QrClaims> parseAndVerify(String token) {
		if (token == null || token.isBlank()) {
			return java.util.Optional.empty();
		}
		String[] parts = token.trim().split("\\.");
		if (parts.length != 3 || !SCHEMA_PREFIX.equals(parts[0])) {
			return java.util.Optional.empty();
		}
		String payloadB64 = parts[1];
		String sig = parts[2];
		String expected = sign(payloadB64);
		if (!constantTimeEquals(expected, sig)) {
			return java.util.Optional.empty();
		}
		try {
			byte[] json = urlDecoder.decode(payloadB64);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = objectMapper.readValue(json, Map.class);
			UUID siteId = UUID.fromString(String.valueOf(map.get("siteId")));
			int qrVersion = ((Number) map.get("qrVersion")).intValue();
			UUID jti = UUID.fromString(String.valueOf(map.get("jti")));
			Instant iat = Instant.ofEpochSecond(((Number) map.get("iat")).longValue());
			Instant exp = Instant.ofEpochSecond(((Number) map.get("exp")).longValue());
			return java.util.Optional.of(new QrClaims(siteId, qrVersion, jti, iat, exp));
		} catch (Exception e) {
			return java.util.Optional.empty();
		}
	}

	private String sign(String payloadB64) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			byte[] secret = requireSecret().getBytes(StandardCharsets.UTF_8);
			mac.init(new SecretKeySpec(secret, "HmacSHA256"));
			byte[] raw = mac.doFinal(payloadB64.getBytes(StandardCharsets.UTF_8));
			return urlEncoder.encodeToString(raw);
		} catch (Exception e) {
			throw new IllegalStateException("Erreur crypto HMAC QR", e);
		}
	}

	private String requireSecret() {
		String secret = properties.getQr().getHmacSecret();
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("PRESENCE_QR_HMAC_SECRET manquant");
		}
		return secret;
	}

	private static boolean constantTimeEquals(String a, String b) {
		if (a == null || b == null) {
			return false;
		}
		byte[] x = a.getBytes(StandardCharsets.UTF_8);
		byte[] y = b.getBytes(StandardCharsets.UTF_8);
		if (x.length != y.length) {
			return false;
		}
		int result = 0;
		for (int i = 0; i < x.length; i++) {
			result |= x[i] ^ y[i];
		}
		return result == 0;
	}

	public record QrClaims(UUID siteId, int qrVersion, UUID jti, Instant iat, Instant exp) {
	}
}
