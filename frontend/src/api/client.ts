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
