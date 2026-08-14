# Vague « hiérarchie / organigramme » — coordination PO

| | |
|---|---|
| **Rôle** | Product Owner SIRH RH-Évènement |
| **Date** | 12 août 2026 |
| **Statut** | Décisions PO posées · **ups RH** avant BA complet sur multi-niveaux · stories UX / sync prêtes BA |
| **Source métier** | [`sirh_basefonctionnelle.md`](sirh_basefonctionnelle.md) § 4, § 5 (M00), § 6.1 (M01), § 22–23 |
| **Vague rôles** | [`sirh_vague_roles_qualite.md`](sirh_vague_roles_qualite.md) (story 3 **livrée**, non rouverte) |
| **UX companion** | [`sirh_hierarchie_ux.md`](sirh_hierarchie_ux.md) (look & parcours édition — **pas** les règles workflow) |
| **Position dans le backlog** | **Avant story 7** (types congés + PJ). Ne bloque pas story 6 (ADR refresh). |
| **Interdit** | Code `svc-*`, `rh-admin-web`, `rh_mobile_app` dans cette vague PO |

**Agents attendus ensuite** : `sirh-business-analyst` (stories H1–H3) · `sirh-ui-ux` déjà livré (réutiliser) · `sirh-senior-frontend` (web org) · backend seulement si H3 (sync `superieur`).

---

## 1. Avis d’expert SIRH (PO)

### 1.1 Diagnostic métier

Le ressenti « ça ne marche pas comme la hiérarchie » vient surtout d’un **écart d’attente**, pas d’un trou total dans le moteur M01 :

| Attente fréquente (métier / terrain) | Règle produit actuelle (story 3) |
|---|---|
| Demande → N+1 → N+2 (Direction) selon l’arbre | Demande → **1 seul** valideur 1er niveau = **manager ACTIF du nœud d’affectation** → **RRH** |
| L’organigramme = chaîne d’approbation | L’organigramme = **structure** + **désignation du responsable de service** |
| « Direction » valide les congés | `DIRECTION` = **lecture BO** ; **pas** valideur M01 |
| Champ `superieur` fiche = qui valide | `superieur` = **dérivé / informatif** ; source de vérité = **manager du nœud** + snapshot `valideur_attendu` |

Si l’arbre est mal peuplé (nœuds sans responsable, agents mal affectés, DnD bancal → mauvais parent), le workflow **semble** cassé alors qu’il applique correctement la règle « manager du nœud ou skip RRH ».

### 1.2 Décision PO (tranchée) — validation multi-niveaux

**Décision D-H01 (Must, vague actuelle)** : conserver **1 niveau opérationnel** puis RRH.

```
Collaborateur → Manager ACTIF du nœud d’affectation → RRH → APPROUVÉE / REFUSÉE
```

- **Pas** de remontée automatique N+1 → N+2 le long de l’arbre parent.
- **Pas** de validation M01 par `DIRECTION` via la hiérarchie.
- Le JWT `RO` / `RESPONSABLE` **n’ouvre pas** le droit de valider hors nœud (403) — déjà livré story 3.

**Pourquoi (produit AGUA)**  

1. Objectif stratégique « −80 % délai » : une chaîne N+2 / Direction **allonge** systématiquement les congés / sorties / missions.  
2. Cohérence vague rôles : Direction = pilotage lecture, pas file d’approbation.  
3. Simplicité opérationnelle terrain (assainissement, multi-sites) : un **responsable de service** identifiable bat une matrice d’approbation floue.  
4. Story 3 déjà livrée (snapshot + 403) : **ne pas rouvrir** sans décision écrite expert RH + impact SLA.

**Cible éventuelle (hors vague, Could)** : workflow multi-niveaux **configurable** (ex. congé > X jours → N+2) — uniquement si l’expert RH AGUA le demande et accepte le coût délai / complexité. **Pas** dans le Must avant story 7.

### 1.3 Décision PO — rôle de l’organigramme

**Décision D-H02** : deux concepts distincts, une seule UI « Organigramme ».

| Concept | Rôle | Qui écrit |
|---|---|---|
| **Structure organisationnelle** | Arbre services / unités, rattachement parent, effectifs | RH / ADMIN |
| **Désignation du valideur 1er niveau** | Champ `manager` du nœud = qui reçoit `EN_VALIDATION_SUPERIEUR` | RH / ADMIN |
| **Chaîne d’approbation multi-étages** | **Hors** organigramme actuel | — (Cible si RH tranche autrement) |

L’arbre parent/enfant sert à **lire** l’organisation et à **naviguer**, pas à enchaîner des validations M01.

### 1.4 Décision PO — sync `superieur` fiche

**Décision D-H03 (Should)** : synchroniser `collaborateur.superieur` depuis le **manager du nœud d’affectation** (à l’affectation d’unité, au changement de manager du nœud, et job de rattrapage optionnel).  

- Affichage fiche / mobile « mon supérieur » aligné sur le valideur.  
- **Ne remplace pas** le snapshot `valideur_attendu` à la création de demande (source de vérité M01 inchangée).

### 1.5 Décision PO — UX (alignement avec `sirh_hierarchie_ux.md`)

**Décision D-H04 (Must)** : prioriser une **édition fiable** (sélecteur parent, panneau responsable dominant, badge « Sans responsable ») plutôt qu’un DnD « joli mais fragile ».  

Libellés UI : **service / unité / responsable** (pas « nœud »). Confirm retrait manager : mention M01 → RRH **acceptée** produit (H2 UX).

**Vocabulaire** : UI = « service » ou « unité » selon type ; doc technique = « nœud ». Catalogue de `type_noeud` fermé = **up RH** (non bloquant pour démarrer l’UX).

---

## 2. Questions ouvertes

### Bloquantes (max 5 — pour l’expert RH ; sinon D-H01 tient)

| # | Question | Si non tranché |
|---|---|---|
| B1 | Confirmez-vous **1 valideur opérationnel (manager du service) → RRH** pour **tous** les types M01 (congé, sortie, mission) ? | PO applique D-H01 pour les 3 types |
| B2 | Souhaitez-vous un **2ᵉ niveau** (ex. chef de département / Direction) pour **certains** cas seulement (durée, type) ? | Reste **Cible** ; pas de story Must multi-niveaux |
| B3 | En l’absence de responsable ACTIF sur le service : skip **direct RRH** OK ? | Oui (règle § 5.2 actuelle) |
| B4 | Un **suppléant** / délégation temporaire du responsable est-il requis avant go-live organigramme « pro » ? | Non en vague ; Could ultérieur |
| B5 | La fiche doit-elle afficher un `superieur` **toujours égal** au manager du nœud (sync auto) ? | PO part sur **oui** (D-H03 Should) |

### Non bloquantes

- Libellé unique « service » vs « unité » dans toute l’UI.  
- Lien croisé Organigramme ↔ page Structure RH.  
- Mobile : ancre « Mon unité ».  
- DnD conservé en progressive enhancement vs retiré (front + UX).

---

## 3. Ups to expert RH (AGUA) — à faire valider

Liste courte à présenter en atelier (oui / non / avec nuance) :

1. **Chaîne M01** : valider D-H01 (manager service → RRH) **ou** spécifier les cas multi-niveaux (qui, pour quels types, quels délais max).  
2. **Rôle Direction** : confirmer « consultation BO uniquement », **jamais** valideur M01 via l’organigramme.  
3. **Sans responsable** : confirmer bascule auto vers RRH + obligation RH de combler les badges « Sans responsable ».  
4. **Qui peut être nommé responsable** d’un service : tout collaborateur ACTIF ? obligation `profil_acces=RO` ? (aujourd’hui : droit = **nœud**, profil recommandé mais non exclusif).  
5. **Sync supérieur fiche** : OK d’écraser / aligner `superieur` sur le manager du nœud (plus de saisie manuelle divergente) ?  
6. **Granularité d’affectation** : un agent = **une** unité principale (hypothèse § 22) — toujours vrai pour AGUA ?  
7. **Types de nœuds** souhaités (Direction / Département / Service / Unité / Équipe) — liste fermée ou libre ?  
8. **Responsable d’un nœud parent** voit-il / valide-t-il les demandes des **sous-unités** ? (**PO : non** — seule l’unité d’affectation compte.) À faire **contredire explicitement** si le métier veut autrement.

---

## 4. Stories produit

Ordre d’implémentation proposé : **H1 → H2 (doc + messages) → H3** ; H4 Could.  
Story 7 (congés) **après** H1 au minimum (UX org ne bloque pas le code congés, mais la **clarté hiérarchie** évite les tickets « ça ne remonte pas à la Direction » pendant la recette congés).

---

### Story H1 — Organigramme web professionnel (édition fiable)

**Module** : M00 | **État actuel** : Livré (fonctionnel) / **Partiel** (UX / utilisabilité)  
**Acteurs** : RH, ADMIN (écriture) · DIRECTION (lecture)  
**Valeur** : une structure fiable et lisible pour désigner le bon responsable → demandes M01 routées correctement.

#### User story
En tant que RRH, je veux éditer l’organigramme de façon claire et fiable (créer / rattacher / assigner un responsable), afin que chaque service ait un valideur identifiable et que le workflow M01 reflète l’organisation réelle.

#### Périmètre
- **In** : parcours web décrit dans `sirh_hierarchie_ux.md` (sélecteur parent prioritaire, panneau responsable, badges sans responsable, mode lecture Direction, microcopy FR, a11y de base).  
- **In** : alignement mobile lecture (libellés Responsable / Sans responsable) en Should léger si capacité.  
- **Out** : changement de règle M01 multi-niveaux ; fusion Organigramme ↔ Structure ; nouvelle lib diagramme (xyflow, etc.) ; édition mobile.

#### Hypothèses
- UX companion `sirh_hierarchie_ux.md` = référence look & parcours.  
- APIs organigramme existantes suffisent (pas de nouveau microservice).  
- D-H01 / D-H02 inchangées.

#### Questions ouvertes
- Non bloquantes : DnD keep vs drop ; catalogue `type_noeud` (up RH #7).

#### Critères d’acceptation (Given / When / Then)

1. **Given** un RH authentifié sur Organigramme, **When** il ouvre le détail d’un service sans responsable, **Then** un signal fort « Sans responsable » est visible (carte + panneau) et le CTA principal propose d’assigner un responsable.  
2. **Given** un RH, **When** il rattache un service via « Rattacher… » / sélecteur parent (sans DnD), **Then** le service apparaît sous le parent choisi et un cycle parent↔descendant est refusé avec message clair.  
3. **Given** un compte DIRECTION, **When** il consulte l’organigramme, **Then** aucune action d’écriture n’est proposée et un bandeau de consultation est affiché.  
4. **Given** un RH qui retire le responsable d’un service, **When** il confirme, **Then** un message l’informe que les demandes M01 de cette unité pourront aller directement à la RRH.

#### Impact doc
- § 5.3 (clarifier structure vs valideur) ; glossaire si besoin « Responsable de service ».

#### Priorité : **Must** | **Prêt BA** : **oui** (UI/UX déjà posé)

---

### Story H2 — Règles hiérarchie / valideurs explicites (produit + messages)

**Module** : M00 + M01 | **État actuel** : Livré (règle story 3) / **Partiel** (compréhension utilisateur)  
**Acteurs** : Collaborateur, RO / manager nœud, RH, Direction  
**Valeur** : aligner l’attente « hiérarchie » sur la règle réelle ; réduire les tickets « ça devrait passer par la Direction ».

#### User story
En tant que collaborateur ou responsable, je veux comprendre **qui** doit valider ma demande et **pourquoi**, afin de ne pas confondre l’arbre organisationnel avec une chaîne N+1 → N+2 → Direction.

#### Périmètre
- **In** : textes d’aide (mobile file « à valider », détail demande, confirmations RH org) ; doc fonctionnel § 5 / § 6.1 / § 22 ; éventuelle aide courte dans l’écran Organigramme.  
- **Out** : implémenter multi-niveaux ; changer le snapshot / 403.

#### Hypothèses
- Moteur story 3 reste la vérité runtime.  
- Ups RH B1–B3 : si contradiction, **stop** et re-prioriser (story Could multi-niveaux) avant d’écrire les messages.

#### Critères d’acceptation (Given / When / Then)

1. **Given** une demande créée avec manager de nœud ACTIF, **When** le demandeur consulte le suivi, **Then** l’étape affiche le **responsable de son service** (pas « Direction » comme valideur attendu).  
2. **Given** une unité sans manager ACTIF, **When** une demande M01 est soumise, **Then** le statut passe en validation RRH et le suivi l’explique (pas d’étape supérieur fantôme).  
3. **Given** la doc `sirh_basefonctionnelle.md`, **When** on lit § 5.3 et § 6.1, **Then** structure vs chaîne d’approbation et D-H01 y sont explicites.

#### Impact doc
- § 5.2–5.3, § 6.1, § 20 (parcours RO), § 22 hyp. 2, § 23 (dette « supérieur vs manager » → cochée / reformulée).

#### Priorité : **Must** | **Prêt BA** : **oui** (sous réserve ups RH B1 = confirmation D-H01)

**Statut backend (12 août 2026)** : **livré** dans `svc-referentiel-rh` — `GET …/suivi` expose `valideur_attendu_*` + `message_explication` ; `etape_superieur_requise` = snapshot only ; détail demande enrichi (Should). Front mobile/web : à consommer.

---

### Story H3 — Synchronisation `superieur` fiche ← manager du nœud

**Module** : M00 | **État actuel** : Partiel (`superieur` optionnel, pas source M01)  
**Acteurs** : RH (indirect) · Collaborateur (lecture fiche / org)  
**Valeur** : une seule vérité affichée « mon supérieur » = responsable du service ; moins d’écarts fiche / organigramme.

#### User story
En tant que RRH, je veux que le supérieur affiché sur la fiche soit aligné sur le responsable du service d’affectation, afin d’éviter deux hiérarchies divergentes.

#### Périmètre
- **In** : sync à l’affectation d’unité ; sync quand le manager du nœud change ; lecture seule ou champ non éditable divergent sur la fiche (BA tranche UX champ).  
- **Out** : changer le calcul du valideur M01 (reste manager nœud + snapshot) ; multi-affectations.

#### Hypothèses
- Up RH #5 = oui.  
- Un collaborateur = une unité (§ 22.1).

#### Critères d’acceptation (Given / When / Then)

1. **Given** un collaborateur affecté à un service dont le manager est M, **When** on consulte sa fiche, **Then** `superieur` référence M (ou vide si pas de manager).  
2. **Given** un changement de manager du nœud, **When** la RH enregistre, **Then** les fiches des membres ACTIFS du nœud ont `superieur` mis à jour.  
3. **Given** une demande M01 déjà créée, **When** le manager du nœud change après coup, **Then** le `valideur_attendu` snapshot de la demande **ne change pas** (comportement story 3 conservé).

#### Impact doc
- § 5.2 (`superieur` dérivé **et** synchronisé) ; § 23 case cochée / reformulée.

#### Priorité : **Should** | **Prêt BA** : **oui** (après confirmation up RH #5)

**Statut backend (12 août 2026)** : **livré** — sync T1–T4 (create/MAJ collab, assign/retire manager) ; `superieur_identifiant` saisi ignoré ; snapshots M01 intacts. Front fiche : lecture seule à brancher.

---

### Story H4 — (Could) Validation multi-niveaux configurable

**Module** : M01 | **État actuel** : Cible  
**Acteurs** : selon matrice RH  
**Valeur** : uniquement si AGUA exige N+2 / Direction pour certains cas.

#### User story
En tant qu’expert RH, je veux pouvoir exiger un second niveau d’approbation pour certains cas, afin de respecter la gouvernance interne sans allonger toutes les demandes.

#### Périmètre
- **In** : règles par type / durée ; nouveaux statuts ; UI files.  
- **Out** : vague actuelle.

#### Priorité : **Could** | **Prêt BA** : **non** (bloqué ups B1–B2)

---

## 5. Impact `sirh_basefonctionnelle.md`

| Section | Action |
|---|---|
| § 5.2 | Rappeler sync `superieur` (H3) ; valideur = manager nœud |
| § 5.3 | **Marquer** : organigramme = structure + responsable, **≠** chaîne multi-niveaux |
| § 6.1 | **Marquer** : pas de remontée parent ; Direction hors valideurs M01 |
| § 4.2 | Déjà OK (valider si manager nœud) — pas de changement de matrice |
| § 20 | Parcours RO : « manager du nœud », pas « hiérarchie ascendante » |
| § 22 | Hyp. 2 : sync auto `superieur` (Should) |
| § 23 | Reformuler dette « supérieur vs manager » → traitée story 3 + H3 Should |
| Vague rôles | Insérer vague hiérarchie **avant** story 7 |

Diffs proposés / appliqués : voir commits locaux du PO sur les sections marquées uniquement (pas de refactor doc hors M00/M01/hiérarchie).

---

## 6. Ordre backlog (vue PO)

```
[Vague rôles 1–5 : FAIT]
        │
        ▼
[Vague hiérarchie]  H1 Must (UX org)  ∥  H2 Must (clarté règles)
        │            H3 Should (sync superieur)
        │            H4 Could (multi-niveaux) — gate RH
        ▼
[Story 7] types congés + PJ
        ▼
[Story 6] refresh token (ADR)
```

---

## 7. Handoff

| Destinataire | Action |
|---|---|
| **Expert RH AGUA** | Valider ups § 3 (surtout 1, 2, 3, 5, 8) |
| **`sirh-business-analyst`** | Spec H1–H3 (API inchangée H1 ; sync H3 ; copy H2) |
| **`sirh-ui-ux`** | Déjà livré → `sirh_hierarchie_ux.md` ; ajuster si ups RH changent le wording M01 |
| **`sirh-senior-frontend`** | Implémenter H1 (web) ; mobile lecture si capacité |
| **`sirh-senior-backend`** | **H2 + H3 livrés** (suivi valideur + sync `superieur`) ; H1 sans nouveau contrat |
| **Orchestrateur** | Ne pas lancer multi-agents code tant que B1 non confirmé si le sponsor pousse N+2 |

**Prêt BA global vague** : **oui pour H1–H3** · **non pour H4** · **gate RH** sur B1/B5 avant de communiquer « hiérarchie = multi-niveaux » au métier.

---

*Fichier de coordination PO — ne pas confondre avec `sirh_hierarchie_ux.md` (look) ni avec le code.*
