import { profile } from '../profile';
import { GitHubIcon, LinkedInIcon, MailIcon } from './icons';

export default function SocialLinks({ className = '' }: { className?: string }) {
  return (
    <div className={`flex items-center gap-1 ${className}`}>
      <a className="icon-btn" href={profile.github} target="_blank" rel="noopener noreferrer" aria-label="GitHub profile">
        <GitHubIcon />
      </a>
      <a className="icon-btn" href={profile.linkedin} target="_blank" rel="noopener noreferrer" aria-label="LinkedIn profile">
        <LinkedInIcon />
      </a>
      <a className="icon-btn" href={`mailto:${profile.email}`} aria-label={`Email ${profile.email}`}>
        <MailIcon />
      </a>
    </div>
  );
}
