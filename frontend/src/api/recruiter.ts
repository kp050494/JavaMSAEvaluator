import client from './client';
import type { RecruiterSessionSummary, SessionDto } from '../types';

export async function listRecruiterSessions(): Promise<RecruiterSessionSummary[]> {
  const { data } = await client.get<RecruiterSessionSummary[]>('/api/recruiter/sessions');
  return data;
}

export async function getRecruiterSession(id: string): Promise<SessionDto> {
  const { data } = await client.get<SessionDto>(`/api/recruiter/sessions/${id}`);
  return data;
}

export async function exportSessionJson(id: string): Promise<string> {
  const { data } = await client.get(`/api/recruiter/sessions/${id}/export`, {
    responseType: 'text',
    transformResponse: (raw) => raw,
  });
  return data as string;
}
