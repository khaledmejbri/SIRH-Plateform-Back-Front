package com.hr.presence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "presence_site",
		uniqueConstraints = @UniqueConstraint(name = "uk_presence_site_code", columnNames = "code"),
		indexes = {
				@Index(name = "idx_presence_site_actif", columnList = "actif")
		}
)
public class PresenceSite {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false, length = 64)
	private String code;

	@Column(nullable = false, length = 255)
	private String libelle;

	@Column
	private Double latitude;

	@Column
	private Double longitude;

	@Column(name = "rayon_metres", nullable = false)
	private int rayonMetres = 50;

	@Column(nullable = false)
	private boolean actif = true;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "updated_by", length = 100)
	private String updatedBy;

	@Column(name = "emplacement_updated_at")
	private Instant emplacementUpdatedAt;

	@Column(name = "emplacement_updated_by", length = 100)
	private String emplacementUpdatedBy;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public int getRayonMetres() {
		return rayonMetres;
	}

	public void setRayonMetres(int rayonMetres) {
		this.rayonMetres = rayonMetres;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Instant getEmplacementUpdatedAt() {
		return emplacementUpdatedAt;
	}

	public void setEmplacementUpdatedAt(Instant emplacementUpdatedAt) {
		this.emplacementUpdatedAt = emplacementUpdatedAt;
	}

	public String getEmplacementUpdatedBy() {
		return emplacementUpdatedBy;
	}

	public void setEmplacementUpdatedBy(String emplacementUpdatedBy) {
		this.emplacementUpdatedBy = emplacementUpdatedBy;
	}

	public boolean hasEmplacement() {
		return latitude != null && longitude != null;
	}
}
