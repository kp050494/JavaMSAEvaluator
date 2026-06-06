import type { ChallengeSummary } from '../types';
import DifficultyBadge from './DifficultyBadge';

interface Props {
  challenges: ChallengeSummary[];
  activeId: number | undefined;
  submittedIds: Set<number>;
  onSelect: (id: number) => void;
}

/** Tab strip across the six challenges with difficulty + submitted state. */
export default function ChallengeNav({ challenges, activeId, submittedIds, onSelect }: Props) {
  return (
    <div className="flex gap-2 overflow-x-auto pb-1">
      {challenges.map((c, idx) => {
        const active = c.id === activeId;
        const submitted = submittedIds.has(c.id);
        return (
          <button
            key={c.id}
            onClick={() => onSelect(c.id)}
            className={`flex min-w-[150px] flex-col gap-1 rounded-lg border px-3 py-2 text-left transition ${
              active
                ? 'border-focus bg-bg-elevated'
                : 'border-default bg-bg-surface hover:border-focus'
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="text-[11px] text-txt-muted">Challenge {idx + 1}</span>
              {submitted && <span className="text-accent-green">✔</span>}
            </div>
            <span className="truncate text-xs font-semibold text-txt-primary">{c.title}</span>
            <DifficultyBadge difficulty={c.difficulty} />
          </button>
        );
      })}
    </div>
  );
}
