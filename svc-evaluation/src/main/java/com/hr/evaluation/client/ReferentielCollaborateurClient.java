package com.hr.evaluation.client;

import java.util.List;

/**
 * Client lecture référentiel pour population campagne M07.
 */
public interface ReferentielCollaborateurClient {

	List<CollaborateurEvaluationSnapshot> listerActifsPourEvaluation();
}
