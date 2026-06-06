import { Navigate, Route, Routes } from 'react-router-dom';
import { useSession } from './store/sessionStore';
import Landing from './pages/Landing';
import CandidateEntry from './pages/CandidateEntry';
import ChallengeArena from './pages/ChallengeArena';
import SessionReport from './pages/SessionReport';
import RecruiterLogin from './pages/RecruiterLogin';
import RecruiterDashboard from './pages/RecruiterDashboard';
import RecruiterSession from './pages/RecruiterSession';
import type { JSX } from 'react';

function RequireCandidate({ children }: { children: JSX.Element }) {
  const { candidate } = useSession();
  return candidate ? children : <Navigate to="/session/start" replace />;
}

function RequireRecruiter({ children }: { children: JSX.Element }) {
  const { recruiter } = useSession();
  return recruiter ? children : <Navigate to="/recruiter/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/session/start" element={<CandidateEntry />} />
      <Route
        path="/challenge"
        element={
          <RequireCandidate>
            <ChallengeArena />
          </RequireCandidate>
        }
      />
      <Route
        path="/session/report"
        element={
          <RequireCandidate>
            <SessionReport />
          </RequireCandidate>
        }
      />
      <Route path="/recruiter/login" element={<RecruiterLogin />} />
      <Route
        path="/recruiter/dashboard"
        element={
          <RequireRecruiter>
            <RecruiterDashboard />
          </RequireRecruiter>
        }
      />
      <Route
        path="/recruiter/sessions/:id"
        element={
          <RequireRecruiter>
            <RecruiterSession />
          </RequireRecruiter>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
