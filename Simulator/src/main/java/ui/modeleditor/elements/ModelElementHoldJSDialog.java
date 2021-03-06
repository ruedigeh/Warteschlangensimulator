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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorPanel;

/**
 * Dialog, der Einstellungen f�r ein {@link ModelElementHoldJS}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementHoldJS
 */
public class ModelElementHoldJSDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 2971721070323863626L;

	private ScriptEditorPanel editor;
	private JCheckBox useTimedChecks;
	private JTextField condition;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementHoldJS}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	public ModelElementHoldJSDialog(final Component owner, final ModelElementHoldJS element, final boolean readOnly) {
		super(owner,Language.tr("Surface.HoldJS.Dialog.Title"),element,"ModelElementHoldJS",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationHoldJS;
	}

	@Override
	protected JComponent getContentPanel() {
		if (element instanceof ModelElementHoldJS) {
			final String script=((ModelElementHoldJS)element).getScript();
			ScriptEditorPanel.ScriptMode mode;
			switch (((ModelElementHoldJS)element).getMode()) {
			case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
			default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
			}
			final JPanel content=new JPanel(new BorderLayout());
			content.add(editor=new ScriptEditorPanel(script,mode,readOnly,Language.tr("Surface.HoldJS.Dialog.Script"),element.getModel(),helpRunnable,ScriptEditorPanel.featuresClientStationHold),BorderLayout.CENTER);

			final JPanel setup=new JPanel();
			setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
			content.add(setup,BorderLayout.SOUTH);

			JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			setup.add(line);
			line.add(useTimedChecks=new JCheckBox(Language.tr("Surface.HoldJS.Dialog.TimeBasedCheck"),((ModelElementHoldJS)element).isUseTimedChecks()));
			useTimedChecks.setEnabled(!readOnly);

			final Object[] obj=getInputPanel(Language.tr("Surface.HoldJS.Dialog.Condition")+":",((ModelElementHoldJS)element).getCondition());
			setup.add(line=(JPanel)obj[0]);
			condition=(JTextField)obj[1];
			line.add(getExpressionEditButton(this,condition,true,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
			condition.addKeyListener(new KeyAdapter() {
				@Override public void keyReleased(KeyEvent e) {checkCondition(false);}
			});

			return content;
		} else {
			return new JPanel();
		}
	}

	/**
	 * Stellt die Gr��e des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		pack();
	}

	private boolean checkCondition(final boolean showErrorMessage) {
		final String text=condition.getText().trim();

		if (text.isEmpty()) {
			condition.setBackground(SystemColor.text);
			return true;
		}

		final int error=ExpressionMultiEval.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
		if (error>=0) {
			condition.setBackground(Color.red);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.HoldJS.Dialog.Condition.Error.Title"),String.format(Language.tr("Surface.HoldJS.Dialog.Condition.Error.Info"),text,error+1));
			return false;
		}
		condition.setBackground(SystemColor.text);
		return true;
	}

	@Override
	protected boolean checkData() {
		if (!editor.checkData()) return false;
		if (!checkCondition(true)) return false;

		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugeh�rigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementHoldJS) {
			((ModelElementHoldJS)element).setScript(editor.getScript());
			switch (editor.getMode()) {
			case Javascript: ((ModelElementHoldJS)element).setMode(ModelElementHoldJS.ScriptMode.Javascript); break;
			case Java: ((ModelElementHoldJS)element).setMode(ModelElementHoldJS.ScriptMode.Java); break;
			}
			((ModelElementHoldJS)element).setUseTimedChecks(useTimedChecks.isSelected());
			((ModelElementHoldJS)element).setCondition(condition.getText());
		}
	}
}