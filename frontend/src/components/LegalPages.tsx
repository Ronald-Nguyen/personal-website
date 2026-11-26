import { ArrowLeftIcon } from 'lucide-react';
import { useEffect, type ReactNode } from 'react';
import { legal } from '@/legal';
import { profile } from '@/profile';

export const LEGAL_ROUTES = {
  '/impressum': 'impressum',
  '/datenschutz': 'datenschutz',
} as const;
export type LegalPage = (typeof LEGAL_ROUTES)[keyof typeof LEGAL_ROUTES];

export function legalPageFor(pathname: string): LegalPage | null {
  const path = pathname.replace(/\/+$/, '').toLowerCase();
  return (LEGAL_ROUTES as Record<string, LegalPage>)[path] ?? null;
}

const link = 'rounded-sm font-medium text-foreground underline decoration-border underline-offset-4 hover:decoration-primary';

function Ext({ href, children }: { href: string; children: ReactNode }) {
  return (
    <a href={href} target="_blank" rel="noopener noreferrer" className={link}>
      {children}
    </a>
  );
}

function H2({ children }: { children: ReactNode }) {
  return <h2 className="mt-10 mb-3 text-lg font-semibold tracking-[-0.01em]">{children}</h2>;
}

function H3({ children }: { children: ReactNode }) {
  return <h3 className="mt-6 mb-2 font-semibold">{children}</h3>;
}

function Address() {
  return (
    <address className="not-italic">
      {profile.name}
      <br />
      {legal.careOf && (
        <>
          c/o {legal.careOf}
          <br />
        </>
      )}
      {legal.street}
      <br />
      {legal.postalCode} {legal.city}
      <br />
      {legal.country}
    </address>
  );
}

function Contact() {
  return (
    <p>
      E-Mail:{' '}
      <a href={`mailto:${profile.email}`} className={link}>
        {profile.email}
      </a>
      {legal.phone && (
        <>
          <br />
          Telefon: {legal.phone}
        </>
      )}
    </p>
  );
}

function Impressum() {
  return (
    <>
      <h1 className="text-3xl font-semibold tracking-[-0.03em]">Impressum</h1>
      <p className="mt-2 text-sm">Legal notice pursuant to § 5 DDG (German Digital Services Act).</p>

      <H2>Angaben gemäß § 5 DDG</H2>
      <Address />

      <H2>Kontakt</H2>
      <Contact />

      <H2>Verantwortlich für den Inhalt nach § 18 Abs. 2 MStV</H2>
      <Address />

      <H2>Haftung für Inhalte</H2>
      <p>
        Die Inhalte dieser Seite wurden mit größter Sorgfalt erstellt. Für die Richtigkeit, Vollständigkeit und
        Aktualität der Inhalte kann ich jedoch keine Gewähr übernehmen. Die Antworten des Assistenten „Ask my CV“
        werden automatisiert von einer KI erzeugt; sie können unvollständig oder fehlerhaft sein und stellen keine
        verbindlichen Aussagen dar. Maßgeblich sind die Angaben im Lebenslauf auf dieser Seite.
      </p>

      <H2>Haftung für Links</H2>
      <p>
        Diese Seite enthält Links zu externen Websites Dritter (z.&nbsp;B. GitHub, LinkedIn), auf deren Inhalte ich
        keinen Einfluss habe. Für diese fremden Inhalte ist stets der jeweilige Anbieter verantwortlich. Zum
        Zeitpunkt der Verlinkung waren keine Rechtsverstöße erkennbar. Bei Bekanntwerden von Rechtsverletzungen werde
        ich derartige Links umgehend entfernen.
      </p>

      <H2>Urheberrecht</H2>
      <p>
        Die von mir erstellten Inhalte und Werke auf dieser Seite unterliegen dem deutschen Urheberrecht. Für den
        Quellcode gelten die Lizenzen der jeweiligen GitHub-Repositories.
      </p>
    </>
  );
}

function Datenschutz() {
  return (
    <>
      <h1 className="text-3xl font-semibold tracking-[-0.03em]">Datenschutzerklärung</h1>
      <p className="mt-2 text-sm">Privacy policy pursuant to Art. 13 GDPR. Stand: {legal.privacyUpdated}</p>

      <H2>1. Verantwortlicher</H2>
      <Address />
      <Contact />

      <H2>2. Überblick</H2>
      <p>
        Diese Website ist ein privates Portfolio. Es gibt keine Benutzerkonten, keine Werbung, keine Analyse- oder
        Tracking-Dienste und keine Social-Media-Plugins. Schriftarten werden von diesem Server ausgeliefert, nicht von
        Drittanbietern. Es werden keine Cookies zu Analyse- oder Marketingzwecken gesetzt, daher gibt es auch keinen
        Cookie-Banner.
      </p>

      <H2>3. Hosting und Bereitstellung der Website</H2>
      <p>
        Die Website wird von mir selbst auf einem eigenen Server in Deutschland betrieben. Beim Aufruf verarbeitet der
        Webserver technisch notwendige Daten: IP-Adresse, Datum und Uhrzeit, aufgerufene Adresse, übertragene
        Datenmenge, Referrer sowie Browser und Betriebssystem (User-Agent). In den Server-Logdateien wird die
        IP-Adresse nur gekürzt gespeichert (letztes Oktett bzw. bei IPv6 die letzten 80 Bit entfernt). Die Logdateien
        sind in ihrer Größe begrenzt und werden laufend überschrieben; ältere Einträge werden so automatisch gelöscht.
      </p>
      <p className="mt-3">
        Rechtsgrundlage ist Art. 6 Abs. 1 lit. f DSGVO. Mein berechtigtes Interesse liegt in der sicheren und
        stabilen Bereitstellung der Website sowie in der Abwehr von Missbrauch.
      </p>

      <H3>Cloudflare</H3>
      <p>
        Die Verbindung zur Website läuft über das Netzwerk der Cloudflare, Inc., 101 Townsend St., San Francisco, CA
        94107, USA („Cloudflare Tunnel“). Cloudflare nimmt dabei alle Anfragen entgegen, leitet sie an meinen Server
        weiter und schützt die Seite vor Angriffen. Dabei verarbeitet Cloudflare insbesondere Ihre IP-Adresse und die
        oben genannten Verbindungsdaten. Cloudflare kann zur Abwehr automatisierter Angriffe (Bot-Erkennung) ein
        technisch notwendiges Cookie („__cf_bm“, Laufzeit 30 Minuten) setzen; dafür ist nach § 25 Abs. 2 Nr. 2 TDDDG
        keine Einwilligung erforderlich. Mit Cloudflare besteht ein Vertrag zur Auftragsverarbeitung (Art. 28 DSGVO). Cloudflare ist
        unter dem EU-US Data Privacy Framework zertifiziert (Angemessenheitsbeschluss, Art. 45 DSGVO); ergänzend gelten
        Standardvertragsklauseln. Rechtsgrundlage ist Art. 6 Abs. 1 lit. f DSGVO (sichere und performante
        Bereitstellung). Cloudflare speichert Verbindungsdaten nach eigenen Angaben nur so lange, wie es für
        Sicherheit und Betrieb erforderlich ist. Weitere Informationen:{' '}
        <Ext href="https://www.cloudflare.com/privacypolicy/">Datenschutzerklärung von Cloudflare</Ext>.
      </p>

      <H2>4. Assistent „Ask my CV“</H2>
      <p>
        Wenn Sie dem Assistenten eine Frage stellen, wird der Text Ihrer Frage an meinen Server übermittelt. Dort
        werden passende Passagen aus meinem Lebenslauf und meinen GitHub-Repositories gesucht. Anschließend werden
        Ihre Frage und diese Passagen an die Mistral AI SAS, 15 rue des Halles, 75001 Paris, Frankreich, übermittelt,
        deren Sprachmodell die Antwort formuliert. Ihre IP-Adresse oder andere Kennungen werden dabei nicht an Mistral
        weitergegeben.
      </p>
      <p className="mt-3">
        Ihre IP-Adresse wird auf meinem Server ausschließlich im Arbeitsspeicher verwendet, um die Anzahl der Anfragen
        pro Besucher zu begrenzen (Missbrauchsschutz); sie wird nicht dauerhaft gespeichert und spätestens nach einer
        Stunde verworfen. Fragen und Antworten werden zur Entlastung des Dienstes bis zu sechs Stunden im
        Arbeitsspeicher zwischengespeichert, ohne Bezug zu Ihrer Person, und nicht protokolliert. Mistral speichert
        API-Anfragen nach eigenen Angaben für bis zu 30 Tage zur Missbrauchserkennung. Der Nutzung meiner
        API-Anfragen zum Training der Modelle von Mistral habe ich in den Datenschutzeinstellungen meines
        Mistral-Kontos widersprochen.
      </p>
      <p className="mt-3">
        <strong className="font-semibold text-foreground">Bitte geben Sie in Fragen keine personenbezogenen Daten
        ein.</strong>{' '}
        Die Nutzung des Assistenten ist freiwillig. Rechtsgrundlage ist Art. 6 Abs. 1 lit. f DSGVO; mein berechtigtes
        Interesse ist, Besuchern eine bequeme Möglichkeit zu bieten, sich über meinen Werdegang zu informieren. Mit
        Mistral besteht ein Vertrag zur Auftragsverarbeitung. Weitere Informationen:{' '}
        <Ext href="https://legal.mistral.ai/terms/privacy-policy">Datenschutzerklärung von Mistral AI</Ext>.
      </p>
      <p className="mt-3">
        Hinweis nach Art. 50 der KI-Verordnung (EU) 2024/1689: Sie interagieren hier mit einem KI-System. Die Antworten
        werden automatisch erzeugt und können fehlerhaft sein.
      </p>

      <H2>5. Lokale Speicherung im Browser</H2>
      <p>
        Wenn Sie zwischen hellem und dunklem Design wechseln, wird Ihre Wahl im lokalen Speicher Ihres Browsers
        (localStorage, Schlüssel „theme“) abgelegt, damit sie beim nächsten Besuch erhalten bleibt. Diese Information
        verlässt Ihr Gerät nicht. Die Speicherung ist für den von Ihnen ausdrücklich gewünschten Dienst erforderlich
        (§ 25 Abs. 2 Nr. 2 TDDDG). Sie können sie jederzeit über die Einstellungen Ihres Browsers löschen.
      </p>

      <H2>6. Projekt-Demos</H2>
      <p>
        Die Pokédex-Demo unter /demos/pokedex/ lädt Daten und Bilder direkt in Ihrem Browser von der öffentlichen
        PokéAPI (pokeapi.co) und von GitHub (raw.githubusercontent.com, GitHub Inc., USA). Dabei werden Ihre
        IP-Adresse und Browserdaten an diese Anbieter übermittelt. Diese Übermittlung findet nur statt, wenn Sie die
        Demo aktiv öffnen. Rechtsgrundlage ist Art. 6 Abs. 1 lit. f DSGVO (Darstellung der Demo).
      </p>
      <p className="mt-3">
        Die Projektliste auf dieser Seite wird von meinem Server bei GitHub abgerufen; dabei werden keine Daten von
        Besuchern an GitHub übermittelt.
      </p>

      <H2>7. Kontakt per E-Mail</H2>
      <p>
        Wenn Sie mir eine E-Mail schreiben, verarbeite ich Ihre Angaben (E-Mail-Adresse, Name, Inhalt), um Ihre
        Anfrage zu beantworten. Rechtsgrundlage ist Art. 6 Abs. 1 lit. b DSGVO, sofern es um eine mögliche
        Zusammenarbeit oder ein Beschäftigungsverhältnis geht, sonst Art. 6 Abs. 1 lit. f DSGVO. Die E-Mails werden
        gelöscht, sobald sie für die Bearbeitung nicht mehr erforderlich sind und keine gesetzlichen
        Aufbewahrungspflichten bestehen. Mein E-Mail-Postfach wird von der Google Ireland Limited, Gordon House,
        Barrow Street, Dublin 4, Irland (Gmail) bereitgestellt; dabei ist eine Übermittlung an die Google LLC in den USA
        möglich. Google ist unter dem EU-US Data Privacy Framework zertifiziert (Art. 45 DSGVO).
      </p>

      <H2>8. Externe Links</H2>
      <p>
        Links zu GitHub, LinkedIn und anderen Seiten sind normale Hyperlinks. Erst wenn Sie einen Link anklicken,
        werden Daten an den jeweiligen Anbieter übertragen; dort gilt dessen Datenschutzerklärung.
      </p>

      <H2>9. Speicherdauer im Überblick</H2>
      <ul className="list-disc space-y-1 pl-5">
        <li>Server-Logdateien (mit gekürzter IP): größenbegrenzt, werden fortlaufend überschrieben</li>
        <li>IP-Adresse für die Anfragebegrenzung des Assistenten: nur im Arbeitsspeicher, höchstens 1 Stunde</li>
        <li>Zwischengespeicherte Fragen und Antworten: nur im Arbeitsspeicher, höchstens 6 Stunden</li>
        <li>Bei Mistral AI: bis zu 30 Tage (Missbrauchserkennung)</li>
        <li>Theme-Einstellung im Browser: bis Sie sie löschen</li>
        <li>E-Mails: bis die Anfrage erledigt ist, sofern keine Aufbewahrungspflicht besteht</li>
      </ul>

      <H2>10. SSL-/TLS-Verschlüsselung</H2>
      <p>
        Diese Seite nutzt aus Sicherheitsgründen eine TLS-Verschlüsselung (erkennbar an „https://“ in der
        Adresszeile). Daten, die Sie an diese Website übermitteln, können so nicht von Dritten mitgelesen werden.
      </p>

      <H2>11. Ihre Rechte</H2>
      <p>Sie haben gegenüber mir folgende Rechte hinsichtlich der Sie betreffenden personenbezogenen Daten:</p>
      <ul className="mt-2 list-disc space-y-1 pl-5">
        <li>Auskunft (Art. 15 DSGVO)</li>
        <li>Berichtigung (Art. 16 DSGVO)</li>
        <li>Löschung (Art. 17 DSGVO)</li>
        <li>Einschränkung der Verarbeitung (Art. 18 DSGVO)</li>
        <li>Datenübertragbarkeit (Art. 20 DSGVO)</li>
      </ul>
      <div className="mt-5 rounded-lg border border-foreground/30 p-4 text-foreground">
        <p className="font-semibold">Widerspruchsrecht nach Art. 21 DSGVO</p>
        <p className="mt-2">
          Soweit ich Ihre Daten auf Grundlage von Art. 6 Abs. 1 lit. f DSGVO (berechtigtes Interesse) verarbeite,
          haben Sie das Recht, aus Gründen, die sich aus Ihrer besonderen Situation ergeben, jederzeit Widerspruch
          gegen diese Verarbeitung einzulegen. Ich verarbeite Ihre Daten dann nicht mehr, es sei denn, ich kann
          zwingende schutzwürdige Gründe nachweisen, die Ihre Interessen, Rechte und Freiheiten überwiegen, oder die
          Verarbeitung dient der Geltendmachung, Ausübung oder Verteidigung von Rechtsansprüchen. Ein formloser
          Widerspruch per E-Mail genügt.
        </p>
      </div>
      <p className="mt-3">
        Außerdem haben Sie das Recht, sich bei einer Datenschutz-Aufsichtsbehörde zu beschweren (Art. 77 DSGVO),
        etwa beim{' '}
        <Ext href="https://www.tlfdi.de/">
          Thüringer Landesbeauftragten für den Datenschutz und die Informationsfreiheit
        </Ext>
        .
      </p>
      <p className="mt-3">
        Eine automatisierte Entscheidungsfindung einschließlich Profiling (Art. 22 DSGVO) findet nicht statt. Die
        Bereitstellung Ihrer Daten ist weder gesetzlich noch vertraglich vorgeschrieben; ohne die technisch
        notwendigen Verbindungsdaten kann die Website jedoch nicht angezeigt werden.
      </p>
    </>
  );
}

export default function LegalPageView({ page }: { page: LegalPage }) {
  // Own title per page; keep the postal address out of search results.
  useEffect(() => {
    document.title = `${page === 'impressum' ? 'Impressum' : 'Datenschutzerklärung'} – ${profile.name}`;
    const robots = document.createElement('meta');
    robots.name = 'robots';
    robots.content = 'noindex, follow';
    document.head.appendChild(robots);
    return () => robots.remove();
  }, [page]);

  return (
    <div className="mx-auto min-h-screen max-w-3xl px-6 py-14 sm:px-10 sm:py-20">
      <a
        href="/"
        className="-ml-1 inline-flex items-center gap-1.5 rounded-sm px-1 text-sm font-medium text-muted-foreground hover:text-foreground"
      >
        <ArrowLeftIcon className="size-4" aria-hidden="true" />
        {profile.name}
      </a>
      <main id="main" lang="de" className="mt-10 leading-relaxed text-muted-foreground [&_h1]:text-foreground [&_h2]:text-foreground [&_h3]:text-foreground">
        {page === 'impressum' ? <Impressum /> : <Datenschutz />}
      </main>
      <LegalLinks className="mt-16" />
    </div>
  );
}

export function LegalLinks({ className }: { className?: string }) {
  return (
    <nav aria-label="Legal" className={className}>
      <ul className="flex flex-wrap gap-x-5 gap-y-1 text-[0.8125rem] text-muted-foreground">
        <li>
          <a href="/impressum" className="rounded-sm hover:text-foreground hover:underline underline-offset-4">
            Impressum
          </a>
        </li>
        <li>
          <a href="/datenschutz" className="rounded-sm hover:text-foreground hover:underline underline-offset-4">
            Datenschutz / Privacy
          </a>
        </li>
      </ul>
    </nav>
  );
}
