import { useCallback, useRef } from 'react';

/**
 * Returns a stable function identity that always invokes the latest callback.
 * Lets effects depend on a handler without re-running when the closure changes.
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function useCallbackRef<T extends (...args: any[]) => any>(callback: T): T {
  const ref = useRef(callback);
  ref.current = callback;
  // eslint-disable-next-line react-hooks/exhaustive-deps
  return useCallback(((...args: unknown[]) => ref.current(...args)) as T, []);
}
