interface Props {
  score: number;
  label?: string;
  size?: 'sm' | 'lg';
}

function colorFor(score: number): string {
  if (score >= 80) return 'text-accent-green border-accent-green/40 bg-accent-green/10';
  if (score >= 50) return 'text-accent-amber border-accent-amber/40 bg-accent-amber/10';
  return 'text-accent-red border-accent-red/40 bg-accent-red/10';
}

/** Score badge whose colour reflects the value. */
export default function ScoreCard({ score, label, size = 'lg' }: Props) {
  const dims = size === 'lg' ? 'h-24 w-24 text-3xl' : 'h-14 w-14 text-lg';
  return (
    <div className="flex flex-col items-center gap-1">
      <div
        className={`flex items-center justify-center rounded-full border font-display font-bold ${dims} ${colorFor(
          score,
        )}`}
      >
        {score}
      </div>
      {label && <span className="text-[11px] uppercase tracking-wider text-txt-secondary">{label}</span>}
    </div>
  );
}
