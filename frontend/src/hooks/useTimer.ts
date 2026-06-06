import { useCallback, useEffect, useRef, useState } from 'react';

interface UseTimerResult {
  seconds: number;
  running: boolean;
  start: () => void;
  pause: () => void;
  reset: () => void;
  toggle: () => void;
}

/**
 * Simple count-up timer (elapsed seconds). Used to record time-on-challenge and
 * drive the colour-state Timer component.
 */
export function useTimer(autoStart = true): UseTimerResult {
  const [seconds, setSeconds] = useState(0);
  const [running, setRunning] = useState(autoStart);
  const intervalRef = useRef<number | null>(null);

  useEffect(() => {
    if (running) {
      intervalRef.current = window.setInterval(() => setSeconds((s) => s + 1), 1000);
    }
    return () => {
      if (intervalRef.current !== null) {
        window.clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };
  }, [running]);

  const start = useCallback(() => setRunning(true), []);
  const pause = useCallback(() => setRunning(false), []);
  const toggle = useCallback(() => setRunning((r) => !r), []);
  const reset = useCallback(() => {
    setSeconds(0);
    setRunning(false);
  }, []);

  return { seconds, running, start, pause, reset, toggle };
}

export function formatDuration(totalSeconds: number): string {
  const m = Math.floor(totalSeconds / 60)
    .toString()
    .padStart(2, '0');
  const s = Math.floor(totalSeconds % 60)
    .toString()
    .padStart(2, '0');
  return `${m}:${s}`;
}
