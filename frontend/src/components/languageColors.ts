// Colors roughly matching GitHub's linguist palette.
const COLORS: Record<string, string> = {
  TypeScript: '#3178c6',
  JavaScript: '#f1e05a',
  Python: '#3572A5',
  Java: '#b07219',
  Kotlin: '#A97BFF',
  'C#': '#178600',
  C: '#555555',
  'C++': '#f34b7d',
  R: '#198CE7',
  HTML: '#e34c26',
  CSS: '#663399',
  SCSS: '#c6538c',
  Shell: '#89e051',
  Dockerfile: '#384d54',
  Go: '#00ADD8',
  Rust: '#dea584',
  Dart: '#00B4AB',
  Swift: '#F05138',
  PHP: '#4F5D95',
  Vue: '#41b883',
  'Jupyter Notebook': '#DA5B0B',
  TeX: '#3D6117',
  MATLAB: '#e16737',
};

export function languageColor(language?: string | null): string {
  return (language && COLORS[language]) || '#94a3b8';
}
