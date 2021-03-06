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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.elements.RunElementTeleportSource;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementDecide}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDecide
 */
public final class ModelElementDecideDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = -1706008891307759220L;

	private JComboBox<String> modeSelect;
	private JPanel contentCards;

	/* Zufall */
	private List<JTextField> rates;

	/* Bedingung */
	private List<JTextField> conditions;

	/* Kundentyp */
	private List<JComboBox<String>> clientTypes;

	/* Key */
	private JTextField key;

	/* Values */
	private List<JTextField> values;

	/* Optional neue Kundentypen */
	private NewClientTypesPanel newClientTypes;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDecide}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	public ModelElementDecideDialog(final Component owner, final ModelElementDecide element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Decide.Dialog.Title"),element,"ModelElementDecide",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	private String getDestination(ModelElementEdge edge) {
		while (true) {
			if (edge==null) return null;
			final ModelElement edgeEnd=edge.getConnectionEnd();
			if (edgeEnd==null) return null;

			if (edgeEnd instanceof ModelElementVertex) {
				edge=((ModelElementVertex)edgeEnd).getEdgeOut();
				continue;
			}

			if (edgeEnd instanceof ModelElementTeleportSource) {
				final ModelElementTeleportDestination destination=RunElementTeleportSource.getDestination(edgeEnd.getModel(),((ModelElementTeleportSource)edgeEnd).getDestination());
				if (destination==null) return null;
				edge=destination.getEdgeOut();
				continue;
			}


			String name;
			if (edgeEnd instanceof ModelElementBox) {
				name=((ModelElementBox)edgeEnd).getTypeName();
			} else {
				name=edgeEnd.getName();
			}

			return name+" (id="+edgeEnd.getId()+")";
		}
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDecide;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JTabbedPane tabs=new JTabbedPane();

		if (!(element instanceof ModelElementDecide)) return tabs;
		ModelElementDecide decide=(ModelElementDecide)element;

		/* Verzweigungsrichtungen */

		final JPanel main=new JPanel(new BorderLayout());
		tabs.addTab(Language.tr("Surface.Decide.Dialog.Directions"),main);

		JPanel sub;
		JLabel label;

		main.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Decide.Dialog.DecideBy")+":"));
		sub.add(modeSelect=new JComboBox<>(new String[]{
				Language.tr("Surface.Decide.Dialog.DecideBy.Chance"),
				Language.tr("Surface.Decide.Dialog.DecideBy.Condition"),
				Language.tr("Surface.Decide.Dialog.DecideBy.ClientType"),
				Language.tr("Surface.Decide.Dialog.DecideBy.Sequence"),
				Language.tr("Surface.Decide.Dialog.DecideBy.ShortestQueueNextStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.ShortestQueueNextProcessStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.LeastClientsNextStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.LeastClientsNextProcessStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.StringProperty")
		}));
		modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_CHANCE,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_CONDITION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_CLIENT_TYPE,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_SEQUENCE,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_SHORTEST_QUEUE_NEXT_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_SHORTEST_QUEUE_NEXT_PROCESS_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_LEAST_CLIENTS_NEXT_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_LEAST_CLIENTS_NEXT_PROCESS_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_TEXT_PROPERTY
		}));
		modeSelect.setEnabled(!readOnly);
		label.setLabelFor(modeSelect);
		modeSelect.addActionListener((e)->setActiveCard((String)modeSelect.getSelectedItem()));

		main.add(contentCards=new JPanel(new CardLayout()),BorderLayout.CENTER);
		JPanel content;

		final ModelElementEdge[] edges=decide.getEdgesOut();

		/* Seite "Zufall" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(0));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		rates=new ArrayList<>();
		for (int i=0;i<edges.length;i++) {
			String name=Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1);
			String destination=getDestination(edges[i]);
			if (destination!=null) name+=" zu "+destination;

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			label=new JLabel(name); labelPanel.add(label);

			Double D=decide.getRates().get(edges[i].getId());
			if (D==null) D=1.0;
			if (D<0) D=0.0;
			final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate")+":",NumberTools.formatNumber(D),10);
			option.add((JPanel)data[0],BorderLayout.CENTER);
			final JTextField input=(JTextField)data[1];
			input.setEditable(!readOnly);
			input.addKeyListener(new KeyListener(){
				@Override public void keyTyped(KeyEvent e) {getRates(false);}
				@Override public void keyPressed(KeyEvent e) {getRates(false);}
				@Override public void keyReleased(KeyEvent e) {getRates(false);}
			});

			rates.add(input);

		}

		/* Seite "Bedingung" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(1));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		conditions=new ArrayList<>();
		for (int i=0;i<edges.length;i++) {
			String name=Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1);
			String destination=getDestination(edges[i]);
			if (destination!=null) name+=" zu "+destination;

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			label=new JLabel(name); labelPanel.add(label);

			final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Condition")+":","");
			final JPanel inputPanel=(JPanel)data[0];
			option.add(inputPanel,BorderLayout.CENTER);
			final JTextField input=(JTextField)data[1];
			inputPanel.add(getExpressionEditButton(this,input,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

			if (i==edges.length-1) {
				input.setEditable(false);
				input.setText(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Condition.Else"));
			} else {
				input.setEditable(!readOnly);
				String condition=decide.getConditions().get(edges[i].getId());
				if (condition==null) condition="";
				input.setText(condition);
				input.addKeyListener(new KeyListener(){
					@Override public void keyTyped(KeyEvent e) {getConditions(false);}
					@Override public void keyPressed(KeyEvent e) {getConditions(false);}
					@Override public void keyReleased(KeyEvent e) {getConditions(false);}
				});
			}

			conditions.add(input);
		}

		/* Seite "Kundentyp" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(2));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		clientTypes=new ArrayList<>();
		List<String> allClientTypesList=element.getSurface().getClientTypes();
		final List<String> typesList=decide.getClientTypes();
		for (int i=0;i<edges.length;i++) {
			String name=Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1);
			String destination=getDestination(edges[i]);
			if (destination!=null) name+=" zu "+destination;

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			label=new JLabel(name); labelPanel.add(label);

			final JPanel inputPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(inputPanel,BorderLayout.CENTER);
			inputPanel.add(new JLabel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType")+":"));

			String[] items;
			if (i==edges.length-1) {
				items=new String[]{Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.Else")};
			} else {
				items=allClientTypesList.toArray(new String[0]);
			}
			JComboBox<String> input=new JComboBox<String>(items);
			if (i!=edges.length-1) {
				input.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildClientTypeIcons(items,element.getModel())));
			}
			clientTypes.add(input);
			inputPanel.add(input);
			label.setLabelFor(input);

			if (i==edges.length-1) {
				input.setEnabled(false);
				input.setSelectedIndex(0);
			} else {
				input.setEnabled(!readOnly);
				if (typesList.size()>i) {
					int index=allClientTypesList.indexOf(typesList.get(i));
					if (index>=0) input.setSelectedIndex(index);
				}
			}
		}

		/* Seite "Reihenfolge" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(3));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel(Language.tr("Surface.Decide.Dialog.DecideBy.Sequence.Info")));

		/* Seite "K�rzeste Warteschlange an der n�chsten Station" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(4));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.ShortestQueueNextStation.Info").replaceAll("\\n","<br>")+"</body></html>"));

		/* Seite "K�rzeste Warteschlange an der n�chsten Bedienstation" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(5));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.ShortestQueueNextProcessStation.Info").replaceAll("\\n","<br>")+"</body></html>"));

		/* Seite "Geringste Anzahl an Kunden an der n�chsten Station" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(6));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.LeastClientsNextStation.Info").replaceAll("\\n","<br>")+"</body></html>"));

		/* Seite "Geringste Anzahl an Kunden an der n�chsten Bedienstation" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(7));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.LeastClientsNextProcessStation.Info").replaceAll("\\n","<br>")+"</body></html>"));

		/* Seite "Texteigenschaft" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(8));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		Object[] data=getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Key"),decide.getKey());
		content.add((JPanel)data[0]);
		key=(JTextField)data[1];
		key.setEditable(!readOnly);
		key.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {getCheckKeyValues(false);}
			@Override public void keyPressed(KeyEvent e) {getCheckKeyValues(false);}
			@Override public void keyReleased(KeyEvent e) {getCheckKeyValues(false);}
		});

		values=new ArrayList<>();
		final List<String> valuesList=decide.getValues();
		for (int i=0;i<edges.length;i++) {
			String name=Language.tr("Surface.Decide.Dialog.OutgoingEdge")+" "+(i+1);
			String destination=getDestination(edges[i]);
			if (destination!=null) name+=" zu "+destination;

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			label=new JLabel(name); labelPanel.add(label);

			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Value")+":","");
			option.add((JPanel)data[0],BorderLayout.CENTER);
			final JTextField input=(JTextField)data[1];

			if (i==edges.length-1) {
				input.setEditable(false);
				input.setText(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Value.Else"));
			} else {
				input.setEditable(!readOnly);
				input.setText((i>=valuesList.size())?"":valuesList.get(i));
				input.addKeyListener(new KeyListener(){
					@Override public void keyTyped(KeyEvent e) {getCheckKeyValues(false);}
					@Override public void keyPressed(KeyEvent e) {getCheckKeyValues(false);}
					@Override public void keyReleased(KeyEvent e) {getCheckKeyValues(false);}
				});
			}

			values.add(input);
		}

		/* Aktive Karte einstellen */
		switch (decide.getMode()) {
		case MODE_CHANCE: modeSelect.setSelectedIndex(0); break;
		case MODE_CONDITION: modeSelect.setSelectedIndex(1); break;
		case MODE_CLIENTTYPE: modeSelect.setSelectedIndex(2); break;
		case MODE_SEQUENCE: modeSelect.setSelectedIndex(3); break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION: modeSelect.setSelectedIndex(4); break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION: modeSelect.setSelectedIndex(5); break;
		case MODE_MIN_CLIENTS_NEXT_STATION: modeSelect.setSelectedIndex(6); break;
		case MODE_MIN_CLIENTS_PROCESS_STATION: modeSelect.setSelectedIndex(7); break;
		case MODE_KEY_VALUE: modeSelect.setSelectedIndex(8); break;
		}

		setActiveCard((String)modeSelect.getSelectedItem());

		/* Neue Kundentypen zuweisen */

		tabs.addTab(Language.tr("Surface.Decide.Dialog.NewClientTypes"),newClientTypes=new NewClientTypesPanel(decide.getChangedClientTypes(),readOnly));

		/* Icons f�r Seiten */

		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_DECIDE.getIcon());
		tabs.setIconAt(1,Images.MODELPROPERTIES_CLIENTS.getIcon());

		return tabs;
	}

	private void setActiveCard(final String name) {
		final CardLayout cardLayout=(CardLayout)(contentCards.getLayout());
		cardLayout.show(contentCards,name);
	}

	private List<Double> getRates(final boolean showErrorDialog) {
		List<Double> values=new ArrayList<>();

		double sum=0;
		for (int i=0;i<rates.size();i++) {
			Double D=NumberTools.getNotNegativeDouble(rates.get(i),true);
			if (D==null) {
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate.Error.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate.Error.InfoInvalid"),i+1));

					return null;
				}
				values=null;
			} else {
				sum+=D;
			}
			if (values!=null) values.add(D);
		}
		if (values!=null && rates.size()>0 && sum==0) {
			if (showErrorDialog) MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate.Error.Title"),Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate.Error.InfoAllZero"));
			return null;
		}

		return values;
	}

	private List<String> getConditions(final boolean showErrorDialog) {
		List<String> values=new ArrayList<>();

		for (int i=0;i<conditions.size()-1;i++) {
			final JTextField field=conditions.get(i);
			final String condition=field.getText();
			final int error=ExpressionMultiEval.check(condition,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true));
			if (error>=0) field.setBackground(Color.red); else field.setBackground(SystemColor.text);

			if (error>=0) {
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Condition.Error.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Condition.Error.Info"),i+1,error+1));
					return null;
				}
				values=null;
			}
			if (values!=null) values.add(condition);
		}

		return values;
	}

	private boolean getCheckClientTypes() {
		final List<String> usedClientTypes=new ArrayList<>();

		for (int i=0;i<clientTypes.size()-1;i++) {
			JComboBox<String> comboBox=clientTypes.get(i);
			if (comboBox.getSelectedIndex()<0) {
				MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.ErrorMissing.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.ErrorMissing.Info"),i+1));
				return false;
			}
			String name=(String)comboBox.getSelectedItem();
			if (usedClientTypes.indexOf(name)>=0) {
				MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.ErrorDouble.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.ErrorDouble.Info"),i+1,name));
				return false;
			}
			usedClientTypes.add(name);
		}

		return true;
	}

	private boolean getCheckKeyValues(final boolean showErrorMessages) {
		boolean ok=true;

		if (key.getText().trim().isEmpty()) {
			key.setBackground(Color.red);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Key.ErrorMissing.Title"),Language.tr("Surface.Decide.Dialog.OutgoingEdge.Key.ErrorMissing.Info"));
				return false;
			}
		} else {
			key.setBackground(SystemColor.text);
		}

		for (int i=0;i<values.size()-1;i++) {
			if (values.get(i).getText().trim().isEmpty()) {
				values.get(i).setBackground(Color.RED);
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Value.ErrorMissing.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Value.ErrorMissing.Info"),i+1));
					return false;
				}
			} else {
				values.get(i).setBackground(SystemColor.text);
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu pr�fen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden k�nnen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		switch (modeSelect.getSelectedIndex()) {
		case 0: return getRates(true)!=null;
		case 1: return getConditions(true)!=null;
		case 2: return getCheckClientTypes();
		case 3: return true;
		case 4: return true;
		case 5: return true;
		case 6: return true;
		case 7: return true;
		case 8: return getCheckKeyValues(true);
		default: return false;
		}
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementDecide) {
			final ModelElementDecide decide=(ModelElementDecide)element;

			ModelElementDecide.DecideMode mode=ModelElementDecide.DecideMode.MODE_CHANCE;
			switch (modeSelect.getSelectedIndex()) {
			case 0: mode=ModelElementDecide.DecideMode.MODE_CHANCE; break;
			case 1: mode=ModelElementDecide.DecideMode.MODE_CONDITION; break;
			case 2: mode=ModelElementDecide.DecideMode.MODE_CLIENTTYPE; break;
			case 3: mode=ModelElementDecide.DecideMode.MODE_SEQUENCE; break;
			case 4: mode=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION; break;
			case 5: mode=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION; break;
			case 6: mode=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_NEXT_STATION; break;
			case 7: mode=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION; break;
			case 8: mode=ModelElementDecide.DecideMode.MODE_KEY_VALUE; break;
			}

			decide.setMode(mode);
			final ModelElementEdge[] edges=decide.getEdgesOut();

			switch (mode) {
			case MODE_CHANCE:
				final Map<Integer,Double> ratesMap=decide.getRates();
				ratesMap.clear();
				final List<Double> rates=getRates(false);
				for (int i=0;i<edges.length;i++) ratesMap.put(edges[i].getId(),rates.get(i));
				break;
			case MODE_CONDITION:
				final Map<Integer,String> conditionsMap=decide.getConditions();
				conditionsMap.clear();
				final List<String> c=getConditions(false);
				for (int i=0;i<edges.length-1;i++) conditionsMap.put(edges[i].getId(),c.get(i));
				break;
			case MODE_CLIENTTYPE:
				final List<String> clientTypesList=decide.getClientTypes();
				clientTypesList.clear();
				for (int i=0;i<clientTypes.size()-1;i++) clientTypesList.add((String)clientTypes.get(i).getSelectedItem());
				break;
			case MODE_SEQUENCE:
				/* nichts zur�ck zu schreiben */
				break;
			case MODE_SHORTEST_QUEUE_NEXT_STATION:
				/* nichts zur�ck zu schreiben */
				break;
			case MODE_SHORTEST_QUEUE_PROCESS_STATION:
				/* nichts zur�ck zu schreiben */
				break;
			case MODE_MIN_CLIENTS_NEXT_STATION:
				/* nichts zur�ck zu schreiben */
				break;
			case MODE_MIN_CLIENTS_PROCESS_STATION:
				/* nichts zur�ck zu schreiben */
				break;
			case MODE_KEY_VALUE:
				decide.setKey(key.getText().trim());
				final List<String> valuesList=decide.getValues();
				valuesList.clear();
				for (int i=0;i<values.size()-1;i++) valuesList.add(values.get(i).getText().trim());
				break;
			}

			decide.setChangedClientTypes(newClientTypes.getNewClientTypeNames());
		}
	}
}
