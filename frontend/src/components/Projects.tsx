import { useProjects } from '../hooks/useProjects';
import { profile } from '../profile';
import { GitHubIcon } from './icons';
import ProjectCard from './ProjectCard';
import Section from './Section';

function Skeleton() {
  return (
    <div className="card animate-pulse p-5" aria-hidden>
      <div className="h-4 w-1/2 rounded bg-slate-200 dark:bg-slate-800" />
      <div className="mt-4 h-3 w-full rounded bg-slate-200 dark:bg-slate-800" />
      <div className="mt-2 h-3 w-5/6 rounded bg-slate-200 dark:bg-slate-800" />
      <div className="mt-6 h-3 w-1/3 rounded bg-slate-200 dark:bg-slate-800" />
    </div>
  );
}

export default function Projects() {
  const { state, retry } = useProjects();

  return (
    <Section
      id="projects"
      eyebrow="Projects"
      title="Open-source projects"
      intro={
        <>
          Pulled live from my{' '}
          <a className="font-medium text-accent-600 hover:underline dark:text-accent-400" href={profile.github} target="_blank" rel="noopener noreferrer">
            GitHub profile
          </a>{' '}
          and indexed into the CV assistant below.
        </>
      }
    >
      {state.status === 'loading' && (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3" role="status" aria-label="Loading projects">
          {Array.from({ length: 6 }, (_, i) => (
            <Skeleton key={i} />
          ))}
        </div>
      )}

      {state.status === 'error' && (
        <div className="card flex flex-col items-start gap-4 p-6 sm:flex-row sm:items-center sm:justify-between" role="alert">
          <div>
            <p className="font-semibold text-slate-900 dark:text-white">Projects couldn't be loaded right now.</p>
            <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">{state.message}</p>
          </div>
          <div className="flex gap-3">
            <button type="button" className="btn-secondary" onClick={retry}>
              Try again
            </button>
            <a className="btn-primary" href={profile.github} target="_blank" rel="noopener noreferrer">
              <GitHubIcon size={16} /> Open GitHub
            </a>
          </div>
        </div>
      )}

      {state.status === 'success' && state.projects.length === 0 && (
        <div className="card p-6 text-sm text-slate-600 dark:text-slate-400">
          No public repositories to show yet — have a look at{' '}
          <a className="font-medium text-accent-600 hover:underline dark:text-accent-400" href={profile.github}>
            GitHub
          </a>
          .
        </div>
      )}

      {state.status === 'success' && state.projects.length > 0 && (
        <>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {state.projects.map((project) => (
              <ProjectCard key={project.id} project={project} />
            ))}
          </div>
          <div className="mt-8">
            <a className="btn-secondary" href={`${profile.github}?tab=repositories`} target="_blank" rel="noopener noreferrer">
              <GitHubIcon size={16} /> All repositories
            </a>
          </div>
        </>
      )}
    </Section>
  );
}
