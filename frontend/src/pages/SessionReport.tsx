import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSessionReport } from '../api/sessions';
import { useSession } from '../store/sessionStore';
import { formatDuration } from '../hooks/useTimer';
import type { SessionReport as Report } from '../types';
import { ScoreBars, ScoreRadar } from '../components/ReportChart';
import ScoreCard from '../components/ScoreCard';
import CodeEditor from '../components/CodeEditor';
import DifficultyBadge from '../components/DifficultyBadge';

export default function SessionReport() {
  const navigate = useNavigate();
  const { candidate, logoutCandidate } = useSession();
  const [report, setReport] = useState<Report | null>(null);
  const [openCode, setOpenCode] = useState<number | null>(null);

  useEffect(() => {
    if (candidate?.sessionId) {
      getSessionReport(candidate.sessionId).then(setReport);
    }
  }, [candidate]);

  if (!report) {
    return <div className="grid-bg flex min-h-screen items-center justify-center text-txt-secondary">Loading report…</div>;
  }

  return (
    <div className="grid-bg min-h-screen px-6 py-8">
      <div className="mx-auto max-w-5xl space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="font-display text-2xl font-bold text-accent-blue">Assessment Report</h1>
            <p className="text-xs text-txt-secondary">
              {report.candidateName} · {report.email}
            </p>
          </div>
          <button
            className="btn btn-ghost text-xs"
            onClick={() => {
              logoutCandidate();
              navigate('/');
            }}
          >
            Back to Start
          </button>
        </div>

        {/* Summary */}
        <div className="card flex flex-wrap items-center justify-around gap-4 p-6">
          <ScoreCard score={report.totalScore} label="Overall" />
          <Stat label="Challenges" value={`${report.challengesAttempted}/${report.totalChallenges}`} />
          <Stat label="Tests passed" value={`${report.totalTestsPassed}/${report.totalTests}`} />
          <Stat label="Pass rate" value={`${report.passRate}%`} />
          <Stat label="Total time" value={formatDuration(report.totalTimeSeconds)} />
        </div>

        {/* Charts */}
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div className="card p-4">
            <h3 className="mb-2 text-xs uppercase tracking-wider text-txt-secondary">Category Coverage</h3>
            <ScoreRadar scores={report.challengeScores} />
          </div>
          <div className="card p-4">
            <h3 className="mb-2 text-xs uppercase tracking-wider text-txt-secondary">Score per Challenge</h3>
            <ScoreBars scores={report.challengeScores} />
          </div>
        </div>

        {/* Per-challenge breakdown + code */}
        <div className="card divide-y divide-[rgba(59,130,246,0.1)]">
          {report.challengeScores.map((cs) => {
            const submission = report.bestSubmissions.find((b) => b.challengeId === cs.challengeId);
            const open = openCode === cs.challengeId;
            return (
              <div key={cs.challengeId} className="p-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <DifficultyBadge difficulty={cs.difficulty} />
                    <span className="text-sm text-txt-primary">{cs.title}</span>
                    <span className="text-[11px] text-txt-muted">({cs.category})</span>
                  </div>
                  <div className="flex items-center gap-3 text-xs">
                    <span className={cs.attempted ? 'text-txt-secondary' : 'text-txt-muted'}>
                      {cs.passed}/{cs.total} tests
                    </span>
                    <ScoreCard score={cs.score} size="sm" />
                    {submission && (
                      <button
                        className="btn btn-ghost text-xs"
                        onClick={() => setOpenCode(open ? null : cs.challengeId)}
                      >
                        {open ? 'Hide code' : 'View code'}
                      </button>
                    )}
                  </div>
                </div>
                {open && submission && (
                  <div className="mt-3 h-64 overflow-hidden rounded-lg border border-default">
                    <CodeEditor value={submission.code} onChange={() => {}} readOnly />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="text-center">
      <div className="font-display text-xl font-bold text-txt-primary">{value}</div>
      <div className="text-[11px] uppercase tracking-wider text-txt-secondary">{label}</div>
    </div>
  );
}
