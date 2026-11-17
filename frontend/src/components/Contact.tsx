import { profile } from '../profile';
import { GitHubIcon, LinkedInIcon, MailIcon, MapPinIcon } from './icons';
import Section from './Section';

export default function Contact() {
  const items = [
    { icon: <MailIcon />, label: 'Email', value: profile.email, href: `mailto:${profile.email}`, external: false },
    { icon: <LinkedInIcon />, label: 'LinkedIn', value: 'in/ronald-nguyen', href: profile.linkedin, external: true },
    { icon: <GitHubIcon />, label: 'GitHub', value: profile.githubUsername, href: profile.github, external: true },
  ];
  return (
    <Section
      id="contact"
      eyebrow="Contact"
      title="Let's talk"
      intro="Always happy to chat about full-stack development and AI-assisted software engineering."
    >
      <div className="grid gap-4 sm:grid-cols-3">
        {items.map((item) => (
          <a
            key={item.label}
            href={item.href}
            {...(item.external ? { target: '_blank', rel: 'noopener noreferrer' } : {})}
            className="card group flex items-center gap-4 p-5 transition hover:border-accent-300 dark:hover:border-accent-700"
          >
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-accent-50 text-accent-700 transition group-hover:bg-accent-600 group-hover:text-white dark:bg-accent-900/40 dark:text-accent-300">
              {item.icon}
            </span>
            <span className="min-w-0">
              <span className="block text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
                {item.label}
              </span>
              <span className="block truncate font-medium text-slate-900 dark:text-white">{item.value}</span>
            </span>
          </a>
        ))}
      </div>
      <p className="mt-6 flex items-center gap-1.5 text-sm text-slate-500 dark:text-slate-400">
        <MapPinIcon size={16} /> Based in {profile.location}
      </p>
    </Section>
  );
}
