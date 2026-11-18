import { ArrowUpIcon, FileTextIcon, SearchIcon, TriangleAlertIcon } from 'lucide-react';
import { type FormEvent, Fragment, type ReactNode, useEffect, useId, useRef, useState } from 'react';
import { Bubble, BubbleContent } from '@/components/ui/bubble';
import { Button } from '@/components/ui/button';
import { InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput } from '@/components/ui/input-group';
import { Message, MessageContent } from '@/components/ui/message';
import {
  MessageScroller,
  MessageScrollerButton,
  MessageScrollerContent,
  MessageScrollerItem,
  MessageScrollerProvider,
  MessageScrollerViewport,
} from '@/components/ui/message-scroller';
import { Spinner } from '@/components/ui/spinner';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import {
  Trace,
  TraceCollapsible,
  TraceCollapsibleContent,
  TraceCollapsibleTrigger,
  TraceIcon,
  TraceLine,
  TraceTitle,
} from '@/components/ui/trace';
import { ApiError, askQuestion } from '@/api';
import type { Source } from '@/types';
import Section from './Section';

const SAMPLE_QUESTIONS = [
  "What was Ronald's Bachelor thesis about?",
  'Does he have experience with C# and full-stack development?',
  'Where does he currently work?',
  'Which programming languages does he know?',
];

const MAX_LENGTH = 500;

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  sources?: Source[];
  generated?: boolean;
  notice?: string | null;
  error?: boolean;
}

function errorText(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 429) return "That's a lot of questions in a short time. Wait a minute, then ask again.";
    if (error.status === 400) return error.message;
  }
  return "The assistant isn't reachable right now. Try again in a moment, or email me directly.";
}

/** Turns "[n]" markers into citation buttons that name their source on hover and focus. */
function withCitations(text: string, sources: Source[] = []): ReactNode[] {
  return text.split(/(\[\d+\])/g).map((part, i) => {
    const match = /^\[(\d+)\]$/.exec(part);
    if (!match) return <Fragment key={i}>{part}</Fragment>;
    const index = Number(match[1]);
    const source = sources.find((s) => s.index === index);
    if (!source) return <Fragment key={i}>{part}</Fragment>;
    return (
      <Tooltip key={i}>
        <TooltipTrigger
          render={
            <button
              type="button"
              className="mx-0.5 inline-flex h-4 min-w-4 -translate-y-1.5 items-center justify-center rounded-sm bg-accent px-1 align-baseline font-mono text-[0.625rem] leading-none font-medium text-accent-foreground tabular-nums transition-colors hover:bg-primary hover:text-primary-foreground"
              aria-label={`Source ${index}: ${source.section}, ${source.title}`}
            />
          }
        >
          {index}
        </TooltipTrigger>
        <TooltipContent>
          {source.section}, {source.title}
        </TooltipContent>
      </Tooltip>
    );
  });
}

function Sources({ sources }: { sources: Source[] }) {
  return (
    <Trace>
      <TraceCollapsible>
        <TraceCollapsibleTrigger>
          <TraceLine>
            <TraceIcon>
              <FileTextIcon />
            </TraceIcon>
            <TraceTitle>
              {sources.length} {sources.length === 1 ? 'source' : 'sources'}
            </TraceTitle>
          </TraceLine>
        </TraceCollapsibleTrigger>
        <TraceCollapsibleContent>
          <ol className="flex flex-col gap-4 py-1">
            {sources.map((s) => (
              <li key={s.index}>
                <p className="flex flex-wrap items-baseline gap-x-2 text-[0.8125rem] font-medium text-foreground">
                  <span className="font-mono text-[0.6875rem] text-accent-foreground tabular-nums">[{s.index}]</span>
                  <span>
                    {s.section}, {s.title}
                  </span>
                  <span className="font-mono text-[0.6875rem] font-normal text-muted-foreground tabular-nums">
                    {(s.score * 100).toFixed(0)}% match
                  </span>
                </p>
                <p className="mt-1 text-[0.8125rem] leading-relaxed">{s.snippet}</p>
                {s.url && (
                  <a
                    href={s.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="mt-1 inline-block rounded-sm text-[0.8125rem] break-all text-primary underline-offset-4 hover:underline"
                  >
                    {s.url.replace(/^https?:\/\//, '')}
                  </a>
                )}
              </li>
            ))}
          </ol>
        </TraceCollapsibleContent>
      </TraceCollapsible>
    </Trace>
  );
}

function SampleQuestions({ onAsk, disabled }: { onAsk: (q: string) => void; disabled: boolean }) {
  return (
    <ul className="flex flex-wrap gap-2" aria-label="Example questions">
      {SAMPLE_QUESTIONS.map((q) => (
        <li key={q}>
          <Button
            variant="outline"
            size="sm"
            disabled={disabled}
            onClick={() => onAsk(q)}
            className="h-auto min-h-7 py-1 text-left whitespace-normal"
          >
            {q}
          </Button>
        </li>
      ))}
    </ul>
  );
}

export default function AskCv() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const nextId = useRef(1);
  const abortRef = useRef<AbortController | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const inputId = useId();
  const hintId = useId();

  useEffect(() => () => abortRef.current?.abort(), []);

  async function ask(question: string) {
    const q = question.trim();
    if (!q || loading) return;
    setInput('');
    setMessages((m) => [...m, { id: `m${nextId.current++}`, role: 'user', text: q }]);
    setLoading(true);
    const controller = new AbortController();
    abortRef.current = controller;
    try {
      const response = await askQuestion(q, controller.signal);
      setMessages((m) => [
        ...m,
        {
          id: `m${nextId.current++}`,
          role: 'assistant',
          text: response.answer,
          sources: response.sources,
          generated: response.generated,
          notice: response.notice,
        },
      ]);
    } catch (error) {
      if (controller.signal.aborted) return;
      setMessages((m) => [...m, { id: `m${nextId.current++}`, role: 'assistant', error: true, text: errorText(error) }]);
    } finally {
      if (!controller.signal.aborted) setLoading(false);
    }
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault();
    void ask(input);
    inputRef.current?.focus();
  }

  const hasMessages = messages.length > 0;

  return (
    <Section id="ask" title="Ask my CV">
      <p className="mb-6 max-w-[65ch] leading-relaxed text-muted-foreground">
        Ask about my experience, skills or projects. Answers are assembled from my CV and GitHub READMEs, and every
        statement links back to its source.
      </p>

      <div className="overflow-hidden rounded-xl border bg-card shadow-[0_1px_2px_oklch(0.2_0.03_262/0.04),0_4px_16px_-4px_oklch(0.2_0.03_262/0.08)]">
        {hasMessages ? (
          <MessageScrollerProvider autoScroll>
            <MessageScroller className="h-[26rem] sm:h-[28rem]">
              <MessageScrollerViewport aria-label="Conversation" className="px-4 sm:px-5">
                <MessageScrollerContent className="py-5">
                  {messages.map((m) => (
                    <MessageScrollerItem key={m.id} messageId={m.id} scrollAnchor={m.role === 'user'}>
                      {m.role === 'user' ? (
                        <Message align="end">
                          <MessageContent>
                            <Bubble variant="default" align="end">
                              <BubbleContent className="text-[0.9375rem] whitespace-pre-wrap">{m.text}</BubbleContent>
                            </Bubble>
                          </MessageContent>
                        </Message>
                      ) : (
                        <Message align="start">
                          <MessageContent>
                            {m.error ? (
                              <Bubble variant="destructive">
                                <BubbleContent className="flex gap-2 text-[0.9375rem]">
                                  <TriangleAlertIcon aria-hidden="true" className="mt-0.5 size-4 shrink-0" />
                                  <span>{m.text}</span>
                                </BubbleContent>
                              </Bubble>
                            ) : (
                              <Bubble variant="ghost">
                                <BubbleContent className="flex max-w-[65ch] flex-col gap-2.5 text-[0.9375rem] leading-relaxed">
                                  {m.text.split(/\n{2,}/).map((paragraph, i) => (
                                    <p key={i} className="whitespace-pre-line">
                                      {withCitations(paragraph, m.sources)}
                                    </p>
                                  ))}
                                </BubbleContent>
                              </Bubble>
                            )}
                            {!m.error && m.notice && (
                              <p className="px-1 text-xs text-muted-foreground">{m.notice}</p>
                            )}
                            {!m.error && !m.notice && m.sources && m.sources.length > 0 && (
                              <p className="px-1 text-xs text-muted-foreground">
                                {m.generated ? 'Written by AI from the sources below' : 'Quoted directly from the sources'}
                              </p>
                            )}
                            {m.sources && m.sources.length > 0 && <Sources sources={m.sources} />}
                          </MessageContent>
                        </Message>
                      )}
                    </MessageScrollerItem>
                  ))}
                  {loading && (
                    <MessageScrollerItem messageId="pending">
                      <Trace role="status">
                        <TraceLine status="running">
                          <TraceIcon>
                            <Spinner />
                          </TraceIcon>
                          <TraceTitle>Searching the CV and READMEs</TraceTitle>
                        </TraceLine>
                      </Trace>
                    </MessageScrollerItem>
                  )}
                </MessageScrollerContent>
              </MessageScrollerViewport>
              <MessageScrollerButton />
            </MessageScroller>
          </MessageScrollerProvider>
        ) : (
          <div className="flex flex-col gap-3 px-4 pt-5 pb-4 sm:px-5">
            <p className="text-sm font-medium">Start with a question like:</p>
            <SampleQuestions onAsk={(q) => void ask(q)} disabled={loading} />
          </div>
        )}

        <form onSubmit={onSubmit} className="border-t bg-background/60 p-3 sm:p-4">
          <label htmlFor={inputId} className="sr-only">
            Your question about Ronald
          </label>
          <InputGroup className="h-11 bg-card">
            <InputGroupAddon>
              <SearchIcon aria-hidden="true" />
            </InputGroupAddon>
            <InputGroupInput
              ref={inputRef}
              id={inputId}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              maxLength={MAX_LENGTH}
              placeholder="What did he do at GECKO?"
              autoComplete="off"
              enterKeyHint="send"
              aria-describedby={hintId}
              className="text-[0.9375rem]"
            />
            <InputGroupAddon align="inline-end">
              <InputGroupButton
                type="submit"
                variant="default"
                size="icon-sm"
                disabled={loading || !input.trim()}
                aria-label="Ask"
              >
                {loading ? <Spinner /> : <ArrowUpIcon />}
              </InputGroupButton>
            </InputGroupAddon>
          </InputGroup>
          <p id={hintId} className="mt-2 px-1 text-xs leading-relaxed text-muted-foreground">
            Retrieval runs on this server (TF-IDF vectors in pgvector); a Mistral model phrases the answer from the sources found.
            {input.length > MAX_LENGTH - 100 && (
              <span className="ml-1 tabular-nums">
                {input.length}/{MAX_LENGTH}
              </span>
            )}
          </p>
        </form>
      </div>

      {hasMessages && (
        <div className="mt-4 flex flex-col gap-2">
          <p className="text-[0.8125rem] font-medium text-muted-foreground">More questions</p>
          <SampleQuestions onAsk={(q) => void ask(q)} disabled={loading} />
        </div>
      )}
    </Section>
  );
}
