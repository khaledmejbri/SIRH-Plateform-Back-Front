# Vague « rôles + qualité du déjà-là » — coordination

**Statut** : stories **1, 2, 3, 4, 5 (backend) livrées** ; **story 7 backend Fait**. Vague hiérarchie / organigramme ([`sirh_hierarchie_vague.md`](sirh_hierarchie_vague.md) · UX [`sirh_hierarchie_ux.md`](sirh_hierarchie_ux.md)). Reste story **7** web/mobile, puis **6** (ADR).  
**Source de vérité métier** : [`sirh_basefonctionnelle.md`](sirh_basefonctionnelle.md).  
**Agents** : [PO backlog](32eb7a73-bb36-4605-9799-41b64ba3f3cc) · [BA specs](4247dc6f-c540-4ce9-832a-9a29c875bf3f) · [UI/UX](85fdc6cd-c974-4858-a233-ab843ea3526e) · [S3 backend](166481ea-24c7-40e8-a651-d70faad65a5f) · [S5 backend](30a22799-9963-4816-95c2-0bef8e8a2dd2)

---

## Décisions PO (non rouvertes)

1. Valideur M01 = **manager ACTIF du nœud d’unité** d’affectation (**1 niveau → RRH** ; pas de remontée N+2 / Direction via l’arbre). `superieur` fiche = **dérivé** (sync Should H3).
2. Catalogue unique JWT = `profil_acces` : `USER`, `RO`, `RESPONSABLE`, `RH`, `DIRECTION`, `ADMIN` (tout rôle métier **inclut** `USER`).
3. **DIRECTION** = back-office **lecture** + compteurs. Pas d’édition, pas d’approbation RRH, pas FIFO, pas campagnes/templates.
4. Changement de `profil_acces` **resynchronise** les rôles du compte lié (effet au plus tard au prochain login).
5. Plateforme RH : JWT `RH` | `DIRECTION` | `ADMIN` seulement.
6. JWT accès **1 h** conservé. Refresh **7 j** = Should. Cible CDC 15 min **non** traitée.
7. Hors vague : M03, M06, M08–M12 go-live, paie, chaud/froid, biométrie.

**Tranche orchestrateur (écart UX vs PO)** : le catalogue congés reste celui du PO/BA — **cinq** valeurs dont `AUTRE` (PJ non obligatoire). L’UX « 4 types sans Autre » est **rejetée**.

**Nuance BA à respecter** : le droit de **valider M01 / noter M07** vient du **nœud** (être le manager snapshot), pas du seul JWT `RO`. Le JWT `RO`/`RESPONSABLE` sert surtout à **afficher** la file mobile. Un USER manager de nœud peut valider ; un JWT `RO` d’un autre nœud → **403**.

---

## Matrice RBAC cible (extrait)

| Capacité | USER | RO / RESPONSABLE | RH | DIRECTION | ADMIN |
|---|---|---|---|---|---|
| SPA Plateforme RH | Non | Non | Oui | Oui (lecture) | Oui |
| Édition référentiel / organigramme | Non | Non | Oui | Non | Oui |
| Validation M01 1er niveau | Si manager nœud | Si manager nœud | Non* | Non | Non* |
| Approbation RRH / FIFO M02 | Non | Non | Oui | Non | Oui |
| M07 admin écriture | Non | Non | Oui | Non | Oui |
| M07 admin lecture | Non | Non | Oui | Oui | Oui |
| Voir / répondre à **son** évaluation | Oui | Oui | Oui | Oui | Oui |

\*RH/ADMIN traitent l’étape RRH, pas l’étape superieur (sauf s’ils sont aussi le manager du nœud).

Expressions Spring :

- `BACKOFFICE_LECTURE` = `hasAnyRole('RH','DIRECTION','ADMIN')`
- `BACKOFFICE_ECRITURE` = `hasAnyRole('RH','ADMIN')`  
  (remplace l’actuel `BACKOFFICE_RH` trop large sur les écritures)

---

## Stories (ordre d’implémentation)

| # | Priorité | Titre | Architecte | Backend | Web | Mobile |
|---|---|---|---|---|---|---|
| 1 | Must | Catalogue unique des rôles | Non | **Fait** | **Fait** | **Fait** |
| 2 | Must | Sync JWT ↔ `profil_acces` | Non (topic existant) | **Fait** | Non bloquant | Non bloquant |
| 4 | Must | ProtectedRoute + Direction lecture | Non | **Fait** | **Fait** | Non |
| 3 | Must | Valideur M01 = manager nœud + 403 | Non | **Fait** | Couplé | **Fait** |
| 5 | Must | M07 RBAC + anti-IDOR | Non | **Fait** | OK Direction lecture (S4) | À vérifier ownership |
| 7 | Should | Types congés fermés + PJ maladie/maternité | Non | **Fait** | Lecture RH | À faire |
| 6 | Should | Refresh token 7 j | **Oui** (stockage) | Après ADR | À faire | À faire |

Ordre : **1 → 2 → 4 → (3 ∥ 5) → 7 → 6**.

---

## UX (livré story 4)

- Nav web filtrée ; Direction : bandeau « Consultation uniquement » ; Structure / Unités **masqués**.
- Page **Accès refusé** ; login refuse les comptes hors RH|DIRECTION|ADMIN.
- Boutons d’écriture masqués (plaintes, docs FIFO, formations, collabs, organigramme, campagnes).

---

## Hors vague (Could)

M03 notes réelles, M09 présence, M12 KPI, M05 chaud/froid, PDF+S3, Whisper, J+30, solde congés, TTL accès 15 min, web RO.

---

## Suite

**Prochaine** : story **7** front (web lecture RH + mobile catalogue 5 types + PJ maternité). Story **6** après ADR architecte.  
Dette S5 : claim JWT `collaborateur_id` pour fermer le spoof `X-Collaborateur-Id`.  
Story 7 backend : `TypeConge` + validation serveur (`certificat` / `pieces_jointes`) — upload S3 multipart = cible.  
Mettre à jour `sirh_basefonctionnelle.md` **en même temps** que le code.
