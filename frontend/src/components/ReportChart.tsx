import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  PolarAngleAxis,
  PolarGrid,
  PolarRadiusAxis,
  Radar,
  RadarChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import type { ChallengeScore } from '../types';

const DIFFICULTY_COLORS: Record<string, string> = {
  EASY: '#22c55e',
  MEDIUM: '#f59e0b',
  HARD: '#ef4444',
};

export function ScoreRadar({ scores }: { scores: ChallengeScore[] }) {
  const data = scores.map((s) => ({ category: s.category, score: s.score }));
  return (
    <ResponsiveContainer width="100%" height={300}>
      <RadarChart data={data} outerRadius="75%">
        <PolarGrid stroke="rgba(59,130,246,0.2)" />
        <PolarAngleAxis dataKey="category" tick={{ fill: '#94a3b8', fontSize: 11 }} />
        <PolarRadiusAxis domain={[0, 100]} tick={{ fill: '#475569', fontSize: 10 }} />
        <Radar dataKey="score" stroke="#3b82f6" fill="#3b82f6" fillOpacity={0.4} />
        <Tooltip
          contentStyle={{ background: '#0f1628', border: '1px solid rgba(59,130,246,0.3)', borderRadius: 8 }}
          labelStyle={{ color: '#e2e8f0' }}
        />
      </RadarChart>
    </ResponsiveContainer>
  );
}

export function ScoreBars({ scores }: { scores: ChallengeScore[] }) {
  const data = scores.map((s) => ({ name: s.title, score: s.score, difficulty: s.difficulty }));
  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={data} margin={{ left: -10, right: 10, bottom: 40 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(59,130,246,0.1)" />
        <XAxis
          dataKey="name"
          tick={{ fill: '#94a3b8', fontSize: 10 }}
          angle={-25}
          textAnchor="end"
          interval={0}
        />
        <YAxis domain={[0, 100]} tick={{ fill: '#475569', fontSize: 10 }} />
        <Tooltip
          cursor={{ fill: 'rgba(59,130,246,0.08)' }}
          contentStyle={{ background: '#0f1628', border: '1px solid rgba(59,130,246,0.3)', borderRadius: 8 }}
          labelStyle={{ color: '#e2e8f0' }}
        />
        <Bar dataKey="score" radius={[4, 4, 0, 0]}>
          {data.map((d, i) => (
            <Cell key={i} fill={DIFFICULTY_COLORS[d.difficulty] ?? '#3b82f6'} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}

export function DistributionBars({ data }: { data: { name: string; score: number }[] }) {
  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={data} margin={{ left: -10, right: 10, bottom: 40 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="rgba(59,130,246,0.1)" />
        <XAxis
          dataKey="name"
          tick={{ fill: '#94a3b8', fontSize: 10 }}
          angle={-25}
          textAnchor="end"
          interval={0}
        />
        <YAxis domain={[0, 100]} tick={{ fill: '#475569', fontSize: 10 }} />
        <Tooltip
          cursor={{ fill: 'rgba(59,130,246,0.08)' }}
          contentStyle={{ background: '#0f1628', border: '1px solid rgba(59,130,246,0.3)', borderRadius: 8 }}
          labelStyle={{ color: '#e2e8f0' }}
        />
        <Bar dataKey="score" radius={[4, 4, 0, 0]} fill="#8b5cf6" />
      </BarChart>
    </ResponsiveContainer>
  );
}
