import { ArrowUpRightIcon, FolderGit2Icon, GitForkIcon, RefreshCwIcon, StarIcon, TriangleAlertIcon } from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Empty, EmptyContent, EmptyDescription, EmptyHeader, EmptyMedia, EmptyTitle } from '@/components/ui/empty';
import { Skeleton } from '@/components/ui/skeleton';
import { useProjects } from '@/hooks/useProjects';
import { formatMonthYear } from '@/lib/format';
import { profile } from '@/profile';
import type { Project } from '@/types';
import { languageColor } from './languageColors';
import Section from './Section';

const thesisByRepo = new Map(
  profile.education.flatMap((edu) => (edu.thesis?.repo ? [[edu.thesis.repo, edu.thesis] as const] : [])),
);

function ProjectRow({ project }: { project: Project }) {
  const updated = formatMonthYear(project.pushedAt);
  const thesis = thesisByRepo.get(project.name);

  return (
    <li className="group relative -mx-4 rounded-xl px-4 py-4 transition-colors hover:bg-muted/70 has-[a:focus-visible]:bg-muted/70">
      <h3 className="font-medium leading-6">
        <a
          href={project.url}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-baseline gap-1 rounded-sm outline-none after:absolute after:inset-0 after:rounded-xl focus-visible:after:ring-2 focus-visible:after:ring-ring group-hover:text-primary"
        >
          <span className="break-all">{project.name}</span>
          <ArrowUpRightIcon
            aria-hidden="true"
            className="size-4 shrink-0 translate-y-0.5 transition-transform duration-200 ease-out group-hover:translate-x-0.5 group-hover:-translate-y-0"
          />
          <span className="sr-only">(opens GitHub)</span>
        </a>
      </h3>

      <p className="mt-1 max-w-[65ch] text-[0.9375rem] leading-relaxed text-muted-foreground">
        {project.description || 'No description on GitHub yet.'}
      </p>

      <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1.5 text-[0.8125rem] text-muted-foreground">
        {project.language && (
          <span className="inline-flex items-center gap-1.5">
            <span
              aria-hidden="true"
              className="size-2.5 rounded-full"
              style={{ backgroundColor: languageColor(project.language) }}
            />
            {project.language}
          </span>
        )}
        {project.stars > 0 && (
          <span className="inline-flex items-center gap-1 tabular-nums">
            <StarIcon aria-hidden="true" className="size-3.5" />
            {project.stars}
            <span className="sr-only">{project.stars === 1 ? 'star' : 'stars'}</span>
          </span>
        )}
        {project.forks > 0 && (
          <span className="inline-flex items-center gap-1 tabular-nums">
            <GitForkIcon aria-hidden="true" className="size-3.5" />
            {project.forks}
            <span className="sr-only">{project.forks === 1 ? 'fork' : 'forks'}</span>
          </span>
        )}
        {project.fork && <span>Fork</span>}
        {updated && <span>Updated {updated}</span>}
        {thesis?.url && (
          <a
            href={thesis.url}
            target="_blank"
            rel="noopener noreferrer"
            className="relative z-10 inline-flex items-center gap-0.5 rounded-sm font-medium text-primary underline-offset-4 hover:underline"
          >
            Read the full thesis (PDF)
            <ArrowUpRightIcon aria-hidden="true" className="size-3.5" />
          </a>
        )}
        {thesis?.listingUrl && (
          <a
            href={thesis.listingUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="relative z-10 inline-flex items-center gap-0.5 rounded-sm text-muted-foreground underline-offset-4 hover:text-foreground hover:underline"
          >
            Published by the Software Engineering chair, University of Rostock
            <ArrowUpRightIcon aria-hidden="true" className="size-3.5" />
          </a>
        )}
        {!thesis && project.homepage && (
          <a
            href={project.homepage}
            target="_blank"
            rel="noopener noreferrer"
            className="relative z-10 inline-flex items-center gap-0.5 rounded-sm font-medium text-primary underline-offset-4 hover:underline"
          >
            Live demo
            <ArrowUpRightIcon aria-hidden="true" className="size-3.5" />
          </a>
        )}
      </div>

      {project.topics.length > 0 && (
        <ul className="mt-3 flex flex-wrap gap-1.5" aria-label="Topics">
          {project.topics.slice(0, 6).map((topic) => (
            <li key={topic}>
              <Badge variant="secondary">{topic}</Badge>
            </li>
          ))}
        </ul>
      )}
    </li>
  );
}

function LoadingRows() {
  return (
    <div role="status" aria-label="Loading projects" className="flex flex-col gap-8 py-4">
      {[0, 1, 2].map((i) => (
        <div key={i} className="flex flex-col gap-2.5">
          <Skeleton className="h-5 w-48" />
          <Skeleton className="h-4 w-full max-w-md" />
          <Skeleton className="h-4 w-2/3 max-w-sm" />
        </div>
      ))}
    </div>
  );
}

export default function Projects() {
  const { state, retry } = useProjects();
  const githubRepos = `${profile.github}?tab=repositories`;

  return (
    <Section id="projects" title="Projects">
      <p className="mb-4 max-w-[65ch] leading-relaxed text-muted-foreground">
        Recently updated repositories, loaded live from GitHub. Their READMEs are also indexed into the assistant, so you
        can ask about any of them.
      </p>

      {state.status === 'loading' && <LoadingRows />}

      {state.status === 'error' && (
        <Alert className="mt-6">
          <TriangleAlertIcon />
          <AlertTitle>Projects couldn't be loaded</AlertTitle>
          <AlertDescription>
            <p>The list of repositories didn't load. Try again, or browse them on GitHub.</p>
            <div className="mt-3 flex flex-wrap gap-2">
              <Button variant="outline" size="sm" onClick={retry}>
                <RefreshCwIcon data-icon="inline-start" />
                Try again
              </Button>
              <Button
                variant="ghost"
                size="sm"
                nativeButton={false}
                render={<a href={githubRepos} target="_blank" rel="noopener noreferrer" />}
              >
                Open GitHub
                <ArrowUpRightIcon data-icon="inline-end" />
              </Button>
            </div>
          </AlertDescription>
        </Alert>
      )}

      {state.status === 'success' && state.projects.length === 0 && (
        <Empty className="mt-6 border">
          <EmptyHeader>
            <EmptyMedia variant="icon">
              <FolderGit2Icon />
            </EmptyMedia>
            <EmptyTitle>No public repositories yet</EmptyTitle>
            <EmptyDescription>New work shows up here as soon as it's pushed to GitHub.</EmptyDescription>
          </EmptyHeader>
          <EmptyContent>
            <Button
              variant="outline"
              size="sm"
              nativeButton={false}
              render={<a href={profile.github} target="_blank" rel="noopener noreferrer" />}
            >
              Open GitHub profile
              <ArrowUpRightIcon data-icon="inline-end" />
            </Button>
          </EmptyContent>
        </Empty>
      )}

      {state.status === 'success' && state.projects.length > 0 && (
        <>
          <ul className="flex flex-col gap-2">
            {state.projects.map((project) => (
              <ProjectRow key={project.id} project={project} />
            ))}
          </ul>
          <a
            href={githubRepos}
            target="_blank"
            rel="noopener noreferrer"
            className="group mt-6 inline-flex items-center gap-1 rounded-sm font-medium underline-offset-4 hover:text-primary hover:underline"
          >
            View all repositories on GitHub
            <ArrowUpRightIcon
              aria-hidden="true"
              className="size-4 transition-transform duration-200 ease-out group-hover:translate-x-0.5 group-hover:-translate-y-0.5"
            />
          </a>
        </>
      )}
    </Section>
  );
}
