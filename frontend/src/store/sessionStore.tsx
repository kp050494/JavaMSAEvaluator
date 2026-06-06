import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import {
  CandidateAuth,
  RecruiterAuth,
  clearCandidateAuth,
  clearRecruiterAuth,
  getCandidateAuth,
  getRecruiterAuth,
  setCandidateAuth,
  setRecruiterAuth,
} from './authStorage';

interface SessionContextValue {
  candidate: CandidateAuth | null;
  recruiter: RecruiterAuth | null;
  loginCandidate: (auth: CandidateAuth) => void;
  logoutCandidate: () => void;
  loginRecruiter: (auth: RecruiterAuth) => void;
  logoutRecruiter: () => void;
}

const SessionContext = createContext<SessionContextValue | undefined>(undefined);

export function SessionProvider({ children }: { children: ReactNode }) {
  const [candidate, setCandidate] = useState<CandidateAuth | null>(() => getCandidateAuth());
  const [recruiter, setRecruiter] = useState<RecruiterAuth | null>(() => getRecruiterAuth());

  const loginCandidate = useCallback((auth: CandidateAuth) => {
    setCandidateAuth(auth);
    setCandidate(auth);
  }, []);

  const logoutCandidate = useCallback(() => {
    clearCandidateAuth();
    setCandidate(null);
  }, []);

  const loginRecruiter = useCallback((auth: RecruiterAuth) => {
    setRecruiterAuth(auth);
    setRecruiter(auth);
  }, []);

  const logoutRecruiter = useCallback(() => {
    clearRecruiterAuth();
    setRecruiter(null);
  }, []);

  const value = useMemo(
    () => ({ candidate, recruiter, loginCandidate, logoutCandidate, loginRecruiter, logoutRecruiter }),
    [candidate, recruiter, loginCandidate, logoutCandidate, loginRecruiter, logoutRecruiter],
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession() {
  const ctx = useContext(SessionContext);
  if (!ctx) {
    throw new Error('useSession must be used within a SessionProvider');
  }
  return ctx;
}
