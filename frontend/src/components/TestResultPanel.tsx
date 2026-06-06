import type { TestResult } from '../types';
import { humanizeTestName } from '../lib/humanize';

interface Props {
  expected: string[];
  results: TestResult[];
  running: boolean;
  score: number;
  passed: number;
  total: number;
}

type Status = 'pending' | 'running' | 'pass' | 'fail';

function Icon({ status }: { status: Status }) {
  if (status === 'pass') {
    return <span className="text-accent-green animate-pop">✔</span>;
  }
  if (status === 'fail') {
    return <span className="text-accent-red font-bold">✕</span>;
  }
  if (status === 'running') {
    return <span className="inline-block h-3 w-3 animate-spin rounded-full border-2 border-accent-blue border-t-transparent" />;
  }
  return <span className="inline-block h-3 w-3 rounded-full border border-txt-muted" />;
}

export default function TestResultPanel({ expected, results, running, score, passed, total }: Props) {
  // Once results stream in, show one row per actual test (with a readable name).
  // Before a run, show the challenge's declared test cases as pending rows.
  const display =
    results.length > 0
      ? results.map((r, i) => ({ key: `s-${i}`, label: humanizeTestName(r.testName), result: r }))
      : expected.map((label, i) => ({ key: `e-${i}`, label, result: undefined as TestResult | undefined }));

  return (
    <div className="flex h-full flex-col">
      <div className="mb-3">
        <div className="mb-1 flex items-center justify-between text-xs text-txt-secondary">
          <span>Test Results</span>
          <span>
            {passed}/{total || expected.length} · {score}%
          </span>
        </div>
        <div className="h-2 w-full overflow-hidden rounded-full bg-bg-elevated">
          <div
            className="h-full rounded-full bg-accent-green transition-all duration-500"
            style={{ width: `${score}%` }}
          />
        </div>
      </div>

      <ul className="flex-1 space-y-2 overflow-y-auto pr-1">
        {display.map(({ key, label, result }) => {
          let status: Status = 'pending';
          if (result) {
            status = result.passed ? 'pass' : 'fail';
          } else if (running) {
            status = 'running';
          }
          return (
            <li
              key={key}
              className={`rounded-lg border border-default bg-bg-elevated/60 p-2 text-xs ${
                status === 'fail' ? 'animate-shake' : ''
              }`}
            >
              <div className="flex items-start gap-2">
                <span className="mt-0.5 w-4 text-center">
                  <Icon status={status} />
                </span>
                <span className="flex-1 break-words text-txt-primary">{label}</span>
              </div>
              {result && !result.passed && result.message && (
                <p className="ml-6 mt-1 break-words text-[11px] text-accent-red/90">{result.message}</p>
              )}
            </li>
          );
        })}
        {display.length === 0 && (
          <li className="text-xs text-txt-muted">Run the tests to see results here.</li>
        )}
      </ul>
    </div>
  );
}
