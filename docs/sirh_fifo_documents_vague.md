# Vague FIFO documents (M02) — décision PO / chef de projet

| | |
|---|---|
| **Date** | 13 août 2026 |
| **Module** | M02 — Documents administratifs |
| **Contrainte** | **C-M02-01** (FIFO) |
| **Statut** | Décision verrouillée · correctif Must |

## 1. Attente métier (confirmée)

1. Le collaborateur dépose une demande de document → elle entre en **file unique**.
2. La RH traite **dans l’ordre d’arrivée** : 1ʳᵉ créée = 1ʳᵉ traitée, puis 2ᵉ, etc.
3. La RH **peut** traiter hors ordre (**exception**), mais **doit** saisir une **raison obligatoire** (tracée + notif DRH si prévu).
4. Sans raison → **blocage** (pas de saut silencieux).

## 2. Diagnostic

| Élément | État |
|---|---|
| Règle produit § 7.3 | Déjà correcte |
| UI web (rang, bandeau, champ justification) | Présente |
| Backend `prendre-prochaine` | Prend bien la plus ancienne |
| **Bug** | Après `prendre-prochaine`, la 1ʳᵉ passe en `EN_TRAITEMENT_RH`. Au moment de valider/rejeter, le contrôle FIFO ne regarde que `EN_ATTENTE_FILE` → la 2ᵉ apparaît « plus ancienne » → **justification exigée à tort** pour terminer la 1ʳᵉ |

Conséquence terrain : le FIFO « ne marche pas comme il faut » (friction / contournements).

## 3. Décisions PO

| ID | Décision |
|---|---|
| **D-F01** | File unique chronologique (`cree_le` ASC). Pas de files par type en Must. |
| **D-F02** | Chemin normal : `prendre-prochaine` → traiter → clôturer **sans** justification. |
| **D-F03** | Exception : traiter une demande `EN_ATTENTE_FILE` qui n’est **pas** la plus ancienne → `justification_derogation_fifo` obligatoire (min. 10 caractères utiles), acteur RH tracé. |
| **D-F04** | Une demande déjà `EN_TRAITEMENT_RH` (prise légitimement) se clôture **sans** re-demander une dérogation. |
| **D-F05** | Direction = lecture seule (inchangé). |

## 4. Story F1 — Correctif FIFO

**Priorité** : Must | **Prêt BA/dev** : oui

### User story
En tant que RRH, je veux terminer la demande que j’ai prise en tête de file sans justification, et ne devoir justifier que les vrais sauts de file, afin de respecter le FIFO métier.

### AC
1. Given A puis B en file, When RH prend prochaine, Then A = `EN_TRAITEMENT_RH` et est traitable sans justification.
2. Given A en traitement et B en attente, When RH valide A sans justification, Then OK.
3. Given A et B en attente, When RH valide B sans justification, Then erreur métier (justification requise).
4. Given A et B en attente, When RH valide B avec justification, Then OK + justification tracée.
5. Given une demande en traitement, When RH tente d’en prendre une autre, Then bloqué jusqu’à clôture.

### Out
Upload PDF réel / S3, multi-files par type, priorisation automatique SLA.

## 5. Handoff
Backend `DemandeDocumentAdministratifRhService.validerOrdreFifo` + tests · Web : vérifier que le bandeau dérogation ne s’affiche que pour hors-ordre réel.
