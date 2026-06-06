import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { recruiterLogin } from '../api/auth';
import { useSession } from '../store/sessionStore';

export default function RecruiterLogin() {
  const navigate = useNavigate();
  const { loginRecruiter } = useSession();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const auth = await recruiterLogin(username.trim(), password);
      loginRecruiter({ token: auth.token, name: auth.name });
      navigate('/recruiter/dashboard');
    } catch {
      setError('Invalid credentials.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid-bg flex min-h-screen items-center justify-center px-6">
      <div className="card w-full max-w-sm p-8">
        <h1 className="font-display text-2xl font-bold text-accent-blue">Recruiter Portal</h1>
        <p className="mt-1 text-xs text-txt-secondary">Sign in to review candidate sessions.</p>
        <form onSubmit={submit} className="mt-6 space-y-4">
          <div>
            <label className="mb-1 block text-xs text-txt-secondary">Username</label>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full rounded-lg border border-default bg-bg-elevated px-3 py-2 text-sm text-txt-primary outline-none focus:border-focus"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs text-txt-secondary">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-lg border border-default bg-bg-elevated px-3 py-2 text-sm text-txt-primary outline-none focus:border-focus"
            />
          </div>
          {error && <p className="text-xs text-accent-red">{error}</p>}
          <button type="submit" disabled={loading} className="btn btn-primary w-full">
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
}
