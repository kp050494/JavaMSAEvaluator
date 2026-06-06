import { useCallback, useEffect, useRef, useState } from 'react';

const KEY_PREFIX = 'spring-arena.code.';

function key(sessionId: string, challengeId: number) {
  return `${KEY_PREFIX}${sessionId}.${challengeId}`;
}

/**
 * Persists the candidate's code per (session, challenge) in localStorage,
 * auto-saving every 30s and exposing a manual save.
 */
export function useCodeSnapshot(
  sessionId: string | undefined,
  challengeId: number | undefined,
  fallback: string,
) {
  const [code, setCode] = useState(fallback);
  const codeRef = useRef(code);
  codeRef.current = code;

  // Load the saved snapshot (or fall back to the starter code) when switching challenge.
  useEffect(() => {
    if (!sessionId || challengeId === undefined) {
      setCode(fallback);
      return;
    }
    const saved = localStorage.getItem(key(sessionId, challengeId));
    setCode(saved ?? fallback);
  }, [sessionId, challengeId, fallback]);

  const save = useCallback(() => {
    if (!sessionId || challengeId === undefined) {
      return;
    }
    localStorage.setItem(key(sessionId, challengeId), codeRef.current);
  }, [sessionId, challengeId]);

  // Auto-save every 30 seconds.
  useEffect(() => {
    const id = window.setInterval(save, 30_000);
    return () => window.clearInterval(id);
  }, [save]);

  // Persist on unmount / challenge switch.
  useEffect(() => save, [save]);

  return { code, setCode, save };
}
