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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modelproperties.ModelPropertiesDialog;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementSource}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSource
 */
public final class ModelElementSourceDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = -86922871601132368L;

	private ModelElementSourceRecordPanel recordPanel;

	private final String oldName;
	private final ModelClientData clientData;
	private JButton editClientDataButton;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSource}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendatenelement aus dem Modell (zum Auslesen von Icons und ggf. um Kundentypen umzubenennen)
	 */
	public ModelElementSourceDialog(final Component owner, final ModelElementSource element, final boolean readOnly, final ModelClientData clientData) {
		super(owner,Language.tr("Surface.Source.Dialog.Title"),element,"ModelElementSource",readOnly,false);
		oldName=element.getName();
		this.clientData=clientData;
		setVisible(true);
	}

	@Override
	protected void initUserNameFieldButtons(final JPanel panel) {
		panel.add(editClientDataButton=new JButton());
		setClientIcon(element.getName(),editClientDataButton,element.getModel());
		editClientDataButton.setToolTipText(Language.tr("Surface.Source.Dialog.ClientTypeSettings"));
		editClientDataButton.addActionListener(e->editClientData());
	}

	/**
	 * Stellt die Gr��e des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(700,600);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSource;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		recordPanel=new ModelElementSourceRecordPanel(readOnly,element.getModel(),element.getSurface(),helpRunnable,true);
		recordPanel.setData(((ModelElementSource)element).getRecord(),element);
		return recordPanel;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return recordPanel.checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementSourceRecord record=((ModelElementSource)element).getRecord();
		recordPanel.getData(record);
		ModelElementSourceRecordPanel.renameClients(oldName,element.getName(),clientData,element.getSurface());
	}

	private void editClientData() {
		final String name=(oldName.isEmpty())?getElementName():oldName;
		if (name.isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Title"),Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Info"));
			return;
		}

		if (ModelPropertiesDialog.editClientData(this,helpRunnable,element.getModel(),name,readOnly)) setClientIcon(name,editClientDataButton,element.getModel());
	}
}
