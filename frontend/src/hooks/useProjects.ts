import { useCallback, useEffect, useState } from 'react';
import { fetchProjects } from '@/api';
import type { Project } from '@/types';

type State =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'success'; projects: Project[] };

export function useProjects() {
  const [state, setState] = useState<State>({ status: 'loading' });
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setState({ status: 'loading' });
    fetchProjects(controller.signal)
      .then((projects) => setState({ status: 'success', projects }))
      .catch((error: unknown) => {
        if (controller.signal.aborted) return;
        setState({
          status: 'error',
          message: error instanceof Error ? error.message : 'Could not load projects.',
        });
      });
    return () => controller.abort();
  }, [attempt]);

  const retry = useCallback(() => setAttempt((n) => n + 1), []);
  return { state, retry };
}
