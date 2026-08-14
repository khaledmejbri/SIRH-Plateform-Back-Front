package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class OrganigrammeMembreResponse {

	@JsonProperty("identifiant")
	private UUID identifiant;

	@JsonProperty("matricule")
	private String matricule;

	@JsonProperty("prenom")
	private String prenom;

	@JsonProperty("nom")
	private String nom;

	@JsonProperty("poste_libelle")
	private String posteLibelle;

	@JsonProperty("profil_acces")
	private String profilAcces;

	public OrganigrammeMembreResponse() {
	}

	public OrganigrammeMembreResponse(UUID identifiant, String matricule, String prenom, String nom,
			String posteLibelle, String profilAcces) {
		this.identifiant = identifiant;
		this.matricule = matricule;
		this.prenom = prenom;
		this.nom = nom;
		this.posteLibelle = posteLibelle;
		this.profilAcces = profilAcces;
	}

	public UUID getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(UUID identifiant) {
		this.identifiant = identifiant;
	}

	public String getMatricule() {
		return matricule;
	}

	public void setMatricule(String matricule) {
		this.matricule = matricule;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPosteLibelle() {
		return posteLibelle;
	}

	public void setPosteLibelle(String posteLibelle) {
		this.posteLibelle = posteLibelle;
	}

	public String getProfilAcces() {
		return profilAcces;
	}

	public void setProfilAcces(String profilAcces) {
		this.profilAcces = profilAcces;
	}
}
