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
package ui.modeleditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog zur Änderung der ID eines <code>ModeleElementBox</code>-Elements.<br>
 * Dieser Dialog wird von <code>ModelElementBaseDialog</code> verwendet, wenn dieser mit einem Objekt, dessen Typ
 * sich von <code>ModelElementBox</code> ableitet, instanziert wird.
 * @author Alexander Herzog
 * @see ModelElementBaseDialog
 */
public class ModelElementBaseIDDialog extends BaseDialog {
	private static final long serialVersionUID = -8329216683933004322L;

	private final int oldID;
	private final ModelSurface mainSurface;
	private final JTextField editID;
	private final JLabel info;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Runnable, das aufgerufen wird, wenn der Nutzer auf die Hilfe-Schaltfläche klickt
	 * @param oldID	Bisherige ID des Elements
	 * @param mainSurface	Hauptebenen Surface
	 */
	public ModelElementBaseIDDialog(final Component owner, final Runnable help, final int oldID, final ModelSurface mainSurface) {
		super(owner,Language.tr("Editor.DialogBase.ID.Title"));

		this.oldID=oldID;
		this.mainSurface=mainSurface;

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.DialogBase.ID.NewID")+":",""+oldID,10);
		content.add((JPanel)data[0]);
		editID=(JTextField)data[1];
		editID.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(info=new JLabel());

		checkData(false);
		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private void setInfo(final String text) {
		info.setText("<html><bod>"+text.replaceAll("\\n","<br>")+"</body></html>");
		pack();
	}

	private boolean checkData(final boolean showErrorMessage) {
		final Integer I=NumberTools.getInteger(editID,true);
		if (I==null || I.intValue()<1) {
			editID.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Editor.DialogBase.ID.InvalidID"),Language.tr("Editor.DialogBase.ID.InvalidID.Info"));
			setInfo(Language.tr("Editor.DialogBase.ID.InvalidID.Info"));
			return false;
		} else {
			editID.setBackground(SystemColor.text);
		}

		if (I.intValue()==oldID) {
			setInfo(Language.tr("Editor.DialogBase.ID.EqualsOldID"));
		} else {
			final ModelElement otherElement=mainSurface.getByIdIncludingSubModels(I.intValue());
			if (otherElement==null) {
				setInfo(Language.tr("Editor.DialogBase.ID.NewIDNotInUse"));
			} else {
				setInfo(Language.tr("Editor.DialogBase.ID.NewIDInUse"));
			}
		}

		return true;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert, wenn der Dialog mit "Ok" geschlossen wurde, die neue ID.
	 * @return	Neue ID oder -1, wenn der Dialog abgebrochen wurde.
	 */
	public int getNewID() {
		if (getClosedBy()!=CLOSED_BY_OK) return -1;
		return NumberTools.getInteger(editID,true);
	}
}
