import axios from 'axios';
import { getCandidateAuth, getRecruiterAuth } from '../store/authStorage';

/**
 * Shared axios instance. The interceptor attaches the right JWT based on the
 * target path: recruiter endpoints use the recruiter token, everything else
 * uses the candidate session token.
 */
export const client = axios.create({
  baseURL: '/',
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
