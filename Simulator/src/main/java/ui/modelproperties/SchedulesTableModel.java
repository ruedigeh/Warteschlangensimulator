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
package ui.modelproperties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.TableCellEditor;

import language.Language;
import mathtools.TimeTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import tools.JTableExtAbstractTableModel;
import ui.images.Images;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSchedule;
import ui.modeleditor.ModelSchedules;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceRecord;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Diese Klasse h�lt das Tabellenmodell f�r die Anzeige von Zeitpl�nen
 * im Modelleigenschaften-Dialog vor.
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 */
public class SchedulesTableModel extends JTableExtAbstractTableModel {
	private static final long serialVersionUID = 6000743642076941503L;

	private ModelSchedules schedules;
	private List<ModelSchedule> schedulesList;
	private ModelSurface surface;
	private ModelResources resources;
	private final JTableExt table;
	private final boolean readOnly;
	private final Runnable help;

	/**
	 * Konstruktor der Klasse
	 * @param surface	Haupt-Zeichenfl�che (um ggf. Zeitpl�ne bei den Stationen umzubenennen)
	 * @param resources	Ressourcen-Objekt des Modells (um ggf. Zeitpl�ne bei den Ressourcen umzubenennen)
	 * @param schedules	Objekt das die initialen Zeitplan-Datens�tze enth�lt
	 * @param table	Zu dem Tabellenmodell geh�renden Tabellenobjekt (wird sowohl zum Ausrichten von Dialogen als auch um ein Update der Tabelle anzusto�en ben�tigt)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public SchedulesTableModel(final ModelSurface surface, final ModelResources resources, final ModelSchedules schedules, final JTableExt table, final boolean readOnly, final Runnable help) {
		super();
		this.table=table;
		this.readOnly=readOnly;
		this.help=help;
		setSchedules(surface,resources,schedules);
	}

	/**
	 * Stellt die Liste der Zeitpl�ne ein
	 * @param surface	Haupt-Zeichenfl�che (um ggf. Zeitpl�ne bei den Stationen umzubenennen)
	 * @param resources	Ressourcen-Objekt des Modells (um ggf. Zeitpl�ne bei den Ressourcen umzubenennen)
	 * @param schedules	Objekt das die initialen Zeitplan-Datens�tze enth�lt
	 */
	public void setSchedules(final ModelSurface surface, final ModelResources resources, final ModelSchedules schedules) {
		this.schedules=schedules;
		this.schedulesList=schedules.getSchedules();
		this.surface=surface;
		this.resources=resources;
		updateTable();
	}

	private void updateTable() {
		schedulesList.sort(new Comparator<ModelSchedule>() {
			@Override
			public int compare(ModelSchedule o1, ModelSchedule o2) {
				if (o1==null || o2==null) return 0;
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		schedules.setSchedules(schedulesList);
		fireTableDataChanged();
		TableCellEditor cellEditor=table.getCellEditor();
		if (cellEditor!=null) cellEditor.stopCellEditing();
	}


	@Override
	public int getRowCount() {
		return schedulesList.size()+1;
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex==schedulesList.size()) {
			switch (columnIndex) {
			case 0:	return makeButtonPanel(new String[]{Language.tr("Schedule.Add")},new URL[]{Images.MODELPROPERTIES_SCHEDULES_ADD.getURL()},new ActionListener[]{new AddButtonListener()});
			case 1: return "";
			}
		}

		switch (columnIndex) {
		case 0: return makeEditPanelSmallBorder(
				Images.MODELPROPERTIES_SCHEDULES.getURL(),
				schedulesList.get(rowIndex).getName(),
				new URL[]{Images.GENERAL_SETUP.getURL(),Images.EDIT_DELETE.getURL()},
				new String[]{Language.tr("Schedule.EditName"),Language.tr("Schedule.Delete")},
				new ActionListener[]{new EditButtonListener(0,rowIndex),new DeleteButtonListener(rowIndex)}
				);
		case 1: return makeEditPanelSmallBorder(
				getScheduleInfo(schedulesList.get(rowIndex)),
				new URL[]{Images.GENERAL_SETUP.getURL()},
				new String[]{Language.tr("Schedule.Edit")},
				new ActionListener[]{new EditButtonListener(1,rowIndex)}
				);
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
		case 0: return Language.tr("Schedule.Name");
		case 1: return Language.tr("Schedule.Schedule");
		default: return super.getColumnName(column);
		}
	}

	private String getScheduleInfo(final ModelSchedule schedule) {
		int count=schedule.getSlotCount();
		int time=schedule.getDurationPerSlot();

		StringBuilder sb=new StringBuilder();
		if (count==1) sb.append(" 1 "+Language.tr("Schedule.TimeSlot.Singular")); else sb.append(" "+count+" "+Language.tr("Schedule.TimeSlot.Plural"));
		sb.append(" a ");
		sb.append(TimeTools.formatLongTime(time));

		return sb.toString();
	}

	private boolean scheduleInUse(final String name, final ModelElementBox element) {
		if (element instanceof ModelElementSource) {
			ModelElementSourceRecord record=((ModelElementSource)element).getRecord();
			return (record.getNextMode()==ModelElementSourceRecord.NextMode.NEXT_SCHEDULE && record.getInterarrivalTimeSchedule().equals(name));
		}

		if (element instanceof ModelElementSourceMulti) {
			for (ModelElementSourceRecord record: ((ModelElementSourceMulti)element).getRecords()) {
				if (record.getNextMode()==ModelElementSourceRecord.NextMode.NEXT_SCHEDULE && record.getInterarrivalTimeSchedule().equals(name)) return true;
			}
			return false;
		}

		return false;
	}

	private List<Object> scheduleInUse(final String name) {
		final List<Object> usingIDs=new ArrayList<>();

		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			if (element instanceof ModelElementSub) for (ModelElement sub: ((ModelElementSub)element).getSubSurface().getElements()) if (sub instanceof ModelElementBox) {
				if (scheduleInUse(name,(ModelElementBox)sub)) usingIDs.add(sub.getId());
			}
			if (scheduleInUse(name,(ModelElementBox)element)) usingIDs.add(element.getId());
		}

		for (String resName: resources.list()) {
			ModelResource resource=resources.get(resName);
			if (resource.getMode()==ModelResource.Mode.MODE_SCHEDULE && name.equals(resource.getSchedule())) usingIDs.add(resName);
		}

		return usingIDs;
	}

	private void getInUseInfoText(final StringBuilder sb, List<Object> usingIDs) {
		if (usingIDs.size()==0) return;
		List<String> usingStations=new ArrayList<>();
		List<String> usingRessources=new ArrayList<>();
		for (Object obj: usingIDs) {
			if (obj instanceof Integer) usingStations.add(""+obj);
			if (obj instanceof String) usingRessources.add(""+obj);
		}

		if (usingStations.size()>0) {
			if (usingStations.size()==1) {
				sb.append(Language.tr("Schedule.InUseByElement.Single"));
			} else {
				sb.append(Language.tr("Schedule.InUseByElement.Multiple"));
			}
			sb.append("\n");
			sb.append(usingStations.get(0));
			for (int i=1;i<usingStations.size();i++) {sb.append(", "); sb.append(usingStations.get(i));}
			sb.append("\n");
		}
		if (usingRessources.size()>0) {
			if (usingStations.size()>0) {
				if (usingStations.size()==1) {
					sb.append(Language.tr("Schedule.InUseAlsoByResouce.Single"));
				} else {
					sb.append(Language.tr("Schedule.InUseAlsoByResource.Multiple"));
				}
			} else {
				if (usingStations.size()==1) {
					sb.append(Language.tr("Schedule.InUseByResouce.Single"));
				} else {
					sb.append(Language.tr("Schedule.InUseByResource.Multiple"));
				}
			}
			sb.append("\n");
			sb.append(usingRessources.get(0));
			for (int i=1;i<usingRessources.size();i++) {sb.append(", "); sb.append(usingRessources.get(i));}
			sb.append("\n");
		}
		sb.append("\n");

	}

	private void renameScheduleInModel(final List<Object> usingIDs, final String oldName, final String newName) {
		for (Object id: usingIDs) {
			if (id instanceof Integer) {
				/* Stationen mit entsprechender ID */
				final ModelElement element=surface.getById((Integer)id);
				if (element!=null && element instanceof ModelElementSource) {
					final ModelElementSourceRecord record=((ModelElementSource)element).getRecord();
					if (record.getNextMode()==ModelElementSourceRecord.NextMode.NEXT_SCHEDULE && record.getInterarrivalTimeSchedule().equals(oldName)) record.setInterarrivalTimeSchedule(newName);
				}
				if (element!=null && element instanceof ModelElementSourceMulti) {
					for(ModelElementSourceRecord record: ((ModelElementSourceMulti)element).getRecords()) {
						if (record.getNextMode()==ModelElementSourceRecord.NextMode.NEXT_SCHEDULE && record.getInterarrivalTimeSchedule().equals(oldName)) record.setInterarrivalTimeSchedule(newName);
					}
				}
			}
			if (id instanceof String) {
				/* Ressourcen mit entsprechendem Namen */
				final ModelResource resource=resources.getNoAutoAdd((String)id);
				if (resource!=null && resource.getMode()==ModelResource.Mode.MODE_SCHEDULE && resource.getSchedule().equals(oldName)) {
					resource.setSchedule(newName);
					fireUpdateResources();
				}
			}
		}
	}

	private List<Runnable> updateResourcesListeners=new ArrayList<>();

	/**
	 * F�gt ein <code>Runnable</code>-Objekt zu der Liste der Listener, die beim �ndern der
	 * verkn�pften Ressourcen aufgerufen werden sollen, hinzu.
	 * @param listener	Bei �nderungen der Ressourcen aufzurufender Listener
	 */
	public void addUpdateResourcesListener(final Runnable listener) {
		if (updateResourcesListeners.indexOf(listener)<0) updateResourcesListeners.add(listener);
	}

	/**
	 * Entfernt ein <code>Runnable</code>-Objekt aus der Liste der Listener, die beim �ndern
	 * der verbundenen Ressourcen aufgerufen werden sollen.
	 * @param listener	Listener, der nicht mehr �ber �nderungen an den Ressourcen benachrichtigt werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte.
	 */
	public boolean removeUpdateResources(final Runnable listener) {
		final int index=updateResourcesListeners.indexOf(listener);
		if (index<0) return false;
		updateResourcesListeners.remove(listener);
		return true;
	}

	/**
	 * Benachrichtigt alle Listener, das sich die Ressourcen ver�ndert haben.<br>
	 */
	public void fireUpdateResources() {
		for (Runnable listener: updateResourcesListeners) listener.run();
	}

	private class AddButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;
			ScheduleTableModelNameDialog dialog=new ScheduleTableModelNameDialog(table,help,ModelSchedules.getScheduleNames(schedulesList),null);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				schedulesList.add(new ModelSchedule(dialog.getScheduleName()));
				updateTable();
			}
		}
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

			switch (col) {
			case 0:
				final String oldName=schedulesList.get(row).getName();
				ScheduleTableModelNameDialog dialog1=new ScheduleTableModelNameDialog(table,help,ModelSchedules.getScheduleNames(schedulesList),oldName);
				dialog1.setVisible(true);
				if (dialog1.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					final String newName=dialog1.getScheduleName();
					if (!oldName.equals(newName)) {
						final List<Object> usingIDs=scheduleInUse(oldName);
						final StringBuilder sb=new StringBuilder();
						if (usingIDs.size()>0) {
							getInUseInfoText(sb,usingIDs);
							sb.append("<br>");
							sb.append(Language.tr("Schedule.Rename.Info"));
							if (MsgBox.confirm(table,Language.tr("Schedule.Rename.Title"),"<html><body>"+sb.toString(),Language.tr("Schedule.Rename.YesInfo"),Language.tr("Schedule.Rename.NoInfo"))) {
								renameScheduleInModel(usingIDs,oldName,newName);
							}
						}
						schedulesList.get(row).setName(newName);
						updateTable();
					}
				}
				break;
			case 1:
				ScheduleTableModelDataDialog dialog2=new ScheduleTableModelDataDialog(table,help,schedulesList.get(row));
				dialog2.setVisible(true);
				if (dialog2.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
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
			final String name=schedulesList.get(row).getName();
			final List<Object> usingIDs=scheduleInUse(name);
			final StringBuilder sb=new StringBuilder();
			getInUseInfoText(sb,usingIDs);
			sb.append("<br>");
			sb.append(String.format(Language.tr("Schedule.Delete.Confirmation"),name));
			if (!MsgBox.confirm(table,Language.tr("Schedule.Delete"),"<html><body>"+sb.toString()+"</body></html>",Language.tr("Schedule.Delete.YesInfo"),Language.tr("Schedule.Delete.NoInfo"))) return;
			schedulesList.remove(row);
			updateTable();
		}
	}

	/**
	 * Liefert alle momentan in dem Datenmodell enthaltenen Zeitplan-Datens�tze
	 * @return	Liste mit den Zeitplan-Datens�tzen
	 */
	public ModelSchedules getSchedules() {
		return schedules;
	}
}
