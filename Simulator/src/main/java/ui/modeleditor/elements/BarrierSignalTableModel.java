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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellEditor;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;

/**
 * Tabellendarstellung der m�glichen Signale zur �ffnung einer Schranke
 * innerhalb eines {@link ModelElementBarrier}-Dialogs
 * @author Alexander Herzog
 * @see ModelElementBarrier
 * @see ModelElementBarrierSignalOption
 */
public class BarrierSignalTableModel extends JTableExtAbstractTableModel {
	private static final long serialVersionUID = 8386629305184893632L;

	private final JTableExt table;
	private final List<ModelElementBarrierSignalOption> options;
	private final String[] signals;
	private final String[] clientTypes;
	private final Runnable help;
	private final boolean readOnly;

	/**
	 * Konstruktor der Klasse
	 * @param table	Zugeh�rige Tabelle (um diese veranlassen zu k�nnen, sich zu aktualisieren)
	 * @param options	Darzustellende Daten
	 * @param signals	Liste mit allen Signalnamen im System
	 * @param clientTypes	Liste mit allen Kundentypnamen im System
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 * @see ModelElementBarrierSignalOption
	 */
	public BarrierSignalTableModel(final JTableExt table, final List<ModelElementBarrierSignalOption> options, final String[] signals, final String[] clientTypes, final boolean readOnly, final Runnable help) {
		super();

		this.table=table;
		this.options=new ArrayList<>();
		for (ModelElementBarrierSignalOption option: options) this.options.add(option.clone());

		this.signals=signals;
		this.clientTypes=clientTypes;
		this.readOnly=readOnly;
		this.help=help;

		updateTable();
	}

	private void updateTable() {
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}

	@Override
	public int getRowCount() {
		return options.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	private String getDescription(final ModelElementBarrierSignalOption option) {
		final StringBuilder sb=new StringBuilder();

		if (option.getClientType()==null) sb.append(Language.tr("Surface.Barrier.Dialog.Description.AllClientTypes")); else sb.append(option.getClientType());
		if (option.getInitialClients()>0) {
			sb.append(", "+Language.tr("Surface.Barrier.Dialog.Description.InitialRelease")+"="+option.getInitialClients());
		}
		sb.append(", "+Language.tr("Surface.Barrier.Dialog.Description.PerSignal")+"=");
		if (option.getClientsPerSignal()>=0) {
			sb.append(option.getClientsPerSignal());
		} else {
			sb.append(Language.tr("Surface.Barrier.Dialog.Description.PerSignal.All"));
		}

		return sb.toString();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==options.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Surface.Barrier.Dialog.Add")},new URL[]{Images.MODELEDITOR_ELEMENT_SIGNAL.getURL()},new ActionListener[]{new EditButtonListener(0,-1)});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0:
			return makeEditPanelSmallBorder(
					Images.MODELEDITOR_ELEMENT_SIGNAL.getURL(),
					options.get(rowIndex).getSignalName(),
					new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
					new String[]{
							Language.tr("Surface.Barrier.Dialog.Edit"),
							Language.tr("Surface.Barrier.Dialog.Delete")
					},
					new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
					);
		case 1:
			if (rowIndex==0 && rowIndex==options.size()-1) {
				/* kein hoch oder runter */
				return makePanel(getDescription(options.get(rowIndex)),null);
			}
			if (rowIndex==0) {
				/* kein Nach-Oben-Button */
				return makeEditPanelSmallBorder(
						getDescription(options.get(rowIndex)),
						new URL[]{Images.ARROW_DOWN.getURL()},
						new String[]{Language.tr("Surface.Barrier.Dialog.Down")},
						new ActionListener[]{new EditButtonListener(2,rowIndex)});
			}
			if (rowIndex==options.size()-1) {
				/* kein Nach-Unten-Button */
				return makeEditPanelSmallBorder(
						getDescription(options.get(rowIndex)),
						new URL[]{Images.ARROW_UP.getURL()},
						new String[]{Language.tr("Surface.Barrier.Dialog.Up")},
						new ActionListener[]{new EditButtonListener(1,rowIndex)});
			}
			return makeEditPanelSmallBorder(
					getDescription(options.get(rowIndex)),
					new URL[]{Images.ARROW_UP.getURL(),Images.ARROW_DOWN.getURL()},
					new String[]{
							Language.tr("Surface.Barrier.Dialog.Up"),
							Language.tr("Surface.Barrier.Dialog.Down")
					},
					new ActionListener[]{new EditButtonListener(1,rowIndex),new EditButtonListener(2,rowIndex)});


		default: return null;
		}
	}

	@Override
	public final boolean isCellEditable(int rowIndex, int columnIndex) {
		return !readOnly;
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return Language.tr("Surface.Barrier.Dialog.ReleaseSignal");
		case 1: return Language.tr("Surface.Barrier.Dialog.ReleaseProperties");
		default: return super.getColumnName(column);
		}
	}

	/**
	 * Schreibt die Daten aus der Tabelle in die �bergebene Liste zur�ck
	 * @param options	Liste in die die in der Tabelle befindlichen Freigabe-Optionen zur�ckgeschrieben werden sollen (die Liste wird dabei als erstes geleert)
	 */
	public void storeData(final List<ModelElementBarrierSignalOption> options) {
		options.clear();
		for (ModelElementBarrierSignalOption option: this.options) options.add(option.clone());
	}

	private class EditButtonListener implements ActionListener {
		private final int col;
		private final int row;

		public EditButtonListener(final int col, final int row) {
			this.col=col;
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			ModelElementBarrierSignalOption option;

			switch (col) {
			case 0:
				final BarrierSignalTableModelDialog dialog=new BarrierSignalTableModelDialog(table,help,(row>=0)?options.get(row):null,signals,clientTypes);
				dialog.setVisible(true);
				if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					if (row<0) {
						options.add(dialog.getOption());
					} else {
						options.set(row,dialog.getOption());
					}
					updateTable();
				}
				break;
			case 1:
				if (row>0) {
					option=options.get(row); options.set(row,options.get(row-1)); options.set(row-1,option);
					updateTable();
				}
				break;
			case 2:
				if (row<options.size()-1) {
					option=options.get(row); options.set(row,options.get(row+1)); options.set(row+1,option);
					updateTable();
				}
				break;
			}
		}
	}

	private class DeleteButtonListener implements ActionListener {
		private final int row;

		public DeleteButtonListener(final int row) {
			this.row=row;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			final String name=options.get(row).getSignalName();
			if (!MsgBox.confirm(table,Language.tr("Surface.Barrier.Dialog.Delete.Confirm.Title"),String.format(Language.tr("Surface.Barrier.Dialog.Delete.Confirm.Info"),name),Language.tr("Surface.Barrier.Dialog.Delete.Confirm.YesInfo"),Language.tr("Surface.Barrier.Dialog.Delete.Confirm.NoInfo"))) return;
			options.remove(row);
			updateTable();
		}
	}
}