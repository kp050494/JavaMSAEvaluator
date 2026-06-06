export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export type SubmissionStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'ERROR';

export interface ChallengeSummary {
  id: number;
  slug: string;
  orderIndex: number;
  title: string;
  difficulty: Difficulty;
  category: string;
  totalTests: number;
  estimatedMinutes: number;
}

export interface ChallengeDetail extends ChallengeSummary {
  description: string;
  starterCode: string;
  concepts: string[];
  hints: string[];
  testCases: string[];
}

export interface TestResult {
  testName: string;
  passed: boolean;
  message: string | null;
}

export interface SubmissionResponse {
  submissionId: number;
  challengeId: number;
  status: SubmissionStatus;
  score: number;
  passed: number;
  total: number;
  elapsedSeconds: number | null;
  createdAt: string;
  results: TestResult[];
  logs: string[];
}

export interface AuthResponse {
  token: string;
  sessionId: string | null;
  name: string;
  role: string;
}

export interface SubmissionDto {
  id: number;
  challengeId: number;
  challengeTitle: string;
  category: string;
  difficulty: Difficulty;
  status: SubmissionStatus;
  score: number;
  passed: number;
  total: number;
  elapsedSeconds: number;
  createdAt: string;
  code: string;
  results: TestResult[];
}

export interface SessionDto {
  id: string;
  candidateName: string;
  email: string;
  status: string;
  totalScore: number;
  startedAt: string;
  completedAt: string | null;
  durationSeconds: number | null;
  challengesCompleted: number;
  submissions: SubmissionDto[];
}

export interface ChallengeScore {
  challengeId: number;
  title: string;
  category: string;
  difficulty: Difficulty;
  score: number;
  passed: number;
  total: number;
  attempted: boolean;
}

export interface SessionReport {
  sessionId: string;
  candidateName: string;
  email: string;
  status: string;
  startedAt: string;
  completedAt: string | null;
  totalTimeSeconds: number;
  totalScore: number;
  challengesAttempted: number;
  totalChallenges: number;
  totalTestsPassed: number;
  totalTests: number;
  passRate: number;
  challengeScores: ChallengeScore[];
  bestSubmissions: SubmissionDto[];
}

export interface RecruiterSessionSummary {
  id: string;
  candidateName: string;
  email: string;
  status: string;
  startedAt: string;
  completedAt: string | null;
  durationSeconds: number | null;
  challengesCompleted: number;
  totalChallenges: number;
  totalScore: number;
  averageScore: number;
}

export type ProgressStep =
  | 'CONNECTED'
  | 'COMPILING'
  | 'RUNNING_TESTS'
  | 'TEST_RESULT'
  | 'COMPLETE'
  | 'ERROR';

export interface ProgressMessage {
  step: ProgressStep;
  message?: string;
  submissionId?: number;
  testName?: string;
  passed?: boolean;
  score?: number;
  passedCount?: number;
  total?: number;
  timestamp?: string;
}
