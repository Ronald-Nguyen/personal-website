import type { Project } from '../types';
import { ExternalIcon, ForkIcon, GitHubIcon, StarIcon } from './icons';
import { languageColor } from './languageColors';

function formatDate(iso?: string | null) {
  if (!iso) return null;
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return null;
  return date.toLocaleDateString('en-GB', { month: 'short', year: 'numeric' });
}

export default function ProjectCard({ project }: { project: Project }) {
  const updated = formatDate(project.pushedAt);
  return (
    <article className="card group relative flex h-full flex-col p-5 transition hover:-translate-y-0.5 hover:border-accent-300 hover:shadow-md dark:hover:border-accent-700">
      <div className="flex items-start justify-between gap-3">
        <h3 className="break-words font-mono text-base font-semibold text-slate-900 dark:text-white">
          <a href={project.url} target="_blank" rel="noopener noreferrer" className="after:absolute after:inset-0 focus:outline-none">
            {project.name}
          </a>
        </h3>
        <GitHubIcon size={18} className="shrink-0 text-slate-400 transition group-hover:text-accent-600 dark:group-hover:text-accent-400" />
      </div>

      <p className="mt-2 flex-1 text-sm leading-relaxed text-slate-600 dark:text-slate-400">
        {project.description || 'No description provided.'}
      </p>

      {project.topics.length > 0 && (
        <ul className="mt-4 flex flex-wrap gap-1.5" aria-label="Topics">
          {project.topics.slice(0, 6).map((topic) => (
            <li
              key={topic}
              className="rounded-full bg-accent-50 px-2 py-0.5 text-[11px] font-medium text-accent-700 dark:bg-accent-900/40 dark:text-accent-300"
            >
              {topic}
            </li>
          ))}
        </ul>
      )}

      <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs text-slate-500 dark:text-slate-400">
        {project.language && (
          <span className="inline-flex items-center gap-1.5">
            <span className="h-2.5 w-2.5 rounded-full" style={{ backgroundColor: languageColor(project.language) }} />
            {project.language}
          </span>
        )}
        <span className="inline-flex items-center gap-1" title="Stars">
          <StarIcon size={14} /> {project.stars}
        </span>
        {project.forks > 0 && (
          <span className="inline-flex items-center gap-1" title="Forks">
            <ForkIcon size={14} /> {project.forks}
          </span>
        )}
        {project.fork && <span className="chip py-0.5">fork</span>}
        {updated && <span className="ml-auto">Updated {updated}</span>}
      </div>

      {project.homepage && (
        <a
          href={project.homepage}
          target="_blank"
          rel="noopener noreferrer"
          className="relative z-10 mt-3 inline-flex items-center gap-1 self-start text-xs font-semibold text-accent-600 hover:underline dark:text-accent-400"
        >
          Live demo <ExternalIcon size={12} />
        </a>
      )}
    </article>
  );
}
