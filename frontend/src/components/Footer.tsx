import { profile } from '../profile';
import SocialLinks from './SocialLinks';

export default function Footer() {
  return (
    <footer className="border-t border-slate-200 py-8 dark:border-slate-800">
      <div className="container-page flex flex-col items-center justify-between gap-4 sm:flex-row">
        <p className="text-sm text-slate-500 dark:text-slate-400">
          © {new Date().getFullYear()} {profile.name} · Built with React, Jakarta EE &amp; pgvector
        </p>
        <div className="flex items-center gap-2">
          <a href={`mailto:${profile.email}`} className="text-sm font-medium text-slate-600 hover:text-accent-600 dark:text-slate-400 dark:hover:text-accent-400">
            {profile.email}
          </a>
          <SocialLinks />
        </div>
      </div>
    </footer>
  );
}
