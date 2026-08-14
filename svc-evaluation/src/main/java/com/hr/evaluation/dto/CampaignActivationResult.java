package com.hr.evaluation.dto;

import java.util.UUID;

/**
 * Résumé d'activation campagne (E4).
 */
public record CampaignActivationResult(
		UUID campagneId,
		String statut,
		int evaluationsCreees,
		int ignoresDejaExistantes,
		int ignoresManagerManquant,
		int profilsIncomplets
) {
}
