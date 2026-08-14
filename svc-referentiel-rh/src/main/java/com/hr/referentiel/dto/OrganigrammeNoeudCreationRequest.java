package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class OrganigrammeNoeudCreationRequest {

	@NotBlank(message = "Le code est obligatoire")
	@Size(max = 32)
	@JsonProperty("code")
	private String code;

	@NotBlank(message = "Le libellé est obligatoire")
	@Size(max = 255)
	@JsonProperty("libelle")
	private String libelle;

	@NotBlank(message = "Le type de nœud est obligatoire")
	@Size(max = 120)
	@JsonProperty("type_noeud")
	private String typeNoeud;

	@Size(max = 255)
	@JsonProperty("titre_poste")
	private String titrePoste;

	@JsonProperty("parent_identifiant")
	private UUID parentIdentifiant;

	@JsonProperty("actif")
	private Boolean actif = Boolean.TRUE;

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

	public UUID getParentIdentifiant() {
		return parentIdentifiant;
	}

	public void setParentIdentifiant(UUID parentIdentifiant) {
		this.parentIdentifiant = parentIdentifiant;
	}

	public Boolean getActif() {
		return actif;
	}

	public void setActif(Boolean actif) {
		this.actif = actif;
	}
}
