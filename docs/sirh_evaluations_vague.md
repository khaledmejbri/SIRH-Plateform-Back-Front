# Vague « évaluations M07 » — coordination PO

| | |
|---|---|
| **Rôle** | Product Owner SIRH RH-Évènement (expert RH) |
| **Date** | 12 août 2026 |
| **Statut** | Stories **E1–E5 livrées** (backend + web + mobile) · E6 polish UX Should · ups RH catalogue familles · E7–E8 Could |
| **Source métier** | [`sirh_basefonctionnelle.md`](sirh_basefonctionnelle.md) § 12 (M07), § 10.2 (hors vague), § 4 (RBAC), § 23 |
| **Vague rôles** | [`sirh_vague_roles_qualite.md`](sirh_vague_roles_qualite.md) (story 5 M07 RBAC **livrée**, non rouverte) |
| **Position backlog** | **Après** vague hiérarchie H1 (UX org) si capacité partagée web ; **indépendante** de story 7 congés. Ne bloque pas M05 chaud/froid ni PDF/S3. |
| **Interdit** | Code `svc-*`, `rh-admin-web`, `rh_mobile_app` dans cette vague PO |

**Agents attendus ensuite** : `sirh-business-analyst` (E1–E5) · `sirh-ui-ux` (parcours web constructeur + mobile évaluation — look) · `sirh-senior-backend` + `sirh-senior-frontend` en parallèle après BA (dossiers disjoints).

---

## 1. Avis d’expert SIRH / RH (PO)

### 1.1 Diagnostic

M07 est **déjà Livré** sur le cœur métier : campagnes, templates GENERIC / TECHNICAL, workflow self → manager, scoring 70/30, couleurs, RBAC story 5. Le besoin utilisateur n’est **pas** « inventer les évaluations », c’est **industrialiser** ce qui est partiel ou fragile côté produit :

| Attente utilisateur | État actuel (doc + code) | Écart produit |
|---|---|---|
| Questionnaire **paramétrable** Admin/RH | Constructeur + types de questions existent | UX / gouvernance templates « par profil » perçue comme incomplète |
| Design plus clair web + mobile | Fonctionnel | Dette **expérience**, pas règle métier |
| Notif + rubrique à fin d’année / semestre | Scheduler + Kafka notif partiels ; liste mobile si campagne active | Fiabiliser **activation → notif + entrée visible** |
| Questions **standard + techniques** selon grade/profil | Résolution `niveauSeniorite` + `roleMetier` ; campagne juin/déc | Clé de matching **pas ancrée** sur la fiche collaborateur (poste / qualification libres ; pas de `famille_metier` structurée) |
| Templates selon profil | TECHNICAL ciblable rôle+niveau | Catalogue métier RH **non verrouillé** ; risque de matching client-side / params manuels |
| Annuelle **et** semestrielle **/ou** trimestrielle | Enum : `ANNUELLE` \| `SEMESTRIELLE` seulement | Trimestriel **non livré** — à trancher (ci-dessous) |

**Ce qui ne doit pas re-ouvrir** : RBAC M07 story 5 (RH/ADMIN écriture, DIRECTION lecture, anti-IDOR ownership). Notation manager = **manager du nœud** (cohérent vague hiérarchie), pas `DIRECTION`.

### 1.2 Pourquoi pas le trimestriel en Must (AGUA)

Pour un opérateur d’assainissement (terrain, multi-sites, effectifs hétérogènes) :

1. **Charge** : 4 cycles/an × (self + manager) = fatigue évaluation + file managers saturée → baisse de qualité des notes.  
2. **Valeur** : le semestre couvre déjà le **mi-parcours** ; l’annuel porte compétences / objectifs N→N+1. Un 3ᵉ rythme n’ajoute pas de pilotage RH sans process d’entretien dédié.  
3. **Code & doc** : déjà alignés sur S1/S2 + annuelle (fenêtre juin / décembre côté campagnes). Introduire `TRIMESTRIELLE` = nouveau type, calendrier, templates, reporting — coût disproportionné pour un Could.

**Décision** : Must = **annuelle + semestrielle** ; trimestrielle = **Could** uniquement si l’expert RH AGUA exige un sous-périmètre (ex. période d’essai / population cadrée) — pas le catalogue général.

---

## 2. Décisions PO verrouillées (D-E*)

### D-E01 — Catalogue de fréquences (Must)

| Fréquence | Statut vague | Usage |
|---|---|---|
| `ANNUELLE` | **Must** | Bilan compétences + objectifs N / N+1 |
| `SEMESTRIELLE` | **Must** | Bilan mi-parcours (S1 / S2) |
| `TRIMESTRIELLE` | **Could / Cible** | Hors Must ; pas de story d’implémentation tant que up RH B2 ≠ oui ciblé |

Calendrier opérationnel **conservé** (cohérent code actuel) : fenêtres d’ouverture type **juin** et **décembre** pour les campagnes périodiques, sauf décision RH contraire documentée (up B3).

### D-E02 — Qui note quoi

| Acteur | Rôle dans l’évaluation | Vague |
|---|---|---|
| **Collaborateur** | Auto-évaluation (général puis compétences) | Must (existant) |
| **Manager** (responsable ACTIF du nœud d’affectation) | Notation croisée ; poids 70 % si notes manager présentes | Must (existant) |
| **RH / ADMIN** | Templates, campagnes, suivi scores / couleurs, alertes orange-rouge | Must (existant) |
| **RH comme 3ᵉ noteur** | — | **Could** (hors vague) |
| **DIRECTION** | Lecture BO uniquement (story 5) | Inchangé |

Pas de changement de pondération 70/30 ni de grille /20 dans cette vague.

### D-E03 — Clé de matching templates (décision PO proposée)

**Ne pas** utiliser `profil_acces` (droit d’accès ≠ métier).  
**Ne pas** matcher uniquement sur `poste_libelle` / `qualification_affectation` texte libre (instable, non catalogue).  
**Ne pas** inventer silencieusement un « grade » unique insuffisant (Senior Java ≠ Senior génie civil).

**Clé Must proposée** (2 axes, alignée sur le code templates existant) :

| Axe | Champ produit | Exemples |
|---|---|---|
| **Famille métier** | Nouveau catalogue RH `famille_metier` (code + libellé) | `DEV_LOGICIEL`, `GENIE_CIVIL`, `EXPLOITATION`, `SUPPORT_ADMIN`… |
| **Niveau de séniorité** | `niveau_seniorite` catalogue fermé | `JUNIOR`, `CONFIRME`, `SENIOR`, `TEAM_LEAD` (aligné templates) |

Résolution questionnaire à l’affectation d’une évaluation :

```
Template GENERIC (campagne)          → questions standard communes
+ Template TECHNICAL                 → famille_metier × niveau_seniorite
    priorité : famille+niveau > famille seule > fallback campagne
```

- `poste` / `fonction` / `qualification` restent des **libellés fiche** (affichage, reporting) — pas la clé de matching.  
- Snapshot `famille_metier` + `niveau_seniorite` **figé** sur l’évaluation à la création (évite dérive si fiche change en cours de campagne).  
- **Up RH B1** : valider le catalogue initial des familles AGUA (sinon BA démarre avec catalogue minimal éditable RH).

### D-E04 — Paramétrage questionnaire

Admin **ou** RH (écriture BO inchangée) peut :

- créer / publier / archiver des templates GENERIC et TECHNICAL ;  
- composer questions (types déjà prévus § 12.3), sections, poids, obligatoire ;  
- rattacher un TECHNICAL à `famille_metier` × `niveau_seniorite` ;  
- assigner à une campagne un GENERIC (+ TECHNICAL de campagne optionnel en fallback).

Pas de nouveau microservice. Pas de template « par `profil_acces` ».

### D-E05 — Notification & rubrique mobile

À l’**activation** d’une campagne (et à la création de l’évaluation collaborateur) :

1. **Notification** in-app (canal NOTIF existant / Kafka `rh.notifications`) — Must.  
2. **Push** mobile si le canal push est déjà branché pour NOTIF ; sinon in-app + pastille rubrique restent Must, push = Should technique.  
3. Rubrique **Évaluations** visible sur l’accueil / navigation dès qu’au moins une évaluation non archivée existe pour le collaborateur (campagne ACTIVE).

Hors vague : e-mail obligatoire, SMS.

### D-E06 — UX (besoin produit, look = UI/UX)

**Must produit** : parcours compréhensible en 2 étapes (Générale → Technique), états de validation croisée lisibles, distinction self vs manager, empty state « pas de campagne », constructeur templates orienté « profil métier ».  
**Look & feel** : délégué à `sirh-ui-ux` (web + mobile) — pas de spec visuelle dans ce fichier.

### D-E07 — Hors vague (explicite)

- Export **PDF + S3** évaluations (§ 12.5 cible CDC).  
- Bascule auto formations recommandées → **M05**.  
- Évaluations formation **chaud / froid** (M05.B / M05.C).  
- Notation RH 3ᵉ voix ; trimestriel généralisé.

---

## 3. Questions ouvertes

### Bloquantes (expert RH AGUA — max 5)

| # | Question | Si non tranché |
|---|---|---|
| **B1** | Validez-vous la clé **`famille_metier` × `niveau_seniorite`** (vs poste seul / grade seul) ? Liste initiale des familles AGUA ? | PO applique D-E03 ; BA démarre avec catalogue éditable + 4–6 familles seed |
| **B2** | Faut-il une fréquence **trimestrielle** pour tout le monde, pour une population, ou **pas du tout** ? | **Pas du tout** (D-E01) — Could seulement |
| **B3** | Calendrier campagnes : conserver **juin / décembre** comme fenêtres principales ? | Oui (comportement actuel) |
| **B4** | Qui peut être **manager noteur** M07 : strictement manager ACTIF du nœud (comme M01) ? | **Oui** — aligné vague hiérarchie |
| **B5** | Un collaborateur **sans** `famille_metier` renseignée à l’activation : bloquer, ou GENERIC seul + alerte RH ? | PO : **GENERIC seul** + file d’alerte RH « profil incomplet » (Should UX) |

### Non bloquantes

- Libellés UI « Confirme » vs `CONFIRMED` technique.  
- Nombre max de questions par template.  
- Relance J+7 si auto-évaluation non démarrée.  
- Push FCM vs in-app only selon infra NOTIF.

---

## 4. Ups to expert RH (AGUA) — atelier oui / non / nuance

1. **Fréquences** : confirmer Must = annuelle + semestrielle ; trimestriel hors catalogue général.  
2. **Clé métier** : confirmer `famille_metier` + `niveau_seniorite` ; fournir liste familles (assainissement, travaux, SI, admin, etc.).  
3. **Qui note** : self + manager uniquement ; RH pilote / alerte, ne note pas (sauf Could futur).  
4. **Sans famille métier** : OK GENERIC seul + alerte RH.  
5. **Fenêtres** juin / décembre OK pour S1/S2 et annuelle.  
6. **Manager noteur** = responsable du service d’affectation (pas Direction, pas N+2).  
7. **Population** : tous les collaborateurs ACTIFS d’une campagne, ou exclusion stagiaires / intérim ? (PO défaut : **tous ACTIFS** de la campagne ciblée.)

---

## 5. Stories produit (ordre Must → Should → Could)

Ordre d’implémentation proposé : **E1 → E2 → E3 → E4 → E5** ; E6 Should ; E7–E8 Could.  
UI/UX en parallèle dès E3/E5 (écrans).

---

### Story E1 — Fréquences Must & calendrier campagne (produit + doc)

**Module** : M07 | **État actuel** : Livré (`ANNUELLE` / `SEMESTRIELLE`)  
**Acteurs** : RH, ADMIN  
**Valeur** : verrouiller le catalogue de fréquences pour éviter le scope creep trimestriel et clarifier le calendrier AGUA.

#### User story
En tant que RRH, je veux créer uniquement des campagnes **annuelles** ou **semestrielles** selon un calendrier clair, afin de piloter les bilans mi-parcours et de fin d’année sans multiplier les cycles.

#### Périmètre
- **In** : doc + messages produit ; UI ne propose pas `TRIMESTRIELLE` ; conserver règles fenêtres juin/déc (ou les documenter si assouplies après B3).  
- **Out** : implémenter trimestriel ; changer scoring.

#### Hypothèses
- D-E01 appliqué.  
- Enum code déjà sans trimestriel.

#### Questions ouvertes
- B2, B3 (ups RH).

#### Critères d’acceptation (Given / When / Then)

1. **Given** un RH sur la création de campagne, **When** il choisit le type, **Then** seuls **Annuelle** et **Semestrielle** sont proposés.  
2. **Given** la doc § 12.1, **When** on la lit après cette vague, **Then** trimestriel est marqué **Cible / Could**, pas Livré.  
3. **Given** une campagne semestrielle S1 ou S2, **When** elle est planifiée selon le calendrier retenu, **Then** elle est acceptée sans exiger un type trimestriel.

#### Impact doc
- § 12.1, glossaire « Campagne », § 23 (ligne vague évaluations).

#### Priorité : **Must** | **Prêt BA** : **oui**

---

### Story E2 — Fiche collaborateur : `famille_metier` + `niveau_seniorite`

**Module** : M00 (+ conso M07) | **État actuel** : Partiel (poste / qualification texte ; matching éval hors fiche)  
**Acteurs** : RH, ADMIN (écriture) · Collaborateur (lecture)  
**Valeur** : une clé stable pour affecter le bon questionnaire technique.

#### User story
En tant que RRH, je veux renseigner la **famille métier** et le **niveau de séniorité** sur la fiche collaborateur, afin que les campagnes d’évaluation proposent automatiquement les bonnes questions techniques.

#### Périmètre
- **In** : catalogue `famille_metier` administrable (RH/ADMIN) ; champ `niveau_seniorite` liste fermée ; édition fiche web ; exposition lecture API pour M07.  
- **Out** : utiliser `profil_acces` comme clé ; matching sur libellé poste libre ; trimestriel.

#### Hypothèses
- D-E03 ; seed familles si B1 non fourni.  
- Un collaborateur = une famille + un niveau à un instant T.

#### Questions ouvertes
- B1, B5.

#### Critères d’acceptation (Given / When / Then)

1. **Given** un RH, **When** il crée ou met à jour un collaborateur, **Then** il peut sélectionner `famille_metier` et `niveau_seniorite` (listes contrôlées).  
2. **Given** un collaborateur sans famille, **When** une campagne s’active, **Then** l’évaluation reçoit le template GENERIC et un signal « profil métier incomplet » est disponible côté RH (pas d’erreur bloquante utilisateur final).  
3. **Given** `profil_acces=RO`, **When** on résout le template technique, **Then** le profil d’accès **n’intervient pas** dans le matching.

#### Impact doc
- § 5 (fiche collaborateur), § 12.3, § 22 hypothèses, glossaire.

#### Priorité : **Must** | **Prêt BA** : **oui** (sous réserve seed familles B1)

---

### Story E3 — Templates paramétrables par profil (GENERIC + TECHNICAL)

**Module** : M07 | **État actuel** : Livré (constructeur) / **Partiel** (rattachement profil métier produit)  
**Acteurs** : RH, ADMIN  
**Valeur** : questionnaires réutilisables « standard + technique selon métier/niveau ».

#### User story
En tant que RRH, je veux créer des templates **généraux** (questions communes) et **techniques** liés à une famille métier et un niveau, afin d’évaluer un senior développeur autrement qu’un junior ou qu’un profil génie civil.

#### Périmètre
- **In** : rattachement TECHNICAL ↔ `famille_metier` × `niveau_seniorite` ; publication ; types de questions existants ; preview / liste filtrable par profil.  
- **Out** : nouveau moteur de scoring ; PDF ; questions IA générées.

#### Hypothèses
- Types de réponse § 12.3 suffisants pour Must.  
- DIRECTION lecture seule (story 5).

#### Questions ouvertes
- Non bloquant : quotas de questions ; libellés niveaux.

#### Critères d’acceptation (Given / When / Then)

1. **Given** un RH, **When** il publie un template TECHNICAL pour `DEV_LOGICIEL` × `SENIOR`, **Then** ce template est sélectionnable / résolvable pour les collaborateurs de ce couple.  
2. **Given** un template GENERIC de campagne, **When** un collaborateur ouvre son évaluation, **Then** il voit d’abord les questions standard, puis les questions techniques de son profil (si template résolu).  
3. **Given** un compte DIRECTION, **When** il consulte les templates, **Then** aucune action d’écriture n’est proposée.

#### Impact doc
- § 12.3 (remplacer formulation floue « grade / rôle » par famille × niveau).

#### Priorité : **Must** | **Prêt BA** : **oui** | **UI/UX** : **oui** (constructeur)

---

### Story E4 — Activation campagne : résolution auto + création évaluations

**Module** : M07 | **État actuel** : Livré (campagne + scheduler) / **Partiel** (fiabilité matching depuis fiche)  
**Acteurs** : RH (activation) · Collaborateur / Manager (évaluations créées)  
**Valeur** : une activation = population couverte avec le bon questionnaire, sans param manuel par agent.

#### User story
En tant que RRH, je veux qu’à l’activation d’une campagne les évaluations soient créées avec le bon couple de templates (général + technique résolu), afin de lancer le cycle sans configuration individuelle.

#### Périmètre
- **In** : à l’activation (ou job associé) : créer 1 évaluation / collaborateur cible ACTIF ; snapshot famille + niveau ; résoudre TECHNICAL ; fallback GENERIC seul.  
- **Out** : PDF ; M05 ; re-noter les règles 70/30.

#### Hypothèses
- Une seule évaluation par (campagne, collaborateur) — règle existante.  
- Population = ACTIFS (up B7 défaut).

#### Critères d’acceptation (Given / When / Then)

1. **Given** une campagne PLANIFIEE avec GENERIC publié et des TECHNICAL publiés, **When** RH active la campagne, **Then** chaque collaborateur ACTIF cible reçoit une évaluation avec snapshot profil et template technique résolu si possible.  
2. **Given** deux collaborateurs Junior Dev et Senior Génie civil, **When** la campagne est active, **Then** leurs questions techniques diffèrent selon E3.  
3. **Given** une campagne déjà ACTIVE, **When** RH tente de réassigner les templates de campagne, **Then** la modification est refusée ou sans effet sur les évaluations déjà créées (snapshot préservé) — BA précise le message.

#### Impact doc
- § 12.2, § 12.3 (ordre de résolution).

#### Priorité : **Must** | **Prêt BA** : **oui**

---

### Story E5 — Mobile : notification + rubrique Évaluation en campagne

**Module** : M07 + NOTIF | **État actuel** : Partiel (liste / détail existent ; notif à fiabiliser)  
**Acteurs** : Collaborateur, Manager  
**Valeur** : le collaborateur sait qu’il doit évaluer et trouve l’entrée sans chercher.

#### User story
En tant que collaborateur, je veux être **notifié** à l’ouverture de ma campagne et voir une **rubrique Évaluations** accessible, afin de compléter mon auto-évaluation dans les délais.

#### Périmètre
- **In** : événement notif à création/activation évaluation ; entrée navigation / accueil si évaluation ouverte ; deep-link vers détail si possible.  
- **Out** : redesign complet hors brief UI/UX ; SMS ; PDF.

#### Hypothèses
- D-E05 ; canal NOTIF existant.  
- Manager reçoit aussi une notif quand le collaborateur a validé sa partie (Should si capacité — sinon Could).

#### Critères d’acceptation (Given / When / Then)

1. **Given** une évaluation créée pour moi suite à activation campagne, **When** l’événement est publié, **Then** je reçois une notification in-app (et push si canal disponible).  
2. **Given** au moins une évaluation non archivée, **When** j’ouvre l’accueil mobile, **Then** la rubrique / raccourci Évaluations est visible et mène à la liste.  
3. **Given** aucune campagne / évaluation ouverte, **When** j’ouvre Évaluations, **Then** un empty state explique qu’aucune campagne n’est en cours (pas d’erreur technique).

#### Impact doc
- § 12.2 (scheduler / notif), § 20 parcours collaborateur.

#### Priorité : **Must** | **Prêt BA** : **oui** | **UI/UX** : **oui** (mobile)

---

### Story E6 — Clarté parcours web + mobile (expérience pro)

**Module** : M07 | **État actuel** : Livré fonctionnel / **Partiel** UX  
**Acteurs** : Collaborateur, Manager, RH  
**Valeur** : réduire les erreurs de saisie et le support « je ne vois pas où noter ».

#### User story
En tant que collaborateur ou manager, je veux un parcours d’évaluation **clair** (étapes, qui doit faire quoi, avancement), afin de terminer la validation croisée sans formation lourde.

#### Périmètre
- **In** : besoin produit ci-dessus ; wireframes / UI states par `sirh-ui-ux` ; implémentation front web (suivi RH) + mobile (parcours).  
- **Out** : changer workflow statutaire ; nouvelles pondérations.

#### Hypothèses
- États § 12.4 inchangés.  
- Look = UI/UX, pas inventé ici.

#### Critères d’acceptation (Given / When / Then)

1. **Given** une évaluation en étape générale, **When** le collaborateur ouvre le détail, **Then** l’UI indique clairement étape 1/2, questions obligatoires restantes, et que le manager note après lui.  
2. **Given** le collaborateur n’a pas répondu à une question, **When** le manager tente de noter cette question, **Then** l’action reste impossible avec message explicite (règle existante).  
3. **Given** un RH sur le suivi campagne, **When** il consulte la liste, **Then** statut validation croisée et signal couleur (si score) sont lisibles sans jargon technique brut.

#### Impact doc
- § 12.4 (formulation parcours) ; éventuellement companion UX dédié.

#### Priorité : **Should** (Must si atelier RH priorise l’adoption terrain) | **Prêt BA** : **oui après** brief UI/UX | **UI/UX** : **oui**

---

### Story E7 — Fréquence trimestrielle (population ciblée)

**Module** : M07 | **État actuel** : Cible  
**Acteurs** : RH  
**Valeur** : cycles courts pour une population précise (ex. période d’essai) — seulement si up B2.

#### User story
En tant que RRH, je veux pouvoir lancer une campagne **trimestrielle** pour une population définie, afin de suivre un dispositif RH spécifique sans imposer 4 cycles à tout le monde.

#### Périmètre
- **In** : type `TRIMESTRIELLE` + règles population (BA).  
- **Out** : remplacer semestriel pour tous.

#### Critères d’acceptation (Given / When / Then)

1. **Given** l’expert RH a validé B2 « population ciblée », **When** RH crée une campagne trimestrielle, **Then** seuls les collaborateurs du périmètre défini sont évalués.  
2. **Given** B2 non validé, **When** on consulte le backlog, **Then** cette story reste Could non engagée.

#### Impact doc
- § 12.1 si activée.

#### Priorité : **Could** | **Prêt BA** : **non** (gate up B2)

---

### Story E8 — Notation RH (3ᵉ voix)

**Module** : M07 | **État actuel** : Cible  
**Acteurs** : RH  
**Valeur** : arbitrage / calibration — hors vague Must.

#### User story
En tant que RRH, je veux pouvoir ajouter une note ou un avis RH après la validation croisée, afin de calibrer les cas critiques (rouge / écart CRITICAL).

#### Périmètre
- **In** : à définir si un jour priorisé.  
- **Out** : vague actuelle.

#### Critères d’acceptation
- À rédiger si priorisation ; **pas** prêt BA.

#### Priorité : **Could** | **Prêt BA** : **non**

---

## 6. Impact `sirh_basefonctionnelle.md`

| Section | Action vague |
|---|---|
| § 3.2 carte M07 | Rester **Livré** ; noter **vague améliorations** (Partiel UX / matching fiche) sans rétrograder tout le module |
| § 12.1 | Fréquences Must vs trimestriel Cible |
| § 12.2–12.3 | Snapshot famille×niveau ; résolution templates ; notif activation |
| § 12.4–12.5 | Inchangé scoring ; PDF/S3 / M05 restent cible |
| § 5 / fiche | Champs `famille_metier`, `niveau_seniorite` |
| § 20 | Notif + rubrique |
| § 23 | Case « Vague évaluations M07 » |
| Glossaire | Famille métier ; préciser Campagne |

Mise à jour **légère** faite en même temps que ce fichier (marqueurs vague, pas d’invention de Livré).

---

## 7. Prêt BA / UI/UX

| Story | Prêt BA | UI/UX requis | Backend | Front |
|---|---|---|---|---|
| E1 | Oui | Léger (libellés type campagne) | Faible | Web |
| E2 | Oui (seed familles) | Fiche collaborateur | `svc-referentiel-rh` | Web (+ lecture mobile) |
| E3 | Oui | **Oui** constructeur | `svc-evaluation` | Web |
| E4 | Oui | Faible | `svc-evaluation` | — |
| E5 | Oui | **Oui** mobile | eval + NOTIF | Mobile |
| E6 | Après UX | **Oui** web+mobile | Faible | Web + Mobile |
| E7–E8 | Non (gates) | — | — | — |

**Handoff** : Prêt pour `sirh-business-analyst` sur **E1–E5**.  
Lancer `sirh-ui-ux` en parallèle sur **E3 + E5 (+ E6)**.  
Ne pas coder tant que B1 (catalogue familles) n’a pas au moins un seed PO/RH.

---

## 8. Synthèse décisions (1 slide)

| ID | Décision |
|---|---|
| D-E01 | Must = annuelle + semestrielle ; trimestriel = Could |
| D-E02 | Self + manager ; RH administre / alerte ; pas 3ᵉ note Must |
| D-E03 | Matching = `famille_metier` × `niveau_seniorite` ; pas `profil_acces` |
| D-E04 | Templates GENERIC + TECHNICAL paramétrables RH/ADMIN |
| D-E05 | Notif + rubrique à activation / création évaluation |
| D-E06 | Clarté parcours = besoin produit ; look = UI/UX |
| D-E07 | PDF/S3, M05 chaud-froid = hors vague |
