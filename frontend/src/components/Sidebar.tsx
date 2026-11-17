import { MailIcon, MoonIcon, SunIcon } from 'lucide-react';
import type { ComponentType, SVGProps } from 'react';
import { Button } from '@/components/ui/button';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { useActiveSection } from '@/hooks/useActiveSection';
import { useTheme } from '@/hooks/useTheme';
import { cn } from '@/lib/utils';
import { profile } from '@/profile';
import { GitHubIcon, LinkedInIcon } from './icons';

export const SECTIONS = [
  { id: 'ask', label: 'Ask my CV' },
  { id: 'about', label: 'About' },
  { id: 'experience', label: 'Experience' },
  { id: 'education', label: 'Education' },
  { id: 'skills', label: 'Skills' },
  { id: 'projects', label: 'Projects' },
  { id: 'contact', label: 'Contact' },
];
const SECTION_IDS = SECTIONS.map((s) => s.id);

interface IconLinkProps {
  href: string;
  label: string;
  icon: ComponentType<SVGProps<SVGSVGElement>>;
  external?: boolean;
}

function IconLink({ href, label, icon: Icon, external }: IconLinkProps) {
  return (
    <Tooltip>
      <TooltipTrigger
        render={
          <Button
            variant="ghost"
            size="icon-lg"
            nativeButton={false}
            render={<a href={href} {...(external ? { target: '_blank', rel: 'noopener noreferrer' } : {})} />}
            aria-label={label}
            className="text-muted-foreground hover:text-foreground"
          />
        }
      >
        <Icon className="size-5" />
      </TooltipTrigger>
      <TooltipContent>{label}</TooltipContent>
    </Tooltip>
  );
}

function ThemeToggle() {
  const { theme, toggle } = useTheme();
  const label = theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme';
  return (
    <Tooltip>
      <TooltipTrigger
        render={
          <Button
            variant="ghost"
            size="icon-lg"
            onClick={toggle}
            aria-label={label}
            className="text-muted-foreground hover:text-foreground"
          />
        }
      >
        {theme === 'dark' ? <SunIcon className="size-5" /> : <MoonIcon className="size-5" />}
      </TooltipTrigger>
      <TooltipContent>{label}</TooltipContent>
    </Tooltip>
  );
}

export default function Sidebar() {
  const active = useActiveSection(SECTION_IDS);

  return (
    <header className="pt-14 pb-4 sm:pt-20 lg:sticky lg:top-0 lg:flex lg:max-h-screen lg:w-[44%] lg:flex-col lg:justify-between lg:py-24">
      <div>
        <h1 className="text-[2.5rem] leading-none font-semibold tracking-[-0.035em] sm:text-5xl">
          <a href="#top" className="rounded-sm">
            {profile.name}
          </a>
        </h1>
        <p className="mt-4 text-lg font-medium tracking-[-0.01em] sm:text-xl">Full-stack &amp; AI software developer</p>
        <p className="mt-4 max-w-[22rem] leading-relaxed text-muted-foreground">
          I build web applications end to end and research how well LLMs change real-world code.
          Working student at Confovis, M.Sc. student in Erfurt, based in {profile.location.split(',')[0]}.
        </p>

        <nav aria-label="Sections" className="mt-14 hidden lg:block">
          <ul className="flex w-max flex-col gap-1">
            {SECTIONS.map((section) => {
              const isActive = active === section.id;
              return (
                <li key={section.id}>
                  <a
                    href={`#${section.id}`}
                    aria-current={isActive ? 'location' : undefined}
                    className="group flex items-center gap-4 rounded-sm py-2"
                  >
                    <span
                      aria-hidden="true"
                      className={cn(
                        'h-px w-8 bg-muted-foreground/50 transition-[width,background-color] duration-300 ease-out group-hover:w-14 group-hover:bg-foreground',
                        isActive && 'w-14 bg-foreground',
                      )}
                    />
                    <span
                      className={cn(
                        'text-sm font-medium text-muted-foreground transition-colors group-hover:text-foreground',
                        isActive && 'text-foreground',
                      )}
                    >
                      {section.label}
                    </span>
                  </a>
                </li>
              );
            })}
          </ul>
        </nav>
      </div>

      <div className="mt-8 -ml-2.5 flex items-center gap-1 lg:mt-0">
        <IconLink href={profile.github} label="GitHub" icon={GitHubIcon} external />
        <IconLink href={profile.linkedin} label="LinkedIn" icon={LinkedInIcon} external />
        <IconLink href={`mailto:${profile.email}`} label="Email" icon={MailIcon} />
        <span aria-hidden="true" className="mx-1.5 h-5 w-px bg-border" />
        <ThemeToggle />
      </div>
    </header>
  );
}
