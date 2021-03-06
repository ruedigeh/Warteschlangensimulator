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
package ui.script;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementInput;
import ui.modeleditor.elements.ModelElementInputDB;
import ui.modeleditor.elements.ModelElementInputDDE;
import ui.modeleditor.elements.ModelElementInputJS;
import ui.modeleditor.elements.ModelElementOutput;
import ui.modeleditor.elements.ModelElementOutputDB;
import ui.modeleditor.elements.ModelElementOutputDDE;
import ui.modeleditor.elements.ModelElementOutputJS;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementRecord;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Dialog zur Auswahl einer Station aus einem Modell
 * @author Alexander Herzog
 * @see ScriptPopupItemCommandID
 */
public class SelectIDDialog extends BaseDialog {
	private static final long serialVersionUID = -3524092476641852821L;

	private int selectedID;
	private int selectedIndex;
	private int[] ids;
	private final JComboBox<String> combo;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param ids	Zuordnung von vorhandenen IDs zu Stationen
	 * @param help	Hilfe-Runnable
	 * @param stationRestrictions	Gibt an, ob es sich bei der Zuordnung um eine Liste aller Stationen (<code>false</code>) oder nur um eine bestimmte Auswahl (<code>true</code>) handelt
	 * @param preferProcessStations	Soll wenn möglich in der Liste eine Bedienstation oder Verzögerungsstation initial ausgewählt werden?
	 */
	public SelectIDDialog(final Component owner, final Map<Integer,ModelElementBox> ids, final Runnable help, final boolean stationRestrictions, final boolean preferProcessStations) {
		super(owner,Language.tr("ScriptPopup.SelectIDDialog.Title"));
		selectedID=-1;
		selectedIndex=-1;
		if (ids.size()==0) {
			if (stationRestrictions) {
				MsgBox.error(owner,Language.tr("ScriptPopup.SelectIDDialog.ErrorMatchingNoStations.Title"),Language.tr("ScriptPopup.SelectIDDialog.ErrorMatchingNoStations.Info"));
			} else {
				MsgBox.error(owner,Language.tr("ScriptPopup.SelectIDDialog.ErrorNoStations.Title"),Language.tr("ScriptPopup.SelectIDDialog.ErrorNoStations.Info"));
			}
			combo=null;
			return;
		}

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		setup.add(line);

		final JLabel label=new JLabel(Language.tr("ScriptPopup.SelectIDDialog.Station")+":");
		line.add(label);

		line.add(combo=new JComboBox<>(getIDNames(ids,preferProcessStations)));
		if (selectedIndex<0) selectedIndex=0;
		combo.setSelectedIndex(selectedIndex);
		label.setLabelFor(combo);

		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell aus dem die Liste der Stationen ausgelesen werden soll
	 * @param help	Hilfe-Runnable
	 * @param stationTypes	Optionale Liste der Klassennamen der Stationen, die in die Auswahl aufgenommen werden sollen (wird hier <code>null</code> oder eine leere Liste übergeben, so erfolgt keine Einschränkung)
	 * @param preferProcessStations	Soll wenn möglich in der Liste eine Bedienstation oder Verzögerungsstation initial ausgewählt werden?
	 */
	public SelectIDDialog(final Component owner, final EditModel model, final Runnable help, final Class<?>[] stationTypes, final boolean preferProcessStations) {
		this(owner,getIDs(model,stationTypes),help,(stationTypes!=null && stationTypes.length>0),true);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell aus dem die Liste der Stationen ausgelesen werden soll
	 * @param help	Hilfe-Runnable
	 */
	public SelectIDDialog(final Component owner, final EditModel model, final Runnable help) {
		this(owner,model,help,null,false);
	}

	private static void addIDsToMap(final ModelSurface surface, final Map<Integer,ModelElementBox> ids, final Class<?>[] stationTypes) {
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			if (element instanceof ModelElementSource) continue;
			if (element instanceof ModelElementSourceDB) continue;
			if (element instanceof ModelElementSourceDDE) continue;
			if (element instanceof ModelElementSourceMulti) continue;
			if (element instanceof ModelElementSourceTable) continue;
			if (element instanceof ModelElementDispose) continue;
			if (element instanceof ModelElementInput) continue;
			if (element instanceof ModelElementInputDB) continue;
			if (element instanceof ModelElementInputDDE) continue;
			if (element instanceof ModelElementInputJS) continue;
			if (element instanceof ModelElementOutput) continue;
			if (element instanceof ModelElementOutputDB) continue;
			if (element instanceof ModelElementOutputDDE) continue;
			if (element instanceof ModelElementOutputJS) continue;
			if (element instanceof ModelElementRecord) continue;

			if (stationTypes!=null && stationTypes.length>0) {
				boolean ok=false;
				for (Class<?> cls: stationTypes) if (cls.isInstance(element)) {ok=true; break;}
				if (!ok) continue;
			}

			if (element instanceof ModelElementSub) {
				addIDsToMap(((ModelElementSub)element).getSubSurface(),ids,stationTypes);
				continue;
			}

			ids.put(element.getId(),(ModelElementBox)element);
		}
	}

	/**
	 * Erstellt eine Zuordnung, die alle IDs und die zugehörigen Namen der Stationen eines Modells enthält
	 * @param model	Modell aus dem die Stationen ausgelesen werden sollen
	 * @return	Zuordnung aus IDs und Stationen
	 */
	public static Map<Integer,ModelElementBox> getIDs(final EditModel model) {
		return getIDs(model,null);
	}

	/**
	 * Erstellt eine Zuordnung, die alle IDs und die zugehörigen Namen der Stationen eines Modells enthält
	 * @param model	Modell aus dem die Stationen ausgelesen werden sollen
	 * @param stationTypes	Optionale Liste der Klassennamen der Stationen, die in die Zuordnung aufgenommen werden sollen (wird hier <code>null</code> oder eine leere Liste übergeben, so erfolgt keine Einschränkung)
	 * @return	Zuordnung aus IDs und Stationen
	 */
	public static Map<Integer,ModelElementBox> getIDs(final EditModel model, final Class<?>[] stationTypes) {
		final Map<Integer,ModelElementBox> ids=new HashMap<>();
		addIDsToMap(model.surface,ids,stationTypes);
		return ids;
	}

	private String[] getIDNames(final Map<Integer,ModelElementBox> ids, final boolean preferProcessStations) {
		this.ids=ids.keySet().stream().mapToInt(i->i).sorted().toArray();

		final String[] names=new String[this.ids.length];
		for (int i=0;i<this.ids.length;i++) {
			final ModelElementBox element=ids.get(this.ids[i]);
			final StringBuilder sb=new StringBuilder();
			sb.append(element.getTypeName());
			if (!element.getName().trim().isEmpty()) sb.append(String.format(" \"%s\"",element.getName()));
			sb.append(String.format(" (id=%d)",element.getId()));
			names[i]=sb.toString();

			if (preferProcessStations && selectedIndex<0 && ((element instanceof ModelElementProcess) || (element instanceof ModelElementDelay))) selectedIndex=i;
		}

		return names;
	}

	@Override
	protected void storeData() {
		selectedID=ids[combo.getSelectedIndex()];
	}

	/**
	 * Liefert die ID der gewählten Station
	 * @return	ID der gewählten Station oder -1, wenn keine Auswahl erfolgt ist
	 */
	public int getSelectedID() {
		return selectedID;
	}
}
