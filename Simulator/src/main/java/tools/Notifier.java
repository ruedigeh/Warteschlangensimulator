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
package tools;

import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.util.Timer;
import java.util.TimerTask;

import language.Language;
import ui.MainFrame;

/**
 * Erm�glicht die Ausgabe einer Meldung �ber das System-Tray
 * @author Alexander Herzog
 */
public class Notifier {
	/**
	 * �ber welches Ereignis soll benachrichtigt werden?
	 * @author Alexander Herzog
	 */
	public enum Message {
		/** Abschluss einer Simulation */
		SIMULATION_DONE,

		/** Abschluss einer Parameterreihen-Simulation */
		PARAMETER_SERIES_DONE,

		/** Abschluss einer Optimierung */
		OPTIMIZATION_DONE
	}

	private static final long MIN_NOTIFY_OPERATION_TIME=10_000;

	private static final SetupData setup=SetupData.getSetup();

	private static TrayIcon lastIcon;

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt nur statische Methoden zur Verf�gung und kann nicht instanziert werden.
	 */
	private Notifier() {

	}

	private static boolean removeLastNotify() {
		if (lastIcon==null) return false;
		try {
			final SystemTray tray=SystemTray.getSystemTray();
			if (tray==null) return false;
			tray.remove(lastIcon);
			lastIcon=null;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/*
	private static boolean playSound() {
		final Runnable runnable=(Runnable)Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.default");
		if (runnable==null) return false;
		runnable.run();
		return true;
	}
	 */

	private static boolean showMessage(final String text) {
		try {
			final SystemTray tray=SystemTray.getSystemTray();
			if (tray==null) return false;

			final TrayIcon icon=new TrayIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.ICON_URL),MainFrame.PROGRAM_NAME);
			icon.setImageAutoSize(true);
			tray.add(icon);

			icon.displayMessage(MainFrame.PROGRAM_NAME,text,MessageType.INFO);
			new Timer().schedule(new TimerTask() {
				@Override public void run() {tray.remove(icon);}
			},15000);
			icon.addActionListener(e->tray.remove(icon));
			lastIcon=icon;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static String getMessageString(final Message message) {
		switch (message) {
		case SIMULATION_DONE: return Language.tr("Notifier.Message.SimulationDone");
		case PARAMETER_SERIES_DONE: return Language.tr("Notifier.Message.ParameterSeriesDone");
		case OPTIMIZATION_DONE: return Language.tr("Notifier.Message.OptimizationDone");
		default: return "";
		}
	}

	/**
	 * Zeigt (wenn das Setup dies zul�sst) eine Meldung an.
	 * @param message	Anzuzeigende Meldung
	 */
	public static void run(final Message message) {
		run(message,0);
	}

	/**
	 * Zeigt (wenn das Setup dies zul�sst) eine Meldung an.
	 * @param message	Anzuzeigende Meldung
	 * @param operationStartTimeMS	Zeitpunkt des Beginns der jetzt abgeschlossenen Operation (falls Meldungen nur angezeigt werden sollen, wenn die jeweilige Operation l�nger gedauert hat)
	 */
	public static void run(final Message message, final long operationStartTimeMS) {
		final long operationTimeMS=System.currentTimeMillis()-operationStartTimeMS;
		removeLastNotify();
		if ((operationTimeMS>=MIN_NOTIFY_OPERATION_TIME && setup.notifyMode!=SetupData.NotifyMode.OFF) || setup.notifyMode==SetupData.NotifyMode.ALWAYS) {
			/* if (setup.useAcusticNotify) playSound(); - TrayIcon.displayMessage gibt bereits einen Ton aus */
			showMessage(getMessageString(message));
		}
	}

}
