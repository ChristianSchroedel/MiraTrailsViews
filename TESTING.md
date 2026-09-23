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

Die Referenzen nutzen API 35, deutsche Sprache, 360 × 800 dp, dreifache Pixeldichte und helles Theme. Erfasst werden Übersicht, Wegliste, leerer Listenstand, Eingabefehler und Etappenanzeige. Die Daten enthalten keine laufenden Uhrzeiten oder Zufallswerte; Animationen sind für die Aufnahme abgeschlossen.
