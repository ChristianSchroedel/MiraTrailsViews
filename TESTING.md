# Tests

Lokale JUnit-Tests prüfen Fachregeln und den Editor. Instrumentierte Espresso-Tests prüfen Navigation, Eingabe, einige UI-Zustände und eine Accessibility-Beschreibung. Roborazzi mit Robolectric hält Bilder klassischer Views fest und kann später auch Compose-Oberflächen aufnehmen. Die Tests verwenden feste Daten und eine feste Bildschirmkonfiguration.

Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat recordRoborazziDebug
.\gradlew.bat verifyRoborazziDebug
.\gradlew.bat compareRoborazziDebug
```

Unix: dieselben Befehle mit `./gradlew`. Für Espresso muss vorher ein Emulator oder Gerät ab API 26 laufen. Die Roborazzi-Befehle laufen auf der JVM. `recordRoborazziDebug` erstellt die Referenzen in `app/src/test/snapshots/`. Nach einer beabsichtigten visuellen Änderung werden die PNG-Dateien geprüft und eingecheckt. `verifyRoborazziDebug` vergleicht den aktuellen Stand mit diesen Bildern und schlägt bei Abweichungen fehl. `compareRoborazziDebug` erzeugt Vergleichsbilder und einen Bericht unter `app/build/reports/roborazzi/`.

Die Referenzen nutzen API 35, deutsche Sprache, die Displaykonfiguration des Pixel-9-Pro-XL-Demo-Emulators (1344 × 2992 Pixel bei 480 dpi, entsprechend 448 × 997 dp) und helles Theme. Die exakte Pixelhöhe wird separat gesetzt, da die ganzzahlige dp-Höhe bei dreifacher Pixeldichte einen Pixel verliert. Die eigenständige Etappenanzeige nutzt dieselbe Bildschirmbreite bei 96 dp Höhe (1344 × 288 Pixel). Die fünf ursprünglichen Referenzen bleiben erhalten. Die erweiterte Abdeckung ist unten beschrieben. Die Daten enthalten keine laufenden Uhrzeiten oder Zufallswerte; Animationen sind für die Aufnahme abgeschlossen.

## Referenzen für die Compose-Migration

Die Klasse `VisualReferenceTest` erfasst alle zehn Navigationsziele. Neue Screen-Fälle liefern `*_window.png` für den sichtbaren Ausschnitt mit Toolbar und bei scrollbaren Inhalten zusätzlich `*_bottom.png` für den unteren Ausschnitt. Beide Bilder nutzen ausschließlich 1344 × 2992 Pixel. Beim Compose-Umbau müssen dieselben Daten, Zustände und Bildausschnitte aufgenommen werden. Die Referenzen dürfen dabei nicht einfach durch neue Compose-Bilder ersetzt werden.

| Bereich | Zusätzliche Zustände |
| --- | --- |
| Katalog | Laden nach Aktualisieren, Ladefehler, Merkliste-Filter, Suchtreffer, leere kombinierte Suche, Auswahl durch langen Druck, neue Karte mit generischem Bild |
| Sammlungen | Merkliste, Geplant, Abgeschlossen und Notizen jeweils gefüllt und leer; Abschlussbanner |
| Details | Alle fünf Beispielwege; alle vier Kombinationen von Merkliste und Planung am selben Weg; 0–3 abgeschlossene Etappen mit aktivem bzw. deaktiviertem Button; lange Texte und mehrzeilige Notiz |
| Anlegen | Leeres und ausgefülltes Formular, Detailansicht nach dem Speichern; beide Pflichtfelder leer, nur Name leer, Name zu kurz, nur Ort leer |
| Bearbeiten | Vorbefüllt mit und ohne Notiz, Pflichtfeldfehler und Detailansicht nach Korrektur |
| Einstellungen | Fehlersimulation aus und an, Reset-Bestätigungsdialog |
| Etappenanzeige | Kein Fortschritt bis vollständig abgeschlossen, eine Etappe, leere Etappenliste mit Start-Fallback, fünf Etappen, lange Beschriftungen |

Alle Screen-Fälle verwenden ausschließlich die vordefinierte Pixel-9-Pro-XL-Konfiguration mit normaler Schriftgröße. Es gibt keine Größenmatrix und keine Schriftvarianten.

Die Repository-Daten werden vor jedem Test zurückgesetzt; erzeugte Activities werden danach geschlossen. Ladebilder frieren nur die Spinneranimation ein. Assertions prüfen die beabsichtigten Lade-, Leer-, Filter- und Buttonzustände vor der Aufnahme.

Die App hat derzeit ein festes helles Theme. Die Suite bildet keine eigene dunkle Gestaltung ab. Tastatur, echte Systemleisten, Bildschirmtastatur-Inset und gerätespezifisches Rendering benötigen weiterhin Prüfungen am Gerät. Die Referenzen decken ausgewählte visuelle Zustände in dieser einen Auflösung ab; beliebige Eingaben und sämtliche Scrollpositionen sind nicht vollständig erfasst.
