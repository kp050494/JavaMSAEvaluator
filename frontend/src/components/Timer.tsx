import { formatDuration } from '../hooks/useTimer';

interface Props {
  seconds: number;
  running: boolean;
  estimatedMinutes: number;
  onToggle: () => void;
}

/** Large count-up timer that shifts colour as it approaches the estimate. */
export default function Timer({ seconds, running, estimatedMinutes, onToggle }: Props) {
  const estimate = estimatedMinutes * 60;
  let color = 'text-accent-green';
  if (estimate > 0) {
    const ratio = seconds / estimate;
    if (ratio >= 1) color = 'text-accent-red';
    else if (ratio >= 0.7) color = 'text-accent-amber';
  }

  return (
    <div className="flex items-center gap-3">
      <span className={`font-display text-2xl font-bold tabular-nums ${color}`}>
        {formatDuration(seconds)}
      </span>
      <button className="btn btn-ghost px-2 py-1 text-xs" onClick={onToggle}>
        {running ? 'Pause' : 'Start'}
      </button>
      <span className="text-[11px] text-txt-muted">est. {estimatedMinutes}m</span>
    </div>
  );
}
