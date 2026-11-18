export interface Thesis {
  title: string;
  titleOriginal: string;
  grade: string;
}

export interface EducationEntry {
  id: string;
  degree: string;
  institution: string;
  location: string;
  period: string;
  grade: string;
  details: string[];
  thesis?: Thesis;
}

export interface ExperienceEntry {
  id: string;
  role: string;
  company: string;
  location: string;
  period: string;
  current: boolean;
  bullets: string[];
  tools: string[];
}

export interface SkillGroup {
  category: string;
  items: string[];
}

export interface SpokenLanguage {
  language: string;
  level: string;
}

export interface Profile {
  name: string;
  headline: string;
  location: string;
  email: string;
  github: string;
  githubUsername: string;
  linkedin: string;
  about: string;
  education: EducationEntry[];
  experience: ExperienceEntry[];
  skills: SkillGroup[];
  languages: SpokenLanguage[];
}

// The backend omits null fields, so nullable fields may also be absent.
export interface Project {
  id: number;
  name: string;
  description?: string | null;
  url: string;
  homepage?: string | null;
  language?: string | null;
  stars: number;
  forks: number;
  topics: string[];
  fork: boolean;
  pushedAt?: string | null;
}

export interface Source {
  index: number;
  section: string;
  title: string;
  snippet: string;
  score: number;
  url?: string | null;
}

export interface ChatResponse {
  answer: string;
  sources: Source[];
  found: boolean;
  generated: boolean;
  notice?: string | null;
}
