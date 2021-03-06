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
package ui.optimizer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.images.Images;

/**
 * Diese Klasse pr�ft im Vorfeld der Optimierung, ob ein Editor-Modell simulierbar ist
 * und f�hrt eine verk�rzte Simulation durch, um an ein {@link Statistics}-Objekt
 * zu kommen, welches f�r die Auswahl von XML-Elementen in den Optimierer-Einstellungen
 * notwendig ist.
 * @author Alexander Herzog
 * @see OptimizerPanel
 */
public final class OptimizerPanelPrepareDialog extends JDialog {
	private static final long serialVersionUID = -138228476553570125L;

	private volatile AnySimulator simulator=null;
	private String error=null;
	private Statistics statistics=null;

	/**
	 * Zweck der Modellvorbereitung durch diesen Dialog
	 * (f�r die Anzeige des Dialogtitels)
	 * @author Alexander Herzog
	 */
	public enum Mode {
		/** Vorbereitung f�r Optimierung */
		MODE_OPTIMIZATION,
		/** Vorbereitung f�r Skript-Ausf�hrung */
		MODE_JAVASCRIPT,
		/** Vorbereitung f�r Parameterreihen-Simulation */
		MODE_PARAMETER_COMPARE
	}

	private static Window getOwnerWindow(Component parent) {
		while (parent!=null && !(parent instanceof Window)) parent=parent.getParent();
		return (Window)parent;
	}

	private static String getTitle(final Mode mode) {
		switch (mode) {
		case MODE_OPTIMIZATION: return Language.tr("Optimizer.Prepare.Title");
		case MODE_JAVASCRIPT: return Language.tr("Optimizer.Prepare.Title.Javascript");
		case MODE_PARAMETER_COMPARE: return Language.tr("Optimizer.Prepare.Title.ParameterCompare");
		default: return "";
		}
	}

	private static String getInfo(final Mode mode) {
		switch (mode) {
		case MODE_OPTIMIZATION: return Language.tr("Optimizer.Prepare.Info");
		case MODE_JAVASCRIPT: return Language.tr("Optimizer.Prepare.Info.Javascript");
		case MODE_PARAMETER_COMPARE: return Language.tr("Optimizer.Prepare.Info.ParameterCompare");
		default: return "";
		}
	}

	/**
	 * Konstruktor der Klasse <code>OptimizerPanelPrepareDialog</code>.<br>
	 * Erstellt den Dialog, macht ihn sichtbar und startet im Falle, dass das
	 * Modell g�ltig ist, die Simulation. Die Methode kehrt erst zur�ck, wenn
	 * der Dialog wieder ausgeblendet wurde und Ergebnisse vorliegen.
	 * @param owner	�bergeordnetes Fenster
	 * @param model	Zu pr�fendes und zu simulierendes Modell
	 * @param statistics	Optionales Statistik-Objekt (kann auch <code>null</code> sein). Wenn diese Statistik zu dem Modell passt, wird dieses Objekt zur�ckgegeben und nicht erst eine Mini-Simulation gestartet
	 * @param mode	Gibt an, f�r welchen Zweck die Modellvorbereitung durchgef�hrt werden soll (Anpassung des Dialogtitels)
	 */
	public OptimizerPanelPrepareDialog(final Component owner, final EditModel model, final Statistics statistics, final Mode mode) {
		super(getOwnerWindow(owner),getTitle(mode),Dialog.ModalityType.APPLICATION_MODAL);

		if (statistics!=null && statistics.editModel.equalsEditModel(model)) {
			/* Wir haben Gl�ck, wir k�nnen die bereits vorhandene Statistik verwenden. */
			this.statistics=statistics;
			return;
		}

		if (model.surface.getElementCount()==0) {
			error=Language.tr("Optimizer.Prepare.ErrorNoStations");
			return;
		}

		/* GUI vorbereiten */
		final Container content=getContentPane();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JButton cancel;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.CENTER)));
		line.add(new JLabel(getInfo(mode)));
		content.add(line=new JPanel(new FlowLayout(FlowLayout.CENTER)));
		line.add(cancel=new JButton(Language.tr("Dialog.Button.Abort")));
		cancel.setIcon(Images.GENERAL_CANCEL.getIcon());
		cancel.addActionListener((e)->{
			if (simulator!=null) {
				simulator.cancel();
				cancelRun();
				error="<html>"+Language.tr("Optimizer.Prepare.Canceled")+"</html>";
			}
			this.statistics=null;
			setVisible(false);
		});

		/* Modell vorbereiten */

		EditModel simpleModel=model.clone();
		simpleModel.useClientCount=true;
		simpleModel.clientCount=Math.min(1000,simpleModel.clientCount);
		simpleModel.warmUpTime=0.0;

		/* Simulation starten */

		final StartAnySimulator starter=new StartAnySimulator(simpleModel);
		error=starter.prepare();
		if (error!=null) {
			simulator=null;
			return;
		}
		simulator=starter.start();
		new Thread(()->{simulator.finalizeRun(); if (simulator!=null) finalizeRun();}).start();

		/* Fenster als solches vorbereiten */

		pack();
		setLocationRelativeTo(this.getOwner());
		setVisible(true);
	}

	private synchronized void cancelRun() {
		simulator=null;
	}

	private synchronized void finalizeRun() {
		if (simulator!=null) {
			statistics=simulator.getStatistic();
			simulator=null;
			SwingUtilities.invokeLater(()->setVisible(false));
		}
	}

	/**
	 * Liefert im Erfolgsfall die Statistik �ber den minimalen Simulationslauf zur�ck.
	 * @return	Simulationsergebnisse oder im Fehlerfall <code>null</code>
	 * @see #getError()
	 */
	public Statistics getMiniStatistics() {
		return statistics;
	}

	/**
	 * Liefert im Fehlerfall die Begr�ndung f�r den Fehler zur�ck
	 * @return	Fehlermeldung in html-Form oder <code>null</code> wenn das Modell fehlerfrei ist und erfolgreich simuliert werden konnte
	 */
	public String getError() {
		return error;
	}
}
