# Spec développeur — Vague hiérarchie / organigramme (H1–H3)

| | |
|---|---|
| **Rôle** | Business Analyst SIRH |
| **Date** | 12 août 2026 |
| **Sources** | `sirh_hierarchie_vague.md` (D-H01…D-H04, stories H1–H3) · `sirh_hierarchie_ux.md` · `sirh_basefonctionnelle.md` § 5, § 6.1, § 19 (C-M01-*, C-S-*) |
| **Décisions PO figées** | 1 valideur M01 = manager ACTIF du nœud → RRH ; organigramme ≠ chaîne d’approbation ; H4 hors spec |
| **Interdit** | Code applicatif dans cette livrable ; H4 multi-niveaux |

---

## 0. Cartographie actuel vs cible

| Zone | Actuel (code) | Cible (cette vague) |
|---|---|---|
| Organigramme API | CRUD nœuds + manager sous `/api/referentiel/v1/organigramme` | **Inchangé** pour H1 (contrat suffisant) |
| Organigramme web | DnD prioritaire, « Sans responsable » discret, parent dans modal Modifier | Sélecteur parent **prioritaire**, badge fort, panneau responsable dominant (`sirh_hierarchie_ux.md`) |
| Suivi M01 | `etapes[].libelle` générique (« Validation Responsable Opérationnel ») ; **pas** d’identité du valideur | Exposer **qui** valide (snapshot) + libellés clairs ; skip RRH sans étape fantôme |
| `valideur_attendu` | Snapshot à la création ; 403 si autre nœud (story 3) | **Non-régression** stricte |
| `superieur` fiche | Saisie manuelle possible ; sync **partielle** à l’assignation manager (membres) ; retrait manager **ne** nettoie **pas** les membres | Sync systématique depuis manager du nœud ; champ non divergent ; snapshots M01 intacts |

**Contraintes citées** : C-S-01 (gateway) · C-S-02 (RBAC) · C-S-05 (idempotence) · C-M01-03 (motif refus) · C-M01-05 (manager nœud / 403).

---

## Spec — H1 Organigramme web professionnel — M00

**Source PO** : Story H1 (Must) · UX `sirh_hierarchie_ux.md` · D-H02 / D-H04  
**Acteurs & RBAC** : `RH` \| `ADMIN` = écriture (`BACKOFFICE_ECRITURE`) · `DIRECTION` = lecture (`BACKOFFICE_LECTURE` via page BO) · tout authentifié = GET organigramme

### Règles métier

| ID | Règle | Erreur métier |
|----|-------|----------------|
| H1-R01 | Un nœud a un `code` unique (insensible à la casse), `libelle`, `actif`, `type_noeud`, `parent` optionnel, `manager` optionnel | 409 / 400 « Code déjà utilisé » (existant) |
| H1-R02 | Rattachement parent : interdiction parent = soi-même ou descendant (cycle) | 400 « Rattachement impossible : cycle… » / message UI H1-UI-08 |
| H1-R03 | Assigner / retirer un manager **ne mute pas** `profil_acces` | — |
| H1-R04 | Nœud actif sans `manager` → signal UI « Sans responsable » (impact M01 skip RRH — info produit, pas mutation workflow ici) | — |
| H1-R05 | Direction / lecture seule : aucune action d’écriture proposée ; tentative API write → 403 | Message UI « Vous n’avez pas le droit de modifier l’organigramme. » |
| H1-R06 | Vocabulaire UI : **service** / **unité** / **responsable** (pas « nœud ») | — |

### Cycle de vie

Pas de nouveau statut. Opérations : créer nœud · modifier · rattacher / détacher parent · assigner / retirer responsable · basculer actif.

### Données

Champs nœud (existants) : `identifiant`, `code`, `libelle`, `type_noeud`, `titre_poste`, `parent_identifiant`, `actif`, `manager` (`OrganigrammeMembreResponse`), `membres[]`, `enfants[]`.

### API (contrat) — **existant à réutiliser, pas de nouvel endpoint Must**

Base gateway : `/api/referentiel/v1/organigramme` (C-S-01).

| Méthode | Chemin | Auth | Body / query | Codes |
|---------|--------|------|--------------|-------|
| `GET` | `/api/referentiel/v1/organigramme` | Authentifié | `inclure_inactifs` (bool, défaut false) | 200 |
| `POST` | `/api/referentiel/v1/organigramme/noeuds` | `BACKOFFICE_ECRITURE` | création (`code`, `libelle`, `type_noeud?`, `titre_poste?`, `parent_identifiant?`) | 201, 400, 403 |
| `PUT` | `/api/referentiel/v1/organigramme/noeuds/{id}` | `BACKOFFICE_ECRITURE` | `libelle?`, `type_noeud?`, `titre_poste?`, `parent_identifiant?`, `detacher_du_parent?`, `actif?` | 200, 400 (cycle), 404, 403 |
| `POST` | `/api/referentiel/v1/organigramme/noeuds/{id}/manager` | `BACKOFFICE_ECRITURE` | `collaborateur_identifiant`, `titre_poste?` | 200, 404, 403 |
| `DELETE` | `/api/referentiel/v1/organigramme/noeuds/{id}/manager` | `BACKOFFICE_ECRITURE` | — | 200, 404, 403 |

**Pas de nouveau microservice.** Extension optionnelle Could : champ calculé `sans_responsable: boolean` — **hors Must** (le front déduit `manager == null && actif`).

### Notifications

Aucune nouvelle notification H1.

### Canaux

| Canal | Impact |
|-------|--------|
| **Web** Plateforme RH `/app/organigramme` | **Must** — contrat UI ci-dessous |
| **Mobile** RH Connect organigramme | **Should** léger : libellés « Responsable » / « Sans responsable » ; pas d’édition |

#### Contrat UI front (H1) — obligatoire

Référence visuelle : `sirh_hierarchie_ux.md` § 2–5.

1. **Sélecteur parent prioritaire**
   - Action panneau **« Rattacher… »** (écriture) → modale arbre / select groupé + recherche.
   - Options : parent choisi **ou** « Aucun parent (racine) » → `PUT` avec `parent_identifiant` ou `detacher_du_parent: true`.
   - Le chemin principal **ne dépend pas** du DnD.
   - CTA head **« + Ajouter un service »** : enfant de la sélection si sélectionnée, sinon racine.

2. **DnD optionnel (progressive enhancement)**
   - Uniquement écriture ; poignée dédiée (pas carte entière).
   - Drop = corps d’une carte cible uniquement ; pas de slots invisibles / zone racine permanente.
   - Cibles invalides (descendants) grisées ; alternative clavier = sélecteur parent.
   - Si a11y / navigateur fragile : masquer DnD, garder le sélecteur.

3. **Badge / signal « Sans responsable »**
   - Carte **et** panneau : badge warning visible si nœud `actif` et `manager == null`.
   - CTA primary panneau : **« Assigner le responsable »** si vide ; sinon ghost **« Changer le responsable »**.
   - Confirm retrait : texte UX § 5.3 (M01 → RRH) — accepté produit (D-H04 / H2 UX).

4. **Mode lecture Direction**
   - Bandeau « Consultation uniquement — les modifications sont réservées à la RH. »
   - Aucun contrôle write focusable ; lead page lecture ; pas de hint DnD.

5. **États** : loading (skeleton ou `.loading`) · vide · erreur + Réessayer · succès toast 4 s · saving désactive CTA.

### Non-régression

- C-S-02 : Direction ne peut pas écrire.
- Assigner manager ≠ mute `profil_acces` (vague rôles).
- Story 3 M01 inchangée (H1 ne touche pas `valideur_attendu`).

### AC testables

1. **Given** un RH sur Organigramme, **When** il ouvre un service actif sans manager, **Then** badge « Sans responsable » visible (carte + panneau) et CTA principal = assigner un responsable.  
2. **Given** un RH, **When** il rattache via « Rattacher… » (sans DnD) vers un parent valide, **Then** le service apparaît sous ce parent après refresh/arbre.  
3. **Given** un RH, **When** il choisit comme parent un descendant, **Then** refus API/UI avec message cycle clair ; arbre inchangé.  
4. **Given** un compte DIRECTION, **When** il consulte l’organigramme, **Then** bandeau consultation + aucune action write.  
5. **Given** un RH qui retire le responsable, **When** il confirme, **Then** message mentionne le basculement possible des demandes M01 vers la RRH et `DELETE …/manager` est appelé.

### Hors spec H1

- Multi-niveaux M01 (H4) · fusion Organigramme ↔ Structure RH · nouvelle lib diagramme (xyflow…) · édition mobile · catalogue fermé `type_noeud` (up RH).

### Handoff H1

- Architecte : **non**  
- Frontend web : **oui** (`sirh-senior-frontend`)  
- Backend : **non** (sauf bug cycle/message à corriger si constaté)  
- Mobile : Should libellés seulement  

---

## Spec — H2 Règles hiérarchie / valideurs explicites — M00 + M01

**Source PO** : Story H2 (Must) · D-H01 · C-M01-05  
**Acteurs & RBAC** : Collaborateur (`USER`) · manager nœud · RH / DIRECTION / ADMIN (lecture suivi BO)

### Règles métier

| ID | Règle | Erreur métier |
|----|-------|----------------|
| H2-R01 | Valideur 1er niveau = **manager ACTIF du nœud d’affectation** du demandeur (snapshot `valideur_attendu` à la création) | Autre acteur → **403** (story 3) |
| H2-R02 | Pas de manager ACTIF → statut initial `EN_VALIDATION_RRH` ; **pas** d’étape supérieur dans le suivi | — |
| H2-R03 | Pas de remontée parent / N+2 / Direction le long de l’arbre | — |
| H2-R04 | `DIRECTION` = lecture BO ; **jamais** valideur M01 via l’organigramme | — |
| H2-R05 | Libellés suivi / aide : « responsable de service / unité », **pas** « Direction » comme valideur attendu | — |
| H2-R06 | `etape_superieur_requise` côté **suivi affiché** = `valideur_attendu != null` au snapshot (pas recalcul live qui réintroduit une étape fantôme) | — |

### Cycle de vie

Inchangé (story 3) :

```
Soumission → (manager ACTIF ?) EN_VALIDATION_SUPERIEUR : EN_VALIDATION_RRH
          → RRH → APPROUVEE | REFUSEE
```

### Données — écart actuel / cible

**Actuel** `GET …/suivi` :

- `etape_superieur_requise` (bool) — calcul actuel peut combiner snapshot **ou** manager live  
- `etapes[]` : `{ code, libelle, terminee, en_cours }` — libellé RO générique, **sans** identité personne  
- `GET` demande : **pas** de `valideur_attendu_*`

**Cible** — étendre le contrat **existant** (pas de nouvelle ressource) :

#### A. `DemandeAdministrativeSuiviResponse` (Must)

| Champ | Type | Règle |
|-------|------|--------|
| `valideur_attendu_identifiant` | UUID \| null | Snapshot ; null si skip RRH |
| `valideur_attendu_matricule` | string \| null | Affichage |
| `valideur_attendu_nom_complet` | string \| null | `prenom + " " + nom` |
| `valideur_attendu_libelle_role` | string | Toujours `"Responsable de service"` (ou unité) — **jamais** « Direction » |
| `etape_superieur_requise` | bool | `valideur_attendu_identifiant != null` |
| `etapes[]` | list | Si étape supérieur : `code` reste `RO` (compat) ; `libelle` = `"Validation — {nom_complet}"` ou `"Validation — responsable de service"` si nom indisponible ; si skip : **pas** d’étape `RO` |
| `message_explication` | string \| null | Si skip : `"Aucun responsable actif sur votre unité : votre demande est directement en validation RRH."` ; si étape RO en cours : `"En attente du responsable de votre service."` ; sinon null / court |

#### B. `DemandeAdministrativeRhResponse` (Should, même vague si coût faible)

| Champ | Type |
|-------|------|
| `valideur_attendu_identifiant` | UUID \| null |
| `valideur_attendu_nom_complet` | string \| null |

Utile liste / détail sans second appel suivi.

#### C. File RO / aide UI (mobile)

Écran file « à valider » : microcopy — « Demandes de **votre** unité (responsable désigné sur l’organigramme), pas la Direction. » (front ; pas d’API nouvelle).

### API (contrat)

Base gateway M01 : `/api/rh/v1/demandes-administratives`.

| Méthode | Chemin | Auth | Changement |
|---------|--------|------|------------|
| `GET` | `/api/rh/v1/demandes-administratives/{id}/suivi` | Demandeur **ou** `BACKOFFICE_LECTURE` | **Étendre** réponse (champs A) |
| `GET` | `/api/rh/v1/demandes-administratives/{id}` | Demandeur / droits existants | **Étendre** optionnel (champs B) |
| `GET` | `/api/rh/v1/demandes-administratives/en-attente-ro` | `USER` (filtre snapshot) | **Inchangé** métier ; copy UI |
| `POST` | `…/valider-superieur` · `…/refuser-superieur` | Manager = snapshot | **Non-régression** 403 |

Doc fonctionnel § 5.2–5.3, § 6.1, § 20, § 22 : aligner textes D-H01 (livrable doc H2, hors code).

### Notifications

Pas de nouveau topic. Contenu notification existant : préférer « responsable de service » si libellé éditable — **Should**.

### Canaux

| Canal | Écrans |
|-------|--------|
| **Mobile** | Détail demande + suivi étapes ; file RO ; texte statut (remplacer formulations ambiguës « Direction ») |
| **Web** | Suivi / détail demande admin si exposé ; aide courte Organigramme (popover) |
| **Doc** | `sirh_basefonctionnelle.md` (PO déjà partiellement marqué — BA confirme cohérence) |

États UI : loading / vide / erreur / 403 inchangés ; enrichir uniquement libellés + champs.

### Non-régression (story 3) — **bloquant**

| ID | Comportement à conserver |
|----|---------------------------|
| NR-01 | À la création, `valideur_attendu` = manager ACTIF du nœud **à cet instant** (ou null → RRH) |
| NR-02 | `POST …/valider-superieur` / `refuser-superieur` : seul le collaborateur = snapshot (ou repli live legacy) → sinon **403** |
| NR-03 | JWT `RO` / `RESPONSABLE` d’un **autre** nœud → **403** |
| NR-04 | Changement ultérieur du manager du nœud **ne** recalcule **pas** le snapshot des demandes déjà créées |
| NR-05 | Motif obligatoire refus (C-M01-03) |

### AC testables

1. **Given** une demande créée avec manager de nœud ACTIF M, **When** le demandeur appelle `GET …/suivi`, **Then** `valideur_attendu_*` décrit M, `etape_superieur_requise=true`, et l’étape en cours n’affiche pas « Direction » comme valideur.  
2. **Given** une unité sans manager ACTIF, **When** une demande M01 est créée, **Then** statut `EN_VALIDATION_RRH`, `valideur_attendu_identifiant=null`, aucune étape `RO` dans `etapes`, `message_explication` explique le skip RRH.  
3. **Given** un USER d’un autre nœud (même JWT RO), **When** il tente `valider-superieur`, **Then** **403** (NR-02/03).  
4. **Given** la doc fonctionnelle, **When** on lit § 5.3 et § 6.1, **Then** structure ≠ chaîne multi-niveaux et D-H01 y sont explicites.

### Hors spec H2

- Implémenter multi-niveaux · changer la règle 403 / snapshot · fusionner fichiers Structure / Org.

### Handoff H2

- Backend : **oui** (extension DTO suivi + fix `etape_superieur_requise` snapshot-only)  
- Frontend mobile (+ web si suivi BO) : **oui** (consommation champs + copy)  
- Architecte : **non**

---

## Spec — H3 Synchronisation `superieur` fiche ← manager du nœud — M00

**Source PO** : Story H3 (Should) · D-H03 · hyp. § 22.1 (une unité principale)  
**Acteurs & RBAC** : RH/ADMIN écrivent unité / manager (`BACKOFFICE_ECRITURE`) · collaborateur lit fiche / org

### Règles métier

| ID | Règle | Erreur métier |
|----|-------|----------------|
| H3-R01 | Source d’affichage « mon supérieur » = **manager du nœud d’affectation** (si ACTIF) ; sinon `null` | — |
| H3-R02 | Si le collaborateur **est** le manager du nœud : `superieur` = manager ACTIF du **nœud parent** s’il existe, sinon `null` (**jamais** soi-même) | — |
| H3-R03 | Sync **ne remplace pas** le calcul M01 : source runtime = manager nœud + snapshot `valideur_attendu` | — |
| H3-R04 | Après sync, **ne pas** muter `valideur_attendu` des demandes déjà créées | — |
| H3-R05 | Champ `superieur_identifiant` en création / MAJ fiche : **ignoré s’il diverge** (ou rejeté 422) — valeur toujours dérivée de l’unité ; pas de saisie manuelle divergente | 422 optionnel « Le supérieur est dérivé du responsable du service » |
| H3-R06 | Membres ACTIFS du nœud mis à jour quand le manager change ; inactifs : Should (aligner aussi) ou Must-only ACTIF selon AC PO — **Must = ACTIFS** |

### Quand synchroniser (triggers)

| Trigger | Action |
|---------|--------|
| T1 | Création collaborateur avec `unite_identifiant` | `superieur` ← règle H3-R01/R02 |
| T2 | MAJ collaborateur : changement d’unité | idem |
| T3 | `POST …/organigramme/noeuds/{id}/manager` | tous membres ACTIFS du nœud (sauf self-rule R02 pour le nouveau manager) ; propager selon R02 aux managers enfants **uniquement** pour *leur* champ `superieur` en tant que managers (parent manager) — **sans** toucher aux snapshots M01 |
| T4 | `DELETE …/manager` | membres ACTIFS : `superieur → null` (ou parent manager si le membre est lui-même manager d’un enfant — N/A sur le nœud courant) ; **écart actuel** : le code ne nettoie pas les membres → **à corriger** |
| T5 | Job de rattrapage optionnel (Could) | Idempotent : pour chaque ACTIF, recalculer `superieur` ; no-op si déjà égal |

**Hors triggers** : simple `PUT` libellé nœud sans changement parent/manager → pas de sync masse.

### Idempotence (C-S-05)

- Réappliquer T1–T5 avec les mêmes données → même état `superieur` ; pas d’événements Kafka inutiles liés au supérieur (pas de topic nouveau).  
- Comparer UUID avant `save` ; skip write si inchangé.

### Ne pas muter snapshots M01

| Interdit | Détail |
|----------|--------|
| Update bulk `demande.valideur_attendu` | Aucun trigger H3 |
| Recalcul file `en-attente-ro` sur changement manager pour **anciennes** demandes | Les demandes restent chez l’ancien snapshot (NR-04) |
| Changer le statut d’une demande ouverte parce que le manager a changé | Non |

### Données / API

| Méthode | Chemin | Changement |
|---------|--------|------------|
| `POST` | `/api/referentiel/v1/collaborateurs` (chemin réel Referentiel) | Sync T1 ; `superieur_identifiant` non autoritatif |
| `PUT` | `/api/referentiel/v1/collaborateurs/{id}` | Sync T2 si unité change |
| `POST`/`DELETE` | `/api/referentiel/v1/organigramme/noeuds/{id}/manager` | Sync T3/T4 complète |
| `GET` | fiche collaborateur | `superieur_identifiant` reflète la sync |

**UI fiche web** : champ supérieur en **lecture seule** (texte dérivé) ou masqué en édition avec mention « Défini par le responsable du service d’affectation ».  
**Mobile** : lecture « Mon supérieur » alignée.

Endpoint job rattrapage : **Could** — `POST /api/referentiel/v1/organigramme/sync-superieurs` (`BACKOFFICE_ECRITURE` ou ADMIN only) — hors Must si T1–T4 couvrent la recette.

### Notifications

Aucune.

### Canaux

Web fiche collaborateur · Mobile profil / org (lecture) · pas d’édition mobile du supérieur.

### Non-régression

- NR-01…NR-05 (H2 / story 3)  
- `profil_acces` non muté par assignation manager  
- C-S-02 sur endpoints write

### AC testables

1. **Given** un collaborateur (non-manager) affecté à un service dont le manager est M ACTIF, **When** on GET sa fiche, **Then** `superieur_identifiant` = M.  
2. **Given** un changement de manager du nœud (M1 → M2), **When** la RH enregistre `POST …/manager`, **Then** les fiches des membres ACTIFS (hors cas R02) ont `superieur` = M2.  
3. **Given** une demande M01 déjà créée avec `valideur_attendu` = M1, **When** le manager du nœud devient M2, **Then** le snapshot de la demande reste M1 et M2 reçoit **403** s’il tente de valider cette demande (sauf s’il est M1).  
4. **Given** retrait du manager, **When** `DELETE …/manager`, **Then** `superieur` des membres ACTIFS du nœud est `null` (plus l’ancien manager).  
5. **Given** deux appels identiques d’assignation manager, **When** le second s’exécute, **Then** résultat identique (idempotent).

### Hors spec H3

- Multi-affectations · changer le calcul valideur M01 · H4 · job rattrapage obligatoire.

### Handoff H3

- Backend : **oui** (`ReferentielRhService` + `OrganigrammeService`, tests sync + non-régression snapshot)  
- Frontend web : **oui** (champ fiche lecture seule)  
- Mobile : Should affichage  
- Architecte : **non** (sauf job batch exposé — alors revue courte)

---

## Synthèse RBAC

| Expression | Rôles | Usage vague |
|------------|-------|-------------|
| Authentifié | tout JWT valide | `GET` organigramme |
| `BACKOFFICE_LECTURE` | RH \| DIRECTION \| ADMIN | Suivi / listes BO M01 ; consultation org (DIRECTION) |
| `BACKOFFICE_ECRITURE` | RH \| ADMIN | CRUD org, assignation manager, sync triggers write, fiche |
| `USER` | collaborateur (+ rôles métier) | Création / suivi ses demandes ; file si = snapshot |

---

## Hors spec (vague entière)

- Story **H4** multi-niveaux configurable  
- Suppléant / délégation  
- Fusion UI Organigramme ↔ Structure  
- Lib organigramme horizontale  
- Édition organigramme mobile  
- Paie / GED / multi-tenant

---

## Handoff final

### Prêt backend H2/H3

Implémenter dans `svc-referentiel-rh` (via gateway) :

**H2 — étendre**

- `GET /api/rh/v1/demandes-administratives/{id}/suivi` ← champs valideur + `message_explication` + libellés étapes  
- (Should) `GET /api/rh/v1/demandes-administratives/{id}` ← `valideur_attendu_*`  
- Fix sémantique `etape_superieur_requise` = snapshot only  
- Tests non-régression : snapshot, 403 autre nœud, skip RRH

**H3 — comportement service**

- Sync sur create/update collaborateur (unité)  
- Sync complète assign / **retirer** manager (membres ACTIFS)  
- Ignorer / refuser `superieur_identifiant` divergent  
- Garantie : zéro mutation `valideur_attendu`  
- Endpoints org inchangés en signature :

  - `GET/POST/PUT/DELETE` `/api/referentiel/v1/organigramme…` (existants)  
  - Collaborateurs Referentiel (existants)

### Prêt front web H1

Page `OrganigrammePage` selon contrat UI § H1 (sélecteur parent prioritaire, DnD optionnel, badge sans responsable, bandeau Direction).

**Endpoints consommés (inchangés)** :

| Action UI | Endpoint |
|-----------|----------|
| Charger arbre | `GET /api/referentiel/v1/organigramme?inclure_inactifs=` |
| Créer service | `POST /api/referentiel/v1/organigramme/noeuds` |
| Modifier / rattacher / racine | `PUT /api/referentiel/v1/organigramme/noeuds/{id}` |
| Assigner responsable | `POST /api/referentiel/v1/organigramme/noeuds/{id}/manager` |
| Retirer responsable | `DELETE /api/referentiel/v1/organigramme/noeuds/{id}/manager` |

**Front H2 (mobile prioritaire)** : consommer nouveaux champs suivi.  
**Front H3 (web fiche)** : supérieur lecture seule après sync backend.

Agents : `sirh-senior-backend` (H2+H3) ∥ `sirh-senior-frontend` web (H1) — dossiers disjoints ; mobile H2/H3 en file ou second passage.

---

*Spec BA figée pour H1–H3 — ne pas confondre avec `sirh_hierarchie_vague.md` (PO) ni `sirh_hierarchie_ux.md` (look).*
