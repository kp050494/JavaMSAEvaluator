import client from './client';
import type { SessionDto, SessionReport } from '../types';

export async function getSession(sessionId: string): Promise<SessionDto> {
  const { data } = await client.get<SessionDto>(`/api/sessions/${sessionId}`);
  return data;
}

export async function getSessionReport(sessionId: string): Promise<SessionReport> {
  const { data } = await client.get<SessionReport>(`/api/sessions/${sessionId}/report`);
  return data;
}

export async function completeSession(sessionId: string): Promise<SessionReport> {
  const { data } = await client.post<SessionReport>(`/api/sessions/${sessionId}/complete`);
  return data;
}
