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
package ui.optimizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Diese Klasse zeigt einen Dialog zum Bearbeiten der Optimierungsalgorithmusparameter an
 * @author Alexander Herzog
 * @see OptimizerPanel
 */
public class OptimizerPanelAlgorithmParametersDialog extends BaseDialog {
	private static final long serialVersionUID = -8885821247331752499L;

	private final OptimizerSetup setup;
	private final JTextField serialChangeSpeed1;
	private final JTextField serialChangeSpeed2;
	private final JTextField serialChangeSpeed3;
	private final JTextField serialChangeSpeed4;
	private final JTextField geneticPopulationSize;
	private final JTextField geneticEvolutionPressure;
	private final JTextField geneticChangeSpeed1;
	private final JTextField geneticChangeSpeed2;
	private final JTextField geneticChangeSpeed3;
	private final JTextField geneticChangeSpeed4;
	private final JTextField geneticChangeSpeed5;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param setup	Optimierungssetup aus dem die Parameter entnommen werden sollen und in das sie im Erfolgsfall wieder zur�ckgeschrieben werden
	 * @param help	Runnable, das aufgerufen wird, wenn der Nutzer auf "Hilfe" klickt
	 */
	public OptimizerPanelAlgorithmParametersDialog(final Component owner, final OptimizerSetup setup, final Runnable help) {
		super(owner,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Title"));
		this.setup=setup;

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tabOuter, tab;

		/* Serielle Algorithmen */
		tabs.addTab(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		serialChangeSpeed1=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate1"),NumberTools.formatPercent(setup.serialChangeSpeed1));
		serialChangeSpeed2=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate2"),NumberTools.formatPercent(setup.serialChangeSpeed2));
		serialChangeSpeed3=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate3"),NumberTools.formatPercent(setup.serialChangeSpeed3));
		serialChangeSpeed4=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate4"),NumberTools.formatPercent(setup.serialChangeSpeed4));

		/* Generischer Algorithmus */
		tabs.addTab(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		geneticPopulationSize=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.PopulationSize"),""+setup.geneticPopulationSize);
		geneticEvolutionPressure=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.EvolutionPressure"),NumberTools.formatPercent(setup.geneticEvolutionPressure));
		geneticChangeSpeed1=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate1"),NumberTools.formatPercent(setup.geneticChangeSpeed1));
		geneticChangeSpeed2=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate2"),NumberTools.formatPercent(setup.geneticChangeSpeed2));
		geneticChangeSpeed3=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate3"),NumberTools.formatPercent(setup.geneticChangeSpeed3));
		geneticChangeSpeed4=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate4"),NumberTools.formatPercent(setup.geneticChangeSpeed4));
		geneticChangeSpeed5=addTabInputLine(tab,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate5"),NumberTools.formatPercent(setup.geneticChangeSpeed5));

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private JTextField addTabInputLine(final JPanel parent, final String label, final String value) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);

		final Object[] data=ModelElementBaseDialog.getInputPanel(label+":",value,5);
		line.add((JPanel)data[0]);
		final JTextField textField=(JTextField)data[1];
		textField.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		return textField;
	}

	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		Double D;

		/* Serielle Algorithmen */

		D=NumberTools.getPositiveDouble(serialChangeSpeed1,true);
		if (D==null || D.doubleValue()>1) {
			serialChangeSpeed1.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate1.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate1.ErrorInfo"),serialChangeSpeed1.getText()));
				return false;
			}
		} else {
			serialChangeSpeed1.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(serialChangeSpeed2,true);
		if (D==null || D.doubleValue()>1) {
			serialChangeSpeed2.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate2.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate2.ErrorInfo"),serialChangeSpeed2.getText()));
				return false;
			}
		} else {
			serialChangeSpeed2.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(serialChangeSpeed3,true);
		if (D==null || D.doubleValue()>1) {
			serialChangeSpeed3.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate3.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate3.ErrorInfo"),serialChangeSpeed3.getText()));
				return false;
			}
		} else {
			serialChangeSpeed3.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(serialChangeSpeed4,true);
		if (D==null || D.doubleValue()>1) {
			serialChangeSpeed4.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate4.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Serial.ChangeRate4.ErrorInfo"),serialChangeSpeed4.getText()));
				return false;
			}
		} else {
			serialChangeSpeed4.setBackground(SystemColor.text);
		}

		/* Generischer Algorithmus */

		final Long L=NumberTools.getPositiveLong(geneticPopulationSize,true);
		if (L==null) {
			geneticPopulationSize.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.PopulationSize.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.PopulationSize.ErrorInfo"),geneticPopulationSize.getText()));
				return false;
			}
		} else {
			geneticPopulationSize.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(geneticEvolutionPressure,true);
		if (D==null || D.doubleValue()>1) {
			geneticEvolutionPressure.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.EvolutionPressure.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.EvolutionPressure.ErrorInfo"),geneticEvolutionPressure.getText()));
				return false;
			}
		} else {
			geneticEvolutionPressure.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(geneticChangeSpeed1,true);
		if (D==null || D.doubleValue()>1) {
			geneticChangeSpeed1.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate1.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate1.ErrorInfo"),geneticChangeSpeed1.getText()));
				return false;
			}
		} else {
			geneticChangeSpeed1.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(geneticChangeSpeed2,true);
		if (D==null || D.doubleValue()>1) {
			geneticChangeSpeed2.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate2.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate2.ErrorInfo"),geneticChangeSpeed2.getText()));
				return false;
			}
		} else {
			geneticChangeSpeed2.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(geneticChangeSpeed3,true);
		if (D==null || D.doubleValue()>1) {
			geneticChangeSpeed3.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate3.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate3.ErrorInfo"),geneticChangeSpeed3.getText()));
				return false;
			}
		} else {
			geneticChangeSpeed3.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(geneticChangeSpeed4,true);
		if (D==null || D.doubleValue()>1) {
			geneticChangeSpeed4.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate4.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate4.ErrorInfo"),geneticChangeSpeed4.getText()));
				return false;
			}
		} else {
			geneticChangeSpeed4.setBackground(SystemColor.text);
		}

		D=NumberTools.getPositiveDouble(geneticChangeSpeed5,true);
		if (D==null || D.doubleValue()>1) {
			geneticChangeSpeed5.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate5.ErrorTitle"),String.format(Language.tr("Optimizer.Tab.Optimization.Kernel.Parameters.Tab.Genetic.ChangeRate5.ErrorInfo"),geneticChangeSpeed5.getText()));
				return false;
			}
		} else {
			geneticChangeSpeed5.setBackground(SystemColor.text);
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		/* Serielle Algorithmen */
		setup.serialChangeSpeed1=NumberTools.getPositiveDouble(serialChangeSpeed1,true);
		setup.serialChangeSpeed2=NumberTools.getPositiveDouble(serialChangeSpeed2,true);
		setup.serialChangeSpeed3=NumberTools.getPositiveDouble(serialChangeSpeed3,true);
		setup.serialChangeSpeed4=NumberTools.getPositiveDouble(serialChangeSpeed4,true);

		/* Generischer Algorithmus */
		setup.geneticPopulationSize=NumberTools.getLong(geneticPopulationSize,true).intValue();
		setup.geneticEvolutionPressure=NumberTools.getPositiveDouble(geneticEvolutionPressure,true);
		setup.geneticChangeSpeed1=NumberTools.getPositiveDouble(geneticChangeSpeed1,true);
		setup.geneticChangeSpeed2=NumberTools.getPositiveDouble(geneticChangeSpeed2,true);
		setup.geneticChangeSpeed3=NumberTools.getPositiveDouble(geneticChangeSpeed3,true);
		setup.geneticChangeSpeed4=NumberTools.getPositiveDouble(geneticChangeSpeed4,true);
		setup.geneticChangeSpeed5=NumberTools.getPositiveDouble(geneticChangeSpeed5,true);
	}
}
