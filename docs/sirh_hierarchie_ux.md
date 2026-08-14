# UX — Organigramme / hiérarchie — M00 — Web (Plateforme RH) + Mobile (lecture)

| | |
|---|---|
| **Rôle** | UI/UX designer SIRH |
| **Date** | 12 août 2026 |
| **Canal principal** | Plateforme RH (React) — édition RH/ADMIN, lecture DIRECTION |
| **Canal secondaire** | RH Connect (Flutter) — consultation uniquement |
| **Source métier** | `docs/sirh_basefonctionnelle.md` § 5 (M00), § 4 (rôles), § 6.1 (M01 = manager nœud → RRH) |
| **Écran audité** | `rh-admin-web/src/pages/OrganigrammePage.tsx` + styles `.org-*` dans `app.css` |
| **Hors scope** | Logique métier, API, code TSX — livrable UX uniquement |
| **Note produit (provisoire)** | Workflow M01 reste « manager nœud → RRH » sauf tranché autrement par le PO. L’UX doit surtout rendre **lisible qui manage quel nœud** et permettre une **édition RH fiable**. |

---

## 1. Diagnostic UX de l’existant (web)

1. **DnD prioritaire mais peu fiable** — L’expérience repose sur le drag HTML5 (poignée `⋮⋮`, carte entière `draggable`, zone racine, *drop slots* « Déposer ici… »). Cibles multiples (carte vs slot enfant vs racine), feedback vert « succès » sur le drop, et disparition des slots hors hover : l’utilisateur ne sait pas où lâcher. Sensation « bancale », surtout sur arbre profond.

2. **Arbre peu « organigramme »** — Layout vertical type liste indentée (`border-left` bleu léger, cartes `max-width: 300px`). Pas de connecteurs en T/L, pas de vue large / zoom, pas de ligne de profondeur lisible. L’œil lit une file de cartes, pas une hiérarchie.

3. **Signal « qui manage » secondaire** — Le manager est une ligne sous le titre ; l’état « Sans responsable » est muted. Or c’est le **critère métier critique** (valideur M01 = manager ACTIF du nœud). Un nœud sans manager devrait crier davantage (badge + panneau), pas se fondre.

4. **Panneau détail sous-exploité** — Sticky panel correct en principe, mais : pas de fil d’Ariane parent → nœud, pas de rappel du parent, actions RH en 3 boutons de poids proche (« Assigner » primary OK, « Modifier » / « + Enfant » ghost). En lecture seule, le panel reste utile mais le canvas invite encore au DnD via le lead / hint.

5. **Chrome édition trop présent en Direction** — Bandeau `org-dnd-hint` + lead page centrés sur le DnD même quand `lectureSeule`. Les poignées / zones de drop sont masquées (bien), mais le **mode consultation** n’est pas un état de page clair (pas de bandeau « Consultation uniquement » dédié organigramme, pas de CTA édition masqué de façon narrative).

6. **États incomplets / peu soignés** — Loading = texte `.loading` ; vide = OK avec CTA racine ; erreur = `alert--error` en haut (peut être écrasée par d’autres messages) ; **pas d’état 403 dédié** sur la page (Direction bloquée ailleurs via `RequireWriteAccess` / `AccesRefusePage` pour d’autres routes — ici lecture OK). Pas de skeleton, pas de toast succès après move/assign.

7. **Microcopy technique** — « Nœud », « detacher », placeholders CEO/CTO, profil en pastille violette (`.org-node__profil`) hors accent principal. Vocabulaire peu RH-Évènement / AGUA ; confusion possible avec « Structure RH » (`/app/structure`) dans la nav.

8. **Mobile (pertinent)** — `OrganigrammeScreen` : liste indentée expandable, avatar manager correct. Manques : connecteurs, libellé explicite « Responsable », état « sans responsable », distinction nœud / membres, recherche / ancrage sur « mon unité ». Lecture seule OK ; ne pas y porter l’édition.

---

## 2. Vision cible

### 2.1 Objectifs UX

| Priorité | Objectif |
|---|---|
| P0 | Lire en 2 secondes : **qui est responsable de quel nœud** (et quels nœuds n’en ont pas). |
| P0 | Édition RH **fiable** : rattacher, créer, assigner manager **sans dépendre du DnD**. |
| P1 | Sensation **professionnelle** : arbre soigné, densité admin, cohérence design system. |
| P1 | Direction : expérience **lecture seule** assumée, zéro friction « j’aurais voulu éditer ». |
| P2 | Mobile : même modèle mental (arbre + responsable), parcours consultation rapide. |

### 2.2 Structure de page (web)

Quatre zones stables dans `.page` :

```
┌─ page__head ─────────────────────────────────────────────────────┐
│ Titre + lead court          │  [Recherche] [Filtres] [Actualiser]│
│                             │  [+ Ajouter un service]  (écriture) │
├─ bandeau mode (si Direction / si write) ─────────────────────────┤
├─ org-toolbar (compact) ──────────────────────────────────────────┤
│  Tout développer · Tout replier · Afficher inactifs · Aide       │
├─ org-layout (grid existante) ────────────────────────────────────┤
│  ┌─ org-canvas (arbre) ──────────────┐  ┌─ org-panel ──────────┐ │
│  │  toolbar locale zoom/scroll hint  │  │  Détail nœud         │ │
│  │  arbre + connecteurs              │  │  Responsable (hero)  │ │
│  │                                   │  │  Membres             │ │
│  │                                   │  │  Actions RH          │ │
│  └───────────────────────────────────┘  └──────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

- **CTA unique principal (écriture)** : `+ Ajouter un service` (racine ou, si sélection, enfant du nœud sélectionné — libellé dynamique).
- **CTA secondaire** : Actualiser (ghost).
- **Pas de second CTA DnD** dans le head.

### 2.3 Canvas

- Surface `.org-canvas` conservée (fond surface, bordure, radius).
- Arbre **vertical** (compatible large org AGUA) avec **connecteurs SVG/CSS** (ligne verticale + tiret horizontal vers chaque carte) — pas une nouvelle lib de diagramme sauf option notée en § 6.
- Cartes nœud **largueur fixe ~280–320 px**, alignées à gauche de leur niveau (pas de layout org-chart horizontal pour v1 — trop coûteux / fragile).
- Expand/collapse par nœud (chevron) ; profondeur > 3 repliée par défaut sauf chemin vers sélection / recherche.
- Zone racine : **pas** de drop permanent ; en mode édition, action « Placer à la racine » uniquement via sélecteur parent.

### 2.4 Panneau détail

Panneau sticky `.org-panel` = **surface d’édition principale** :

1. Type (badge) + libellé + code  
2. **Bloc Responsable** (visuellement dominant) — avatar initiales, nom, matricule, profil d’accès en texte (pas pastille violette obligatoire)  
3. Parent (lien « Voir le parent »)  
4. Collaborateurs rattachés (liste)  
5. Actions RH (écriture uniquement) : Assigner / Modifier / Ajouter un enfant / Rattacher…

En Direction : mêmes infos, **aucune** action d’écriture ; mention douce « Consultation — modification réservée à la RH ».

### 2.5 Toolbar

| Contrôle | Rôle |
|---|---|
| Recherche (libellé, code, nom manager) | Filtre + highlight + expand path |
| Tout développer / Tout replier | Navigation arbre large |
| Afficher les inactifs | Toggle (API `inclureInactifs` déjà utilisée côté page) |
| Aide courte | Popover : « Comment rattacher / assigner » |

---

## 3. Interactions

### 3.1 Rattachement — **sélecteur parent d’abord** (recommandé)

**Décision UX** : le DnD n’est plus le chemin principal. Chemin fiable :

1. Sélectionner un nœud → panneau → **Rattacher…** (ou dans modal Modifier → champ Parent).
2. Modale / drawer « Choisir le parent » : liste arborescente ou select groupé par profondeur, avec recherche.
3. Options : parent X, ou « Aucun parent (racine) ».
4. Validation : interdiction de choisir un descendant (message clair).
5. Confirmation courte si le déplacement change beaucoup de profondeur : « « Unité A » sera placée sous « Direction Ops ». Continuer ? »

**DnD (option progressive enhancement, écriture seulement)** :

- Activé uniquement via poignée dédiée (pas toute la carte) — `aria-grabbed`, focus clavier alternatif obligatoire.
- Drop = **uniquement** sur le corps d’une carte cible (devenir enfant) ; **pas** de slots invisibles.
- Pendant le drag : overlay fantôme + liste des cibles invalides grisées (descendants).
- Si le navigateur / a11y : masquer DnD et garder le sélecteur.

### 3.2 Création de nœud

| Déclencheur | Comportement |
|---|---|
| CTA head « + Ajouter un service » | Si sélection → enfant de la sélection ; sinon → racine. Prévisualiser le parent dans la modale. |
| Bouton `+` sur carte | Identique à « enfant de ce nœud » (écriture). |
| Panneau « + Enfant » | Idem. |

Champs modale (ordre) : Type → Libellé → Code → Titre du poste (opt.) → Parent (readonly si déjà choisi) → Actif.

Succès : toast / `alert--success` « Service créé » + sélection du nouveau nœud + panneau ouvert.

### 3.3 Assignation manager

- Entrée principale : panneau → **Assigner le responsable** (primary).
- Modale : recherche collaborateur (matricule / nom), titre de poste libre, rappel : *« L’affectation ne change pas le profil d’accès (fiche collaborateur). »*
- Retirer = danger secondaire avec confirm : « Sans responsable, les demandes M01 de cette unité iront directement à la RRH. » (aligné § 5.2 / 6.1 — à valider wording PO si besoin).
- Carte nœud : badge **Sans responsable** (warning) tant que `manager` null et nœud actif.

### 3.4 États d’écran

| État | Web | Mobile |
|---|---|---|
| **Vide** | `.empty-state` : illustration sobre + « Aucune structure publiée » + CTA créer racine (écriture) / texte Direction « Contactez la RH » | Texte centré + Réessayer inutile |
| **Loading** | Skeleton 3–4 cartes + panneau fantôme (ou `.loading` minimal) ; `aria-busy` sur canvas | `CircularProgressIndicator` |
| **Erreur** | `alert--error` sticky sous head + bouton Réessayer ; ne pas vider l’arbre précédent si reload partiel | Message + Réessayer |
| **403 / hors droits écriture** | Direction sur page org = **lecture** (pas 403) : bandeau consultation. Tentative d’API write → message « Vous n’avez pas le droit de modifier l’organigramme. » | N/A (pas d’édition) |
| **Succès action** | `alert--success` auto-dismiss 4 s ou toast | SnackBar |
| **Moving / saving** | Désactiver actions + spinner sur CTA ; garder sélection | — |

### 3.5 Clavier & a11y (WCAG 2.1 AA)

- Arbre en `role="tree"` / `treeitem` (ou liste + boutons) : flèches haut/bas, Enter sélection, Expand/Collapse.
- Focus visible sur cartes et boutons (contraste ≥ 3:1 focus ring — réutiliser accent).
- Labels FR sur tous les contrôles ; poignée DnD `aria-label="Déplacer [libellé]"` + alternative sélecteur.
- Contraste texte muted / badges : vérifier pastilles ; éviter violet seul pour l’info profil (texte + icône).
- Modales : focus trap, Escape (déjà présent), titre `h3`.
- Mobile : zones tactiles ≥ 44 px (expand, refresh).

---

## 4. Hiérarchie visuelle (handoff front React)

### 4.1 Profondeur

| Niveau | Traitement |
|---|---|
| 0 (racine) | Carte pleine opacité ; type accent ; ombre légère existante |
| 1–2 | Indent `24px` + connecteur ; même carte |
| ≥ 3 | Indent cumulatif ; option « fond surface-2 » très léger sur la branche ; collapse par défaut |
| Inactif | `.org-node--inactive` (opacity) **+** badge texte « Inactif » (ne pas s’appuyer sur l’opacité seule) |

### 4.2 Connecteurs

- Remplacer / enrichir le seul `border-left` actuel.
- Spécification CSS cible :
  - Colonne guide : `2px` `rgba(91, 141, 239, 0.35)` (déjà proche).
  - Trait horizontal vers la carte : `12–16px` avant le bord gauche de la carte.
  - Coin arrondi optionnel (L) pour aspect pro.
- Pas de connecteurs croisés ; un parent → N enfants en colonne.

### 4.3 Cards nœuds (composition)

```
┌────┬─────────────────────────────┬───┐
│ ⋮⋮ │ TYPE                        │ + │  ← + et ⋮⋮ : écriture only
│    │ Libellé du service          │   │
│    │ Titre poste (muted)         │   │
│    │ ┌ Responsable ────────────┐ │   │
│    │ │ ○ Prénom Nom   [profil] │ │   │  ← zone hero mini
│    │ └─────────────────────────┘ │   │
│    │ 3 collaborateurs            │   │
└────┴─────────────────────────────┴───┘
```

- **Sélection** : conserver `.org-node--selected` (ring accent).
- **Sans responsable** : bordure gauche `warning` (#f59e0b déjà en design system docs/accent) ou badge `.badge--warning` « Sans responsable ».
- **Drop target** (si DnD) : ring accent (bleu), **pas** vert succès — le vert est réservé aux alertes métier / succès d’action.
- Largeur max 300–320 px ; hauteur contenu intrinsèque ; éviter le grab cursor sur toute la carte en lecture.

### 4.4 Panneau

- Hiérarchie typo : type (eyebrow) → titre → code muted → **section Responsable** (fond `surface-2`, radius-sm) → sections Membres / Parent → actions.
- Boutons : primary unique « Assigner le responsable » si vide ; sinon ghost « Changer le responsable ».

---

## 5. Microcopy FR

### 5.1 Page

| Emplacement | Texte |
|---|---|
| Titre | Organigramme |
| Lead (écriture) | Visualisez la hiérarchie et désignez le responsable de chaque service. |
| Lead (Direction) | Consultez la hiérarchie et les responsables de chaque service. |
| Bandeau Direction | Consultation uniquement — les modifications sont réservées à la RH. |
| Hint aide (écriture) | Pour rattacher un service : ouvrez le détail puis « Rattacher… », ou utilisez la poignée pour glisser-déposer. |
| Toolbar recherche | Rechercher un service ou un responsable… |
| Vide (écriture) | Aucune structure pour le moment. Commencez par le sommet de la hiérarchie. |
| Vide CTA | Créer le premier service |
| Vide (lecture) | Aucune structure organisationnelle publiée. |
| Loading | Chargement de l’organigramme… |
| Erreur | Impossible de charger l’organigramme. |
| Réessayer | Réessayer |
| Moving | Déplacement en cours… |
| Succès move | Service rattaché. |
| Succès manager | Responsable assigné. |
| Succès retrait | Responsable retiré. |
| Erreur cycle | Impossible : un service ne peut pas être placé sous l’un de ses rattachements. |
| 403 write | Vous n’avez pas le droit de modifier l’organigramme. |

### 5.2 Carte & panneau

| Emplacement | Texte |
|---|---|
| Sans manager | Sans responsable |
| Compteur | {n} collaborateur · {n} collaborateurs |
| Panneau vide | Sélectionnez un service dans l’arbre pour voir le détail. |
| Section | Responsable |
| Section | Collaborateurs |
| Section | Rattachement |
| Parent aucun | À la racine (aucun parent) |
| Actions | Assigner le responsable · Changer le responsable · Modifier · Ajouter un service enfant · Rattacher… |

### 5.3 Modales

| Modale | Titre / CTA |
|---|---|
| Créer racine | Ajouter un service (racine) |
| Créer enfant | Ajouter sous « {libellé} » |
| Éditer | Modifier le service |
| Parent | Service parent |
| Parent option racine | — Aucun parent (racine) — |
| Manager | Responsable — {libellé} |
| Manager aide | L’affectation du responsable ne change pas le profil d’accès. Celui-ci se définit sur la fiche collaborateur. |
| Confirm retrait | Retirer le responsable de « {libellé} » ? Les demandes de cette unité pourront être traitées directement par la RRH s’il n’y a plus de responsable actif. |
| Annuler / Enregistrer / Créer / Assigner / Retirer | standards |

### 5.4 Vocabulaire

- Préférer **service** / **unité** / **responsable** à « nœud » dans l’UI (garder « nœud » en doc technique / API).
- Éviter CEO/CTO comme exemples par défaut AGUA → suggestions : Direction, Département, Service, Unité, Équipe.

### 5.5 Mobile

| Emplacement | Texte |
|---|---|
| AppBar | Organigramme |
| Vide | Aucune structure organisationnelle publiée. |
| Erreur | Impossible de charger l’organigramme. |
| Label manager | Responsable |
| Sans manager | Sans responsable |
| Membres | Collaborateurs |

---

## 6. Handoff composants / classes CSS à réutiliser

### 6.1 Web — réutiliser (pas de nouvelle lib)

| Besoin | Classe / motif existant |
|---|---|
| Page | `.page`, `.page__head`, `.page__title`, `.page__lead`, `.page__head-actions` |
| Boutons | `.btn`, `.btn--primary`, `.btn--ghost`, `.btn--danger`, `.btn--sm` |
| Alertes | `.alert`, `.alert--error`, `.alert--info`, `.alert--success` |
| Formulaire | `.form-grid`, `.form-group`, `.form-actions`, `.full-width` |
| Modale | `.modal-backdrop`, `.modal`, `.modal__title`, `.org-modal-close` |
| Vide générique | `.empty-state` |
| Loading | `.loading` |
| Badges | `.badge`, `.badge--warning`, `.badge--info` (préférer à pastille violette profil) |
| Texte | `.text-muted`, `.text-sm` |
| Layout org | `.org-layout`, `.org-canvas`, `.org-panel`, `.org-panel__*` |
| Arbre / carte | `.org-tree`, `.org-tree__item`, `.org-node`, `.org-node__*` |
| Tokens | `--accent`, `--surface`, `--surface-2`, `--border`, `--muted`, `--radius`, `--text` |

**À ajuster en CSS (senior front, hors ce livrable)** : connecteurs, badge sans responsable, bandeau lecture, déprécier slots `.org-drop-slot` / vert drop, profil sans violet forcé.

**Auth / mode** : `useLectureSeule()`, bandeau shell Direction déjà en place — aligner copy organigramme.

**Option lib (à trancher PO/architecte, non recommandée v1)** : `react-organizational-chart` ou canvas type `xyflow` — **uniquement** si vue organigramme horizontal devient un must ; coût a11y + maintenance élevé. Défaut = CSS tree actuel enrichi.

### 6.2 Mobile — réutiliser

| Besoin | Source |
|---|---|
| Couleurs / surfaces | `AppTheme` (`primary`, `surface`, `border`, `textSecondary`, `warning`) |
| Cards Material | Motif actuel `_NoeudCard` + `InkWell` |
| États async | `AsyncValue.when` (loading / error / data) |
| Avatar initiales | Motif existant `CircleAvatar` + `_initials` |

Pas de nouvelle dépendance Flutter pour l’arbre.

### 6.3 Frontière produit

- **Organigramme** (`/app/organigramme`) = hiérarchie visuelle + managers (ce doc).
- **Structure RH** (`/app/structure`) = référentiel départements / unités « admin table » — ne pas fusionner les UI ; éventuellement lien croisé soft : « Gérer le référentiel détail → Structure RH ».

---

## 7. Maquette textuelle / wireframes

### 7.1 Accueil organigramme — RH (écriture)

```
╔══════════════════════════════════════════════════════════════════╗
║  Organigramme                                                    ║
║  Visualisez la hiérarchie et désignez le responsable             ║
║  de chaque service.                                              ║
║                                                                  ║
║            [🔍 Rechercher…]  [Actualiser]  [+ Ajouter un service]║
╠══════════════════════════════════════════════════════════════════╣
║  [Tout développer] [Tout replier]  ☐ Afficher les inactifs  (?) ║
╠═══════════════════════════════════════════════╦══════════════════╣
║  CANVAS                                       ║  DÉTAIL          ║
║                                               ║                  ║
║  ┌─────────────────────────┐                  ║  DÉPARTEMENT     ║
║  │ DIRECTION               │                  ║  Exploitation    ║
║  │ Direction générale      │                  ║  Code : EXP      ║
║  │ ○ A. Benali  · RH       │                  ║                  ║
║  └──────────┬──────────────┘                  ║  ┌────────────┐  ║
║             │                                 ║  │ Responsable║  ║
║       ┌─────┴──────┐                          ║  │ ○ S. Trabelsi║ ║
║       ▼            ▼                          ║  │ Mat. 1042  ║  ║
║  ┌──────────┐  ┌──────────┐                   ║  │ RO         ║  ║
║  │ SERVICE  │  │ SERVICE  │◄── sélection      ║  └────────────┘  ║
║  │ Ops Nord │  │ Ops Sud  │                   ║                  ║
║  │ ○ …      │  │ ⚠ Sans   │                   ║  Parent : Dir.   ║
║  │          │  │   resp.  │                   ║  générale        ║
║  └──────────┘  └──────────┘                   ║                  ║
║                                               ║  Collaborateurs  ║
║                                               ║  · …             ║
║                                               ║                  ║
║                                               ║  [Assigner le    ║
║                                               ║   responsable]   ║
║                                               ║  [Modifier]       ║
║                                               ║  [Rattacher…]    ║
║                                               ║  [+ Enfant]      ║
╚═══════════════════════════════════════════════╩══════════════════╝
```

### 7.2 Direction (lecture seule)

```
╔══════════════════════════════════════════════════════════════════╗
║  Organigramme                                                    ║
║  Consultez la hiérarchie et les responsables de chaque service.  ║
║                                              [Actualiser]        ║
╠══════════════════════════════════════════════════════════════════╣
║  ℹ Consultation uniquement — modifications réservées à la RH.    ║
╠═══════════════════════════════════════════════╦══════════════════╣
║  (arbre sans poignées ni +)                   ║  (détail sans    ║
║                                               ║   boutons write) ║
╚═══════════════════════════════════════════════╩══════════════════╝
```

### 7.3 Modale « Rattacher »

```
┌─ Rattacher « Ops Sud » ─────────────────────┐
│  Nouveau parent                              │
│  ┌─────────────────────────────────────────┐ │
│  │ 🔍 Rechercher un service…               │ │
│  ├─────────────────────────────────────────┤ │
│  │ ○ Aucun parent (racine)                 │ │
│  │ ● Direction générale                    │ │
│  │   ○ Exploitation                        │ │
│  │   ○ Support                             │ │
│  └─────────────────────────────────────────┘ │
│              [Annuler]  [Enregistrer]        │
└──────────────────────────────────────────────┘
```

### 7.4 Mobile — consultation

```
┌─────────────────────────┐
│ ← Organigramme      ⟳   │
├─────────────────────────┤
│ ┌─────────────────────┐ │
│ │ DIRECTION           │ │
│ │ Direction générale  │ │
│ │ ○ A. Benali         │ │
│ │              ▾      │ │
│ └─────────────────────┘ │
│   ┌───────────────────┐ │
│   │ SERVICE           │ │
│   │ Ops Nord          │ │
│   │ Responsable       │ │
│   │ ○ S. Trabelsi     │ │
│   └───────────────────┘ │
│   ┌───────────────────┐ │
│   │ SERVICE           │ │
│   │ Ops Sud           │ │
│   │ ⚠ Sans responsable│ │
│   └───────────────────┘ │
└─────────────────────────┘
```

### 7.5 Parcours critiques (résumé)

| Acteur | Parcours | Succès visible |
|---|---|---|
| RH | Créer service → assigner responsable → (opt.) rattacher | Badges à jour, panneau, toast |
| RH | Corriger « Sans responsable » sur unité terrain | Badge warning disparaît |
| Direction | Ouvrir org → chercher un service → lire le responsable | Aucune action write |
| Collaborateur (mobile) | Ouvrir org → expand → voir son responsable d’unité | Clair, offline non requis v1 |

---

## A11y checklist (recette UX)

- [ ] Mode lecture : aucun contrôle d’édition focusable  
- [ ] Contraste badges warning / muted  
- [ ] Alternative clavier au DnD (sélecteur parent)  
- [ ] Annonce live region pour succès / erreur (`aria-live`)  
- [ ] Titres de modales uniques ; Escape ferme  
- [ ] Mobile : expand ≥ 44 px ; sens de lecture FR  

---

## TODO / hypothèses (pour PO / BA / front)

| ID | Point | Owner |
|---|---|---|
| H1 | Libellé UI « service » vs « unité » vs « nœud » — trancher catalogue types | PO |
| H2 | Confirm retrait manager : mention M01 → RRH — OK produit ? | PO / BA |
| H3 | DnD v1 : garder en progressive enhancement ou retirer totalement | UX + Front |
| H4 | Lien croisé Organigramme ↔ Structure RH | PO |
| H5 | Recherche serveur vs filtre client (arbres < ~200 nœuds = client OK) | Front / Archi |
| H6 | Mobile : ancre « Mon unité » si API expose le nœud du collaborateur | BA |

---

*Fin du livrable UX — aucun code applicatif produit. Fichier dédié : `docs/sirh_hierarchie_ux.md` (ne pas confondre avec une vague PO éventuelle `docs/sirh_hierarchie_vague.md`).*
