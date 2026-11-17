import type { ReactNode } from 'react';

interface Props {
  id: string;
  title: string;
  children: ReactNode;
}

export default function Section({ id, title, children }: Props) {
  return (
    <section id={id} aria-labelledby={`${id}-title`} className="mb-20 scroll-mt-4 lg:mb-28 lg:scroll-mt-24">
      <h2
        id={`${id}-title`}
        className="sticky top-0 z-20 -mx-6 mb-2 bg-background px-6 py-4 text-base font-semibold tracking-[-0.01em] sm:-mx-10 sm:px-10 lg:static lg:mx-0 lg:mb-6 lg:bg-transparent lg:px-0 lg:py-0 lg:text-lg"
      >
        {title}
      </h2>
      {children}
    </section>
  );
}
