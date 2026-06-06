import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { exportSessionJson, getRecruiterSession } from '../api/recruiter';
import { getChallenge } from '../api/challenges';
import { formatDuration } from '../hooks/useTimer';
import type { ChallengeDetail, SessionDto } from '../types';
import CodeEditor from '../components/CodeEditor';
import DifficultyBadge from '../components/DifficultyBadge';
import ScoreCard from '../components/ScoreCard';

export default function RecruiterSession() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [session, setSession] = useState<SessionDto | null>(null);
  const [descriptions, setDescriptions] = useState<Record<number, string>>({});

  useEffect(() => {
    if (!id) return;
    getRecruiterSession(id).then(async (s) => {
      setSession(s);
      const uniqueIds = [...new Set(s.submissions.map((sub) => sub.challengeId))];
      const details = await Promise.all(uniqueIds.map((cid) => getChallenge(cid).catch(() => null)));
      const map: Record<number, string> = {};
      details.forEach((d: ChallengeDetail | null) => {
        if (d) map[d.id] = d.description;
      });
      setDescriptions(map);
    });
  }, [id]);

  async function download() {
    if (!id) return;
    const json = await exportSessionJson(id);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `session-${id}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }

  if (!session) {
    return <div className="grid-bg flex min-h-screen items-center justify-center text-txt-secondary">Loading…</div>;
  }

  return (
    <div className="grid-bg min-h-screen px-6 py-8">
      <div className="mx-auto max-w-6xl space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <button className="mb-1 text-xs text-accent-blue hover:underline" onClick={() => navigate('/recruiter/dashboard')}>
              ← Back to dashboard
            </button>
            <h1 className="font-display text-2xl font-bold text-txt-primary">{session.candidateName}</h1>
            <p className="text-xs text-txt-secondary">{session.email}</p>
          </div>
          <div className="flex items-center gap-4">
            <ScoreCard score={session.totalScore} label="Overall" size="sm" />
            <button className="btn btn-primary text-xs" onClick={() => void download()}>
              Export JSON
            </button>
          </div>
        </div>

        {/* Timeline */}
        <div className="card p-4">
          <h3 className="mb-2 text-xs uppercase tracking-wider text-txt-secondary">Submission Timeline</h3>
          <ol className="space-y-1 text-xs">
            {session.submissions.map((sub) => (
              <li key={sub.id} className="flex items-center gap-3 text-txt-secondary">
                <span className="text-txt-muted">{new Date(sub.createdAt).toLocaleTimeString()}</span>
                <span className="text-txt-primary">{sub.challengeTitle}</span>
                <span>·</span>
                <span>{sub.status}</span>
                <span>·</span>
                <span>
                  {sub.passed}/{sub.total} ({sub.score}%)
                </span>
                <span className="text-txt-muted">· {formatDuration(sub.elapsedSeconds)}</span>
              </li>
            ))}
            {session.submissions.length === 0 && <li className="text-txt-muted">No submissions.</li>}
          </ol>
        </div>

        {/* Per-submission detail */}
        {session.submissions.map((sub) => (
          <div key={sub.id} className="card p-4">
            <div className="mb-3 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <DifficultyBadge difficulty={sub.difficulty} />
                <span className="text-sm font-semibold text-txt-primary">{sub.challengeTitle}</span>
                <span className="text-[11px] text-txt-muted">({sub.category})</span>
              </div>
              <ScoreCard score={sub.score} size="sm" />
            </div>
            <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
              <div className="space-y-3">
                <div>
                  <h4 className="mb-1 text-[11px] uppercase tracking-wider text-txt-secondary">Description</h4>
                  <p className="max-h-40 overflow-y-auto whitespace-pre-wrap text-xs text-txt-secondary">
                    {descriptions[sub.challengeId] ?? '—'}
                  </p>
                </div>
                <div>
                  <h4 className="mb-1 text-[11px] uppercase tracking-wider text-txt-secondary">Test Results</h4>
                  <ul className="space-y-1 text-xs">
                    {sub.results.map((r, i) => (
                      <li key={i} className="flex items-start gap-2">
                        <span className={r.passed ? 'text-accent-green' : 'text-accent-red'}>
                          {r.passed ? '✔' : '✕'}
                        </span>
                        <span className="text-txt-secondary">{r.testName}</span>
                      </li>
                    ))}
                    {sub.results.length === 0 && <li className="text-txt-muted">No parsed results.</li>}
                  </ul>
                </div>
              </div>
              <div className="h-72 overflow-hidden rounded-lg border border-default">
                <CodeEditor value={sub.code} onChange={() => {}} readOnly />
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
