/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.images;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Diese Enumerations-Klasse h�lt die Icons f�r Toolbars und Men�s vor.
 * @author Alexander Herzog
 */
public enum Images {
	/* Allgemeine Icons */

	/** Symbol "Drucken" */
	GENERAL_PRINT("printer.png"),

	/** Symbol "Einstellungen" (Programmsetup) */
	GENERAL_SETUP("wrench.png"),

	/** Symbol "Speichern" */
	GENERAL_SAVE("disk.png"),

	/** Symbol "Ende" */
	GENERAL_EXIT("door_in.png"),

	/** Symbol "Suchen" */
	GENERAL_FIND("find.png"),

	/** Symbol "Suchen - nach ID" */
	GENERAL_FIND_BY_ID("Counter.png"),

	/** Symbol "Suchen - nach Name" */
	GENERAL_FIND_BY_NAME("font.png"),

	/** Symbol "Suchen - �ber alles" */
	GENERAL_FIND_BY_ALL("find.png"),

	/** Symbol "Anwendung" (f�r Zugriff auf Anwendungsfunktionen �ber die Quick-Access-Funktion */
	GENERAL_APPLICATION("application.png"),

	/** Symbol "Information" */
	GENERAL_INFO("information.png"),

	/** Symbol "Fehler" */
	GENERAL_WARNING("error.png"),

	/** Symbol "Fehler" bzw. "Fehlerhafte Einstellungen" (f�r das Quick-Fix-Popupmen�) */
	GENERAL_WARNING_BUG("bug_error.png"),

	/** Symbol "Abbruch" */
	GENERAL_CANCEL("cancel.png"),

	/** Symbol "Tools" */
	GENERAL_TOOLS("cog.png"),

	/** Symbol "Datei ausw�hlen" */
	GENERAL_SELECT_FILE("folder_page_white.png"),

	/** Symbol "Tabelle in Datei ausw�hlen" */
	GENERAL_SELECT_TABLE_IN_FILE("page_excel.png"),

	/** Symbol "Verzeichnis ausw�hlen" */
	GENERAL_SELECT_FOLDER("folder.png"),

	/** Symbol "Aus" */
	GENERAL_OFF("cross.png"),

	/** Symbol "An" */
	GENERAL_ON("accept.png"),

	/** Symbol "Script" */
	GENERAL_SCRIPT("page_white_code_red.png"),

	/** Symbol "Startseite" */
	GENERAL_HOME("house.png"),

	/** Symbol "Warten" (animiert) */
	GENERAL_WAIT_INDICATOR("ajax-loader.gif"),

	/** Symbol "Wert erh�hen" */
	GENERAL_INCREASE("Plain_Plus.png"),

	/** Symbol "Wert verringern" */
	GENERAL_DECREASE("Plain_Minus.png"),

	/** Symbol "Versionsgeschichte" */
	GENERAL_CHANGELOG("calendar.png"),

	/** Symbol "Lizenzen" */
	GENERAL_LICENSE("key.png"),

	/** Symbol "Schloss offen" (z.B. kein Schreibschutz) */
	GENERAL_LOCK_OPEN("lock_open.png"),

	/** Symbol "Schloss geschlossen" (z.B. Schreibschutz) */
	GENERAL_LOCK_CLOSED("lock.png"),

	/** Symbol "Bearbeiten" */
	GENERAL_EDIT("pencil.png"),

	/** Symbol "Dialog-Button 'Ok'" */
	MSGBOX_OK("accept.png"),

	/** Symbol "Dialog-Button 'Ja'" */
	MSGBOX_YES("tick.png"),

	/** Symbol "Dialog-Button 'Ja, speichern'" */
	MSGBOX_YES_SAVE("disk.png"),

	/** Symbol "Dialog-Button 'Nein'" */
	MSGBOX_NO("cancel.png"),

	/** Symbol "Dialog-Button 'Abbruch/Zur�ck'" */
	MSGBOX_CANCEL("arrow_redo2.png"),

	/** Symbol f�r Definitionsmodus "�ber Verteilung" */
	MODE_DISTRIBUTION("chart_curve.png"),

	/** Symbol f�r Definitionsmodus "�ber Ausdruck" */
	MODE_EXPRESSION("fx.png"),

	/** Symbol "Nach unten" */
	ARROW_DOWN("arrow_down.png"),

	/** Symbol "Nach unten ans Ende" */
	ARROW_DOWN_END("arrow_down_double.png"),

	/** Symbol "Nach links" */
	ARROW_LEFT("arrow_left.gif"),

	/** Symbol "Nach rechts" */
	ARROW_RIGHT("arrow_right.gif"),

	/** Symbol "Nach oben" */
	ARROW_UP("arrow_up.png"),

	/* Bearbeiten */

	/** Symbol "Bearbeiten - R�ckg�ngig" */
	EDIT_UNDO("arrow_undo.png"),

	/** Symbol "Bearbeiten - Wiederholen" */
	EDIT_REDO("arrow_redo.png"),

	/** Symbol "Bearbeiten - Kopieren" */
	EDIT_COPY("copy.gif"),

	/** Symbol "Bearbeiten - Kopieren (als Bild) */
	EDIT_COPY_AS_IMAGE("image.gif"),

	/** Symbol "Bearbeiten - Ausschneiden" */
	EDIT_CUT("cut.gif"),

	/** Symbol "Bearbeiten - Einf�gen" */
	EDIT_PASTE("paste_plain.png"),

	/** Symbol "Bearbeiten - Hinzuf�gen" */
	EDIT_ADD("add.png"),

	/** Symbol "Bearbeiten - L�schen" */
	EDIT_DELETE("delete.png"),

	/** Symbol "Ebenen" */
	EDIT_LAYERS("layers.png"),

	/** Symbol "Ebene - sichtbar" */
	EDIT_LAYERS_VISIBLE("lightbulb.png"),

	/** Symbol "Ebene - unsichtbar" */
	EDIT_LAYERS_INVISIBLE("lightbulb_off.png"),

	/** Symbol "Hintergrundfarbe" */
	EDIT_BACKGROUND_COLOR("color_wheel.png"),

	/** Symbol "Verbindungskante" */
	EDIT_EDGES("link.png"),

	/** Symbol "Verbindungskante hinzuf�gen" */
	EDIT_EDGES_ADD("link_add.png"),

	/** Symbol "Verbindungskante hinzuf�gen - Panel schlie�en" */
	EDIT_EDGES_ADD_CLOSEPANEL("application_side_contract2.gif"),

	/** Symbol "Verbindungskante entfernen" */
	EDIT_EDGES_DELETE("link_delete.png"),

	/** Symbol f�r "Raster anzeigen" */
	EDIT_VIEW_RASTER("Raster.gif"),

	/** Symbol f�r "Lineale anzeigen" */
	EDIT_VIEW_RULERS("Ruler.png"),

	/** Symbol "Vertikal ausrichten - Oberkante */
	ALIGN_TOP("shape_align_top.png"),

	/** Symbol "Vertikal ausrichten - Mitte */
	ALIGN_MIDDLE("shape_align_middle.png"),

	/** Symbol "Vertikal ausrichten - Unterkante */
	ALIGN_BOTTOM("shape_align_bottom.png"),

	/** Symbol "Horizontal ausrichten - Linke Kante */
	ALIGN_LEFT("shape_align_left.png"),

	/** Symbol "Horizontal ausrichten - mittig */
	ALIGN_CENTER("shape_align_center.png"),

	/** Symbol "Horizontal ausrichten - Rechte Kante */
	ALIGN_RIGHT("shape_align_right.png"),

	/** Symbol "Anordnen - ganz nach vorne" */
	MOVE_FRONT("shape_move_front.png"),

	/** Symbol "Anordnen - eine Ebene nach vorne" */
	MOVE_FRONT_STEP("shape_move_forwards.png"),

	/** Symbol "Anordnen - eine Ebene nach hinten" */
	MOVE_BACK_STEP("shape_move_backwards.png"),

	/** Symbol "Anordnen - ganz nach hinten" */
	MOVE_BACK("shape_move_back.png"),

	/** Symbol "Zoom" (allgemein) */
	ZOOM("zoom.png"),

	/** Symbol "Hinein zoomen" */
	ZOOM_IN("zoom_in.png"),

	/** Symbol "Heraus zoomen" */
	ZOOM_OUT("zoom_out.png"),

	/** Symbol "Auf Modell zoomen" */
	ZOOM_CENTER_MODEL("image.gif"),

	/** Symbol "Modell�berblick" */
	MODE_OVERVIEW("find.png"),

	/* Expression Builder */

	/** Symbol "Expression-Builder" */
	EXPRESSION_BUILDER("wand.png"),

	/** Symbol im Expression-Builder "Konstante" */
	EXPRESSION_BUILDER_CONST("text_letter_omega.png"),

	/** Symbol im Expression-Builder "Variable" */
	EXPRESSION_BUILDER_VARIABLE("font.png"),

	/** Symbol im Expression-Builder "Funktion" */
	EXPRESSION_BUILDER_FUNCTION("fx.png"),

	/** Symbol im Expression-Builder "Verteilung" */
	EXPRESSION_BUILDER_DISTRIBUTION("chart_curve.png"),

	/** Symbol im Expression-Builder "Simulationsdaten" */
	EXPRESSION_BUILDER_SIMDATA("action_go.gif"),

	/** Symbol im Expression-Builder "Stations-ID aus Name bestimmen" */
	EXPRESSION_BUILDER_STATION_ID("station.png"),

	/** Symbol im Expression-Builder "Kundenspezifische Simulationsdaten" */
	EXPRESSION_BUILDER_CLIENT_DATA("user.png"),

	/* Modell */

	/** Symbol "Modell" */
	MODEL("brick.png"),

	/** Symbol "Modell - Neu" */
	MODEL_NEW("brick_add.png"),

	/** Symbol "Modell - Neu mit Generator" */
	MODEL_GENERATOR("wand.png"),

	/** Symbol "Modell - Laden" */
	MODEL_LOAD("brick_go.png"),

	/** Symbol "Modell - Speichern" */
	MODEL_SAVE("disk.png"),

	/** Symbol "Modell - Elementenliste" */
	MODEL_LIST_ELEMENTS("text_list_numbers.png"),

	/** Symbol "Modell - Beschreibung" */
	MODEL_DESCRIPTION("page_gear.png"),

	/** Symbol "Modell - Vergleich mit analytischem Nodell" */
	MODEL_ANALYTIC_COMPARE("fx.png"),

	/** Symbol "Modell - Vorlagen" */
	MODEL_TEMPLATES("pictures.png"),

	/** Symbol "Vergleichen - mehrere Statistikdaten" */
	MODEL_COMPARE("application_tile_horizontal.png"),

	/** Symbol "Vergleichen - Modell festhalten" */
	MODEL_COMPARE_KEEP("basket_put.png"),

	/** Symbol "Vergleichen - festgehaltenes und aktuelles Modell vergleichen" */
	MODEL_COMPARE_COMPARE("basket_go.png"),

	/** Symbol "Vergleichen - Zu festgehaltenem Modell zur�ckkehren" */
	MODEL_COMPARE_GO_BACK("basket_remove.png"),

	/** Symbol "Modell - Hinzuf�gen (aus Zwischenablage) - Station" */
	MODEL_ADD_STATION("station.png"),

	/** Symbol "Modell - Hinzuf�gen (aus Zwischenablage) - Bild" */
	MODEL_ADD_IMAGE("image.gif"),

	/** Symbol "Modell - Hinzuf�gen (aus Zwischenablage) - Text" */
	MODEL_ADD_TEXT("Text.png"),

	/** Symbol "Modelleigenschaften - Seite 'Modellbeschreibung'" */
	MODELPROPERTIES_DESCRIPTION("brick_edit.png"),

	/** Symbol "Modelleigenschaften - Seite 'Modellbeschreibung' - Titel auf Zeichenfl�che einf�gen" */
	MODELPROPERTIES_DESCRIPTION_ADD_TO_SURFACE("brick_go.png"),

	/** Symbol "Modelleigenschaften - Seite 'Modellbeschreibung' - Autor einstellen" */
	MODELPROPERTIES_DESCRIPTION_SET_AUTHOR("user.png"),

	/** Symbol "Modelleigenschaften - Seite 'Modellbeschreibung' - automatisch erstellen" */
	MODELPROPERTIES_DESCRIPTION_AUTO_CREATE("page_gear.png"),

	/** Symbol "Modellbeschreibung - Seite 'Simulation'" */
	MODELPROPERTIES_SIMULATION("action_go.gif"),

	/** Symbol "Modellbeschreibung - Seite 'Simulation' - Startwert f�r Zufallszahlengenerator festlegen" */
	MODELPROPERTIES_SIMULATION_RANDOM_SEED("calculator.png"),

	/** Symbol "Modellbeschreibung - Seite 'Kunden'" */
	MODELPROPERTIES_CLIENTS("user.png"),

	/** Symbol "Modellbeschreibung - Seite 'Kunden' - Kundengruppe" */
	MODELPROPERTIES_CLIENTS_GROUPS("group.png"),

	/** Symbol "Modellbeschreibung - Seite 'Kunden' - Icon f�r Kundengruppe" */
	MODELPROPERTIES_CLIENTS_ICON("image.gif"),

	/** Symbol "Modellbeschreibung - Seite 'Kunden' - Kosten f�r Kundengruppe" */
	MODELPROPERTIES_CLIENTS_COSTS("money_euro.png"),

	/** Symbol "Modellbeschreibung - Seite 'Bediener'" */
	MODELPROPERTIES_OPERATORS("group.png"),

	/** Symbol "Modellbeschreibung - Seite 'Bediener' - Gruppe hinzuf�gen" */
	MODELPROPERTIES_OPERATORS_ADD("group_add.png"),

	/** Symbol "Modellbeschreibung - Seite 'Bediener' - Gruppe l�schen" */
	MODELPROPERTIES_OPERATORS_DELETE("group_delete.png"),

	/** Symbol "Modellbeschreibung - Seite 'Bediener' - Kosten f�r Gruppe" */
	MODELPROPERTIES_OPERATORS_COSTS("money_euro.png"),

	/** Symbol "Modellbeschreibung - Seite 'Bediener' - Ausf�lle" */
	MODELPROPERTIES_OPERATORS_FAILURES("group_error.png"),

	/** Symbol "Modellbeschreibung - Seite 'Bediener' - R�stzeiten" */
	MODELPROPERTIES_OPERATORS_SETUP("chart_curve.png"),

	/** Symbol "Modellbeschreibung - Seite 'Bediener' - nachgelagerte Priorit�t 'zuf�llig'" */
	MODELPROPERTIES_PRIORITIES_RANDOM("arrow_switch.png"),

	/** Symbol "Modellbeschreibung - Seite 'Bediener' - nachgelagerte Priorit�t 'Kundenpriorit�t'" */
	MODELPROPERTIES_PRIORITIES_CLIENT("user.png"),

	/** Symbol "Modellbeschreibung - Seite 'Transporter'" */
	MODELPROPERTIES_TRANSPORTERS("lorry.png"),

	/** Symbol "Modellbeschreibung - Seite 'Transporter' - Gruppe hinzuf�gen" */
	MODELPROPERTIES_TRANSPORTERS_ADD("lorry_add.png"),

	/** Symbol "Modellbeschreibung - Seite 'Transporter' - Ausf�lle" */
	MODELPROPERTIES_TRANSPORTERS_FAILURE("lorry_error.png"),

	/** Symbol "Modellbeschreibung - Seite 'Transporter' - Anzahl" */
	MODELPROPERTIES_TRANSPORTERS_COUNT("Counter.png"),

	/** Symbol "Modellbeschreibung - Seite 'Transporter' - Distanzen" */
	MODELPROPERTIES_TRANSPORTERS_DISTANCES("Dispose.png"),

	/** Symbol "Modellbeschreibung - Seite 'Transporter' - Distanzen - Tabellen-Werkzeuge" */
	MODELPROPERTIES_TRANSPORTERS_DISTANCES_TABLE_TOOLS("Table.png"),

	/** Symbol "Modellbeschreibung - Seite 'Transporter' - Beladezeiten" */
	MODELPROPERTIES_TRANSPORTERS_LOADING_TIMES("package_go.png"),

	/** Symbol "Modellbeschreibung - Seite 'Transporter' - Entladezeiten" */
	MODELPROPERTIES_TRANSPORTERS_UNLOADING_TIMES("package_go_left.png"),

	/** Symbol "Modellbeschreibung - Seite 'Zeitpl�ne'" */
	MODELPROPERTIES_SCHEDULES("time.png"),

	/** Symbol "Modellbeschreibung - Seite 'Zeitpl�ne' - hinzuf�gen" */
	MODELPROPERTIES_SCHEDULES_ADD("clock_add.png"),

	/** Symbol "Modellbeschreibung - Seite 'Fertigungspl�ne'" */
	MODELPROPERTIES_SEQUENCES("text_list_numbers.png"),

	/** Symbol "Modellbeschreibung - Seite 'Fertigungspl�ne' - Kundenvariable zuweisen" */
	MODELPROPERTIES_SEQUENCES_ASSIGNMENT("user.png"),

	/** Symbol "Modellbeschreibung - Seite 'Initiale Variablenwerte'" */
	MODELPROPERTIES_INITIAL_VALUES("font.png"),

	/** Symbol "Modellbeschreibung - Seite 'Laufzeitstatistik'" */
	MODELPROPERTIES_RUNTIME_STATISTICS("chart_curve_add.png"),

	/** Symbol "Modellbeschreibung - Seite 'Ausgabeanalyse'" */
	MODELPROPERTIES_OUTPUT_ANALYSIS("chart_curve.png"),

	/** Symbol "Modellbeschreibung - Seite 'Pfadaufzeichnung'" */
	MODELPROPERTIES_PATH_RECORDING("Vertex.png"),

	/** Symbol "Modellbeschreibung - Seite 'Ausgabeanalyse' - Autokorrelation schnell" */
	MODELPROPERTIES_OUTPUT_ANALYSIS_AUTOCORRELATION_FAST("user.png"),

	/** Symbol "Modellbeschreibung - Seite 'Ausgabeanalyse' - Autokorrelation vollst�ndig" */
	MODELPROPERTIES_OUTPUT_ANALYSIS_AUTOCORRELATION_FULL("group.png"),

	/** Symbol "Modellbeschreibung - Seite 'Simulationssystem'" */
	MODELPROPERTIES_INFO("computer.png"),

	/* Statistik */

	/** Symbol "Statistik" */
	STATISTICS("sum.png"),

	/** Symbol "Statistik" (dunkler) */
	STATISTICS_DARK("sum2.png"),

	/** Symbol "Statistik - laden" */
	STATISTICS_LOAD("icon_package_open.gif"),

	/** Symbol "Statistik - speichern" */
	STATISTICS_SAVE("icon_package_get.gif"),

	/** Symbol "Statistik - Modell in Editor laden */
	STATISTICS_SHOW_MODEL("brick.png"),

	/** Symbol "Animation - Pause" */
	STATISTICS_ANIMATION_PAUSE("Pause.png"),

	/** Symbol "Animation - Play" */
	STATISTICS_ANIMATION_PLAY("Play.png"),

	/** Symbol in der Statistik "Sankey" */
	STATISTICS_DIAGRAM_SANKEY("chart_organisation.png"),

	/* Simulation */

	/** Symbol "Simulation - Start" */
	SIMULATION("action_go.gif"),

	/** Symbol "Simulation - In Logdatei aufzeichnen" */
	SIMULATION_LOG("Text.gif"),

	/** Symbol "Simulation - In Logdatei aufzeichnen - als Text" */
	SIMULATION_LOG_MODE_FILE("page.png"),

	/** Symbol "Simulation - In Logdatei aufzeichnen - als Tabelle" */
	SIMULATION_LOG_MODE_TABLE("page_excel.png"),

	/** Symbol "Simulation - Modell pr�fen" */
	SIMULATION_CHECK("accept.png"),

	/** Symbol "Simulation - Datenbank pr�fen" */
	SIMULATION_CHECK_DATABASE("database.png"),

	/* Animation */

	/** Symbol "Animation starten" */
	ANIMATION("film_go.png"),

	/** Symbol "Animation aus Video aufzeichnen" */
	ANIMATION_RECORD("stop.png"),

	/** Symbol "Animation - Screenshot aufnehmen" */
	ANIMATION_SCREENSHOT("image.gif"),

	/** Symbol "Animation in Logdatei aufzeichnen" */
	ANIMATION_LOG("Text.gif"),

	/** Symbol "Animation - Steuerung 'Play'" */
	ANIMATION_PLAY("control_play_blue.png"),

	/** Symbol "Animation - Steuerung 'Pause'" */
	ANIMATION_PAUSE("control_pause_blue.png"),

	/** Symbol "Animation - Steuerung 'Schritt'" */
	ANIMATION_STEP("control_end_blue.png"),

	/** Symbol "Animation - Steuerung 'Geschwindigkeit'" */
	ANIMATION_SPEED("control_fastforward_blue.png"),

	/** Symbol "Animation - Ausdruck auswerten" */
	ANIMATION_EVALUATE_EXPRESSION("fx.png"),

	/** Symbol "Animation - Skript auswerten" */
	ANIMATION_EVALUATE_SCRIPT("page_white_code_red.png"),

	/** Symbol "Animation - Liste der n�chsten Ereignisse" */
	ANIMATION_LIST_NEXT_EVENTS("text_list_numbers.png"),

	/** Symbol "Animation - Stationsdaten anzeigen - Update" */
	ANIMATION_DATA_UPDATE("arrow_refresh.png"),

	/** Symbol "Animation - Stationsdaten anzeigen - Auto-Update" */
	ANIMATION_DATA_UPDATE_AUTO("action_go.gif"),

	/* Parameterreihe */

	/** Symbol "Parameterreihe" */
	PARAMETERSERIES("table_gear.png"),

	/** Symbol "Parameterreihe (Varianzanalyse)" */
	PARAMETERSERIES_VARIANCE("chart_curve_error.png"),

	/** Symbol "Parameterreihe - Eingabemodus 'Ressource �ndern' */
	PARAMETERSERIES_INPUT_MODE_RESOURCE("group.png"),

	/** Symbol "Parameterreihe - Eingabemodus 'Variablenwert �ndern' */
	PARAMETERSERIES_INPUT_MODE_VARIABLE("font.png"),

	/** Symbol "Parameterreihe - Eingabemodus 'XML-Element �ndern' */
	PARAMETERSERIES_INPUT_MODE_XML("brick.png"),

	/** Symbol "Parameterreihe - Konfiguration - neu" */
	PARAMETERSERIES_SETUP_NEW("page_new.gif"),

	/** Symbol "Parameterreihe - Konfiguration - laden" */
	PARAMETERSERIES_SETUP_LOAD("folder_page_white.png"),

	/** Symbol "Parameterreihe - Konfiguration - speichern" */
	PARAMETERSERIES_SETUP_SAVE("disk.png"),

	/** Symbol "Parameterreihe - Konfiguration - Vorlagen" */
	PARAMETERSERIES_SETUP_TEMPLATES("wand.png"),

	/** Symbol "Parameterreihe - Konfiguration - Ausgangsmodell" */
	PARAMETERSERIES_SETUP_SHOW_BASE_MODEL("brick.png"),

	/** Symbol "Parameterreihe - Konfiguration - Ausgangsmodell in Editor laden" */
	PARAMETERSERIES_SETUP_SHOW_BASE_MODEL_LOAD_TO_EDITOR("brick_go.png"),

	/** Symbol "Parameterreihe - Konfiguration - Eingabeparameter" */
	PARAMETERSERIES_SETUP_INPUT("brick_edit.png"),

	/** Symbol "Parameterreihe - Konfiguration - Ausgabeparameter" */
	PARAMETERSERIES_SETUP_OUTPUT("sum.png"),

	/** Symbol "Parameterreihe - Start" */
	PARAMETERSERIES_RUN("action_go.gif"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten" */
	PARAMETERSERIES_PROCESS_RESULTS("chart_curve.png"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten - Ergebnisse l�schen" */
	PARAMETERSERIES_PROCESS_RESULTS_CLEAR("delete.png"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten - Ergebnisse vergleichen" */
	PARAMETERSERIES_PROCESS_RESULTS_COMPARE("application_tile_horizontal.png"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten - Skript anwenden" */
	PARAMETERSERIES_PROCESS_RESULTS_SCRIPT("lightning_go.png"),

	/** Symbol "Parameterreihe - Ergebnisse verarbeiten - Diagramme" */
	PARAMETERSERIES_PROCESS_RESULTS_CHARTS("chart_curve.png"),

	/** Symbol "Parameterreihe - XML-Element w�hlen */
	PARAMETERSERIES_SELECT_XML("add.png"),

	/** Symbol "Parameterreihe - Eintr�ge per Assistent hinzuf�gen */
	PARAMETERSERIES_ADD_BY_ASSISTANT("wand.png"),

	/** Symbol "Parameterreihe - Eintr�ge sortieren */
	PARAMETERSERIES_SORT_TABLE("arrow_refresh.png"),

	/** Symbol "Parameterreihe - Ausgabemodus - XML-Element */
	PARAMETERSERIES_OUTPUT_MODE_XML("add.png"),

	/** Symbol "Parameterreihe - Ausgabemodus - Javascript R�ckgabe */
	PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVASCRIPT("page_white_code_red.png"),

	/** Symbol "Parameterreihe - Ausgabemodus - Java R�ckgabe */
	PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVA("page_white_cup.png"),

	/** Symbol "Parameterreihe - Ausgabemodus - Rechenausdruck */
	PARAMETERSERIES_OUTPUT_MODE_COMMAND("fx.png"),

	/** Symbol "Parameterreihe - Vorlage - Zwischenankunftszeit variieren */
	PARAMETERSERIES_TEMPLATE_MODE_INTERARRIVAL("chart_curve.png"),

	/** Symbol "Parameterreihe - Vorlage - Bedieneranzahl variieren */
	PARAMETERSERIES_TEMPLATE_MODE_OPERATORS("group.png"),

	/** Symbol "Parameterreihe - Vorlage - Bedienzeiten variieren */
	PARAMETERSERIES_TEMPLATE_MODE_SERVICETIMES("chart_curve.png"),

	/** Symbol "Parameterreihe - Vorlage - Variablenwert variieren */
	PARAMETERSERIES_TEMPLATE_MODE_VARIABLES("font.png"),

	/** Symbol "Parameterreihe - Vorlage - Verz�gerungszeit variieren */
	PARAMETERSERIES_TEMPLATE_MODE_DELAY("chart_curve.png"),

	/** Symbol "Parameterreihe - Vorlage - Analogwert variieren */
	PARAMETERSERIES_TEMPLATE_MODE_ANALOG("Bar.png"),

	/** Symbol "Parameterreihe - Vorlage - Flie�band-Transportzeit variieren */
	PARAMETERSERIES_TEMPLATE_MODE_CONVEYOR("Conveyor.png"),

	/* Optimierer */

	/** Symbol "Optimierer" */
	OPTIMIZER("chart_bar.png"),

	/** Symbol "Optimierer - Kernel w�hlen" */
	OPTIMIZER_KERNEL("cog.png"),

	/** Symbol "Optimierer - Speichermodus - alle" */
	OPTIMIZER_SAVE_MODE_ALL("application_double.png"),

	/** Symbol "Optimierer - Speichermodus - letztes Modell" */
	OPTIMIZER_SAVE_MODE_LAST("sum.png"),

	/** Symbol "Optimierer - Einstellungen - neu" */
	OPTIMIZER_SETUP_NEW("page_new.gif"),

	/** Symbol "Optimierer - Einstellungen - laden" */
	OPTIMIZER_SETUP_LOAD("folder_page_white.png"),

	/** Symbol "Optimierer - Einstellungen - speichern" */
	OPTIMIZER_SETUP_SAVE("disk.png"),

	/** Symbol "Optimierer - Starten" */
	OPTIMIZER_RUN("action_go.gif"),

	/** Symbol "Optimierer - Nebenbedingungen" */
	OPTIMIZER_CONSTRAIN("fx.png"),

	/** Symbol "Optimierer - XML-Element w�hlen" */
	OPTIMIZER_SELECT_XML("add.png"),

	/** Symbol "Optimierer - Seite 'Kontrollvariable'" */
	OPTIMIZER_PAGE_CONTROL_VARIABLE("cog.png"),

	/** Symbol "Optimierer - Seite 'Ziel'" */
	OPTIMIZER_PAGE_TARGET("chart_bar.png"),

	/** Symbol "Optimierer - Seite 'Optimierung'" */
	OPTIMIZER_PAGE_OPTIMIZATION("action_go.gif"),

	/** Symbol "Optimierer - Ergebnisdiagramm exportieren - als Text" */
	OPTIMIZER_EXPORT_TEXT("Text.gif"),

	/** Symbol "Optimierer - Ergebnisdiagramm exportieren - als Grafik" */
	OPTIMIZER_EXPORT_CHART("chart_curve.png"),

	/* Weitere Programmfunktionen */

	/** Symbol "Element-Vorlagen-Leiste" */
	ELEMENTTEMPLATES("add.png"),

	/** Symbol "Element-Vorlagen-Leiste - Filtern" */
	ELEMENTTEMPLATES_FILTER("text_list_bullets.png"),

	/** Symbol "Element-Vorlagen-Leiste - Gruppe schlie�en (Minus)" */
	ELEMENTTEMPLATES_GROUP_CLOSE("SmallMinus2.png"),

	/** Symbol "Element-Vorlagen-Leiste - Gruppe �ffnen (Plus)" */
	ELEMENTTEMPLATES_GROUP_OPEN("SmallPlus2.png"),

	/** Symbol "Element-Vorlagen-Leiste - Seitenleiste ausblenden" */
	ELEMENTTEMPLATES_CLOSEPANEL("application_side_contract.gif"),

	/** Symbol "Modell-Navigator" */
	NAVIGATOR("Navigator.png"),

	/** Symbol "Modell-Navigator - Seitenleiste ausblenden" */
	NAVIGATOR_CLOSEPANEL("application_side_contract2.gif"),

	/* Extras */

	/** Symbol "Rechner" */
	EXTRAS_CALCULATOR("calculator.png"),

	/** Symbol "Rechner - Funktionsplotter" */
	EXTRAS_CALCULATOR_PLOTTER("chart_curve.png"),

	/** Symbol "Rechner - Wahrscheinlichkeitsverteilungen" */
	EXTRAS_CALCULATOR_DISTRIBUTION("chart_curve.png"),

	/** Symbol "Rechner - Funktionsplotter - Funktion l�schen" */
	EXTRAS_CALCULATOR_PLOTTER_CLEAR("chart_curve_delete.png"),

	/** Symbol "Warteschlangenrechner (Tab-Icons)" */
	EXTRAS_QUEUE_FUNCTION("fx.png"),

	/** Symbol "Warteschlangenrechner" */
	EXTRAS_QUEUE("Symbol.png"),

	/** Symbol "Verteilung anpassen" */
	EXTRAS_FIT_DISTRIBUTION("chart_curve.png"),

	/** Symbol "Datenbankverbindung testen" */
	EXTRAS_DATABASE_TEST("database_connect.png"),

	/** Symbol "Kommandozeile" */
	EXTRAS_COMMANDLINE("application_xp_terminal.png"),

	/** Symbol "Server" */
	EXTRAS_SERVER("server.png"),

	/* Hilfe */

	/** Symbol "Hilfe" */
	HELP("help.png"),

	/** Symbol "Hilfeinhalt" */
	HELP_CONTENT("book_open.png"),

	/** Symbol "Tutorial (pdf)" */
	HELP_TUTORIAL("book.png"),

	/** Symbol "Interaktives Tutorial" */
	HELP_TUTORIAL_INTERACTIVE("cursor.png"),

	/** Symbol "Interaktive Stationsbeschreibungen" */
	HELP_STATIONS_INTERACTIVE("station.png"),

	/** Symbol "Skripting-Beschreibung (pdf)" */
	HELP_SCRIPTING("page_white_code_red.png"),

	/** Symbol f�r Untereintr�ge von "Empfohlene Literatur" */
	HELP_BOOK("book.png"),

	/** Symbol "E-Mail" */
	HELP_EMAIL("icon_mail.gif"),

	/** Symbol "Homepage" */
	HELP_HOMEPAGE("world.png"),

	/* Bilder */

	/** Symbol "Bild - Laden" */
	IMAGE_LOAD("image_add.png"),

	/** Symbol "Bild - Speichern" */
	IMAGE_SAVE("disk.png"),

	/** Symbol "Bild - Vorlagebilder" */
	IMAGE_TEMPLATE("folder_page_white.png"),

	/* Sprache */

	/** Symbol "Sprache - Englisch" */
	LANGUAGE_EN("flag_gb.png"),

	/** Symbol "Sprache - Deutsch" */
	LANGUAGE_DE("flag_de.png"),

	/* Server */

	/** Symbol "Rechenserver" */
	SERVER_CALC("server.png"),

	/** Symbol "Rechenserver - starten" */
	SERVER_CALC_START("server_add.png"),

	/** Symbol "Rechenserver - stoppen" */
	SERVER_CALC_STOP("server_delete.png"),

	/** Symbol "Webserver" */
	SERVER_CALC_WEB("world.png"),

	/** Symbol "Webserver - starten" */
	SERVER_CALC_WEB_START("world_add.png"),

	/** Symbol "Webserver - stoppen" */
	SERVER_CALC_WEB_STOP("world_delete.png"),

	/** Symbol "Fernsteuerungsserver" */
	SERVER_WEB("film.png"),

	/** Symbol "Fernsteuerungsserver - starten" */
	SERVER_WEB_START("film_add.png"),

	/** Symbol "Fernsteuerungsserver - stoppen" */
	SERVER_WEB_STOP("film_delete.png"),

	/** Symbol "DDE-Server" */
	SERVER_DDE("comment.png"),

	/** Symbol "DDE-Server - starten" */
	SERVER_DDE_START("comment_add.png"),

	/** Symbol "DDE-Server- stoppen" */
	SERVER_DDE_STOP("comment_delete.png"),

	/* Info-Panel */

	/** Symbol "Infoleiste - ausblenden - f�r diesen Eintrag" */
	INFO_PANEL_CLOSE_THIS("application_side_expand.png"),

	/** Symbol "Infoleiste - ausblenden - f�r alle Eintr�ge" */
	INFO_PANEL_CLOSE_ALL("cross.png"),

	/** Symbol "Infoleiste - Konfiguration - Gruppe ausblenden (Minus)" */
	INFO_PANEL_SETUP_HIDE("SmallMinus2.png"),

	/** Symbol "Infoleiste - Konfiguration - Gruppe einblenden (Plus)" */
	INFO_PANEL_SETUP_SHOW("SmallPlus2.png"),

	/* Datenquellen-Pr�f-Dialog */

	/** Symbol "Pr�fung der Datenquellen" */
	DATA_CHECK("action_refresh.gif"),

	/** Symbol "Pr�fung der Datenquellen - Station" */
	DATA_CHECK_STATION("station.png"),

	/** Symbol "Pr�fung der Datenquellen - Quelle 'Tabelle'" */
	DATA_CHECK_MODE_FILE("Table.png"),

	/** Symbol "Pr�fung der Datenquellen - Quelle 'Datenbank'" */
	DATA_CHECK_MODE_DB("database.png"),

	/** Symbol "Pr�fung der Datenquellen - Quelle 'DDE'" */
	DATA_CHECK_MODE_DDE("comment.png"),

	/** Symbol "Pr�fung der Datenquellen - Ergebnis 'Ok'" */
	DATA_CHECK_RESULT_OK("tick.png"),

	/** Symbol "Pr�fung der Datenquellen - Ergebnis 'Fehler'" */
	DATA_CHECK_RESULT_ERROR("cross.png"),

	/* Verbindungskanten */

	/** Symbol "Verbindungskanten-Modus - direkt" */
	EDGE_MODE_DIRECT("Line.png"),

	/** Symbol "Verbindungskanten-Modus - abgewinkelt" */
	EDGE_MODE_MULTI_LINE("Line2.png"),

	/** Symbol "Verbindungskanten-Modus - abgerundet" */
	EDGE_MODE_MULTI_LINE_ROUNDED("Line3.png"),

	/* Skripte */

	/** Symbol "Skript ausf�hren" */
	SCRIPTRUNNER("page_white_code_red.png"),

	/** Symbol "Skript - Neu" */
	SCRIPT_NEW("page_new.gif"),

	/** Symbol "Skript - Laden" */
	SCRIPT_LOAD("page_up.gif"),

	/** Symbol "Skript - Speichern" */
	SCRIPT_SAVE("disk.png"),

	/** Symbol "Skript - L�schen" */
	SCRIPT_CLEAR("page_delete.png"),

	/** Symbol "Skript - Tools" */
	SCRIPT_TOOLS("cog.png"),

	/** Symbol "Skript - Ausf�hren" */
	SCRIPT_RUN("action_go.gif"),

	/** Symbol "Skript - Ausf�hrung abbrechen" */
	SCRIPT_CANCEL("cancel.png"),

	/** Symbol "Skript - Vorlagen (f�r Elemente)" */
	SCRIPT_TEMPLATE("book.png"),

	/** Symbol "Skript - Beispiele (vollst�ndige Skripte)" */
	SCRIPT_EXAMPLE("book.png"),

	/** Symbol "Schnellfilter-Modus - Rechenausdruck" */
	SCRIPT_MODE_EXPRESSION("fx.png"),

	/** Symbol "Schnellfilter-Modus - Javascript" */
	SCRIPT_MODE_JAVASCRIPT("page_white_code_red.png"),

	/** Symbol "Schnellfilter-Modus - Java" */
	SCRIPT_MODE_JAVA("page_white_cup.png"),

	/** Symbol "Schnellfilter-Modus - Liste mit Anweisungen" */
	SCRIPT_MODE_LIST("text_list_bullets.png"),


	/** Symbol "Skript-Ausdruck - Text" */
	SCRIPT_RECORD_TEXT("font.png"),

	/** Symbol "Skript-Ausdruck - Rechenausdruck" */
	SCRIPT_RECORD_EXPRESSION("calculator.png"),

	/** Symbol "Skript-Ausdruck - XML" */
	SCRIPT_RECORD_XML("add.png"),

	/** Symbol "Skript-Ausdruck - Formatierung" */
	SCRIPT_RECORD_FORMAT("page_white_code_red.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Kunden" */
	SCRIPT_RECORD_DATA_CLIENT("user.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Stationen" */
	SCRIPT_RECORD_DATA_STATION("station.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Stationswarteschlangen" */
	SCRIPT_RECORD_DATA_STATION_QUEUE("Process.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Ressourcen" */
	SCRIPT_RECORD_DATA_RESOURCE("group.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Z�hler" */
	SCRIPT_RECORD_DATA_COUNTER("Counter.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Kosten" */
	SCRIPT_RECORD_DATA_COSTS("money_euro.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Modell" */
	SCRIPT_RECORD_MODEL("brick.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Modell bearbeiten" */
	SCRIPT_RECORD_MODEL_EDIT("brick_edit.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Statistik" */
	SCRIPT_RECORD_STATISTICS("sum2.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Statistik speichern" */
	SCRIPT_RECORD_STATISTICS_SAVE("icon_package_get.gif"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Zeit" */
	SCRIPT_RECORD_TIME("clock.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Laufzeit" */
	SCRIPT_RECORD_RUNTIME("application.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Variable" */
	SCRIPT_RECORD_VARIABLE("font.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Eingabewert" */
	SCRIPT_RECORD_INPUT("keyboard.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Analoger Wert" */
	SCRIPT_RECORD_ANALOG_VALUE("Bar.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Kunden freigeben" */
	SCRIPT_RECORD_RELEASE("TrafficLights.png"),

	/** Symbol "Skript-Ausdruck - Simulationsdaten - Ausgabewert" */
	SCRIPT_RECORD_OUTPUT("application_xp_terminal.png"),

	/** Symbol "Skript ausf�hren - Eingabe (=Skript)" */
	SCRIPT_PANEL_INPUT("lightning_go.png"),

	/** Symbol "Skript ausf�hren - Ausgabe" */
	SCRIPT_PANEL_OUTPUT("application_xp_terminal.png"),

	/* Verteilung anpassen */

	/** Symbol "Verteilung anpassen - Seite 'Werte'" */
	FIT_PAGE_VALUES("Table.png"),

	/** Symbol "Verteilung anpassen - Seite 'Empirische Verteilung'" */
	FIT_PAGE_EMPIRICAL_DISTRIBUTION("chart_bar.png"),

	/** Symbol "Verteilung anpassen - Seite 'Anpassung'" */
	FIT_PAGE_FIT("calculator.png"),

	/** Symbol "Verteilung anpassen - Seite 'Ergebnisse'" */
	FIT_PAGE_RESULT("chart_curve.png"),

	/* Einstellungen */

	/** Symbol "Einstellungen - Seite 'Benutzeroberfl�che'" */
	SETUP_PAGE_APPLICATION("application_go.png"),

	/** Symbol "Einstellungen - Seite 'Leistung'" */
	SETUP_PAGE_PERFORMANCE("computer.png"),

	/** Symbol "Einstellungen - Seite 'Animation'" */
	SETUP_PAGE_ANIMATION("film_go.png"),

	/** Symbol "Einstellungen - Seite 'Statistik'" */
	SETUP_PAGE_STATISTICS("sum.png"),

	/** Symbol "Einstellungen - Seite 'Dateiformate'" */
	SETUP_PAGE_FILE_FORMATS("folder_wrench.png"),

	/** Symbol "Dateiformat 'XML'" */
	SETUP_PAGE_FILE_FORMATS_XML("page_white_code_red.png"),

	/** Symbol "Dateiformat 'JSON'" */
	SETUP_PAGE_FILE_FORMATS_JSON("page_world.png"),

	/** Symbol "Dateiformat 'ZIP-komprimierte XML-Datei'" */
	SETUP_PAGE_FILE_FORMATS_ZIP_XML("compress.png"),

	/** Symbol "Dateiformat 'TAR.GZ-komprimierte XML-Datei'" */
	SETUP_PAGE_FILE_FORMATS_TAR_XML("tux.png"),

	/** Symbol "Dateiformat 'Verschl�sselte XML-Datei'" */
	SETUP_PAGE_FILE_FORMATS_CRYPT("lock.png"),

	/** Symbol "Einstellungen - Seite 'Lizenz'" */
	SETUP_PAGE_LICENSE("key.png"),

	/** Symbol "Einstellungen - Seite 'Update'" */
	SETUP_PAGE_UPDATE("action_refresh.gif"),

	/** Symbol "Einstellungen - Proxy" */
	SETUP_PROXY("server.png"),

	/** Symbol "Einstellungen - ... bei Anwendungsstart" */
	SETUP_APPLICATION_START("application.png"),

	/** Symbol "Einstellungen - Nach Abschluss langer Simulation Benachrichtigung anzeigen" */
	SETUP_NOTIFY_ON_LONG_RUN("clock.png"),

	/** Symbol "Einstellungen - Lizenz" */
	SETUP_LICENSE("key.png"),

	/** Symbol "Einstellungen - Lizenz - Hinzuf�gen" */
	SETUP_LICENSE_ADD("key_add.png"),

	/** Symbol "Einstellungen - Lizenz - Entfernen" */
	SETUP_LICENSE_DELETE("key_delete.png"),

	/** Symbol "Einstellungen - Schriftgr��e klein" */
	SETUP_FONT_SIZE1("FontSize_1.png"),

	/** Symbol "Einstellungen - Schriftgr��e normal" */
	SETUP_FONT_SIZE2("FontSize_2.png"),

	/** Symbol "Einstellungen - Schriftgr��e gr��er" */
	SETUP_FONT_SIZE3("FontSize_3.png"),

	/** Symbol "Einstellungen - Schriftgr��e gro�" */
	SETUP_FONT_SIZE4("FontSize_4.png"),

	/** Symbol "Einstellungen - Schriftgr��e ganz gro�" */
	SETUP_FONT_SIZE5("FontSize_5.png"),

	/** Symbol "Einstellungen - Fenstergr��e - Vorgabe" */
	SETUP_WINDOW_SIZE_DEFAULT("application_double.png"),

	/** Symbol "Einstellungen - Fenstergr��e - Vollbild" */
	SETUP_WINDOW_SIZE_FULL("application.png"),

	/** Symbol "Einstellungen - Fenstergr��e - Letzte wiederherstellen" */
	SETUP_WINDOW_SIZE_LAST("application_edit.png"),

	/** Symbol "Einstellungen - Vorlagenleiste beim Programmstart - Ausblenden" */
	SETUP_TEMPLATES_ON_START_HIDE("application_side_contract.png"),

	/** Symbol "Einstellungen - Vorlagenleiste beim Programmstart - Einblenden" */
	SETUP_TEMPLATES_ON_START_SHOW("application_side_expand.png"),

	/** Symbol "Einstellungen - Vorlagenleiste beim Programmstart - Letzter Zustand" */
	SETUP_TEMPLATES_ON_START_LAST("application_side_boxes.png"),

	/** Symbol "Einstellungen - Javascript-Engine - automatisch" */
	SETUP_ENGINE_AUTOMATIC("page_white_find.png"),

	/** Symbol "Einstellungen - Javascript-Engine - Nashorn" */
	SETUP_ENGINE_NASHORN("cup.png"),

	/** Symbol "Einstellungen - Javascript-Engine - Rhino" */
	SETUP_ENGINE_RHINO("MozillaRhino.gif"),

	/** Symbol "Einstellungen - Javascript-Engine - Graal" */
	SETUP_ENGINE_GRAAL("GraalJS.gif"),

	/** Symbol "Einstellungen - Einschwingphase bei Animationen - Anwenden" */
	SETUP_ANIMATION_START_NORMAL("action_go.gif"),

	/** Symbol "Einstellungen - Einschwingphase bei Animationen - Fragen ob �berspringen" */
	SETUP_ANIMATION_START_ASK("help.png"),

	/** Symbol "Einstellungen - Einschwingphase bei Animationen - �berspringen" */
	SETUP_ANIMATION_START_SKIP("control_end_blue.png"),

	/** Symbol "Einstellungen - Einschwingphase bei Animationen - Vorab ohne Animation ausf�hren" */
	SETUP_ANIMATION_START_FAST("control_fastforward_blue.png"),

	/* Modelleditor */

	/** Symbol "Modelleditor - Element hinzuf�gen nicht zul�ssig" */
	MODELEDITOR_NOT_ALLOWED("NotAllowed.png"),

	/** Symbol "Modelleditor - Kante hinzuf�gen nicht zul�ssig" */
	MODELEDITOR_NOT_ALLOWED_EDGE("NotAllowedEdge.png"),

	/** Symbol "Modelleditor - Beschreibung zu einzelnem Element" */
	MODELEDITOR_COMMENT("comment_edit.png"),

	/** Symbol "Modelleditor - Vorlagen - Gruppe ausklappen (Plus)" */
	MODELEDITOR_GROUP_PLUS("SmallPlus.png"),

	/** Symbol "Modelleditor - Vorlagen - Gruppe einklappen (Minus)" */
	MODELEDITOR_GROUP_MINUS("SmallMinus.png"),

	/** Symbol "Modelleditor - Eingabetabelle w�hlen" */
	MODELEDITOR_OPEN_INPUT_FILE("table_go.png"),

	/** Symbol "Modelleditor - Ausgabetabelle w�hlen" */
	MODELEDITOR_OPEN_OUTPUT_FILE("table_go.png"),

	/** Symbol "Modelleditor - Eigenschaften" */
	MODELEDITOR_ELEMENT_PROPERTIES("cog.png"),

	/** Symbol "Modelleditor - Eigenschaften - Aussehen" */
	MODELEDITOR_ELEMENT_PROPERTIES_APPEARANCE("image.gif"),

	/** Symbol "Modelleditor - Eigenschaften - Text" */
	MODELEDITOR_ELEMENT_PROPERTIES_TEXT("font.png"),

	/** Symbol "Modelleditor - Eigenschaften - Rahmen" */
	MODELEDITOR_ELEMENT_PROPERTIES_BORDER("Rectangle.png"),

	/** Symbol "Modelleditor - Kontextmen� 'Visualisierung hinzuf�gen'" */
	MODELEDITOR_ELEMENT_ADD_VISUALIZATION("chart_bar.png"),


	/** Symbol "Modelleditor - Kontextmen� 'Laufzeitstatistik hinzuf�gen'" */
	MODELEDITOR_ELEMENT_ADD_LONG_RUN_STATISTICS("chart_curve_add.png"),

	/** Symbol "Modelleditor - Kontextmen� 'Typische Folgestation hinzuf�gen'" */
	MODELEDITOR_ELEMENT_NEXT_STATIONS("station.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Ziel" */
	MODELEDITOR_ELEMENT_TRANSPORT_TARGET("Dispose.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Transporter" */
	MODELEDITOR_ELEMENT_TRANSPORT_TRANSPORTER("lorry.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Priorit�ten" */
	MODELEDITOR_ELEMENT_TRANSPORT_PRIORITIES("user.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Gruppengr��e" */
	MODELEDITOR_ELEMENT_TRANSPORT_BATCH("Batch.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Modus 'explizit'" */
	MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_EXPLICITE("Dispose.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Modus 'Fertigungsplan'" */
	MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_SEQUENCE("text_list_numbers.png"),

	/** Symbol "Modelleditor - Transporteigenschaften - Modus 'nach Eigenschaft'" */
	MODELEDITOR_ELEMENT_TRANSPORT_TARGET_MODE_PROPERTY("font.png"),

	/* Modelleditor: Eingang/Ausgang */

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle'" */
	MODELEDITOR_ELEMENT_SOURCE("Source.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Verteilung " */
	MODELEDITOR_ELEMENT_SOURCE_MODE_DISTRIBUTION("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Ausdruck " */
	MODELEDITOR_ELEMENT_SOURCE_MODE_EXPRESSION("fx.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Zeitplan" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_SCHEDULE("clock.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Bedingung" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_CONDITION("TrafficLights.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Schwllenwert" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_THRESHOLD("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Signal" */
	MODELEDITOR_ELEMENT_SOURCE_MODE_SIGNALS("Signal.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Zwischenankunftszeiten'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_INTERARRIVAL("clock.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Batch-Gr��e'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_BATCH("group.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Anzahl an Kunden'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_COUNT("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Startzeitpunkt'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_START("flag_green.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Zuweisung von Kundenvariablen'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_SET_NUMBERS("font.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Quelle' - Dialogseite 'Zuweisung von Texten'" */
	MODELEDITOR_ELEMENT_SOURCE_PAGE_SET_TEXTS("font.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Mehrfachquelle'" */
	MODELEDITOR_ELEMENT_SOURCE_MULTI("Source.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Tabellenquelle'" */
	MODELEDITOR_ELEMENT_SOURCE_TABLE("Source.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Datenbankquelle'" */
	MODELEDITOR_ELEMENT_SOURCE_DB("database_go.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'DDE-Quelle'" */
	MODELEDITOR_ELEMENT_SOURCE_DDE("database_go.png"),

	/** Symbol "Modelleditor - Gruppe 'Eingang/Ausgang' - Element 'Ausgang'" */
	MODELEDITOR_ELEMENT_DISPOSE("Dispose.png"),

	/* Modelleditor: Verarbeitung */

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation'" */
	MODELEDITOR_ELEMENT_PROCESS("Process.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Bedienzeiten'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_SERVICE("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'R�stzeiten'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_SETUP("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Nachbearbeitungszeiten'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_POST_PROCESS("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Wartezeittoleranzen'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_CANCEL("cancel.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Priorit�ten und Batch-Gr��en'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_PRORITY("user.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Bediener'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_RESOURCES("group.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Bedienstation' - Dialogseite 'Kosten'" */
	MODELEDITOR_ELEMENT_PROCESS_PAGE_COSTS("money_euro.png"),

	/** Symbol "Modelleditor - Gruppe 'Verarbeitung' - Element 'Verz�gerung'" */
	MODELEDITOR_ELEMENT_DELAY("Delay.png"),

	/* Modelleditor: Zuweisungen */

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Typzuweisung'" */
	MODELEDITOR_ELEMENT_ASSIGN("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Textzuweisung'" */
	MODELEDITOR_ELEMENT_ASSIGN_STRING("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Kosten'" */
	MODELEDITOR_ELEMENT_COSTS("money_euro.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Variable'" */
	MODELEDITOR_ELEMENT_SET("Set.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Script'" */
	MODELEDITOR_ELEMENT_SET_JS("Set.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Z�hler'" */
	MODELEDITOR_ELEMENT_COUNTER("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Multiz�hler'" */
	MODELEDITOR_ELEMENT_COUNTER_MULTI("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Durchsatz'" */
	MODELEDITOR_ELEMENT_THROUGHPUT("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Zustand'" */
	MODELEDITOR_ELEMENT_STATE_STATISTICS("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Differenzz�hler'" */
	MODELEDITOR_ELEMENT_DIFFERENTIAL_COUNTER("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Bereich betreten'" */
	MODELEDITOR_ELEMENT_SECTION_START("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Bereich verlassen'" */
	MODELEDITOR_ELEMENT_SECTION_END("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Zuweisungen' - Element 'Bereich Kundenstatistik'" */
	MODELEDITOR_ELEMENT_SET_STATISTICS_MODE("chart_curve.png"),

	/* Modelleditor: Verzweigungen */

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Duplizieren'" */
	MODELEDITOR_ELEMENT_DUPLICATE("Duplicate.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen'" */
	MODELEDITOR_ELEMENT_DECIDE("Decide.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - zuf�llig" */
	MODELEDITOR_ELEMENT_DECIDE_BY_CHANCE("arrow_switch.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach Bedingung" */
	MODELEDITOR_ELEMENT_DECIDE_BY_CONDITION("TrafficLights.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach Kundentyp" */
	MODELEDITOR_ELEMENT_DECIDE_BY_CLIENT_TYPE("group.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - abwechselnd" */
	MODELEDITOR_ELEMENT_DECIDE_BY_SEQUENCE("Counter.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach k�rzester Warteschlange an der n�chsten Station" */
	MODELEDITOR_ELEMENT_DECIDE_BY_SHORTEST_QUEUE_NEXT_STATION("Station.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach k�rzester Warteschlange an der n�chsten Bedienstation" */
	MODELEDITOR_ELEMENT_DECIDE_BY_SHORTEST_QUEUE_NEXT_PROCESS_STATION("Process.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - wenigste Kunden an der n�chsten Station" */
	MODELEDITOR_ELEMENT_DECIDE_BY_LEAST_CLIENTS_NEXT_STATION("Station.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - wenigste Kunden an der n�chsten Bedienstation" */
	MODELEDITOR_ELEMENT_DECIDE_BY_LEAST_CLIENTS_NEXT_PROCESS_STATION("Process.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen' - nach Texteigenschaft" */
	MODELEDITOR_ELEMENT_DECIDE_BY_TEXT_PROPERTY("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Verzweigen (Skript)'" */
	MODELEDITOR_ELEMENT_DECIDE_JS("Decide.png"),

	/** Symbol "Modelleditor - Gruppe 'Verzweigungen' - Element 'Zur�ckschrecken'" */
	MODELEDITOR_ELEMENT_BALKING("Decide.png"),

	/* Modelleditor: Schranken */

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Bedingung'" */
	MODELEDITOR_ELEMENT_HOLD("Hold.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Multibedingung'" */
	MODELEDITOR_ELEMENT_HOLD_MULTI("HoldMulti.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Bedingung (Skript)'" */
	MODELEDITOR_ELEMENT_HOLD_JS("HoldJS.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Signal'" */
	MODELEDITOR_ELEMENT_SIGNAL("Signal.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Schranke'" */
	MODELEDITOR_ELEMENT_BARRIER("Barrier.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Pull-Schranke'" */
	MODELEDITOR_ELEMENT_BARRIER_PULL("Barrier.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Ressource belegen'" */
	MODELEDITOR_ELEMENT_SEIZE("Seize.png"),

	/** Symbol "Modelleditor - Gruppe 'Schranken' - Element 'Ressource freigeben'" */
	MODELEDITOR_ELEMENT_RELEASE("Release.png"),

	/* Modelleditor: Kunden verbinden */

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Zusammenfassen'" */
	MODELEDITOR_ELEMENT_BATCH("Batch.png"),

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Trennen'" */
	MODELEDITOR_ELEMENT_SEPARATE("Separate.png"),

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Zusammenf�hren'" */
	MODELEDITOR_ELEMENT_MATCH("Match.png"),

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Ausleiten'" */
	MODELEDITOR_ELEMENT_PICK_UP("PickUp.png"),

	/** Symbol "Modelleditor - Gruppe 'Kunden verbinden' - Element 'Zerteilen'" */
	MODELEDITOR_ELEMENT_SPLIT("Source.png"),

	/* Modelleditor: Transport */

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Transportstart'" */
	MODELEDITOR_ELEMENT_TRANSPORT_SOURCE("TransportSource.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Haltestelle'" */
	MODELEDITOR_ELEMENT_TRANSPORT_TRANSPORTER_SOURCE("TransportSource.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Transportstart (Fertigungsplan)' (veraltet)" */
	MODELEDITOR_ELEMENT_TRANSPORT_SOURCE_ROUTER("TransportSource.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Transportziel'" */
	MODELEDITOR_ELEMENT_TRANSPORT_DESTINATION("TransportDestination.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Parkplatz'" */
	MODELEDITOR_ELEMENT_TRANSPORT_PARKING("Vertex.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Plan zuweisen'" */
	MODELEDITOR_ELEMENT_ASSIGN_SEQUENCE("Assign.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Wegpunkt'" */
	MODELEDITOR_ELEMENT_WAY_POINT("Vertex.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Teleport-Transport Startpunkt'" */
	MODELEDITOR_ELEMENT_TELEPORT_SOURCE("TransportSource.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Teleport-Transport Zielpunkt'" */
	MODELEDITOR_ELEMENT_TELEPORT_DESTINATION("TransportDestination.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Flie�band'" */
	MODELEDITOR_ELEMENT_CONVEYOR("Conveyor.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Flie�band'- Dialogseite 'Ben�tigte und vorhandene Kapazit�t'" */
	MODELEDITOR_ELEMENT_CONVEYOR_PAGE_CAPACITY("package.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Flie�band'- Dialogseite 'Transportzeit'" */
	MODELEDITOR_ELEMENT_CONVEYOR_PAGE_TIME("clock.png"),

	/** Symbol "Modelleditor - Gruppe 'Transport' - Element 'Flie�band'- Dialogseite 'Animation'" */
	MODELEDITOR_ELEMENT_CONVEYOR_PAGE_ANIMATION("film_go.png"),

	/* Modelleditor: Daten Ein-/Ausgabe */

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Eingabe'" */
	MODELEDITOR_ELEMENT_INPUT("folder_page_white.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Eingabe (Skript)'" */
	MODELEDITOR_ELEMENT_INPUT_JS("folder_page_white.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Eingabe (DB)'" */
	MODELEDITOR_ELEMENT_INPUT_DB("folder_page_white.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Eingabe (DDE)'" */
	MODELEDITOR_ELEMENT_INPUT_DDE("folder_page_white.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Ausgabe'" */
	MODELEDITOR_ELEMENT_OUTPUT("disk.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Ausgabe (Skript)'" */
	MODELEDITOR_ELEMENT_OUTPUT_JS("disk.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Ausgabe (DB)'" */
	MODELEDITOR_ELEMENT_OUTPUT_DB("disk.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Ausgabe (DDE)'" */
	MODELEDITOR_ELEMENT_OUTPUT_DDE("disk.png"),

	/** Symbol "Modelleditor - Gruppe 'Daten Ein-/Ausgabe' - Element 'Aufzeichnung'" */
	MODELEDITOR_ELEMENT_RECORD("disk.png"),

	/* Modelleditor: Flusssteuerungslogik */

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'If'" */
	MODELEDITOR_ELEMENT_LOGIC_IF("LogicIf.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'ElseIf'" */
	MODELEDITOR_ELEMENT_LOGIC_ELSE_IF("LogicElseIf.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'Else'" */
	MODELEDITOR_ELEMENT_LOGIC_ELSE("LogicElse.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'EndIf'" */
	MODELEDITOR_ELEMENT_LOGIC_END_IF("LogicEndIf.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'While'" */
	MODELEDITOR_ELEMENT_LOGIC_WHILE("LogicWhile.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'EndWhile'" */
	MODELEDITOR_ELEMENT_LOGIC_END_WHILE("LogicEndWhile.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'Do'" */
	MODELEDITOR_ELEMENT_LOGIC_DO("LogicDo.png"),

	/** Symbol "Modelleditor - Gruppe 'Flusssteuerungslogik' - Element 'Until'" */
	MODELEDITOR_ELEMENT_LOGIC_UNTIL("LogicUntil.png"),

	/* Modelleditor: Analoge Werte */

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Analoger Wert'" */
	MODELEDITOR_ELEMENT_ANALOG_VALUE("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Analogen W. �ndern'" */
	MODELEDITOR_ELEMENT_ANALOG_ASSIGN("Set.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Tank'" */
	MODELEDITOR_ELEMENT_TANK("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Fluss'" */
	MODELEDITOR_ELEMENT_TANK_FLOW_BY_CLIENT("arrow_right.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Fluss (Signal)'" */
	MODELEDITOR_ELEMENT_TANK_FLOW_BY_SIGNAL("arrow_right.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Tank' - Ventil" */
	MODELEDITOR_ELEMENT_TANK_VALVE("database_gear.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Sensor'" */
	MODELEDITOR_ELEMENT_SENSOR("eye.png"),

	/** Symbol "Modelleditor - Gruppe 'Analoge Werte' - Element 'Ventil-Setup'" */
	MODELEDITOR_ELEMENT_VALVE_SETUP("arrow_right.png"),

	/* Modelleditor: Animation */

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Icon'" */
	MODELEDITOR_ELEMENT_CLIENT_ICON("image.gif"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdaten als Text'" */
	MODELEDITOR_ELEMENT_ANIMATION_TEXT_VALUE("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'LCD-Anzeige'" */
	MODELEDITOR_ELEMENT_ANIMATION_LCD("SevenSegments.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Analogskala'" */
	MODELEDITOR_ELEMENT_ANIMATION_POINTER_MEASURING("Scale.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Skriptergebnis als Text'" */
	MODELEDITOR_ELEMENT_ANIMATION_TEXT_VALUE_JS("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Text gem�� Simulationsdaten'" */
	MODELEDITOR_ELEMENT_ANIMATION_TEXT_SELECT(""),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdaten als Balken'" */
	MODELEDITOR_ELEMENT_ANIMATION_BAR("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdaten als gestapelter Balken'" */
	MODELEDITOR_ELEMENT_ANIMATION_BAR_STACK("BarStack.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenampel (2 Lichter)'" */
	MODELEDITOR_ELEMENT_ANIMATION_TRAFFIC_LIGHTS("TrafficLights.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenampel (3 Lichter)'" */
	MODELEDITOR_ELEMENT_ANIMATION_TRAFFIC_LIGHTS3("TrafficLights3.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenliniendiagramm'" */
	MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenliniendiagramm' - Reihe hinzuf�gen" */
	MODELEDITOR_ELEMENT_ANIMATION_DIAGRAM_ADD("chart_curve_add.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenbalkendiagramm'" */
	MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART("chart_bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatenbalkendiagramm' - Reihe hinzuf�gen" */
	MODELEDITOR_ELEMENT_ANIMATION_BAR_CHART_ADD("chart_bar_add.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatentortendiagramm'" */
	MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART("chart_pie.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationsdatentortendiagramm' - Segment hinzuf�gen" */
	MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART_ADD("chart_pie_add.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Simulationszeit'" */
	MODELEDITOR_ELEMENT_ANIMATION_CLOCK("clock.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Animationsbild'" */
	MODELEDITOR_ELEMENT_ANIMATION_IMAGE("image.gif"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Animationsbild' - Beispiel" */
	MODELEDITOR_ELEMENT_ANIMATION_IMAGE_EXAMPLE("Picture_Example.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation' - Element 'Datenaufzeichnung anzeigen'" */
	MODELEDITOR_ELEMENT_ANIMATION_RECORD("chart_curve.png"),

	/* Modelleditor: Animation - Interaktiv */

	/** Symbol "Modelleditor - Gruppe 'Animation - Interaktiv' - Element 'Schaltfl�che'" */
	MODELEDITOR_ELEMENT_INTERACTIVE_BUTTON("buttonOk.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation - Interaktiv' - Element 'Schieberegler'" */
	MODELEDITOR_ELEMENT_INTERACTIVE_SLIDER("Bar.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation - Interaktiv' - Element 'Checkbox'" */
	MODELEDITOR_ELEMENT_INTERACTIVE_CHECKBOX("Check.png"),

	/** Symbol "Modelleditor - Gruppe 'Animation - Interaktiv' - Element 'Radiobutton'" */
	MODELEDITOR_ELEMENT_INTERACTIVE_RADIOBUTTON("Radiobutton.png"),

	/* Modelleditor: Sonstiges */

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Statistik'" */
	MODELEDITOR_ELEMENT_USER_STATISTICS("chart_curve.png"),

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Aktion'" */
	MODELEDITOR_ELEMENT_ACTION("eye.png"),

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Untermodell'" */
	MODELEDITOR_ELEMENT_SUB("application_add.png"),

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Untermodell' - Bearbeiten" */
	MODELEDITOR_ELEMENT_SUB_EDIT("brick_edit.png"),

	/** Symbol "Modelleditor - Gruppe 'Sonstiges' - Element 'Referenz'" */
	MODELEDITOR_ELEMENT_REFERENCE("group.png"),

	/* Modelleditor: Optische Gestaltung */

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Beschreibungstext'" */
	MODELEDITOR_ELEMENT_TEXT("Text.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Verbindungsecke'" */
	MODELEDITOR_ELEMENT_VERTEX("Vertex.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Linie'" */
	MODELEDITOR_ELEMENT_LINE("Line.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Rechteck'" */
	MODELEDITOR_ELEMENT_RECTANGLE("Rectangle.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Ellipse'" */
	MODELEDITOR_ELEMENT_ELLIPSE("Ellipse.png"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Bild'" */
	MODELEDITOR_ELEMENT_IMAGE("image.gif"),

	/** Symbol "Modelleditor - Gruppe 'Optische Gestaltung' - Element 'Bild' - Beispiel" */
	MODELEDITOR_ELEMENT_IMAGE_EXAMPLE("Picture_Example.png");

	private final String name;
	private URL url;
	private Icon icon;

	Images(final String name) {
		this.name=name;
	}

	/**
	 * Liefert die URL des Icons
	 * @return	URL des Icons
	 */
	public URL getURL() {
		if (url==null) url=getClass().getResource("res/"+name);
		assert(url!=null);
		return url;
	}

	/**
	 * Liefert das Icon
	 * @return	Icon
	 */
	public Icon getIcon() {
		if (icon==null) {
			final URL url=getURL();
			if (url!=null) icon=new ImageIcon(url);
		}
		assert(icon!=null);
		return icon;
	}

	/**
	 * Pr�ft, ob alle Icons vorhanden sind.
	 */
	public static void checkAll() {
		for (Images image: values()) {
			System.out.print(image.name+": ");
			if (image.getIcon()==null) System.out.println("missing"); else System.out.println("ok");
		}
	}
}
