# Spec — Pointage QR + géofence (vague Must) — M09

| | |
|---|---|
| **Rôle** | Business Analyst SIRH |
| **Date** | 13 août 2026 |
| **Statut** | Spec développeur figée pour P1–P5 (+ P3 alerte J-30 Must) · P6 Should (rayon + J-7) esquissé |
| **Source PO** | [`docs/sirh_pointage_qr_vague.md`](sirh_pointage_qr_vague.md) — D-P01…D-P10, stories P1–P5 |
| **Source ADR** | [`docs/adr/ADR-M09-pointage-qr-gps.md`](adr/ADR-M09-pointage-qr-gps.md) — `svc-presence`, HMAC QR, Haversine |
| **Source métier** | [`docs/sirh_basefonctionnelle.md`](sirh_basefonctionnelle.md) § 14 M09, § 18 NOTIF, § 19 C-M09-01 / C-L-03/04 / C-S-\*, § 19.6 |
| **État actuel code** | Prototype UX mobile (scan + face) **sans** `svc-presence`, **sans** horodatage métier (§ 14.5) |
| **État cible vague** | **Partiel → Livré (sous-périmètre QR+géofence)** ; face / TOTP / HMAC intégrité pointage CDC restent **Cible** |
| **Interdit** | Code · commit · écraser vague PO / ADR · inventer un point § 23 |

**Acteurs & RBAC** : `USER` (pointage soi) · `RO`/`RESPONSABLE` (pointage soi si JWT multi-rôles ; pas d’admin sites) · `RH` \| `ADMIN` (écriture sites / QR / GPS) · `DIRECTION` (lecture sites / pointages org)

---

## 0. Alignement PO ↔ Architecte (tranches BA)

| Sujet | Position PO (vague) | Position ADR | **Décision BA (spec)** |
|---|---|---|---|
| HMAC | Header « pas de HMAC serveur en Must » = HMAC **intégrité payload pointage CDC / face** (Could P10) | HMAC-SHA256 sur le **jeton QR** (`PRESENCE_QR_HMAC_SECRET`) | **Retenir l’ADR** pour anti-forge QR. Écart noté : pas de HMAC d’intégrité du *pointage* (score facial, payload métier CDC) en Must. |
| Géofence | D-P02 : **50 m** exact, Haversine **serveur** | Idem ; défaut ADR = **50 m strict** (pas +5 m) | **50 m strict** serveur. `d ≤ 50` → OK ; `d > 50` → `REJETE_HORS_ZONE`. |
| Rotation QR | 90 j calendaires ; régénération invalide l’ancien | Révocation **immédiate** (défaut) | **90 j** ; régénération → ancienne version `REVOQUE` **immédiatement**. |
| Téléchargement | Bouton **toujours visible** (D-P04) | `GET …/qr/download` | Bouton toujours visible dès qu’un QR a été généré / est actif. |
| Alerte J-30 | Story **P3 Must** ; destinataires = tous les `RH` (B4) | Notif Kafka Should pour rejet hors zone ; alerte expiration non détaillée | **P3 Must** : notif J-30 à tous les `RH`. Relance **J-7** = Should (P6), hors Must. |
| Service | « présence ou module dédié » | **`svc-presence` uniquement** | **`svc-presence`** + `presence_db`. **Pas** d’extension `svc-referentiel-rh`. |
| Face / TOTP 5 min | Hors Must | Hors Must | **Hors spec Must** (voir § Hors spec). |
| C-M09-01 | Amendement D-P09 (QR+GPS sans face) | Écart CDC noté ; MAJ doc hors ADR | Spec Must = **C-M09-01 (vague QR)** ; Cible CDC = **C-M09-01b** (QR+GPS puis face). |

### Hypothèses BA (ups RH / questions PO non bloquantes)

Les ups atelier § 4 du doc PO ne bloquent pas les AC Must. Défauts retenus :

| ID | Hypothèse | Origine | Si RH tranche autrement → renvoyer PO |
|---|---|---|---|
| H-BA-01 | Catalogue sites : CRUD vide + seed démo 1–2 sites (pas de catalogue AGUA figé) | B1 | Liste nominative seed |
| H-BA-02 | Tout collaborateur `ACTIF` peut pointer sur **tout** site `ACTIF` (QR valide + ≤ 50 m) | B2 / D-P05 | Restriction par unité / affectation |
| H-BA-03 | Pas d’alternance stricte ENTREE↔SORTIE (suggestion UI only) | B3 / D-P06 | Blocage métier double ENTREE |
| H-BA-04 | Alerte J-30 → destinataire spécial **`RH`** (tous les comptes rôle RH, § 18) | B4 | Référent par site |
| H-BA-05 | Pointages `REJETE_*` conservés comme les `VALIDE` (audit) ; GPS scan **5 ans** (C-L-02) puis purge/agrégation ; métadonnées présence **10 ans** (C-L-01) | B5 / ADR | Durées différentes |
| H-BA-06 | Libellé UI : **« site de pointage »** (pas « borne » obligatoire) | Non bloquant PO | Renommer |
| H-BA-07 | Fichier téléchargé Must : **PNG** (QR seul) ; PDF A4 consignes = Should | Non bloquant PO | PDF obligatoire |
| H-BA-08 | `accuracyMeters` > **100 m** → `REJETE_PRECISION_GPS` (rejet, pas de VALIDE) | ADR laisse au BA | Seuil autre / ignorer accuracy |
| H-BA-09 | Site créable **sans** lat/lon ; pointage impossible tant qu’emplacement non posé | ADR `latitude NULL` | GPS obligatoire à la création |

**Pas d’invention § 23** : calendrier M09 / consentement biométrique restent ouverts côté PO pour la vague face (P9).

---

## 1. Règles métier

| ID | Règle | Erreur métier / statut |
|----|-------|------------------------|
| **R-P01** | Un site de pointage a un `code` unique, un `libelle`, un flag `actif`, un `rayon_metres` = **50** (Must fixe), lat/lon optionnels jusqu’à pose GPS. | Création invalide → **422** ; `code` dupliqué → **409** |
| **R-P02** | Seuls `RH` \| `ADMIN` créent / modifient / désactivent un site, génèrent / révoquent un QR, posent le GPS. `DIRECTION` = lecture seule. `USER` / `RO` = écriture **403**. | **403** |
| **R-P03** | Un seul credential QR **`ACTIF`** par site à un instant T. | — |
| **R-P04** | Génération / régénération : `valid_until` = `valid_from` + **90 jours** calendaires (UTC). Payload QR opaque signé **HMAC-SHA256** (`p1.<payload>.<sig>`). Aucune lat/lon dans le QR. | Secret manquant / erreur crypto → **500** (ops) |
| **R-P05** | Régénération ou `revoke-qr` → credential précédent passe à **`REVOQUE` immédiatement** (pas de grâce 24 h en Must). | Ancien jeton au scan → `REJETE_QR_REVOQUE` |
| **R-P06** | QR dont `valid_until` &lt; now serveur → statut credential **`EXPIRE`** (job ou évaluation lazy) ; scan → `REJETE_QR_EXPIRE`. | `REJETE_QR_EXPIRE` |
| **R-P07** | Signature invalide / payload altéré / schéma inconnu → `REJETE_QR_INVALIDE` **avant** toute géofence. | `REJETE_QR_INVALIDE` |
| **R-P08** | Site `actif = false` → aucun pointage `VALIDE` ; scan → `REJETE_SITE_INACTIF`. | `REJETE_SITE_INACTIF` |
| **R-P09** | Site sans lat/lon → pointage impossible → `REJETE_SITE_INACTIF` ou motif dédié **`REJETE_EMPLACEMENT_ABSENT`** (retenir ce code pour clarté AC). | `REJETE_EMPLACEMENT_ABSENT` |
| **R-P10** | Distance Haversine serveur (WGS84, R ≈ 6 371 000 m) entre GPS client et emplacement site. `d ≤ 50` → OK géofence ; `d > 50` → `REJETE_HORS_ZONE`. Source de vérité = serveur ; **ignorer** tout booléen client `inGeofence`. | `REJETE_HORS_ZONE` |
| **R-P11** | GPS client absent / refusé / non fourni → rejet (pas de fallback Wi-Fi / manuel RO en Must). | **422** corps + message ; si body partiel côté API → pas de `VALIDE` |
| **R-P12** | Si `accuracyMeters` fourni et **> 100** → `REJETE_PRECISION_GPS` (H-BA-08). Si absent → ne pas bloquer sur ce critère seul. | `REJETE_PRECISION_GPS` |
| **R-P13** | Types de pointage : `ENTREE` \| `SORTIE` (choix utilisateur obligatoire). Pas de calcul d’heures / paie. | Type inconnu → **422** |
| **R-P14** | Alternance ENTREE/SORTIE **non bloquante** en Must (double ENTREE autorisé). UI peut suggérer le type opposé au dernier `VALIDE`. | — |
| **R-P15** | Horodatage de vérité = `server_ts` (Instant UTC). `clientTimestamp` informatif seulement. Affichage UI = `Africa/Tunis`. | — |
| **R-P16** | Identité pointage = `collaborateur_id` dérivé du JWT (pas d’IDOR). Un `USER` ne crée / ne lit que **ses** pointages. | **403** si tentative autre matricule |
| **R-P17** | Tout essai (VALIDE ou REJETE_*) est **persisté** (audit anti-fraude) avec lat/lon réels, `distance_metres` si calculable, `motif_rejet` si rejet. | — |
| **R-P18** | Idempotence : si `idempotencyKey` fournie, `UNIQUE(collaborateur_id, idempotency_key)` → rejouer la **même** réponse (C-S-05). | Doublon → **200** même corps (pas second VALIDE) |
| **R-P19** | Bouton « Télécharger code QR » **toujours visible** sur fiche site dès qu’un QR actif existe (ou après 1ʳᵉ génération encore téléchargeable selon credential actif). **Pas** de masquage hors J-30. | Pas de QR → bouton désactivé / CTA « Générer » |
| **R-P20** | À J-30 avant `valid_until` (fenêtre calendaire UTC, 1 notif / credential / site), publier NOTIF destinataire `RH` (tous les RH). Badge / bandeau fiche site « expire le … ». | — |
| **R-P21** | Gateway unique (C-S-01) : clients → `/api/rh/v1/.../presence/**` uniquement. | — |
| **R-P22** | Aucune photo / biométrie / score facial en Must (C-L-04 non déclenché pour traitement face). Minimisation GPS (C-L-03) selon rétention H-BA-05. | — |
| **R-P23** | Pointage `VALIDE` Must = QR actif valide (sig + version + non expiré) **et** site actif **et** emplacement posé **et** GPS ≤ 50 m **et** précision OK. **Pas** d’étape face. | Amendement opérationnel C-M09-01 vague QR (D-P09) |

---

## 2. Cycle de vie

### 2.1 Site (`presence_site`)

```text
(création) → ACTIF (actif=true)
                │
                ├─ PUT emplacement (lat/lon)     [RH|ADMIN]
                ├─ désactivation (actif=false)   [RH|ADMIN]
                └─ réactivation (actif=true)     [RH|ADMIN]
```

- Suppression physique : **hors Must** (soft via `actif=false`).
- Site inactif : credentials QR peuvent rester `ACTIF` en base mais **R-P08** bloque le pointage.

### 2.2 Credential QR (`presence_qr_credential`)

```text
GENERATE → ACTIF (valid_from … valid_until = +90 j)
              │
              ├─ REGÉNÉRATION / REVOKE → REVOQUE (immédiat)
              └─ now > valid_until     → EXPIRE
```

- Transitions : `ACTIF` → `REVOQUE` \| `EXPIRE` uniquement (pas de retour arrière).
- Nouveau generate sur site : nouvelle `qr_version` monotone ; unique `(site_id, qr_version)`.

### 2.3 Pointage (`presence_pointage`)

Pas de machine à états post-création : enregistrement **immuable** `VALIDE` ou `REJETE_*`.

Ordre de validation serveur (court-circuit) :

1. Auth JWT + extraction `collaborateur_id`
2. Body valide (type, lat/lon, qrToken) sinon **422**
3. Vérif HMAC / parse → sinon persister `REJETE_QR_INVALIDE`
4. Credential actif pour site/version/jti → sinon `REJETE_QR_REVOQUE` / `REJETE_QR_EXPIRE`
5. Site actif → sinon `REJETE_SITE_INACTIF`
6. Emplacement posé → sinon `REJETE_EMPLACEMENT_ABSENT`
7. Précision GPS (R-P12) → sinon `REJETE_PRECISION_GPS`
8. Haversine ≤ 50 → sinon `REJETE_HORS_ZONE`
9. Sinon `VALIDE` + `server_ts`

---

## 3. Données

### 3.1 Site

| Champ | Obligatoire création | Format / notes |
|---|---|---|
| `code` | oui | VARCHAR unique, court métier |
| `libelle` | oui | VARCHAR |
| `latitude` / `longitude` | non (H-BA-09) | WGS84 ; posés via mobile P5 ou Should web |
| `rayon_metres` | non (défaut 50) | Must = **50** figé ; configurable = P6 Should |
| `actif` | non (défaut true) | boolean |

### 3.2 Credential QR

| Champ | Notes |
|---|---|
| `qr_version`, `jti` | Monotone / UUID unique |
| `valid_from`, `valid_until` | TIMESTAMPTZ UTC ; durée **90 j** |
| `statut` | `ACTIF` \| `REVOQUE` \| `EXPIRE` |
| Contenu QR | Opaque `p1.<payload>.<sig>` ; claims : siteId, qrVersion, exp, iat, jti |

### 3.3 Pointage (écriture mobile)

| Champ | Obligatoire | Notes |
|---|---|---|
| `qrToken` | oui | Jeton scanné |
| `type` | oui | `ENTREE` \| `SORTIE` |
| `latitude`, `longitude` | oui | Mesure appareil |
| `accuracyMeters` | non | Déclenche R-P12 si &gt; 100 |
| `clientTimestamp` | non | Informatif ISO-8601 |
| `deviceId` | non | Trace anti-fraude |
| `idempotencyKey` | fortement recommandé | UUID client ; C-S-05 |

### 3.4 Pointage (persistance)

`collaborateur_id`, `site_id`, `qr_credential_id`, `type`, `statut`, `server_ts`, `client_ts`, GPS, `accuracy_metres`, `distance_metres`, `device_id`, `idempotency_key`, `motif_rejet`.

**Formats** : dates/heures API en ISO-8601 UTC ; UI `Africa/Tunis`.

---

## 4. API (contrat via gateway)

Base : predicates **avant** catch-all référentiel (ADR) :

`/api/rh/v1/presence/**`, `/api/rh/v1/admin/presence/**`, `/api/rh/v1/mobile/presence/**` → `lb://svc-presence`

### 4.1 Admin / RH (web + mobile RH)

| Méthode | Path | Rôles | Body / query | Succès | 4xx métier |
|---|---|---|---|---|---|
| `GET` | `/api/rh/v1/admin/presence/sites` | RH, ADMIN, DIRECTION | pagination 20–50 | **200** liste | **401** |
| `POST` | `/api/rh/v1/admin/presence/sites` | RH, ADMIN | code, libelle, lat/lon opt. | **201** | **403**, **409** code, **422** |
| `GET` | `/api/rh/v1/admin/presence/sites/{siteId}` | RH, ADMIN, DIRECTION | — | **200** | **404** |
| `PATCH` | `/api/rh/v1/admin/presence/sites/{siteId}` | RH, ADMIN | libelle, actif | **200** | **403**, **404**, **422** |
| `PUT` | `/api/rh/v1/admin/presence/sites/{siteId}/emplacement` | RH, ADMIN | `{ "latitude", "longitude" }` | **200** | **403**, **404**, **422** (bornes lat/lon) |
| `POST` | `/api/rh/v1/admin/presence/sites/{siteId}/qr/generate` | RH, ADMIN | — | **200**/ **201** + métadonnées (`validUntil`, `qrVersion`) + data image ou lien | **403**, **404**, **409** si site inactif (refus generate) |
| `GET` | `/api/rh/v1/admin/presence/sites/{siteId}/qr/download` | RH, ADMIN | — | **200** `image/png` (H-BA-07) | **403**, **404** (pas de QR actif) |
| `POST` | `/api/rh/v1/admin/presence/sites/{siteId}/revoke-qr` | RH, ADMIN | — | **204**/ **200** | **403**, **404**, **409** si aucun ACTIF |
| `GET` | `/api/rh/v1/admin/presence/pointages` | RH, ADMIN, DIRECTION | filtres site/statut/dates, page | **200** | **403**, **401** |

> Consultation org (`GET …/pointages`) = utile dès Must pour support / recette ; reporting riche = story P7 Should.

### 4.2 Mobile collaborateur

| Méthode | Path | Rôles | Body | Succès | 4xx |
|---|---|---|---|---|---|
| `POST` | `/api/rh/v1/mobile/presence/pointages` | USER (+ RO si multi-rôles) | voir § 3.3 | **200** `{ statut, serverTs, siteId, distanceMetres?, motifRejet? }` — **y compris** pour rejets métier persistés | **401**, **403** IDOR, **422** body invalide / GPS manquant |
| `GET` | `/api/rh/v1/mobile/presence/pointages/me` | USER | pagination | **200** historique soi | **401**, **403** |

**Convention rejets métier pointage** : HTTP **200** avec `statut=REJETE_*` (essai tracé), **sauf** erreurs techniques / auth / validation structurelle (**401/403/422**). Ne pas utiliser 409 pour hors zone.

**Exemple body pointage** (ADR) :

```json
{
  "qrToken": "p1.eyJ…",
  "type": "ENTREE",
  "latitude": 36.8065,
  "longitude": 10.1815,
  "accuracyMeters": 12.0,
  "clientTimestamp": "2026-08-13T10:15:00Z",
  "deviceId": "optional-stable-id",
  "idempotencyKey": "uuid-client"
}
```

**Statuts stables** : `VALIDE` \| `REJETE_QR_INVALIDE` \| `REJETE_QR_EXPIRE` \| `REJETE_QR_REVOQUE` \| `REJETE_HORS_ZONE` \| `REJETE_SITE_INACTIF` \| `REJETE_EMPLACEMENT_ABSENT` \| `REJETE_PRECISION_GPS`

---

## 5. Notifications

| Événement | Priorité | Destinataires | Canal | Payload min. |
|---|---|---|---|---|
| `PRESENCE_QR_EXPIRE_J30` | **Must (P3)** | Destinataire spécial `RH` (tous comptes RH, § 18) | Kafka `rh.notifications` → cloche BO / mobile RH | `eventId`, `siteId`, `libelle`, `validUntil`, `joursRestants` |
| Badge fiche site | **Must (P3)** | UI web RH | Affichage local API site (`validUntil`, flag `expireBientot`) | — |
| `PRESENCE_QR_EXPIRE_J7` | Should (P6) | idem RH | idem | idem |
| `PRESENCE_REJET_HORS_ZONE` | Should (ADR) | RO unité (résolution référentiel) — **hors Must P1–P5** | `rh.notifications` | pointageId, siteId, distanceMetres |

Idempotence Kafka : `eventId` UUID (C-S-05). Une seule notif J-30 par couple `(siteId, qr_version)` (pas de spam quotidien).

---

## 6. RBAC (matrice)

| Action | USER | RO | RH / ADMIN | DIRECTION |
|---|---|---|---|---|
| Pointer (soi, mobile) | oui | oui* | non via API admin ; oui si parcours mobile USER | non |
| Historique soi | oui | oui | — | — |
| CRUD sites / QR / revoke | non | non | oui | non |
| Poser GPS (PUT emplacement) | non | non | oui | non |
| Télécharger QR | non | non | oui | non |
| Liste sites / pointages org | non | non Must | oui | lecture |
| Recevoir alerte J-30 | non | non | oui (RH) | non Must |

\* JWT agent de terrain. Pas d’IDOR. Compte BO seul (`RH` sans matricule mobile) ne pointe pas via admin.

---

## 7. Canaux

### 7.1 Web — Plateforme RH (`rh-admin-web`)

| Écran | Stories | États UI |
|---|---|---|
| Liste / fiche **sites de pointage** | P1, P2, P3 | loading / vide / erreur / 403 lecture seule DIRECTION |
| Actions : créer site, activer/désactiver, **Générer QR**, **Télécharger code QR** (toujours visible si QR), revoke | P2 | confirmation régénération (invalide ancien) |
| Bandeau / badge « expire le … » / J-30 | P3 | — |
| Consultation pointages (liste basique) | support Must / amorce P7 | filtres + pagination |
| Édition lat/lon clavier | Should (D-P07) | hors Must |

### 7.2 Mobile — RH Connect (`rh_mobile_app`)

| Écran | Stories | États |
|---|---|---|
| Scan QR → choix ENTREE/SORTIE → envoi GPS → feedback VALIDE / rejet | P4 | loading ; GPS refusé ; camera ; erreur réseau ; message motif serveur |
| Historique personnel pointages | P4 | vide / liste |
| RH/ADMIN : liste sites → « Utiliser ma position » → confirmer | P5 | 403 si USER ; GPS refusé ; succès MAJ |
| **Retirer / masquer** étape face du parcours Must (feature flag off) | P4 | aligné R-P22 |

---

## 8. AC testables (Given / When / Then)

### P1 — Site + GPS référentiel présence

1. **Given** un RH authentifié, **When** il `POST` un site avec `code` + `libelle` valides, **Then** **201**, site `actif=true`, `rayon_metres=50`.
2. **Given** un `USER` sans RH/ADMIN, **When** il appelle une API d’écriture sites, **Then** **403**.
3. **Given** un `code` déjà existant, **When** création, **Then** **409**.
4. **Given** un site sans lat/lon, **When** un collaborateur tente un pointage contre un QR de ce site, **Then** `REJETE_EMPLACEMENT_ABSENT` (pas de `VALIDE`).

### P2 — QR 90 j + téléchargement

5. **Given** un site ACTIF sans QR, **When** RH `POST …/qr/generate` puis `GET …/qr/download`, **Then** PNG obtenu et `validUntil ≈ now+90j` (UTC).
6. **Given** un QR ACTIF, **When** RH régénère, **Then** l’ancien jeton produit `REJETE_QR_REVOQUE` même dans le rayon ; le nouveau jeton est seul `ACTIF`.
7. **Given** la fiche site avec QR actif, **When** l’UI charge, **Then** le bouton « Télécharger code QR » est visible **sans** condition de date.
8. **Given** un jeton dont la signature HMAC est altérée, **When** pointage, **Then** `REJETE_QR_INVALIDE`.

### P3 — Alerte J-30 (Must)

9. **Given** un QR ACTIF avec `valid_until` = aujourd’hui UTC + 30 j, **When** le job / règle d’alerte s’exécute, **Then** chaque compte rôle `RH` reçoit une notification `PRESENCE_QR_EXPIRE_J30` (idempotente par version).
10. **Given** un QR expiré non renouvelé, **When** un collaborateur scanne dans le rayon, **Then** `REJETE_QR_EXPIRE` + message clair.
11. **Given** la fiche site à J-30, **When** RH ouvre la fiche, **Then** bandeau / badge d’expiration visible ; bouton téléchargement **toujours** présent.

### P4 — Pointage mobile + géofence 50 m

12. **Given** collaborateur ≤ 50,0 m, QR ACTIF, site ACTIF, GPS fourni, **When** `POST …/pointages` type ENTREE, **Then** `VALIDE` + `server_ts` UTC serveur.
13. **Given** distance serveur **50,1 m**, **When** scan, **Then** `REJETE_HORS_ZONE` (pas de `VALIDE`) + `distanceMetres` persistée.
14. **Given** QR révoqué ou expiré, **When** scan même ≤ 50 m, **Then** rejet QR correspondant (pas de contournement GPS).
15. **Given** GPS refusé / absent du body, **When** tentative, **Then** **422** ou rejet explicite — **aucun** silent success.
16. **Given** même `idempotencyKey` rejouée, **When** second `POST`, **Then** même réponse, un seul enregistrement logique VALIDE.
17. **Given** un USER A, **When** il tente de lire l’historique de B, **Then** **403** / données uniquement soi.

### P5 — Pose GPS mobile RH/ADMIN

18. **Given** un RH sur site, **When** il `PUT …/emplacement` avec sa position, **Then** lat/lon du site mises à jour (+ audit who/when).
19. **Given** un USER seul, **When** il ouvre / appelle config GPS site, **Then** accès refusé (**403**).
20. **Given** GPS site mis à jour, **When** un scan ultérieur, **Then** Haversine utilise les **nouvelles** coordonnées.

### Non-régression architecture

21. **Given** clients web/mobile, **When** appels présence, **Then** uniquement via gateway `:8080` (C-S-01) — aucun port `svc-presence` exposé.
22. **Given** parcours Must, **When** pointage réussi, **Then** aucune photo / biométrie envoyée au serveur (C-L-04).

### P6 — Should (hors engagement Must, AC indicatifs)

23. **Given** rayon site configuré 20–100 (P6), **When** Haversine, **Then** seuil = rayon site (pas 50 figé).
24. **Given** J-7 avant expiration, **When** job, **Then** 2ᵉ notif RH.

---

## 9. Non-régression (contraintes)

| ID | À ne pas casser / à respecter |
|---|---|
| **C-M09-01** | Doc CDC encore « QR+GPS puis face » : au go-dev, porter **C-M09-01 (vague QR)** + **C-M09-01b** Cible face dans `sirh_basefonctionnelle.md` **avec** le code (PO D-P09). Recette Must = QR+GPS uniquement. |
| **C-L-01** | Métadonnées présence / pièces : rétention **10 ans**. |
| **C-L-02** | Journal / GPS scan : **5 ans** puis purge ou agrégation. |
| **C-L-03** | Minimisation GPS ; pas de finalités hors contrôle présence. Consentement biométrique **non requis** tant que face absente. |
| **C-L-04** | Pas de photo serveur ; face hors Must ⇒ contrainte non déclenchée pour biométrie. |
| **C-S-01** | Gateway unique. |
| **C-S-02** | JWT + RBAC ; BO ≠ USER seul en écriture admin. |
| **C-S-03** | Sessions API stateless. |
| **C-S-05** | Idempotence pointage (`idempotencyKey`) + `eventId` notifs. |
| **C-M01-01** | Pointage ≠ autorisation de sortie ; ne pas mélanger pause/M01. |
| **§ 19.6** | Multi-sites : N sites, chacun GPS + QR + rayon 50 m Must. |

---

## 10. Hors spec (Must)

| Sujet | Renvoi |
|---|---|
| Reconnaissance faciale / liveness / consentement RGPD | P9 / C-M09-01b / § 23 PO |
| QR TOTP ~5 min | P10 / Cible CDC |
| HMAC intégrité **pointage** CDC (payload face) | Could P10 — **distinct** du HMAC jeton QR (Must ADR) |
| Fallback Wi-Fi BSSID / pointage manuel RO | Cible / Should ultérieur |
| Pause / déjeuner | Could |
| Calcul heures / paie / export DG | § 21 hors périmètre |
| Restriction sites par unité | Could |
| Alternance stricte ENTREE/SORTIE | Should UX |
| Offline pointage + synchro | Cible NFR |
| Rayon configurable 20–100 + alerte J-7 | **P6 Should** |
| Alerte RO systématique hors zone | Should ADR |
| Mock location / Play Integrity | P8 Could |
| Édition lat/lon clavier web | Should D-P07 |
| PDF A4 avec consignes | Should H-BA-07 |
| Grâce 24 h post-rotation QR | Rejeté Must (révocation immédiate) |
| Tolérance Haversine +5 m | Rejeté (50 m strict) |
| Extension `svc-referentiel-rh` | Rejeté ADR |

---

## 11. Impact doc fonctionnel (avec le code, pas dans cette spec seule)

Portage attendu dans `sirh_basefonctionnelle.md` (PO § 7) : § 3.2 M09, § 14.1–14.5, § 4.2 matrice, C-M09-01 / C-M09-01b, § 19.6, § 20 parcours, § 22 hyp. 6, § 23 calendrier M09 (cocher vague QR documentée ; garder consentement biométrie ouvert).

**Ce fichier BA ne substitue pas** la MAJ du doc fonctionnel.

---

## 12. Handoff

| Rôle | Contenu |
|---|---|
| **Architecte** | ADR déjà posé (`svc-presence`) — valider statut ADR → accepté ; port Eureka / DB au bootstrap |
| **`sirh-senior-backend`** | Bootstrap `svc-presence` + Flyway + gateway route + APIs admin/mobile + HMAC QR + Haversine 50 m strict + job notif J-30 + tests (frontière 49.9/50.1, sig, RBAC, idempotence) |
| **`sirh-senior-frontend` (web)** | Écrans sites, générer / télécharger QR, bandeau expiration, liste pointages basique |
| **`sirh-senior-frontend` (mobile)** | Scan + GPS + ENTREE/SORTIE ; masquer face Must ; écran pose GPS RH/ADMIN ; historique me |
| **`sirh-ui-ux`** | Parcours web fiche site + mobile scan / rejets / pose GPS ; a11y messages rejet ; pas de clutter J-30 vs bouton download |

### Prêt pour développement

- **Prêt `sirh-senior-backend`** (P1–P5 + notif P3) après acceptation ADR.
- **Prêt `sirh-senior-frontend`** web ‖ mobile en parallèle (**dossiers disjoints**).
- **Prêt `sirh-ui-ux`** pour nouveaux écrans.
- Ups RH (atelier § 4 PO) : non bloquants ; écarts → **renvoyer au PO** (ne pas élargir le Must ici).

---

*Spec BA M09 vague QR+géofence — ne contient pas de code. Citations : vague PO D-P\* / P1–P5 ; ADR-M09.*
