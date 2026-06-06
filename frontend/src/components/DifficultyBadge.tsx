import type { Difficulty } from '../types';

const STYLES: Record<Difficulty, string> = {
  EASY: 'text-difficulty-easy border-difficulty-easy/40 bg-difficulty-easy/10',
  MEDIUM: 'text-difficulty-medium border-difficulty-medium/40 bg-difficulty-medium/10',
  HARD: 'text-difficulty-hard border-difficulty-hard/40 bg-difficulty-hard/10',
};

export default function DifficultyBadge({ difficulty }: { difficulty: Difficulty }) {
  return (
    <span
      className={`inline-block rounded-md border px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wider ${STYLES[difficulty]}`}
    >
      {difficulty}
    </span>
  );
}
