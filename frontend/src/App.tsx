import { useEffect } from 'react';
import { TooltipProvider } from '@/components/ui/tooltip';
import AskCv from './components/AskCv';
import { Contact, Footer } from './components/Contact';
import { About, Education, Experience, Skills } from './components/CvSections';
import LegalPageView, { legalPageFor } from './components/LegalPages';
import Projects from './components/Projects';
import Sidebar from './components/Sidebar';

export default function App() {
  const legalPage = legalPageFor(window.location.pathname);
  return legalPage ? <LegalPageView page={legalPage} /> : <Portfolio />;
}

function Portfolio() {
  // The browser tries the initial #hash jump before React has rendered the sections.
  useEffect(() => {
    const id = decodeURIComponent(window.location.hash.slice(1));
    if (id) document.getElementById(id)?.scrollIntoView();
  }, []);

  return (
    <TooltipProvider delay={250}>
      <a
        href="#main"
        className="sr-only z-50 rounded-md bg-primary px-3 py-2 text-sm font-medium text-primary-foreground focus:not-sr-only focus:fixed focus:top-3 focus:left-3"
      >
        Skip to content
      </a>
      <div
        id="top"
        className="mx-auto min-h-screen max-w-6xl px-6 sm:px-10 lg:flex lg:justify-between lg:gap-12 lg:px-12"
      >
        <Sidebar />
        <main id="main" tabIndex={-1} className="pt-6 outline-none lg:w-[54%] lg:py-24">
          <AskCv />
          <About />
          <Experience />
          <Education />
          <Skills />
          <Projects />
          <Contact />
          <Footer />
        </main>
      </div>
    </TooltipProvider>
  );
}
