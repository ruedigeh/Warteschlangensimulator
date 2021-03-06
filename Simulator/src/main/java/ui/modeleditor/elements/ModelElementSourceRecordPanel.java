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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.JTableExt;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modelproperties.ModelPropertiesDialog;

/**
 * H�lt die GUI-Elemente f�r die Bearbeitung einer Kundenquelle vor
 * @author Alexander Herzog
 * @see ModelElementSourceDialog
 * @see ModelElementSourceMultiDialog
 */
public final class ModelElementSourceRecordPanel extends JPanel {
	private static final long serialVersionUID = 3050008572741778868L;

	private final boolean hasOwnArrivals;
	private final boolean readOnly;
	private final EditModel model;
	private final ModelSurface surface;
	private final Runnable helpRunnable;
	private ModelElementSourceRecord lastRecord;
	private String lastName;

	private final JPanel namePanel;
	private final JTextField nameEdit;
	private final JButton nameButton;

	private final JTabbedPane tabs;

	private JPanel cards;
	private final JComboBox<String> selectCard;

	private final JComboBox<String> timeBase1;
	private final JDistributionPanel distributionPanel;

	private JComboBox<String> timeBase2;
	private final JTextField expression;

	private final String[] scheduleNames;
	private final JComboBox<String> schedule;

	private final JTextField condition;
	private final JTextField conditionMinDistance;

	private final ModelElementSourceRecordSignalTableModel signalsTableModel;

	private final JTextField thresholdExpression;
	private final JTextField thresholdExpressionValue;
	private final JComboBox<String> thresholdDirection;

	private final JRadioButton optionFixedSize;
	private final JRadioButton optionSizesDistribution;
	private final JTextField batchField;
	private final JButton batchButton;
	private final JRadioButton optionInfinite;
	private final JRadioButton optionFixedNumberArrivals;
	private final JRadioButton optionFixedNumberClients;
	private final JTextField numberFieldArrivals;
	private final JTextField numberFieldClients;
	private JPanel arrivalStartSub1;
	private JPanel arrivalStartSub2;
	private final JTextField arrivalStart;
	private JLabel arrivalStartTimeUnit;

	private double[] batchRates;

	private final JPanel panelNumbers;
	private VariablesTableModel modelNumbers;

	private final JPanel panelText;
	private VariablesTextsTableModel modelText;

	/**
	 * Konstruktor der Klasse <code>ModelElementSourceRecordPanel</code>
	 * @param readOnly	Gibt an, ob die Daten nur angezeigt (<code>true</code>) oder auch bearbeitet weden d�rfen (<code>false</code>)
	 * @param model	Element vom Typ <code>EditModel</code> (wird ben�tigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Zeichenoberfl�che
	 * @param helpRunnable	Hilfe-Runnable
	 * @param hasOwnArrivals	Gibt an, ob diese Quelle von sich aus Kunden generiert (<code>true</code>) oder nur von au�en angesto�en wird (<code>false</code>).
	 */
	public ModelElementSourceRecordPanel(final boolean readOnly, final EditModel model, final ModelSurface surface, final Runnable helpRunnable, final boolean hasOwnArrivals) {
		super();
		this.readOnly=readOnly;
		this.model=model;
		this.surface=surface;
		this.helpRunnable=helpRunnable;
		this.hasOwnArrivals=hasOwnArrivals;
		setLayout(new BorderLayout());

		Object[] data;
		JPanel card,panel,sub;
		JLabel label;
		ButtonGroup buttonGroup;

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.ClientTypeName")+":","");
		add(namePanel=(JPanel)data[0],BorderLayout.NORTH);
		nameEdit=(JTextField)data[1];
		namePanel.setVisible(false);
		nameEdit.setEditable(!readOnly);
		nameEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		final JPanel buttons=new JPanel(new FlowLayout(FlowLayout.LEFT));
		namePanel.add(buttons,BorderLayout.EAST);
		buttons.add(nameButton=new JButton());
		nameButton.setToolTipText(Language.tr("Surface.Source.Dialog.ClientTypeSettings"));
		nameButton.addActionListener(e->editClientData());

		add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		JPanel tab;

		tab=new JPanel(new BorderLayout(0,5));
		if (hasOwnArrivals) tabs.add(Language.tr("Surface.Source.Dialog.Tab.InterArrivalTimes"),tab);

		/* Karten */

		tab.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);

		panel.add(label=new JLabel(Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes")+":"));
		panel.add(selectCard=new JComboBox<>(new String[]{
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Distribution"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Expression"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Schedule"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Condition"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Threshold"),
				Language.tr("Surface.Source.Dialog.CalculationOfTheInterarrivalTimes.Signals"),
		}));
		selectCard.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_DISTRIBUTION,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_EXPRESSION,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_SCHEDULE,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_CONDITION,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_THRESHOLD,
				Images.MODELEDITOR_ELEMENT_SOURCE_MODE_SIGNALS
		}));
		label.setLabelFor(selectCard);
		selectCard.addActionListener((e)->{
			final int index=selectCard.getSelectedIndex();
			((CardLayout)cards.getLayout()).show(cards,"Seite"+(index+1));
			if (arrivalStartSub1!=null) arrivalStartSub1.setVisible(index!=2 && index!=3 && index!=5);
			if (arrivalStartSub2!=null) arrivalStartSub2.setVisible(index!=2 && index!=3 && index!=5);
			checkData(false);
		});
		selectCard.setEnabled(!readOnly);

		tab.add(cards=new JPanel(new CardLayout()),BorderLayout.CENTER);

		/* Karte: Verteilung */

		cards.add(card=new JPanel(new BorderLayout()),"Seite1");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Distribution")+":</b></html>"));
		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(label=new JLabel(Language.tr("Surface.Source.Dialog.TimeBase")+":"));
		sub.add(timeBase1=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBase1.setEnabled(!readOnly);
		label.setLabelFor(timeBase1);
		timeBase1.addActionListener((e)->{
			timeBase2.setSelectedIndex(timeBase1.getSelectedIndex());
			arrivalStartTimeUnit.setText((String)timeBase1.getSelectedItem());
			updateTabTitle();
		});
		card.add(distributionPanel=new JDistributionPanel(new ExponentialDistribution(null,100,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY),3600,!readOnly),BorderLayout.CENTER);

		/* Karte: Ausdruck */

		cards.add(card=new JPanel(new BorderLayout()),"Seite2");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Expression")+":</b></html>"));
		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(label=new JLabel(Language.tr("Surface.Source.Dialog.TimeBase")+":"));
		sub.add(timeBase2=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBase2.setEnabled(!readOnly);
		label.setLabelFor(timeBase2);
		timeBase2.addActionListener((e)->{
			timeBase1.setSelectedIndex(timeBase2.getSelectedIndex());
			arrivalStartTimeUnit.setText((String)timeBase2.getSelectedItem());
			updateTabTitle();
		});
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Expression.Expression")+":","");
		sub=(JPanel)data[0];
		panel.add(sub);
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],false,true,model,surface),BorderLayout.EAST);
		expression=(JTextField)data[1];
		expression.setEditable(!readOnly);
		expression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Karte: Zeitplan */

		cards.add(card=new JPanel(new BorderLayout()),"Seite3");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Schedule")+":</b></html>"));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(label=new JLabel(Language.tr("Surface.Source.Dialog.Schedule.Schedule")+":"));
		scheduleNames=surface.getSchedules().getScheduleNames();
		sub.add(schedule=new JComboBox<>(scheduleNames));
		schedule.setEnabled(!readOnly);

		/* Karte: Bedingung */

		cards.add(card=new JPanel(new BorderLayout()),"Seite4");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Condition")+":</b></html>"));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Condition.Condition")+":","");
		sub=(JPanel)data[0];
		panel.add(sub);
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],false,false,model,surface),BorderLayout.EAST);
		condition=(JTextField)data[1];
		condition.setEditable(!readOnly);
		condition.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Condition.MinDistance")+":","",10);
		sub=(JPanel)data[0];
		sub.add(new JLabel(Language.tr("Surface.Source.Dialog.Condition.MinDistance.InSeconds")));
		panel.add(sub);
		conditionMinDistance=(JTextField)data[1];
		conditionMinDistance.setEditable(!readOnly);
		conditionMinDistance.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Karte: Schwellenwert */

		cards.add(card=new JPanel(new BorderLayout()),"Seite5");
		card.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.Threshold")+":</b></html>"));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Threshold.ThresholdExpression")+":","");
		sub=(JPanel)data[0];
		panel.add(sub);
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,(JTextField)data[1],false,false,model,surface),BorderLayout.EAST);
		thresholdExpression=(JTextField)data[1];
		thresholdExpression.setEditable(!readOnly);
		thresholdExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Source.Dialog.Threshold.ThresholdValue")+":","",10);
		panel.add((JPanel)data[0]);
		thresholdExpressionValue=(JTextField)data[1];
		thresholdExpressionValue.setEditable(!readOnly);
		thresholdExpressionValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(label=new JLabel(Language.tr("Surface.Source.Dialog.Threshold.Direction")));
		sub.add(thresholdDirection=new JComboBox<>(new String[]{
				Language.tr("Surface.Source.Dialog.Threshold.Direction.Up"),
				Language.tr("Surface.Source.Dialog.Threshold.Direction.Down")
		}));
		thresholdDirection.setRenderer(new IconListCellRenderer(new Images[]{
				Images.ARROW_UP,
				Images.ARROW_DOWN
		}));
		label.setLabelFor(thresholdDirection);

		/* Karte: Signale */

		cards.add(card=new JPanel(new BorderLayout()),"Seite6");
		card.add(panel=new JPanel(new BorderLayout()),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		final JTableExt signalTable;
		panel.add(new JScrollPane(signalTable=new JTableExt()),BorderLayout.CENTER);
		signalTable.setModel(signalsTableModel=new ModelElementSourceRecordSignalTableModel(signalTable,model.surface,readOnly));
		signalTable.setIsPanelCellTable(0);
		signalTable.setIsPanelCellTable(1);
		signalTable.getColumnModel().getColumn(0).setMinWidth(250);
		signalTable.getColumnModel().getColumn(1).setMaxWidth(100);
		signalTable.getColumnModel().getColumn(1).setMinWidth(100);
		signalTable.setEnabled(!readOnly);
		signalTable.putClientProperty("terminateEditOnFocusLost",true);
		signalTable.getTableHeader().setReorderingAllowed(false);
		signalTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		/* Batch */

		tabs.add(Language.tr("Surface.Source.Dialog.Tab.BatchSize"),tab=new JPanel(new BorderLayout(0,5)));
		tab.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.ClientsPerArrival")+":</b></html>"));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionFixedSize=new JRadioButton(Language.tr("Surface.Source.Dialog.BatchSize.Fixed")+":"));
		optionFixedSize.setEnabled(!readOnly);
		optionFixedSize.addActionListener(e->checkData(false));
		sub.add(batchField=new JTextField(15));
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,batchField,false,false,model,surface));
		batchField.setEditable(!readOnly);
		batchField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionFixedSize.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionFixedSize.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionFixedSize.setSelected(true); checkData(false);}
		});

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionSizesDistribution=new JRadioButton(Language.tr("Surface.Source.Dialog.BatchSize.Distribution")));
		optionSizesDistribution.setEnabled(!readOnly);
		optionSizesDistribution.addActionListener(e->checkData(false));
		sub.add(batchButton=new JButton(Language.tr("Surface.Source.Dialog.BatchSize.Distribution.Edit")));
		batchButton.setIcon(Images.MODE_DISTRIBUTION.getIcon());
		batchButton.addActionListener((e)->editBatchSizesDistibution());

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionFixedSize);
		buttonGroup.add(optionSizesDistribution);

		batchButton.setEnabled(!readOnly || optionSizesDistribution.isSelected());

		/* Anzahl an Ank�nften */

		tab=new JPanel(new BorderLayout(0,5));
		if (hasOwnArrivals) tabs.add(Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals"),tab);
		tab.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.ArrivalCount")+":</b></html>"));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionInfinite=new JRadioButton(Language.tr("Surface.Source.Dialog.ArrivalCount.Unlimited")));
		optionInfinite.setEnabled(!readOnly);
		optionInfinite.addActionListener(e->checkData(false));

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionFixedNumberArrivals=new JRadioButton(Language.tr("Surface.Source.Dialog.ArrivalCount.Number")+":"));
		optionFixedNumberArrivals.setEnabled(!readOnly);
		optionFixedNumberArrivals.addActionListener(e->checkData(false));
		sub.add(numberFieldArrivals=new JTextField(7));
		numberFieldArrivals.setEditable(!readOnly);
		numberFieldArrivals.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionFixedNumberArrivals.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionFixedNumberArrivals.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionFixedNumberArrivals.setSelected(true); checkData(false);}
		});

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionFixedNumberClients=new JRadioButton(Language.tr("Surface.Source.Dialog.ClientCount.Number")+":"));
		optionFixedNumberClients.setEnabled(!readOnly);
		optionFixedNumberClients.addActionListener(e->checkData(false));
		sub.add(numberFieldClients=new JTextField(7));
		numberFieldClients.setEditable(!readOnly);
		numberFieldClients.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionFixedNumberClients.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionFixedNumberClients.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionFixedNumberClients.setSelected(true); checkData(false);}
		});

		panel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JTextPane pane=new JTextPane();
		sub.add(pane);
		pane.setOpaque(false);
		pane.setEditable(false);
		pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		pane.setPreferredSize(new Dimension(650,150));
		pane.setText(Language.tr("Surface.Source.Dialog.CountInfo"));

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionInfinite);
		buttonGroup.add(optionFixedNumberArrivals);
		buttonGroup.add(optionFixedNumberClients);

		/* Startzeitpunkt */

		tab=new JPanel(new BorderLayout(0,5));
		if (hasOwnArrivals) tabs.add(Language.tr("Surface.Source.Dialog.Tab.StartingTime"),tab);
		tab.add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(arrivalStartSub1=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		arrivalStartSub1.add(new JLabel("<html><b>"+Language.tr("Surface.Source.Dialog.ArrivalStart")+":</b></html>"));

		panel.add(arrivalStartSub2=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		arrivalStartSub2.add(label=new JLabel(Language.tr("Surface.Source.Dialog.ArrivalStart.Label")+":"));
		arrivalStartSub2.add(arrivalStart=new JTextField(10));
		label.setLabelFor(arrivalStart);
		arrivalStart.setEditable(!readOnly);
		arrivalStart.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		arrivalStartSub2.add(arrivalStartTimeUnit=new JLabel((String)timeBase1.getSelectedItem()));

		arrivalStartSub1.setVisible(selectCard.getSelectedIndex()!=2);
		arrivalStartSub2.setVisible(selectCard.getSelectedIndex()!=2);

		/* Zuweisungen (Zahlen) */

		tabs.add(Language.tr("Surface.Source.Dialog.Tab.SetNumbers"),panelNumbers=new JPanel(new BorderLayout(0,5)));

		/* Zuweisungen (Texte) */

		tabs.add(Language.tr("Surface.Source.Dialog.Tab.SetTexts"),panelText=new JPanel(new BorderLayout(0,5)));

		/* Icons */

		int index=0;
		if (hasOwnArrivals) tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_INTERARRIVAL.getIcon());
		tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_BATCH.getIcon());
		if (hasOwnArrivals) {
			tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_COUNT.getIcon());
			tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_START.getIcon());
		}
		tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_SET_NUMBERS.getIcon());
		tabs.setIconAt(index++,Images.MODELEDITOR_ELEMENT_SOURCE_PAGE_SET_TEXTS.getIcon());
	}

	/**
	 * Tr�gt die Daten eines Kundenquellen-Datensatz in die GUI-Element ein
	 * @param record	Datensatz, dessen Daten in die GUI-Element eingestellt werden sollen
	 * @param element	Element, dessen Zuweisungen bearbeitet werden sollen (f�r den ExpressionBuilder und um die Variablenliste zusammenzustellen)
	 */
	public void setData(final ModelElementSourceRecord record, final ModelElement element) {
		lastRecord=record;
		lastName=record.getName();

		if (record.hasName()) {
			namePanel.setVisible(true);
			nameEdit.setText(record.getName());
			ModelElementBaseDialog.setClientIcon(record.getName(),nameButton,model);
		}

		/* Combobox einstellen */
		switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION: selectCard.setSelectedIndex(0); break;
		case NEXT_EXPRESSION: selectCard.setSelectedIndex(1); break;
		case NEXT_SCHEDULE: selectCard.setSelectedIndex(2); break;
		case NEXT_CONDITION: selectCard.setSelectedIndex(3); break;
		case NEXT_THRESHOLD: selectCard.setSelectedIndex(4); break;
		case NEXT_SIGNAL: selectCard.setSelectedIndex(5); break;
		}

		/* Verteilung */
		timeBase1.setSelectedIndex(record.getTimeBase().id);
		distributionPanel.setDistribution(record.getInterarrivalTimeDistribution());

		/* Ausdruck */
		timeBase2.setSelectedIndex(record.getTimeBase().id);
		expression.setText(record.getInterarrivalTimeExpression());

		/* Zeitplan */
		int index=-1;
		for (int i=0;i<scheduleNames.length;i++) if (scheduleNames[i].equals(record.getInterarrivalTimeSchedule())) {index=i; break;}
		if (index>=0) schedule.setSelectedIndex(index);

		/* Bedingung */
		condition.setText(record.getArrivalCondition());
		conditionMinDistance.setText(NumberTools.formatNumberMax(record.getArrivalConditionMinDistance()));

		/* Schwellenwert */
		thresholdExpression.setText(record.getThresholdExpression());
		thresholdExpressionValue.setText(NumberTools.formatNumberMax(record.getThresholdValue()));
		if (record.isThresholdDirectionUp()) thresholdDirection.setSelectedIndex(0); else thresholdDirection.setSelectedIndex(1);

		/* Signale */
		signalsTableModel.setData(record.getArrivalSignalNames());

		/* Passende Seite aktivieren */
		switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION:
			((CardLayout)cards.getLayout()).show(cards,"Seite1");
			break;
		case NEXT_EXPRESSION:
			((CardLayout)cards.getLayout()).show(cards,"Seite2");
			break;
		case NEXT_SCHEDULE:
			((CardLayout)cards.getLayout()).show(cards,"Seite3");
			break;
		case NEXT_CONDITION:
			((CardLayout)cards.getLayout()).show(cards,"Seite4");
			break;
		case NEXT_THRESHOLD:
			((CardLayout)cards.getLayout()).show(cards,"Seite5");
			break;
		case NEXT_SIGNAL:
			((CardLayout)cards.getLayout()).show(cards,"Seite6");
			break;
		}

		/* Batch */
		if (record.getBatchSize()!=null) {
			optionFixedSize.setSelected(true);
			batchField.setText(record.getBatchSize());
		} else {
			optionSizesDistribution.setSelected(true);
			batchField.setText("1");
		}
		batchRates=record.getMultiBatchSize();
		if (batchRates==null) batchRates=new double[]{1.0};

		/* Ank�nfte-Anzahl */
		optionInfinite.setSelected(true);
		numberFieldArrivals.setText("1000");
		numberFieldClients.setText("1000");
		if (record.getMaxArrivalCount()>0) {
			optionFixedNumberArrivals.setSelected(true);
			numberFieldArrivals.setText(""+record.getMaxArrivalCount());
		}
		if (record.getMaxArrivalClientCount()>0) {
			optionFixedNumberClients.setSelected(true);
			numberFieldClients.setText(""+record.getMaxArrivalClientCount());
		}

		/* Start der Ank�nfte */
		arrivalStart.setText(NumberTools.formatNumber(record.getArrivalStart()));

		Object[] data;

		/* Zuweisungen (Zahlen) */
		data=VariablesTableModel.buildTable(record.getSetRecord(),element,readOnly,helpRunnable);
		panelNumbers.add((JScrollPane)data[0],BorderLayout.CENTER);
		modelNumbers=(VariablesTableModel)data[1];

		/* Zuweisungen (Texte) */
		data=VariablesTextsTableModel.buildTable(record.getStringRecord(),readOnly,helpRunnable);
		panelText.add((JScrollPane)data[0],BorderLayout.CENTER);
		modelText=(VariablesTextsTableModel)data[1];

		/* Start */
		checkData(false);
	}

	private void editBatchSizesDistibution() {
		final ModelElementSourceBatchDialog dialog=new ModelElementSourceBatchDialog(this,"ModelElementSource",readOnly,batchRates);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			batchRates=dialog.getBatchRates();
			optionSizesDistribution.setSelected(true);
		}
	}

	private void updateTabTitle() {
		String info;
		Long L;

		/* Batch-Gr��e */
		info="";
		if (optionFixedSize.isSelected()) {
			info=batchField.getText();
		} else {
			info=Language.tr("Surface.Source.Dialog.Tab.BatchSize.DifferentSizes");
		}
		tabs.setTitleAt(hasOwnArrivals?1:0,Language.tr("Surface.Source.Dialog.Tab.BatchSize")+": "+info);

		/* Anzahl an Ank�nften */
		if (hasOwnArrivals) {
			if (optionInfinite.isSelected()) {
				info=Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Infinite");
			} else {
				if (optionFixedNumberArrivals.isSelected()) {
					L=NumberTools.getPositiveLong(numberFieldArrivals,true);
					if (L==null) {
						info=Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Invalide");
					} else {
						info=NumberTools.formatLong(L.longValue());
					}
				} else {
					L=NumberTools.getPositiveLong(numberFieldClients,true);
					if (L==null) {
						info=Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Clients.Invalide");
					} else {
						info=NumberTools.formatLong(L.longValue());
					}
				}
			}
			tabs.setTitleAt(2,Language.tr("Surface.Source.Dialog.Tab.NumberOfArrivals.Clients")+": "+info);
		}

		/* Startzeit */
		if (hasOwnArrivals) {
			final Double D=NumberTools.getNotNegativeDouble(arrivalStart,true);
			if (D==null) {
				info=Language.tr("Surface.Source.Dialog.Tab.StartingTime.Invalid");
			} else {
				if (D.doubleValue()==0.0) {
					info=Language.tr("Surface.Source.Dialog.Tab.StartingTime.Immediately");
				} else {
					info=String.format(Language.tr("Surface.Source.Dialog.Tab.StartingTime.AfterTime"),NumberTools.formatNumber(D.doubleValue())+" "+(String)timeBase1.getSelectedItem());
				}
			}
			tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime")+": "+info);
		}
	}

	/**
	 * Pr�ft, ob die aktuellen GUI-Daten g�ltig sind
	 * @param showErrorMessage	Gibt an, ob im Fehlerfall auch eine Meldung ausgegeben werden soll
	 * @return	Liefert <code>true</code>, wenn alle Daten in Ordnung sind
	 */
	public boolean checkData(final boolean showErrorMessage) {
		updateTabTitle();
		if (readOnly) return false;

		boolean ok=true;
		Long L;
		int error;

		if (namePanel.isVisible() && nameEdit.getText().trim().isEmpty()) {
			nameEdit.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Source.Dialog.ErrorName.Title"),Language.tr("Surface.Source.Dialog.ErrorName.Info"));
				return false;
			}
			ok=false;
		} else {
			nameEdit.setBackground(SystemColor.text);
		}

		switch (selectCard.getSelectedIndex()) {
		case 0: /* Verteilung */
			/* nichts zu pr�fen */
			break;
		case 1: /* Ausdruck */
			error=ExpressionCalc.check(expression.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
			if (error>=0) {
				expression.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.Expression.Error.Info"),expression.getText(),error+1));
					return false;
				}
				ok=false;
			} else {
				expression.setBackground(SystemColor.text);
			}
			break;
		case 2: /* Zeitplan */
			if (schedule.getSelectedIndex()<0) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Schedule.Error.Title"),Language.tr("Surface.Source.Dialog.Schedule.Error.Info"));
					return false;
				}
				return false;
			}
			break;
		case 3: /* Bedingung */
			error=ExpressionMultiEval.check(condition.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
			if (error>=0) {
				condition.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.Condition.Error.Info"),condition.getText(),error+1));
					return false;
				}
				ok=false;
			} else {
				condition.setBackground(SystemColor.text);
			}
			if (NumberTools.getPositiveDouble(conditionMinDistance,true)==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ConditionMinDistance.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ConditionMinDistance.Error.Info"),conditionMinDistance.getText()));
					return false;
				}
				ok=false;
			}
			break;
		case 4: /* Schwellenwert */
			error=ExpressionCalc.check(thresholdExpression.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true));
			if (error>=0) {
				thresholdExpression.setBackground(Color.RED);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Threshold.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.Threshold.Error.Info"),thresholdExpression.getText(),error+1));
					return false;
				}
				ok=false;
			} else {
				thresholdExpression.setBackground(SystemColor.text);
			}
			if (NumberTools.getDouble(thresholdExpressionValue,true)==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ThresholdValue.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ThresholdValue.Error.Info"),thresholdExpressionValue.getText()));
					return false;
				}
				ok=false;
			}
			break;
		case 5: /* Signale */
			if (signalsTableModel.getData().size()==0) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.Signals.Error.Title"),Language.tr("Surface.Source.Dialog.Signals.Error.Info"));
					return false;
				}
				ok=false;
			}
			break;
		}

		error=ExpressionCalc.check(batchField.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
		if (error>=0) {
			batchField.setBackground(Color.RED);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Source.Dialog.BatchSize.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.BatchSize.Error.Info"),batchField.getText(),error+1));
				return false;
			}
			ok=false;
		} else {
			batchField.setBackground(SystemColor.text);
		}

		if (hasOwnArrivals) {
			L=NumberTools.getPositiveLong(numberFieldArrivals,true);
			if (L==null && optionFixedNumberArrivals.isSelected()) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ArrivalCount.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ArrivalCount.Error.Info"),numberFieldArrivals.getText()));
					return false;
				}
				ok=false;
			}
			L=NumberTools.getPositiveLong(numberFieldClients,true);
			if (L==null && optionFixedNumberClients.isSelected()) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ClientCount.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ClientCount.Error.Info"),numberFieldClients.getText()));
					return false;
				}
				ok=false;
			}

			final Double D=NumberTools.getNotNegativeDouble(arrivalStart,true);
			if (D==null) {
				tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime")+": "+Language.tr("Surface.Source.Dialog.Tab.StartingTime.Invalid"));
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Source.Dialog.ArrivalStart.Error.Title"),String.format(Language.tr("Surface.Source.Dialog.ArrivalStart.Error.Info"),arrivalStart.getText()));
					return false;
				}
				ok=false;
			} else {
				if (D.doubleValue()==0.0) {
					tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime")+": "+Language.tr("Surface.Source.Dialog.Tab.StartingTime.Immediately"));
				} else {
					tabs.setTitleAt(3,Language.tr("Surface.Source.Dialog.Tab.StartingTime")+": "+String.format(Language.tr("Surface.Source.Dialog.Tab.StartingTime.AfterSeconds"),NumberTools.formatNumber(D.doubleValue())));
				}
			}
		}

		return ok;
	}

	/**
	 * Schreibt die Daten aus der GUI in ein Datensatz-Objekt zur�ck
	 * @param record	Datensatz-Objekt, in das die Daten eingetragen werden sollen
	 */
	public void getData(final ModelElementSourceRecord record) {
		if (nameEdit.isVisible()) {
			record.setName(nameEdit.getText());
		}

		Double D;

		switch (selectCard.getSelectedIndex()) {
		case 0: /* Verteilung */
			record.setTimeBase(ModelSurface.TimeBase.byId(timeBase1.getSelectedIndex()));
			record.setInterarrivalTimeDistribution(distributionPanel.getDistribution());
			break;
		case 1: /* Ausdruck */
			record.setTimeBase(ModelSurface.TimeBase.byId(timeBase2.getSelectedIndex()));
			record.setInterarrivalTimeExpression(expression.getText());
			break;
		case 2: /* Zeitplan */
			String s="";
			if (schedule.getSelectedIndex()>=0 && schedule.getSelectedIndex()<scheduleNames.length) s=scheduleNames[schedule.getSelectedIndex()];
			record.setInterarrivalTimeSchedule(s);
			break;
		case 3: /* Bedingung */
			D=NumberTools.getPositiveDouble(conditionMinDistance,true);
			final double conditionMinDistance=(D!=null)?D.doubleValue():0.0;
			record.setArrivalCondition(condition.getText(),conditionMinDistance);
			break;
		case 4: /* Schwellenwert */
			D=NumberTools.getDouble(thresholdExpressionValue,true);
			final double thresholdValue=(D!=null)?D.doubleValue():0.0;
			record.setThreshold(thresholdExpression.getText(),thresholdValue,thresholdDirection.getSelectedIndex()==0);
			break;
		case 5: /* Signale */
			record.getArrivalSignalNames().clear();
			record.getArrivalSignalNames().addAll(signalsTableModel.getData());
			record.setSignalMode();
			break;
		}

		if (optionFixedSize.isSelected()) {
			record.setBatchSize(batchField.getText().trim());
		} else {
			record.setMultiBatchSize(batchRates);
		}

		record.setMaxArrivalCount(-1);
		record.setMaxArrivalClientCount(-1);
		if (optionFixedNumberArrivals.isSelected()) {
			final Long L=NumberTools.getPositiveLong(numberFieldArrivals,true);
			if (L!=null) record.setMaxArrivalCount(L);
		}
		if (optionFixedNumberClients.isSelected()) {
			final Long L=NumberTools.getPositiveLong(numberFieldClients,true);
			if (L!=null) record.setMaxArrivalClientCount(L);
		}

		if (selectCard.getSelectedIndex()!=2 && selectCard.getSelectedIndex()!=3 && selectCard.getSelectedIndex()!=4) {
			record.setArrivalStart(NumberTools.getNotNegativeDouble(arrivalStart,true));
		}

		/* Zuweisungen (Zahlen) */
		modelNumbers.storeData();

		/* Zuweisungen (Texte) */
		modelText.storeData();
	}

	/**
	 * Schreibt die Daten in das Datensatz-Objekt, aus dem sie ausgelesen wurden, zur�ck
	 * @param updateSystem	Stellt ein, ob Namens�nderungen ins System �bertragen werden sollen,
	 * @param clientData	Datenelement, welches die Kundentypenliste vorh�lt
	 */
	public void getData(final boolean updateSystem, final ModelClientData clientData) {
		if (lastRecord==null) return;
		getData(lastRecord);
		if (lastRecord.hasName() && updateSystem) renameClients(lastName,lastRecord.getName(),clientData,surface);
	}

	/**
	 * Pr�ft, ob sich der Name einer Kundengruppe ge�ndert hat und �bertr�gt die �nderungen ggf. in die anderen Surface-Elemente.<br>
	 * Besitzt das Datensatz-Element einen eigenen Namen und wird <code>getData()</code> (ohne Parameter) verwendet, so wird
	 * diese Methode automatisch aufgerufen. Dient jedoch der Name des Elementes als Kundengruppenname, so muss diese
	 * Methode manuell aufgerufen werden.
	 * @param oldName	Alter Name
	 * @param newName	Neuer Name
	 * @param clientData	Kundendatenobjekt aus dem Modell, in dem der Kundendatensatz ggf. umbenannt werden soll
	 * @param surface	Zeichenfl�che (muss bei Umbenennung benachrichtigt werden). Es kann sich auch um eine Sub-Zeichenfl�che handeln, die Information wird dann zur Hauptzeichenfl�che weitergereicht.
	 */
	public static void renameClients(final String oldName, final String newName, final ModelClientData clientData, final ModelSurface surface) {
		if (oldName!=null && newName!=null && !oldName.trim().isEmpty() && !newName.trim().isEmpty() && !oldName.equals(newName)) {
			if (clientData!=null) clientData.copyDataIfNotExistent(oldName,newName);
			surface.objectRenamed(oldName,newName,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE,true);
		}
	}

	private void editClientData() {
		final String name=(lastName.isEmpty())?nameEdit.getText().trim():lastName;
		if (name.isEmpty()) {
			MsgBox.error(this,Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Title"),Language.tr("Surface.Source.Dialog.ClientTypeSettings.NoNameError.Info"));
			return;
		}

		if (ModelPropertiesDialog.editClientData(this,helpRunnable,model,name,readOnly)) ModelElementBaseDialog.setClientIcon(name,nameButton,model);
	}
}
