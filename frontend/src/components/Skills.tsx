import { profile } from '../profile';
import Section from './Section';

export default function Skills() {
  return (
    <Section id="skills" eyebrow="Skills" title="Technical skills">
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {profile.skills.map((group) => (
          <div key={group.category} className="card p-5">
            <h3 className="text-sm font-semibold text-slate-900 dark:text-white">{group.category}</h3>
            <ul className="mt-3 flex flex-wrap gap-2">
              {group.items.map((item) => (
                <li key={item} className="chip">
                  {item}
                </li>
              ))}
            </ul>
          </div>
        ))}
        <div className="card p-5">
          <h3 className="text-sm font-semibold text-slate-900 dark:text-white">Languages</h3>
          <ul className="mt-3 space-y-2">
            {profile.languages.map((l) => (
              <li key={l.language} className="flex items-center justify-between text-sm">
                <span className="font-medium text-slate-800 dark:text-slate-200">{l.language}</span>
                <span className="text-slate-500 dark:text-slate-400">{l.level}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </Section>
  );
}
