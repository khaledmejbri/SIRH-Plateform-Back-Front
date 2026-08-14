package com.hr.presence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hr.presence.config.PresenceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QrTokenServiceTest {

	private QrTokenService qrTokenService;

	@BeforeEach
	void setUp() {
		PresenceProperties props = new PresenceProperties();
		props.getQr().setHmacSecret("test-secret-hmac-presence");
		qrTokenService = new QrTokenService(props, new ObjectMapper());
	}

	@Test
	void buildAndVerify_ok() {
		UUID siteId = UUID.randomUUID();
		UUID jti = UUID.randomUUID();
		Instant iat = Instant.now();
		Instant exp = iat.plus(90, ChronoUnit.DAYS);

		String token = qrTokenService.buildToken(siteId, 1, jti, iat, exp);
		assertThat(token).startsWith("p1.");

		Optional<QrTokenService.QrClaims> claims = qrTokenService.parseAndVerify(token);
		assertThat(claims).isPresent();
		assertThat(claims.get().siteId()).isEqualTo(siteId);
		assertThat(claims.get().qrVersion()).isEqualTo(1);
		assertThat(claims.get().jti()).isEqualTo(jti);
	}

	@Test
	void alteredSignature_rejected() {
		UUID siteId = UUID.randomUUID();
		Instant iat = Instant.now();
		String token = qrTokenService.buildToken(siteId, 1, UUID.randomUUID(), iat, iat.plus(90, ChronoUnit.DAYS));
		String tampered = token.substring(0, token.length() - 2) + "xx";

		assertThat(qrTokenService.parseAndVerify(tampered)).isEmpty();
	}

	@Test
	void unknownSchema_rejected() {
		assertThat(qrTokenService.parseAndVerify("p0.abc.def")).isEmpty();
		assertThat(qrTokenService.parseAndVerify("not-a-token")).isEmpty();
	}
}
