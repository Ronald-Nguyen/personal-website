import { Badge } from '@/components/ui/badge';
import { formatPeriod } from '@/lib/format';
import { profile } from '@/profile';
import Section from './Section';

function Period({ children }: { children: string }) {
  return (
    <p className="text-[0.8125rem] leading-6 font-medium text-muted-foreground tabular-nums sm:pt-px">
      {formatPeriod(children)}
    </p>
  );
}

export function About() {
  return (
    <Section id="about" title="About">
      <div className="flex max-w-[65ch] flex-col gap-4 leading-relaxed text-muted-foreground">
        <p>
          I'm a Master's student in Applied Computer Science, focused on{' '}
          <strong className="font-medium text-foreground">AI-assisted software engineering</strong> and{' '}
          <strong className="font-medium text-foreground">full-stack enterprise applications</strong>.
        </p>
        <p>
          I've built applications end to end with Angular and React on the front, C#/ASP.NET and Java behind them. My
          Bachelor's thesis measured how well LLM-generated code transformations hold up in real software systems, and
          it was graded 1.0. Today I work on inspection software for the semiconductor industry at Confovis.
        </p>
        <p>
          This site is a small example of how I like to work: the assistant above is a retrieval pipeline I wrote in
          Jakarta EE and PostgreSQL, with no external AI service behind it.
        </p>
      </div>
    </Section>
  );
}

export function Experience() {
  return (
    <Section id="experience" title="Experience">
      <ol className="flex flex-col gap-12">
        {profile.experience.map((job) => (
          <li key={job.id} className="grid gap-x-6 gap-y-1 sm:grid-cols-[8.5rem_1fr]">
            <Period>{job.period}</Period>
            <div>
              <h3 className="flex flex-wrap items-center gap-x-2 gap-y-1 leading-6 font-medium">
                {job.role}
                {job.current && <Badge variant="secondary">Current</Badge>}
              </h3>
              <p className="text-[0.9375rem] text-muted-foreground">
                {job.company}, {job.location}
              </p>
              <ul className="mt-3 flex list-disc flex-col gap-1.5 pl-4 text-[0.9375rem] leading-relaxed text-muted-foreground marker:text-border">
                {job.bullets.map((bullet) => (
                  <li key={bullet}>{bullet}</li>
                ))}
              </ul>
              {job.tools.length > 0 && (
                <ul className="mt-4 flex flex-wrap gap-1.5" aria-label="Technologies">
                  {job.tools.map((tool) => (
                    <li key={tool}>
                      <Badge variant="outline">{tool}</Badge>
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

export function Education() {
  return (
    <Section id="education" title="Education">
      <ol className="flex flex-col gap-12">
        {profile.education.map((edu) => (
          <li key={edu.id} className="grid gap-x-6 gap-y-1 sm:grid-cols-[8.5rem_1fr]">
            <Period>{edu.period}</Period>
            <div>
              <h3 className="leading-6 font-medium">{edu.degree}</h3>
              <p className="text-[0.9375rem] text-muted-foreground">{edu.institution}</p>
              <p className="mt-1 text-[0.9375rem] text-muted-foreground">{edu.grade}</p>
              {edu.thesis && (
                <figure className="mt-4 rounded-lg bg-muted px-4 py-3.5">
                  <figcaption className="text-[0.8125rem] font-medium text-muted-foreground">
                    Bachelor's thesis, graded {edu.thesis.grade}
                  </figcaption>
                  <p className="mt-1.5 leading-snug font-medium">{edu.thesis.title}</p>
                  <p lang="de" className="mt-1.5 text-sm leading-snug text-muted-foreground italic">
                    {edu.thesis.titleOriginal}
                  </p>
                </figure>
              )}
            </div>
          </li>
        ))}
      </ol>
    </Section>
  );
}

export function Skills() {
  return (
    <Section id="skills" title="Skills">
      <dl className="flex flex-col gap-6">
        {profile.skills.map((group) => (
          <div key={group.category} className="grid gap-x-6 gap-y-2 sm:grid-cols-[8.5rem_1fr]">
            <dt className="text-[0.8125rem] leading-6 font-medium text-muted-foreground">{group.category}</dt>
            <dd>
              <ul className="flex flex-wrap gap-1.5">
                {group.items.map((item) => (
                  <li key={item}>
                    <Badge variant="secondary">{item}</Badge>
                  </li>
                ))}
              </ul>
            </dd>
          </div>
        ))}
        <div className="grid gap-x-6 gap-y-2 sm:grid-cols-[8.5rem_1fr]">
          <dt className="text-[0.8125rem] leading-6 font-medium text-muted-foreground">Languages</dt>
          <dd className="text-[0.9375rem] leading-6">
            {profile.languages.map((l, i) => (
              <span key={l.language}>
                {i > 0 && ', '}
                {l.language} <span className="text-muted-foreground">({l.level})</span>
              </span>
            ))}
          </dd>
        </div>
      </dl>
    </Section>
  );
}
