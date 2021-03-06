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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import scripting.java.DynamicFactory;
import scripting.js.JSRunDataFilterTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptEditorAreaBuilder;
import ui.script.ScriptPopup;

/**
 * Dieser Dialog erlaubt die Berechnung von Ausdr�cken aus einer
 * laufenden Animation oder �hnlichem heraus.
 * @author Alexander Herzog
 */
public final class ExpressionCalculatorDialog extends BaseDialog {
	private static final long serialVersionUID = -2213485790093666048L;

	private static final String DEFAULT_JAVA_SCRIPT="Output.println(\"\");";
	private static final String DEFAULT_JAVA="void function(SimulationInterface sim) {\n  sim.getOutput().println(\"\");\n}\n";

	private final EditModel model;

	private final Function<String,Double> calc;
	private final Function<String,String> runJavaScript;
	private final Function<String,String> runJava;

	private final JTabbedPane tabs;

	private final JTextField expressionEdit;
	private final JTextField resultsEdit;

	private final JButton buttonJavaScriptNew;
	private final JButton buttonJavaScriptLoad;
	private final JButton buttonJavaScriptSave;
	private final JButton buttonJavaScriptTools;
	private final JButton buttonJavaScriptRun;
	private final JButton buttonJavaScriptHelp;
	private final RSyntaxTextArea scriptJavaScriptEdit;
	private final JTextArea scriptJavaScriptResults;
	private String lastJavaScript="";

	private final JButton buttonJavaNew;
	private final JButton buttonJavaLoad;
	private final JButton buttonJavaSave;
	private final JButton buttonJavaTools;
	private final JButton buttonJavaRun;
	private final JButton buttonJavaHelp;
	private final RSyntaxTextArea scriptJavaEdit;
	private final JTextArea scriptJavaResults;
	private String lastJava="";

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 * @param model	Editor-Modell als Information f�r den Expression-Builder
	 * @param calc	Funktion, die die Berechnungen erlaubt
	 * @param runJavaScript	Function, die die Javascript-Ausf�hrungen erlaubt
	 * @param runJava	Function, die die Java-Ausf�hrungen erlaubt
	 * @param initialTab	Anf�nglich anzuzeigenden Tab
	 * @param initialExpression	Startwert f�r das Eingabefeld
	 * @param initialJavaScript	Startwert f�r das Javaskript-Eingabefeld
	 * @param initialJava	Startwert f�r das Java-Eingabefeld
	 * @see ExpressionCalculatorDialog#getLastExpression()
	 */
	public ExpressionCalculatorDialog(final Component owner, final EditModel model, final Function<String,Double> calc, final Function<String,String> runJavaScript, final Function<String,String> runJava, final int initialTab, final String initialExpression, final String initialJavaScript, final String initialJava) {
		super(owner,Language.tr("ExpressionCalculator.Title"));
		this.model=model;
		this.calc=calc;
		this.runJavaScript=runJavaScript;
		this.runJava=runJava;

		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);
		JPanel tab;

		JPanel sub, line;
		Object[] data;
		Dimension size;
		JToolBar toolbar;
		JSplitPane split;
		ScriptEditorAreaBuilder builder;

		/* Ausdruck */

		tabs.add(Language.tr("ExpressionCalculator.Tab.Expression"),tab=new JPanel(new BorderLayout()));
		tab.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ExpressionCalculator.Expression")+":",(initialExpression==null)?"":initialExpression);
		sub.add(line=(JPanel)data[0]);
		expressionEdit=(JTextField)data[1];
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {recalc();}
			@Override public void keyReleased(KeyEvent e) {recalc();}
			@Override public void keyPressed(KeyEvent e) {recalc();}
		});
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,false,model,model.surface),BorderLayout.EAST);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ExpressionCalculator.Results")+":","");
		sub.add(line=(JPanel)data[0]);
		resultsEdit=(JTextField)data[1];
		resultsEdit.setEditable(false);

		final JButton button=new JButton("");
		button.setIcon(Images.EDIT_COPY.getIcon());
		button.setToolTipText(Language.tr("ExpressionCalculator.Results.Copy"));
		size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));
		button.addActionListener(e->copyResultToClipboard());
		line.add(button,BorderLayout.EAST);

		/* Javascript */

		tabs.add(Language.tr("ExpressionCalculator.Tab.Javascript"),sub=new JPanel(new BorderLayout()));
		sub.add(split=new JSplitPane());
		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split.setTopComponent(tab=new JPanel(new BorderLayout()));

		toolbar=new JToolBar();
		toolbar.setFloatable(false);
		buttonJavaScriptNew=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.New"),Images.SCRIPT_NEW.getIcon(),Language.tr("ExpressionCalculator.Toolbar.New.Hint"));
		buttonJavaScriptLoad=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.Load"),Images.SCRIPT_LOAD.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Load.Hint"));
		buttonJavaScriptSave=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.Save"),Images.SCRIPT_SAVE.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Save.Hint"));
		toolbar.addSeparator();
		buttonJavaScriptTools=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.Tools"),Images.SCRIPT_TOOLS.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Tools.Hint"));
		toolbar.addSeparator();
		buttonJavaScriptRun=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.Run"),Images.SCRIPT_RUN.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Run.Hint"));
		toolbar.addSeparator();
		buttonJavaScriptHelp=addToolbarButton(toolbar,Language.tr("Main.Toolbar.Help"),Images.HELP.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Help.Hint"));
		tab.add(toolbar,BorderLayout.NORTH);

		builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Javascript);
		builder.addFileDropper(new ButtonListener());
		builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Simulation);
		builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Output);
		builder.setText((initialJavaScript==null || initialJavaScript.isEmpty())?DEFAULT_JAVA_SCRIPT:initialJavaScript);
		tab.add(new JScrollPane(scriptJavaScriptEdit=builder.get()),BorderLayout.CENTER);
		lastJavaScript=scriptJavaScriptEdit.getText();

		split.setBottomComponent(sub=new JPanel(new BorderLayout()));
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("ExpressionCalculator.Results")+":"));
		sub.add(new JScrollPane(scriptJavaScriptResults=new JTextArea("")),BorderLayout.CENTER);
		scriptJavaScriptResults.setEditable(false);

		split.setDividerLocation(0.66);
		split.setResizeWeight(0.75);

		/* if (DynamicClass.isWindows()) { - brauchen wir nicht mehr bei fully intern */
		if (DynamicFactory.isWindows() || DynamicFactory.isInMemoryProcessing()) {
			/* Java */

			tabs.add(Language.tr("ExpressionCalculator.Tab.Java"),sub=new JPanel(new BorderLayout()));
			sub.add(split=new JSplitPane());
			split.setOrientation(JSplitPane.VERTICAL_SPLIT);
			split.setTopComponent(tab=new JPanel(new BorderLayout()));

			toolbar=new JToolBar();
			toolbar.setFloatable(false);
			buttonJavaNew=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.NewJava"),Images.SCRIPT_NEW.getIcon(),Language.tr("ExpressionCalculator.Toolbar.NewJava.Hint"));
			buttonJavaLoad=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.LoadJava"),Images.SCRIPT_LOAD.getIcon(),Language.tr("ExpressionCalculator.Toolbar.LoadJava.Hint"));
			buttonJavaSave=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.SaveJava"),Images.SCRIPT_SAVE.getIcon(),Language.tr("ExpressionCalculator.Toolbar.SaveJava.Hint"));
			toolbar.addSeparator();
			buttonJavaTools=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.ToolsJava"),Images.SCRIPT_TOOLS.getIcon(),Language.tr("ExpressionCalculator.Toolbar.ToolsJava.Hint"));
			toolbar.addSeparator();
			buttonJavaRun=addToolbarButton(toolbar,Language.tr("ExpressionCalculator.Toolbar.RunJava"),Images.SCRIPT_RUN.getIcon(),Language.tr("ExpressionCalculator.Toolbar.RunJava.Hint"));
			toolbar.addSeparator();
			buttonJavaHelp=addToolbarButton(toolbar,Language.tr("Main.Toolbar.Help"),Images.HELP.getIcon(),Language.tr("ExpressionCalculator.Toolbar.Help.Hint"));
			tab.add(toolbar,BorderLayout.NORTH);

			builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Java);
			builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Simulation);
			builder.addAutoCompleteFeature(ScriptPopup.ScriptFeature.Output);
			builder.addFileDropper(new ButtonListener());
			builder.setText((initialJava==null || initialJava.isEmpty())?DEFAULT_JAVA:initialJava);
			tab.add(new JScrollPane(scriptJavaEdit=builder.get()),BorderLayout.CENTER);
			lastJava=scriptJavaEdit.getText();

			split.setBottomComponent(sub=new JPanel(new BorderLayout()));
			sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			line.add(new JLabel(Language.tr("ExpressionCalculator.Results")+":"));
			sub.add(new JScrollPane(scriptJavaResults=new JTextArea("")),BorderLayout.CENTER);
			scriptJavaResults.setEditable(false);

			split.setDividerLocation(0.66);
			split.setResizeWeight(0.75);
		} else {
			buttonJavaNew=null;
			buttonJavaLoad=null;
			buttonJavaSave=null;
			buttonJavaTools=null;
			buttonJavaRun=null;
			buttonJavaHelp=null;
			scriptJavaEdit=null;
			scriptJavaResults=null;
		}

		/* Icons f�r Tabs */

		tabs.setIconAt(0,Images.SCRIPT_MODE_EXPRESSION.getIcon());
		tabs.setIconAt(1,Images.SCRIPT_MODE_JAVASCRIPT.getIcon());
		if (DynamicFactory.isWindows() || DynamicFactory.isInMemoryProcessing()) {
			tabs.setIconAt(2,Images.SCRIPT_MODE_JAVA.getIcon());
		}

		/* Start */

		if (initialTab>=0 && initialTab<tabs.getTabCount()) tabs.setSelectedIndex(initialTab);

		recalc();

		setMinSizeRespectingScreensize(550,400);
		pack();
		size=getSize();
		setSize(size);
		setMinimumSize(size);
		setResizable(true);
		setLocationRelativeTo(this.owner);
	}

	private JButton addToolbarButton(final JToolBar toolbar, final String title, final Icon icon, final String hint) {
		final JButton button=new JButton(title);
		toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		if (icon!=null) button.setIcon(icon);
		button.addActionListener(new ButtonListener());
		return button;
	}

	private void recalc() {
		final String expression=expressionEdit.getText().trim();

		if (expression.isEmpty()) {
			resultsEdit.setText(Language.tr("ExpressionCalculator.Results.NoExpression"));
			return;
		}

		final Double D=calc.apply(expression);
		if (D==null) {
			resultsEdit.setText(Language.tr("ExpressionCalculator.Results.NoResult"));
			return;
		}

		resultsEdit.setText(NumberTools.formatNumberMax(D.doubleValue()));
	}

	private void copyResultToClipboard() {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(resultsEdit.getText()),null);
	}

	/**
	 * Liefert die 0-basierende Nummer des zuletzt angezeigten Tabs
	 * (z.B. zur Wiederverwendung beim n�chsten �ffnen des Dialogs).
	 * @return	0-basierende Nummer des zuletzt angezeigten Tabs
	 */
	public int getLastMode() {
		return tabs.getSelectedIndex();
	}

	/**
	 * Liefert den zuletzt in das Eingabefeld eingegebenen Ausdruck
	 * (z.B. zur Wiederverwendung beim n�chsten �ffnen des Dialogs).
	 * @return	Letzter eingegebener Ausdruck
	 */
	public String getLastExpression() {
		return expressionEdit.getText().trim();
	}

	/**
	 * Liefert das zuletzt in das JavaSkript-Eingabefeld eingegebene Skript
	 * (z.B. zur Wiederverwendung beim n�chsten �ffnen des Dialogs).
	 * @return	Letztes eingegebenes JavaSkript
	 */
	public String getLastJavaScript() {
		return scriptJavaScriptEdit.getText().trim();
	}

	/**
	 * Liefert das zuletzt in das Java-Eingabefeld eingegebene Skript
	 * (z.B. zur Wiederverwendung beim n�chsten �ffnen des Dialogs).
	 * @return	Letztes eingegebener Java-Code
	 */
	public String getLastJava() {
		return scriptJavaEdit.getText().trim();
	}

	private boolean allowDiscardJavaScript() {
		if (lastJavaScript.equals(scriptJavaScriptEdit.getText())) return true;
		switch (MsgBox.confirmSave(this,Language.tr("ExpressionCalculator.DiscardConfirmationJavascript.Title"),Language.tr("ExpressionCalculator.DiscardConfirmationJavascript.Info"))) {
		case JOptionPane.YES_OPTION: commandSaveJavaScript(); return allowDiscardJavaScript();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	private boolean allowDiscardJava() {
		if (lastJava.equals(scriptJavaEdit.getText())) return true;
		switch (MsgBox.confirmSave(this,Language.tr("ExpressionCalculator.DiscardConfirmationJava.Title"),Language.tr("ExpressionCalculator.DiscardConfirmationJava.Info"))) {
		case JOptionPane.YES_OPTION: commandSaveJava(); return allowDiscardJava();
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	private void commandLoadJavaScript(File file) {
		if (file==null) {
			JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			FileFilter filter;
			fc.setDialogTitle(Language.tr("FileType.Load.JS"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0 && fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".js");
		}

		final String text=JSRunDataFilterTools.loadText(file);
		if (text==null) return;
		scriptJavaScriptEdit.setText(text);
		lastJavaScript=scriptJavaScriptEdit.getText();
	}

	private void commandLoadJava(File file) {
		if (file==null) {
			JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			FileFilter filter;
			fc.setDialogTitle(Language.tr("FileType.Load.Java"));
			filter=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0 && fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".java");
		}

		final String text=JSRunDataFilterTools.loadText(file);
		if (text==null) return;
		scriptJavaEdit.setText(text);
		lastJava=scriptJavaEdit.getText();
	}

	private void commandSaveJavaScript() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		FileFilter filter;
		fc.setDialogTitle(Language.tr("FileType.Save.JS"));
		filter=new FileNameExtensionFilter(Language.tr("FileType.JS")+" (*.js)","js");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".js");
		}
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		if (!JSRunDataFilterTools.saveText(scriptJavaScriptEdit.getText(),file,false)) return;
		lastJavaScript=scriptJavaScriptEdit.getText();
	}

	private void commandSaveJava() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		FileFilter filter;
		fc.setDialogTitle(Language.tr("FileType.Save.Java"));
		filter=new FileNameExtensionFilter(Language.tr("FileType.Java")+" (*.java)","java");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".java");
		}
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		if (!JSRunDataFilterTools.saveText(scriptJavaEdit.getText(),file,false)) return;
		lastJava=scriptJavaEdit.getText();
	}

	private void commandRunJavaScript() {
		final String s=runJavaScript.apply(scriptJavaScriptEdit.getText().trim());
		scriptJavaScriptResults.setText(s);
	}

	private void commandRunJava() {
		final String s=runJava.apply(scriptJavaEdit.getText().trim());
		scriptJavaResults.setText(s);
	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (readOnly) return;

			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final File file=data.getFile();
				if (file.isFile()) {
					if (data.getDropComponent()==scriptJavaScriptEdit) {
						if (allowDiscardJavaScript()) commandLoadJavaScript(file);
					}
					if (data.getDropComponent()==scriptJavaEdit) {
						if (allowDiscardJava()) commandLoadJava(file);
					}
					data.dragDropConsumed();
				}
				return;
			}

			/* Javascript */

			if (e.getSource()==buttonJavaScriptNew) {
				if (allowDiscardJavaScript()) {
					scriptJavaScriptEdit.setText("");
					lastJavaScript=scriptJavaScriptEdit.getText();
				}
				return;
			}

			if (e.getSource()==buttonJavaScriptLoad) {
				if (allowDiscardJavaScript()) commandLoadJavaScript(null);
				return;
			}

			if (e.getSource()==buttonJavaScriptSave) {
				commandSaveJavaScript();
				return;
			}

			if (e.getSource()==buttonJavaScriptRun) {
				commandRunJavaScript();
				return;
			}

			if (e.getSource()==buttonJavaScriptTools) {
				final ScriptPopup popup=new ScriptPopup(buttonJavaScriptTools,model,ScriptPopup.ScriptMode.Javascript,null);
				popup.addFeature(ScriptPopup.ScriptFeature.Simulation);
				popup.addFeature(ScriptPopup.ScriptFeature.Output);
				popup.build();
				popup.show(scriptJavaScriptEdit);
				return;
			}

			if (e.getSource()==buttonJavaScriptHelp) {
				Help.topicModal(ExpressionCalculatorDialog.this,"JS");
				return;
			}

			/* Java */

			if (e.getSource()==buttonJavaNew) {
				if (allowDiscardJava()) {
					scriptJavaEdit.setText("");
					lastJava=scriptJavaEdit.getText();
				}
				return;
			}

			if (e.getSource()==buttonJavaLoad) {
				if (allowDiscardJava()) commandLoadJava(null);
				return;
			}

			if (e.getSource()==buttonJavaSave) {
				commandSaveJava();
				return;
			}

			if (e.getSource()==buttonJavaTools) {
				final ScriptPopup popup=new ScriptPopup(buttonJavaTools,model,ScriptPopup.ScriptMode.Java,null);
				popup.addFeature(ScriptPopup.ScriptFeature.Simulation);
				popup.addFeature(ScriptPopup.ScriptFeature.Output);
				popup.build();
				popup.show(scriptJavaEdit);
				return;
			}

			if (e.getSource()==buttonJavaRun) {
				commandRunJava();
				return;
			}

			if (e.getSource()==buttonJavaHelp) {
				Help.topicModal(ExpressionCalculatorDialog.this,"Java");
				return;
			}
		}
	}
}