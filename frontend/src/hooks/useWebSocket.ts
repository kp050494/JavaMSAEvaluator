import { useCallback, useEffect, useRef, useState } from 'react';
import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { ProgressMessage, TestResult } from '../types';
import { humanizeTestName } from '../lib/humanize';

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
    setIsRunning(true);
  }, [reset]);

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
          const passed = msg.passedCount ?? 0;
          const total = msg.total ?? 0;
          const score = msg.score ?? 0;
          const allPassed = total > 0 && passed === total;
          appendLog('────────────────────────────', 'muted');
          appendLog(
            `${allPassed ? '✅ CHALLENGE PASSED' : '❌ CHALLENGE FAILED'} — ${passed}/${total} tests passed · score ${score}%`,
            allPassed ? 'success' : 'error',
          );
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
    [appendLog],
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

  return { logs, testResults, isConnected, isRunning, completion, error, startRun, reset };
}
