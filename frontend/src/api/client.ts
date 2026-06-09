import axios from 'axios';
import { getCandidateAuth, getRecruiterAuth } from '../store/authStorage';

/**
 * Shared axios instance. The interceptor attaches the right JWT based on the
 * target path: recruiter endpoints use the recruiter token, everything else
 * uses the candidate session token.
 */
// In dev this stays '/' (Vite proxies /api to :8080). In a split deploy
// (frontend on Vercel, backend on Render) set VITE_API_URL to the backend origin.
export const API_BASE_URL = import.meta.env.VITE_API_URL ?? '';

export const client = axios.create({
  baseURL: API_BASE_URL || '/',
  timeout: 130_000, // Judge0 Maven builds can take ~60-90s
});

/**
 * Fire-and-forget ping to wake a sleeping free-tier backend (Render spins the
 * service down after ~15 min idle; the first request then takes ~50s). Calling
 * this on page load means the backend is usually awake by the time the user acts.
 */
export function warmUpBackend(): void {
  client.get('/api/health', { timeout: 120_000 }).catch(() => {
    /* ignore — this is best-effort */
  });
}

client.interceptors.request.use((config) => {
  const url = config.url ?? '';
  const isRecruiter = url.includes('/api/recruiter');
  const auth = isRecruiter ? getRecruiterAuth() : getCandidateAuth();
  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

export default client;
