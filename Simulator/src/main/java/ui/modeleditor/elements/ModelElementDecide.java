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

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModelFixer;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Verzweigt die eintreffenden Kunden in verschiedene Richtungen
 * @author Alexander Herzog
 */
public class ModelElementDecide extends ModelElementBox implements ModelDataRenameListener, ModelElementEdgeMultiIn, ModelElementEdgeMultiOut, ElementWithNewClientNames {
	/**
	 * Art der Verzweigung
	 * @author Alexander Herzog
	 * @see ModelElementDecide#getMode()
	 * @see ModelElementDecide#setMode(DecideMode)
	 */
	public enum DecideMode {
		/** Zuf�llig */
		MODE_CHANCE,
		/** Gem�� Bedingung */
		MODE_CONDITION,
		/** Nach Kundentyp */
		MODE_CLIENTTYPE,
		/** Reihum */
		MODE_SEQUENCE,
		/** K�rzeste Warteschlange an der n�chsten Station */
		MODE_SHORTEST_QUEUE_NEXT_STATION,
		/** K�rzeste Warteschlange an der n�chsten Bedienstation */
		MODE_SHORTEST_QUEUE_PROCESS_STATION,
		/** Wenigster Kunden an der n�chsten Station */
		MODE_MIN_CLIENTS_NEXT_STATION,
		/** Wenigster Kunden an der n�chsten Bedienstation */
		MODE_MIN_CLIENTS_PROCESS_STATION,
		/** Gem�� Wert eines Kundendaten-Textfeldes */
		MODE_KEY_VALUE
	}

	private final List<ModelElementEdge> connectionsIn;
	private final List<ModelElementEdge> connectionsOut;

	/* Wird nur beim Laden und Clonen verwendet. */
	private List<Integer> connectionsInIds=null;
	private List<Integer> connectionsOutIds=null;

	private DecideMode mode=DecideMode.MODE_CHANCE;

	private String key;
	private final Map<Integer,Double> rates;
	private final Map<Integer,String> conditions;
	private final List<String> values;
	private final List<String> clientTypes;

	private List<String> newClientTypes=null;

	/**
	 * Konstruktor der Klasse <code>ModelElementDecide</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementDecide(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_OCTAGON);
		connectionsIn=new ArrayList<>();
		connectionsOut=new ArrayList<>();
		key="";
		rates=new HashMap<>();
		conditions=new HashMap<>();
		values=new ArrayList<>();
		clientTypes=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DECIDE.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Decide.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements �ndert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		super.fireChanged();
	}

	private String getNewClientType(final int index) {
		if (index<0 || newClientTypes==null || newClientTypes.size()<=index) return "";
		final String newClientType=newClientTypes.get(index).trim();
		if (newClientType.isEmpty()) return "";
		return Language.tr("Surface.Duplicate.NewClientType")+": "+newClientType;
	}

	private void updateEdgeLabel() {
		if (connectionsOut==null) return;

		double sum=0;
		if (mode==DecideMode.MODE_CHANCE) {
			for (ModelElementEdge connection: connectionsOut) {
				final int id=connection.getId();
				Double rate=rates.get(id);
				if (rate==null) rate=1.0;
				if (rate<0) rate=0.0;
				rates.put(id,rate);
				sum+=rate;
			}
			if (sum==0) sum=1;
		}

		for (int i=0;i<connectionsOut.size();i++) {
			final ModelElementEdge connection=connectionsOut.get(i);
			String name="";
			String s;
			switch (mode) {
			case MODE_CHANCE:
				final int id=connection.getId();
				final Double rate=rates.get(id);
				name=Language.tr("Surface.Decide.Rate")+" "+NumberTools.formatNumber(rate)+" ("+NumberTools.formatPercent(rate/sum)+")";
				break;
			case MODE_CONDITION:
				name=(i<connectionsOut.size()-1)?(Language.tr("Surface.Decide.Condition")+" "+(i+1)):Language.tr("Surface.Decide.Condition.ElseCase");
				break;
			case MODE_CLIENTTYPE:
				s=(i>=clientTypes.size())?Language.tr("Dialog.Title.Error").toUpperCase():clientTypes.get(i);
				name=(i<connectionsOut.size()-1)?s:Language.tr("Surface.Decide.AllOtherClientTypes");
				break;
			case MODE_SEQUENCE:
				name=String.format(Language.tr("Surface.Decide.SequenceNumber"),i+1);
				break;
			case MODE_SHORTEST_QUEUE_NEXT_STATION:
				name="";
				break;
			case MODE_SHORTEST_QUEUE_PROCESS_STATION:
				name="";
				break;
			case MODE_MIN_CLIENTS_NEXT_STATION:
				name="";
				break;
			case MODE_MIN_CLIENTS_PROCESS_STATION:
				name="";
				break;
			case MODE_KEY_VALUE:
				s=(i>=values.size())?Language.tr("Dialog.Title.Error").toUpperCase():(key+"="+values.get(i));
				name=(i<connectionsOut.size()-1)?s:Language.tr("Surface.Decide.AllOtherValues");
				break;
			}

			final String newClientType=getNewClientType(i);
			if (!newClientType.isEmpty()) name=name+", "+newClientType;
			connection.setName(name);
		}
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementDecide)) return false;

		final List<ModelElementEdge> connectionsIn2=((ModelElementDecide)element).connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		final List<ModelElementEdge> connectionsOut2=((ModelElementDecide)element).connectionsOut;
		if (connectionsOut==null || connectionsOut2==null || connectionsOut.size()!=connectionsOut2.size()) return false;
		for (int i=0;i<connectionsOut.size();i++) if (connectionsOut.get(i).getId()!=connectionsOut2.get(i).getId()) return false;

		if (((ModelElementDecide)element).mode!=mode) return false;

		switch (mode) {
		case MODE_CHANCE:
			Map<Integer,Double> rates2=((ModelElementDecide)element).rates;
			for (ModelElementEdge connection: connectionsOut) {
				final int id=connection.getId();
				final Double rate1=rates.get(id);
				final Double rate2=rates2.get(id);
				if (rate1==null && rate2==null) continue;
				if (rate1==null || rate2==null) return false;
				final double d1=rate1;
				final double d2=rate2;
				if (d1!=d2) return false;
			}
			break;
		case MODE_CONDITION:
			Map<Integer,String> conditions2=((ModelElementDecide)element).conditions;
			for (int i=0;i<connectionsOut.size()-1;i++) { /* das letzte ist "sonst", daher nur bis <size-1 */
				final int id=connectionsOut.get(i).getId();
				final String c1=conditions.get(id);
				final String c2=conditions2.get(id);
				if (c1==null && c2==null) continue;
				if (c1==null || c2==null) return false;
				if (!c1.equals(c2)) return false;
			}
			break;
		case MODE_CLIENTTYPE:
			List<String> clientTypes2=((ModelElementDecide)element).clientTypes;
			for (int i=0;i<connectionsOut.size()-1;i++) { /* das letzte ist "sonst", daher nur bis <size-1 */
				if (i>=clientTypes.size() && i>=clientTypes2.size()) continue;
				if (i>=clientTypes.size() || i>=clientTypes2.size()) return false;
				if (!clientTypes.get(i).equals(clientTypes2.get(i))) return false;
			}
			break;
		case MODE_SEQUENCE:
			/* immer alles ok */
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			/* immer alles ok */
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			/* immer alles ok */
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			/* immer alles ok */
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			/* immer alles ok */
			break;
		case MODE_KEY_VALUE:
			if (!key.equals(((ModelElementDecide)element).key)) return false;
			List<String> values2=((ModelElementDecide)element).values;
			for (int i=0;i<connectionsOut.size()-1;i++) { /* das letzte ist "sonst", daher nur bis <size-1 */
				if (i>=values.size() && i>=values2.size()) continue;
				if (i>=values.size() || i>=values2.size()) return false;
				if (!values.get(i).equals(values2.get(i))) return false;
			}
			break;
		}

		if (connectionsOut.size()>0) {
			final List<String> newClientTypes2=((ModelElementDecide)element).newClientTypes;
			if (newClientTypes==null && newClientTypes2!=null) return false;
			if (newClientTypes!=null && newClientTypes2==null) return false;
			if (newClientTypes!=null && newClientTypes2!=null) {
				for (int i=0;i<connectionsOut.size();i++) {
					String name1="";
					String name2="";
					if (newClientTypes.size()>i) name1=newClientTypes.get(i);
					if (newClientTypes2.size()>i) name2=newClientTypes2.get(i);
					if (!name1.equals(name2)) return false;
				}
			}
		}

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDecide) {

			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=((ModelElementDecide)element).connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}

			connectionsOut.clear();
			final List<ModelElementEdge> connectionsOut2=((ModelElementDecide)element).connectionsOut;
			if (connectionsOut2!=null) {
				connectionsOutIds=new ArrayList<>();
				for (int i=0;i<connectionsOut2.size();i++) connectionsOutIds.add(connectionsOut2.get(i).getId());
			}

			mode=((ModelElementDecide)element).mode;

			switch (mode) {
			case MODE_CHANCE:
				for (Map.Entry<Integer,Double> entry: ((ModelElementDecide)element).rates.entrySet()) rates.put(entry.getKey(),entry.getValue());
				break;
			case MODE_CONDITION:
				for (Map.Entry<Integer,String> entry: ((ModelElementDecide)element).conditions.entrySet()) conditions.put(entry.getKey(),entry.getValue());
				break;
			case MODE_CLIENTTYPE:
				clientTypes.addAll(((ModelElementDecide)element).clientTypes);
				break;
			case MODE_SEQUENCE:
				/* nichts zu kopieren */
				break;
			case MODE_SHORTEST_QUEUE_NEXT_STATION:
				/* nichts zu kopieren */
				break;
			case MODE_SHORTEST_QUEUE_PROCESS_STATION:
				/* nichts zu kopieren */
				break;
			case MODE_MIN_CLIENTS_NEXT_STATION:
				/* nichts zu kopieren */
				break;
			case MODE_MIN_CLIENTS_PROCESS_STATION:
				/* nichts zu kopieren */
				break;
			case MODE_KEY_VALUE:
				key=((ModelElementDecide)element).key;
				values.addAll(((ModelElementDecide)element).values);
				break;
			}

			if (((ModelElementDecide)element).newClientTypes!=null) newClientTypes=new ArrayList<>(((ModelElementDecide)element).newClientTypes);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDecide clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDecide element=new ModelElementDecide(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	@Override
	public void initAfterLoadOrClone() {
		super.initAfterLoadOrClone();

		ModelElement element;

		if (connectionsInIds!=null) {
			for (int i=0;i<connectionsInIds.size();i++) {
				element=surface.getById(connectionsInIds.get(i));
				if (element instanceof ModelElementEdge) connectionsIn.add((ModelElementEdge)element);
			}
			connectionsInIds=null;
			updateEdgeLabel();
		}

		if (connectionsOutIds!=null) {
			for (int i=0;i<connectionsOutIds.size();i++) {
				element=surface.getById(connectionsOutIds.get(i));
				if (element instanceof ModelElementEdge) connectionsOut.add((ModelElementEdge)element);
			}
			connectionsOutIds=null;
			updateEdgeLabel();
		}

		if (newClientTypes!=null) {
			while (newClientTypes.size()>connectionsOut.size()) newClientTypes.remove(newClientTypes.size()-1);
			while (newClientTypes.size()<connectionsOut.size()) newClientTypes.add("");
			updateEdgeLabel();
		}
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Decide.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Decide.Name.Short");
	}

	/**
	 * Liefert optional eine zus�tzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zus�tzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null) return null; /* keinen Untertitel in der Templates-Liste anzeigen */
		switch (mode) {
		case MODE_CHANCE: return Language.tr("Surface.Decide.ByChance");
		case MODE_CONDITION: return Language.tr("Surface.Decide.ByCondition");
		case MODE_CLIENTTYPE: return Language.tr("Surface.Decide.ByClientType");
		case MODE_SEQUENCE: return Language.tr("Surface.Decide.BySequence");
		case MODE_SHORTEST_QUEUE_NEXT_STATION: return Language.tr("Surface.Decide.ByQueueLength");
		case MODE_SHORTEST_QUEUE_PROCESS_STATION: return Language.tr("Surface.Decide.ByQueueLength");
		case MODE_MIN_CLIENTS_NEXT_STATION: return Language.tr("Surface.Decide.ByClientsAtStation");
		case MODE_MIN_CLIENTS_PROCESS_STATION: return Language.tr("Surface.Decide.ByClientsAtStation");
		case MODE_KEY_VALUE: return Language.tr("Surface.Decide.ByStringProperty");
		default: return null;
		}
	}

	private static final Color defaultBackgroundColor=new Color(204,99,255);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementDecideDialog(owner,ModelElementDecide.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsDecide(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final boolean readOnly) {
		JMenuItem item;
		final URL imgURL=Images.EDIT_EDGES_DELETE.getURL();
		boolean needSeparator=false;

		if (connectionsIn!=null && connectionsIn.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesIn")));
			item.addActionListener((e)->{
				for (ModelElementEdge element : new ArrayList<>(connectionsIn)) surface.remove(element);
			});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}

		if (connectionsOut!=null && connectionsOut.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesOut")));
			item.addActionListener((e)->{
				for (ModelElementEdge element : new ArrayList<>(connectionsOut)) surface.remove(element);
			});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}

		if (needSeparator) popupMenu.addSeparator();
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionsIn!=null) {
			while (connectionsIn.size()>0) {
				ModelElement element=connectionsIn.remove(0);
				surface.remove(element);
			}
		}
		if (connectionsOut!=null) {
			while (connectionsOut.size()>0) {
				ModelElement element=connectionsOut.remove(0);
				surface.remove(element);
			}
		}
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionsIn!=null && connectionsIn.indexOf(element)>=0) {connectionsIn.remove(element); fireChanged();}
		if (connectionsOut!=null && connectionsOut.indexOf(element)>=0) {connectionsOut.remove(element); fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Decide.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Decide.XML.Mode")));
		switch (mode) {
		case MODE_CHANCE: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.ByChance")); break;
		case MODE_CONDITION: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.ByCondition")); break;
		case MODE_CLIENTTYPE: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.ByClientType")); break;
		case MODE_SEQUENCE: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.BySequence")); break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.ByQueueLengthNext")); break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.ByQueueLengthProcess")); break;
		case MODE_MIN_CLIENTS_NEXT_STATION: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.ByClientsAtStationNext")); break;
		case MODE_MIN_CLIENTS_PROCESS_STATION: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.ByClientsAtStationProcess")); break;
		case MODE_KEY_VALUE: sub.setTextContent(Language.trPrimary("Surface.Decide.XML.Mode.ByStringProperty")); break;
		}

		if (mode==DecideMode.MODE_KEY_VALUE) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Decide.XML.Key")));
			sub.setTextContent(key);
		}

		if (connectionsIn!=null) for (ModelElementEdge element: connectionsIn) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		if (connectionsOut!=null) for (int i=0;i<connectionsOut.size();i++) {
			ModelElementEdge element=connectionsOut.get(i);
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Out"));
			if (newClientTypes!=null && newClientTypes.size()>i && !newClientTypes.get(i).trim().isEmpty()) sub.setAttribute(Language.trPrimary("Surface.XML.Connection.NewClientType"),newClientTypes.get(i).trim());
			switch (mode) {
			case MODE_CHANCE:
				Double rate=rates.get(element.getId());
				if (rate==null) rate=1.0;
				if (rate<0) rate=0.0;
				sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.Rate"),NumberTools.formatSystemNumber(rate));
				break;
			case MODE_CONDITION:
				if (i<connectionsOut.size()-1) {
					String condition=conditions.get(element.getId());
					if (condition==null) condition="";
					sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.Condition"),condition);
				}
				break;
			case MODE_CLIENTTYPE:
				if (i<connectionsOut.size()-1) {
					String clientType=(i>=clientTypes.size())?"":clientTypes.get(i);
					sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.ClientType"),clientType);
				}
				break;
			case MODE_SEQUENCE:
				/* nichts zu speichern */
				break;
			case MODE_SHORTEST_QUEUE_NEXT_STATION:
				/* nichts zu speichern */
				break;
			case MODE_SHORTEST_QUEUE_PROCESS_STATION:
				/* nichts zu speichern */
				break;
			case MODE_MIN_CLIENTS_NEXT_STATION:
				/* nichts zu speichern */
				break;
			case MODE_MIN_CLIENTS_PROCESS_STATION:
				/* nichts zu speichern */
				break;
			case MODE_KEY_VALUE:
				if (i<connectionsOut.size()-1) {
					String value=(i>=values.size())?"":values.get(i);
					sub.setAttribute(Language.trPrimary("Surface.Decide.XML.Connection.Value"),value);
				}
				break;
			}
		}
	}

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.Decide.XML.Mode",name)) {
			boolean ok=false;
			if (Language.trAll("Surface.Decide.XML.Mode.ByChance",content)) {mode=DecideMode.MODE_CHANCE; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByCondition",content)) {mode=DecideMode.MODE_CONDITION; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByClientType",content)) {mode=DecideMode.MODE_CLIENTTYPE; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.BySequence",content)) {mode=DecideMode.MODE_SEQUENCE; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthNext",content)) {mode=DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByQueueLengthProcess",content)) {mode=DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationNext",content)) {mode=DecideMode.MODE_MIN_CLIENTS_NEXT_STATION; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByClientsAtStationProcess",content)) {mode=DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION; ok=true;}
			if (Language.trAll("Surface.Decide.XML.Mode.ByStringProperty",content)) {mode=DecideMode.MODE_KEY_VALUE; ok=true;}
			if (!ok) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.Decide.XML.Key",name)) {
			key=content;
			return null;
		}

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			final String s=Language.trAllAttribute("Surface.XML.Connection.Type",node);
			if (Language.trAll("Surface.XML.Connection.Type.In",s)) {
				if (connectionsInIds==null) connectionsInIds=new ArrayList<>();
				connectionsInIds.add(I);
			}
			if (Language.trAll("Surface.XML.Connection.Type.Out",s)) {
				if (connectionsOutIds==null) connectionsOutIds=new ArrayList<>();
				connectionsOutIds.add(I);

				final String newClientType=Language.trAllAttribute("Surface.XML.Connection.NewClientType",node);
				if (!newClientType.trim().isEmpty()) {
					if (newClientTypes==null) newClientTypes=new ArrayList<>();
					while (newClientTypes.size()<connectionsOutIds.size()-1) newClientTypes.add("");
					newClientTypes.add(newClientType);
				}

				/* Chance */
				final String rateString=Language.trAllAttribute("Surface.Decide.XML.Connection.Rate",node);
				if (!rateString.isEmpty()) {
					Double rate=NumberTools.getNotNegativeDouble(rateString);
					if (rate==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Decide.XML.Connection.Rate"),name,node.getParentNode().getNodeName());
					rates.put(I,rate);
				}

				/* Condition */
				final String condition=Language.trAllAttribute("Surface.Decide.XML.Connection.Condition",node);
				if (!condition.isEmpty()) conditions.put(I,condition);

				/* ClientType */
				clientTypes.add(Language.trAllAttribute("Surface.Decide.XML.Connection.ClientType",node));

				/* Key=Value */
				values.add(Language.trAllAttribute("Surface.Decide.XML.Connection.Value",node));
			}
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		return true;
	}

	/**
	 * F�gt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzuf�gende Kante
	 * @return	Gibt <code>true</code> zur�ck, wenn die einlaufende Kante hinzugef�gt werden konnte.
	 */
	@Override
	public boolean addEdgeIn(ModelElementEdge edge) {
		if (edge!=null && connectionsIn.indexOf(edge)<0 && connectionsOut.indexOf(edge)<0) {
			connectionsIn.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Einlaufende Kanten
	 * @return Einlaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		return connectionsIn.toArray(new ModelElementEdge[0]);
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return true;
	}

	/**
	 * F�gt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzuf�gende Kante
	 * @return	Gibt <code>true</code> zur�ck, wenn die auslaufende Kante hinzugef�gt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (edge!=null && connectionsIn.indexOf(edge)<0 && connectionsOut.indexOf(edge)<0) {
			connectionsOut.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Auslaufende Kanten
	 * @return	Auslaufenden Kante
	 */
	@Override
	public ModelElementEdge[] getEdgesOut() {
		return connectionsOut.toArray(new ModelElementEdge[0]);
	}

	/**
	 * Gibt an, ob die Verzweigung zuf�llig (gem�� vorgegebenen Raten), nach Bedingungen oder nach Kundentypen erfolgen soll
	 * @return	Verzweigungsmodus
	 * @see DecideMode
	 */
	public DecideMode getMode() {
		if (mode==null) return DecideMode.MODE_CHANCE;
		return mode;
	}

	/**
	 * Stellt den Verzweigungsmodus (zuf�llig gem�� vorgegebenen Raten, nach Bedingungen oder nach Kundentypen) ein.
	 * @param mode	Verzweigungsmodus
	 */
	public void setMode(final DecideMode mode) {
		if (mode==null) this.mode=DecideMode.MODE_CHANCE; else this.mode=mode;
		fireChanged();
	}

	/**
	 * Liefert die Raten, mit denen die Kunden zu den einzelnen Zielstationen der auslaufenden Kanten weitergeleitet werden.
	 * @return	Maps, die den Stations-IDs die jeweiligen Raten zuordnet.
	 */
	public Map<Integer,Double> getRates() {
		return rates;
	}

	/**
	 * Liefert die Bedingungen, mit denen die Kunden zu den einzelnen Zielstationen der auslaufenden Kanten weitergeleitet werden.
	 * @return	Maps, die den Stations-IDs die jeweiligen Bedingungen zuordnet.
	 */
	public Map<Integer,String> getConditions() {
		return conditions;
	}

	/**
	 * Liefert eine Liste aller Kundentypen-Verzweigungen
	 * @return	Liste der Namen der Kundentypen f�r die Verzweigungen
	 */
	public List<String> getClientTypes() {
		return clientTypes;
	}

	/**
	 * Erfolgt die Verzweigung im Key-Value-Modus, so liefert diese Funktion den Schl�ssel
	 * @return	Schl�ssel gem�� dessen Werten die Verzweigung erfolgen soll
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Erfolgt die Verzweigung im Key-Value-Modus, so kann �ber diese Funktion der Schl�ssel eingestellt werden
	 * @param key	Schl�ssel gem�� dessen Werten die Verzweigung erfolgen soll
	 */
	public void setKey(final String key) {
		if (key!=null) this.key=key;
		fireChanged();
	}

	/**
	 * Werte anhand denen die Verzweigung erfolgen soll
	 * @return	Verzweigungswerte
	 */
	public List<String> getValues() {
		return values;
	}

	/**
	 * Liefert die Liste der Namen der neuen Kundentypen, die bei Weiterleitung �ber die verschiedenen Ausgangskanten zugewiesen werden sollen.
	 * @return	Liste mit neuen Kundentypen (L�nge entspricht der Anzahl an Ausgangskanten; leere Strings stehen f�r "keine �nderung"; R�ckgabewert ist nie <code>null</code> und Eintr�ge sind ebenfalls nie <code>null</code>)
	 */
	public List<String> getChangedClientTypes() {
		final List<String> result=new ArrayList<>();
		if (newClientTypes!=null) for (int i=0;i<Math.min(newClientTypes.size(),connectionsOut.size());i++) result.add(newClientTypes.get(i).trim());
		while (result.size()<connectionsOut.size()) result.add("");
		return result;
	}

	/**
	 * Stellt die Namen der neuen Kundentypen, die bei Weiterleitung �ber die verschiedenen Ausgangskanten zugewiesen werden sollen, ein.
	 * @param changedClientTypes	Liste mit neuen Kundentypen (leere Strings stehen f�r "keine �nderung")
	 */
	public void setChangedClientTypes(final List<String> changedClientTypes) {
		if (newClientTypes==null) newClientTypes=new ArrayList<>();
		newClientTypes.clear();
		if (changedClientTypes!=null) for (int i=0;i<Math.min(changedClientTypes.size(),connectionsOut.size());i++) {
			if (changedClientTypes.get(i)==null) newClientTypes.add(""); else  newClientTypes.add(changedClientTypes.get(i).trim());
		}
		while (newClientTypes.size()<connectionsOut.size()) newClientTypes.add("");
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation �berhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung �bersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation ber�cksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden m�ssten).
	 * @return	Gibt <code>true</code> zur�ck, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	@Override
	public boolean inputConnected() {
		return connectionsIn.size()>0;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (!isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) return;
		if (mode!=DecideMode.MODE_CLIENTTYPE) return;

		for (int i=0;i<clientTypes.size();i++) if (clientTypes.get(i).equals(oldName)) {
			clientTypes.set(i,newName);
			updateEdgeLabel();
		}

		if (newClientTypes!=null) for (int i=0;i<newClientTypes.size();i++) if (newClientTypes.get(i).equals(oldName)) {
			newClientTypes.set(i,newName);
			updateEdgeLabel();
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementDecide";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		switch (mode) {
		case MODE_CHANCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Rate"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String edgeDescription=String.format(Language.tr("ModelDescription.Decide.Rate"),NumberTools.formatNumber(rates.get(edge.getId())));
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(edgeDescription+newClientType,edge);
			}
			break;
		case MODE_CONDITION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Condition"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String edgeDescription;
				if (i<connectionsOut.size()-1) {
					edgeDescription=String.format(Language.tr("ModelDescription.Decide.Condition"),conditions.get(edge.getId()));
				} else {
					edgeDescription=Language.tr("ModelDescription.Decide.Condition.Else");
				}
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(edgeDescription+newClientType,edge);
			}
			break;
		case MODE_CLIENTTYPE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ClientType"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String edgeDescription;
				if (i<connectionsOut.size()-1) {
					final String s=(i<clientTypes.size())?clientTypes.get(i):"";
					edgeDescription=String.format(Language.tr("ModelDescription.Decide.ClientType"),s);
				} else {
					edgeDescription=Language.tr("ModelDescription.Decide.ClientType.Else");
				}
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(edgeDescription+newClientType,edge);
			}
			break;
		case MODE_SEQUENCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Sequence"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ShortestQueueNextStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ShortestQueueNextProcessStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LeastClientsNextStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LeastClientsNextProcessStation"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.NextElement")+newClientType,edge);
			}
			break;
		case MODE_KEY_VALUE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.StringProperty"),1000);
			for (int i=0;i<connectionsOut.size();i++) {
				final ModelElementEdge edge=connectionsOut.get(i);
				final String edgeDescription;
				if (i<connectionsOut.size()-1) {
					final String s=(i<values.size())?values.get(i):"";
					edgeDescription=String.format(Language.tr("ModelDescription.Decide.StringProperty"),key,s);
				} else {
					edgeDescription=Language.tr("ModelDescription.Decide.StringProperty.Else");
				}
				String newClientType=getNewClientType(i); if (!newClientType.isEmpty()) newClientType=", "+newClientType;
				descriptionBuilder.addConditionalEdgeOut(edgeDescription+newClientType,edge);
			}
			break;
		}
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		this.connectionsIn.clear();
		this.connectionsIn.addAll(connectionsIn);

		final List<Integer> oldIDs=this.connectionsOut.stream().map(edge->edge.getId()).collect(Collectors.toList());
		final List<Integer> newIDs=connectionsOut.stream().map(edge->edge.getId()).collect(Collectors.toList());

		final Map<Integer,Double> newRates=new HashMap<>();
		final Map<Integer,String> newConditions=new HashMap<>();

		for (Map.Entry<Integer,Double> entry: rates.entrySet()) {
			final int index=oldIDs.indexOf(entry.getKey());
			final Integer ID=(index<0)?entry.getKey():newIDs.get(index);
			newRates.put(ID,entry.getValue());
		}
		rates.clear();
		rates.putAll(newRates);

		for (Map.Entry<Integer,String> entry: conditions.entrySet()) {
			final int index=oldIDs.indexOf(entry.getKey());
			final Integer ID=(index<0)?entry.getKey():newIDs.get(index);
			newConditions.put(ID,entry.getValue());
		}
		conditions.clear();
		conditions.putAll(newConditions);

		this.connectionsOut.clear();
		this.connectionsOut.addAll(connectionsOut);

		return true;
	}

	@Override
	public String[] getNewClientTypes() {
		final Set<String> set=new HashSet<>();
		if (newClientTypes!=null) for (String newClientType: newClientTypes) set.add(newClientType);
		return set.toArray(new String[0]);
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.duplicate,fixer);
	}
}