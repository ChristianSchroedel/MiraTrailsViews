# Mira Wege

Mira Wege ist eine kleine Offline-App für Spaziergänge in der eigenen Umgebung. Man kann Wege suchen, merken, planen, Etappen abschließen und persönliche Notizen festhalten. Die fünf mitgelieferten Wege sind fiktiv. Änderungen bleiben während der App-Sitzung im Speicher; ein Neustart stellt den Ausgangszustand her.

## Aufbau

Die App hat eine MainActivity mit zehn typisierten Compose-Zielen in Navigation 3 (1.2.0). NavDisplay zeigt die bestehenden Screens direkt; Fragment-Hüllen und XML-Navigationsgraph sind entfernt. Der einzelne Backstack bleibt erhalten und wird mit rememberNavBackStack gespeichert. Jeder Eintrag hat eine eigene Identität und einen eigenen ViewModelStore. Editorentwürfe und Validierungsfehler liegen zusätzlich im SavedStateHandle. WalkViewModel bleibt Activity-weit geteilt; EditorViewModel gehört zum jeweiligen Navigationseintrag. MainActivity ist eine ComponentActivity mit setContent. AppShell rendert Kopfzeile, Toolbar und Bildschirmfläche in Compose und behandelt Systemleisten, Displayausschnitt und Tastatur-Inset einmal zentral. Alle Screen-Inhalte einschließlich Eingaben, Listen, Scrolling und Reset-Dialog sind Compose-Komponenten. Farben, Schriftarten, Typografie und Abstände liegen in SunnyTheme.kt; AppCompat, Material-Views und XML-Layouts sind entfernt. Das minimale Android-Theme konfiguriert nur das Startfenster. Die Fachregeln liegen in WalkRules, die Daten hinter WalkRepository. Testabdeckung und Prüfstand stehen in [TESTING.md](TESTING.md); die Schuldenliste steht in [MIGRATION_REQUIREMENTS.md](MIGRATION_REQUIREMENTS.md).

| Ziel | Funktion |
| --- | --- |
| Übersicht | Bestand und Zugänge zu den Sammlungen anzeigen |
| Alle Wege | Suchen, nach Merkliste filtern, laden und Details öffnen |
| Wegdetails | Beschreibung und Etappen ansehen, Fortschritt, Merkliste und Planung ändern |
| Neuer Weg | Weg mit Eingabeprüfung anlegen |
| Weg bearbeiten | Texte und Notiz eines bestehenden Wegs ändern |
| Merkliste | Gemerkte Wege öffnen |
| Geplant | Noch nicht abgeschlossene geplante Wege öffnen |
| Abgeschlossen | Fertige Wege öffnen |
| Notizen | Wege mit eigenen Notizen öffnen |
| Einstellungen | Ladefehler simulieren und Beispieldaten nach Bestätigung zurücksetzen |

## Voraussetzungen und Befehle

Android SDK mit API 37, JDK 17 oder neuer und ein Emulator oder Gerät ab API 26. Die Versionen der Build-Werkzeuge und Bibliotheken sind in den Gradle-Dateien festgelegt. Beim ersten Build müssen Gradle und die veröffentlichten Artefakte einmal geladen werden; danach benötigt die App selbst keine Verbindung. `local.properties` kann lokal auf das SDK zeigen und wird nicht eingecheckt.

Windows (PowerShell):

```powershell
.\gradlew.bat clean assembleDebug testDebugUnitTest verifyRoborazziDebug
.\gradlew.bat connectedDebugAndroidTest
```

Unix:

```sh
./gradlew clean assembleDebug testDebugUnitTest verifyRoborazziDebug
./gradlew connectedDebugAndroidTest
```

Die zweite Zeile setzt ein gestartetes Gerät voraus. Installieren und starten:

```sh
./gradlew installDebug
adb shell am start -n org.example.miratrail/.MainActivity
```

Für Windows die Endung `.bat` beim Gradle-Befehl ergänzen. Die Testverfahren und Referenzbilder sind in [TESTING.md](TESTING.md) beschrieben.

## Reproduzierbare Zustände

Beim Öffnen oder Aktualisieren von „Alle Wege“ erscheint kurz der Ladezustand. Eine Suche ohne Treffer zeigt den Leerzustand. In „Einstellungen“ lässt sich für den nächsten Listenaufruf ein lokaler Ladefehler einschalten. Der Fehler kann dort wieder ausgeschaltet werden. Es gibt weder Serverzugriffe noch Konten oder Schlüssel. Die Daten haben keine Uhrzeiten und keinen Zufall.
