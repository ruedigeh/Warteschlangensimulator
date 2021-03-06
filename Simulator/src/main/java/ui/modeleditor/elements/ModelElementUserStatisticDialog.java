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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import tools.JTableExt;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementUserStatistic}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementUserStatistic
 */
public class ModelElementUserStatisticDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 1758925669523956820L;

	private UserStatisticTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementUserStatistic}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	public ModelElementUserStatisticDialog(final Component owner, final ModelElementUserStatistic element, final boolean readOnly) {
		super(owner,Language.tr("Surface.UserStatistic.Dialog.Title"),element,"ModelElementUserStatistic",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationUserStatistic;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		if (element instanceof ModelElementUserStatistic) {
			JTableExt table=new JTableExt();
			table.setModel(tableModel=new UserStatisticTableModel(table,helpRunnable,((ModelElementUserStatistic)element).getKeys(),((ModelElementUserStatistic)element).getIsTime(),((ModelElementUserStatistic)element).getExpressions(),element.getModel(),element.getSurface(),readOnly));
			table.setIsPanelCellTable(0);
			table.setIsPanelCellTable(1);
			table.getColumnModel().getColumn(0).setMaxWidth(200);
			table.getColumnModel().getColumn(0).setMinWidth(200);
			table.setEnabled(!readOnly);
			content.add(new JScrollPane(table),BorderLayout.CENTER);
		}

		return content;
	}

	/**
	 * Stellt die Gr��e des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		pack();
		setResizable(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementUserStatistic) {
			final List<String> keys=((ModelElementUserStatistic)element).getKeys();
			final List<Boolean> isTime=((ModelElementUserStatistic)element).getIsTime();
			final List<String> expressions=((ModelElementUserStatistic)element).getExpressions();
			keys.clear();
			isTime.clear();
			expressions.clear();
			keys.addAll(tableModel.getKeys());
			isTime.addAll(tableModel.getIsTime());
			expressions.addAll(tableModel.getExpressions());
		}
	}
}