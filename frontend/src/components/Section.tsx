import type { ReactNode } from 'react';

interface Props {
  id: string;
  eyebrow: string;
  title: string;
  intro?: ReactNode;
  children: ReactNode;
  className?: string;
}

export default function Section({ id, eyebrow, title, intro, children, className = '' }: Props) {
  return (
    <section id={id} aria-labelledby={`${id}-title`} className={`py-16 sm:py-20 ${className}`}>
      <div className="container-page">
        <p className="font-mono text-xs font-semibold uppercase tracking-[0.2em] text-accent-600 dark:text-accent-400">
          {eyebrow}
        </p>
        <h2 id={`${id}-title`} className="mt-2 text-2xl font-bold tracking-tight text-slate-900 dark:text-white sm:text-3xl">
          {title}
        </h2>
        {intro && <div className="mt-3 max-w-2xl text-slate-600 dark:text-slate-400">{intro}</div>}
        <div className="mt-10">{children}</div>
      </div>
    </section>
  );
}
