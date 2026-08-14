package com.hr.referentiel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class OrganigrammeAssignerManagerRequest {

	@NotNull(message = "collaborateur_identifiant est obligatoire")
	@JsonProperty("collaborateur_identifiant")
	private UUID collaborateurIdentifiant;

	@Size(max = 255)
	@JsonProperty("titre_poste")
	private String titrePoste;

	/**
	 * Ignoré. Conservé pour rétrocompatibilité JSON : n'est plus appliqué à {@code profil_acces}.
	 * L'assignation / le retrait de manager ne mute plus le profil d'accès.
	 *
	 * @deprecated le profil se gère uniquement via PUT fiche collaborateur ({@code profil_acces}).
	 */
	@Deprecated
	@Size(max = 32)
	@JsonProperty("role_workflow")
	private String roleWorkflow;

	public UUID getCollaborateurIdentifiant() {
		return collaborateurIdentifiant;
	}

	public void setCollaborateurIdentifiant(UUID collaborateurIdentifiant) {
		this.collaborateurIdentifiant = collaborateurIdentifiant;
	}

	public String getTitrePoste() {
		return titrePoste;
	}

	public void setTitrePoste(String titrePoste) {
		this.titrePoste = titrePoste;
	}

	public String getRoleWorkflow() {
		return roleWorkflow;
	}

	public void setRoleWorkflow(String roleWorkflow) {
		this.roleWorkflow = roleWorkflow;
	}
}
