package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class OrganigrammeNoeudMiseAJourRequest {

	@Size(max = 255)
	@JsonProperty("libelle")
	private String libelle;

	@Size(max = 120)
	@JsonProperty("type_noeud")
	private String typeNoeud;

	@Size(max = 255)
	@JsonProperty("titre_poste")
	private String titrePoste;

	@JsonProperty("parent_identifiant")
	private UUID parentIdentifiant;

	/** Si true, détache le nœud (devient une racine). */
	@JsonProperty("detacher_du_parent")
	private Boolean detacherDuParent;

	@JsonProperty("actif")
	private Boolean actif;

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

	public Boolean getDetacherDuParent() {
		return detacherDuParent;
	}

	public void setDetacherDuParent(Boolean detacherDuParent) {
		this.detacherDuParent = detacherDuParent;
	}

	public Boolean getActif() {
		return actif;
	}

	public void setActif(Boolean actif) {
		this.actif = actif;
	}
}
