# Anforderungen an eine spätere UI-Migration

Die zehn Ziele und ihre Navigation müssen erhalten bleiben. Suche findet Wege nach Name oder Ort ohne Beachtung der Großschreibung; der Merkliste-Filter lässt sich damit kombinieren. Die Liste zeigt Laden, Inhalt, keinen Treffer und den simulierten Fehler klar unterscheidbar. Ein Weg kann geöffnet, gemerkt, geplant, bearbeitet und in seinen Etappen fortgeschrieben werden. Die nächste Etappe ist nach vollständigem Abschluss nicht mehr aktiv. Neue Wege brauchen einen Namen mit mindestens drei Zeichen und einen Ort. Fehler müssen am jeweiligen Eingabefeld sichtbar sein. Das Zurücksetzen der Beispieldaten braucht weiterhin eine Bestätigung.

Die ruhige Farbpalette, Abstände, Textgrößen, Karten und die Etappenanzeige sollen in den festgelegten Referenzzuständen wiedererkennbar bleiben. Kurze und lange Beschreibungen müssen lesbar sein. Die Anzeige der Etappen unterscheidet erledigte und offene Schritte.

Bedienelemente, Listeneinträge und die Etappenanzeige brauchen verständliche Accessibility-Namen. Der Fortschritt muss als Text beziehungsweise Accessibility-Beschreibung verfügbar sein. Deaktivierte Aktionen und Eingabefehler müssen auch ohne Farberkennung erkennbar bleiben.

Die App arbeitet lokal mit deterministischen Beispieldaten. `WalkRepository`, `WalkRules` und die ViewModels bilden die fachlichen Schnittstellen. Die erste Migration betrifft die Oberfläche; Verhalten und Datenmodell sollen dabei erhalten bleiben. Es sind keine Netzwerkdienste, Konten oder Schlüssel vorgesehen.

## Schuldenliste für die Compose-Migration

- [x] Native MaterialButtons einschließlich OverviewButton durch Compose-Buttons ersetzt.
- [x] ScrollView und NestedScrollView aus den Fragment-Hüllen entfernt; Scrolling liegt in Compose.
- [x] NativeInput, TextInputLayout/EditText, MaterialCheckBox und TextView-Interop entfernt.
- [x] RecyclerView, WalkAdapter und item_walk.xml durch LazyColumn und WalkCard ersetzt.
- [x] Verschachteltes Etappen-ComposeView, Abschlussbanner und Reset-Dialog direkt in Compose integriert.
- [x] NativeTestInterop und synchronisierte Test-Widget-Bindungen entfernt.
- [x] Die sechs bisherigen Espresso-Flows auf instrumentierte Compose UI Tests übertragen. VisualReferenceTest mit seinen 28 Fällen verwendet Compose-Semantik einschließlich oberer und unterer Bildausschnitte.
- [x] Entfallene View-Test-IDs und RecyclerView-Testabhängigkeiten entfernt; die ehemalige Fragment-ComposeView-ID ist ebenfalls entfernt.

- [x] Fragment-Hüllen und XML-Navigationsgraph durch typisierte Nav-3-Ziele mit gespeichertem Backstack ersetzt.
- [x] Editor-ViewModels pro Backstack-Eintrag isoliert; Entwürfe über SavedStateHandle gesichert.
- [x] Native Toolbar und Activity-Chrome durch AppShell ersetzt; Systemleisten, Displayausschnitt und Tastatur-Inset zentral in Compose behandelt.
- [x] Letztes XML-Layout und View-Styles entfernt. Compose-Theme bündelt Farben, Schriftarten, Typografie und Abstände; AppCompat und Material-Views sind entfernt.

Compose Image bleibt für die Illustrationen erhalten. Abweichungen seiner Bitmap- und Kanten-Rasterisierung sind akzeptiert; Abstände, Bildgrößen, Textumbrüche und Sichtbarkeitszustände bleiben Gegenstand des visuellen Vergleichs. PNG-Referenzen werden nicht durch neue Compose-Aufnahmen ersetzt. Layout- und Textabweichungen werden gegen die unveränderten Referenzen korrigiert; verbleibende Rasterisierungsunterschiede stehen im Bildvergleich zur Begutachtung.
