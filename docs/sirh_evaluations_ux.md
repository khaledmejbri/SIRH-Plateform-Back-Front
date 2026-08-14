# UX — Évaluations M07 — Plateforme RH & RH Connect

| | |
|---|---|
| **Rôle** | UI/UX designer SIRH |
| **Date** | 12 août 2026 |
| **Statut** | Brief handoff front (**ne code pas**) |
| **Sources** | [`sirh_evaluations_vague.md`](sirh_evaluations_vague.md) (D-E01…D-E06, E3/E5/E6) · [`sirh_evaluations_spec.md`](sirh_evaluations_spec.md) (catalogue, notif) · [`sirh_basefonctionnelle.md`](sirh_basefonctionnelle.md) § 12 · écrans actuels (lecture seule) |
| **Canaux** | Web *Plateforme RH* · Mobile *RH Connect* |
| **Langue / a11y** | Français · **WCAG 2.1 AA** |
| **Design system** | `rh-admin-web/src/app.css` · `rh_mobile_app/lib/core/theme/app_theme.dart` — **pas de nouvelle lib UI** |
| **Hors scope** | Code · écrasement vague/spec · PDF/S3 · M05 chaud/froid · trimestriel · 3ᵉ note RH · redesign shell global |

**Agents suivants** : `sirh-senior-frontend` (web **ou** mobile, dossiers disjoints) après BA ; implémentation alignée E3 → E5 → E6.

---

## 1. Diagnostic actuel (écrans lus)

### 1.1 Synthèse

M07 est **fonctionnel** (campagnes ANNUELLE/SEMESTRIELLE, constructeur + types de questions, workflow 2 étapes, scoring 70/30, RBAC Direction lecture story 5). La dette est **expérience** : vocabulaire « grade / rôle » libre, matching non ancré `famille_metier × niveau_seniorite`, parcours mobile peu guidé (3 CTA), **pas de mode manager** distinct, couleurs § 12.5 absentes du suivi web, empty states orientés debug, Direction privée des templates (alors que l’AC E3 exige la **consultation** sans écriture).

### 1.2 Web — `EvaluationsPage` / `EnhancedTemplatesTab` / `TemplateBuilder`

| Constat (code actuel) | Impact utilisateur | Cible vague / UX |
|---|---|---|
| Onglets « Évaluation générale » / « Compétences **par grade** » | RH pense « grade IT » ≠ profil métier AGUA | Renommer ; filtres **famille × niveau** (E3) |
| TECHNICAL : `role` / `domaine` **texte libre** ; grades `ARCHITECT`, `DIRECTOR` ; `CONFIRMED` | Matching instable ; hors catalogue PO/BA (`JUNIOR`/`CONFIRME`/`SENIOR`/`TEAM_LEAD`) | Selects catalogue ; retirer Architecte / Directeur Must |
| Suivi : score `/20` **sans** pastille VERT/ORANGE/ROUGE | § 12.5 / C-M07-04 invisibles pour RH & Direction | Colonne Alerte + icône + libellé |
| Collab / manager en UUID tronqués (`code`) | File illisible | Nom + matricule (API déjà / à exposer) |
| Direction : onglets templates **masqués** (`useLectureSeule`) | Contredit AC E3 « consulte les templates » | Afficher templates en **lecture seule** (pas de CTA écriture) |
| Écarts analytics `HIGH` / `CRITICAL` anglais brut | Jargon | Libellés FR + icône (jamais couleur seule) |
| Campagne : Annuelle / Semestrielle déjà | Aligné D-E01 | Ne jamais proposer Trimestrielle |
| Erreurs : `alert` + emoji ; pas de skeleton | OK basique | Harmoniser § 4 ; ton pro sans emoji décoratif |
| Modal assign templates : emoji dans labels | Look peu pro | Labels sobres FR |

### 1.3 Mobile — `evaluations_list_screen` / `evaluation_detail_screen` / `home_screen`

| Constat | Impact | Cible |
|---|---|---|
| Carte **Évaluations** toujours sur l’accueil, **sans pastille** | Hors campagne : entrée morte ; en campagne : peu de signal | Pastille compteur si action requise (E5) |
| Empty : « Activez une campagne côté admin… évaluation de test » | Copy **dev**, pas métier | Microcopy § 5 |
| Progress : barre 0,25 / 0,65 + pills ; 3 CTA (Enregistrer / Passer technique / Valider) | Charge cognitive ; validation prématurée | Stepper 1/2 + **1 CTA primaire** (E6) |
| Pas de parcours **manager** (réponse collab + note) | Manager note comme collab ou bloque | Mode manager § 3.4 |
| Score sans couleur § 12.5 | Pas d’alerte terrain | Chip alerte + appréciation |
| 403 mappé Dio partiellement | Message OK ; pas d’écran dédié | État 403 § 4.2 |

### 1.4 Principes de redesign (règles métier inchangées)

1. **Densité vs simplicité** : RH densifie (tableaux) ; collab/manager simplifie (étapes, 1 CTA).
2. **Profil = `famille_metier` × `niveau_seniorite`** — jamais `profil_acces` ni poste libre comme clé.
3. **Couleur + texte + icône** pour alertes et écarts (WCAG).
4. **Direction = lecture seule** : mêmes vues utiles (campagnes, templates, suivi), **zéro** contrôle d’écriture focusable.
5. **Hors campagne ≠ erreur** : empty rassurant.
6. **Ton pro** : français métier ; supprimer jargon admin/test et emoji décoratifs.

---

## 2. Parcours web — Plateforme RH

### 2.1 IA de navigation

```
Évaluations RH
├── Campagnes           (RH/ADMIN écriture · DIRECTION lecture)
├── Templates           (tous BO M07) — sous-onglets : Généraux | Techniques
│     └── écriture masquée si lecture seule
├── Suivi & scores      (tous BO M07)
└── [Should] Alertes profil — collabs sans famille_metier (B5 / H-B5)
```

**Renommages**

| Actuel | Cible |
|---|---|
| Évaluation générale | Templates généraux |
| Compétences par grade | Templates techniques (par profil) |
| Grade | Niveau de séniorité |
| Rôle / famille métier (texte) | Famille métier (liste catalogue) |
| Compétences techniques par grade | Questionnaire technique lié au profil |

**Bandeau Direction** (sous le titre, permanent) :

> Consultation seule — vous visualisez campagnes, questionnaires et scores sans pouvoir les modifier.

---

### 2.2 UX — Campagnes — Web · E1

**Acteur** : RH / ADMIN (écriture) · DIRECTION (lecture)  
**Objectif** : créer / activer une campagne **annuelle** ou **semestrielle** avec template général (+ technique campagne en fallback optionnel).

**Structure**

1. Toolbar : titre + compteur · CTA `+ Nouvelle campagne` (**masqué** lecture seule).
2. Liste cartes : nom · type (Annuelle \| Semestrielle) · période · statut · templates liés · mini-analytics.
3. Actions selon statut (écriture) : Assigner templates · Activer · Terminer.

**Règles visibles**

- Type : **uniquement** Annuelle / Semestrielle (pas Trimestrielle).
- Aide : « Fenêtres habituelles : juin / décembre (S1, S2, annuelle). »
- Activation sans template **général** publié → message inline bloquant (déjà partiel côté UI).
- Campagne ACTIVE : templates de campagne non modifiables (snapshot) — message métier.

**États** : § 4.1  
**A11y** : labels selects ; focus trap modal ; `aria-live` erreurs ; contrastes badges statut.  
**Handoff** : `page`, `tabs`, `toolbar`, `card`, `btn`, `badge`, `modal`, `form-grid`, `stat-card`.

**Wireframe — création campagne**

```
┌──────────────────────────────────────────────────────────────┐
│ Campagnes (3)                          [+ Nouvelle campagne] │
├──────────────────────────────────────────────────────────────┤
│ Créer une campagne                                           │
│ Nom * [ Campagne annuelle 2026                         ]     │
│ Type * ( ) Annuelle  ( ) Semestrielle                        │
│      Aide : mi-parcours (semestre) ou bilan annuel.          │
│ Année [2026]  Début [Juin ▾]  Fin [Décembre ▾]               │
│ Template général * [ Éval. générale 2026 ▾ ]                 │
│ Template technique campagne (optionnel fallback) [ — ▾ ]     │
│                         [Annuler]  [Créer la campagne]       │
└──────────────────────────────────────────────────────────────┘
```

---

### 2.3 UX — Constructeur templates (profil) — Web · E3

**Acteur** : RH / ADMIN (écriture) · DIRECTION (lecture liste + preview)  
**Objectif** : publier un GENERIC commun ou un TECHNICAL lié à **`famille_metier` × `niveau_seniorite`**.

#### Flux création TECHNICAL

```
[1 Métadonnées] → [2 Questions] → [3 Aperçu] → [4 Publier]
```

**Écran 1 — Métadonnées**

| Champ | UI | Obligatoire |
|---|---|---|
| Nom | input | oui |
| Description | textarea | non |
| Famille métier | **select catalogue** (seed BA) | oui (TECHNICAL) |
| Niveau de séniorité | **select fermé** | oui (TECHNICAL) |
| Domaine | optionnel Should (affichage, **pas** clé matching) | non |

**Niveaux UI (alignés BA)** : Junior · Confirmé · Senior · Team lead  
Codes : `JUNIOR` · `CONFIRME` · `SENIOR` · `TEAM_LEAD`  
**Retirer** de l’UI Must : Architecte, Directeur. Alias lecture `CONFIRMED` → afficher « Confirmé ».

**Familles seed (libellés UI)**

| Code | Libellé |
|---|---|
| `EXPLOITATION` | Exploitation / terrain assainissement |
| `GENIE_CIVIL` | Génie civil / travaux |
| `DEV_LOGICIEL` | Développement logiciel / SI |
| `SUPPORT_ADMIN` | Support administratif |
| `MAINTENANCE` | Maintenance technique |
| `HSE_QUALITE` | HSE / qualité |

(+ familles actives ajoutées par RH).

**Écran 2 — Questions**

- Liste ordonnée (↑↓ Must ; drag Should).
- CTA `+ Ajouter une question` → panneau inline.
- Types (inchangés § 12.3) — libellés FR :

| Type API | Libellé UI | Hint |
|---|---|---|
| TEXT | Texte court | Une ligne |
| PARAGRAPH | Paragraphe | Réponse libre |
| MULTIPLE_CHOICE | Choix unique | Radio |
| CHECKBOX | Cases à cocher | Plusieurs options |
| RATING | Note | Score numérique |
| SCALE | Échelle | Ex. 1 à 5 + libellés |
| NUMBER | Nombre | Valeur |
| DATE | Date | Sélecteur |

- GENERIC : sections prédéfinies (Objectifs N-1, Savoir-faire…).
- TECHNICAL : défaut SCALE 1–5 + libellés Débutant → Expert.
- Obligatoire · poids · placeholder.

**Écran 3 — Aperçu** : rendu lecture seule « vue collaborateur ».  
**Écran 4 — Publier** : confirm « Utilisable pour le matching famille × niveau ».

**Liste templates**

- Filtres : statut · **famille** · **niveau** (TECHNICAL).
- Carte : badges `Publié` + `Exploitation × Senior` (pas UUID).
- Direction : boutons Publier / Archiver / Nouveau **absents** ; « Voir les questions » OK.

**Wireframe — builder TECHNICAL**

```
┌─────────────────────────────────────────────────────────────┐
│ Templates techniques (par profil)          [+ Nouveau]      │
│ Filtres: [Statut ▾] [Famille ▾] [Niveau ▾]                  │
├─────────────────────────────────────────────────────────────┤
│ Nouveau template technique                                  │
│ Nom * [ Compétences — Exploitation Senior            ]      │
│ Famille * [ Exploitation ▾ ]  Niveau * [ Senior ▾ ]         │
│                                                             │
│ Questions (3)                          [+ Ajouter question] │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ 1. Sécurité chantier *          Échelle 1–5   [↑][↓][✕] │ │
│ │ 2. Lecture plans *              Échelle 1–5   [↑][↓][✕] │ │
│ └─────────────────────────────────────────────────────────┘ │
│              [Annuler]  [Enregistrer brouillon]  [Publier]  │
└─────────────────────────────────────────────────────────────┘
```

**États** : § 4.1 · **A11y** : label/contrôle ; `aria-describedby` erreurs ; cibles ≥ 44 px si usage tactile admin.

---

### 2.4 UX — Suivi & scores — Web · E6

**Acteur** : RH / ADMIN / DIRECTION  
**Objectif** : avancement validation croisée + score + **couleur d’alerte** § 12.5.

**Structure**

1. Stats : Total · En cours · Validées · Taux · **Alertes orange** · **Alertes rouge**.
2. Tableau : Campagne | Collaborateur | Manager | Étape | Statut croisé | Score/20 | **Alerte** | Analyser.
3. Panneau analytics : self / manager / final · sections · écarts FR · reco ; aide « Score final : 70 % manager + 30 % auto-évaluation ».

**Should** : filtre « Profil métier incomplet » (GENERIC seul — B5).

**Wireframe — suivi**

```
┌──────────────────────────────────────────────────────────────────────┐
│ [12] Total  [5] En cours  [7] Validées  [2] Orange  [1] Rouge        │
│ Statut [Tous ▾]  Alerte [Tous ▾]                     [Actualiser]    │
├──────────────────────────────────────────────────────────────────────┤
│ Campagne │ Collaborateur │ Manager │ Étape │ Statut │ Score │ Alerte │
│ Ann. 26  │ Ben Ali K.    │ Trabelsi│ 2/2   │ Validée│ 11/20 │ ● Orang│
│          │ MAT-0042      │         │       │        │ Satisf│ 2 crit.│
└──────────────────────────────────────────────────────────────────────┘
```

**Handoff** : `table`, `badge`, `stats-grid`, `heatbar` ; classes `badge--eval-vert|orange|rouge` sur tokens success/warning/error existants — **pas** de 4ᵉ palette métier.

---

### 2.5 UX — Fiche collaborateur (contexte E2, léger)

Hors page M07 mais **prérequis matching** : champs Famille métier + Niveau de séniorité (listes).  
Si vides à l’activation : pas de blocage collab ; côté RH badge « Profil incomplet » sur la ligne suivi (Should).

---

## 3. Parcours mobile — RH Connect

### 3.1 Entrée & notification — E5

**Acteur** : Collaborateur · Manager  
**Objectif** : savoir qu’une campagne est ouverte et y accéder en ≤ 2 taps.

| Élément | Comportement |
|---|---|
| Accueil · carte Évaluations | Toujours accessible ; **pastille** numérique si ≥ 1 évaluation non archivée / action requise |
| Notification in-app | À création évaluation (type `EVALUATION_CAMPAGNE_OUVERTE`) · deep-link `/evaluations/{id}` |
| Push | Should si canal NOTIF déjà branché |
| Manager | Notif Should quand collab a validé sa partie |

**Copy notif collab (alignée spec, ton UX)**  
Titre : Évaluation ouverte  
Corps : La campagne « {nom} » est active. Complétez votre auto-évaluation (générale puis compétences).

**Empty hors campagne** : § 4.2 / § 5 — **pas** de jargon admin/test.

---

### 3.2 UX — Liste — Mobile

**Acteur** : Collab (mes évaluations) · Manager (à noter / équipe)

```
Mes évaluations
[ À faire | Terminées ]           ← collab
ou
[ À noter | Suivi équipe ]        ← si responsable ACTIF du nœud
```

Carte : nom campagne · type · statut croisé FR · étape 1/2 · score + couleur si dispo · chevron.

**Wireframe — liste collab**

```
┌─────────────────────────────┐
│ ← Mes évaluations      ↻    │
├─────────────────────────────┤
│ [ À faire ]  [ Terminées ]  │
│                             │
│ ┌─────────────────────────┐ │
│ │ En cours                │ │
│ │ Campagne annuelle 2026  │ │
│ │ Étape 1 sur 2 · Générale│ │
│ │ Manager : S. Trabelsi   │ │
│ └─────────────────────────┘ │
└─────────────────────────────┘
```

---

### 3.3 UX — Self (collaborateur) — E6

**Acteur** : Collaborateur  
**Objectif** : Générale → Technique, puis valider sa partie.

**Structure (1 composition, CTA unique)**

1. En-tête : campagne · manager · statut.
2. **Stepper** : `1 Générale` → `2 Compétences` (à faire / en cours / terminé — pas couleur seule).
3. Compteur : « 3 questions obligatoires restantes ».
4. Questions groupées par section.
5. Pied sticky :
   - Étape 1 : primaire `Enregistrer et continuer` · secondaire `Enregistrer brouillon`.
   - Étape 2 : primaire `Enregistrer mes compétences` puis, une fois complet, `Valider mon auto-évaluation`.
6. Aide : « Votre manager notera après votre validation. »

**Règles visibles**

- Passage technique bloqué si obligatoires générales manquantes.
- Validation collab bloquée si technique incomplète (si TECHNICAL présent).
- GENERIC seul : stepper 1 étape ou étape 2 masquée + « Pas de questions techniques pour votre profil ».

**Wireframe — détail self étape 1**

```
┌─────────────────────────────┐
│ ← Évaluation                │
│ Campagne annuelle 2026      │
│ Manager : S. Trabelsi       │
│ ●───○  1 Générale → 2 Comp. │
│ 2 obligatoires restantes    │
├─────────────────────────────┤
│ Objectifs année précédente  │
│ ┌─────────────────────────┐ │
│ │ Bilan des objectifs *   │ │
│ │ [____________________]  │ │
│ └─────────────────────────┘ │
├─────────────────────────────┤
│ [ Enregistrer brouillon ]   │
│ [ Enregistrer et continuer ]│  ← primaire
└─────────────────────────────┘
```

---

### 3.4 UX — Manager — E6

**Acteur** : Manager = responsable **ACTIF** du nœud (D-E02 / M01)  
**Objectif** : noter **après** le collaborateur ; valider sa partie.

**Structure**

1. Bannière : « Vous notez en tant que manager ».
2. Par question : réponse collab (lecture) + saisie note manager.
3. Si collab n’a pas répondu : champ **désactivé** +  
   « Le collaborateur n’a pas encore répondu à cette question. »
4. Synthèse écarts : Aligné / Modéré / Élevé / Critique — icône + texte.
5. CTA : `Enregistrer les notes` puis `Valider mes notes manager`.

**Interdit UI** : pas de bouton « noter pour RH » ; Direction n’évalue pas sur mobile.

**Wireframe — question manager**

```
┌─────────────────────────────┐
│ Vous notez en tant que      │
│ manager                     │
│ Q. Sécurité chantier        │
│ Réponse collab. : Autonome  │
│ Votre note :                │
│ [1][2][3][4][5]  Autonome   │
│ Écart : Aligné              │
└─────────────────────────────┘
```

---

### 3.5 Design system mobile (réemploi)

| Token | Usage M07 |
|---|---|
| `AppTheme.primary` / `primarySurface` | Stepper actif, liens |
| `success` / `warning` / `error` | Badges VERT / ORANGE / ROUGE |
| `textSecondary` | Aides, empty |
| Cards radius 16, boutons hauteur ≥ 52 | Conserver |
| Pastille accueil | Compteur ou dot `error` sur carte Évaluations |

---

## 4. États UI (matrice)

### 4.1 Web

| État | Campagnes | Templates | Suivi |
|---|---|---|---|
| **Vide** | « Aucune campagne. Créez la première campagne annuelle ou semestrielle. » + CTA | « Aucun template. Créez un questionnaire général ou technique. » | « Aucune évaluation. Elles apparaissent à l’activation d’une campagne. » |
| **Loading** | Skeleton cartes / `loading` | Idem | Skeleton lignes table |
| **Erreur** | Bandeau `alert--error` + Réessayer | Idem | Idem |
| **403** | Page Accès refusé ; pas de fuite d’actions | Lecture OK Direction ; écriture absente | Lecture + Analyser OK |
| **Succès** | Refresh liste après create/activate | Badge Publié | Expand analytics |
| **Lecture seule** | Pas de `+` / Activer / Terminer / Assigner | Pas de Nouveau / Publier / Archiver | Tableau + analytics sans mutation |

### 4.2 Mobile

| État | Liste | Détail |
|---|---|---|
| **Hors campagne / vide** | Illus. + copy § 5 | — |
| **Loading** | `CircularProgressIndicator` | Idem (+ shimmer questions Should) |
| **Erreur réseau** | Icône offline + Réessayer (pattern actuel, copy FR) | Idem |
| **403** | « Accès refusé. Cette évaluation ne vous est pas destinée. » + Retour | Idem |
| **404** | — | « Évaluation introuvable. » |
| **Bloqué règle** | — | Inline sous CTA (obligatoires manquantes, etc.) |
| **Succès** | SnackBar court | SnackBar + refresh providers |

---

## 5. Microcopy FR (référence)

### 5.1 Web

| Contexte | Texte |
|---|---|
| Sous-titre RH | Campagnes, questionnaires par profil métier et suivi des scores. |
| Sous-titre Direction | Consultation des campagnes, questionnaires et scores (lecture seule). |
| Type campagne | Annuelle · Semestrielle |
| Aide fréquence | Bilan mi-parcours (semestriel) ou bilan annuel compétences et objectifs. |
| Publier TECHNICAL | Ce template sera proposé aux collaborateurs de la famille et du niveau choisis. |
| Sans template général | Impossible d’activer : publiez et assignez d’abord un template général. |
| Campagne active templates | Les questionnaires des évaluations déjà créées ne changent plus (instantané conservé). |
| Profil incomplet | Profil métier incomplet — questionnaire général uniquement. |
| Alerte orange | Alerte RH — 2 critères sous le seuil. |
| Alerte rouge | Plan d’action obligatoire — escalade direction générale. |
| Manager manquant (Should RH) | Impossible de créer l’évaluation : aucun manager actif sur le nœud. |

### 5.2 Mobile

| Contexte | Texte |
|---|---|
| Empty | Aucune campagne d’évaluation n’est en cours. Revenez lorsque RH aura ouvert une campagne. |
| Notif collab | Votre évaluation « {campagne} » est ouverte. Complétez votre auto-évaluation. |
| Notif manager (Should) | {Prénom} a validé son évaluation. À vous de noter. |
| Stepper | Étape 1 sur 2 — Évaluation générale · Étape 2 sur 2 — Compétences |
| Aide manager | Vous ne pouvez noter une question qu’après la réponse du collaborateur. |
| Pas de technique | Aucune question technique pour votre profil. Validez après l’étape générale. |
| CTA self fin | Valider mon auto-évaluation |
| CTA manager fin | Valider mes notes manager |
| Snack succès | Réponses enregistrées. |

**À supprimer** : « évaluation de test », « côté admin », IDs techniques, emoji décoratifs dans labels métier.

---

## 6. Codage couleur scores & écarts (§ 12.5)

**Seules** couleurs métier d’alerte évaluation. Toujours **libellé + icône** (WCAG).

### 6.1 Alerte (`couleurAlerte`)

| Code | Condition (rappel) | UI libellé | Token DS | Icône (texte alt.) |
|---|---|---|---|---|
| **VERT** | &lt; 2 critères sous seuil (défaut note &lt; 3), ou profil stable | Situation stable | `success` / `#16A34A` | Check |
| **ORANGE** | **2** critères sous le seuil | Alerte RH | `warning` / `#F59E0B` | Triangle |
| **ROUGE** | **≥ 3** critères insuffisants | Plan d’action · escalade DG | `error` / `#DC2626` | Alerte forte |

**Appréciation score /20** (texte, indépendant de la pastille) :

| Score /20 | Libellé |
|---|---|
| ≤ 7 | Insuffisant |
| ≤ 10 | À améliorer |
| ≤ 14 | Satisfaisant |
| ≤ 17 | Positif |
| &gt; 17 | Excellent |

Afficher **score + appréciation + pastille alerte** (trois signaux).

### 6.2 Écarts self / manager

| Code API | Libellé FR | Icône | Complément |
|---|---|---|---|
| ALIGNED | Aligné | ≈ | — |
| MODERATE | Écart modéré | ~ | — |
| HIGH | Écart élevé | ↑ | + badge warning |
| CRITICAL | Écart critique | !! | + badge error |

Aide RH : « Score final : 70 % notes manager + 30 % auto-évaluation » (si notes manager présentes).

---

## 7. Wireframes ASCII (compléments)

### 7.1 Accueil mobile — pastille campagne

```
┌─────────────────────────────┐
│ Bonjour, Karim              │
│ Services                    │
│ ┌────┐ ┌────┐ ┌────┐        │
│ │Doc │ │Form│ │Éval│ ← (1)  │
│ └────┘ └────┘ │    │        │
│               └────┘        │
└─────────────────────────────┘
```

### 7.2 Direction web — lecture seule

```
┌────────────────────────────────────────────┐
│ Évaluations RH                             │
│ Consultation seule — Direction             │
│ [ Campagnes ] [ Templates ] [ Suivi ]      │
│ Cartes / listes sans boutons d’écriture    │
└────────────────────────────────────────────┘
```

### 7.3 GENERIC — liste + preview (lecture Direction OK)

```
┌────────────────────────────────────────────┐
│ Templates généraux                         │
│ [Statut ▾]                    (pas de +)   │
│ ┌────────────────────────────────────────┐ │
│ │ Évaluation annuelle 2026   [Publié]    │ │
│ │ 12 questions · v3                      │ │
│ │                    [Voir les questions]│ │
│ └────────────────────────────────────────┘ │
└────────────────────────────────────────────┘
```

---

## 8. Handoff composants (front)

### 8.1 Format livrable par écran (checklist)

Pour chaque écran livré par le front, vérifier :

| Rubrique | Contenu attendu |
|---|---|
| Acteur | RH / ADMIN / DIRECTION / Collab / Manager |
| Objectif | Une phrase |
| Structure | Zones + **CTA primaire unique** |
| États | vide / loading / erreur / succès / 403 / lecture seule |
| Règles visibles | type campagne, matching profil, barre étapes, motif blocage |
| A11y | focus, labels, contraste, tactile ≥ 44 px mobile |
| Handoff | composants / classes existants |

### 8.2 Web — réutiliser

| Composant / classe | Usage |
|---|---|
| `page` / `page__header` / `page__title` | Shell page |
| `tabs` / `tab` / `tab--active` | Navigation M07 |
| `toolbar` · `btn` · `btn--primary\|ghost\|sm\|success\|warning` | Actions |
| `card` · `form-grid` · `form-group` · `field-input` | Formulaires builder |
| `badge` · `badge--success\|warning\|info\|default` | Statuts |
| `table` · `stats-grid` · `stat-card` · `MiniStat` | Suivi |
| `modal` · `modal-backdrop` | Assign templates |
| `alert alert--error` | Erreurs |
| `empty-state` | Vides |
| `heatbar` | Sections analytics |
| `TemplateBuilder` | Étendre : selects famille/niveau ; retirer grades hors catalogue ; preview |
| `EnhancedTemplatesTab` | Filtres famille × niveau ; mode `lectureSeule` |
| `useLectureSeule` · Accès refusé | Direction |
| **À ajouter (tokens existants)** | `badge--eval-vert\|orange\|rouge` · `EvalAlertBadge` · `DiscrepancyLabel` |

### 8.3 Mobile — réutiliser

| Élément | Usage |
|---|---|
| `AppTheme` | Couleurs / boutons |
| `EvaluationsListScreen` / cards | Liste + empty métier |
| `EvaluationDetailScreen` | Découper self vs manager ; stepper |
| `_QuestionCard` · chips SkillLevel | Questions |
| `home_screen` `_ServiceCard` Évaluations | Pastille compteur |
| Providers Riverpod | Invalider après save |
| **À ajouter** | `EvaluationStepper` · `EvalAlertChip` · `ManagerRatingRow` · `_HorsCampagneView` · mapping notif → deep-link |

### 8.4 Ordre d’implémentation UI (aligné PO)

1. **E3** web : libellés + selects famille × niveau + preview ; Direction lecture templates.  
2. **E5** mobile : empty métier + pastille + deep-link notif.  
3. **E6** : stepper + CTA unique self · parcours manager · badges couleur suivi web.  
4. E1 libellés campagne (léger) · E2 champs fiche (hors page M07).

### 8.5 Critères a11y check-list

- [ ] Contraste texte/badge ≥ 4.5:1 (vérifier orange sur fond clair).  
- [ ] Alerte jamais **couleur seule** (libellé + icône).  
- [ ] Focus visible modales / onglets.  
- [ ] Mobile : zones tactiles ≥ 44×44 px.  
- [ ] Labels FR sur tous les champs ; erreurs liées au champ.  
- [ ] Direction : aucun contrôle d’écriture focusable.  
- [ ] Stepper : état annoncé (pas seulement pastille colorée).

---

## 9. Hors scope UX (rappel)

- Export PDF / S3 · bascule formations M05 · chaud/froid.  
- Fréquence trimestrielle (Could).  
- Notation RH 3ᵉ voix.  
- Nouvelle lib UI · dark mode · redesign shell global.  
- Modification des règles de scoring / pondération.

---

## 10. Fichiers de référence (lecture)

| Fichier | Rôle |
|---|---|
| `docs/sirh_evaluations_vague.md` | Décisions PO / stories (**ne pas écraser**) |
| `docs/sirh_evaluations_spec.md` | Spec BA E1–E5 (**ne pas écraser**) |
| `docs/sirh_basefonctionnelle.md` § 12 | Règles métier / couleurs |
| `rh-admin-web/src/pages/EvaluationsPage.tsx` | Campagnes + suivi actuel |
| `rh-admin-web/src/pages/EnhancedTemplatesTab.tsx` | Liste templates |
| `rh-admin-web/src/components/TemplateBuilder.tsx` | Constructeur |
| `rh_mobile_app/.../evaluations_list_screen.dart` | Liste |
| `rh_mobile_app/.../evaluation_detail_screen.dart` | Parcours saisie |
| `rh_mobile_app/.../home_screen.dart` | Entrée rubrique |
| `rh-admin-web/src/app.css` · `app_theme.dart` | Design system |

---

*Fin du brief UX M07 — prêt handoff `sirh-senior-frontend` (web ou mobile) et BA E6.*
