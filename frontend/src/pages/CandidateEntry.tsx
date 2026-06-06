import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { candidateLogin } from '../api/auth';
import { useSession } from '../store/sessionStore';

export default function CandidateEntry() {
  const navigate = useNavigate();
  const { loginCandidate } = useSession();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function begin(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const auth = await candidateLogin(name.trim(), email.trim());
      loginCandidate({ token: auth.token, sessionId: auth.sessionId ?? '', name: auth.name });
      navigate('/challenge');
    } catch {
      setError('Could not start the assessment. Please check your details and try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid-bg flex min-h-screen items-center justify-center px-6">
      <div className="card w-full max-w-md p-8">
        <h1 className="font-display text-2xl font-bold text-accent-blue">Begin Assessment</h1>
        <p className="mt-1 text-xs text-txt-secondary">
          Six Spring Boot challenges · ~97 minutes total
        </p>

        <div className="mt-5 grid grid-cols-3 gap-2 text-center text-xs">
          <div className="rounded-lg border border-difficulty-easy/30 bg-difficulty-easy/10 p-2 text-difficulty-easy">
            Easy · 2
          </div>
          <div className="rounded-lg border border-difficulty-medium/30 bg-difficulty-medium/10 p-2 text-difficulty-medium">
            Medium · 3
          </div>
          <div className="rounded-lg border border-difficulty-hard/30 bg-difficulty-hard/10 p-2 text-difficulty-hard">
            Hard · 1
          </div>
        </div>

        <form onSubmit={begin} className="mt-6 space-y-4">
          <div>
            <label className="mb-1 block text-xs text-txt-secondary">Full name</label>
            <input
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded-lg border border-default bg-bg-elevated px-3 py-2 text-sm text-txt-primary outline-none focus:border-focus"
              placeholder="Ada Lovelace"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs text-txt-secondary">Email</label>
            <input
              required
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-lg border border-default bg-bg-elevated px-3 py-2 text-sm text-txt-primary outline-none focus:border-focus"
              placeholder="ada@example.com"
            />
          </div>
          {error && <p className="text-xs text-accent-red">{error}</p>}
          <button type="submit" disabled={loading} className="btn btn-primary w-full">
            {loading ? 'Starting…' : 'Begin Assessment'}
          </button>
        </form>
      </div>
    </div>
  );
}
