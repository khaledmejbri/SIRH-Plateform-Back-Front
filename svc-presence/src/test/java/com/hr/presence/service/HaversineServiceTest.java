package com.hr.presence.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HaversineServiceTest {

	private final HaversineService haversine = new HaversineService();

	@Test
	void distanceZeroSamePoint() {
		assertThat(haversine.distanceMetres(36.8065, 10.1815, 36.8065, 10.1815)).isEqualTo(0.0);
	}

	@Test
	void within50m_accepted() {
		// ~49.9 m north of reference (approx 0.000449 degrees lat)
		double latRef = 36.8065;
		double lonRef = 10.1815;
		double latNear = latRef + (49.9 / 111_320.0);
		double d = haversine.distanceMetres(latRef, lonRef, latNear, lonRef);
		assertThat(d).isLessThanOrEqualTo(50.0);
		assertThat(haversine.withinRadius(d, 50)).isTrue();
	}

	@Test
	void beyond50m_rejected() {
		double latRef = 36.8065;
		double lonRef = 10.1815;
		double latFar = latRef + (50.1 / 111_320.0);
		double d = haversine.distanceMetres(latRef, lonRef, latFar, lonRef);
		assertThat(d).isGreaterThan(50.0);
		assertThat(haversine.withinRadius(d, 50)).isFalse();
	}
}
