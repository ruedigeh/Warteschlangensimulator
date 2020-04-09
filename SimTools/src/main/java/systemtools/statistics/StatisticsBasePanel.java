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
package systemtools.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.Range;

import mathtools.Table;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.commandline.AbstractReportCommandConnect;
import systemtools.images.SimToolsImages;
import systemtools.statistics.StatisticViewer.CanDoAction;
import systemtools.statistics.StatisticViewerReport.FileFormat;
import systemtools.statistics.StatisticViewerSpecialText.SpecialMode;

/**
 * Diese Klasse stellt Basisfunktionen zur Anzeige von Statistikdaten bereit
 * @author Alexander Herzog
 * @version 1.7
 */
public abstract class StatisticsBasePanel extends JPanel implements AbstractReportCommandConnect {
	private static final long serialVersionUID = 6849412691047065086L;

	/**
	 * Welche Optionen sollen auf Text- und Tabellen-Seiten zum direkten Anzeigen der Daten in externen Anwendungen angezeigt werden?
	 * @author Alexander Herzog
	 * @see #viewerPrograms
	 */
	public enum ViewerPrograms {
		/** Auf Textseiten anbieten: Word */
		WORD,
		/** Auf Tabellenseiten anbieten: Excel */
		EXCEL,
		/** Auf Textseiten anbieten: OpenOffice/LibreOffice Text */
		ODT,
		/** Auf Tabellenseiten anbieten: OpenOffice/LibreOffice Spreadsheet */
		ODS
	}

	/** Bezeichner f�r den Typ des Baumeintrags "Text" */
	public static String typeText="Text";
	/** Bezeichner f�r den Typ des Baumeintrags "Tabelle" */
	public static String typeTable="Tabelle";
	/** Bezeichner f�r den Typ des Baumeintrags "Grafik" */
	public static String typeImage="Grafik";
	/** Bezeichner f�r den Typ des Baumeintrags "keine Daten" */
	public static String typeNoData="keine Daten ausgew�hlt";

	/** Titel f�r die Warnung "Datei existiert bereits" */
	public static String overwriteTitle="Datei existiert bereits";
	/** Inhalt f�r die Warnung "Datei existiert bereits" */
	public static String overwriteInfo="Die Datei\n%s\n existiert bereits. Soll diese jetzt �berschrieben werden?";

	/** Titel f�r die Fehlermeldung "Fehler beim Speichern der Datei" */
	public static String writeErrorTitle="Fehler beim Speichern der Datei";
	/** Inhalt f�r die Fehlermeldung "Fehler beim Speichern der Datei" */
	public static String writeErrorInfo="Die Datei\n%s\nkonnte nicht erstellt werden.";

	/** Bezeichner f�r Kontextmen� f�r Kommandozeilenparameter f�r die Statistikseite */
	public static String treeCopyParameter="Parameter f�r Daten in die Zwischenablage kopieren";
	/** Bezeichner f�r Tooltip f�r Kontextmen� f�r Kommandozeilenparameter f�r die Statistikseite */
	public static String treeCopyParameterHint="Parameter, um diese Daten per Kommandozeile abzurufen, in die Zwischenablage kopieren";

	/** �berschrift �ber einen Viewer, der als Inhalt lediglich auf die Unterelemente verweist */
	public static String viewersInformation="Information";

	/** Inhalt der Fehlermeldung "html kann nicht direkt gedruckt werden." */
	public static String viewersNoHTMLApplicationInfo="Es ist keine Anwendung f�r den direkten Druck von html-Dateien registriert. Die Reportdatei wird nun im Standardbrowser ge�ffnet. Bitte starten Sie den Druck von dort aus manuell.";
	/** Titel der Fehlermeldung "html kann nicht direkt gedruckt werden." */
	public static String viewersNoHTMLApplicationTitle="Keine Anwendung f�r den Druck von html-Dateien registriert";

	/** Titel des Text-Speichern Dateiauswahldialogs */
	public static String viewersSaveText="Text speichern";
	/** Titel des Tabelle-Speichern Dateiauswahldialogs */
	public static String viewersSaveTable="Tabelle speichern";
	/** Titel des Bild-Speichern Dateiauswahldialogs */
	public static String viewersSaveImage="Bild speichern";

	/** Titel des Eingabedialogs zur Definition der Gr��e der zu speichernden Bilder */
	public static String viewersSaveImageSizeTitle="Exportieren von Grafiken";
	/** Eingabeprompt im Eingabedialog zur Definition der Gr��e der zu speichernden Bilder */
	public static String viewersSaveImageSizePrompt="Aufl�sung beim Speichern";
	/** Titel der Fehlermeldung "Ung�ltige Bildgr��e" */
	public static String viewersSaveImageSizeErrorTitle="Bildgr��e muss eine nat�rliche Zahl sein";
	/** Inhalt der Fehlermeldung "Ung�ltige Bildgr��e" */
	public static String viewersSaveImageSizeErrorInfo="Die angegebene Bildgr��e ist ung�ltig. Die Gr��e muss eine nat�rliche Zahl sein.";
	/** Titel der Fehlermeldung "Fehler beim Speichern der Grafik" */
	public static String viewersSaveImageErrorTitle="Fehler beim Speichern der Grafik";
	/** Inhalt der Fehlermeldung "Fehler beim Speichern der Grafik" */
	public static String viewersSaveImageErrorInfo="Die Grafik konnte nicht in der Datei\n%s\ngespeichert werden.";

	/** Bezeichner f�r das Statistikbaum-Toolbar-Button "Zusammenfassung erstellen" */
	public static String viewersReport="Zusammenfassung erstellen";
	/** Bezeichner f�r den Tooltip f�r das Statistikbaum-Toolbar-Button "Zusammenfassung erstellen" */
	public static String viewersReportHint="Erstellt einen Bericht �ber eine selbst zusammenstellbare Teilmenge der Ergebnisse";
	/** Bezeichner f�r den Tooltip f�r das Statistikbaum-Toolbar-Button "Funktionen zum Ein- und Ausklappen von Kategorien" */
	public static String viewersToolsHint="Funktionen zum Ein- und Ausklappen von Kategorien";
	/** Bezeichner f�r den Kontextmen�-Eintrag "Alle Kategorien ausklappen" */
	public static String viewersToolsShowAll="Alle Kategorien ausklappen";
	/** Bezeichner f�r den Kontextmen�-Eintrag "Alle Kategorien einklappen" */
	public static String viewersToolsHideAll="Alle Kategorien einklappen";

	/** Titel f�r die Report-Generierungs-Fehlermeldung "Keine Daten zum Speichern ausgew�hlt" */
	public static String viewersReportNoTablesSelectedTitle="Keine Daten zum Speichern ausgew�hlt";
	/** Inhalt der Report-Generierungs-Fehlermeldung "Keine Daten zum Speichern ausgew�hlt" */
	public static String viewersReportNoTablesSelectedInfo="Es sind keine Tabellen ausgew�hlt, die in einer gemeinsamen Arbeitsmappe gespeichert werden k�nnten.";
	/** Titel f�r den Speicherndialog zum Speichern von Report-Tabellen als Arbeitsmappe */
	public static String viewersReportSaveWorkbook="Arbeitsmappe speichern";
	/** Titel f�r die Report-Generierungs-Fehlermeldung "Fehlern beim Speichern der Arbeitsmappe" */
	public static String viewersReportSaveWorkbookErrorTitle="Fehlern beim Speichern der Arbeitsmappe";
	/** Inhalt der Report-Generierungs-Fehlermeldung "Fehlern beim Speichern der Arbeitsmappe" */
	public static String viewersReportSaveWorkbookErrorInfo="Die Arbeitsmappe konnte nicht in der Datei\n%s\ngespeichert werden.";
	/** Bezeichner f�r Frage, ob Bilder in html-Reports inline oder separat gespeichert werden sollen */
	public static String viewersReportSaveHTMLImages="Bitte w�hlen Sie aus, wie Bilder in der Zusammenfassung gespeichert werden sollen";
	/** Bezeichner f�r Bilder inline in html-Reports */
	public static String viewersReportSaveHTMLImagesInline="Direkt in der html-Datei (insgesamt nur eine Ausgabedatei; kann von Word nicht gelesen werden)";
	/** Bezeichner f�r Bilder separat bei html-Reports */
	public static String viewersReportSaveHTMLImagesFile="Als verkn�pfte Grafikdateien (mehrere Grafikdateien neben der html-Ausgabedatei)";
	/** Bezeichner f�r Titel des html+js interaktiven html-Reports */
	public static String viewersReportSaveHTMLAppTitle="Statistik";
	/** Bezeichner f�r Info zur Baumstruktur im html+js interaktiven html-Report */
	public static String viewersReportSaveHTMLAppInfo="W�hlen Sie in der Baumstruktur links die anzuzeigende Rubrik aus.";

	/** Bezeichner f�r das Toolbar-Button "Standardzoom" */
	public static String viewersToolbarZoom="Standardzoom";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Standardzoom" */
	public static String viewersToolbarZoomHint="Stellt den Zoomfaktor so ein, dass das gesamte Diagramm sichtbar ist.";
	/** Bezeichner f�r das Toolbar-Button "Kopieren" */
	public static String viewersToolbarCopy="Kopieren";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Kopieren" */
	public static String viewersToolbarCopyHint="Kopiert die Ergebnisse von dieser Seite in die Zwischenablage.";
	/** Bezeichner f�r das Toolbar-Button "Drucken" */
	public static String viewersToolbarPrint="Drucken";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Kopieren" */
	public static String viewersToolbarPrintHint="Druckt die Ergebnisse von dieser Seite aus.";
	/** Bezeichner f�r das Toolbar-Button "Speichern" */
	public static String viewersToolbarSave="Speichern";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Kopieren" */
	public static String viewersToolbarSaveHint="Speichert die auf dieser Seite angezeigten Ergebnisse in einer Datei.";
	/** Bezeichner f�r das Toolbar-Button "Einstellungen" */
	public static String viewersToolbarSettings="Einstellungen";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Kopieren" */
	public static String viewersToolbarSettingsHint="Einstellungen zu der gew�hlten Statistik-Anzeige vornehmen.";
	/** Bezeichner f�r das Toolbar-Button "Text in externer Anwendung anzeigen" */
	public static String viewersToolbarOpenText="�ffnen";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Text in externer Anwendung anzeigen" */
	public static String viewersToolbarOpenTextHint="Text in externer Anwendung anzeigen";
	/** Bezeichner f�r das Toolbar-Button "Tabelle in externer Anwendung anzeigen" */
	public static String viewersToolbarOpenTable="�ffnen";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Tabelle in externer Anwendung anzeigen" */
	public static String viewersToolbarOpenTableHint="Tabelle in externer Anwendung anzeigen";
	/** Bezeichner f�r das Toolbar-Button "Word" */
	public static String viewersToolbarWord="Word";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Word" */
	public static String viewersToolbarWordHint="Text in Word anzeigen";
	/** Bezeichner f�r das Toolbar-Button "OpenOffice/LibreOffice Text" */
	public static String viewersToolbarODT="OpenOffice/LibreOffice";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "OpenOffice/LibreOffice Text" */
	public static String viewersToolbarODTHint="Text in OpenOffice/LibreOffice anzeigen";
	/** Bezeichner f�r das Toolbar-Button "Excel" */
	public static String viewersToolbarExcel="Excel";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "Excel" */
	public static String viewersToolbarExcelHint="Tabelle in Excel anzeigen";
	/** Bezeichner f�r das Toolbar-Button "OpenOffice/LibreOffice Tabelle" */
	public static String viewersToolbarODS="OpenOffice/LibreOffice";
	/** Bezeichner f�r den Tooltip f�r das Toolbar-Button "OpenOffice/LibreOffice Tabelle" */
	public static String viewersToolbarODSHint="Tabelle in OpenOffice/LibreOffice anzeigen";
	/** Bezeichner f�r Prefix f�r Excel-Temp-Dateien */
	public static String viewersToolbarExcelPrefix="Simulator_Temp";
	/** Bezeichner f�r Titel f�r Fehlermeldung f�r Excel-Speicherung */
	public static String viewersToolbarExcelSaveErrorTitle="Speichern fehlgeschlagen";
	/** Bezeichner f�r Infotext f�r Fehlermeldung f�r Excel-Speicherung */
	public static String viewersToolbarExcelSaveErrorInfo="Die Tabelle konnte nicht gespeichert werden.";

	/** Bezeichner f�r das Report-Toolbar-Button "Alle" */
	public static String viewersToolbarSelectAll="Alle";
	/** Bezeichner f�r den Tooltip f�r das Report-Toolbar-Button "Alle" */
	public static String viewersToolbarSelectAllHint="Alle Eintr�ge ausw�hlen";
	/** Bezeichner f�r das Report-Toolbar-Button "Keine" */
	public static String viewersToolbarSelectNone="Keine";
	/** Bezeichner f�r den Tooltip f�r das Report-Toolbar-Button "Keine" */
	public static String viewersToolbarSelectNoneHint="Keine Eintr�ge ausw�hlen";
	/** Bezeichner f�r das Report-Toolbar-Button "Tabellen speichern" */
	public static String viewersToolbarSaveTables="Tabellen speichern";
	/** Bezeichner f�r den Tooltip f�r das Report-Toolbar-Button "Tabellen speichern" */
	public static String viewersToolbarSaveTablesHint="Speichert nur die ausgew�hlten Tabellen in einer gemeinsamen Arbeitsmappe.";

	/** html-Bezeichner f�r die Hinweismeldung "Bitte Eintrag in Baumstruktur ausw�hlen." */
	public static String viewersSpecialTextCategory="<p>Bitte w�hlen Sie in der Baumstruktur eine <b>Kategorie</b> aus, um die entsprechenden Informationen angezeigt zu bekommen.</p>";
	/** html-Bezeichner f�r die Hinweismeldung "Bitte Unterpunkt in Baumstruktur ausw�hlen." */
	public static String viewersSpecialTextSubCategory="<p>Bitte w�hlen Sie in der Baumstruktur eine <b>Unterkategorie</b> aus, um die entsprechenden Informationen angezeigt zu bekommen.</p>";
	/** html-Bezeichner f�r die Hinweismeldung "Noch keine Daten." */
	public static String viewersSpecialTextNoData="<p>Momentan stehen noch keine Statistik-Daten zur Verf�gung.</p>\n<p>Dr�cken Sie die <b>F5-Taste</b> oder w�hlen Sie auf der Seite \"Modell-Editor\" die Funktion <b>\"Simulation starten\"</b>.</p>";
	/** html-Bezeichner f�r die Link "Simulation starten" */
	public static String viewersSpecialTextStartSimulation="Simulation jetzt starten";
	/** html-Bezeichner f�r die Link "Statistik laden" */
	public static String viewersSpecialTextLoadData="Statistikdaten von fr�herem Simulationslauf laden";

	/** Bezeichner f�r Diagramme "Anzahl" */
	public static String viewersChartNumber="Anzahl";
	/** Bezeichner f�r Diagramme "Anteil" */
	public static String viewersChartPart="Anteil";
	/** Bezeichner f�r Diagramme "Zeit" */
	public static String viewersChartTime="Zeit";
	/** Bezeichner f�r Diagramme "in Sekunden" */
	public static String viewersChartInSeconds="in Sekunden";
	/** Bezeichner f�r Diagramme "in Minuten" */
	public static String viewersChartInMinutes="in Minuten";
	/** Bezeichner f�r Diagramme "in Stunden" */
	public static String viewersChartInHours="in Stunden";
	/** Bezeichner f�r Diagramme "Sekunden" */
	public static String viewersTextSeconds="Sekunden";

	/** Bezeichner f�r Titel f�r Schaltfl�che zum Erkl�rung einblenden */
	public static String descriptionShow="Erkl�rung einblenden";
	/** Bezeichner f�r Tooltip f�r Schaltfl�che zum Erkl�rung einblenden */
	public static String descriptionShowHint="Zeigt zus�tzliche Erkl�rungen zu dieser Statistikseiten an.";
	/** Bezeichner f�r Titel f�r Schaltfl�che zum Erkl�rung ausblenden */
	public static String descriptionHide="Erkl�rung ausblenden";
	/** Bezeichner f�r Tooltip f�r Schaltfl�che zum Erkl�rung ausblenden */
	public static String descriptionHideHint="Blendet die Erkl�rungen wieder aus.";
	/** Bezeichner f�r Titel der Schaltfl�che zur Anzeige der Vorg�ngerergebnisse */
	public static String previousAdd="Vorherige";
	/** Bezeichner f�r Tooltip der Schaltfl�che zur Anzeige der Vorg�ngerergebnisse */
	public static String previousAddHint="Mit vorherigen Statistikergebnissen vergleichen";
	/** Bezeichner f�r Titel der Schaltfl�che zum Ausblenden der Vorg�ngerergebnisse */
	public static String previousRemove="Ausblenden";
	/** Bezeichner f�r Tooltip der Schaltfl�che zum Ausblenden der Vorg�ngerergebnisse */
	public static String previousRemoveHint="Vergleich mit vorherigen Ergebnissen wieder ausblenden";

	/** Titel der Fehlermeldung "Keine Internet-Verbindung m�glich" */
	public static String internetErrorTitle="Keine Internet-Verbindung m�glich";
	/** Inhalt der Fehlermeldung "Keine Internet-Verbindung m�glich" */
	public static String internetErrorInfo="Die angegebene Adresse\n%s\nkonnte nicht aufgerufen werden.";
	/** Titel der Fehlermeldung "Kein E-Mail-Programm festgelegt" */
	public static String mailErrorTitle="Kein E-Mail-Programm festgelegt";
	/** Inhalt der Fehlermeldung "Kein E-Mail-Programm festgelegt" */
	public static String mailErrorInfo="Der angegeben E-Mail-Link\n%s\nkonnte nicht aufgerufen werden.";

	/** Bezeichner f�r Dateiformat txt (im Dateiauswahldialog) */
	public static String fileTypeTXT="Textdateien";
	/** Bezeichner f�r Dateiformat rtf (im Dateiauswahldialog) */
	public static String fileTypeRTF="Richtextdateien";
	/** Bezeichner f�r Dateiformat html (im Dateiauswahldialog) */
	public static String fileTypeHTML="html-Dateien";
	/** Bezeichner f�r Dateiformat html(+js) (im Dateiauswahldialog) */
	public static String fileTypeHTMLJS="html-App-Dateien";
	/** Bezeichner f�r Dateiformat docx (im Dateiauswahldialog) */
	public static String fileTypeDOCX="Word-Texte";
	/** Bezeichner f�r Dateiformat odt (im Dateiauswahldialog) */
	public static String fileTypeODT="OpenOffice/LibreOffice-Texte";
	/** Bezeichner f�r Dateiformat pdf (im Dateiauswahldialog) */
	public static String fileTypePDF="Portable Document Dateien";
	/** Bezeichner f�r Dateiformat md (im Dateiauswahldialog) */
	public static String fileTypeMD="Markdown-Dateien";
	/** Bezeichner f�r Dateiformat jpeg (im Dateiauswahldialog) */
	public static String fileTypeJPG="jpeg-Dateien";
	/** Bezeichner f�r Dateiformat gif (im Dateiauswahldialog) */
	public static String fileTypeGIF="gif-Dateien";
	/** Bezeichner f�r Dateiformat png (im Dateiauswahldialog) */
	public static String fileTypePNG="png-Dateien";
	/** Bezeichner f�r Dateiformat bmp(im Dateiauswahldialog) */
	public static String fileTypeBMP="bmp-Dateien";
	/** Bezeichner f�r Dateiformat docx(+Bild) (im Dateiauswahldialog) */
	public static String fileTypeWordWithImage="Word-Text mit eingebettetem Bild";

	/** Der hier eingetragene Programmname wird in html-Reports verwendet. */
	public static String program_name="";

	/**
	 * Welche Optionen sollen auf Text- und Tabellen-Seiten zum direkten Anzeigen der Daten in externen Anwendungen angezeigt werden?
	 * @see StatisticsBasePanel.ViewerPrograms
	 */
	public static final Set<ViewerPrograms> viewerPrograms=new HashSet<ViewerPrograms>(Arrays.asList(ViewerPrograms.WORD,ViewerPrograms.EXCEL));

	private Runnable helpRunnable, startSimulation, loadStatistics;

	/**
	 * Werden mehrere Statistikdokumente gleichzeitig angezeigt, so kann �ber dieses
	 * Array jeweils ein zus�tzlicher Titel f�r die einzelnen Spalten festgelegt werden.
	 */
	protected String[] additionalTitle;

	private final JSplitPane splitPane;

	private final JPanel[] titlePanel;
	private final JLabel[] titleLabel;
	private final JPanel[] dataPanel;
	private final JLabel[] dataLabel;
	private final Component[] dataContent;
	private final StatisticViewer[] dataViewer;
	private final JToolBar[] dataToolBar;
	private final ArrayList<JButton>[] userToolbarButtons;
	private final JButton[] zoom, copy, print, save, settings, selectAll, selectNone, saveTables;
	private final JPopupMenu[] settingsMenu;
	private final JMenuItem[] settingsItem;

	private final boolean storeLastRoot;
	private StatisticNode lastRoot;
	private StatisticNode currentRoot;

	private final StatisticTree tree;
	private DefaultMutableTreeNode reportNode;
	private final JButton report;
	private final JButton tools;

	private final List<ActionListener> fileDropListeners;

	private final DefaultMutableTreeNode noDataSelected=new DefaultMutableTreeNode("("+typeNoData+")");

	/**
	 * Gibt an, wie viele Statistikdateien nebeneinander dargestellt werden.
	 */
	protected final int numberOfViewers;

	/**
	 * Konstruktor der Klasse <code>StatisticsBasePanel</code>
	 * @param numberOfViewers	Anzahl der nebeneinander anzuzeigenden Ergebnisse
	 * @param title	Titel, der �ber der Baumstruktur angezeigt wird
	 * @param icon	Icon, das neben dem Titel �ber der Baumstruktur angezeigt wird (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param commandLineCommand	Kommandozeilenbefehl, �ber den einzelne Statistikergebnisse abgerufen werden k�nnen (zur Anzeige eines Kontextmen�s, welche den jeweiligen Befehl benennt; wird hier <code>null</code> �bergeben, so erh�lt die Baumansicht kein Kontextmen�)
	 * @param storeLastRoot	Soll ein Vergleich zu den jeweils vorherigen Ergebnissen angeboten werden?
	 */
	@SuppressWarnings("unchecked")
	protected StatisticsBasePanel(int numberOfViewers, final String title, final URL icon, final String commandLineCommand, final boolean storeLastRoot) {
		super(new BorderLayout());
		fileDropListeners=new ArrayList<>();
		if (numberOfViewers<1) numberOfViewers=1;
		this.numberOfViewers=numberOfViewers;
		this.storeLastRoot=storeLastRoot;

		/* Splitter initialisieren */

		add(splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT),BorderLayout.CENTER);
		splitPane.setContinuousLayout(true);
		JPanel treePanel;
		splitPane.setLeftComponent(treePanel=new JPanel(new BorderLayout()));
		JPanel p=new JPanel(new GridLayout(0,numberOfViewers));
		dataPanel=new JPanel[numberOfViewers];
		for (int i=0;i<dataPanel.length;i++) p.add(dataPanel[i]=new JPanel(new BorderLayout()),i);
		splitPane.setRightComponent(p);

		Object[] obj;

		/* Baumstruktur initialisieren */

		obj=addTopInfoArea(treePanel,title,icon);
		JToolBar treeToolBar=(JToolBar)obj[3];
		JScrollPane sp=new JScrollPane(tree=new StatisticTree(commandLineCommand,null){
			private static final long serialVersionUID = 5013035517806204341L;
			@Override
			protected void nodeSelected(StatisticNode node, DefaultMutableTreeNode treeNode) {updateDataPanel(node,treeNode);}
		});
		tree.setBackground(new Color(0xFF,0xFF,0xF8));
		sp.setBorder(BorderFactory.createLineBorder(Color.GRAY,1));
		treePanel.add(sp,BorderLayout.CENTER);

		/* Icons �ber der Baumstruktur */

		treeToolBar.add(report=new JButton(viewersReport));
		report.addActionListener(new ButtonListener());
		report.setToolTipText(viewersReportHint);
		report.setIcon(SimToolsImages.STATISTICS_REPORT.getIcon());

		treeToolBar.add(Box.createHorizontalGlue());

		treeToolBar.add(tools=new JButton());
		tools.addActionListener(new ButtonListener());
		tools.setToolTipText(viewersToolsHint);
		tools.setIcon(SimToolsImages.STATISTICS_LISTTOOLS.getIcon());

		/* Datenbereich initialisieren */

		titlePanel=new JPanel[dataPanel.length];
		titleLabel=new JLabel[dataPanel.length];
		dataLabel=new JLabel[dataPanel.length];
		dataToolBar=new JToolBar[dataPanel.length];
		userToolbarButtons=new ArrayList[dataPanel.length];
		for (int i=0;i<userToolbarButtons.length;i++) userToolbarButtons[i]=new ArrayList<>();
		dataContent=new Component[dataPanel.length];
		dataViewer=new StatisticViewer[dataPanel.length];
		for (int i=0;i<dataPanel.length;i++) {
			obj=addTopInfoArea(dataPanel[i],"",null);
			titlePanel[i]=(JPanel)obj[0];
			titleLabel[i]=(JLabel)obj[1];
			dataLabel[i]=(JLabel)obj[2];
			dataToolBar[i]=(JToolBar)obj[3];
		}

		/* Icons �ber den Datenbereichen */

		zoom=new JButton[dataToolBar.length];
		copy=new JButton[dataToolBar.length];
		print=new JButton[dataToolBar.length];
		save=new JButton[dataToolBar.length];
		settings=new JButton[dataToolBar.length];
		selectAll=new JButton[dataToolBar.length];
		selectNone=new JButton[dataToolBar.length];
		saveTables=new JButton[dataToolBar.length];
		settingsMenu=new JPopupMenu[dataToolBar.length];
		settingsItem=new JMenuItem[dataToolBar.length];
		for (int i=0;i<dataToolBar.length;i++) {
			zoom[i]=new JButton(viewersToolbarZoom);
			zoom[i].setToolTipText(viewersToolbarZoomHint);
			zoom[i].addActionListener(new ButtonListener());
			zoom[i].setIcon(SimToolsImages.ZOOM.getIcon());
			dataToolBar[i].add(zoom[i]);
			copy[i]=new JButton(viewersToolbarCopy);
			copy[i].setToolTipText(viewersToolbarCopyHint);
			copy[i].addActionListener(new ButtonListener());
			copy[i].setIcon(SimToolsImages.COPY.getIcon());
			dataToolBar[i].add(copy[i]);
			print[i]=new JButton(viewersToolbarPrint);
			print[i].setToolTipText(viewersToolbarPrintHint);
			print[i].addActionListener(new ButtonListener());
			print[i].setIcon(SimToolsImages.PRINT.getIcon());
			dataToolBar[i].add(print[i]);
			save[i]=new JButton(viewersToolbarSave);
			save[i].setToolTipText(viewersToolbarSaveHint);
			save[i].addActionListener(new ButtonListener());
			save[i].setIcon(SimToolsImages.SAVE.getIcon());
			dataToolBar[i].add(save[i]);
			settings[i]=new JButton(viewersToolbarSettings);
			settings[i].setToolTipText(viewersToolbarSettingsHint);
			settings[i].addActionListener(new ButtonListener());
			settings[i].setIcon(SimToolsImages.SETUP.getIcon());
			dataToolBar[i].add(settings[i]);
			selectAll[i]=new JButton(viewersToolbarSelectAll);
			selectAll[i].setToolTipText(viewersToolbarSelectAllHint);
			selectAll[i].addActionListener(new ButtonListener());
			selectAll[i].setIcon(SimToolsImages.ADD.getIcon());
			dataToolBar[i].add(selectAll[i]);
			selectNone[i]=new JButton(viewersToolbarSelectNone);
			selectNone[i].setToolTipText(viewersToolbarSelectNoneHint);
			selectNone[i].addActionListener(new ButtonListener());
			selectNone[i].setIcon(SimToolsImages.DELETE.getIcon());
			dataToolBar[i].add(selectNone[i]);
			saveTables[i]=new JButton(viewersToolbarSaveTables);
			saveTables[i].setToolTipText(viewersToolbarSaveTablesHint);
			saveTables[i].addActionListener(new ButtonListener());
			saveTables[i].setIcon(SimToolsImages.SAVE_TABLE.getIcon());
			dataToolBar[i].add(saveTables[i]);
			settingsMenu[i]=new JPopupMenu();
			settingsMenu[i].add(settingsItem[i]=new JMenuItem());
			settingsItem[i].addActionListener(new ButtonListener());
		}

		/* Copy-Hotkey erkennen */

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK),"CopyViewer");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.CTRL_DOWN_MASK),"CopyViewer");
		getActionMap().put("CopyViewer",new AbstractAction() {
			private static final long serialVersionUID = 6834309003536671412L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (dataViewer==null  || dataViewer.length!=1 || dataViewer[0]==null) return;
				if (!(dataViewer[0] instanceof StatisticViewer)) return;
				final StatisticViewer viewer=dataViewer[0];
				if (!viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_COPY)) return;
				viewer.copyToClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
			}
		});

		/* Drag&Drop  */
		registerComponentForFileDrop(this);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsBasePanel</code>
	 * @param title	Titel, der �ber der Baumstruktur angezeigt wird
	 * @param icon	Icon, das neben dem Titel �ber der Baumstruktur angezeigt wird (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param commandLineCommand	Kommandozeilenbefehl, �ber den einzelne Statistikergebnisse abgerufen werden k�nnen (zur Anzeige eines Kontextmen�s, welche den jeweiligen Befehl benennt; wird hier <code>null</code> �bergeben, so erh�lt die Baumansicht kein Kontextmen�)
	 * @param storeLastRoot	Soll ein Vergleich zu den jeweils vorherigen Ergebnissen angeboten werden?
	 */
	protected StatisticsBasePanel(final String title, final URL icon, final String commandLineCommand, final boolean storeLastRoot) {
		this(1,title,icon,commandLineCommand,storeLastRoot);
	}

	/**
	 * Setzt Methoden, die aufgerufen werden, wenn auf der "Noch keine Daten"-Seite die verschiedenen L�sungsvorschl�ge angeklickt werden.
	 * @param startSimulation	Callback, das ausgel�st wird, wenn der Nutzer auf der "Noch keine Daten"-Seite auf "Simulation jetzt starten" klickt. (Wird hier <code>null</code> �bergeben, so wird diese Option nicht angezeigt.)
	 * @param loadStatistics	Callback, das ausgel�st wird, wenn der Nutzer auf der "Noch keine Daten"-Seite auf "Statistikdaten laden" klickt. (Wird hier <code>null</code> �bergeben, so wird diese Option nicht angezeigt.)
	 * @param helpRunnable Runnable, das aufgerufen wird, wenn die Hilfe-Schaltfl�che angeklickt wird. (Wenn <code>null</code> �bergeben wird, erscheint keine Hilfe-Schaltfl�che.)
	 */
	protected final void setCallBacks(final Runnable startSimulation, final Runnable loadStatistics, final Runnable helpRunnable) {
		this.startSimulation=startSimulation;
		this.loadStatistics=loadStatistics;
		this.helpRunnable=helpRunnable;
	}

	private Object[] addTopInfoArea(final JPanel parent, final String title, final URL icon) {
		JPanel topPanel;
		parent.add(topPanel=new JPanel(new BorderLayout()),BorderLayout.NORTH);

		/* Infotext oben */
		JPanel infoPanel;
		topPanel.add(infoPanel=new JPanel(),BorderLayout.NORTH);
		infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.PAGE_AXIS));
		infoPanel.setBackground(Color.GRAY);
		infoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		JPanel titlePanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		infoPanel.add(titlePanel);
		titlePanel.setOpaque(false);
		JLabel titleLabel;
		titlePanel.add(titleLabel=new JLabel(""));
		Font font=titleLabel.getFont();
		titleLabel.setFont(new java.awt.Font(font.getFontName(),java.awt.Font.PLAIN,font.getSize()+1));
		titleLabel.setForeground(Color.WHITE);
		titlePanel.setVisible(false);

		JPanel infoSubPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		infoPanel.add(infoSubPanel);
		infoSubPanel.setOpaque(false);
		JLabel infoLabel;
		infoSubPanel.add(infoLabel=new JLabel(title));
		if (icon!=null) infoLabel.setIcon(new ImageIcon(icon));
		font=infoLabel.getFont();
		infoLabel.setFont(new java.awt.Font(font.getFontName(),java.awt.Font.PLAIN,font.getSize()+3));
		infoLabel.setForeground(Color.WHITE);

		/* Toolbar oben */
		JToolBar buttonPanel;
		topPanel.add(buttonPanel=new JToolBar(),BorderLayout.CENTER);
		buttonPanel.setFloatable(false);

		return new Object[]{titlePanel,titleLabel,infoLabel,buttonPanel};
	}

	/**
	 * W�hlt das vorgabem��ige Element in der Baumstruktur aus (wenn diese neu geladen wurde und nicht das zuletzt verwendete Element erneut ausgew�hlt werden kann)
	 * @param tree	Baumstruktur
	 * @param root	Struktur aus <code>StatisticNode</code>-Elementen, die den Inhalt des Baum repr�sentieren
	 */
	protected void selectDefaultTreeNode(final JTree tree, final StatisticNode root) {
		tree.setSelectionRow(0);
	}

	/**
	 * Liefert die aktuelle Gr��e zum Speichern von Bildern
	 * @return	Gr��e zum Speichern von Bildern
	 */
	protected abstract int getImageSize();

	/**
	 * Stellt die Gr��e zum Speichern von Bildern ein.
	 * @param newSize	Neue Gr��e zum Speichern von Bildern
	 */
	protected abstract void setImageSize(final int newSize);

	private void setImageSizeCallbacks(final StatisticNode node) {
		for (StatisticViewer viewer: node.viewer) {
			viewer.setRequestImageSize(()->getImageSize());
			viewer.setUpdateImageSize(size->setImageSize(size));
		}

		for (int i=0;i<node.getChildCount();i++) setImageSizeCallbacks(node.getChild(i));
	}

	private static boolean selectPath(final JTree tree, final List<String> path) {
		Object obj;
		DefaultMutableTreeNode node;

		final List<Object> selectionPath=new ArrayList<>();

		obj=tree.getModel().getRoot();
		if (!(obj instanceof DefaultMutableTreeNode)) return false;
		node=(DefaultMutableTreeNode)obj;
		selectionPath.add(node);

		int index=1;

		while (index<path.size()) {
			boolean ok=false;
			for (int i=0;i<node.getChildCount();i++) {
				TreeNode treeNode=node.getChildAt(i);
				if (!(treeNode instanceof DefaultMutableTreeNode)) return false;
				final DefaultMutableTreeNode sub=(DefaultMutableTreeNode)treeNode;
				if (sub.toString().equals(path.get(index))) {
					node=sub;
					selectionPath.add(node);
					index++;
					ok=true;
					break;
				}
			}
			if (!ok) break;
		}

		if (selectionPath.size()>1) {
			tree.setSelectionPath(new TreePath(selectionPath.toArray(new Object[0])));
			return true;
		} else {
			return false;
		}
	}

	private static boolean selectDefault(final JTree tree) {
		Object obj;

		obj=tree.getModel().getRoot();
		if (!(obj instanceof DefaultMutableTreeNode)) return false;
		final DefaultMutableTreeNode root=(DefaultMutableTreeNode)obj;

		for (int i=0;i<root.getChildCount();i++) {
			final TreeNode node=root.getChildAt(i);
			if (node instanceof DefaultMutableTreeNode) {
				obj=((DefaultMutableTreeNode)node).getUserObject();
				if (obj instanceof StatisticNode) {
					final StatisticViewer[] viewer=((StatisticNode)obj).viewer;
					if (viewer!=null && viewer.length>0 && viewer[0] instanceof StatisticViewerText) {
						tree.setSelectionPath(new TreePath(new Object[] {root,node}));
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Setzt eine Baumstruktur aus <code>StatisticNode</code>-Elementen als Inhalt der Baumstruktur
	 * @param root Basis der <code>StatisticNode</code>-Elemente, die die Baumstruktur enthalten
	 * @param reportTitle	Optional Titel f�r den html-Web-App-Export (kann auch <code>null</code> sein)
	 */
	protected final void setData(final StatisticNode root, final String reportTitle) {
		if (storeLastRoot) {
			lastRoot=currentRoot;
			currentRoot=root;
		}

		/* Bisher selektiertes Element speichern */
		final List<String> sel=new ArrayList<>();
		final TreePath[] paths=tree.getSelectionPaths();
		if (paths!=null && paths.length==1) {
			final Object[] path=paths[0].getPath();
			if (path!=null) for (Object obj: path) {
				if (!(obj instanceof DefaultMutableTreeNode )) break;
				final DefaultMutableTreeNode node=(DefaultMutableTreeNode)obj;
				sel.add(node.toString());
			}
		}

		/* �berall Callbacks f�r Bildgr��e laden/speichern setzen */
		if (root!=null) setImageSizeCallbacks(root);

		/* Elemente laden */
		DefaultMutableTreeNode rootNode=new DefaultMutableTreeNode();
		if (root!=null) addToTree(root,rootNode);
		reportNode=null;
		if (root!=null && root.getChildCount()>0) {
			final List<StatisticViewer> list=new ArrayList<>();
			for (int i=0;i<dataPanel.length;i++) list.add(new StatisticViewerReport(root,reportTitle,i,helpRunnable){
				@Override
				protected String getSelectSettings() {return getReportSelectSettings();}
				@Override
				protected void setSelectSettings(String settings) {setReportSelectSettings(settings);}
				@Override
				protected boolean loadImagesInline() {return getImagesInlineSetting();}
				@Override
				protected void saveImagesInline(final boolean imagesInline) {setImagesInlineSetting(imagesInline);}
			});
			rootNode.add(reportNode=new DefaultMutableTreeNode(new StatisticNode(viewersReport,list)));
		} else {
			rootNode.add(noDataSelected);
		}
		((DefaultTreeModel)(tree.getModel())).setRoot(rootNode);

		/* Elemente ein- und ausklappen */
		int row=0; while (row<tree.getRowCount()) {
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getPathForRow(row).getLastPathComponent());
			if (!node.isLeaf() && !((StatisticNode)(node.getUserObject())).collapseChildren) tree.expandRow(row);
			row++;
		}

		/* Selektion wiederherstellen */
		boolean done=false;
		if (!sel.isEmpty()) done=selectPath(tree,sel);
		if (!done) done=selectDefault(tree);
		if (!done) selectDefaultTreeNode(tree,root);

		/* Report-Button aktivieren oder deaktivieren */
		report.setVisible(reportNode!=null);

		/* Breite der Baumstruktur anpassen */
		updateTreeWidth();
	}

	private final void addToTree(StatisticNode sNode, DefaultMutableTreeNode tNode) {
		for (int i=0;i<sNode.getChildCount();i++) {
			StatisticNode sChild=sNode.getChild(i);
			DefaultMutableTreeNode tChild=new DefaultMutableTreeNode(sChild);
			tNode.add(tChild);
			if (sChild.getChildCount()>0) addToTree(sChild,tChild);
		}
	}

	private final void updateTreeWidth() {
		/* Breite der linken Spalte anpassen */
		Dimension d=tree.getPreferredSize();
		d.width=Math.min(d.width,Math.max(250,getBounds().width/5));

		/* Minimale Breite der Baumstruktur */
		d=tree.getMinimumSize();
		d.width=Math.max(d.width,250);
		tree.setMinimumSize(d);

		if (d.width!=splitPane.getDividerLocation()) splitPane.setDividerLocation(d.width);
	}

	private StatisticViewer[] getLastViewer(final StatisticNode currentNode) {
		if (lastRoot==null) return null;

		final List<Integer> path=currentNode.getPath();
		if (path==null) return null;

		final StatisticNode lastNode=lastRoot.getChildByPath(path);
		if (lastNode==null) return null;

		return lastNode.viewer;
	}

	private void updateDataPanel(StatisticNode node, DefaultMutableTreeNode treeNode) {
		if (node==null || node.viewer.length==0) {
			report.setVisible(reportNode!=null);
		} else {
			StatisticViewer viewer=node.viewer[0];
			if (viewer.getType()==StatisticViewer.ViewerType.TYPE_REPORT) {
				report.setVisible(false);
			} else {
				report.setVisible(reportNode!=null);
			}
		}

		StatisticViewer[] viewer=new StatisticViewer[dataPanel.length];
		StatisticViewer[] lastViewer=null;
		String info;
		if (node==null) {
			if (reportNode==null) {
				for (int i=0;i<viewer.length;i++) viewer[i]=new StatisticViewerSpecialText(SpecialMode.VIEWER_NODATA,startSimulation,loadStatistics);
			} else {
				for (int i=0;i<viewer.length;i++) viewer[i]=new StatisticViewerSpecialText(SpecialMode.VIEWER_CATEGORY);
			}
			info=viewersInformation;
		} else {
			if (node.viewer.length==0) {
				for (int i=0;i<viewer.length;i++) viewer[i]=new StatisticViewerSpecialText(SpecialMode.VIEWER_SUBCATEGORY);
				info=viewersInformation;
			} else {
				viewer=node.viewer;
				lastViewer=getLastViewer(node);
				TreePath path=tree.getSelectionPath();
				String s="";
				while (path!=null) {
					DefaultMutableTreeNode n=(DefaultMutableTreeNode)path.getLastPathComponent();
					if (n.getUserObject()==null) break;
					if (!s.isEmpty()) s=" - "+s;
					s=((StatisticNode)(n.getUserObject())).name+s;
					path=path.getParentPath();
				}
				info=s;
			}
		}

		URL icon=null;
		if (treeNode!=null) {
			icon=new StatisticTreeCellRenderer().getIconURL(treeNode);
		}
		updateViewer(viewer,lastViewer,info,icon);
	}

	/**
	 * Diese Funktion wird aufgerufen, wenn die Einstellungen, welche Report-Eintr�ge ausgew�hlt sein sollen, abgefragt werden sollen.
	 * @return	Einstellungen, welche Report-Eintr�ge selektiert sein sollen
	 */
	protected String getReportSelectSettings() {return "";}

	/**
	 * Diese Funktion wird aufgerufen, wenn die Einstellungen, welche Report-Eintr�ge ausgew�hlt sind, gespeichert werden sollen.
	 * @param settings	Neue Einstellungen, welche Report-Eintr�ge selektiert sind
	 */
	protected void setReportSelectSettings(String settings) {}

	/**
	 * L�dt die Einstellung, ob Bilder bei HTML-Reports inline ausgegeben werden sollen, aus dem Setup (in einer abgeleiteten Klasse)
	 * @return	Gibt an, ob Bilder bei bei HTML-Reports inline ausgegeben werden sollen.
	 */
	protected boolean getImagesInlineSetting() {
		return true;
	}

	/**
	 * Speichert die Einstellung, ob Bilder bei HTML-Reports inline ausgegeben werden sollen, im Setup (in einer abgeleiteten Klasse)
	 * @param imagesInline	Gibt an, ob Bilder bei HTML-Reports inline ausgegeben werden sollen.
	 */
	protected void setImagesInlineSetting(final boolean imagesInline) {}

	/**
	 * W�hlt den "Zusammenfassung erstellen"-Knoten aus
	 */
	public final void selectReportNode() {
		if (reportNode!=null) tree.selectNode(reportNode);
	}

	/**
	 * W�hlt einen bestimmten Knoten aus
	 * @param tester	Testfunktion, die angibt, ob der Knoten der gesuchte ist
	 * @return	Gibt <code>true</code> zur�ck, wenn ein Knoten gew�hlt wurde
	 */
	public final boolean selectNode(final Predicate<StatisticNode> tester) {
		return selectNode(tree.getModel().getRoot(),tester);
	}

	/**
	 * W�hlt einen bestimmten Knoten aus
	 * @param node	Statistik-Element, das in dem Baumknoten hinterlegt sein soll
	 * @return	Gibt <code>true</code> zur�ck, wenn ein Knoten gew�hlt wurde
	 */
	public final boolean selectNode(final StatisticNode node) {
		return selectNode(test->node==test);
	}

	private final boolean selectNode(final Object node, final Predicate<StatisticNode> tester) {
		if (!(node instanceof DefaultMutableTreeNode)) return false;
		final DefaultMutableTreeNode node2=(DefaultMutableTreeNode)node;
		final Object userObject=node2.getUserObject();

		if (userObject instanceof StatisticNode) {
			if (tester.test((StatisticNode)userObject)) {
				tree.selectNode(node2);
				return true;
			}
		}

		for (int i=0;i<node2.getChildCount();i++) {
			if (selectNode(node2.getChildAt(i),tester)) return true;
		}

		return false;
	}

	/**
	 * F�gt einen Listener hinzu, der benachrichtigt wird, wenn eine Datei auf der Komponente abgelegt wird
	 * @param fileDropListener	Zu benachrichtigender Listener (der Dateiname ist �ber die <code>getActionCommand()</code>-Methode des �bergebenen <code>ActionEvent</code>-Objekts abrufbar)
	 */
	public void addFileDropListener(final ActionListener fileDropListener) {
		if (fileDropListeners.indexOf(fileDropListener)<0) fileDropListeners.add(fileDropListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer auf dieser Komponente abgelegten Datei zu benachrichtigenden Listener
	 * @param fileDropListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeFileDropListener(final ActionListener fileDropListener) {
		return fileDropListeners.remove(fileDropListener);
	}

	private boolean dropFile(final FileDropperData data) {
		final ActionEvent event=FileDropperData.getActionEvent(data);
		for (ActionListener listener: fileDropListeners) listener.actionPerformed(event);
		return true;
	}

	private void registerComponentForFileDrop(final Component component) {
		new FileDropper(component,e->{
			final FileDropperData dropper=(FileDropperData)e.getSource();
			dropFile(dropper);
		});
	}

	private void registerComponentAndChildsForFileDrop(final Component component) {
		if (component==null) return;
		registerComponentForFileDrop(component);

		if (component instanceof Container) {
			final Container container=(Container)component;
			for (int i=0;i<container.getComponentCount();i++) {
				registerComponentAndChildsForFileDrop(container.getComponent(i));
			}
		}
	}

	private void addSubViewer(final StatisticViewer currentViewer, final StatisticViewer additionalViewer) {
		final Container viewerComponent=currentViewer.getViewer(false);
		final Container parent=viewerComponent.getParent();
		if (parent==null) return;
		parent.remove(viewerComponent);

		final JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setLeftComponent(viewerComponent);
		split.setRightComponent(additionalViewer.getViewer(true));
		split.setBorder(BorderFactory.createEmptyBorder());
		parent.add(split);
		split.setResizeWeight(0.66);
		split.setDividerLocation(0.5);
	}

	private void resetSubViewer(final StatisticViewer currentViewer) {
		final Container viewerComponent=currentViewer.getViewer(false).getParent();
		final Container parent=viewerComponent.getParent();
		if (parent==null) return;
		parent.remove(viewerComponent);

		parent.add(currentViewer.getViewer(false));
	}

	private final void updateViewer(final StatisticViewer[] viewer, final StatisticViewer[] lastViewer, final String title, final URL icon) {
		for (int i=0;i<dataPanel.length;i++) {
			Container container=null;
			StatisticViewer view=null;
			if (i<viewer.length) view=viewer[i]; else view=null;
			if (view==null) container=null; else container=view.getViewer(false);
			updateViewer(i,(additionalTitle==null || additionalTitle.length<=i)?null:additionalTitle[i],title,icon,container,view);

			/* Drag&Drop  */
			registerComponentAndChildsForFileDrop(container);

			zoom[i].setVisible(container!=null && viewer[i].getCanDo(CanDoAction.CAN_DO_UNZOOM));
			copy[i].setVisible(container!=null && viewer[i].getCanDo(CanDoAction.CAN_DO_COPY));
			print[i].setVisible(container!=null && viewer[i].getCanDo(CanDoAction.CAN_DO_PRINT));
			save[i].setVisible(container!=null && viewer[i].getCanDo(CanDoAction.CAN_DO_SAVE));

			for (JButton oldButton: userToolbarButtons[i]) dataToolBar[i].remove(oldButton);
			userToolbarButtons[i].clear();
			final JButton[] newButtons=viewer[i].getAdditionalButton();
			if (newButtons!=null) for (JButton newButton: newButtons) {
				dataToolBar[i].add(newButton);
				userToolbarButtons[i].add(newButton);
			}

			if (lastViewer!=null && lastViewer.length>i && lastViewer[i]!=null) {
				final JButton showLast=new JButton(previousAdd);
				showLast.setToolTipText(previousAddHint);
				showLast.setIcon(SimToolsImages.STATISTICS_COMPARE_LAST.getIcon());
				final int nr=i;
				showLast.addActionListener(e->{
					final JButton button=(JButton)e.getSource();
					if (button.getText().equals(previousAdd)) {
						button.setText(previousRemove);
						button.setToolTipText(previousRemoveHint);
						addSubViewer(viewer[nr],lastViewer[nr]);
					} else {
						button.setText(previousAdd);
						button.setToolTipText(previousAddHint);
						resetSubViewer(viewer[nr]);
					}
				});
				dataToolBar[i].add(showLast);
				userToolbarButtons[i].add(showLast);
			}

			if (container!=null && viewer[i].ownSettingsName()!=null) {
				settings[i].setVisible(true);
				settingsItem[i].setText(viewer[i].ownSettingsName());
				settingsItem[i].setIcon(viewer[i].ownSettingsIcon());
			} else {
				settings[i].setVisible(false);
			}

			selectAll[i].setVisible(container!=null && (viewer[i] instanceof StatisticViewerReport));
			selectNone[i].setVisible(container!=null && (viewer[i] instanceof StatisticViewerReport));
			saveTables[i].setVisible(container!=null && (viewer[i] instanceof StatisticViewerReport));
		}

		if (viewer[0]!=null) {
			if (viewer[0].getImageType()==StatisticViewer.ViewerImageType.IMAGE_TYPE_LINE) {
				JFreeChart[] charts=getCharts(viewer,StatisticViewerLineChart.class);
				if (charts!=null) adjustLineCharts(charts);
			}
			if (viewer[0].getImageType()==StatisticViewer.ViewerImageType.IMAGE_TYPE_BAR) {
				JFreeChart[] charts=getCharts(viewer,StatisticViewerBarChart.class);
				if (charts!=null) adjustBarCharts(charts);
			}
		}
	}

	private final JFreeChart[] getCharts(final StatisticViewer[] viewer, final Class<? extends StatisticViewer> chartClass) {
		JFreeChart[] chart=new JFreeChart[viewer.length];
		for (int i=0;i<viewer.length;i++) {
			if (!chartClass.isInstance(viewer[i])) return null;
			Container c=((StatisticViewerJFreeChart)viewer[i]).getViewer(false);
			if (!(c instanceof ChartPanel)) return null;
			chart[i]=((ChartPanel)c).getChart();
		}
		return chart;
	}

	private final void adjustLineCharts(JFreeChart[] chart) {
		for (int nr=0;nr<chart[0].getXYPlot().getRangeAxisCount();nr++) {
			Range r=chart[0].getXYPlot().getRangeAxis(nr).getRange();
			double min=r.getLowerBound();
			double max=r.getUpperBound();
			for (int i=1;i<chart.length;i++) {
				r=chart[i].getXYPlot().getRangeAxis(nr).getRange();
				min=Math.min(min,r.getLowerBound());
				max=Math.max(max,r.getUpperBound());
			}
			r=new Range(min,max);
			for (int i=0;i<chart.length;i++) chart[i].getXYPlot().getRangeAxis(nr).setRange(r);
		}
	}

	private final void adjustBarCharts(JFreeChart[] chart) {
		for (int nr=0;nr<chart[0].getCategoryPlot().getRangeAxisCount();nr++) {
			Range r=chart[0].getCategoryPlot().getRangeAxis(nr).getRange();
			double min=r.getLowerBound();
			double max=r.getUpperBound();
			for (int i=1;i<chart.length;i++) {
				r=chart[i].getCategoryPlot().getRangeAxis(nr).getRange();
				min=Math.min(min,r.getLowerBound());
				max=Math.max(max,r.getUpperBound());
			}
			r=new Range(min,max);
			for (int i=0;i<chart.length;i++) chart[i].getCategoryPlot().getRangeAxis(nr).setRange(r);
		}
	}

	private final void updateViewer(final int index, final String supTitle, final String title, final URL icon, final Component component, final StatisticViewer viewer) {
		if (index<0 || index>=dataLabel.length) return;

		dataLabel[index].setText(title);

		int delta=0;
		if (supTitle!=null && !supTitle.trim().isEmpty()) {
			titlePanel[index].setVisible(true);
			titleLabel[index].setText(supTitle);
			delta=titlePanel[index].getY();
		} else {
			titlePanel[index].setVisible(false);
			titleLabel[index].setText("");
		}

		String info=title;
		while (dataLabel[index].getY()-delta>dataLabel[index].getSize().height && info.length()>20) {
			info=info.substring(0,info.length()-1);
			dataLabel[index].setText(info+"...");
			dataLabel[index].doLayout();
		}

		dataLabel[index].setIcon((icon==null)?null:new ImageIcon(icon));

		if (dataContent[index]!=null) {
			Component c=dataContent[index];
			while (c!=null && c.getParent()!=dataPanel[index]) c=c.getParent();
			if (c!=null) dataPanel[index].remove(c);
		}
		dataContent[index]=component;
		dataViewer[index]=viewer;
		if (component!=null) dataPanel[index].add(component,BorderLayout.CENTER);

		/* Ist leider n�tig, damit der neue Viewer auch wirklich sofort aktiviert wird. */
		dataPanel[index].revalidate();
		dataPanel[index].repaint();
		Container c=dataPanel[index].getParent();
		while (c!=null) {c.revalidate(); c.repaint(); c=c.getParent();}
		if (component!=null) {
			component.setVisible(false);
			component.setVisible(true);
		}
	}

	private final Window getOwnerWindow() {
		Container c=getParent();
		while (c!=null) {
			if (c instanceof Window) return (Window)c;
			c=c.getParent();
		}
		return null;
	}

	private final void showToolsContextMenu(final JButton button) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		item=new JMenuItem(viewersToolsShowAll);
		popup.add(item);
		item.setIcon(SimToolsImages.PLUS.getIcon());
		item.addActionListener(e->{
			for (int i=0;i<tree.getRowCount();i++) tree.expandRow(i);
		});
		item=new JMenuItem(viewersToolsHideAll);
		popup.add(item);
		item.setIcon(SimToolsImages.MINUS.getIcon());
		item.addActionListener(e->{
			for (int i=0;i<tree.getRowCount();i++) tree.collapseRow(i);
		});

		popup.show(button,0,button.getHeight());
	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final Object sender=e.getSource();
			if (sender==report) selectReportNode();
			if (sender==tools) showToolsContextMenu(tools);
			for (int i=0;i<dataPanel.length;i++) {
				if (dataViewer[i]==null) continue;
				if (sender==zoom[i]) dataViewer[i].unZoom();
				if (sender==copy[i]) dataViewer[i].copyToClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
				if (sender==print[i]) dataViewer[i].print();
				if (sender==save[i]) dataViewer[i].save(getOwnerWindow());
				if (sender==settings[i]) settingsMenu[i].show(settings[i],0,settings[i].getHeight());
				if (sender==settingsItem[i]) dataViewer[i].ownSettings(StatisticsBasePanel.this);
				if (sender==selectAll[i] && dataViewer[i] instanceof StatisticViewerReport) ((StatisticViewerReport)dataViewer[i]).selectAll();
				if (sender==selectNone[i] && dataViewer[i] instanceof StatisticViewerReport) ((StatisticViewerReport)dataViewer[i]).selectNone();
				if (sender==saveTables[i] && dataViewer[i] instanceof StatisticViewerReport) ((StatisticViewerReport)dataViewer[i]).saveTablesToWorkbook(StatisticsBasePanel.this);
			}
		}
	}

	private StatisticViewerReport getStatisticViewerReport() {
		if (reportNode==null) return null;
		final StatisticNode statisticNode=(StatisticNode)reportNode.getUserObject();
		if (statisticNode==null || statisticNode.viewer==null || statisticNode.viewer.length==0) return null;
		if (!(statisticNode.viewer[0] instanceof StatisticViewerReport)) return null;
		return (StatisticViewerReport)statisticNode.viewer[0];
	}

	@Override
	public boolean runReportGeneratorHTML(File output, boolean inline, boolean exportAllItems) {
		final StatisticViewerReport report=getStatisticViewerReport();
		if (report==null) return false;
		return report.save(null,output,inline?FileFormat.FORMAT_HTML_INLINE:FileFormat.FORMAT_HTML,exportAllItems);
	}

	@Override
	public boolean runReportGeneratorDOCX(File output, boolean exportAllItems) {
		final StatisticViewerReport report=getStatisticViewerReport();
		if (report==null) return false;
		return report.save(null,output,FileFormat.FORMAT_DOCX,exportAllItems);
	}

	@Override
	public boolean runReportGeneratorPDF(File output, boolean exportAllItems) {
		final StatisticViewerReport report=getStatisticViewerReport();
		if (report==null) return false;
		return report.save(null,output,FileFormat.FORMAT_PDF,exportAllItems);
	}

	private void getViewersAndNames(String parentName, DefaultMutableTreeNode parent, List<StatisticViewer> viewers, List<String> types, List<String> names) {
		if (parent==null) return;
		int count=parent.getChildCount();
		String s=parentName; if (!s.isEmpty()) s+=" - ";
		for (int i=0;i<count;i++) {
			if (!(parent.getChildAt(i) instanceof DefaultMutableTreeNode)) continue;
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)(parent.getChildAt(i));
			if (!(node.getUserObject() instanceof StatisticNode)) continue;
			StatisticNode stat=(StatisticNode)(node.getUserObject());
			if (stat.viewer.length>0) {
				switch (stat.viewer[0].getType()) {
				case TYPE_TEXT: viewers.add(stat.viewer[0]); types.add(typeText); names.add(s+stat.name); break;
				case TYPE_TABLE: viewers.add(stat.viewer[0]); types.add(typeTable); names.add(s+stat.name); break;
				case TYPE_IMAGE: viewers.add(stat.viewer[0]); types.add(typeImage); names.add(s+stat.name); break;
				case TYPE_SPECIAL: viewers.add(stat.viewer[0]); types.add(typeText); names.add(s+stat.name); break;
				case TYPE_REPORT: /* Report-Node nicht zu Report-Liste hinzuf�gen. */ break;
				}
			}

			getViewersAndNames(s+stat.name,node,viewers,types,names);
		}
	}

	@Override
	public boolean getReportList(final  File output) {
		if (!(tree.getModel().getRoot() instanceof DefaultMutableTreeNode)) return false;
		final DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getModel().getRoot());

		final List<StatisticViewer> viewers=new ArrayList<>();
		final List<String> types=new ArrayList<>();
		final List<String> names=new ArrayList<>();
		getViewersAndNames("",node,viewers,types,names);

		final StringBuilder reportNames=new StringBuilder();
		for (int i=0;i<names.size();i++) reportNames.append(names.get(i)+" ("+types.get(i)+")\n");
		return Table.saveTextToFile(reportNames.toString(),output);
	}

	@Override
	public boolean getReportListEntry(File output, String entry) {
		if (!(tree.getModel().getRoot() instanceof DefaultMutableTreeNode)) return false;
		DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getModel().getRoot());

		List<StatisticViewer> viewers=new ArrayList<>();
		List<String> types=new ArrayList<>();
		List<String> names=new ArrayList<>();
		getViewersAndNames("",node,viewers,types,names);

		int index=-1;
		for (int i=0;i<names.size();i++) {
			String s=names.get(i)+" ("+types.get(i)+")";
			if (s.equalsIgnoreCase(entry)) {index=i; break;}
		}
		if (index<0) return false;

		return viewers.get(index).save(this,output);
	}

	/**
	 * Wenn im Kontextmen� der Baumstruktur Befehle f�r die Kommandozeile angeboten werden
	 * sollen, �ber die die jeweilige Information �ber die Kommandozeile abgerufen werden
	 * kann, so muss hier ein Beispieldateiname f�r die zu verwendende Statistikdatei
	 * angegeben werden.
	 * @param commandLineDataFileName	Dateiname f�r die Statistikdatei der in Beispiel-Kommandozeilen-Befehlen angezeigt werden soll
	 */
	public void setDataFileName(final String commandLineDataFileName) {
		tree.setDataFileName(commandLineDataFileName);
	}

	/**
	 * Liefert den Basis-Statistikknoten der Baumstruktur
	 * @return	Basis-Statistikknoten der Baumstruktur oder <code>null</code>, wenn kein solcher ermittelt werden konnte
	 * {@link #selectNode(StatisticNode)}
	 */
	public StatisticNode getStatisticNodeRoot() {
		final Object root=tree.getModel().getRoot();
		if (!(root instanceof DefaultMutableTreeNode)) return null;
		if (((DefaultMutableTreeNode)root).getChildCount()==0) return null;
		final TreeNode child=((DefaultMutableTreeNode)root).getChildAt(0);
		if (!(child instanceof DefaultMutableTreeNode)) return null;
		final Object obj=((DefaultMutableTreeNode)child).getUserObject();
		if (!(obj instanceof StatisticNode)) return null;
		return ((StatisticNode)obj).getParent();
	}
}