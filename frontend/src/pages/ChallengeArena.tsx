import { useCallbackRef } from '../hooks/useCallbackRef';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getChallenge, listChallenges } from '../api/challenges';
import { submitCode } from '../api/submissions';
import { completeSession } from '../api/sessions';
import { useSession } from '../store/sessionStore';
import { useWebSocket } from '../hooks/useWebSocket';
import { useTimer } from '../hooks/useTimer';
import { useCodeSnapshot } from '../hooks/useCodeSnapshot';
import type { ChallengeDetail, ChallengeSummary } from '../types';
import CodeEditor from '../components/CodeEditor';
import ChallengeNav from '../components/ChallengeNav';
import TestResultPanel from '../components/TestResultPanel';
import AnalysisLog from '../components/AnalysisLog';
import Timer from '../components/Timer';
import HintsDrawer from '../components/HintsDrawer';
import ConceptsTags from '../components/ConceptsTags';
import DifficultyBadge from '../components/DifficultyBadge';

export default function ChallengeArena() {
  const navigate = useNavigate();
  const { candidate, logoutCandidate } = useSession();
  const sessionId = candidate?.sessionId;

  const [challenges, setChallenges] = useState<ChallengeSummary[]>([]);
  const [activeId, setActiveId] = useState<number | undefined>(undefined);
  const [detail, setDetail] = useState<ChallengeDetail | null>(null);
  const [submittedIds, setSubmittedIds] = useState<Set<number>>(new Set());
  const [hintsOpen, setHintsOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [finishing, setFinishing] = useState(false);

  const ws = useWebSocket(sessionId);
  const timer = useTimer(true);
  const snapshot = useCodeSnapshot(sessionId, activeId, detail?.starterCode ?? '');

  // Load challenge list once.
  useEffect(() => {
    listChallenges().then((list) => {
      setChallenges(list);
      if (list.length > 0) setActiveId(list[0].id);
    });
  }, []);

  // Load active challenge detail + reset the timer for the new challenge.
  useEffect(() => {
    if (activeId === undefined) return;
    getChallenge(activeId).then(setDetail);
    timer.reset();
    timer.start();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeId]);

  // Mark a challenge submitted when a run completes.
  useEffect(() => {
    if (ws.completion && activeId !== undefined) {
      setSubmittedIds((prev) => new Set(prev).add(activeId));
    }
  }, [ws.completion, activeId]);

  const runTests = useCallbackRef(async () => {
    if (!sessionId || activeId === undefined || submitting || ws.isRunning) return;
    snapshot.save();
    setSubmitting(true);
    ws.startRun();
    try {
      const resp = await submitCode({
        challengeId: activeId,
        code: snapshot.code,
        sessionId,
        elapsedSeconds: timer.seconds,
      });
      // Poll for the result as a fallback in case the WebSocket stream is unavailable.
      ws.trackSubmission(resp.submissionId);
    } catch {
      // surfaced via the analysis log / error state when the WS misses it
    } finally {
      setSubmitting(false);
    }
  });

  const switchTo = useCallbackRef((id: number) => {
    snapshot.save();
    setActiveId(id);
  });

  // Keyboard shortcuts.
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (!(e.ctrlKey || e.metaKey)) return;
      if (e.key === 'Enter') {
        e.preventDefault();
        void runTests();
      } else if (e.key.toLowerCase() === 'h') {
        e.preventDefault();
        setHintsOpen((o) => !o);
      } else if (e.key.toLowerCase() === 's') {
        e.preventDefault();
        snapshot.save();
      } else if (/^[1-6]$/.test(e.key)) {
        const idx = Number(e.key) - 1;
        if (challenges[idx]) {
          e.preventDefault();
          switchTo(challenges[idx].id);
        }
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [challenges, runTests, switchTo, snapshot]);

  async function finish() {
    if (!sessionId) return;
    snapshot.save();
    setFinishing(true);
    try {
      await completeSession(sessionId);
      navigate('/session/report');
    } finally {
      setFinishing(false);
    }
  }

  const lineCount = snapshot.code.split('\n').length;
  const charCount = snapshot.code.length;
  const score = ws.completion?.score ?? 0;
  const passed = ws.completion?.passed ?? ws.testResults.filter((r) => r.passed).length;
  const total = ws.completion?.total ?? detail?.totalTests ?? 0;

  const expectedCases = useMemo(() => detail?.testCases ?? [], [detail]);

  return (
    <div className="flex h-screen flex-col">
      {/* Header */}
      <header className="flex h-14 shrink-0 items-center justify-between border-b border-default bg-bg-surface px-4">
        <div className="flex items-center gap-3">
          <span className="font-display text-lg font-bold text-accent-blue">SPRING ARENA</span>
          <span className="text-xs text-txt-muted">·</span>
          <span className="text-xs text-txt-secondary">{candidate?.name}</span>
        </div>
        <div className="flex items-center gap-2">
          <button className="btn btn-ghost text-xs" onClick={() => setHintsOpen((o) => !o)}>
            Hints (Ctrl+H)
          </button>
          <button className="btn btn-primary text-xs" disabled={finishing} onClick={finish}>
            {finishing ? 'Finishing…' : 'Submit All & Finish'}
          </button>
          <button
            className="btn btn-ghost text-xs"
            onClick={() => {
              logoutCandidate();
              navigate('/');
            }}
          >
            Exit
          </button>
        </div>
      </header>

      <div className="grid min-h-0 flex-1 grid-cols-1 lg:grid-cols-[60%_40%]">
        {/* LEFT */}
        <div className="flex min-h-0 flex-col border-r border-default">
          <div className="border-b border-default p-2">
            <ChallengeNav
              challenges={challenges}
              activeId={activeId}
              submittedIds={submittedIds}
              onSelect={switchTo}
            />
          </div>
          <div className="min-h-0 flex-1">
            <CodeEditor
              value={snapshot.code}
              onChange={snapshot.setCode}
              onSubmit={() => void runTests()}
              onSave={snapshot.save}
            />
          </div>
          <div className="flex h-12 shrink-0 items-center justify-between border-t border-default px-3 text-xs text-txt-secondary">
            <span>
              {lineCount} lines · {charCount} chars
            </span>
            <div className="flex gap-2">
              <button
                className="btn btn-ghost text-xs"
                onClick={() => detail && snapshot.setCode(detail.starterCode)}
              >
                Clear
              </button>
              <button
                className="btn btn-primary flex items-center gap-2 text-xs"
                disabled={submitting || ws.isRunning}
                onClick={() => void runTests()}
              >
                {(submitting || ws.isRunning) && (
                  <span className="inline-block h-3 w-3 animate-spin rounded-full border-2 border-white border-t-transparent" />
                )}
                Run Tests (Ctrl+Enter)
              </button>
            </div>
          </div>
        </div>

        {/* RIGHT */}
        <div className="flex min-h-0 flex-col">
          {/* Section 1: Challenge info */}
          <div className="shrink-0 border-b border-default p-4">
            {detail ? (
              <>
                <div className="mb-2 flex items-center gap-2">
                  <h2 className="font-display text-base font-bold text-txt-primary">{detail.title}</h2>
                  <DifficultyBadge difficulty={detail.difficulty} />
                  <span className="rounded-md border border-default px-2 py-0.5 text-[10px] text-txt-secondary">
                    {detail.category}
                  </span>
                </div>
                <p className="mb-3 max-h-[220px] overflow-y-auto whitespace-pre-wrap text-xs leading-relaxed text-txt-secondary">
                  {detail.description}
                </p>
                <div className="mb-3">
                  <ConceptsTags concepts={detail.concepts} />
                </div>
                <Timer
                  seconds={timer.seconds}
                  running={timer.running}
                  estimatedMinutes={detail.estimatedMinutes}
                  onToggle={timer.toggle}
                />
              </>
            ) : (
              <p className="text-xs text-txt-muted">Loading challenge…</p>
            )}
          </div>

          {/* Section 2: Test results */}
          <div className="min-h-0 flex-1 overflow-hidden p-4">
            <TestResultPanel
              expected={expectedCases}
              results={ws.testResults}
              running={ws.isRunning}
              score={score}
              passed={passed}
              total={total}
            />
          </div>

          {/* Section 3: Analysis log */}
          <div className="h-[220px] shrink-0 p-3 pt-0">
            <AnalysisLog logs={ws.logs} isConnected={ws.isConnected} />
          </div>
        </div>
      </div>

      <HintsDrawer hints={detail?.hints ?? []} open={hintsOpen} onClose={() => setHintsOpen(false)} />
    </div>
  );
}
