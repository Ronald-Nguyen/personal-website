import { profile } from '../profile';
import { GitHubIcon, LinkedInIcon, MailIcon, MapPinIcon, SparkIcon } from './icons';

export default function Hero() {
  return (
    <section id="top" className="pb-16 pt-16 sm:pb-24 sm:pt-24">
      <div className="container-page">
        <a
          href="#contact"
          className="inline-flex items-center gap-2 rounded-full border border-emerald-300/60 bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-800 dark:border-emerald-700/50 dark:bg-emerald-900/20 dark:text-emerald-300"
        >
          <span className="relative flex h-2 w-2">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-60" />
            <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
          </span>
          Open to Master's thesis opportunities from April 2027
        </a>

        <h1 className="mt-6 text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white sm:text-6xl">
          {profile.name}
        </h1>
        <p className="mt-4 max-w-3xl text-lg font-medium text-slate-700 dark:text-slate-300 sm:text-2xl">
          {profile.headline}
        </p>
        <p className="mt-3 flex items-center gap-1.5 text-sm text-slate-500 dark:text-slate-400">
          <MapPinIcon size={16} /> {profile.location}
        </p>

        <div className="mt-8 flex flex-wrap gap-3">
          <a href="#ask" className="btn-primary">
            <SparkIcon size={18} /> Ask my CV
          </a>
          <a href="#projects" className="btn-secondary">
            View projects
          </a>
        </div>

        <div className="mt-8 flex flex-wrap gap-x-6 gap-y-3 text-sm">
          <a
            href={profile.github}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-slate-600 hover:text-accent-600 dark:text-slate-400 dark:hover:text-accent-400"
          >
            <GitHubIcon size={18} /> github.com/{profile.githubUsername}
          </a>
          <a
            href={profile.linkedin}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 text-slate-600 hover:text-accent-600 dark:text-slate-400 dark:hover:text-accent-400"
          >
            <LinkedInIcon size={18} /> LinkedIn
          </a>
          <a
            href={`mailto:${profile.email}`}
            className="inline-flex items-center gap-2 text-slate-600 hover:text-accent-600 dark:text-slate-400 dark:hover:text-accent-400"
          >
            <MailIcon size={18} /> {profile.email}
          </a>
        </div>
      </div>
    </section>
  );
}
