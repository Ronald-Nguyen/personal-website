import { profile } from '../profile';
import { BriefcaseIcon } from './icons';
import Section from './Section';

export default function Experience() {
  return (
    <Section id="experience" eyebrow="Experience" title="Work experience">
      <ol className="relative space-y-8 border-l border-slate-200 pl-6 dark:border-slate-800 sm:pl-8">
        {profile.experience.map((job) => (
          <li key={job.id} className="relative">
            <span
              aria-hidden
              className={`absolute -left-[2.28rem] top-1.5 flex h-6 w-6 items-center justify-center rounded-full border sm:-left-[2.78rem] ${
                job.current
                  ? 'border-accent-500 bg-accent-600 text-white'
                  : 'border-slate-300 bg-white text-slate-500 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-400'
              }`}
            >
              <BriefcaseIcon size={12} />
            </span>
            <div className="card p-5 sm:p-6">
              <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
                <div>
                  <h3 className="text-base font-semibold text-slate-900 dark:text-white sm:text-lg">{job.role}</h3>
                  <p className="text-sm font-medium text-accent-700 dark:text-accent-400">
                    {job.company} · {job.location}
                  </p>
                </div>
                <p className="shrink-0 font-mono text-xs text-slate-500 dark:text-slate-400 sm:pt-1">
                  {job.period}
                  {job.current && (
                    <span className="ml-2 rounded-full bg-accent-100 px-2 py-0.5 font-sans font-semibold text-accent-800 dark:bg-accent-900/50 dark:text-accent-200">
                      Current
                    </span>
                  )}
                </p>
              </div>
              <ul className="mt-4 list-disc space-y-1.5 pl-5 text-sm leading-relaxed marker:text-slate-400">
                {job.bullets.map((b) => (
                  <li key={b}>{b}</li>
                ))}
              </ul>
              {job.tools.length > 0 && (
                <ul className="mt-4 flex flex-wrap gap-2" aria-label="Technologies">
                  {job.tools.map((t) => (
                    <li key={t} className="chip">
                      {t}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </li>
        ))}
      </ol>
    </Section>
  );
}
