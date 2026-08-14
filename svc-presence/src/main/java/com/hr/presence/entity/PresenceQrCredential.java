package com.hr.presence.entity;

import com.hr.presence.domain.QrCredentialStatut;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "presence_qr_credential",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_presence_qr_site_version", columnNames = {"site_id", "qr_version"}),
				@UniqueConstraint(name = "uk_presence_qr_jti", columnNames = "jti")
		},
		indexes = {
				@Index(name = "idx_presence_qr_site_statut", columnList = "site_id,statut"),
				@Index(name = "idx_presence_qr_valid_until", columnList = "valid_until")
		}
)
public class PresenceQrCredential {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "site_id", nullable = false)
	private PresenceSite site;

	@Column(name = "qr_version", nullable = false)
	private int qrVersion;

	@Column(nullable = false, unique = true)
	private UUID jti;

	@Column(name = "valid_from", nullable = false)
	private Instant validFrom;

	@Column(name = "valid_until", nullable = false)
	private Instant validUntil;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private QrCredentialStatut statut = QrCredentialStatut.ACTIF;

	@Column(name = "created_by", length = 100)
	private String createdBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "alerte_j30_envoyee", nullable = false)
	private boolean alerteJ30Envoyee = false;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public PresenceSite getSite() {
		return site;
	}

	public void setSite(PresenceSite site) {
		this.site = site;
	}

	public int getQrVersion() {
		return qrVersion;
	}

	public void setQrVersion(int qrVersion) {
		this.qrVersion = qrVersion;
	}

	public UUID getJti() {
		return jti;
	}

	public void setJti(UUID jti) {
		this.jti = jti;
	}

	public Instant getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Instant validFrom) {
		this.validFrom = validFrom;
	}

	public Instant getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Instant validUntil) {
		this.validUntil = validUntil;
	}

	public QrCredentialStatut getStatut() {
		return statut;
	}

	public void setStatut(QrCredentialStatut statut) {
		this.statut = statut;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public boolean isAlerteJ30Envoyee() {
		return alerteJ30Envoyee;
	}

	public void setAlerteJ30Envoyee(boolean alerteJ30Envoyee) {
		this.alerteJ30Envoyee = alerteJ30Envoyee;
	}
}
