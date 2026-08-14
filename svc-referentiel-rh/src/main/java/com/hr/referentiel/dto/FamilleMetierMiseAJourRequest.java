package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FamilleMetierMiseAJourRequest {

	@NotBlank
	@Size(max = 255)
	@JsonProperty("libelle")
	private String libelle;

	@NotNull
	@JsonProperty("actif")
	private Boolean actif;

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Boolean getActif() {
		return actif;
	}

	public void setActif(Boolean actif) {
		this.actif = actif;
	}
}
