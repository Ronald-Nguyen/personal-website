import { profile } from '../profile';
import { GradIcon } from './icons';
import Section from './Section';

export default function Education() {
  return (
    <Section id="education" eyebrow="Education" title="Education">
      <div className="grid gap-5 md:grid-cols-3">
        {profile.education.map((edu) => (
          <article key={edu.id} className={`card flex flex-col p-5 sm:p-6 ${edu.thesis ? 'md:col-span-2' : ''}`}>
            <div className="flex items-start gap-3">
              <span className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-accent-50 text-accent-700 dark:bg-accent-900/40 dark:text-accent-300">
                <GradIcon size={18} />
              </span>
              <div>
                <h3 className="font-semibold text-slate-900 dark:text-white">{edu.degree}</h3>
                <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400">
                  {edu.institution} · {edu.location}
                </p>
              </div>
            </div>
            <div className="mt-4 flex flex-wrap items-center gap-2 text-xs">
              <span className="font-mono text-slate-500 dark:text-slate-400">{edu.period}</span>
              <span className="chip">{edu.grade}</span>
            </div>
            {edu.thesis && (
              <div className="mt-5 rounded-xl border border-accent-200 bg-accent-50/60 p-4 dark:border-accent-800/60 dark:bg-accent-900/20">
                <p className="text-xs font-semibold uppercase tracking-wide text-accent-700 dark:text-accent-300">
                  Bachelor's thesis · Grade {edu.thesis.grade}
                </p>
                <p className="mt-2 font-medium text-slate-900 dark:text-white">“{edu.thesis.title}”</p>
                <p lang="de" className="mt-1 text-sm italic text-slate-600 dark:text-slate-400">
                  „{edu.thesis.titleOriginal}“
                </p>
              </div>
            )}
          </article>
        ))}
      </div>
    </Section>
  );
}
