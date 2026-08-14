# UX — Pointage QR + géofence (Must M09)

| | |
|---|---|
| **Rôle** | UI/UX SIRH — RH Connect & Plateforme RH |
| **Date** | 13 août 2026 |
| **Statut** | Parcours Must figés pour handoff front (P1–P5 + P3) · **sans code métier** |
| **Sources** | [`sirh_pointage_qr_vague.md`](sirh_pointage_qr_vague.md) · [`sirh_pointage_qr_spec.md`](sirh_pointage_qr_spec.md) · [`adr/ADR-M09-pointage-qr-gps.md`](adr/ADR-M09-pointage-qr-gps.md) |
| **Thèmes** | Mobile `AppTheme` · Web `app.css` (shell, btn, badge, alert, empty-state) |
| **Libellé produit** | **« Site de pointage »** (pas « borne ») — H-BA-06 |
| **Fuseau UI** | `Africa/Tunis` (affichage) · horodatage métier = serveur UTC |
| **A11y** | WCAG 2.1 AA · cibles tactiles ≥ 44 px mobile · statut ≠ couleur seule |

**Principe vague** : Must = **QR + GPS ≤ 50 m + ENTREE/SORTIE**. **Aucune** étape face, **aucune** photo envoyée au serveur (C-L-04).  
**Transitoire** : tant que `svc-presence` n’est pas branché, bandeau discret « En cours de déploiement » (pas « prototype jeu ») sur les écrans M09 ; retirer le bandeau dès l’API live.  
**Hors Must UX** : face, TOTP 5 min, pause/déjeuner, édition lat/lon clavier web, PDF A4 consignes, rayon configurable, alerte J-7.

---

## Glossaire microcopy (statuts)

| Statut API | Titre UI | Message utilisateur (corps) | Ton / icône |
|---|---|---|---|
| `VALIDE` | Pointage enregistré | « Votre {Entrée\|Sortie} a été enregistrée à {heure Tunis}. » | Succès · check |
| `REJETE_HORS_ZONE` | Hors zone | « Vous êtes trop loin du site de pointage (rayon 50 m). Rapprochez-vous de l’emplacement du QR, puis réessayez. » | Erreur · map-pin |
| `REJETE_QR_INVALIDE` | QR non reconnu | « Ce code QR n’est pas valide. Scannez le QR affiché sur le site (pas une photo floue ou un code modifié). » | Erreur · qr |
| `REJETE_QR_EXPIRE` | QR expiré | « Ce QR a expiré. Prévenez la RH pour le renouveler et réimprimer. » | Erreur · clock |
| `REJETE_QR_REVOQUE` | QR remplacé | « Ce QR n’est plus actif (un nouveau a été généré). Scannez le QR affiché actuellement sur le site. » | Erreur · refresh |
| `REJETE_SITE_INACTIF` | Site indisponible | « Ce site de pointage est désactivé. Contactez la RH. » | Erreur · block |
| `REJETE_EMPLACEMENT_ABSENT` | Emplacement non configuré | « L’emplacement GPS de ce site n’est pas encore posé. La RH doit le configurer sur place. » | Erreur · location_off |
| `REJETE_PRECISION_GPS` | GPS imprécis | « La précision de votre localisation est insuffisante. Sortez à découvert, activez le GPS haute précision, puis réessayez. » | Warning · gps |
| GPS refusé / absent (422) | Localisation requise | « Activez la localisation pour pointer. Sans GPS, le pointage est refusé. » | Erreur · location |
| Caméra refusée | Caméra requise | « Autorisez l’accès à la caméra pour scanner le QR du site. » | Erreur · camera |

Toujours : **titre + corps + action** (Réessayer / Fermer / Ouvrir réglages). Ne jamais afficher codes techniques seuls (`REJETE_*`) sans libellé.

---

## Cartographie écrans Must

| # | Canal | Écran | Stories |
|---|---|---|---|
| W1 | Web | Liste sites de pointage | P1, P3 |
| W2 | Web | Fiche site (+ générer / télécharger QR, badge J-30) | P1, P2, P3 |
| W3 | Web | Liste pointages (filtres basiques) | support Must / amorce P7 |
| M1 | Mobile collab | Parcours pointage (QR → type → GPS → résultat) | P4 |
| M2 | Mobile collab | Historique « Mes pointages » | P4 |
| M3 | Mobile RH/ADMIN | Liste sites → poser / modifier emplacement GPS | P5 |

Navigation web : entrée sidebar **« Sites de pointage »** (+ sous-lien ou onglet **« Pointages »**).  
Navigation mobile : FAB / tuile Accueil **« Pointage »** → M1 ; historique accessible depuis M1 (lien secondaire) ; M3 réservé menu RH/ADMIN (ex. « Admin sites » ou tuile conditionnelle rôle).

---

## UX — Liste sites de pointage — M09 — Web

**Acteur** : RH, ADMIN (écriture) · DIRECTION (lecture seule)

**Objectif** : voir tous les sites, repérer ceux sans GPS / QR / proches expiration, ouvrir une fiche.

**Structure**
- Zone haut : titre **Sites de pointage** · sous-titre court (« Emplacements QR et géofence 50 m ») · CTA primaire unique **Créer un site** (masqué / désactivé + tooltip si DIRECTION).
- Filtres légers : recherche libellé/code · filtre Actif / Inactif · filtre « QR expire ≤ 30 j » (option).
- Tableau dense (pattern Plateforme RH) : Code · Libellé · Statut site · Emplacement (posé / manquant) · QR (aucun / actif + date exp.) · Badge expiration · Actions (Voir).
- Badge ligne : `badge--warning` « Expire le JJ/MM » si `expireBientot` ; `badge--default` « Expiré » si passé ; icône + texte (pas couleur seule).

**États**
| État | Comportement |
|---|---|
| Vide | `empty-state` : « Aucun site de pointage » · CTA Créer (RH/ADMIN) |
| Loading | Skeleton lignes tableau |
| Erreur | `alert--error` + Réessayer |
| Succès | Liste paginée 20–50 |
| 403 | Redirection / `AccesRefusePage` si hors BO ; DIRECTION : liste OK, CTA création absents |

**Règles visibles**
- Rayon affiché en lecture : **50 m** (fixe Must).
- Colonne Emplacement : « Non posé » → hint « Configurer via mobile RH ».
- Pas de carte obligatoire Must.

**A11y**
- En-têtes de colonnes `<th>` · focus visible sur CTA · badges avec `aria-label` (« QR expire le … »).

**Handoff front**
- Shell : `AppShell` / `shell__nav-item`.
- CTA : `btn btn--primary`, secondaires `btn--ghost` / `btn--sm`.
- Badges : `badge badge--success|warning|default|info` (ne pas réutiliser `badge--eval-*`).
- Alertes : `alert alert--info|error`.
- Vide : `empty-state`.
- RBAC UI : `useLectureSeule` + `RequireWriteAccess` (masquer Créer / actions écriture pour DIRECTION).

---

## UX — Fiche site de pointage — M09 — Web

**Acteur** : RH, ADMIN · DIRECTION lecture seule

**Objectif** : administrer un site : identité, GPS (consultation), cycle de vie QR (générer / télécharger / régénérer / révoquer), alerte expiration.

**Structure** (une composition, CTA principal clair)
1. **En-tête** : libellé + code · chips Actif/Inactif · retour liste.
2. **Bandeau expiration** (si J-30 ou expiré) : `alert--warning` / `alert--error`  
   - J-30 : « Ce QR expire le {date}. Renouvelez-le puis réimprimez l’affichage. »  
   - Expiré : « QR expiré — les collaborateurs ne peuvent plus pointer sur ce site. »
3. **Bloc Identité** : code, libellé, rayon 50 m (lecture), bascule Actif (écriture RH/ADMIN uniquement).
4. **Bloc Emplacement GPS** : lat/lon formatés ou « Non posé » · texte d’aide « Posez l’emplacement depuis RH Connect (mobile) sur le lieu d’affichage du QR. » · **pas** de saisie clavier Must (Should).
5. **Bloc Code QR** — zone actions :
   - Sans QR : CTA primaire **Générer QR** (`btn--primary`).
   - Avec QR actif : métadonnées (version, valide du … au …) +  
     - **Télécharger code QR** (`btn--secondary` ou `btn--primary` outline) — **toujours visible**, jamais masqué hors J-30 (D-P04 / R-P19).  
     - Secondaire : **Régénérer** (`btn--warning`) → modal confirmation.  
     - Danger discret : **Révoquer** (`btn--danger` / ghost danger) → modal.
6. CTA principal de la fiche selon état : *Générer* si absent, sinon *Télécharger* (action la plus fréquente terrain). Régénérer reste secondaire volontaire ( irreversible UX).

**Modal régénération** (obligatoire)
- Titre : « Remplacer le QR ? »
- Corps : « L’ancien QR sera **immédiatement** invalide. Réimprimez et remplacez l’affichage sur site avant de communiquer. »
- Actions : Annuler · **Régénérer** (confirm destructif).

**États**
| État | Comportement |
|---|---|
| Loading | Skeleton blocs |
| Erreur | `alert--error` |
| Succès génération | Toast / `alert--success` + aperçu QR optionnel + focus sur **Télécharger** |
| Succès download | Téléchargement PNG (H-BA-07) · pas d’écran bloquant |
| 403 écriture | DIRECTION : bandeaux + métadonnées visibles ; boutons Générer / Régénérer / Révoquer / Actif absents ou disabled + texte « Lecture seule » |
| Pas de QR | Télécharger **disabled** avec `title` « Générez d’abord un QR » |

**Règles visibles**
- Validité **90 jours** affichée en clair sous le QR.
- Télécharger ≠ alerte J-30 (deux artefacts distincts).
- Notification cloche BO (existant) pour `PRESENCE_QR_EXPIRE_J30` : deep-link vers cette fiche.

**A11y**
- Modales : focus trap (`modal` / `modal-backdrop`) · `aria-labelledby`.
- Bouton Télécharger toujours dans le tab order s’il est enabled.

**Handoff front**
- Pattern fiche : pages Structure / Collaborateurs (header + sections card).
- Modales : `.modal-backdrop` / `.modal`.
- `useLectureSeule` pour cacher écriture.
- Pas de nouveau design system ; tokens `--accent`, `--muted`, `--border`.

---

## UX — Liste pointages — M09 — Web

**Acteur** : RH, ADMIN · DIRECTION lecture

**Objectif** : consulter les essais (VALIDE + REJETE_*) pour support / anti-fraude basique — **pas** de reporting riche Must.

**Structure**
- Titre **Pointages** · filtres basiques uniquement : période (du/au) · site · statut (liste déroulante des statuts stables) · type ENTREE/SORTIE.
- Tableau : Date/heure Tunis · Collaborateur (id/matricule enrichi si dispo) · Site · Type · Statut (badge) · Distance (m) si connue · Motif si rejet.
- Pagination 20–50 · pas d’export Must.

**États**
| État | Comportement |
|---|---|
| Vide | « Aucun pointage pour ces filtres » |
| Loading | Skeleton |
| Erreur | `alert--error` + Réessayer |
| 403 | Accès BO uniquement |

**Règles visibles**
- Badge statut : succès = `badge--success` ; rejets = `badge--warning` ou `badge--default` + libellé texte (tableau glossaire).
- Ne pas calculer d’heures travaillées.

**A11y**
- Filtres avec labels visibles · résultats annoncés (« N résultats »).

**Handoff front**
- Pattern tables + filtres (Demandes / Évaluations).
- Badges existants ; colonnes distance en `muted`.

---

## UX — Parcours pointage collaborateur — M09 — Mobile

**Acteur** : Collaborateur (`USER`) · RO en JWT multi-rôles (pointage soi uniquement)

**Objectif** : enregistrer une **Entrée** ou **Sortie** en scannant le QR du site **uniquement** si GPS ≤ 50 m — feedback immédiat et compréhensible.

**Parcours (remplace le proto 3 steps QR → face → succès)**

```text
[Accueil] → Pointage
    │
    ├─ Préflight permissions (caméra + localisation)
    │     └─ refus → écran bloquant + CTA Réglages
    │
    ├─ Étape 1/3 — Scanner le QR
    │     └─ MobileScanner plein cadre · annuler
    │
    ├─ Étape 2/3 — Type de pointage
    │     ├─ ENTREE | SORTIE (2 grandes cibles)
    │     └─ suggestion non bloquante : « Dernier pointage valide : Entrée → Sortie suggérée »
    │
    ├─ Étape 3/3 — Localisation & envoi
    │     ├─ Lecture GPS (spinner) · précision affichée si dispo
    │     └─ CTA unique « Confirmer le pointage »
    │
    └─ Résultat plein écran (VALIDE | REJETE_*)
          ├─ VALIDE → heure serveur Tunis · site · type · CTA « Terminer » / « Pointer à nouveau »
          └─ REJETE_* → microcopy glossaire · CTA « Réessayer » (reprend étape 1) · pas de silent success
```

**Indicateur d’étapes** : 3 dots **QR → Type → GPS** (réutiliser `_StepIndicator` du proto en **retirant** l’étape Face).  
**Interdit Must** : caméra face, ML Kit face, envoi photo, message « reconnaissance faciale ».

**Structure visuelle**
- Fond `AppTheme.background` · cartes `surface` + `border` (radius 16).
- Boutons `FilledButton` hauteur ≥ 52 px (thème).
- Type : deux boutons pleine largeur (Entrée primary outline / Sortie) — sélection exclusive avant confirm.
- Écran résultat : icône grande + titre + corps + heure · couleurs `success` / `error` / `warning` **et** icône distincte.

**États**
| État | Comportement |
|---|---|
| Loading GPS / POST | Overlay non dismiss « Vérification en cours… » (évite double tap ; idempotencyKey côté front) |
| Succès VALIDE | Écran vert + détail |
| Rejet métier (HTTP 200 + statut) | Écran rouge/ambre selon glossaire — **même parcours résultat** |
| Erreur réseau / 5xx | SnackBar / écran « Impossible de joindre le serveur » + Réessayer |
| 401 | Session expirée → login |
| 403 | Ne devrait pas arriver (soi) ; message générique accès |
| Caméra / GPS refusés | Écrans dédiés avant scan (pas après) |
| Hors ligne | Message « Connexion requise pour pointer » (Must = online only) |

**Règles visibles**
- Mention courte sous le scan : « Le pointage n’est valide que dans un rayon de **50 m** autour du site. »
- Heure affichée = **serveur** (pas l’horloge téléphone) après réponse.
- Suggestion type opposé = hint texte, **jamais** verrouillage.

**A11y**
- Cibles ≥ 44 px · contraste titres sur fond résultat · annonce `Semantics` / TalkBack du statut.
- Scanner : instruction textuelle hors cadre (pas seulement icône).
- Ne pas s’appuyer sur la seule couleur (vert/rouge).

**Handoff front**
- Réutiliser : `PointageScreen` (refonte steps), `MobileScanner`, `AppTheme`, `NotificationActionBadge`, `FilledButton` / `OutlinedButton`, `SnackBar` thème.
- Retirer / feature-flag **off** : `camera` face + `google_mlkit_face_detection`.
- Nouveau : service/repository présence mobile ; permission_handler / geolocator (stack projet) ; écran résultat unique paramétré par statut.
- Accueil : tuile / FAB existants `Icons.qr_code_scanner_rounded` → route `/pointage`.

---

## UX — Historique mes pointages — M09 — Mobile

**Acteur** : Collaborateur (soi)

**Objectif** : retrouver ses derniers pointages VALIDES et rejets (transparence).

**Structure**
- Liste chronologique : date Tunis · type · site · statut (chip) · distance si rejet hors zone.
- Entrée depuis M1 (lien AppBar « Historique ») ou tuile secondaire.
- Pas d’édition / suppression.

**États** : vide (« Aucun pointage pour le moment ») · loading · erreur + Réessayer · 403 autre matricule impossible (API me).

**A11y** : chips statut avec label textuel.

**Handoff front** : pattern listes Documents / Demandes ; `RhSectionCard` si regroupement par jour.

---

## UX — Poser / modifier emplacement GPS — M09 — Mobile RH/ADMIN

**Acteur** : RH, ADMIN uniquement (`USER` / RO → 403)

**Objectif** : calibrer sur le terrain le point GPS du site (= lieu d’affichage du QR).

**Parcours**
```text
Menu RH → Sites de pointage
  → Liste (libellé, actif, GPS posé/non, QR résumé)
  → Fiche site mobile
       ├─ Infos lecture (code, libellé, rayon 50 m, dernière MAJ qui/quand)
       ├─ CTA primaire « Utiliser ma position »
       │     → permission GPS → lecture précision → écran confirmation
       │           « Enregistrer cette position pour {libellé} ? »
       │           lat/lon + précision affichées
       │           Confirmer → succès SnackBar / écran
       └─ Si GPS déjà posé : même CTA libellé « Mettre à jour ma position »
```

**Structure**
- Liste simple (pas de carte Must) · pastille « GPS manquant » (`warning`) prioritaire en tête de tri recommandé.
- Confirmation obligatoire avant PUT (évite pose accidentelle depuis le parking).
- Après succès : rappel « Les prochains pointages utiliseront cette position (rayon 50 m). »

**États**
| État | Comportement |
|---|---|
| 403 USER | Écran « Accès réservé à la RH » (pas de fuite liste) |
| GPS refusé | Même pattern que collab + CTA réglages |
| Précision mauvaise (> 100 m) | Warning non bloquant à la pose admin **ou** conseil « attendez un meilleur signal » (recommandation UX ; règle R-P12 s’applique au **scan collab**, pas forcément au PUT admin — afficher la précision pour décision humaine) |
| Succès | SnackBar + retour fiche avec coordonnées à jour |
| Erreur API | Message + Réessayer |

**Règles visibles**
- Consigne : « Placez-vous **devant le QR affiché**, puis enregistrez. »
- Pas d’édition manuelle lat/lon sur mobile Must.

**A11y** : boutons 52 px · confirmation dialog avec actions explicites.

**Handoff front**
- Nouvelle feature `presence` / `sites_pointage` sous `features/` · garde rôle JWT (`RH`/`ADMIN`) comme autres écrans admin mobile.
- Réutiliser `AppTheme`, dialogs Material, liste type Formations/Documents.
- API : `PUT …/emplacement` (spec § 4.1).

---

## Direction — lecture seule (Web)

| Surface | Visible | Masqué |
|---|---|---|
| Liste / fiche sites | Oui | Créer, Générer, Régénérer, Révoquer, Activer/Désactiver |
| Télécharger QR | **Masqué** pour DIRECTION (RBAC spec : download RH/ADMIN seulement) | — |
| Liste pointages | Oui | Toute mutation |
| Bandeau J-30 | Oui (information) | Actions renouvellement |

Microcopy bandeau lecture : « Profil Direction — consultation uniquement. »

---

## Notifications (impact UX)

| Événement | Canal | UX |
|---|---|---|
| `PRESENCE_QR_EXPIRE_J30` | Cloche BO (+ mobile RH si feed unifié) | Titre « QR à renouveler » · corps site + date · tap → fiche site W2 |
| Badge fiche | Web W2 | Bandeau local indépendant de la cloche |

Ne pas masquer **Télécharger** quand la notif apparaît.

---

## Wireflows résumé

### Collab (terrain, 4G)
Permission OK → Scan → Choisir Entrée/Sortie → Confirmer GPS → Résultat clair.

### RH web (bureau)
Liste → Fiche → Générer → Télécharger PNG → imprimer / poser au mur → (J-30) notif + bandeau → Régénérer + réimprimer.

### RH mobile (sur site)
Liste sites → site → Utiliser ma position → Confirmer → collaborateurs peuvent pointer.

---

## Checklist a11y Must

- [ ] Contraste texte / fond ≥ 4.5:1 (tokens `textPrimary` / `--text` sur surfaces).
- [ ] Statuts : icône + texte (+ badge), jamais couleur seule.
- [ ] Focus visible web ; ordre tabulaire logique (Générer / Télécharger).
- [ ] Cibles tactiles ≥ 44 px (thème mobile déjà 52 px boutons).
- [ ] Messages rejet actionnables (Réessayer / Réglages).
- [ ] Modales régénération / confirmation GPS : Escape / Annuler.
- [ ] Pas de captcha / puzzle ; langage FR simple (terrain AGUA).

---

## Handoff composants à réutiliser

### Web (`rh-admin-web`)
| Besoin | Réutiliser |
|---|---|
| Shell / nav | `AppShell`, `shell__nav-item` |
| Boutons | `btn`, `btn--primary`, `btn--secondary`, `btn--ghost`, `btn--sm`, `btn--warning`, `btn--danger`, `btn--success` |
| Badges statut / expiration | `badge`, `badge--success\|warning\|info\|default` |
| Alertes / bandeaux | `alert`, `alert--info\|error\|success` (+ warning style aligné `badge--warning` si besoin classe dédiée légère) |
| Vide | `empty-state` |
| Modales | `modal-backdrop`, `modal` |
| Lecture seule Direction | `useLectureSeule`, `RequireWriteAccess`, `AccesRefusePage` |
| Tables / filtres | Patterns `DemandesAdministrativesPage` / `EvaluationsPage` |

### Mobile (`rh_mobile_app`)
| Besoin | Réutiliser |
|---|---|
| Thème | `AppTheme` (primary, surface, border, success, warning, error) |
| Scan QR | `mobile_scanner` + cadre existant proto |
| Steps | `_StepIndicator` / `_StepCard` (adapter à 3 steps sans face) |
| Accueil entrée | Tuile / FAB Pointage `home_screen.dart` |
| Cloche | `NotificationActionBadge` |
| Cartes | `RhSectionCard`, `PressableScale` |
| Feedback | `SnackBar` thème · Dialogs Material |
| **Retirer Must** | Pipeline face (`camera` front + ML Kit) du parcours pointage |

### Ne pas introduire
- Nouvelle lib UI / design system parallèle.
- Cartographie lourde (Mapbox/Google) en Must.
- Codage couleur M07 (`badge--eval-*`) pour la présence.

---

## Hors scope UI (rappel)

Face / liveness · TOTP · pause · pointage manuel RO · offline queue pointage · PDF A4 consignes · rayon éditable · alerte J-7 · édition lat/lon clavier web · restriction sites par unité.

---

## TODO / hypothèses UX

| ID | Hypothèse | Si invalidée |
|---|---|---|
| UX-01 | Ordre Must = Scan → Type → Confirm GPS (aligné BA § 7.2) | PO peut inverser Type → Scan |
| UX-02 | DIRECTION ne télécharge pas le PNG (RBAC API) | Si PO élargit lecture fichier → montrer bouton disabled ou enabled lecture |
| UX-03 | Bandeau « déploiement » uniquement pré-API ; retiré à la bascule live | — |
| UX-04 | Suggestion type opposé basée sur dernier `VALIDE` de `/pointages/me` | Si endpoint lent → suggestion absente sans bloquer |
| UX-05 | Format download = PNG seul (H-BA-07) | PDF Should = écran aperçu + consignes plus tard |

---

*Livrable UI/UX M09 Must — pas de code React/Flutter métier. Handoff : seniors front web ‖ mobile après BA/ADR.*
