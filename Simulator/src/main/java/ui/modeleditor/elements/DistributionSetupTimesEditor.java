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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Stellt einen Verteilungs- bzw. Ausdruckeditor f�r die R�stzeiten dar.
 * @author Alexander Herzog
 */
public class DistributionSetupTimesEditor extends JPanel {
	private static final long serialVersionUID = -734275539715521114L;

	private static final String cardDistribution="Distribution";
	private static final String cardExpression="Expression";

	private final EditModel model;
	private final ModelSurface surface;
	private final boolean readOnly;
	private final DistributionSystemSetupTimes dataOriginal;
	private final DistributionSystemSetupTimes data;
	private final String[] clientTypes;

	private int typeLast;
	private final JComboBox<String> typeCombo;
	private final JCheckBox activeCheckBox;
	private final JLabel infoLabel;
	private final JComboBox<String> modeSelect;
	private final JPanel cards;
	private final JDistributionPanel distributionPanel;
	private final JPanel expressionLines;
	private final JTextField expressionEdit;

	private List<ActionListener> userChangeListeners;

	/**
	 * Konstruktor der Klasse <code>DistributionSetupTimesEditor</code>
	 * @param model	Element vom Typ <code>EditModel</code> (wird ben�tigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Element vom Typ <code>ModelSurface</code> (wird ben�tigt, um die Liste der Kundentypen oder Stationen auszulesen)
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden d�rfen, oder ob sich das System im Read-Only-Modus befindet
	 * @param data	Zu bearbeitende Daten (das Objekt wird durch den Aufruf von <code>storeData</code> aktualisiert)
	 */
	public DistributionSetupTimesEditor(final EditModel model, final ModelSurface surface, final boolean readOnly, final DistributionSystemSetupTimes data) {
		super();

		userChangeListeners=new ArrayList<>();

		/* Daten vorbereiten */
		this.model=model;
		this.surface=surface;
		this.readOnly=readOnly;
		dataOriginal=data;
		this.data=data.clone();
		clientTypes=surface.getClientTypes().toArray(new String[0]);

		/* GUI aufbauen */
		setLayout(new BorderLayout());
		JPanel panel, line;
		JLabel label;

		add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Process.Dialog.SetupTimes.Type")+":"));
		line.add(typeCombo=new JComboBox<>(getTypeItems()));
		label.setLabelFor(typeCombo);

		if (clientTypes.length>0) {

			line.add(activeCheckBox=new JCheckBox(Language.tr("Surface.Process.Dialog.SetupTimes.Active")));
			activeCheckBox.setEnabled(!readOnly);

			panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(infoLabel=new JLabel());
			line.add(modeSelect=new JComboBox<>(new String[]{
					Language.tr("Surface.DistributionByClientTypeEditor.DefineByDistribution"),
					Language.tr("Surface.DistributionByClientTypeEditor.DefineByExpression")
			}));
			modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
					Images.MODE_DISTRIBUTION,
					Images.MODE_EXPRESSION
			}));
			infoLabel.setLabelFor(modeSelect);
			modeSelect.setEnabled(!readOnly);

			add(cards=new JPanel(new CardLayout()),BorderLayout.CENTER);
			cards.add(distributionPanel=new JDistributionPanel(null,3600,!readOnly) {
				private static final long serialVersionUID = -8375312389773855243L;
				@Override
				protected boolean editButtonClicked() {
					fireUserChangeListener();
					activeCheckBox.setSelected(true);
					return super.editButtonClicked();
				}
			},cardDistribution);

			cards.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)),cardExpression);
			panel.add(expressionLines=new JPanel());
			expressionLines.setLayout(new BoxLayout(expressionLines,BoxLayout.PAGE_AXIS));
			Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DistributionByClientTypeEditor.Expression")+":","");
			line=(JPanel)obj[0];
			expressionLines.add(line);
			expressionEdit=(JTextField)obj[1];
			line.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,true,model,surface),BorderLayout.EAST);
			expressionEdit.setEditable(!readOnly);
			expressionEdit.addKeyListener(new KeyListener(){
				@Override public void keyTyped(KeyEvent e) {fireUserChangeListener(); activeCheckBox.setSelected(true); checkExpression();}
				@Override public void keyPressed(KeyEvent e) {fireUserChangeListener(); activeCheckBox.setSelected(true); checkExpression();}
				@Override public void keyReleased(KeyEvent e) {fireUserChangeListener(); activeCheckBox.setSelected(true); checkExpression();}
			});

			/* Verarbeitung starten */
			typeLast=-1;
			typeCombo.addActionListener(e->activeClientTypeChanged());
			modeSelect.addActionListener(e->{
				setCard((modeSelect.getSelectedIndex()==0)?cardDistribution:cardExpression);
				fireUserChangeListener();
			});

			typeCombo.setSelectedIndex(0);
		} else {
			activeCheckBox=null;
			infoLabel=null;
			modeSelect=null;
			cards=null;
			distributionPanel=null;
			expressionLines=null;
			expressionEdit=null;
		}
	}

	private String[] getTypeItems() {
		final List<String> list=new ArrayList<>(clientTypes.length*clientTypes.length);

		for (String type1: clientTypes) for (String type2: clientTypes) list.add(type1+" -> "+type2);

		return list.toArray(new String[0]);
	}

	/**
	 * F�gt einen Listener zu der Liste der im Falle einer Nutzereingabe zu benachrichtigenden Objekte hinzu
	 * @param actionListener	Neuer Listener
	 */
	public void addUserChangeListener(final ActionListener actionListener) {
		if (userChangeListeners.indexOf(actionListener)<0) userChangeListeners.add(actionListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer Nutzereingabe zu benachrichtigenden Objekte
	 * @param actionListener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich entfernt werden konnte.
	 */
	public boolean removeUserChangeListener(final ActionListener actionListener) {
		return userChangeListeners.remove(actionListener);
	}

	private void fireUserChangeListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"");
		for (ActionListener listener: userChangeListeners) listener.actionPerformed(event);
	}

	/**
	 * Schreibt die �ber die GUI vorgenommenen �nderungen in das dem Konstruktor �bergebene Datenobjekt zur�ck.
	 */
	public void storeData() {
		activeClientTypeChanged();
		dataOriginal.setData(data);
	}

	/**
	 * Gibt an, ob mindestens eine R�stzeit hinterlegt ist
	 * @return	Gibt <code>true</code> zur�ck, wenn mindestens eine R�stzeit vorhanden ist
	 */
	public boolean isActive() {
		return data.isActive();
	}

	private void setCard(final String cardName) {
		((CardLayout)cards.getLayout()).show(cards,cardName);

		SwingUtilities.invokeLater(()->{
			final int h=expressionLines.getSize().height;
			final int w=cards.getSize().width;
			if (h>0 && w>0) {
				expressionLines.setPreferredSize(new Dimension(w-15,h));
				expressionLines.setSize(new Dimension(w-15,h));
			}
		});
	}

	private void checkExpression() {
		if (expressionEdit.getText().trim().isEmpty()) {
			expressionEdit.setBackground(SystemColor.text);
			return;
		}

		final int error=ExpressionCalc.check(expressionEdit.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true));
		if (error>=0) expressionEdit.setBackground(Color.red); else expressionEdit.setBackground(SystemColor.text);
	}

	private void activeClientTypeChanged() {
		if (clientTypes.length==0) return;

		if (typeLast>=0 && !readOnly) {
			final String type1=clientTypes[typeLast/clientTypes.length];
			final String type2=clientTypes[typeLast%clientTypes.length];
			if (activeCheckBox.isSelected()) {
				if (modeSelect.getSelectedIndex()==0) {
					data.set(type1,type2,distributionPanel.getDistribution());
				} else {
					data.set(type1,type2,expressionEdit.getText());
				}
			} else {
				data.remove(type1,type2);
			}
		}

		typeLast=typeCombo.getSelectedIndex();
		final String type1=clientTypes[typeLast/clientTypes.length];
		final String type2=clientTypes[typeLast%clientTypes.length];
		infoLabel.setText(String.format(Language.tr("Surface.Process.Dialog.SetupTimes.Info"),type1,type2));
		final Object obj=data.get(type1,type2);
		activeCheckBox.setSelected(obj!=null);
		if (obj==null) {
			setCard(cardDistribution);
			modeSelect.setSelectedIndex(0);
			distributionPanel.setDistribution(new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
			expressionEdit.setText("");
			checkExpression();
		} else {
			if (obj instanceof String) {
				setCard(cardExpression);
				modeSelect.setSelectedIndex(1);
				distributionPanel.setDistribution(new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
				expressionEdit.setText((String)obj);
				checkExpression();
			}
			if (obj instanceof AbstractRealDistribution) {
				setCard(cardDistribution);
				modeSelect.setSelectedIndex(0);
				distributionPanel.setDistribution((AbstractRealDistribution)obj);
				expressionEdit.setText("");
				checkExpression();
			}
		}
	}
}
