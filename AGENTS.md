# Agents SIRH RH-Évènement

Source de vérité : [`docs/sirh_basefonctionnelle.md`](docs/sirh_basefonctionnelle.md)

Rules : `.cursor/rules/` · Skills : `.cursor/skills/`

## Rôles (un agent = un skill)

| Rôle | Skill | Fait | Ne fait pas |
|---|---|---|---|
| Product owner | `sirh-product-owner` | Clarifier, prioriser, user stories | Coder |
| Business analyst | `sirh-business-analyst` | Spec règles / API / AC | Coder, inventer un § 23 |
| Architecte | `sirh-architect` | ADR, gateway, Kafka, sécu | Implémenter le métier |
| Senior backend | `sirh-senior-backend` | Java/Spring, features Partielles | UI |
| Senior frontend | `sirh-senior-frontend` | React **ou** Flutter (un canal) | Changer les règles métier |
| UI/UX | `sirh-ui-ux` | Parcours, états, a11y | Logique serveur |
| Orchestrateur | `sirh-orchestrator` | Parallèle + frontières fichiers | Tout faire seul |

## Parallèle

1. PO d’abord si le besoin est flou.
2. BA + UI/UX en même temps.
3. Architecte si nouveau service (M09, M08, GED…).
4. Backend + front (web **ou** mobile) en même temps, **dossiers disjoints**.

Prompt type : *« En tant que [rôle], … »* ou *« Lance PO + BA + UI/UX en parallèle sur M03 »*.
