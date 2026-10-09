# Fröhliche XML-Oberfläche

Die zehn bestehenden Fragment-Ziele folgen der Alternative „Sonnige Entdeckertour“ im [MiraTrails-Figma-File](https://www.figma.com/design/Gz6qHiFrnnurASeAyiVlOg/MiraTrails?node-id=49-120). Navigation, Daten und Fachregeln bleiben erhalten. Die Umsetzung verwendet XML, Material Components und die vorhandene Canvas-View; es gibt keine Compose-Abhängigkeit.

| Screen | Figma-Node |
| --- | --- |
| Übersicht | 49:120 |
| Alle Wege | 49:162 |
| Wegdetails | 49:227 |
| Neuer Weg | 49:269 |
| Weg bearbeiten | 49:292 |
| Merkliste | 49:319 |
| Geplant | 49:350 |
| Abgeschlossen | 49:381 |
| Notizen | 49:405 |
| Einstellungen | 49:436 |

## Wiederverwendung

`values/colors.xml`, `dimens.xml` und `styles.xml` enthalten Farben, Abstände, Typografie, Eingabefelder, Buttons und Bildformen. Die Drawables `sunny_surface`, `sunny_note` und `sunny_stat` bilden Karten und Hinweisflächen. `include_sunny_footer.xml` liefert den gemeinsamen Abschluss.

`item_walk.xml` wird für Katalog und Sammlungen verwendet. `WalkAdapter` wählt die Bildhöhe; `StageProgressView` zeichnet entweder den beschrifteten Etappenverlauf oder kompakte Fortschrittssegmente. Beide Darstellungen verwenden den tatsächlichen Fortschritt. `WalkIllustrations` ordnet den Beispieldaten feste Illustrationen zu. Neu angelegte Wege erhalten die allgemeine Spaziergangsillustration. Die Bilder stellen keine geografisch genaue Karte dar.

Lange Inhalte scrollen einschließlich ihrer Aktionen. Die Formularfelder bleiben native TextInputLayouts mit Eingabeprüfung. Die Etappenanzeige behält ihre gesprochene Zusammenfassung; dekorative Bilder sind von Accessibility ausgenommen.

## Assets

Alle Illustrationen und Symbole stammen aus den genannten Figma-Screens und liegen lokal unter `drawable-nodpi`. Die JPEG-Illustrationen wurden übernommen. Die SVG-Symbole wurden als transparente PNGs mit dreifacher Exportdichte rasterisiert; ihre Originale liegen in `design-assets`. Im App-Code gibt es keine Figma- oder Bild-Downloads.

Inter wird lokal als variable Schrift mit den Gewichten 600 und 700 eingebunden. Quelle: [Google Fonts, Inter](https://github.com/google/fonts/tree/main/ofl/inter). Die Lizenz liegt unter `licenses/Inter-OFL.txt`. Fließtext und Bedienelemente verwenden die Android-Systemschrift Roboto.

Die Screenshot-Referenzen umfassen alle zehn Screens sowie Leerzustand, Eingabefehler und die eigenständige Etappenanzeige. Befehle und Bildschirmkonfiguration stehen in [TESTING.md](../TESTING.md).

## App-Icon

Das [Sonnenweg-Icon](https://www.figma.com/design/Gz6qHiFrnnurASeAyiVlOg/MiraTrails?node-id=53-80) ist im Manifest als `icon` und `roundIcon` eingebunden. Das Original-SVG liegt unter `design-assets/sonnenweg.svg`. Die klassischen PNG-Exporte liegen in den fünf Mipmap-Dichteordnern mit 48, 72, 96, 144 und 192 Pixeln.

Ab API 26 verwendet Android `mipmap-anydpi-v26/ic_launcher.xml`. Der unveränderte Entwurf liegt zentriert in einer transparenten Vordergrundebene: 288 Pixel Motiv plus jeweils 72 Pixel Rand auf einer 432-Pixel-Fläche, entsprechend 72 dp Motiv auf 108 dp. Der Hintergrund verwendet die vorhandene Himmelsfarbe. Die Form bestimmt der Launcher. Die runde Darstellung wurde auf dem Pixel-Emulator geprüft.
