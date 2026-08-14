package com.hr.presence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "presence")
public class PresenceProperties {

	private final Qr qr = new Qr();
	private final Geofence geofence = new Geofence();
	private final Alert alert = new Alert();

	public Qr getQr() {
		return qr;
	}

	public Geofence getGeofence() {
		return geofence;
	}

	public Alert getAlert() {
		return alert;
	}

	public static class Qr {
		/** Secret HMAC — env PRESENCE_QR_HMAC_SECRET (défaut DEV non-prod uniquement). */
		private String hmacSecret = "dev-only-presence-qr-hmac-change-me";
		private int validityDays = 90;

		public String getHmacSecret() {
			return hmacSecret;
		}

		public void setHmacSecret(String hmacSecret) {
			this.hmacSecret = hmacSecret;
		}

		public int getValidityDays() {
			return validityDays;
		}

		public void setValidityDays(int validityDays) {
			this.validityDays = validityDays;
		}
	}

	public static class Geofence {
		private int defaultRadiusMetres = 50;
		private double maxAccuracyMetres = 100;

		public int getDefaultRadiusMetres() {
			return defaultRadiusMetres;
		}

		public void setDefaultRadiusMetres(int defaultRadiusMetres) {
			this.defaultRadiusMetres = defaultRadiusMetres;
		}

		public double getMaxAccuracyMetres() {
			return maxAccuracyMetres;
		}

		public void setMaxAccuracyMetres(double maxAccuracyMetres) {
			this.maxAccuracyMetres = maxAccuracyMetres;
		}
	}

	public static class Alert {
		private boolean j30Enabled = true;
		private String j30Cron = "0 0 8 * * *";

		public boolean isJ30Enabled() {
			return j30Enabled;
		}

		public void setJ30Enabled(boolean j30Enabled) {
			this.j30Enabled = j30Enabled;
		}

		public String getJ30Cron() {
			return j30Cron;
		}

		public void setJ30Cron(String j30Cron) {
			this.j30Cron = j30Cron;
		}
	}
}
