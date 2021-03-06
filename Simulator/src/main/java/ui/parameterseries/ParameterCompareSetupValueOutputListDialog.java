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
package ui.parameterseries;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.statistics.ListPopup;
import ui.statistics.ListPopup.XMLMode;

/**
 * Dieser Dialog erm�glicht das Einstellen der Ausgabewerte
 * f�r die Parameter-Vergleichs-Funktion
 * @author Alexander Herzog
 * @see ParameterComparePanel
 */
public class ParameterCompareSetupValueOutputListDialog extends ParameterCompareSetupValueBaseListDialog {
	private static final long serialVersionUID = 1863917114149952906L;

	private final Statistics miniStatistics;

	private final List<ParameterCompareSetupValueOutput> outputOriginal;
	private final List<ParameterCompareSetupValueOutput> output;

	/**
	 * Konstruktor der Klasse.<br>
	 * Macht den Dialog auch direkt sichtbar
	 * @param owner	�bergeordnetes Element
	 * @param model	Editor-Modell, welches die Basis f�r die Parameterstudie darstellt
	 * @param miniStatistics	Statistikdaten bezogen auf einen kurzen Lauf �ber das angegebene Editor-Modell (zur Auswahl von XML-Elementen als Zielwerte)
	 * @param output	Liste der Ausgabewerte-Einstellungen
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareSetupValueOutputListDialog(final Component owner, final EditModel model, final Statistics miniStatistics, final List<ParameterCompareSetupValueOutput> output, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.Settings.Output.List.Title"),model,help);
		this.miniStatistics=miniStatistics;

		this.outputOriginal=output;
		this.output=new ArrayList<>();
		for (ParameterCompareSetupValueOutput record: output) this.output.add(record.clone());

		initToolbar(
				Language.tr("ParameterCompare.Settings.Output.List.Add"),
				Language.tr("ParameterCompare.Settings.Output.List.Add.Hint"),
				Language.tr("ParameterCompare.Settings.Output.List.Edit"),
				Language.tr("ParameterCompare.Settings.Output.List.Edit.Hint"),
				Language.tr("ParameterCompare.Settings.Output.List.Delete"),
				Language.tr("ParameterCompare.Settings.Output.List.Delete.Hint"),
				Language.tr("ParameterCompare.Settings.Output.List.MoveUp"),
				Language.tr("ParameterCompare.Settings.Output.List.MoveUp.Hint"),
				Language.tr("ParameterCompare.Settings.Output.List.MoveDown"),
				Language.tr("ParameterCompare.Settings.Output.List.MoveDown.Hint"));

		start();
	}

	private String getOutputInfo(final ParameterCompareSetupValueOutput record) {
		switch (record.getMode()) {
		case MODE_XML: return String.format(Language.tr("ParameterCompare.Settings.Output.List.InfoXML"),record.getTag());
		case MODE_SCRIPT_JS: return String.format(Language.tr("ParameterCompare.Settings.Output.List.InfoScript"),record.getTag());
		case MODE_SCRIPT_JAVA: return String.format(Language.tr("ParameterCompare.Settings.Output.List.InfoScriptJava"),record.getTag());
		case MODE_COMMAND: return String.format(Language.tr("ParameterCompare.Settings.Output.List.InfoCommand"),record.getTag());
		default: return "";
		}
	}

	@Override
	protected DefaultListModel<JLabel> getListModel() {
		final DefaultListModel<JLabel> listModel=new DefaultListModel<>();

		for (ParameterCompareSetupValueOutput record: output) {
			final JLabel label=new JLabel();
			final StringBuilder sb=new StringBuilder();
			sb.append("<html><body>");
			sb.append(Language.tr("ParameterCompare.Table.Column.Output")+"<br>");
			String time="";
			if (record.getIsTime()) time=" ("+Language.tr("ParameterCompare.Table.Column.Output.IsTime")+")";
			sb.append("<b>"+record.getName()+"</b>"+time+"<br>");
			sb.append(getOutputInfo(record));
			sb.append("</html></body>");
			label.setText(sb.toString());
			label.setIcon(Images.PARAMETERSERIES_SETUP_OUTPUT.getIcon());
			listModel.addElement(label);
		}

		return listModel;
	}

	private boolean editOutput(final ParameterCompareSetupValueOutput record) {
		final ParameterCompareSetupValueOutputDialog dialog=new ParameterCompareSetupValueOutputDialog(owner,record,model,miniStatistics,help);
		return dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK;
	}

	@Override
	protected void addAddModesToMenu(final JButton anchor, final JPopupMenu popupMenu) {
		super.addAddModesToMenu(anchor,popupMenu);

		addAddButton(popupMenu,Language.tr("ParameterCompare.Settings.List.AddScript"),Language.tr("ParameterCompare.Settings.List.AddScript.Hint"),Images.PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVASCRIPT.getIcon(),1);
		addAddButton(popupMenu,Language.tr("ParameterCompare.Settings.List.AddScriptJava"),Language.tr("ParameterCompare.Settings.List.AddScriptJava.Hint"),Images.PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVA.getIcon(),2);
		addAddButton(popupMenu,Language.tr("ParameterCompare.Settings.List.AddExpression"),Language.tr("ParameterCompare.Settings.List.AddExpression.Hint"),Images.PARAMETERSERIES_OUTPUT_MODE_COMMAND.getIcon(),3);
	}

	@Override
	protected void addTemplatesToMenu(final JButton anchor, final JPopupMenu popupMenu) {
		final ListPopup helper=new ListPopup(anchor,help);
		helper.popupCustom(
				popupMenu,
				miniStatistics,
				record->addValueFromTemplate(recordToOutput(record)),
				record->!isParameterInUse(recordToOutput(record)));
	}

	@Override
	protected void commandAdd(final int nr) {
		final ParameterCompareSetupValueOutput record=new ParameterCompareSetupValueOutput();
		switch (nr) {
		case 0:
			record.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_XML);
			break;
		case 1:
			record.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_SCRIPT_JS);
			break;
		case 2:
			record.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_SCRIPT_JAVA);
			break;
		case 3:
			record.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_COMMAND);
			break;
		}
		record.setName(String.format(Language.tr("ParameterCompare.Settings.Output.Name.Default"),output.size()+1));
		if (!editOutput(record)) return;
		output.add(record);

		updateList(Integer.MAX_VALUE);
	}

	@Override
	protected void commandEdit(final int index) {
		if (!editOutput(output.get(index))) return;

		updateList(0);
	}

	@Override
	protected void commandDelete(final int index) {
		if (!MsgBox.confirm(
				this,
				Language.tr("ParameterCompare.Settings.Output.List.Delete.Confirm.Title"),
				String.format(Language.tr("ParameterCompare.Settings.Output.List.Delete.Confirm.Info"),output.get(index).getName()),
				Language.tr("ParameterCompare.Settings.Output.List.Delete.Confirm.YesInfo"),
				Language.tr("ParameterCompare.Settings.Output.List.Delete.Confirm.NoInfo"))) return;

		output.remove(index);
		updateList(-1);
	}

	@Override
	protected void commandSwap(final int index1, final int index2) {
		Collections.swap(output,index1,index2);
	}

	private boolean isParameterInUse(final ParameterCompareSetupValueOutput output) {
		for (ParameterCompareSetupValueOutput test: this.output) {
			if (test.getMode()!=output.getMode()) continue;
			if (!test.getTag().equals(output.getTag())) continue;
			if (test.getIsTime()!=output.getIsTime()) continue;
			return true;
		}
		return false;
	}

	private ParameterCompareSetupValueOutput recordToOutput(ListPopup.ScriptHelperRecord record) {
		final ParameterCompareSetupValueOutput output=new ParameterCompareSetupValueOutput();
		output.setName(record.title);
		output.setTag(record.xml);
		output.setIsTime(record.xmlMode==XMLMode.XML_NUMBER_TIME);
		return output;
	}

	private void addValueFromTemplate(final ParameterCompareSetupValueOutput template) {
		output.add(template);
		updateList(Integer.MAX_VALUE);
	}

	@Override
	public void storeData() {
		outputOriginal.clear();
		for (ParameterCompareSetupValueOutput record: output) outputOriginal.add(record.clone());
	}
}