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
package ui.statistics;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import language.Language;
import scripting.js.JSRunDataFilterTools;
import simulator.statistics.Statistics;
import systemtools.MsgBox;
import systemtools.statistics.StatisticViewerSpecialBase;
import tools.SetupData;
import ui.script.ScriptTools;

/**
 * Diese Klasse kapselt einen xslt-�bersetzer, der innerhalb von <code>StatisticPanel</code> verwendet wird.
 * @author Alexander Herzog
 * @version 2.0
 */
public class StatisticViewerFastAccess extends StatisticViewerSpecialBase {
	private final Runnable helpFastAccess;
	private final Runnable helpFastAccessModal;
	private final Runnable helpFastAccessJS;
	private final Runnable helpFastAccessModalJS;
	private final Runnable helpFastAccessJava;
	private final Runnable helpFastAccessModalJava;
	private final Statistics statistic;

	private JTextArea results;
	private JTabbedPane tabs;

	private StatisticViewerFastAccessList fastAccessList;
	private StatisticViewerFastAccessJS fastAccessJS;
	private StatisticViewerFastAccessJava fastAccessJava;

	/**
	 * Konstruktor der Klasse <code>StatisticViewerFastAccess</code>
	 * @param statistic	Statistik-Objekt, dem die Daten entnommen werden sollen
	 * @param helpFastAccess	Hilfe f�r Schnellzugriff-Seite
	 * @param helpFastAccessModal	Hilfe f�r Schnellzugriff-Dialog
	 * @param helpFastAccessJS	Hilfe f�r Schnellzugriff-Seite (Javascript)
	 * @param helpFastAccessModalJS	Hilfe f�r Schnellzugriff-Dialog (Javascript)
	 * @param helpFastAccessJava	Hilfe f�r Schnellzugriff-Seite (Java)
	 * @param helpFastAccessModalJava	Hilfe f�r Schnellzugriff-Dialog (Java)
	 */
	public StatisticViewerFastAccess(final Statistics statistic, final Runnable helpFastAccess, final Runnable helpFastAccessModal, final Runnable helpFastAccessJS, final Runnable helpFastAccessModalJS, final Runnable helpFastAccessJava, final Runnable helpFastAccessModalJava) {
		this.statistic=statistic;
		this.helpFastAccess=helpFastAccess;
		this.helpFastAccessModal=helpFastAccessModal;
		this.helpFastAccessJS=helpFastAccessJS;
		this.helpFastAccessModalJS=helpFastAccessModalJS;
		this.helpFastAccessJava=helpFastAccessJava;
		this.helpFastAccessModalJava=helpFastAccessModalJava;
	}

	@Override
	public ViewerType getType() {
		return ViewerType.TYPE_SPECIAL;
	}

	private JSplitPane viewer;

	@Override
	public Container getViewer(boolean needReInit) {
		if (viewer!=null && !needReInit) return viewer;

		viewer=new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		/* Ausgabe */
		viewer.add(new JScrollPane(results=new JTextArea()));
		results.setEditable(false);

		/* Filter */
		viewer.add(tabs=new JTabbedPane());
		tabs.setPreferredSize(new Dimension(1,250));
		final int lastMode=SetupData.getSetup().lastFilterMode; /* M�ssen wir vor dem ersten Hinzuf�gen eines Tabs abfragen. */

		tabs.add(fastAccessList=new StatisticViewerFastAccessList(helpFastAccess,helpFastAccessModal,statistic,()->resultsChanged()),Language.tr("Statistic.FastAccess.FilterList"));
		tabs.add(fastAccessJS=new StatisticViewerFastAccessJS(helpFastAccessJS,helpFastAccessModalJS,statistic,()->resultsChanged()),Language.tr("Statistic.FastAccess.FilterJS"));
		tabs.add(fastAccessJava=new StatisticViewerFastAccessJava(helpFastAccessJava,helpFastAccessModalJava,statistic,()->resultsChanged()),Language.tr("Statistic.FastAccess.FilterJava"));

		for (int i=0;i<tabs.getTabCount();i++) tabs.setIconAt(i,((StatisticViewerFastAccessBase)tabs.getComponentAt(i)).getIcon());
		if (lastMode>=0 && lastMode<tabs.getTabCount()) tabs.setSelectedIndex(lastMode);
		tabs.addChangeListener(e->resultsChanged());
		resultsChanged();

		viewer.setResizeWeight(1);
		viewer.setDividerLocation(viewer.getSize().height-200);
		return viewer;
	}

	@Override
	public void copyToClipboard(Clipboard clipboard) {
		clipboard.setContents(new StringSelection(results.getText()),null);
	}

	@Override
	public boolean print() {
		return false;
	}

	@Override
	public void save(Component owner) {
		final String fileName=ScriptTools.selectTextSaveFile(owner,null,null);
		if (fileName==null) return;
		final File file=new File(fileName);

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		JSRunDataFilterTools.saveText(results.getText(),file,false);
	}

	@Override
	public boolean save(Component owner, File file) {
		return JSRunDataFilterTools.saveText(results.getText(),file,false);
	}

	/* (non-Javadoc)
	 * @see tools.ui.statistics.StatisticViewer#ownSettings()
	 */
	@Override
	public String ownSettingsName() {return null;}

	@Override
	public boolean getCanDo(CanDoAction canDoType) {
		switch (canDoType) {
		case CAN_DO_COPY : return true;
		case CAN_DO_PRINT : return false;
		case CAN_DO_SAVE : return true;
		default: return false;
		}
	}

	private void resultsChanged() {
		final int index=tabs.getSelectedIndex();
		if (index<0) return;

		final SetupData setup=SetupData.getSetup();
		if (index!=setup.lastFilterMode) {
			setup.lastFilterMode=index;
			setup.saveSetup();
		}

		results.setText(((StatisticViewerFastAccessBase)tabs.getComponentAt(index)).getResults());
	}

	/**
	 * F�hrt die Schnellzugriff-Skripte neu aus (nach einem Wechsel der JS-Engine aufzurufen).
	 */
	public void updateResults() {
		if (fastAccessList!=null) fastAccessList.process(true);
		if (fastAccessJS!=null) fastAccessJS.process(true);
		/* fastAccessJava -> nur durch Button-Klick ausl�sen, nicht automatisch */
	}

	/**
	 * Zu welcher Schnellzugriff-Varianten soll der XML-Selektor hinzugef�gt werden?
	 * @author Alexander Herzog
	 * @see StatisticViewerFastAccess#addXML(AddXMLMode, String)
	 */
	public enum AddXMLMode {
		/** Zur Liste hinzuf�gen */
		LIST,
		/** Zur Javascript-Variante hinzuf�gen */
		JAVASCRIPT,
		/** Zur Java-Variante hinzuf�gen */
		JAVA
	}

	/**
	 * F�gt einen XML-Selektor-Ausdruck zu einer der Schnellzugriff-Varianten hinzu
	 * @param addXMLMode	Schnellzugriff-Varianten zu der der XML-Selektor-Ausdruck hinzugef�gt werden soll
	 * @param selector	Hinzuzuf�gender Selektor
	 * @see StatisticViewerFastAccess.AddXMLMode
	 * @see StatisticViewerFastAccessBase#addXML(String)
	 */
	public void addXML(final AddXMLMode addXMLMode, final String selector) {
		getViewer(false);

		switch (addXMLMode) {
		case LIST: fastAccessList.addXML(selector); break;
		case JAVASCRIPT: fastAccessJS.addXML(selector); break;
		case JAVA: fastAccessJava.addXML(selector); break;
		}
	}
}