import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const SNIPPET = `@RestController
@RequestMapping("/api/products")
class ProductController {
  @GetMapping
  List<Product> all() {
    return service.findAll();
  }
}
// > compiling... ✔  running tests... ✔`;

function useTypewriter(text: string, speed = 28) {
  const [out, setOut] = useState('');
  useEffect(() => {
    let i = 0;
    const id = window.setInterval(() => {
      i += 1;
      setOut(text.slice(0, i));
      if (i >= text.length) window.clearInterval(id);
    }, speed);
    return () => window.clearInterval(id);
  }, [text, speed]);
  return out;
}

export default function Landing() {
  const navigate = useNavigate();
  const typed = useTypewriter(SNIPPET);

  return (
    <div className="grid-bg min-h-screen w-full">
      <div className="mx-auto flex min-h-screen max-w-5xl flex-col items-center justify-center gap-10 px-6 py-12">
        <header className="text-center">
          <h1 className="font-display text-5xl font-black text-accent-blue drop-shadow-[0_0_18px_rgba(59,130,246,0.4)]">
            SPRING ARENA
          </h1>
          <p className="mt-3 text-sm text-txt-secondary">
            Live Java Microservices Assessment — real compilation, real JUnit, real scores.
          </p>
        </header>

        <pre className="w-full max-w-2xl overflow-hidden rounded-xl border border-default bg-[#080d1a] p-5 text-left text-xs leading-relaxed text-accent-green">
          {typed}
          <span className="animate-blink">▋</span>
        </pre>

        <div className="grid w-full max-w-2xl grid-cols-1 gap-5 sm:grid-cols-2">
          <button
            onClick={() => navigate('/recruiter/login')}
            className="card group p-6 text-left transition hover:border-focus"
          >
            <div className="mb-2 text-2xl">🛡️</div>
            <h2 className="font-display text-lg font-bold text-txt-primary">Recruiter Portal</h2>
            <p className="mt-1 text-xs text-txt-secondary">
              Review candidate sessions, scores and submitted code.
            </p>
            <span className="mt-4 inline-block text-xs text-accent-blue group-hover:underline">
              Enter portal →
            </span>
          </button>

          <button
            onClick={() => navigate('/session/start')}
            className="card group p-6 text-left transition hover:border-focus"
          >
            <div className="mb-2 text-2xl">⚔️</div>
            <h2 className="font-display text-lg font-bold text-txt-primary">Candidate Assessment</h2>
            <p className="mt-1 text-xs text-txt-secondary">
              Solve six Spring Boot challenges in a live editor.
            </p>
            <span className="mt-4 inline-block text-xs text-accent-green group-hover:underline">
              Begin assessment →
            </span>
          </button>
        </div>
      </div>
    </div>
  );
}
