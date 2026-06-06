import { useEffect, useRef } from 'react';
import type { LogLine } from '../hooks/useWebSocket';

const COLORS: Record<LogLine['level'], string> = {
  info: 'text-accent-blue',
  success: 'text-accent-green',
  error: 'text-accent-red',
  warn: 'text-accent-amber',
  muted: 'text-txt-muted',
};

interface Props {
  logs: LogLine[];
  isConnected: boolean;
}

/** Streaming terminal that auto-scrolls to the latest line. */
export default function AnalysisLog({ logs, isConnected }: Props) {
  const endRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [logs]);

  return (
    <div className="flex h-full flex-col overflow-hidden rounded-lg border border-default">
      <div className="flex items-center justify-between border-b border-default bg-bg-elevated px-3 py-1.5">
        <span className="text-[11px] uppercase tracking-wider text-txt-secondary">Analysis Log</span>
        <span className="flex items-center gap-1.5 text-[11px] text-txt-muted">
          <span
            className={`inline-block h-2 w-2 rounded-full ${
              isConnected ? 'bg-accent-green' : 'bg-accent-red'
            }`}
          />
          {isConnected ? 'connected' : 'offline'}
        </span>
      </div>
      <div
        className="flex-1 overflow-y-auto px-3 py-2 font-mono text-xs leading-relaxed"
        style={{ backgroundColor: '#080d1a' }}
      >
        {logs.length === 0 && (
          <p className="text-txt-muted">$ waiting for a test run…</p>
        )}
        {logs.map((line, i) => (
          <div key={i} className={COLORS[line.level]}>
            <span className="text-txt-muted">[{line.ts}]</span> {line.text}
          </div>
        ))}
        <div ref={endRef} />
      </div>
    </div>
  );
}
