package com.hr.referentiel.service;

import com.hr.referentiel.entity.Collaborateur;
import com.hr.referentiel.entity.UniteOrganisation;

import java.util.Objects;
import java.util.UUID;

/**
 * H3 — dérivation du champ fiche {@code superieur} depuis le manager ACTIF du nœud.
 * Ne touche jamais {@code valideur_attendu} des demandes M01.
 */
final class SuperieurHierarchieRules {

	private SuperieurHierarchieRules() {
	}

	/**
	 * H3-R01 / H3-R02 : supérieur = manager ACTIF du nœud ; si le collab est ce manager,
	 * remonter au manager ACTIF du parent (jamais soi-même).
	 */
	static Collaborateur resoudreSuperieurFiche(Collaborateur collab, UniteOrganisation unite) {
		if (collab == null || unite == null) {
			return null;
		}
		Collaborateur manager = managerActif(unite);
		if (manager == null) {
			return null;
		}
		if (manager.getId().equals(collab.getId())) {
			UniteOrganisation parent = unite.getParent();
			return parent != null ? managerActif(parent) : null;
		}
		return manager;
	}

	static Collaborateur managerActif(UniteOrganisation unite) {
		if (unite == null || unite.getManager() == null) {
			return null;
		}
		Collaborateur manager = unite.getManager();
		return estActif(manager) ? manager : null;
	}

	static boolean estActif(Collaborateur c) {
		return c != null && "ACTIF".equalsIgnoreCase(c.getStatut());
	}

	/**
	 * Applique le supérieur si l'UUID change (idempotence C-S-05).
	 *
	 * @return true si une mutation a été faite
	 */
	static boolean appliquerSiChange(Collaborateur collab, Collaborateur nouveauSuperieur) {
		UUID actuel = collab.getSuperieur() != null ? collab.getSuperieur().getId() : null;
		UUID cible = nouveauSuperieur != null ? nouveauSuperieur.getId() : null;
		if (Objects.equals(actuel, cible)) {
			return false;
		}
		collab.setSuperieur(nouveauSuperieur);
		return true;
	}
}
