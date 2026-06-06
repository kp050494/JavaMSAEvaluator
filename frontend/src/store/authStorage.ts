// Plain (non-React) persistence for auth tokens, shared by the axios client and
// the React context so both stay in sync via localStorage.

export interface CandidateAuth {
  token: string;
  sessionId: string;
  name: string;
}

export interface RecruiterAuth {
  token: string;
  name: string;
}

const CANDIDATE_KEY = 'spring-arena.candidate';
const RECRUITER_KEY = 'spring-arena.recruiter';

function read<T>(key: string): T | null {
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : null;
  } catch {
    return null;
  }
}

export const getCandidateAuth = () => read<CandidateAuth>(CANDIDATE_KEY);
export const getRecruiterAuth = () => read<RecruiterAuth>(RECRUITER_KEY);

export function setCandidateAuth(auth: CandidateAuth) {
  localStorage.setItem(CANDIDATE_KEY, JSON.stringify(auth));
}

export function setRecruiterAuth(auth: RecruiterAuth) {
  localStorage.setItem(RECRUITER_KEY, JSON.stringify(auth));
}

export function clearCandidateAuth() {
  localStorage.removeItem(CANDIDATE_KEY);
}

export function clearRecruiterAuth() {
  localStorage.removeItem(RECRUITER_KEY);
}
