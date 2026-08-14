# ADR-M09 — Pointage QR renouvelable + géofence GPS (vague Must)

| | |
|---|---|
| **ID** | ADR-M09 |
| **Statut** | proposé |
| **Date** | 2026-08-13 |
| **Module** | M09 — Pointage digital (**Partiel** : UI mobile prototype, pas de service présence) |
| **Auteur** | Architecte SIRH |
| **Références** | `docs/sirh_basefonctionnelle.md` § 3, 14, 19.3–19.5, 21–23 ; `docs/architecture/02-microservices-et-responsabilites.md` |

---

## Contexte

Le CDC (§ 14) vise un pointage anti-fraude **QR dynamique + GPS + reconnaissance faciale on-device**. Dans le monorepo actuel :

- aucun `svc-presence` ;
- écran mobile = prototype UX (scan QR + caméra) **sans** enregistrement métier serveur ;
- hypothèse § 22.6 : anti-fraude TOTP / HMAC / liveness **non engageable en recette** tant que le service n’existe pas ;
- dette § 23 : calendrier M09 (prototype vs recette) et consentement biométrique avant go-live face.

**Besoin PO (vague Must)** — livrer rapidement, 0 risque inutile, fiable :

1. QR **renouvelable ~3 mois**, généré / téléchargé **web** RH \| ADMIN.
2. **Emplacement GPS** du QR enregistré / modifié par RH \| ADMIN **mobile**.
3. Pointage collaborateur : scan QR + position GPS **≤ 50 m** de l’emplacement enregistré.
4. Anti-fraude photocopie hors site → **rejet** (géofence serveur).
5. Face / TOTP court = **Could / Should** selon risque — **pas Must** de cette vague (C-L-04 : si face plus tard, on-device only).

**Contraintes monorepo non négociables** : gateway unique (C-S-01), Eureka, Kafka, JWT resource server, **un service = une base**, pas de JOIN inter-services, pas de multi-tenant.

---

## Décision

### 1. Service : `svc-presence` (nouveau) — **tranché**

| Option | Rapidité | Fiabilité / risque | Verdict |
|---|---|---|---|
| **A. Nouveau `svc-presence`** | +1 sprint bootstrap (jar, Eureka, Flyway, route gateway) | Isolation secrets QR, rétention GPS, charge d’écriture pointages, frontière M09 claire ; aligné architecture § présence | **Retenu** |
| **B. Étendre `svc-referentiel-rh`** | Plus rapide J+0 | Contamine le BC référentiel (déjà M00–M05) ; rotation secrets QR colocalisée avec fiches RH ; scaling / RGPD GPS plus difficiles ; contredit la carte cible (`svc-presence` isolé) | **Rejeté** |

**Pourquoi A malgré le coût bootstrap** : le skill Architecte et `docs/architecture/02-…` classent M09 comme **service dédié** ; le gain « tout dans référentiel » n’est pas 0 risque (régression métier RH livré + dette d’extraction). Bootstrap standard Spring Boot 3 + PostgreSQL + JWT (copie du squelette `svc-evaluation`) reste **prévisible** et ne bloque pas le Must QR+GPS.

**Pas d’extraction** de `svc-demandes` / `svc-plainte` pour « beauté CDC ».

### 2. Périmètre Must vs différé

| Capacité | Priorité vague | Note |
|---|---|---|
| Site / emplacement GPS + rayon 50 m (défaut) | **Must** | Haversine **serveur** |
| QR signé, validité ~90 j, téléchargement PNG/PDF web | **Must** | Rotation RH/ADMIN |
| Pointage `ENTREE` / `SORTIE` + statuts `VALIDE` / `REJETE_*` | **Must** | Idempotence client |
| Alerte RO sur rejet géofence (Kafka → NOTIF) | **Should** | Utile anti-fraude ops |
| TOTP ~5 min (§ 14.1 CDC) | **Could** | Ops d’impression incompatible avec QR ~3 mois |
| Reconnaissance faciale + consentement | **Could** (vague ultérieure) | C-L-04 ; débloque C-M09-01 CDC complet |
| Fallback Wi-Fi BSSID / pointage manuel RO | **Should** ultérieur | Hors Must |

**Alignement produit** : pour cette vague, la règle opérationnelle est **QR + GPS ≤ 50 m** (pas de face). La contrainte documentaire C-M09-01 (« QR+GPS puis face ») reste la **cible CDC** ; le BA / PO doivent noter l’écart *vague Must* dans `sirh_basefonctionnelle.md` au moment du go-dev (hors scope de cet ADR, fichier PO non modifié ici).

### 3. Sécurité QR (anti-forge, anti-photocopie hors site)

**Problème** : un QR statique « `siteId=42` » est forgeable et photocopiable. La photocopie **hors site** est déjà battue par la géofence ; la forge **sans connaître le secret** doit être impossible.

**Mécanisme retenu — jeton signé longue durée (pas TOTP 5 min)** :

1. À la génération, le serveur crée une **version** de credential pour le site :
   - `siteId` (UUID),
   - `qrVersion` (entier monotone),
   - `exp` (UTC, ≈ now + 90 j),
   - `iat`,
   - `nonce` / `jti` (UUID unique de génération — révoque la série précédente si rotation).
2. Payload compact (ex. Base64URL JSON ou claims JWT) + signature **HMAC-SHA256** avec clé serveur `PRESENCE_QR_HMAC_SECRET` (env / secret manager, **jamais** en git).
3. Contenu QR = **opaque** : `p1.<payload>.<sig>` (préfixe version de schéma). Aucune lat/lon dans le QR.
4. Validation au pointage (serveur) :
   - signature OK ;
   - `exp` non dépassé ;
   - `qrVersion` / `jti` **actif** pour ce `siteId` (table credentials) ;
   - site `ACTIF` ;
   - puis géofence Haversine.
5. **Rotation ~3 mois** : RH/ADMIN régénère → nouvelle `qrVersion`, ancienne **révoquée** immédiatement (ou grâce ≤ 24 h configurable si digicode physique déjà imprimé — défaut proposé : **révocation immédiate** pour 0 ambiguïté anti-fraude).
6. Le même QR de site est **partagé** (N collaborateurs) : **pas** de one-time nonce par scan (sinon file d’attente bornes). L’anti-rejeu inter-personne hors site = GPS ; l’anti-rejeu horloge = horodatage serveur + règles métier entrée/sortie (hors détail BA).

**Ce que le client envoie** : jeton QR scanné + `lat`/`lon` mesurés + `type` + `deviceId` optionnel + `clientTs` informatif. **Jamais** un booléen `inGeofence=true` comme preuve.

### 4. Calcul distance (Haversine serveur)

Formule WGS84 classique (rayon Terre ≈ 6 371 000 m) :

\[
d = 2 R \arcsin\sqrt{\sin^2\frac{\Delta\phi}{2} + \cos\phi_1\cos\phi_2\sin^2\frac{\Delta\lambda}{2}}
\]

- Entrées : `(lat_ref, lon_ref)` emplacement site ; `(lat, lon)` client.
- Décision : `d ≤ rayonMetres` (défaut **50**, configurable **par site**, borne technique 10–500 m).
- Si hors zone → `REJETE_HORS_ZONE`, persister lat/lon réels + `distanceMetres`, **ne pas** faire confiance au client.
- Précision GPS : documenter une tolérance produit éventuelle (ex. +5 m) **côté serveur uniquement** si le BA le tranche ; défaut ADR = **50 m strict**.

### 5. Intégrations

| Flux | Mécanisme |
|---|---|
| Identité collaborateur | JWT (`sub` / matricule / `collaborateurId` selon claim existant) — pas de FK cross-DB |
| Enrichissement fiche (optionnel) | Appel S2S authentifié vers `svc-referentiel-rh` **ou** snapshot minimal au pointage ; **pas** de JOIN SQL |
| Notifications rejet / alerte RO | Kafka `rh.notifications` (C-S-05 idempotence : `eventId`) |
| Domaine présence (audit async) | Topic dédié `rh.presence.events` (optionnel vague 1 ; sinon logs + table suffit) |

---

## Conséquences

### Routes gateway (predicates **avant** le catch-all `/api/rh/v1/**`)

Alignement sur le pattern `svc-evaluation` :

```yaml
- id: svc-presence
  uri: lb://svc-presence
  predicates:
    - Path=/api/rh/v1/presence/**,/api/rh/v1/admin/presence/**,/api/rh/v1/mobile/presence/**
```

Port service : à attribuer hors collision (ex. 8086 — à confirmer au bootstrap). Eureka name : `svc-presence`. Base : PostgreSQL dédiée `presence_db`.

### Contrats API (esquisse)

#### Admin / RH — web & mobile RH

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| `GET` | `/api/rh/v1/admin/presence/sites` | `RH`, `ADMIN`, `DIRECTION` (lecture) | Liste sites / emplacements |
| `POST` | `/api/rh/v1/admin/presence/sites` | `RH`, `ADMIN` | Créer site (libellé, rayon défaut 50) |
| `PUT` | `/api/rh/v1/admin/presence/sites/{siteId}/emplacement` | `RH`, `ADMIN` | Fixer / modifier `lat`, `lon` (usage mobile RH sur site) |
| `POST` | `/api/rh/v1/admin/presence/sites/{siteId}/qr/generate` | `RH`, `ADMIN` | Rotation credential + retour payload + URL/fichier image |
| `GET` | `/api/rh/v1/admin/presence/sites/{siteId}/qr/download` | `RH`, `ADMIN` | Téléchargement PNG/PDF du QR **actif** |
| `GET` | `/api/rh/v1/admin/presence/pointages` | `RH`, `ADMIN`, `DIRECTION` | Consultation / filtres (pagination 20–50) |
| `POST` | `/api/rh/v1/admin/presence/sites/{siteId}/revoke-qr` | `RH`, `ADMIN` | Révocation anticipée |

#### Mobile collaborateur

| Méthode | Path | Rôles | Description |
|---|---|---|---|
| `POST` | `/api/rh/v1/mobile/presence/pointages` | `USER` (+ RO/RESPONSABLE si JWT multi-rôles) | Scan + GPS → validation serveur |
| `GET` | `/api/rh/v1/mobile/presence/pointages/me` | `USER` | Historique personnel |

**Body pointage (exemple)** :

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

**Réponses métier stables** : `VALIDE` \| `REJETE_QR_INVALIDE` \| `REJETE_QR_EXPIRE` \| `REJETE_QR_REVOQUE` \| `REJETE_HORS_ZONE` \| `REJETE_SITE_INACTIF` \| `REJETE_PRECISION_GPS` (si `accuracyMeters` >> rayon — règle BA).

### Modèle de données minimal (`presence_db`)

```text
presence_site
  id UUID PK
  code VARCHAR UNIQUE          -- métier court
  libelle VARCHAR NOT NULL
  latitude DOUBLE PRECISION NULL   -- null = emplacement pas encore posé → pointage impossible
  longitude DOUBLE PRECISION NULL
  rayon_metres INT NOT NULL DEFAULT 50
  actif BOOLEAN NOT NULL DEFAULT TRUE
  created_at / updated_at / updated_by

presence_qr_credential
  id UUID PK
  site_id UUID FK → presence_site
  qr_version INT NOT NULL
  jti UUID NOT NULL UNIQUE
  valid_from TIMESTAMPTZ NOT NULL
  valid_until TIMESTAMPTZ NOT NULL
  statut VARCHAR NOT NULL      -- ACTIF | REVOQUE | EXPIRE
  created_by VARCHAR
  UNIQUE(site_id, qr_version)

presence_pointage
  id UUID PK
  collaborateur_id VARCHAR NOT NULL   -- id métier (pas FK cross-DB)
  site_id UUID NOT NULL
  qr_credential_id UUID NULL
  type VARCHAR NOT NULL               -- ENTREE | SORTIE
  statut VARCHAR NOT NULL             -- VALIDE | REJETE_*
  server_ts TIMESTAMPTZ NOT NULL      -- horodatage de vérité
  client_ts TIMESTAMPTZ NULL
  latitude DOUBLE PRECISION NOT NULL
  longitude DOUBLE PRECISION NOT NULL
  accuracy_metres DOUBLE PRECISION NULL
  distance_metres DOUBLE PRECISION NULL
  device_id VARCHAR NULL
  idempotency_key VARCHAR NULL
  motif_rejet VARCHAR NULL
  UNIQUE(collaborateur_id, idempotency_key)  -- si key fournie
```

Index : `(site_id, server_ts DESC)`, `(collaborateur_id, server_ts DESC)`, `(statut, server_ts)`.

**Fichiers QR** : génération à la volée ou cache court ; pas de GED S3 obligatoire en Must (métadonnées + secret en DB suffisent).

### RBAC

| Action | `USER` | `RO` | `RH` / `ADMIN` | `DIRECTION` |
|---|---|---|---|---|
| Pointer (soi) | oui | oui* | non** | non |
| Historique soi | oui | oui | — | — |
| Poser GPS / générer QR | non | non | oui | non |
| Consulter pointages org | non | Should (unité) vague 2 | oui | lecture |
| Révocation QR | non | non | oui | non |

\* JWT agent de terrain.  
\*\* Compte back-office ne pointe pas via admin API ; s’il a aussi un matricule mobile, parcours `USER` mobile.

Pas d’IDOR : un `USER` ne lit / ne crée que **son** `collaborateur_id` dérivé du JWT.

### Rétention / RGPD

| Donnée | Rétention | Base légale / contrainte |
|---|---|---|
| Pointages (qui, quand, site, statut) | **10 ans** (C-L-01 pièces / données RH présence) | Droit du travail / audit paie future |
| Coordonnées GPS du scan | **5 ans** alignées journal d’audit (C-L-02), puis purge ou agrégation (site + distance seulement) | Minimisation (C-L-03) |
| Emplacement de référence du site | Tant que site actif + historique credentials | Nécessaire au contrôle |
| Secrets HMAC | Rotation ops ; anciennes clés gardées le temps de valider QR encore `ACTIF` | Secret manager |
| Biométrie | **Hors vague** ; si introduite : score seul, pas d’image (C-L-04) | Consentement § 23 |

Pas de multi-société / multi-tenant.

### Observabilité

- Logs structurés : `correlationId`, `siteId`, `statut`, `distanceMetres` (pas le secret QR).
- Métriques : taux `REJETE_HORS_ZONE`, latence `POST …/pointages` (cible P95 &lt; 300 ms hors réseau mobile).

---

## Rejetés (trop lourds ou hors Must)

| Alternative | Motif de rejet |
|---|---|
| Étendre `svc-referentiel-rh` | Couplage BC, secrets QR, risque régression M00–M05 ; contredit cible archi |
| QR TOTP 5 min (§ 14.1) | Incompatible besoin « imprimer / télécharger ~3 mois » ; ops terrain |
| Face + liveness Must | C-L-04 + consentement § 23 ; allonge délai ; Could |
| Preuve géofence client-only | Contournable ; non conforme anti-fraude |
| QR non signé (`siteId` en clair) | Forge trivial |
| One-time QR par collaborateur | UX borne partagée dégradée |
| Wi-Fi BSSID / BLE beacon Must | Infra sites + complexité ; Should plus tard |
| Pointage offline-first complet | Horloge / GPS spoof + synchro conflictuelle ; vague 2 |
| Base partagée avec référentiel | Viole « un service = une base » |
| Multi-tenant | Hors périmètre § 21 |
| Mesh mTLS / WAF / service mesh | Surdimensionné pour vague 1 |
| Extraction massive microservices M01–M05 | Interdit sans scaling / équipe dédiée |

---

## Handoff backend

1. **Bootstrap** `svc-presence` (Spring Boot 3, Java 21, Security resource server JWT, Eureka, Flyway, Actuator health).
2. **Gateway** : ajouter la route `svc-presence` **avant** `svc-referentiel-rh-v1` (`/api/rh/v1/**`).
3. **Implémenter** tables + APIs admin (sites, emplacement, generate/download QR) + `POST` mobile pointages avec Haversine + validation HMAC.
4. **Config** : `PRESENCE_QR_HMAC_SECRET`, `presence.qr.validity-days=90`, `presence.geofence.default-radius-metres=50`.
5. **Kafka** (Should) : publier sur `rh.notifications` si `REJETE_HORS_ZONE` (destinataire RO unité — résolution via appel référentiel ou topic + matricule ; détail BA).
6. **Tests** : unitaires Haversine (frontière 49.9 / 50.1 m), signature QR (expire / révoqué / mauvais sig), `@WebMvcTest` RBAC, idempotence `idempotencyKey`.
7. **Ne pas** implémenter face, TOTP 5 min, ni BSSID dans cette vague.

### Handoff frontend (indicatif, hors code)

- **Web** : écran RH sites + bouton générer / télécharger QR ; pas de carte obligatoire si lat/lon viennent du mobile RH.
- **Mobile RH/ADMIN** : action « Enregistrer emplacement ici » → `PUT …/emplacement`.
- **Mobile collab** : scanner → envoyer token + GPS ; afficher motif rejet serveur ; **retirer** l’étape face du parcours Must (ou la masquer derrière feature flag off).

### Événements (esquisse)

```text
rh.notifications
  type: PRESENCE_REJET_HORS_ZONE | PRESENCE_QR_ROTATE (optionnel)
  destinataire: matricule RO ou groupe RH
  payload: { pointageId, siteId, collaborateurId, distanceMetres, serverTs }
  eventId: UUID  # idempotence C-S-05
```

---

## Critères d’acceptation architecturaux (recette vague Must)

1. Un QR généré web, imprimé, scanné **sur site** (GPS ≤ 50 m) → `VALIDE`.
2. Le **même** QR photocopié **hors** rayon → `REJETE_HORS_ZONE` (distance serveur &gt; 50 m).
3. Payload QR altéré / signature invalide → `REJETE_QR_INVALIDE` sans toucher à la géofence.
4. Après rotation, ancien QR → `REJETE_QR_REVOQUE` (ou `EXPIRE`).
5. Aucun client n’appelle un port interne : uniquement gateway `:8080`.
6. Face absente du parcours Must ; pas de photo envoyée au serveur.

---

## Suivi

| Action | Owner |
|---|---|
| Valider ADR (statut → accepté) | PO + Architecte |
| Noter écart C-M09-01 vague Must dans base fonctionnelle | BA / PO |
| Implémenter `svc-presence` | Senior backend |
| Brancher web + mobile | Senior frontend |
| Politique consentement biométrie | PO (avant vague face) |
