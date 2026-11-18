# Webseite auf dem Windows-Laptop

Die Webseite läuft per Docker auf dem Windows-Laptop `LAPTOPRO` unter `192.168.2.130`. Du bearbeitest sie auf dem Linux-Rechner und verteilst sie mit einem einzigen Befehl.

## Kurzfassung

| Was | Befehl / Adresse |
| --- | --- |
| Webseite öffnen | http://192.168.2.130:3000 |
| Backend-Status | http://192.168.2.130:3000/api/status |
| Änderungen auf den Laptop bringen und neu bauen | `./deploy-laptop.sh` |
| Auf dem Laptop einloggen | `ssh laptop` |

## 1. Webseite bearbeiten (der normale Weg)

1. Ändere die Dateien in `~/Projekte/personal-website` auf dem Linux-Rechner, wie gewohnt.
2. Optional kannst du lokal testen: `cd frontend && npm run dev`.
3. Verteile die Änderungen:
   ```bash
   ./deploy-laptop.sh
   ```
   Das Skript kopiert das Projekt nach `C:\Users\ronal\personal-website` und führt dort `docker compose up --build -d` aus. Der erste Build dauert ein paar Minuten, spätere Builds gehen schneller.

## 2. Direkt auf dem Laptop arbeiten

```bash
ssh laptop
cd C:\Users\ronal\personal-website
.\site.ps1 ps        # Status der Container
.\site.ps1 logs      # Logs live ansehen (Strg+C beendet)
.\site.ps1 restart   # neu starten ohne Neubau
.\site.ps1 up        # neu bauen und starten
.\site.ps1 down      # stoppen
```

Mit VS Code oder Antigravity geht es auch grafisch: Installiere die Erweiterung **Remote - SSH**, wähle dann **Connect to Host...**, dort `laptop`, und öffne den Ordner `C:\Users\ronal\personal-website`.

Achtung: Wenn du direkt auf dem Laptop änderst und danach `./deploy-laptop.sh` ausführst, überschreibt der Linux-Stand diese Änderungen. Arbeite also an einer Stelle, am besten auf dem Linux-Rechner mit Git.

## 3. Was eingerichtet ist

- **SSH:** Der Login läuft per Key `~/.ssh/id_ed25519_edgeserver`. Der Kurzname `laptop` steht in `~/.ssh/config`.
- **Firewall:** Port 22 (SSH) und Port 3000 (Webseite) sind für alle Netzwerkprofile freigegeben.
- **Docker Desktop:** Es startet beim Windows-Login automatisch. Alle Container haben `restart: unless-stopped` und kommen nach einem Neustart von selbst wieder.
- **Docker über SSH:** Der Windows-Anmeldespeicher funktioniert in SSH-Sitzungen nicht. Deshalb nutzt `site.ps1` eine eigene Config unter `C:\Users\ronal\.docker-ssh`, mit einem Helper, der öffentliche Images anonym zieht. Nutze für Docker-Befehle über SSH also immer `site.ps1`.
- **Aufgabe `DockerDesktopStart`:** Diese geplante Aufgabe startet Docker Desktop in der angemeldeten Windows-Sitzung. `site.ps1` ruft sie automatisch auf, wenn Docker nicht läuft.

## 3a. Öffentlich: https://ronald-nguyen.de (Cloudflare Tunnel)

- Der Compose-Dienst `tunnel` (`cloudflare/cloudflared`) baut eine ausgehende Verbindung zu Cloudflare auf. Dafür sind keine Portfreigaben im Router nötig.
- Er läuft nur, wenn in der `.env` `COMPOSE_PROFILES=tunnel` und `CLOUDFLARE_TUNNEL_TOKEN=…` stehen. Das Token findest du im Cloudflare-Dashboard unter Tunnels → `laptop` → Overview → Install-Befehl, der Teil nach `--token`.
- Die Route im Tunnel ist `ronald-nguyen.de → http://frontend:80`. Der DNS-Eintrag ist ein CNAME `@ → 65105438-2f65-4aba-bd7f-e81cbc111385.cfargotunnel.com` (proxied).
- `www.ronald-nguyen.de` hat eine eigene Tunnel-Route. Die Redirect Rule „www zu Hauptdomain“ leitet mit 301 auf `https://ronald-nguyen.de` weiter, Pfad und Query bleiben erhalten. „Always Use HTTPS“ ist aktiv.
- Nginx übernimmt die Besucher-IP aus `CF-Connecting-IP`, aber nur aus dem Tunnel-Netz `172.31.250.0/24`.

## 3b. Tailscale

Der Laptop heißt im Tailnet `laptopro` und hat die Adresse `100.82.34.109`. Ist er nicht im Heimnetz, deployst du über Tailscale. Ändere dafür in `~/.ssh/config` den `HostName` auf `100.82.34.109`, oder lege einen zweiten Host-Alias an und nutze `HOST=<alias> ./deploy-laptop.sh`.

## 4. Fehlerbehebung

| Problem | Lösung |
| --- | --- |
| `ssh laptop` hängt oder Timeout | Prüfen, ob der Laptop an und im selben WLAN ist (`ping 192.168.2.130`). Eine geänderte IP in `~/.ssh/config` anpassen. |
| `failed to connect to the docker API` | Docker Desktop läuft nicht. Starten mit `ssh laptop "Start-ScheduledTask DockerDesktopStart"`, dann etwa 1 Minute warten. Der Laptop muss dafür angemeldet sein, nicht nur eingeschaltet. |
| `error getting credentials` | Docker wurde ohne `site.ps1` aufgerufen. Den Befehl über `site.ps1` ausführen. |
| Build bricht mit „Maven Repository cannot be reached“ ab | Das ist ein kurzer Netzwerkaussetzer. Einfach `./deploy-laptop.sh` noch einmal ausführen. |
| Seite lädt nicht, Container laufen aber | `ssh laptop` und dann `cd C:\Users\ronal\personal-website; .\site.ps1 logs`. |

## 5. Optional: Feste IP

Damit `192.168.2.130` gleich bleibt, kannst du im Router (Heimnetz, DHCP) für den Laptop „immer dieselbe IP-Adresse zuweisen“ aktivieren.
