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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Diese Klasse kapselt die Dialogdaten f�r ein
 * {@link TransportResourceRecord}-Element.
 * @author Alexander Herzog
 * @see TransportResourceRecord
 * @see ModelElementTransportSourceDialog
 */
public class TransportResourceRecordPanel extends JPanel {
	private static final long serialVersionUID = 7027999560443225809L;

	private final TransportResourceRecord data;
	private final DistributionSystem delayedRelease;
	private final boolean readOnly;
	private final Runnable help;
	private final EditModel model;
	private final ModelSurface surface;

	private JTextField textResourcePriority;
	private ResourceTableModel tableResource;
	private ModelSurface.TimeBase timeBase;
	private JLabel delayedReleaseInfo;

	/**
	 * Konstruktor der Klasse
	 * @param data	Daten, die in dem Panel bearbeitet werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param model	Gesamtes Modell (zum Auslesen von Daten f�r den Expression-Builder)
	 * @param surface	Zeichenoberfl�che (zum Auslesen von Daten f�r den Expression-Builder)
	 * @param help	Hilfe-Runnable
	 */
	public TransportResourceRecordPanel(final TransportResourceRecord data, final boolean readOnly, final EditModel model, final ModelSurface surface, final Runnable help) {
		super();
		this.data=data;
		delayedRelease=data.getDelayedRelease().clone();
		this.readOnly=readOnly;
		this.help=help;
		this.model=model;
		this.surface=surface;
		setLayout(new BorderLayout());

		JPanel sub;

		/* Priorit�t */
		final Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.TransportSource.Dialog.Ressource.Priority")+":",data.getResourcePriority());
		sub=(JPanel)obj[0];
		textResourcePriority=(JTextField)obj[1];
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,textResourcePriority,false,false,model,surface),BorderLayout.EAST);
		textResourcePriority.setEditable(!readOnly);
		add(sub,BorderLayout.NORTH);
		textResourcePriority.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Ressourcen */
		final JTable table;
		add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);
		table.setModel(tableResource=new ResourceTableModel(((JTableExt)table),data.getResources(),model,surface.getResources(),readOnly,help));
		table.getColumnModel().getColumn(1).setMaxWidth(275);
		table.getColumnModel().getColumn(1).setMinWidth(275);
		((JTableExt)table).setIsPanelCellTable(0);
		((JTableExt)table).setIsPanelCellTable(1);
		table.setEnabled(!readOnly);

		/* Verz�gerte Freigabe */
		timeBase=data.getTimeBase();
		add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		final JButton button=new JButton(Language.tr("Surface.TransportSource.Dialog.Ressource.DelayedRelease.Button"));
		button.setIcon(Images.MODELEDITOR_ELEMENT_DELAY.getIcon());
		button.addActionListener(e->editDelayedRelease());
		sub.add(button);
		sub.add(delayedReleaseInfo=new JLabel());

		/* Start */
		updateDelayedReleaseInfo();
		checkData(false);
	}

	private void editDelayedRelease() {
		final TransportResourceRecordDelayDialog dialog=new TransportResourceRecordDelayDialog(this,timeBase,delayedRelease,model,surface,readOnly,help);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			timeBase=dialog.getTimeBase();
			updateDelayedReleaseInfo();
		}
	}

	private void updateDelayedReleaseInfo() {
		if (delayedRelease.hasData()) {
			delayedReleaseInfo.setText(Language.tr("Surface.TransportSource.Dialog.Ressource.DelayedRelease.Info.Active"));
		} else {
			delayedReleaseInfo.setText(Language.tr("Surface.TransportSource.Dialog.Ressource.DelayedRelease.Info.Inactive"));
		}
	}

	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		final int error=ExpressionCalc.check(textResourcePriority.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
		if (error>=0) {
			textResourcePriority.setBackground(Color.red);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.TransportSource.Dialog.Ressource.Priority.ErrorTitle"),String.format(Language.tr("Surface.TransportSource.Dialog.Ressource.Priority.ErrorInfo"),textResourcePriority.getText()));
			ok=false;
		} else {
			textResourcePriority.setBackground(SystemColor.text);
		}

		return ok;
	}

	/**
	 * Pr�ft, ob die Einstellungen in Ordnung sind und gibt im Fehlerfall eine Fehlermeldung aus.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Einstellungen in Ordnung sind.
	 */
	public boolean checkData() {
		return checkData(true);
	}

	/**
	 * Schreibt die m�glicherweise ver�nderten Daten in das
	 * im Konstruktor �bergebene {@link TransportResourceRecord}-Objekt zur�ck.
	 */
	public void storeData() {
		data.setResourcePriority(textResourcePriority.getText());
		tableResource.storeData(data.getResources());
		data.getDelayedRelease().setData(delayedRelease);
		data.setTimeBase(timeBase);
	}
}