import client from './client';
import type { AuthResponse } from '../types';

export async function candidateLogin(name: string, email: string): Promise<AuthResponse> {
  const { data } = await client.post<AuthResponse>('/api/auth/login', { name, email });
  return data;
}

export async function recruiterLogin(username: string, password: string): Promise<AuthResponse> {
  const { data } = await client.post<AuthResponse>('/api/auth/recruiter/login', { username, password });
  return data;
}
