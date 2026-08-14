package com.hr.referentiel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "rh_famille_metier")
public class FamilleMetier {

	@Id
	@Column(name = "code", nullable = false, length = 64)
	private String code;

	@Column(name = "libelle", nullable = false, length = 255)
	private String libelle;

	@Column(name = "actif", nullable = false)
	private boolean actif = true;

	/** Seed H-B1 : non suppressible (désactivation seule). */
	@Column(name = "systeme", nullable = false)
	private boolean systeme = false;

	@Column(name = "cree_le", nullable = false, updatable = false)
	private Instant creeLe;

	@Column(name = "modifie_le")
	private Instant modifieLe;

	public FamilleMetier() {
	}

	@PrePersist
	public void prePersist() {
		if (creeLe == null) {
			creeLe = Instant.now();
		}
	}

	@PreUpdate
	public void preUpdate() {
		modifieLe = Instant.now();
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

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public boolean isSysteme() {
		return systeme;
	}

	public void setSysteme(boolean systeme) {
		this.systeme = systeme;
	}

	public Instant getCreeLe() {
		return creeLe;
	}

	public void setCreeLe(Instant creeLe) {
		this.creeLe = creeLe;
	}

	public Instant getModifieLe() {
		return modifieLe;
	}

	public void setModifieLe(Instant modifieLe) {
		this.modifieLe = modifieLe;
	}
}
