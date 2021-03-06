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
package ui.modeleditor.elements;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementDifferentialCounter}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDifferentialCounter
 */
public class ModelElementDifferentialCounterDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 8074769774808895018L;

	private JTextField change;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDifferentialCounter}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	public ModelElementDifferentialCounterDialog(final Component owner, final ModelElementDifferentialCounter element, final boolean readOnly) {
		super(owner,Language.tr("Surface.DifferentialCounter.Dialog.Title"),element,"ModelElementDifferentialCounter",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDifferentialCounter;
	}

	@Override
	protected JComponent getContentPanel() {
		final Object[] data=getInputPanel(Language.tr("Surface.DifferentialCounter.Dialog.Increment")+":",""+((ModelElementDifferentialCounter)element).getChange(),7);

		change=(JTextField)data[1];
		change.setEditable(!readOnly);
		change.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		checkData(false);
		return (JPanel)data[0];
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		Integer I=NumberTools.getInteger(change,true);
		if (I==null || I==0) {
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.DifferentialCounter.Dialog.Increment.Error.Title"),Language.tr("Surface.DifferentialCounter.Dialog.Increment.Error.Info"));
			return false;
		}
		return true;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();
		((ModelElementDifferentialCounter)element).setChange(NumberTools.getInteger(change,true));
	}
}