/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: {
          primary: '#0a0e1a',
          surface: '#0f1628',
          elevated: '#151f35',
        },
        accent: {
          blue: '#3b82f6',
          green: '#22c55e',
          amber: '#f59e0b',
          red: '#ef4444',
          purple: '#8b5cf6',
        },
        txt: {
          primary: '#e2e8f0',
          secondary: '#94a3b8',
          muted: '#475569',
        },
        difficulty: {
          easy: '#22c55e',
          medium: '#f59e0b',
          hard: '#ef4444',
        },
      },
      fontFamily: {
        mono: ['"JetBrains Mono"', 'monospace'],
        display: ['Orbitron', 'sans-serif'],
      },
      borderColor: {
        default: 'rgba(59,130,246,0.15)',
        focus: 'rgba(59,130,246,0.4)',
      },
      keyframes: {
        shake: {
          '0%, 100%': { transform: 'translateX(0)' },
          '25%': { transform: 'translateX(-4px)' },
          '75%': { transform: 'translateX(4px)' },
        },
        pop: {
          '0%': { transform: 'scale(0.6)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        blink: {
          '0%, 100%': { opacity: '1' },
          '50%': { opacity: '0' },
        },
      },
      animation: {
        shake: 'shake 0.4s ease-in-out',
        pop: 'pop 0.25s ease-out',
        blink: 'blink 1s step-end infinite',
      },
    },
  },
  plugins: [],
};
