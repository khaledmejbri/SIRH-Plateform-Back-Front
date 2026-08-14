package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrganigrammeNoeudResponse {

	@JsonProperty("identifiant")
	private UUID identifiant;

	@JsonProperty("code")
	private String code;

	@JsonProperty("libelle")
	private String libelle;

	@JsonProperty("type_noeud")
	private String typeNoeud;

	@JsonProperty("titre_poste")
	private String titrePoste;

	@JsonProperty("parent_identifiant")
	private UUID parentIdentifiant;

	@JsonProperty("actif")
	private boolean actif;

	@JsonProperty("manager")
	private OrganigrammeMembreResponse manager;

	@JsonProperty("membres")
	private List<OrganigrammeMembreResponse> membres = new ArrayList<>();

	@JsonProperty("enfants")
	private List<OrganigrammeNoeudResponse> enfants = new ArrayList<>();

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

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public OrganigrammeMembreResponse getManager() {
		return manager;
	}

	public void setManager(OrganigrammeMembreResponse manager) {
		this.manager = manager;
	}

	public List<OrganigrammeMembreResponse> getMembres() {
		return membres;
	}

	public void setMembres(List<OrganigrammeMembreResponse> membres) {
		this.membres = membres;
	}

	public List<OrganigrammeNoeudResponse> getEnfants() {
		return enfants;
	}

	public void setEnfants(List<OrganigrammeNoeudResponse> enfants) {
		this.enfants = enfants;
	}
}
