package com.hr.presence.service;

import org.springframework.stereotype.Component;

/**
 * Distance Haversine WGS84 (R = 6_371_000 m) — source de vérité géofence (R-P10).
 */
@Component
public class HaversineService {

	public static final double EARTH_RADIUS_METRES = 6_371_000d;

	public double distanceMetres(double lat1, double lon1, double lat2, double lon2) {
		double phi1 = Math.toRadians(lat1);
		double phi2 = Math.toRadians(lat2);
		double dPhi = Math.toRadians(lat2 - lat1);
		double dLambda = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
				+ Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return EARTH_RADIUS_METRES * c;
	}

	public boolean withinRadius(double distanceMetres, int radiusMetres) {
		return distanceMetres <= radiusMetres;
	}
}
