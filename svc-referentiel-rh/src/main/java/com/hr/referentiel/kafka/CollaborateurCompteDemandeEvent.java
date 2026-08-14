package com.hr.referentiel.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Demande de compte identité : création initiale, ou resynchronisation des rôles JWT
 * ({@link #OPERATION_MAJ_ROLES}) après changement de {@code profil_acces}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CollaborateurCompteDemandeEvent(

		@JsonProperty("collaborateur_identifiant") UUID collaborateurIdentifiant,

		@JsonProperty("matricule") String matricule,

		@JsonProperty("courriel") String courriel,

		@JsonProperty("prenom") String prenom,

		@JsonProperty("nom") String nom,

		@JsonProperty("profil_acces") String profilAcces,

		@JsonProperty("mot_de_passe_initial") String motDePasseInitial,

		@JsonProperty("operation") String operation,

		@JsonProperty("compte_utilisateur_id") UUID compteUtilisateurId
) {

	public static final String OPERATION_CREATION = "CREATION";
	public static final String OPERATION_MAJ_ROLES = "MAJ_ROLES";

	public CollaborateurCompteDemandeEvent {
		if (operation == null || operation.isBlank()) {
			operation = OPERATION_CREATION;
		}
	}

	/** Rétrocompat : création de compte (pas d'{@code operation} ni d'id compte). */
	public CollaborateurCompteDemandeEvent(
			UUID collaborateurIdentifiant,
			String matricule,
			String courriel,
			String prenom,
			String nom,
			String profilAcces,
			String motDePasseInitial) {
		this(collaborateurIdentifiant, matricule, courriel, prenom, nom, profilAcces,
				motDePasseInitial, OPERATION_CREATION, null);
	}
}
