import {
  ALERTE_META,
  DISCREPANCY_FR,
  type CouleurAlerte,
} from '../api/evaluationCatalog';

interface EvalAlertBadgeProps {
  couleur?: CouleurAlerte | null;
  compact?: boolean;
}

/** Pastille VERT / ORANGE / ROUGE § 12.5 — toujours libellé + icône (WCAG). */
export function EvalAlertBadge({ couleur, compact = false }: EvalAlertBadgeProps) {
  if (!couleur) {
    return <span className="text-muted" title="Couleur d’alerte non renvoyée par l’API">—</span>;
  }
  const meta = ALERTE_META[couleur];
  return (
    <span
      className={`badge ${meta.className}`}
      title={meta.label}
      aria-label={`${couleur} : ${meta.label}`}
    >
      <span aria-hidden="true">{meta.icon}</span>
      {' '}
      {compact ? couleur : meta.label}
    </span>
  );
}

interface DiscrepancyLabelProps {
  severity: string;
}

export function DiscrepancyLabel({ severity }: DiscrepancyLabelProps) {
  const meta = DISCREPANCY_FR[severity] ?? { label: severity, icon: '·' };
  return (
    <span className={meta.badge ? `badge ${meta.badge}` : undefined} title={meta.label}>
      <span aria-hidden="true">{meta.icon}</span> {meta.label}
    </span>
  );
}
