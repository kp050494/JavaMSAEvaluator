import { useCallback, useEffect, useRef, useState } from 'react';
import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { ProgressMessage, SubmissionResponse, TestResult } from '../types';
import { humanizeTestName } from '../lib/humanize';
import { getSubmission } from '../api/submissions';

export type LogLevel = 'info' | 'success' | 'error' | 'warn' | 'muted';

export interface LogLine {
  text: string;
  level: LogLevel;
  ts: string;
}

export interface Completion {
  score: number;
  passed: number;
  total: number;
}

interface UseWebSocketResult {
  logs: LogLine[];
  testResults: TestResult[];
  isConnected: boolean;
  isRunning: boolean;
  completion: Completion | null;
  error: string | null;
  startRun: () => void;
  trackSubmission: (submissionId: number) => void;
  reset: () => void;
}

function levelFor(step: ProgressMessage['step'], passed?: boolean): LogLevel {
  switch (step) {
    case 'COMPLETE':
      return 'success';
    case 'ERROR':
      return 'error';
    case 'TEST_RESULT':
      return passed ? 'success' : 'error';
    case 'CONNECTED':
      return 'warn';
    default:
      return 'info';
  }
}

/**
 * Subscribes to /topic/submission/{sessionId} and accumulates the streamed
 * compile/test progress. StompJS handles reconnection with a fixed backoff.
 */
export function useWebSocket(sessionId: string | undefined): UseWebSocketResult {
  const [logs, setLogs] = useState<LogLine[]>([]);
  const [testResults, setTestResults] = useState<TestResult[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [isRunning, setIsRunning] = useState(false);
  const [completion, setCompletion] = useState<Completion | null>(null);
  const [error, setError] = useState<string | null>(null);
  const clientRef = useRef<Client | null>(null);
  // Guards against double-finishing when both WebSocket and the polling fallback fire.
  const completedRef = useRef(false);

  const appendLog = useCallback((text: string, level: LogLevel) => {
    setLogs((prev) => [...prev, { text, level, ts: new Date().toLocaleTimeString() }]);
  }, []);

  const reset = useCallback(() => {
    setLogs([]);
    setTestResults([]);
    setCompletion(null);
    setError(null);
  }, []);

  const startRun = useCallback(() => {
    reset();
    completedRef.current = false;
    setIsRunning(true);
  }, [reset]);

  const finishSummary = useCallback((passed: number, total: number, score: number) => {
    const allPassed = total > 0 && passed === total;
    appendLog('────────────────────────────', 'muted');
    appendLog(
      `${allPassed ? '✅ CHALLENGE PASSED' : '❌ CHALLENGE FAILED'} — ${passed}/${total} tests passed · score ${score}%`,
      allPassed ? 'success' : 'error',
    );
  }, [appendLog]);

  /**
   * Fallback: poll the submission via REST until it finishes, in case the live
   * WebSocket stream never connects (e.g. blocked WS on some hosts). Whichever
   * source finishes first wins; the other is ignored via completedRef.
   */
  const trackSubmission = useCallback((submissionId: number) => {
    const startedAt = Date.now();
    const poll = async () => {
      if (completedRef.current) return;
      let sub: SubmissionResponse | null = null;
      try {
        sub = await getSubmission(submissionId);
      } catch {
        /* transient; will retry */
      }
      if (completedRef.current) return;
      if (sub && (sub.status === 'COMPLETED' || sub.status === 'ERROR')) {
        completedRef.current = true;
        setTestResults(
          (sub.results ?? []).map((r) => ({
            testName: r.testName,
            passed: r.passed,
            message: r.message ?? null,
          })),
        );
        const passed = sub.passed ?? 0;
        const total = sub.total ?? 0;
        const score = sub.score ?? 0;
        if (sub.status === 'ERROR') {
          appendLog(sub.logs && sub.logs.length ? sub.logs[0] : 'Execution failed', 'error');
          setError('Execution failed');
        } else {
          finishSummary(passed, total, score);
        }
        setCompletion({ score, passed, total });
        setIsRunning(false);
        return;
      }
      if (Date.now() - startedAt < 130000) {
        window.setTimeout(poll, 2500);
      } else {
        setIsRunning(false);
        setError('Timed out waiting for results');
        appendLog('Timed out waiting for results.', 'error');
      }
    };
    window.setTimeout(poll, 2000);
  }, [appendLog, finishSummary]);

  const handleMessage = useCallback(
    (msg: ProgressMessage) => {
      switch (msg.step) {
        case 'TEST_RESULT': {
          if (!msg.testName) break;
          // Log each test by name so it's clear which test passed/failed.
          const label = humanizeTestName(msg.testName);
          const failNote = msg.message && msg.message !== 'passed' ? ` — ${msg.message}` : '';
          appendLog(
            `${msg.passed ? '✓ PASS' : '✗ FAIL'}  ${label}${msg.passed ? '' : failNote}`,
            msg.passed ? 'success' : 'error',
          );
          const result: TestResult = {
            testName: msg.testName,
            passed: Boolean(msg.passed),
            message: msg.message ?? null,
          };
          setTestResults((prev) => [...prev.filter((r) => r.testName !== result.testName), result]);
          break;
        }
        case 'COMPLETE': {
          if (completedRef.current) break;
          completedRef.current = true;
          const passed = msg.passedCount ?? 0;
          const total = msg.total ?? 0;
          const score = msg.score ?? 0;
          finishSummary(passed, total, score);
          setCompletion({ score, passed, total });
          setIsRunning(false);
          break;
        }
        case 'ERROR': {
          if (msg.message) appendLog(msg.message, 'error');
          setError(msg.message ?? 'Execution failed');
          setIsRunning(false);
          break;
        }
        default: {
          if (msg.message) appendLog(msg.message, levelFor(msg.step, msg.passed));
        }
      }
    },
    [appendLog, finishSummary],
  );

  useEffect(() => {
    if (!sessionId) {
      return;
    }
    const client = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL ?? ''}/ws`),
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setIsConnected(true);
        client.subscribe(`/topic/submission/${sessionId}`, (frame: IMessage) => {
          try {
            handleMessage(JSON.parse(frame.body) as ProgressMessage);
          } catch {
            /* ignore malformed frames */
          }
        });
      },
      onWebSocketClose: () => setIsConnected(false),
      onStompError: () => setIsConnected(false),
    });
    client.activate();
    clientRef.current = client;
    return () => {
      void client.deactivate();
      clientRef.current = null;
    };
  }, [sessionId, handleMessage]);

  return { logs, testResults, isConnected, isRunning, completion, error, startRun, trackSubmission, reset };
}
