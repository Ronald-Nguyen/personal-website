import { type FormEvent, Fragment, type ReactNode, useEffect, useId, useRef, useState } from 'react';
import { askQuestion } from '../api';
import type { Source } from '../types';
import { ExternalIcon, SendIcon, SparkIcon } from './icons';
import Section from './Section';

const SAMPLE_QUESTIONS = [
  "What was Ronald's Bachelor thesis about?",
  'Does he have experience with C# and full-stack development?',
  "When is he available for a Master's thesis?",
  'Which programming languages does he know?',
];

const MAX_LENGTH = 500;

interface Message {
  id: number;
  role: 'user' | 'assistant';
  text: string;
  sources?: Source[];
  error?: boolean;
}

/** Renders "[n]" markers as small citation links to the matching source. */
function withCitations(text: string, messageId: number): ReactNode[] {
  return text.split(/(\[\d+\])/g).map((part, i) => {
    const match = /^\[(\d+)\]$/.exec(part);
    if (!match) return <Fragment key={i}>{part}</Fragment>;
    return (
      <a
        key={i}
        href={`#src-${messageId}-${match[1]}`}
        className="ml-0.5 inline-flex h-4 min-w-4 -translate-y-1 items-center justify-center rounded bg-accent-100 px-1 align-baseline font-mono text-[10px] font-bold text-accent-800 no-underline hover:bg-accent-200 dark:bg-accent-900/60 dark:text-accent-200"
        aria-label={`Source ${match[1]}`}
      >
        {match[1]}
      </a>
    );
  });
}

function SourceList({ sources, messageId }: { sources: Source[]; messageId: number }) {
  return (
    <details className="group mt-3 rounded-xl border border-slate-200 bg-white/60 dark:border-slate-700 dark:bg-slate-900/60">
      <summary className="cursor-pointer select-none px-3 py-2 text-xs font-semibold text-slate-600 marker:text-slate-400 dark:text-slate-400">
        Show {sources.length} cited source{sources.length === 1 ? '' : 's'}
      </summary>
      <ol className="space-y-2 px-3 pb-3">
        {sources.map((s) => (
          <li key={s.index} id={`src-${messageId}-${s.index}`} className="scroll-mt-24 rounded-lg bg-slate-50 p-3 text-xs dark:bg-slate-800/60">
            <div className="flex flex-wrap items-center gap-2">
              <span className="flex h-5 w-5 items-center justify-center rounded bg-accent-600 font-mono text-[10px] font-bold text-white">
                {s.index}
              </span>
              <span className="font-semibold text-slate-800 dark:text-slate-200">
                {s.section} · {s.title}
              </span>
              <span className="ml-auto font-mono text-[10px] text-slate-500" title="Cosine similarity">
                {(s.score * 100).toFixed(0)}% match
              </span>
            </div>
            <p className="mt-1.5 leading-relaxed text-slate-600 dark:text-slate-400">{s.snippet}</p>
            {s.url && (
              <a
                href={s.url}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-1.5 inline-flex items-center gap-1 font-medium text-accent-600 hover:underline dark:text-accent-400"
              >
                {s.url.replace(/^https?:\/\//, '')} <ExternalIcon size={11} />
              </a>
            )}
          </li>
        ))}
      </ol>
    </details>
  );
}

export default function ChatSection() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const nextId = useRef(1);
  const scrollRef = useRef<HTMLDivElement>(null);
  const abortRef = useRef<AbortController | null>(null);
  const inputId = useId();

  // Keep the start of the newest message in view (answers can be long).
  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    const all = el.querySelectorAll<HTMLElement>('[data-message]');
    const last = all.length > 0 ? all[all.length - 1] : null;
    const top = loading || !last ? el.scrollHeight : last.offsetTop - 16;
    el.scrollTo({ top, behavior: 'smooth' });
  }, [messages, loading]);

  useEffect(() => () => abortRef.current?.abort(), []);

  async function ask(question: string) {
    const q = question.trim();
    if (!q || loading) return;
    setInput('');
    setMessages((m) => [...m, { id: nextId.current++, role: 'user', text: q }]);
    setLoading(true);
    const controller = new AbortController();
    abortRef.current = controller;
    try {
      const response = await askQuestion(q, controller.signal);
      setMessages((m) => [
        ...m,
        { id: nextId.current++, role: 'assistant', text: response.answer, sources: response.sources },
      ]);
    } catch (error) {
      if (controller.signal.aborted) return;
      setMessages((m) => [
        ...m,
        {
          id: nextId.current++,
          role: 'assistant',
          error: true,
          text:
            error instanceof Error && error.message
              ? `Sorry — ${error.message}`
              : 'Sorry — the assistant is not reachable right now. Please try again in a moment.',
        },
      ]);
    } finally {
      if (!controller.signal.aborted) setLoading(false);
    }
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    void ask(input);
  }

  return (
    <Section
      id="ask"
      eyebrow="Ask my CV"
      title="Ask the CV assistant"
      intro="Ask anything about my background, projects and skills. Answers are retrieved from my CV and GitHub repositories (vector search in PostgreSQL + pgvector) and always cite their sources."
    >
      <div className="card overflow-hidden">
        <div ref={scrollRef} className="relative max-h-[32rem] min-h-[16rem] space-y-4 overflow-y-auto p-4 sm:p-6" aria-live="polite">
          {messages.length === 0 && (
            <div className="flex flex-col items-center py-8 text-center">
              <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-accent-600 text-white shadow-sm">
                <SparkIcon size={24} />
              </span>
              <p className="mt-4 font-semibold text-slate-900 dark:text-white">What would you like to know?</p>
              <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Try one of these questions:</p>
            </div>
          )}

          {messages.map((m) =>
            m.role === 'user' ? (
              <div key={m.id} data-message className="flex justify-end">
                <p className="max-w-[85%] whitespace-pre-wrap rounded-2xl rounded-br-sm bg-accent-600 px-4 py-2.5 text-sm text-white">
                  {m.text}
                </p>
              </div>
            ) : (
              <div key={m.id} data-message className="flex justify-start">
                <div
                  className={`max-w-[95%] rounded-2xl rounded-bl-sm px-4 py-3 text-sm sm:max-w-[85%] ${
                    m.error
                      ? 'border border-rose-200 bg-rose-50 text-rose-800 dark:border-rose-900/60 dark:bg-rose-950/40 dark:text-rose-200'
                      : 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-200'
                  }`}
                >
                  {m.text.split(/\n{2,}/).map((paragraph, i) => (
                    <p key={i} className={`leading-relaxed ${i > 0 ? 'mt-2' : ''} ${i === 0 && m.sources?.length ? 'font-medium' : ''}`}>
                      {withCitations(paragraph, m.id)}
                    </p>
                  ))}
                  {m.sources && m.sources.length > 0 && <SourceList sources={m.sources} messageId={m.id} />}
                </div>
              </div>
            ),
          )}

          {loading && (
            <div className="flex justify-start" role="status" aria-label="Assistant is answering">
              <div className="flex gap-1 rounded-2xl rounded-bl-sm bg-slate-100 px-4 py-3 dark:bg-slate-800">
                {[0, 1, 2].map((i) => (
                  <span
                    key={i}
                    className="h-2 w-2 animate-blink rounded-full bg-slate-400"
                    style={{ animationDelay: `${i * 0.2}s` }}
                  />
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="border-t border-slate-200 p-4 dark:border-slate-800 sm:p-5">
          <div className="mb-3 flex flex-wrap gap-2">
            {SAMPLE_QUESTIONS.map((q) => (
              <button
                key={q}
                type="button"
                disabled={loading}
                onClick={() => void ask(q)}
                className="rounded-full border border-accent-200 bg-accent-50 px-3 py-1.5 text-left text-xs font-medium text-accent-800 transition hover:bg-accent-100 disabled:opacity-50 dark:border-accent-800 dark:bg-accent-900/30 dark:text-accent-200 dark:hover:bg-accent-900/60"
              >
                {q}
              </button>
            ))}
          </div>
          <form onSubmit={onSubmit} className="flex gap-2">
            <label htmlFor={inputId} className="sr-only">
              Your question
            </label>
            <input
              id={inputId}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              maxLength={MAX_LENGTH}
              placeholder="e.g. What did he do at GECKO?"
              autoComplete="off"
              className="min-w-0 flex-1 rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm text-slate-900 placeholder:text-slate-400 focus:border-accent-500 focus:outline-none focus:ring-2 focus:ring-accent-500/30 dark:border-slate-700 dark:bg-slate-900 dark:text-white"
            />
            <button type="submit" className="btn-primary" disabled={loading || !input.trim()}>
              <SendIcon size={16} />
              <span className="hidden sm:inline">Ask</span>
            </button>
          </form>
        </div>
      </div>
    </Section>
  );
}
