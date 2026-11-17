const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

/** "07/2026 – Present" → "Jul 2026 – Present". Leaves anything else untouched. */
export function formatPeriod(period: string): string {
  return period.replace(/\b(0[1-9]|1[0-2])\/(\d{4})\b/g, (_, m: string, y: string) => `${MONTHS[Number(m) - 1]} ${y}`);
}

export function formatMonthYear(iso?: string | null): string | null {
  if (!iso) return null;
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return null;
  return `${MONTHS[date.getMonth()]} ${date.getFullYear()}`;
}
