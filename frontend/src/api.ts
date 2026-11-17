import type { ChatResponse, Project } from './types';

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
  }
}

async function parse<T>(response: Response): Promise<T> {
  if (response.ok) {
    return (await response.json()) as T;
  }
  let message = `Request failed (HTTP ${response.status})`;
  try {
    const body = (await response.json()) as { error?: string };
    if (body.error) message = body.error;
  } catch {
    // non-JSON error body (e.g. proxy error page)
  }
  throw new ApiError(message, response.status);
}

export async function fetchProjects(signal?: AbortSignal): Promise<Project[]> {
  const response = await fetch('/api/projects', { signal, headers: { Accept: 'application/json' } });
  return parse<Project[]>(response);
}

export async function askQuestion(question: string, signal?: AbortSignal): Promise<ChatResponse> {
  const response = await fetch('/api/chat', {
    method: 'POST',
    signal,
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    body: JSON.stringify({ question }),
  });
  return parse<ChatResponse>(response);
}
