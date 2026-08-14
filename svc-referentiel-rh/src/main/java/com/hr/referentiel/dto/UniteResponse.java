package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class UniteResponse {

	@JsonProperty("identifiant")
	private UUID identifiant;

	@JsonProperty("code")
	private String code;

	@JsonProperty("libelle")
	private String libelle;

	@JsonProperty("parent_identifiant")
	private UUID parentIdentifiant;

	@JsonProperty("type_noeud")
	private String typeNoeud;

	@JsonProperty("titre_poste")
	private String titrePoste;

	@JsonProperty("actif")
	private boolean actif;

	@JsonProperty("cree_le")
	private Instant creeLe;

	@JsonProperty("modifie_le")
	private Instant modifieLe;

	public UniteResponse() {
	}

	public UniteResponse(UUID identifiant, String code, String libelle, UUID parentIdentifiant,
			boolean actif, Instant creeLe, Instant modifieLe) {
		this(identifiant, code, libelle, parentIdentifiant, null, null, actif, creeLe, modifieLe);
	}

	public UniteResponse(UUID identifiant, String code, String libelle, UUID parentIdentifiant,
			String typeNoeud, String titrePoste, boolean actif, Instant creeLe, Instant modifieLe) {
		this.identifiant = identifiant;
		this.code = code;
		this.libelle = libelle;
		this.parentIdentifiant = parentIdentifiant;
		this.typeNoeud = typeNoeud;
		this.titrePoste = titrePoste;
		this.actif = actif;
		this.creeLe = creeLe;
		this.modifieLe = modifieLe;
	}

	public UUID getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(UUID identifiant) {
		this.identifiant = identifiant;
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

	public UUID getParentIdentifiant() {
		return parentIdentifiant;
	}

	public void setParentIdentifiant(UUID parentIdentifiant) {
		this.parentIdentifiant = parentIdentifiant;
	}

	public String getTypeNoeud() {
		return typeNoeud;
	}

	public void setTypeNoeud(String typeNoeud) {
		this.typeNoeud = typeNoeud;
	}

	public String getTitrePoste() {
		return titrePoste;
	}

	public void setTitrePoste(String titrePoste) {
		this.titrePoste = titrePoste;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
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
