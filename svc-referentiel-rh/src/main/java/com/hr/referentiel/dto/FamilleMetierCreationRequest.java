package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FamilleMetierCreationRequest {

	@NotBlank
	@Size(max = 64)
	@JsonProperty("code")
	private String code;

	@NotBlank
	@Size(max = 255)
	@JsonProperty("libelle")
	private String libelle;

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
}
