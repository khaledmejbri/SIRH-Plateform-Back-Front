package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class UniteMiseAJourRequest {

	@Size(max = 255)
	@JsonProperty("libelle")
	private String libelle;

	@JsonProperty("parent_identifiant")
	private UUID parentIdentifiant;

	@Size(max = 120)
	@JsonProperty("type_noeud")
	private String typeNoeud;

	@Size(max = 255)
	@JsonProperty("titre_poste")
	private String titrePoste;

	@JsonProperty("actif")
	private Boolean actif;

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

	public Boolean getActif() {
		return actif;
	}

	public void setActif(Boolean actif) {
		this.actif = actif;
	}
}
