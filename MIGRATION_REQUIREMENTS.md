# Anforderungen an eine spätere UI-Migration

Die zehn Ziele und ihre Navigation müssen erhalten bleiben. Suche findet Wege nach Name oder Ort ohne Beachtung der Großschreibung; der Merkliste-Filter lässt sich damit kombinieren. Die Liste zeigt Laden, Inhalt, keinen Treffer und den simulierten Fehler klar unterscheidbar. Ein Weg kann geöffnet, gemerkt, geplant, bearbeitet und in seinen Etappen fortgeschrieben werden. Die nächste Etappe ist nach vollständigem Abschluss nicht mehr aktiv. Neue Wege brauchen einen Namen mit mindestens drei Zeichen und einen Ort. Fehler müssen am jeweiligen Eingabefeld sichtbar sein. Das Zurücksetzen der Beispieldaten braucht weiterhin eine Bestätigung.

Die ruhige Farbpalette, Abstände, Textgrößen, Karten und die Etappenanzeige sollen in den festgelegten Referenzzuständen wiedererkennbar bleiben. Kurze und lange Beschreibungen müssen lesbar sein. Die Anzeige der Etappen unterscheidet erledigte und offene Schritte.

Bedienelemente, Listeneinträge und die Etappenanzeige brauchen verständliche Accessibility-Namen. Der Fortschritt muss als Text beziehungsweise Accessibility-Beschreibung verfügbar sein. Deaktivierte Aktionen und Eingabefehler müssen auch ohne Farberkennung erkennbar bleiben.

Die App arbeitet lokal mit deterministischen Beispieldaten. `WalkRepository`, `WalkRules` und die ViewModels bilden die fachlichen Schnittstellen. Die erste Migration betrifft die Oberfläche; Verhalten und Datenmodell sollen dabei erhalten bleiben. Es sind keine Netzwerkdienste, Konten oder Schlüssel vorgesehen.
