# Vague « pointage QR + géofence » (M09) — coordination PO

| | |
|---|---|
| **Rôle** | Product Owner SIRH RH-Évènement (expert RH / SIRH) |
| **Date** | 13 août 2026 |
| **Statut** | Décisions PO **D-P\*** posées · Must = QR renouvelable + géofence 50 m + admin web + GPS mobile RH · **ups RH** avant BA complet sur catalogue sites / ENTREE-SORTIE |
| **Source métier** | [`sirh_basefonctionnelle.md`](sirh_basefonctionnelle.md) § 14 (M09), § 19 (C-M09-01, C-L-03/04, C-S-\*), § 21–23 |
| **Position backlog** | Activation M09 **Partiel → recette anti-fraude allégée** ; **indépendante** de M07 / story 7 congés / M03. Ne bloque pas paie. |
| **Cadre technique (mental, non-ADR)** | Solution **0 risque / rapide / fiable** avec architecte : Must = QR fixe renouvelable + géofence 50 m + BO web + config GPS mobile RH/ADMIN. Pas de biométrie face, pas de TOTP 5 min, pas de HMAC serveur en Must. |
| **Interdit** | Code `svc-*`, `rh-admin-web`, `rh_mobile_app` · ADR (architecte) · commit |

**Agents attendus ensuite** : `sirh-architect` (ADR `svc-presence` / gateway / stockage GPS) · `sirh-business-analyst` (P1–P5) · `sirh-ui-ux` (écrans web QR + mobile scan / GPS) · backend + frontend en parallèle **après** ADR + BA (dossiers disjoints).

---

## 1. Avis d’expert SIRH / RH (PO)

### 1.1 Diagnostic

M09 est aujourd’hui un **prototype UX** (scan QR + caméra face) **sans** `svc-presence`, sans horodatage métier, sans anti-fraude recettable (§ 14.5, § 22 hyp. 6, § 23). Le CDC cible (QR TOTP ~5 min → GPS → face on-device) est **lourd** (RGPD biométrie C-L-03/04, liveness, consentement, HMAC) et incompatible avec une vague « activation rapide et fiable ».

| Attente utilisateur (cette vague) | État actuel | Écart produit |
|---|---|---|
| QR **renouvelable tous les 3 mois**, généré Admin/RH web | UI mobile scan ; pas de génération / cycle de vie QR | **Créer** borne/site + QR + renouvellement |
| Bouton **« Télécharger code QR »** + rappel ~2 mois | Absent | BO web + alerte expiration |
| Affichage / impression à un **emplacement fixe** | Non modélisé | Entité **site / borne** + GPS |
| RH/ADMIN mobile : **poser / modifier GPS** du QR | Absent | Écran mobile RH/ADMIN |
| Collaborateur : scan **uniquement** si ≤ **50 m** | GPS mentionné CDC, non branché | Géofence serveur + rejet hors zone |
| Anti-fraude « pas de QR imprimé chez soi » | Face + TOTP CDC non livrés | **Géofence** = levier Must ; face = vague suivante |

**Position PO** : le vrai risque terrain AGUA (assainissement, multi-sites, équipes mobiles) n’est pas « un collègue pointe pour un autre à 2 m », c’est **le télétravail de pointage** (QR photo / impression à domicile). Un QR **fixe** + **géofence stricte** traite ~80 % de ce risque sans ouvrir le chantier biométrie.

### 1.2 Fraude — analyse produit

| Vecteur de fraude | Gravité AGUA | Mitigation Must | Mitigation différée |
|---|---|---|---|
| QR imprimé / photo scannée **hors site** | **Haute** | Géofence **50 m** + GPS obligatoire au scan | — |
| QR volé / recopié d’un autre site | Moyenne | QR **lié au site** ; renouvellement **90 j** | Rotation plus courte (Should) |
| Collègue scannant pour un autre **sur site** | Moyenne | JWT = identité du token ; 1 device session | Face / liveness (Could) |
| Spoof GPS (mock location) | Moyenne–haute | Détection best-effort OS (Should) ; log `device_id` | Attestation Play Integrity / DeviceCheck (Could) |
| Replay scan / horloge manipulée | Moyenne | Horodatage **serveur** (pas client) | HMAC payload (Could) |
| QR permanent jamais renouvelé | Haute | Expiration **J+90** + alerte J-30 | TOTP dynamique 5 min (Cible CDC) |

**Verdict** : Must = **QR + géofence + identité JWT**. La biométrie face reste utile pour l’usurpation *sur site*, mais **hors Must** tant que consentement RGPD opérationnel n’est pas prêt (§ 23).

### 1.3 Sites multiples (AGUA)

AGUA opère typiquement **plusieurs emplacements** (sièges, dépôts, chantiers, stations). § 19.6 le prévoit déjà.

**Décision produit** : un collaborateur peut pointer sur **n’importe quel site ACTIF** dont le QR est valide et dont il est dans le rayon — **pas** de liste blanche « sites autorisés par agent » en Must (complexité RH / affectations terrain). Could : restriction par unité / site d’affectation.

### 1.4 Pause / déjeuner

**Hors Must.** Raisons :

1. Le besoin utilisateur ne demande **pas** de gestion de pause.  
2. Pause ≠ autorisation de sortie M01 (C-M01-01 ≤ 4 h) : mélanger les deux crée des dettes paie / présence.  
3. Sans moteur paie, un « déjeuner » n’a pas de règle de solde ni de seuil légal automatisé dans le SIRH.

**Could ultérieur** : type de pointage `PAUSE_DEBUT` / `PAUSE_FIN` **ou** s’appuyer sur M01 sortie pour absences > X min — **après** usage réel ENTREE/SORTIE.

### 1.5 Pourquoi pas TOTP 5 min ni face en Must

| Option CDC | Coût | Valeur relative Must | Décision |
|---|---|---|---|
| QR TOTP ~5 min (écran / tablette connectée) | Infra borne online, sync horloge, UX fragile terrain | Forte anti-copie **si** écran toujours online | **Cible / Could** — incompatible avec QR **papier** 3 mois |
| Face on-device + consentement | RGPD, enrôlement, ML Kit, blocage 15 min | Anti-usurpation sur site | **Vague suivante** (gate C-L-03/04) |
| QR fixe 90 j + géofence 50 m | Service présence + BO + GPS | Anti « pointage à domicile » + impression terrain | **Must cette vague** |

---

## 2. Décisions PO verrouillées (D-P\*)

### D-P01 — Périmètre Must (verrouillage vague)

**Must** = :

1. Cycle de vie **site / borne de pointage** (libellé, actif, GPS, QR).  
2. Génération QR depuis **Plateforme RH** (RH \| ADMIN).  
3. Téléchargement / impression du QR.  
4. Renouvellement **tous les 90 jours** (calendaire).  
5. Config / modification **GPS** (lat/lon) depuis **mobile** par RH \| ADMIN.  
6. Pointage collaborateur mobile : scan QR **uniquement** si distance GPS ≤ **50 m** du point enregistré.  
7. Enregistrement métier `ENTREE` / `SORTIE` + horodatage serveur + statut `VALIDE` / `REJETE_HORS_ZONE` / `REJETE_QR_*`.

**Hors Must** : voir § 6.

### D-P02 — Rayon géofence

| Paramètre | Valeur Must | Commentaire |
|---|---|---|
| Rayon | **50 m** exact (fixe) | Trancher l’ambiguïté « ≤ 50 m ou moins » : **50 m**, pas 30/20 en Must |
| Mesure | Distance haversine GPS téléphone ↔ lat/lon site | Calcul **serveur** (source de vérité) |
| Configurable par site | **Should** (défaut 50, borne min 20 / max 100) | Hors Must pour éviter débats terrain avant go-live |
| GPS indisponible | **Rejet** clair (pas de fallback Wi-Fi / manuel RO en Must) | Fallback = Should / Cible CDC |

### D-P03 — Modèle QR (pas TOTP en Must)

| Règle | Décision |
|---|---|
| Nature | QR **semi-statique** : payload opaque (id site + id version / secret de version) |
| Durée de validité | **90 jours** à compter de la génération / dernier renouvellement |
| Renouvellement | RH/ADMIN régénère → **invalide** l’ancienne version dès activation de la nouvelle |
| Support physique | Papier **ou** affichage tablette **offline** OK (pas d’exigence d’écran connecté) |
| Unicité | **1 QR actif** par site à un instant T |

*Note architecte (non-ADR)* : le payload ne doit pas être devinable ; détail crypto / URL = ADR.

### D-P04 — Bouton téléchargement vs rappel (clarification besoin)

Le besoin mélangeait « bouton » et « rappel tous les 2 mois ». **Deux artefacts distincts** :

| Artefact | Comportement Must | Pourquoi |
|---|---|---|
| Bouton **« Télécharger code QR »** | **Toujours visible** sur la fiche site (si QR actif ou après 1ʳᵉ génération) | RH doit pouvoir réimprimer après perte / dégradation sans attendre une alerte |
| Alerte / rappel expiration | **J-30** avant `date_expiration` (≈ « tous les 2 mois » sur un cycle de 90 j) | Notification RH (canal NOTIF / cloche BO) ; pas un bouton qui apparaît seulement à J-60 |
| Relance optionnelle | Should : 2ᵉ rappel **J-7** | Renforce sans clutter Must |

**Pas** de masquage du bouton hors fenêtre J-60/J-30.

### D-P05 — Multi-sites

- Must : **N sites** ACTIFS, chacun avec GPS + QR + rayon 50 m.  
- Un scan valide = site dont le QR matche **et** distance ≤ 50 m.  
- Pas de quota « un seul pointage / jour / site » en Must (voir D-P06).

### D-P06 — Types de pointage ENTREE / SORTIE

| Règle | Décision Must |
|---|---|
| Types | `ENTREE` et `SORTIE` obligatoires au choix utilisateur (ou détection assistée UI — BA) |
| Alternance stricte | **Should** : suggérer le type opposé au dernier `VALIDE` ; **ne pas bloquer** un double ENTREE en Must (terrain chaotique) |
| Fuseau | Stockage **UTC** ; affichage **`Africa/Tunis`** (UTC+1, sans DST) |
| Paie / heures | **Aucun** calcul d’heures travaillées, majorations, ni export paie |

### D-P07 — Qui configure le GPS

| Acteur | Canal | Droit |
|---|---|---|
| RH \| ADMIN | Mobile | Créer / modifier lat/lon du site (idéalement « me géolocaliser ici ») |
| RH \| ADMIN | Web | Consulter GPS ; édition manuelle lat/lon **Should** (saisie numérique de secours) |
| DIRECTION | Web | Lecture sites / pointages si exposé (aligné lecture BO) — **Could** liste présence |
| Collaborateur / RO | — | **Pas** de modification GPS site |
| RO | Mobile | Pas de pointage manuel de substitution en Must |

Pose GPS = **sur place** recommandée (précision). Tolérance produit : RH peut ajuster si le QR est déplacé de quelques mètres dans le même local.

### D-P08 — Identité & sécurité produit (aligné C-S-\*)

- Pointage collaborateur : JWT avec rôle `USER` (tous profils).  
- Admin sites / QR / GPS : `RH` \| `ADMIN` (écriture) ; pas `DIRECTION` en écriture.  
- Gateway unique (C-S-01).  
- Pas de biométrie → C-L-04 **non déclenché** pour cette vague ; C-L-03 minimisation GPS (conservation alignée audit / pièces — BA + juridique).

### D-P09 — Amendement contrainte C-M09-01 (cette vague)

**Avant (CDC)** : pointage = QR+GPS **puis** face ; pas de partiel.  
**Must vague** : pointage **recettable** = QR valide + GPS dans rayon ; **pas** d’étape face obligatoire.

Nouvelle formulation doc à porter :

> **C-M09-01 (vague QR)** : un pointage `VALIDE` exige QR actif du site **et** position GPS ≤ rayon du site (50 m). La reconnaissance faciale reste **Cible / vague suivante** (alors C-M09-01b : QR+GPS puis face, pas de partiel).

### D-P10 — Timezone & horodatage

- Serveur = horodatage de vérité (`Instant` UTC).  
- UI = `Africa/Tunis`.  
- Pas de « heure appareil » acceptée pour le statut `VALIDE`.

---

## 3. Questions ouvertes

### Bloquantes (expert RH AGUA — max 5)

| # | Question | Si non tranché |
|---|---|---|
| **B1** | Catalogue initial des **sites** AGUA (noms, combien) pour le seed / recette ? | BA démarre avec CRUD vide + 1–2 sites de démo |
| **B2** | Un agent peut-il pointer sur **tous** les sites, ou seulement son site / unité d’affectation ? | **Tous les sites ACTIFS** (D-P05) |
| **B3** | Faut-il **imposer** l’alternance ENTREE → SORTIE (blocage si non respectée) dès le Must ? | **Non** — suggestion UI only (D-P06) |
| **B4** | Qui reçoit l’alerte J-30 : tous les comptes `RH`, ou un référent par site ? | **Tous les `RH`** (pattern NOTIF existant) |
| **B5** | Conservation des pointages rejetés (hors zone) : même durée que validés (audit) ? | **Oui** — journal anti-fraude 5 ans (C-L-02) côté événements ; détail BA |

### Non bloquantes

- Libellé UI « borne » vs « site de pointage ».  
- Format fichier téléchargé (PNG vs PDF A4 avec consignes d’affichage).  
- Seuil d’alerte RO si N rejets hors zone / jour (Should).  
- Édition lat/lon au clavier sur web.

---

## 4. Ups to expert RH (AGUA) — atelier oui / non / nuance

1. **Must = QR papier/tablette 90 j + géofence 50 m** ; face et TOTP **plus tard** — confirmer.  
2. **Rayon 50 m** fixe partout pour le go-live — OK, ou besoin immédiat de rayons différents (parking vs hall) ? (PO : fixe Must ; configurable Should.)  
3. **Multi-sites ouverts** à tout collaborateur ACTIF — OK pour agents itinérants ?  
4. **ENTREE/SORTIE** sans calcul d’heures ni lien paie — suffisant pour le pilotage RH cette année ?  
5. **Alerte J-30** à tous les RH + bouton téléchargement toujours visible — OK.  
6. **Pas de pause/déjeuner** dans le pointage — OK (absences via M01 si besoin).  
7. **Pas de pointage manuel RO** en secours Must — accepter les rejets GPS / QR (formation terrain) ?

---

## 5. Stories produit (ordre Must → Should → Could)

Ordre d’implémentation proposé : **P1 → P2 → P3 → P4 → P5** (Must) ; **P6–P7** Should ; **P8–P10** Could.  
Architecte **avant** P1 (création `svc-presence` / routage gateway). UI/UX en parallèle dès P2/P4.

---

### Story P1 — Site de pointage + GPS (référentiel)

**Module** : M09 | **État actuel** : Partiel (UI proto, pas de service)  
**Acteurs** : RH, ADMIN  
**Valeur** : ancrer chaque QR à un lieu physique vérifiable.

#### User story
En tant que RRH, je veux créer un **site de pointage** avec un libellé et des coordonnées GPS, afin que le scan ne soit valide que sur place.

#### Périmètre
- **In** : CRUD site (code/libellé, actif, lat, lon, rayon figé 50 m) ; API + persistance.  
- **Out** : génération QR ; scan collaborateur ; face.

#### Hypothèses
- D-P02, D-P05, D-P07.  
- Service présence (ou module dédié) disponible après ADR.

#### Questions ouvertes
- B1 (liste sites).

#### Critères d’acceptation (Given / When / Then)

1. **Given** un RH authentifié, **When** il crée un site avec libellé + lat/lon valides, **Then** le site est `ACTIF` avec rayon **50 m**.  
2. **Given** un site existant, **When** un `USER` sans rôle RH/ADMIN appelle l’API d’écriture, **Then** **403**.  
3. **Given** un site inactif, **When** on tente un pointage contre ce site, **Then** rejet métier (story P4).

#### Impact doc
- § 14 (nouvelle sous-section sites) ; § 19.6.

#### Priorité : **Must** | **Prêt BA** : **oui** (après ADR existence service)

---

### Story P2 — Génération QR 90 j + téléchargement web

**Module** : M09 | **État actuel** : Partiel  
**Acteurs** : RH, ADMIN  
**Valeur** : produire un support affichable / imprimable à l’emplacement fixe.

#### User story
En tant que RRH, je veux **générer** et **télécharger** le code QR d’un site, valable **90 jours**, afin de l’imprimer ou de l’afficher sur tablette.

#### Périmètre
- **In** : générer / régénérer QR ; bouton **toujours visible** « Télécharger code QR » ; `date_expiration` = génération + 90 j ; invalidation de l’ancienne version à la régénération.  
- **Out** : TOTP 5 min ; alerte J-30 (P3) ; scan.

#### Hypothèses
- D-P03, D-P04.  
- Une version active par site.

#### Critères d’acceptation

1. **Given** un site ACTIF sans QR, **When** RH clique générer puis télécharger, **Then** un fichier image (ou PDF) est obtenu et `date_expiration` = J+90.  
2. **Given** un QR déjà actif, **When** RH régénère, **Then** l’ancien QR ne permet plus un pointage `VALIDE`.  
3. **Given** la fiche site, **When** un QR actif existe, **Then** le bouton téléchargement est visible **sans** condition de date.

#### Impact doc
- § 14.1 (remplacer « QR dynamique TOTP » par modèle vague pour l’état Partiel/Livré Must).

#### Priorité : **Must** | **Prêt BA** : **oui**

---

### Story P3 — Alerte expiration J-30

**Module** : M09 | **État actuel** : Partiel  
**Acteurs** : RH  
**Valeur** : éviter les QR périmés oubliés sur les murs (rappel « ~2 mois »).

#### User story
En tant que RRH, je veux être **alerté 30 jours avant** l’expiration du QR d’un site, afin de le renouveler et de réimprimer à temps.

#### Périmètre
- **In** : notification (NOTIF / cloche) à **tous les RH** à J-30 ; badge / bandeau sur fiche site « expire le … ».  
- **Out** : e-mail obligatoire ; SMS ; masquage du bouton téléchargement.

#### Hypothèses
- D-P04 ; B4 = tous les RH.

#### Critères d’acceptation

1. **Given** un QR dont `date_expiration` = aujourd’hui + 30 j, **When** le job / règle d’alerte s’exécute, **Then** chaque compte rôle RH reçoit une notification site concerné.  
2. **Given** un QR expiré non renouvelé, **When** un collaborateur scanne, **Then** rejet `REJETE_QR_EXPIRE` (P4) + message clair.

#### Impact doc
- § 14 exceptions ; § 18 événements notif.

#### Priorité : **Must** | **Prêt BA** : **oui**

---

### Story P4 — Pointage mobile collaborateur (scan + géofence 50 m)

**Module** : M09 | **État actuel** : Partiel (UI sans backend)  
**Acteurs** : Collaborateur (`USER`)  
**Valeur** : enregistrer une présence anti-fraude domicile.

#### User story
En tant que collaborateur, je veux **scanner le QR** du site pour une **entrée** ou une **sortie**, uniquement si je suis à **≤ 50 m**, afin que mon pointage soit reconnu sur place.

#### Périmètre
- **In** : scan → envoi token QR + GPS + type ; validation serveur ; feedback VALIDE / rejet ; historique personnel simple.  
- **Out** : face ; TOTP ; fallback Wi-Fi ; pointage manuel RO ; pause.

#### Hypothèses
- D-P02, D-P06, D-P08, D-P09, D-P10.  
- GPS OS autorisé ; sinon rejet.

#### Critères d’acceptation

1. **Given** un collaborateur dans le rayon ≤ 50 m avec QR actif, **When** il scanne et choisit ENTREE (ou SORTIE), **Then** un pointage `VALIDE` est créé avec horodatage **serveur** UTC.  
2. **Given** le même QR mais distance > 50 m, **When** il scanne, **Then** `REJETE_HORS_ZONE` (pas d’enregistrement `VALIDE`) + message explicite.  
3. **Given** un QR expiré ou invalidé, **When** il scanne même dans le rayon, **Then** rejet QR (pas de contournement GPS).  
4. **Given** GPS refusé / indisponible, **When** il tente de pointer, **Then** rejet avec message d’activation localisation (pas de silent success).

#### Impact doc
- § 14.1–14.3 ; C-M09-01 amendé (D-P09) ; § 22 hyp. 6 ; § 23.

#### Priorité : **Must** | **Prêt BA** : **oui** | **UI/UX** : oui

---

### Story P5 — Pose / modification GPS sur mobile RH-ADMIN

**Module** : M09 | **État actuel** : Partiel  
**Acteurs** : RH, ADMIN  
**Valeur** : calibrer le point de vérité sur le terrain (emplacement réel du QR).

#### User story
En tant que RRH sur mobile, je veux **enregistrer ou modifier** la position GPS de l’emplacement du QR, afin d’aligner la géofence sur le lieu d’affichage réel.

#### Périmètre
- **In** : écran mobile RH/ADMIN : sélection site → « Utiliser ma position » → confirmer ; historique basique de dernière MAJ (qui / quand).  
- **Out** : édition GPS par RO ; geofencing collaborateur.

#### Hypothèses
- D-P07.  
- Même API site que P1.

#### Critères d’acceptation

1. **Given** un RH sur le lieu du QR, **When** il valide « enregistrer ma position » pour le site, **Then** lat/lon du site sont mises à jour.  
2. **Given** un collaborateur `USER` seul, **When** il ouvre l’écran de config GPS site, **Then** accès refusé.  
3. **Given** une MAJ GPS, **When** un scan a lieu ensuite, **Then** la géofence utilise les **nouvelles** coordonnées.

#### Impact doc
- § 14 ; matrice § 4.2 (capacité « admin sites pointage »).

#### Priorité : **Must** | **Prêt BA** : **oui** | **UI/UX** : oui

---

### Story P6 — Rayon configurable + alerte J-7 (Should)

**Module** : M09  
**Valeur** : adapter hall vs parking ; relance courte avant expiration.

#### User story
En tant que RRH, je veux ajuster le rayon par site (bornes raisonnables) et recevoir un rappel J-7, afin d’affiner sans attendre une nouvelle vague majeure.

#### Périmètre
- **In** : rayon 20–100 m (défaut 50) ; 2ᵉ notif J-7.  
- **Out** : Wi-Fi BSSID.

#### Priorité : **Should** | **Prêt BA** : oui (après P1–P5)

---

### Story P7 — Consultation RH des pointages + signalements hors zone (Should)

**Module** : M09  
**Acteurs** : RH, ADMIN (+ DIRECTION lecture Could)  
**Valeur** : piloter fraudes / absences sans paie.

#### User story
En tant que RRH, je veux consulter les pointages validés et les rejets hors zone, afin de détecter les tentatives de fraude et d’accompagner les sites.

#### Priorité : **Should** | **Prêt BA** : partiel (ups reporting)

---

### Story P8 — Détection mock location / integrité device (Could)

Hors Must ; dépend OS / stores. Ne bloque pas la recette P1–P5.

#### Priorité : **Could**

---

### Story P9 — Reconnaissance faciale on-device + consentement RGPD (Could / vague suivante)

Réactive C-M09-01b + C-L-03/04. Gate § 23 « consentement biométrique opérationnel ».

#### Priorité : **Could** (vague M09-b)

---

### Story P10 — QR TOTP dynamique ~5 min + HMAC + fallback Wi-Fi / manuel RO (Could / Cible CDC)

Alignement CDC complet. Incompatible avec le seul support papier 90 j sans borne online.

#### Priorité : **Could / Cible**

---

## 6. Hors Must explicite

| Sujet | Statut | Motif |
|---|---|---|
| Biométrie / reconnaissance **faciale** | Vague suivante (P9) | RGPD, consentement, complexité ; pas requis pour anti-domicile |
| **TOTP** QR ~5 min | Cible CDC (P10) | Exige borne online ; contredit QR papier 90 j |
| **HMAC** intégrité payload serveur | Could | Renfort crypto ; horodatage serveur suffit Must |
| Fallback **Wi-Fi BSSID** | Cible CDC | Complexité ; rejet GPS plus simple |
| **Pointage manuel RO** | Cible / Could | Contournement fraude si mal cadré |
| **Pause / déjeuner** | Could | Hors besoin ; risque confusion M01 |
| Calcul **heures / paie** | Hors périmètre (§ 21) | Pas de paie cette vague |
| Restriction sites par unité | Could | Itinérance AGUA : ouvert Must |
| Alternance stricte ENTREE/SORTIE | Should UX | Terrain chaotique |
| Offline pointage + synchro | Cible NFR | Fiabilité anti-fraude = online Must |
| Export DG / M12 présence | Could | Après P7 |

---

## 7. Impact `sirh_basefonctionnelle.md`

| Section | Modification attendue (quand stories acceptées / en même temps que le code) |
|---|---|
| § 3.2 carte M09 | Passer de « Partiel (UI mobile, pas de service présence) » → **Partiel** puis **Livré** sur le sous-périmètre QR+GPS (pas le CDC face complet) — formulation : *Livré (QR+géofence) / Cible (face, TOTP)* |
| § 14.1 Principe | Remplacer la séquence obligatoire QR TOTP + GPS + face par : **Must livré** = QR semi-statique 90 j + GPS ≤ 50 m ; **Cible** = face puis TOTP/HMAC |
| § 14.2 Données | Ajouter site_id, version QR, type ENTREE/SORTIE, distance_m, statut ; score facial / HMAC = Cible |
| § 14.3 Exceptions | Prioriser hors zone + QR expiré ; face 3 échecs / Wi-Fi / manuel RO = Cible |
| § 14.4 RGPD biométrie | Marquer **non applicable** tant que face non activée ; conserver pour vague P9 |
| § 14.5 État actuel | Mettre à jour au fil des livraisons (`svc-presence`, BO QR, mobile scan+GPS RH) |
| § 4.2 Matrice | Ligne « Admin sites / QR / GPS pointage » : RH/ADMIN écriture ; USER pointage self |
| § 19.1 C-M09-01 | Amender selon **D-P09** (C-M09-01 vague QR ; C-M09-01b face) |
| § 19.6 | Confirmer multi-sites + rayon 50 m Must |
| § 20 Parcours | Collaborateur : pointer via scan ; RH : générer QR, poser GPS mobile, renouveler à J-30 |
| § 22 Hyp. 6 | Remplacer : *le sous-périmètre QR+géofence est engagement de recette de cette vague ; TOTP/HMAC/face restent hors engagement* |
| § 23 | Cocher / reformuler : *Décider calendrier M09* → vague QR documentée ; garder *Consentement biométrique* ouvert jusqu’à P9 |

**Ce fichier PO ne substitue pas** la MAJ du doc fonctionnel : le BA / les seniors mettent à jour `sirh_basefonctionnelle.md` **avec** le code.

---

## 8. Handoff

| Vers | Contenu |
|---|---|
| **`sirh-architect`** | ADR : bounded context présence, gateway routes, modèle site/QR/version, calcul distance, pas de biométrie Must |
| **`sirh-business-analyst`** | Spec P1–P5 (règles, API, RBAC, AC) dès ADR posé |
| **`sirh-ui-ux`** | Parcours web (fiche site, téléchargement, bandeau expiration) + mobile (scan ENTREE/SORTIE, config GPS RH) |
| **Seniors** | Après BA ; dossiers disjoints `svc-presence` (ou nom ADR) ‖ `rh-admin-web` ‖ `rh_mobile_app` |

**Prêt pour `sirh-architect` + `sirh-business-analyst`** (P1–P5) · **`sirh-ui-ux`** pour nouveaux écrans · ups RH B1–B5 non bloquants pour démarrer ADR/BA avec défauts PO.

---

*Document de coordination PO — vague pointage QR. Ne contient pas d’ADR ni de code.*
