import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listRecruiterSessions } from '../api/recruiter';
import { useSession } from '../store/sessionStore';
import { formatDuration } from '../hooks/useTimer';
import type { RecruiterSessionSummary } from '../types';
import { DistributionBars } from '../components/ReportChart';

export default function RecruiterDashboard() {
  const navigate = useNavigate();
  const { logoutRecruiter } = useSession();
  const [sessions, setSessions] = useState<RecruiterSessionSummary[]>([]);
  const [query, setQuery] = useState('');

  useEffect(() => {
    listRecruiterSessions().then(setSessions);
  }, []);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return sessions;
    return sessions.filter(
      (s) => s.candidateName.toLowerCase().includes(q) || s.email.toLowerCase().includes(q),
    );
  }, [sessions, query]);

  const distribution = useMemo(
    () => sessions.map((s) => ({ name: s.candidateName, score: s.totalScore })),
    [sessions],
  );

  return (
    <div className="grid-bg min-h-screen px-6 py-8">
      <div className="mx-auto max-w-6xl space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="font-display text-2xl font-bold text-accent-blue">Recruiter Dashboard</h1>
          <button
            className="btn btn-ghost text-xs"
            onClick={() => {
              logoutRecruiter();
              navigate('/');
            }}
          >
            Sign out
          </button>
        </div>

        {sessions.length > 0 && (
          <div className="card p-4">
            <h3 className="mb-2 text-xs uppercase tracking-wider text-txt-secondary">
              Score Distribution
            </h3>
            <DistributionBars data={distribution} />
          </div>
        )}

        <div className="card overflow-hidden">
          <div className="flex items-center justify-between border-b border-default p-3">
            <span className="text-sm text-txt-secondary">{filtered.length} sessions</span>
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search name or email…"
              className="w-64 rounded-lg border border-default bg-bg-elevated px-3 py-1.5 text-xs text-txt-primary outline-none focus:border-focus"
            />
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-bg-elevated text-txt-secondary">
                <tr>
                  <Th>Candidate</Th>
                  <Th>Email</Th>
                  <Th>Started</Th>
                  <Th>Duration</Th>
                  <Th>Completed</Th>
                  <Th>Avg score</Th>
                  <Th>Status</Th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((s) => (
                  <tr
                    key={s.id}
                    onClick={() => navigate(`/recruiter/sessions/${s.id}`)}
                    className="cursor-pointer border-t border-default hover:bg-bg-elevated/60"
                  >
                    <Td className="text-txt-primary">{s.candidateName}</Td>
                    <Td>{s.email}</Td>
                    <Td>{new Date(s.startedAt).toLocaleString()}</Td>
                    <Td>{s.durationSeconds != null ? formatDuration(s.durationSeconds) : '—'}</Td>
                    <Td>
                      {s.challengesCompleted}/{s.totalChallenges}
                    </Td>
                    <Td>{s.averageScore}%</Td>
                    <Td>
                      <StatusBadge status={s.status} />
                    </Td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr>
                    <td colSpan={7} className="p-6 text-center text-txt-muted">
                      No sessions yet.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

function Th({ children }: { children: React.ReactNode }) {
  return <th className="px-3 py-2 font-medium uppercase tracking-wider">{children}</th>;
}
function Td({ children, className }: { children: React.ReactNode; className?: string }) {
  return <td className={`px-3 py-2 text-txt-secondary ${className ?? ''}`}>{children}</td>;
}
function StatusBadge({ status }: { status: string }) {
  const done = status === 'COMPLETED';
  return (
    <span
      className={`rounded-md border px-2 py-0.5 text-[10px] ${
        done
          ? 'border-accent-green/40 bg-accent-green/10 text-accent-green'
          : 'border-accent-amber/40 bg-accent-amber/10 text-accent-amber'
      }`}
    >
      {status}
    </span>
  );
}
