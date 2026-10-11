# Tests

Die zehn Ziele bleiben bestehen und werden direkt durch Compose Navigation 3 angezeigt. Fragment-Hüllen und XML-Navigationsgraph sind entfernt. Die Activity behält die native Toolbar und die bisherige Insets-Behandlung.

## Funktionsprüfung

Die Fachregeln, EditorViewModel und Screenshot-Toleranz werden durch acht JVM-Tests geprüft. ComposeScreenTest bedient die realen Nav-3-Compose-Screens über Compose-Semantik: Suche, Merkliste-Filter, Auswahl und Öffnen, Formularvalidierung und Speichern, Etappenfortschritt, Bearbeitungsnavigation sowie Reset mit Abbrechen und Bestätigen.

NavigationAndFlowsTest enthält die sechs ursprünglichen Espresso-Flows als instrumentierte Compose UI Tests sowie den unten beschriebenen Neustart-Test:

| Ursprünglicher Flow | Compose-Prüfung |
| --- | --- |
| Alle Übersichtszugänge | Alle sieben Buttons öffnen ihr Compose-Ziel; Inhalt und Navigationsziel werden geprüft; Zurück führt zur Übersicht |
| Katalog → Details → Bearbeiten | Karte öffnen, Etappenanzeige prüfen, Editor öffnen und vorbelegten Namen prüfen |
| Validierung und Anlegen | Leeres Formular zeigt Namensfehler; gültiger Name und Ort werden eingegeben und gespeichert |
| Vollständiger Weg | Der Button für die nächste Etappe ist sichtbar und deaktiviert |
| Suche ohne Treffer | Leerzustand, Ergebniszahl null und fehlende bisherige Karte |
| Accessibility der Etappen | Vollständige Beschreibung mit Fortschritt und Etappennamen |

Die instrumentierten Tests warten auf sichtbaren Kataloginhalt statt auf einen festen Sleep. Eingaben, Buttons, Listen und Scrollaktionen werden über Compose-Semantik bedient. Alt-IDs und RecyclerView-Testabhängigkeiten sind entfernt; es gibt keine Ersatz-Widgets für alte Tests.

Ein weiterer Gerätetest öffnet die echte Bildschirmtastatur im Editor, scrollt Notizfeld und Speichern-Button in den sichtbaren Bereich und prüft ihre Position oberhalb der Tastatur. Anschließend speichert er den Weg einschließlich Notiz. Der Wiederherstellungstest bedient den Compose-Zurück-Button direkt über seine Accessibility-Beschreibung.

## Screenshot-Vergleiche

VisualReferenceTest behält die 28 bisherigen Fälle und die 86 vorhandenen Referenzbilder. Die Tests verwenden die realen Nav-3-Compose-Screens und Compose-Semantik einschließlich oberer und unterer Bildausschnitte. Fixture-Texte werden über die Compose-SetText-Semantik ohne Fokus gesetzt; Datenzustände über WalkViewModel und Repository, ohne Tastatur oder Fokus in die Referenzzustände einzubringen. Die UI-Flows prüfen die tatsächliche Bedienung separat.

Alle Vergleiche erzwingen RoborazziTaskType.Verify. Review-Aufnahmen verwenden Record ausschließlich für Dateien unter app/build/compose-review/. Referenzen unter app/src/test/snapshots/ werden nicht überschrieben. Bildfehler werden pro Test gesammelt, damit weitere Zustände desselben Tests ebenfalls verglichen werden. Vor dem JVM-Lauf werden alte *_actual.png und *_compare.png im Build-Ausgabeordner entfernt.

| Bereich | Zustände |
| --- | --- |
| Katalog | Inhalt einschließlich Scrollende, Laden, Fehler, Merkliste-Filter, Suchtreffer, leere kombinierte Suche, langer Druck zur Auswahl, generisches Bild |
| Sammlungen | Merkliste, Geplant, Abgeschlossen und Notizen jeweils gefüllt und leer; Abschlussbanner |
| Details | Fünf Beispielwege; vier Merkliste-/Planungskombinationen; 0–3 abgeschlossene Etappen; Fortschrittsänderung am geöffneten Screen; lange Texte und mehrzeilige Notiz |
| Anlegen | Leer, ausgefüllt, gespeichert; beide Pflichtfelder leer, nur Name leer, Name zu kurz, nur Ort leer |
| Bearbeiten | Vorbelegt mit und ohne Notiz, Pflichtfeldfehler, korrigierter und gespeicherter Weg |
| Einstellungen | Fehlersimulation aus/an und Reset-Dialog |
| Etappenanzeige | 0 bis vollständig abgeschlossen, eine Etappe, leere Liste mit Start-Fallback, fünf Etappen, lange Beschriftungen |

Die Screenshots verwenden ausschließlich API 35, Deutsch, helles Theme und die Pixel-9-Pro-XL-Konfiguration mit 1344 × 2992 Pixeln bei 480 dpi. FixedScreenshotDisplay setzt die exakte Höhe vor dem Activity-Start. Die ursprüngliche einzelne Etappenaufnahme bleibt bei 1344 × 288 Pixeln; ihre Varianten behalten 1344 × 2992 Pixel. Es gibt keine Größen- oder Schriftmatrix.

## Toleranz und Abnahme

ScreenshotTolerance.kt erlaubt einen Farbabstand von 0,01 und höchstens 0,05 % abweichende Pixel, ohne Verschiebungstoleranz. Abweichende Bildabmessungen werden zurückgewiesen. Die eigenständigen Etappenaufnahmen bleiben strikt. Vier Gegenproben prüfen Farbrundung, eine fehlende Kartenliste, eine um einen Pixel verschobene Überschrift und andere Abmessungen.

Die Toleranz wurde für die Layoutkorrekturen nicht erweitert. Compose-Image-Rasterisierung darf abweichen und wird weiterhin als Bildunterschied gemeldet. Feldhöhen, Textbaselines und Umbrüche, Checkbox-Geometrie, Abstände am Katalogende und die Anordnung des Reset-Dialogs wurden gegen die ursprünglichen Referenzen korrigiert. Die übrigen feinen Schrift-, Kontur- und Illustrationsunterschiede bleiben in den Vergleichsbildern sichtbar.

Review-Aufnahmen: app/build/compose-review/. Differenzen: app/build/outputs/roborazzi/. Berichte: app/build/reports/tests/testDebugUnitTest/, app/build/reports/roborazzi/debug/ und app/build/reports/androidTests/connected/debug/.

## Befehle

Windows (PowerShell):

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest --tests '*ComposeScreenTest' --tests '*EditorViewModelTest' --tests '*WalkRulesTest' --tests '*ScreenshotToleranceTest' --tests '*TrailNavigationTest' --tests '*EditorSavedStateTest'
.\gradlew.bat verifyRoborazziDebug --tests '*VisualReferenceTest'
$env:ANDROID_SERIAL = 'emulator-5554'
.\gradlew.bat connectedDebugAndroidTest
```

Unix: dieselben Gradle-Befehle mit ./gradlew. Ein erfolgreicher Build und funktionale Tests ersetzen keine visuelle Abnahme.

## Navigation 3

Die sechs bestehenden UI-Flows bleiben erhalten und prüfen jetzt typisierte Routen statt Fragment-IDs. Ein zusätzlicher Gerätetest prüft die Wiederherstellung eines ungespeicherten Editorentwurfs nach Activity-Neustart und danach die Toolbar- und System-Zurück-Navigation durch Details, Katalog und Übersicht.

TrailNavigationTest stellt den gespeicherten Backstack wieder her und prüft Argumente, Identitäten wiederholter Ziele, Zurück-Reihenfolge und die unveränderliche Startseite. ComposeScreenTest prüft getrennte Entwürfe für wiederholt geöffnete Editoren. EditorSavedStateTest prüft die Wiederherstellung leerer Eingaben, Texte und Validierungsfehler ohne Änderung des Repository.

## Prüflauf nach Nav 3 (11. Oktober 2026)

assembleDebug erfolgreich. Alle 16 funktionalen JVM-Tests bestehen, ebenso alle sieben instrumentierten Compose-UI-Tests auf Pixel_9_Pro_XL (Android 16), einschließlich Activity-Neustart mit Editorentwurf und Zurück-Navigation.

Der frische Screenshot-Lauf führt alle 28 Fälle aus: drei bestehen, 25 melden weiterhin die Bildabweichungen gegenüber den ursprünglichen XML-Referenzen. Alle Fehler sind Bildvergleiche; es verbleiben keine funktionalen Fehler in den Screenshot-Zuständen. Referenzen und Toleranz wurden nicht angepasst. Alle 86 PNG-Referenzen sind per SHA-256 unverändert bestätigt.

Zusätzlich sind alle 77 vorhandenen Review-Aufnahmen pixelgleich mit dem unmittelbar vor Nav 3 gesicherten Stand. Der Nachweis liegt unter app/build/navigation-comparison.json; Vorher-Aufnahmen unter app/build/navigation-before/. Der Navigationsumbau erzeugt in diesen Aufnahmen keine neuen visuellen Abweichungen.

Zu diesem Prüfstand war die Toolbar noch nativ; die Shell wurde im folgenden Schritt separat auf Compose umgestellt. Die bekannte visuelle Restabweichung der Compose-Screens blieb offen.

## Prüflauf nach Full-Compose-Shell (11. Oktober 2026)

assembleDebug und lintDebug erfolgreich; Lint meldet 0 Fehler und 24 Hinweise. Alle 16 funktionalen JVM-Tests und alle acht instrumentierten Compose-UI-Tests auf Pixel_9_Pro_XL (Android 16) bestehen. Der neue Tastaturtest bestätigt, dass Notizfeld und Speichern-Button bei geöffneter Tastatur erreichbar sind und der Weg mit Notiz gespeichert werden kann. Der Wiederherstellungstest bedient den Compose-Zurück-Button.

Der frische Screenshot-Lauf führt unverändert alle 28 Fälle aus: drei bestehen, 25 melden die bekannten Bildabweichungen gegenüber den ursprünglichen XML-Referenzen. Es gibt keine funktionalen Fehler in den Screenshot-Zuständen. Referenzen und Toleranz wurden nicht angepasst. Alle 86 PNG-Referenzen sind per SHA-256 identisch zum Git-Ausgangsstand; Nachweis unter app/build/shell-baseline-hashes.json.

Alle 77 Review-Aufnahmen sind pixelgleich mit dem unmittelbar vor der Shell-Migration gesicherten Stand. Nachweis: app/build/shell-comparison.json; Vorher-Aufnahmen: app/build/shell-before/. Pfeilgröße, Titelbaseline und Button-Inhaltsfarben wurden vor diesem letzten Lauf abgeglichen. Die Migration erzeugt in diesen Aufnahmen keine zusätzlichen visuellen Abweichungen.

MainActivity, Kopfzeile, Toolbar und Insets sind vollständig Compose. Es gibt keine XML-Layouts, View-Styles, AppCompat- oder Material-View-Abhängigkeiten mehr. SunnyTheme.kt bündelt Farben, Schriftarten, Typografie und Abstände. Das minimale Plattform-Theme in values/themes.xml konfiguriert nur das Android-Startfenster. Android-Ressourcen für Texte, Schriftdateien, Illustrationen und Launcher-Icon bleiben erhalten.

Die installierte APK wurde zusätzlich mit der Android-CLI auf dem Emulator sichtbar geprüft. Aufnahme: app/build/shell-device.png; UI-Baum: app/build/shell-device-layout.json.
