package com.hr.referentiel.web;

import com.hr.referentiel.domain.NiveauSeniorite;
import com.hr.referentiel.dto.FamilleMetierCreationRequest;
import com.hr.referentiel.dto.FamilleMetierMiseAJourRequest;
import com.hr.referentiel.dto.FamilleMetierResponse;
import com.hr.referentiel.dto.NiveauSenioriteResponse;
import com.hr.referentiel.security.PreAuthorizeExpressions;
import com.hr.referentiel.service.FamilleMetierService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/referentiel/v1")
public class FamilleMetierController {

	private final FamilleMetierService familleMetierService;

	public FamilleMetierController(FamilleMetierService familleMetierService) {
		this.familleMetierService = familleMetierService;
	}

	@GetMapping("/familles-metier")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<FamilleMetierResponse>> lister(
			@RequestParam(name = "actif", required = false) Boolean actif) {
		return ResponseEntity.ok(familleMetierService.lister(actif));
	}

	@PostMapping("/familles-metier")
	@PreAuthorize(PreAuthorizeExpressions.BACKOFFICE_ECRITURE)
	public ResponseEntity<FamilleMetierResponse> creer(@Valid @RequestBody FamilleMetierCreationRequest requete) {
		return ResponseEntity.status(HttpStatus.CREATED).body(familleMetierService.creer(requete));
	}

	@PutMapping("/familles-metier/{code}")
	@PreAuthorize(PreAuthorizeExpressions.BACKOFFICE_ECRITURE)
	public ResponseEntity<FamilleMetierResponse> mettreAJour(
			@PathVariable String code,
			@Valid @RequestBody FamilleMetierMiseAJourRequest requete) {
		return ResponseEntity.ok(familleMetierService.mettreAJour(code, requete));
	}

	@GetMapping("/niveaux-seniorite")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<NiveauSenioriteResponse>> niveaux() {
		List<NiveauSenioriteResponse> list = Arrays.stream(NiveauSeniorite.values())
				.map(n -> new NiveauSenioriteResponse(n.name(), n.getLibelle()))
				.toList();
		return ResponseEntity.ok(list);
	}
}
