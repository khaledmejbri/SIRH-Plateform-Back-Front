package com.hr.referentiel.web;

import com.hr.referentiel.dto.*;
import com.hr.referentiel.security.PreAuthorizeExpressions;
import com.hr.referentiel.service.OrganigrammeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/referentiel/v1/organigramme")
public class OrganigrammeController {

	private final OrganigrammeService organigrammeService;

	public OrganigrammeController(OrganigrammeService organigrammeService) {
		this.organigrammeService = organigrammeService;
	}

	/** Visualisation pour tout utilisateur authentifié (collaborateurs inclus). */
	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<OrganigrammeResponse> obtenir(
			@RequestParam(name = "inclure_inactifs", defaultValue = "false") boolean inclureInactifs) {
		return ResponseEntity.ok(organigrammeService.obtenirOrganigramme(inclureInactifs));
	}

	@PostMapping("/noeuds")
	@PreAuthorize(PreAuthorizeExpressions.BACKOFFICE_ECRITURE)
	public ResponseEntity<OrganigrammeNoeudResponse> creerNoeud(
			@Valid @RequestBody OrganigrammeNoeudCreationRequest requete) {
		return ResponseEntity.status(HttpStatus.CREATED).body(organigrammeService.creerNoeud(requete));
	}

	@PutMapping("/noeuds/{identifiant}")
	@PreAuthorize(PreAuthorizeExpressions.BACKOFFICE_ECRITURE)
	public ResponseEntity<Object> mettreAJourNoeud(@PathVariable UUID identifiant,
			@Valid @RequestBody OrganigrammeNoeudMiseAJourRequest requete) {
		try {
			return ResponseEntity.ok(organigrammeService.mettreAJourNoeud(identifiant, requete));
		} catch (IllegalArgumentException ex) {
			if (ex.getMessage() != null && ex.getMessage().contains("introuvable")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erreur", ex.getMessage()));
			}
			throw ex;
		}
	}

	@PostMapping("/noeuds/{identifiant}/manager")
	@PreAuthorize(PreAuthorizeExpressions.BACKOFFICE_ECRITURE)
	public ResponseEntity<Object> assignerManager(@PathVariable UUID identifiant,
			@Valid @RequestBody OrganigrammeAssignerManagerRequest requete) {
		try {
			return ResponseEntity.ok(organigrammeService.assignerManager(identifiant, requete));
		} catch (IllegalArgumentException ex) {
			if (ex.getMessage() != null && ex.getMessage().contains("introuvable")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erreur", ex.getMessage()));
			}
			throw ex;
		}
	}

	@DeleteMapping("/noeuds/{identifiant}/manager")
	@PreAuthorize(PreAuthorizeExpressions.BACKOFFICE_ECRITURE)
	public ResponseEntity<Object> retirerManager(@PathVariable UUID identifiant) {
		try {
			return ResponseEntity.ok(organigrammeService.retirerManager(identifiant));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erreur", ex.getMessage()));
		}
	}
}
