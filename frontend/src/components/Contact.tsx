import { ArrowUpRightIcon, MailIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { profile } from '@/profile';
import { GitHubIcon, LinkedInIcon } from './icons';
import { LegalLinks } from './LegalPages';
import Section from './Section';

export function Contact() {
  return (
    <Section id="contact" title="Contact">
      <p className="max-w-[34ch] text-2xl leading-snug font-medium tracking-[-0.02em] text-balance">
        Always happy to talk about full-stack development, AI-assisted software engineering and job opportunities.
      </p>
      <p className="mt-3 max-w-[65ch] leading-relaxed text-muted-foreground">
        The quickest way to reach me is email:{' '}
        <a
          href={`mailto:${profile.email}`}
          className="rounded-sm font-medium break-all text-foreground underline decoration-border underline-offset-4 hover:decoration-primary"
        >
          {profile.email}
        </a>
      </p>
      <div className="mt-6 flex flex-wrap gap-2">
        <Button size="lg" nativeButton={false} render={<a href={`mailto:${profile.email}`} />}>
          <MailIcon data-icon="inline-start" />
          Email me
        </Button>
        <Button
          size="lg"
          variant="outline"
          nativeButton={false}
          render={<a href={profile.linkedin} target="_blank" rel="noopener noreferrer" />}
        >
          <LinkedInIcon data-icon="inline-start" />
          LinkedIn
          <ArrowUpRightIcon data-icon="inline-end" className="text-muted-foreground" />
        </Button>
        <Button
          size="lg"
          variant="outline"
          nativeButton={false}
          render={<a href={profile.github} target="_blank" rel="noopener noreferrer" />}
        >
          <GitHubIcon data-icon="inline-start" />
          GitHub
          <ArrowUpRightIcon data-icon="inline-end" className="text-muted-foreground" />
        </Button>
      </div>
    </Section>
  );
}

export function Footer() {
  return (
    <footer className="max-w-[65ch] pb-16 text-[0.8125rem] leading-relaxed text-muted-foreground lg:pb-0">
      <p>
        Built with React, Tailwind CSS and shadcn/ui on a Jakarta EE and PostgreSQL backend. Set in Geist.{' '}
        <span className="tabular-nums">© {new Date().getFullYear()}</span> {profile.name}.
      </p>
      <LegalLinks className="mt-3" />
    </footer>
  );
}
