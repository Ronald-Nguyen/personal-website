import { profile } from '../profile';
import Section from './Section';

const HIGHLIGHTS = [
  { label: 'Focus', value: 'AI-assisted software engineering' },
  { label: 'Builds', value: 'Full-stack enterprise apps' },
  { label: 'Research', value: 'LLM evaluation pipelines' },
  { label: 'Available', value: 'Master’s thesis · April 2027' },
];

export default function About() {
  return (
    <Section id="about" eyebrow="About" title="About me">
      <div className="grid gap-8 lg:grid-cols-5">
        <p className="text-lg leading-relaxed text-slate-700 dark:text-slate-300 lg:col-span-3">{profile.about}</p>
        <dl className="grid grid-cols-2 gap-3 lg:col-span-2">
          {HIGHLIGHTS.map((h) => (
            <div key={h.label} className="card p-4">
              <dt className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                {h.label}
              </dt>
              <dd className="mt-1 text-sm font-semibold text-slate-900 dark:text-white">{h.value}</dd>
            </div>
          ))}
        </dl>
      </div>
    </Section>
  );
}
