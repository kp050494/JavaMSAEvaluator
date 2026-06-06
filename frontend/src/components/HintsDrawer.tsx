import { useState } from 'react';

interface Props {
  hints: string[];
  open: boolean;
  onClose: () => void;
}

/** Slide-out panel that reveals hints one at a time. */
export default function HintsDrawer({ hints, open, onClose }: Props) {
  const [revealed, setRevealed] = useState(1);

  return (
    <div
      className={`fixed inset-y-0 right-0 z-40 w-80 transform border-l border-default bg-bg-surface shadow-2xl transition-transform duration-300 ${
        open ? 'translate-x-0' : 'translate-x-full'
      }`}
    >
      <div className="flex items-center justify-between border-b border-default p-4">
        <h3 className="font-display text-sm font-bold text-accent-amber">HINTS</h3>
        <button className="btn btn-ghost px-2 py-1 text-xs" onClick={onClose}>
          ✕
        </button>
      </div>
      <div className="space-y-3 p-4">
        {hints.slice(0, revealed).map((h, i) => (
          <div
            key={i}
            className="animate-pop rounded-lg border border-accent-amber/30 bg-accent-amber/5 p-3 text-xs text-txt-primary"
          >
            <span className="mr-2 font-bold text-accent-amber">#{i + 1}</span>
            {h}
          </div>
        ))}
        {revealed < hints.length ? (
          <button
            className="btn btn-ghost w-full text-xs"
            onClick={() => setRevealed((r) => Math.min(r + 1, hints.length))}
          >
            Reveal next hint ({revealed}/{hints.length})
          </button>
        ) : (
          <p className="text-center text-[11px] text-txt-muted">All hints revealed.</p>
        )}
      </div>
    </div>
  );
}
