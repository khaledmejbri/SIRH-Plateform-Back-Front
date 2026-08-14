# Spec développeur — Vague évaluations M07 (E1–E5)

| | |
|---|---|
| **Rôle** | Business Analyst SIRH |
| **Date** | 12 août 2026 |
| **Source PO** | [`sirh_evaluations_vague.md`](sirh_evaluations_vague.md) (D-E01…D-E07, stories E1–E5 Must) |
| **Source métier** | [`sirh_basefonctionnelle.md`](sirh_basefonctionnelle.md) § 4 (RBAC), § 5 (fiche), § 12 (M07), § 20, contraintes C-M07-*, C-S-01, C-S-02 |
| **Statut** | Spec figée pour implémentation — **ne pas rouvrir** D-E* ni RBAC story 5 |
| **Interdit** | Coder dans cette livraison BA ; PDF/S3 ; M05 chaud/froid ; note RH 3ᵉ voix ; `TRIMESTRIELLE` Must |

**Contraintes citées** : C-S-01 (gateway unique) · C-S-02 (RBAC) · C-M07-01 (1 éval / campagne×collab) · C-M07-02 (manager après auto) · C-M07-03 (passage technique) · C-M07-04 (rouge → plan d’action).

---

## 0. État actuel du code vs cible vague

| Zone | Comportement actuel | Cible vague |
|---|---|---|
| Fréquences | Enum `ANNUELLE` \| `SEMESTRIELLE` uniquement (DB check) | Verrouiller produit/UI/doc ; pas de `TRIMESTRIELLE` Must |
| Calendrier | `moisDebut` / `moisFin` ∈ {6, 12} à la création campagne | Conservé (B3) |
| Matching technique | `role_metier` + `niveau_seniorite` (strings libres / params mobile) | `famille_metier` (catalogue) × `niveau_seniorite` (enum fermé) |
| Fiche collaborateur | Pas de `famille_metier` / `niveau_seniorite` | Champs fiche + seed catalogue |
| Snapshot éval | `niveauSeniorite`, `roleMetier` sur `Evaluation` | `familleMetierCode` + `niveauSeniorite` figés à la création |
| Activation campagne | Statut → `ACTIVE` seulement ; population scheduler = UUID hardcodés | Création bulk ACTIFS + snapshot + résolution + notif |
| Notif création | Appel abusif `publierAlerteSiNecessaire` (topic alerte couleur) | Message `rh.notifications` dédié `EVALUATION_CAMPAGNE_OUVERTE` |
| Mobile `/moi?ensure` | Client peut passer `niveau_seniorite` / `role_metier` | Snapshot serveur uniquement ; params matching **ignorés / dépréciés** |
| RBAC story 5 | `BACKOFFICE_LECTURE` / `ECRITURE` + ownership mobile | **Non-régression obligatoire** |

---

## Hypothèses BA (ups RH non fournis)

| ID | Hypothèse | Source |
|---|---|---|
| H-B1 | Catalogue familles métier **éditable** RH/ADMIN + **seed minimal** 6 codes (ci-dessous) tant que B1 AGUA non livré | PO D-E03 / B1 |
| H-B5 | Sans `famille_metier` à l’activation → **GENERIC seul** + signal RH `profil_metier_incomplet` (pas de 4xx collab) | PO B5 |
| H-B7 | Population campagne = tous collaborateurs **`statut=ACTIF`** du référentiel (pas d’exclusion stagiaire/intérim Must) | PO up 7 défaut |
| H-MGR | `superieur_identifiant` = manager **ACTIF** du nœud d’unité d’affectation (aligné M01 / D-E02 / B4). Si absent → évaluer avec règles fallback documentées en E4 (création refusée OU file RH — **décision BA** : création **refusée pour ce collab** + alerte RH `manager_manquant`, les autres collabs sont créés) | D-E02, B4 |
| H-NOTIF | Canal Must = in-app via Kafka `rh.notifications` (type `WEBSOCKET`). Push FCM = Should si déjà branché dans `svc-notification` | D-E05 |

### Seed familles métier (H-B1)

| Code | Libellé |
|---|---|
| `EXPLOITATION` | Exploitation / terrain assainissement |
| `GENIE_CIVIL` | Génie civil / travaux |
| `DEV_LOGICIEL` | Développement logiciel / SI |
| `SUPPORT_ADMIN` | Support administratif |
| `MAINTENANCE` | Maintenance technique |
| `HSE_QUALITE` | HSE / qualité |

RH peut créer / désactiver d’autres codes après seed. Codes seed **non supprimables** (désactivation seule) pour stabilité matching.

### Niveaux de séniorité (catalogue fermé)

| Code API | Libellé UI |
|---|---|
| `JUNIOR` | Junior |
| `CONFIRME` | Confirmé |
| `SENIOR` | Senior |
| `TEAM_LEAD` | Team lead |

Alias lecture : `CONFIRMED` → normaliser en `CONFIRME` à l’écriture. Valeurs legacy `MID` / `EXPERT` hors catalogue → **422** à l’écriture fiche / template ; lectures historiques tolérées jusqu’à migration.

---

## Modèle données consolidé (cible E1–E5)

```
famille_metier (référentiel)
  code PK, libelle, actif, systeme

Collaborateur (référentiel)
  …champs existants § 5.2…
  + famille_metier_code? → famille_metier
  + niveau_seniorite? ∈ {JUNIOR,CONFIRME,SENIOR,TEAM_LEAD}

EvaluationCampaign (évaluation)
  type ∈ {ANNUELLE,SEMESTRIELLE}
  statut PLANIFIEE|ACTIVE|TERMINEE|ANNULEE
  annee, moisDebut/moisFin ∈ {6,12}
  template_general → EvaluationTemplate GENERIC (requis avant activate)
  template_competence? → EvaluationTemplate TECHNICAL (fallback campagne)

EvaluationTemplate
  type GENERIC|TECHNICAL, statut DRAFT|PUBLISHED|ARCHIVED
  TECHNICAL : famille_metier_code (+ niveau_seniorite?)
  questions[] (types § 12.3)

Evaluation (1 / campagne × collab — C-M07-01)
  snapshot famille_metier_code?, niveau_seniorite?
  template_competence_assigne?
  profil_metier_incomplet
  superieur_identifiant (manager nœud ACTIF)
  statut validation croisée § 12.4 · etape GENERALE|TECHNIQUE
```

**Legacy à ne plus écrire (Must)** : `Evaluation.role_metier`, `EvaluationTemplate.role` (mapper lecture → `famille_metier_code`), entité `TechnicalTemplate` / endpoints `/technical-templates` (dépréciés — source de vérité = `EvaluationTemplate` type `TECHNICAL`).

---

# Spec — E1 — Fréquences Must & calendrier campagne — M07

**Source PO** : story E1, D-E01, B3  
**Acteurs & RBAC** : RH \| ADMIN (`BACKOFFICE_ECRITURE`) · DIRECTION lecture seule (`BACKOFFICE_LECTURE`)

### Règles métier

| ID | Règle | Erreur métier |
|----|-------|----------------|
| E1-R01 | Types campagne autorisés : `ANNUELLE`, `SEMESTRIELLE` uniquement | `422` `TYPE_CAMPAGNE_INVALIDE` |
| E1-R02 | `moisDebut` et `moisFin` ∈ {6, 12} | `400` / `422` message calendrier juin/décembre |
| E1-R03 | Une seule campagne `PLANIFIEE` ou `ACTIVE` par couple (`type`, `annee`) | `409` `CAMPAGNE_DEJA_EXISTANTE` |
| E1-R04 | UI / API ne doivent **jamais** proposer ni accepter `TRIMESTRIELLE` en Must | Même `422` si envoyé |
| E1-R05 | Doc § 12.1 : trimestriel = **Cible / Could** | — (impact doc, hors runtime) |

### Cycle de vie

Inchangé : `PLANIFIEE` → `ACTIVE` → `TERMINEE` \| `ANNULEE`.

### Données

Champs campagne existants : `nom`, `description`, `type`, `annee`, `moisDebut`, `moisFin`, `dateDebut`/`dateFin` dérivées, templates (assignation E3/E4).

### API (contrat)

Via gateway (C-S-01) :

| Méthode | Chemin | Rôle | Notes |
|---|---|---|---|
| `GET` | `/api/rh/v1/admin/evaluations/campaigns` | Lecture BO | Filtre `statut` optionnel |
| `POST` | `/api/rh/v1/admin/evaluations/campaigns` | Écriture BO | Body : `nom`, `description?`, `type` ∈ {ANNUELLE,SEMESTRIELLE}, `annee`, `moisDebut`, `moisFin`, `creePar` |
| `POST` | `/api/rh/v1/admin/evaluations/campaigns/{id}/activate` | Écriture | Voir E4 (effet étendu) |
| `POST` | `/api/rh/v1/admin/evaluations/campaigns/{id}/terminate` | Écriture | — |

**Codes** : `201` création · `400`/`422` validation · `409` conflit · `403` DIRECTION sur écriture · `401` non auth.

### Notifications

Aucune spécifique E1.

### Canaux

- **Web** : formulaire création campagne — liste déroulante type = Annuelle / Semestrielle uniquement (libellés FR).
- **Mobile** : N/A.

### Non-régression

- Ne pas introduire `TRIMESTRIELLE` en enum runtime Must.
- Ne pas casser fenêtres juin/décembre sans up B3 explicite.

### AC testables

1. **Given** un RH sur création de campagne, **When** il consulte les types, **Then** seuls Annuelle et Semestrielle sont proposés.
2. **Given** un client API envoie `type=TRIMESTRIELLE`, **When** `POST .../campaigns`, **Then** `422 TYPE_CAMPAGNE_INVALIDE`.
3. **Given** `moisDebut=3`, **When** création, **Then** rejet calendrier (juin/décembre).
4. **Given** une campagne semestrielle S1 (`moisDebut=6`) ou S2 (`moisDebut=12`) valide, **When** création, **Then** `201` et statut `PLANIFIEE`.

### Hors spec

Trimestriel population ciblée (E7 Could) · scoring · PDF.

### Handoff E1

- Backend : garde API + messages erreur stables (faible).
- Front web : libellés type (faible).
- Architecte : non.

---

# Spec — E2 — Fiche collaborateur : `famille_metier` + `niveau_seniorite` — M00 (+ conso M07)

**Source PO** : story E2, D-E03, B1, B5  
**Acteurs & RBAC** : RH \| ADMIN écriture fiche · DIRECTION lecture · Collaborateur lecture `moi` · **pas** de matching via `profil_acces`

### Règles métier

| ID | Règle | Erreur métier |
|----|-------|----------------|
| E2-R01 | `famille_metier_code` optionnel à la création/MAJ ; si présent → doit exister et être `actif=true` dans le catalogue | `422 FAMILLE_METIER_INCONNUE` |
| E2-R02 | `niveau_seniorite` optionnel ; si présent → ∈ {JUNIOR, CONFIRME, SENIOR, TEAM_LEAD} | `422 NIVEAU_SENIORITE_INVALIDE` |
| E2-R03 | `profil_acces` **n’intervient pas** dans le matching M07 | — |
| E2-R04 | `poste_libelle` / `fonction` / `qualification_affectation` restent libellés affichage — **pas** clé matching | — |
| E2-R05 | Catalogue familles : CRUD RH/ADMIN ; lecture authentifiée (au moins BO + mobile lecture listes) | `403` si USER tente écriture catalogue |
| E2-R06 | Un collab = au plus une famille + un niveau à un instant T | — |
| E2-R07 | Seed H-B1 appliqué si catalogue vide au démarrage / migration | — |

### Cycle de vie

Catalogue famille : `actif` true/false (soft). Pas d’archivage dur Must.

### Données — nouveaux champs

**Table / entité `Collaborateur` (svc-referentiel-rh)**

| Champ | Type | Obligatoire | Notes |
|---|---|---|---|
| `famille_metier_code` | `VARCHAR(64)` nullable FK logique → catalogue | Non | Code catalogue |
| `niveau_seniorite` | `VARCHAR(32)` nullable | Non | Enum fermé |

**Nouvelle table `famille_metier`**

| Champ | Type | Notes |
|---|---|---|
| `code` | PK `VARCHAR(64)` | Unique, UPPER |
| `libelle` | `VARCHAR(255)` | FR |
| `actif` | boolean | défaut true |
| `systeme` | boolean | true pour seed (non suppressible) |
| `cree_le` / `modifie_le` | instant | — |

Exposition DTO collaborateur (création / MAJ / réponse) :

```json
{
  "famille_metier_code": "DEV_LOGICIEL",
  "famille_metier_libelle": "Développement logiciel / SI",
  "niveau_seniorite": "SENIOR"
}
```

### API (contrat)

Gateway → `svc-referentiel-rh` :

| Méthode | Chemin | Rôle | Body / notes |
|---|---|---|---|
| `GET` | `/api/referentiel/v1/familles-metier` | Auth (BO + mobile lecture) | `?actif=true` optionnel |
| `POST` | `/api/referentiel/v1/familles-metier` | RH\|ADMIN | `{ "code", "libelle" }` |
| `PUT` | `/api/referentiel/v1/familles-metier/{code}` | RH\|ADMIN | `{ "libelle", "actif" }` ; seed : pas de DELETE |
| `GET` | `/api/referentiel/v1/niveaux-seniorite` | Auth | Liste fermée statique |
| `POST` | `/api/referentiel/v1/collaborateurs` | RH\|ADMIN | Champs existants + 2 nouveaux optionnels |
| `PUT` | `/api/referentiel/v1/collaborateurs/{id}` | RH\|ADMIN | Idem |
| `GET` | `/api/referentiel/v1/collaborateurs/{id}` | selon droits existants | Inclut les 2 champs |
| `GET` | `/api/referentiel/v1/collaborateurs/moi` | USER | Lecture de sa fiche (champs inclus) |

**Lecture M07** : `svc-evaluation` consomme la fiche (HTTP interne ou API référentiel via gateway inter-service selon pattern existant) **uniquement côté serveur** à l’activation — jamais trust client mobile pour la clé matching.

### Notifications

Aucune E2.

### Canaux

- **Web** : fiche collaborateur — sélecteurs contrôlés famille + niveau.
- **Mobile** : lecture seule sur profil / fiche si déjà exposée ; pas d’édition Must.

### Non-régression

- Ne pas casser provisionnement compte / `profil_acces`.
- Ne pas utiliser `profil_acces` comme clé template.

### AC testables

1. **Given** un RH, **When** création/MAJ collaborateur avec `famille_metier_code=DEV_LOGICIEL` et `niveau_seniorite=SENIOR`, **Then** persistance OK et relecture cohérente.
2. **Given** code famille inexistant, **When** MAJ, **Then** `422 FAMILLE_METIER_INCONNUE`.
3. **Given** `profil_acces=RO` sans famille, **When** résolution template (E4), **Then** le RO **n’influence pas** le matching.
4. **Given** catalogue vide en migration, **When** seed, **Then** les 6 familles H-B1 sont présentes et éditables (libellé / actif).

### Hors spec

Matching runtime (E4) · constructeur template (E3).

### Handoff E2

- Backend : `svc-referentiel-rh` (+ conso lecture depuis `svc-evaluation` en E4).
- Front web : fiche collab + éventuel écran catalogue familles (minimal).
- Architecte : non (pas de nouveau service).

---

# Spec — E3 — Templates paramétrables GENERIC + TECHNICAL — M07

**Source PO** : story E3, D-E04  
**Acteurs & RBAC** : RH\|ADMIN écriture · DIRECTION lecture seule · USER pas d’admin templates

### Règles métier

| ID | Règle | Erreur métier |
|----|-------|----------------|
| E3-R01 | Types template : `GENERIC`, `TECHNICAL` (et cycle `DRAFT`→`PUBLISHED`→`ARCHIVED`) | `422` si type inconnu |
| E3-R02 | `GENERIC` : pas de `famille_metier_code` / `niveau_seniorite` requis (ignorés si envoyés) | — |
| E3-R03 | `TECHNICAL` publié : `famille_metier_code` **obligatoire** ; `niveau_seniorite` **recommandé** (nullable = « famille seule », priorité plus basse en E4) | `422 TECHNICAL_PROFIL_INCOMPLET` si publication sans famille |
| E3-R04 | Famille technique doit exister et être active | `422 FAMILLE_METIER_INCONNUE` |
| E3-R05 | Questions : types existants § 12.3 / enum `QuestionType` (TEXT, PARAGRAPH, MULTIPLE_CHOICE, CHECKBOX, RATING, SCALE, DATE, NUMBER) | `422` type inconnu |
| E3-R06 | Questions : ordre, obligatoire, section, poids, options/échelle selon type | validation champ |
| E3-R07 | Publication : au moins 1 question active | `422 TEMPLATE_SANS_QUESTION` |
| E3-R08 | DIRECTION : GET OK, POST/PUT/DELETE/publish → `403` | `403` |
| E3-R09 | Pas de template « par `profil_acces` » | — |
| E3-R10 | Champ legacy `role` / `role_metier` : **déprécié** ; écriture Must utilise `famille_metier_code` ; lecture peut mapper `role` → `famille_metier_code` pour templates existants jusqu’à migration | — |

### Cycle de vie template

`DRAFT` → `PUBLISHED` → `ARCHIVED`. Seuls `PUBLISHED` + `actif=true` sont résolvables à l’activation.

### Données — template

Sur `EvaluationTemplate` (cible) :

| Champ | GENERIC | TECHNICAL |
|---|---|---|
| `type` | GENERIC | TECHNICAL |
| `famille_metier_code` | null | obligatoire à publication |
| `niveau_seniorite` | null | optionnel (null = famille seule) |
| `role` (legacy) | — | migrer vers `famille_metier_code` |

Filtre liste admin : `type`, `famille_metier_code`, `niveau_seniorite`, `statut`.

### API (contrat)

Préfixe gateway : `/api/rh/v1/admin/evaluations`

| Méthode | Chemin | Rôle | Notes |
|---|---|---|---|
| `GET` | `/v2` ou `/templates` | Lecture | Filtres profil |
| `POST` | `/v2` | Écriture | Body type + profil si TECHNICAL |
| `GET` | `/v2/{templateId}` | Lecture | + questions |
| `POST` | `/v2/{templateId}/publish` | Écriture | Garde E3-R03/R07 |
| `POST` | `/v2/{templateId}/archive` | Écriture | — |
| `POST` | `/v2/{templateId}/questions` | Écriture | Types § 12.3 |
| `POST` | `/v2/{templateId}/questions/reorder` | Écriture | — |
| `POST` | `/campaigns/{id}/assign-templates` | Écriture | GENERIC obligatoire pour activer (E4) ; TECHNICAL campagne = fallback |

Body création TECHNICAL (cible) :

```json
{
  "nom": "Compétences Dev Senior",
  "description": "...",
  "type": "TECHNICAL",
  "famille_metier_code": "DEV_LOGICIEL",
  "niveau_seniorite": "SENIOR",
  "creePar": "<uuid>"
}
```

*Écart code* : `CreateTemplateRequest` expose encore `role` / `niveauSeniorite` camelCase — Must = accepter `famille_metier_code` (snake) + alias `role` en écriture dépréciée mappée vers famille.

Filtres liste `/v2` : ajouter query `famille_metier_code`, `niveau_seniorite` (en plus de `type`, `statut`).

Endpoints legacy `/technical-templates` : soit alignés sur la même clé, soit marqués dépréciés (une seule source de vérité `EvaluationTemplate` type TECHNICAL — **recommandation BA**).

### Notifications

Aucune E3.

### Canaux

- **Web** : constructeur templates (UI/UX) — orientation « profil métier » (famille × niveau), preview, filtre liste.
- **Mobile** : consommation questions via parcours collab (E5) — pas d’édition.

États UI : loading / vide / erreur / **interdit** (DIRECTION lecture).

### Non-régression

- Scoring 70/30 et types de questions inchangés.
- Story 5 : DIRECTION sans écriture.

### AC testables

1. **Given** un RH, **When** il publie un TECHNICAL `DEV_LOGICIEL` × `SENIOR` avec ≥1 question, **Then** template `PUBLISHED` filtrable / résolvable pour ce couple.
2. **Given** campagne avec GENERIC, **When** collab ouvre son évaluation (après E4), **Then** questions standard puis techniques du profil résolu.
3. **Given** compte DIRECTION, **When** `POST /v2` ou publish, **Then** `403`.
4. **Given** publication TECHNICAL sans `famille_metier_code`, **When** publish, **Then** `422 TECHNICAL_PROFIL_INCOMPLET`.

### Hors spec

Nouveau moteur scoring · PDF · IA générative · E6 polish UX (Should).

### Handoff E3

- Backend : `svc-evaluation` templates.
- Front web : constructeur (avec UI/UX).
- Mobile : non (sauf lecture).

---

# Spec — E4 — Activation campagne : résolution auto + création évaluations — M07

**Source PO** : story E4, D-E03, D-E05 (déclenchement), B5, B7  
**Acteurs & RBAC** : RH\|ADMIN active · Collab/Manager reçoivent évaluations créées · Lecture suivi BO

### Règles métier

| ID | Règle | Erreur métier |
|----|-------|----------------|
| E4-R01 | Activation uniquement depuis `PLANIFIEE` | `409` / `422 CAMPAGNE_STATUT_INVALIDE` |
| E4-R02 | Prérequis : template **GENERIC** assigné, `PUBLISHED`, `actif` | `422 CAMPAGNE_GENERIC_MANQUANT` |
| E4-R03 | À l’activation : créer **1 évaluation** par collaborateur **ACTIF** (H-B7) respectant C-M07-01 | Doublon → skip idempotent (pas d’erreur globale) |
| E4-R04 | Snapshot figé à la création : `famille_metier_code`, `niveau_seniorite`, `template_competence_assigne` (nullable) | — |
| E4-R05 | Résolution TECHNICAL (ordre strict) : 1) famille+niveau 2) famille seule 3) template technique/compétence de campagne 4) sinon null (= GENERIC seul) | — |
| E4-R06 | Si fiche sans `famille_metier` : GENERIC seul + enregistrement signal `profil_metier_incomplet` pour RH (liste/compteur campagne) | Pas d’erreur collab |
| E4-R07 | `superieur_identifiant` = manager ACTIF du nœud (H-MGR) ; si absent → **pas** de création pour ce collab + signal `manager_manquant` | Skip + alerte RH |
| E4-R08 | Après `ACTIVE`, `assign-templates` **refusé** | `409 CAMPAGNE_ACTIVE_TEMPLATES_FIGES` — message : *« Impossible de modifier les templates d’une campagne active : les évaluations déjà créées conservent leur snapshot. »* |
| E4-R09 | Snapshot **immuable** après création (MAJ fiche collab n’altère pas l’éval en cours) | — |
| E4-R10 | `profil_acces` ignoré dans E4-R05 | — |
| E4-R11 | Params client `niveau_seniorite` / `role_metier` sur `/mobile/.../moi` et `/ensure` : **ignorés** pour le matching (dépréciés) ; création hors activation admin ne doit pas recréer hors population si politique « activation only » — BA : conserver ensure **uniquement** si éval déjà due / campagne active **et** collab dans population, sinon liste vide | — |
| E4-R12 | Activation publie notifs (détail E5) pour chaque éval créée | Échec notif ≠ rollback création (log WARN + retry Should) |

### Cycle de vie

Activation : `PLANIFIEE` → `ACTIVE` puis job synchrone (ou transaction + outbox) de création bulk.

Réponse activation recommandée :

```json
{
  "campagneId": "...",
  "statut": "ACTIVE",
  "evaluationsCreees": 120,
  "ignoresDejaExistantes": 2,
  "ignoresManagerManquant": 1,
  "profilsIncomplets": 5
}
```

### Données — Evaluation (nouveaux / renommés)

| Champ | Notes |
|---|---|
| `famille_metier_code` | Snapshot (remplace usage produit de `role_metier`) |
| `niveau_seniorite` | Snapshot existant, normalisé |
| `role_metier` | Legacy : ne plus écrire ; lecture optionnelle migration |
| `template_competence_assigne` | FK template résolu (peut être null) |
| `profil_metier_incomplet` | boolean défaut false |
| `signale_manager_manquant` | N/A sur éval (signal agrégé campagne) |

Agrégats campagne (vue admin) : compteurs `profilsIncomplets`, `managersManquants`.

### API (contrat)

| Méthode | Chemin | Rôle | Effet |
|---|---|---|---|
| `POST` | `/api/rh/v1/admin/evaluations/campaigns/{id}/activate` | Écriture | E4-R01…R12 |
| `POST` | `/api/rh/v1/admin/evaluations/campaigns/{id}/assign-templates` | Écriture | Refus si ACTIVE (E4-R08) |
| `GET` | `/api/rh/v1/admin/evaluations/campaigns/{campaignId}/analytics` | Lecture | Inclure signaux incomplets |
| `GET` | `/api/rh/v1/admin/evaluations?campagneId=&profilIncomplet=true` | Lecture | File alerte RH (Should minimal Must : filtre) |
| `POST` | `/api/rh/v1/evaluations/admin/trigger-evaluation-cycle` | Écriture | Tests ; ne remplace pas l’activation pour population réelle |

**Intégration référentiel** : résoudre population ACTIF + manager nœud + famille/niveau **côté serveur**.

### Notifications

Voir E5 — déclenchées ici à chaque création réussie.

### Canaux

- **Web** : bouton Activer + toast résumé créations / alertes profils incomplets.
- **Mobile** : réception évaluations (E5).

### Non-régression

- C-M07-01 unicité.
- Ne pas recalculer snapshot sur GET `/moi`.
- Anti-IDOR story 5 inchangé.

### AC testables

1. **Given** campagne `PLANIFIEE` avec GENERIC publié + TECHNICAL publiés, **When** RH active, **Then** chaque ACTIF éligible reçoit une éval avec snapshot et template technique résolu si possible ; réponse agrégée OK.
2. **Given** Junior `DEV_LOGICIEL` et Senior `GENIE_CIVIL`, **When** activation, **Then** `template_competence_assigne` (et donc questions techniques) diffèrent selon E3.
3. **Given** campagne déjà `ACTIVE`, **When** `assign-templates`, **Then** `409 CAMPAGNE_ACTIVE_TEMPLATES_FIGES` et évaluations existantes inchangées.
4. **Given** collab sans famille, **When** activation, **Then** éval créée GENERIC seul + `profil_metier_incomplet=true` visible RH.
5. **Given** collab sans manager nœud ACTIF, **When** activation, **Then** pas d’éval pour lui + compteur `ignoresManagerManquant`.

### Hors spec

PDF · M05 · repondération 70/30 · trimestriel.

### Handoff E4

- Backend : `svc-evaluation` (+ client référentiel) — **critique**.
- Front web : feedback activation (léger).
- Architecte : optionnel si outbox/idempotence Kafka notif (sinon pattern `rh.notifications` existant suffit).

---

# Spec — E5 — Mobile : notification + rubrique Évaluations — M07 + NOTIF

**Source PO** : story E5, D-E05  
**Acteurs & RBAC** : USER (collab + manager) · ownership story 5 **strict**

### Règles métier

| ID | Règle | Erreur métier |
|----|-------|----------------|
| E5-R01 | À chaque création d’évaluation (activation E4), publier notif in-app destinataire = `collaborateur_identifiant` | — |
| E5-R02 | Topic : `rh.notifications` ; payload compatible `NotificationMessage` | — |
| E5-R03 | Rubrique / raccourci **Évaluations** visible sur accueil si ≥1 évaluation **non** `ARCHIVEE` pour l’acteur (en tant que collab) | — |
| E5-R04 | Empty state si aucune éval ouverte / non archivée : message métier, HTTP `200` liste vide (pas `500`) | — |
| E5-R05 | Deep-link Should : notif → `/evaluations/{id}` si route mobile existe | — |
| E5-R06 | Accès détail / questions : ownership anti-IDOR (ci-dessous) | `403` / `SecurityException` |
| E5-R07 | Manager notif quand collab valide sa partie | **Should** (si capacité) ; sinon Could — hors Must bloquant |
| E5-R08 | Push FCM | Should si canal NOTIF déjà branché ; Must = in-app + pastille rubrique |

### Notifications — contrat événement

**Topic** : `rh.notifications` (C-S-05 idempotence)  
**Clé Kafka** : UUID collaborateur  
**Payload** : record existant `NotificationMessage(type, recipient, subject, content)` — **4 champs uniquement** (pas d’extension de schéma Must).

```json
{
  "type": "WEBSOCKET",
  "recipient": "<collaborateurUuid>",
  "subject": "Votre évaluation est ouverte",
  "content": "EVALUATION_CAMPAGNE_OUVERTE|campagne=<uuid>|evaluation=<uuid>|La campagne « <nom> » est active. Merci de compléter votre auto-évaluation (générale puis technique)."
}
```

Convention `content` (parsable mobile Should) :

| Segment | Valeur |
|---|---|
| Préfixe | `EVALUATION_CAMPAGNE_OUVERTE` |
| `|campagne=` | UUID campagne |
| `|evaluation=` | UUID évaluation |
| Texte FR | message utilisateur |

Deep-link mobile dérivé : `rhconnect://evaluations/{evaluationId}` (Should).

**Interdit** : réutiliser topic `rh.evaluation.alerte` / `publierAlerteSiNecessaire` pour l’ouverture de campagne (réservé couleur orange/rouge C-M07-04).

Idempotence : une notif par `(evaluationId, EVALUATION_CAMPAGNE_OUVERTE)` — pas de spam si scheduler relance.

### API mobile (contrat)

Préfixe : `/api/rh/v1/mobile/evaluations` — rôle `USER`

| Méthode | Chemin | Ownership | Notes |
|---|---|---|---|
| `GET` | `/moi` | Acteur = collab | Liste ; `ensure` ne doit plus accepter matching client (E4-R11) |
| `GET` | `/{id}` | Participant (collab **ou** manager de l’éval) | — |
| `GET` | `/{id}/questions/*` | Participant | — |
| `POST` | `/{id}/reponses/*` | **Collaborateur** uniquement | — |
| `POST` | `/{id}/manager/reponses/*` | **Manager** uniquement | C-M07-02 |
| `GET` | `/manager/pending` | Acteur = supérieur | — |
| `POST` | `/{id}/validate/collaborator` | Collaborateur | — |

Indicateur rubrique : dérivé de `GET /moi` (count > 0 non archivées) — pas besoin d’endpoint dédié Must.

### RBAC & non-régression story 5 (anti-IDOR) — **obligatoire**

| Cas | Attendu |
|---|---|
| USER A lit/écrit éval de USER B (ni collab ni manager) | `403` |
| Collaborateur tente `manager/reponses` | `403` |
| Manager tente `reponses` collab ou `validate/collaborator` | `403` |
| DIRECTION `POST` admin templates/campagnes | `403` |
| DIRECTION `GET` admin campagnes/scores | `200` |
| RH/ADMIN écriture campagnes/templates | `200`/`201` |
| Header `X-Collaborateur-Id` ≠ identité autorisée pour une autre éval | `403` (comportement ownership actuel à conserver) |

Tests automatisés existants (`EvaluationAdminControllerSecurityTest`, ownership) : **ne pas régresser** ; ajouter cas E5 notif n’ouvre pas de trou IDOR.

### Canaux

- **Mobile** : pastille / tuile Évaluations ; liste ; empty state ; détail notif.
- **Web** : hors Must E5 (suivi RH déjà admin).

États : loading / vide (« Aucune campagne d’évaluation en cours ») / erreur réseau / interdit (403).

### Non-régression

- C-M07-02, C-M07-03 parcours réponses.
- Story 5 matrice § 4.2.
- C-S-01 : pas d’appel hors gateway.

### AC testables

1. **Given** éval créée à l’activation, **When** événement publié, **Then** collab reçoit notif in-app (`rh.notifications`, sujet ouverture campagne).
2. **Given** ≥1 éval non archivée, **When** ouverture accueil mobile, **Then** rubrique Évaluations visible et mène à la liste.
3. **Given** aucune éval, **When** ouverture liste Évaluations, **Then** empty state métier + `200 []`.
4. **Given** USER B, **When** `GET /mobile/evaluations/{idA}`, **Then** `403` (anti-IDOR).

### Hors spec

SMS · e-mail obligatoire · redesign complet (UI/UX brief) · notif manager post-validation (Should).

### Handoff E5

- Backend : publication `rh.notifications` depuis `svc-evaluation` (ou via référentiel publisher — même contrat).
- Mobile : rubrique + empty + conso notif.
- UI/UX : look mobile en parallèle.

---

## Matrice RBAC consolidée (E1–E5)

| Action | USER | Manager (USER + ownership) | RH | DIRECTION | ADMIN |
|---|---|---|---|---|---|
| CRUD familles métier / fiche champs E2 | — | — | Écriture | Lecture | Écriture |
| Campagnes / templates écriture | — | — | Oui | Non | Oui |
| Campagnes / templates / scores lecture | — | — | Oui | Oui | Oui |
| Activer campagne + bulk évals | — | — | Oui | Non | Oui |
| Auto-évaluation / validation collab | Oui (soi) | — | — | — | — |
| Notation manager | — | Oui (périmètre) | — | — | — |
| Voir rubrique mobile | Oui | Oui (pending manager) | via BO | via BO | via BO |

---

## Scoring / couleurs (hors modification vague — rappel non-régression)

Inchangé § 12.5 : notes /5, score /20, **70 % manager + 30 % self** si notes manager, grille appréciation, couleurs vert/orange/rouge (C-M07-04). E1–E5 ne touchent **pas** à ces règles.

---

## Impact doc fonctionnel (à faire avec le code, pas dans cette seule livraison BA)

| Section | Action |
|---|---|
| § 5.2 | Ajouter `famille_metier`, `niveau_seniorite` |
| § 12.1–12.3 | Déjà partiellement aligné vague ; confirmer seed + erreurs API |
| § 12.2 | Activation = bulk + notif |
| § 20 | Rubrique + notif |
| Glossaire | Famille métier (déjà) |
| § 23 | Cocher avancement E1–E5 à la livraison |

---

## Hors vague (rappel D-E07)

- Export PDF + S3 évaluations  
- Bascule formations → M05  
- Évaluations formation chaud/froid  
- Note RH 3ᵉ voix (E8)  
- `TRIMESTRIELLE` généralisée (E7)  
- E6 Should UX (après brief UI/UX)

---

## Handoff global

### Statuts de prêt

| Canal / rôle | Statut | Scope Must |
|---|---|---|
| **Prêt backend** (`sirh-senior-backend`) | **Oui** | E1 gardes · E2 `svc-referentiel-rh` · E3–E4 `svc-evaluation` (+ lecture référentiel) · E5 publish `rh.notifications` |
| **Prêt front web** (`sirh-senior-frontend` web) | **Oui** | E1 types campagne · E2 fiche + catalogue · E3 constructeur (avec UI/UX) · E4 feedback activation |
| **Prêt mobile** (`sirh-senior-frontend` mobile) | **Oui** | E5 notif + rubrique + empty + ignore params matching client |
| UI/UX | Parallèle | Look E3 constructeur + E5 mobile (+ E6 Should) |
| Architecte | Non bloquant | Pas de nouveau microservice ; outbox notif optionnel |
| PO | Seed H-B1 | Ups B1 AGUA pour remplacer seed ; B2/E7–E8 hors Must |

Dossiers disjoints recommandés : `svc-referentiel-rh` ∥ `svc-evaluation` ∥ `rh-admin-web` ∥ `rh_mobile_app`.

### Inventaire endpoints (gateway C-S-01)

#### Référentiel — E2 (`/api/referentiel/v1`)

| Méthode | Chemin | Story | Rôle |
|---|---|---|---|
| `GET` | `/familles-metier` | E2 | Auth lecture |
| `POST` | `/familles-metier` | E2 | RH\|ADMIN |
| `PUT` | `/familles-metier/{code}` | E2 | RH\|ADMIN |
| `GET` | `/niveaux-seniorite` | E2 | Auth lecture |
| `GET`/`POST`/`PUT` | `/collaborateurs`… `/moi`… `/{id}` | E2 | Existants + champs `famille_metier_code`, `niveau_seniorite` |

#### Admin évaluations — E1 / E3 / E4 (`/api/rh/v1/admin/evaluations`)

| Méthode | Chemin | Story | Rôle |
|---|---|---|---|
| `GET` | `/campaigns` | E1 | BO lecture |
| `POST` | `/campaigns` | E1 | BO écriture |
| `POST` | `/campaigns/{id}/activate` | E4 | BO écriture — **bulk + snapshot + notif** |
| `POST` | `/campaigns/{id}/terminate` | E1 | BO écriture |
| `POST` | `/campaigns/{id}/assign-templates` | E3/E4 | BO écriture — **refus si ACTIVE** |
| `GET` | `/campaigns/{campaignId}/analytics` | E4 | BO lecture (+ incomplets) |
| `GET` | `/` (+ filtre `profilIncomplet`) | E4 | BO lecture |
| `GET`/`POST` | `/v2`, `/v2/{id}`, `/publish`, `/archive`, `/questions`, `/reorder` | E3 | Lecture / écriture selon méthode |
| `GET`/`POST` | `/templates`, `/templates/{id}/questions` | E3 | Legacy OK si alignés clé famille |
| `GET`/`POST` | `/technical-templates` | E3 | **Déprécié** — migrer vers `/v2` TECHNICAL |

#### Mobile évaluations — E5 (`/api/rh/v1/mobile/evaluations`) — `USER` + ownership

| Méthode | Chemin | Story | Ownership |
|---|---|---|---|
| `GET` | `/moi` | E5 | Collab soi — **ignorer** query matching client |
| `POST` | `/ensure` | E4/E5 | Déprécié matching client ; ensure seulement si campagne active |
| `GET` | `/{id}` | E5 | Participant |
| `GET` | `/{id}/questions/generales` \| `/techniques` | E5 | Participant |
| `POST` | `/{id}/reponses/generales` \| `/techniques` | E5 | Collaborateur |
| `POST` | `/{id}/manager/reponses/*` | E5 | Manager (C-M07-02) |
| `GET` | `/manager/pending` | E5 | Supérieur |
| `POST` | `/{id}/validate/collaborator` | E5 | Collaborateur |

#### Scheduler / test

| Méthode | Chemin | Story | Note |
|---|---|---|---|
| `POST` | `/api/rh/v1/evaluations/admin/trigger-evaluation-cycle` | E4 | Ne remplace pas activation population réelle |

#### Kafka

| Topic | Événement | Story |
|---|---|---|
| `rh.notifications` | `EVALUATION_CAMPAGNE_OUVERTE` (via `content`) | E5 |
| `rh.evaluation.alerte` | Couleurs orange/rouge | **Hors** ouverture campagne (C-M07-04) |

### Hypothèses figées (pas de gate PO)

H-B1 seed 6 familles · H-B5 GENERIC seul + alerte · H-B7 tous ACTIFS · H-MGR skip si manager manquant · H-NOTIF in-app Must / push Should.

**Ne pas coder** si un up RH **contredit** B1/B5 — sinon appliquer les hypothèses ci-dessus.
