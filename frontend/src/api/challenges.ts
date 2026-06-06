import client from './client';
import type { ChallengeDetail, ChallengeSummary } from '../types';

export async function listChallenges(): Promise<ChallengeSummary[]> {
  const { data } = await client.get<ChallengeSummary[]>('/api/challenges');
  return data;
}

export async function getChallenge(id: number): Promise<ChallengeDetail> {
  const { data } = await client.get<ChallengeDetail>(`/api/challenges/${id}`);
  return data;
}
