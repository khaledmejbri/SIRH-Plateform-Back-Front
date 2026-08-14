package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class OrganigrammeResponse {

	@JsonProperty("racines")
	private List<OrganigrammeNoeudResponse> racines = new ArrayList<>();

	public OrganigrammeResponse() {
	}

	public OrganigrammeResponse(List<OrganigrammeNoeudResponse> racines) {
		this.racines = racines;
	}

	public List<OrganigrammeNoeudResponse> getRacines() {
		return racines;
	}

	public void setRacines(List<OrganigrammeNoeudResponse> racines) {
		this.racines = racines;
	}
}
