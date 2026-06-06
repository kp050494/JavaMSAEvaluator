import client from './client';
import type { SubmissionResponse } from '../types';

export interface SubmitCodePayload {
  challengeId: number;
  code: string;
  sessionId: string;
  elapsedSeconds: number;
}

export async function submitCode(payload: SubmitCodePayload): Promise<SubmissionResponse> {
  const { data } = await client.post<SubmissionResponse>('/api/submissions', payload);
  return data;
}

export async function getSubmission(id: number): Promise<SubmissionResponse> {
  const { data } = await client.get<SubmissionResponse>(`/api/submissions/${id}`);
  return data;
}
