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
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.EditorPanel;
import ui.help.Help;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * In diesem Dialog kann ein in einem {@link ModelElementSub}-Element
 * enthaltenes Unter-Modell bearbeitet werden.
 * @author Alexander Herzog
 * @see ModelElementSub
 */
public class ModelElementSubEditDialog extends BaseDialog {
	private static final long serialVersionUID = -6057942278443843417L;

	private final EditModel model;
	private final ModelSurface mainSurface;
	private final EditorPanel editorPanel;

	/**
	 * Konstruktor der Klasse <code>ModelElementSubEditDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param model	Element vom Typ <code>EditModel</code> (wird ben�tigt, um die Liste der globalen Variablen zu laden)
	 * @param mainSurface	Surface der obersten Ebene (enth�lt Ressourcen usw.)
	 * @param subSurface	Zu bearbeitendes Surface
	 * @param edgesIn	In das untergeordnete Modell einlaufende Ecken (mit ids)
	 * @param edgesOut	Aus dem untergeordneten Modell auslaufende Ecken (mit ids)
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 */
	public ModelElementSubEditDialog(final Component owner, final EditModel model, final ModelSurface mainSurface, final ModelSurface subSurface, final int[] edgesIn, final int[] edgesOut, final boolean readOnly) {
		super(owner,Language.tr("Surface.Sub.Dialog.Title"),readOnly);
		JPanel content=createGUI(()->Help.topicModal(ModelElementSubEditDialog.this,"ModelElementSub"));
		content.setBorder(null);
		content.setLayout(new BorderLayout());

		this.model=model;
		this.mainSurface=mainSurface;
		final EditModel ownModel=model.clone();
		ownModel.resources=mainSurface.getResources().clone();
		ownModel.surface=prepareSurface(subSurface,edgesIn,edgesOut);

		content.add(editorPanel=new EditorPanel(this,ownModel,readOnly,false,false),BorderLayout.CENTER);

		final InputMap im=content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap am=content.getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,InputEvent.CTRL_DOWN_MASK),"zoomOut");
		am.put("zoomOut",new AbstractAction() {
			private static final long serialVersionUID = -8149785411312199622L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.zoomOut();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,InputEvent.CTRL_DOWN_MASK),"zoomIn");
		am.put("zoomIn",new AbstractAction() {
			private static final long serialVersionUID = -4571322864625867012L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.zoomIn();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,InputEvent.CTRL_DOWN_MASK),"zoomDefault");
		am.put("zoomDefault",new AbstractAction() {
			private static final long serialVersionUID = -8292205126167185688L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.zoomDefault();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0,InputEvent.CTRL_DOWN_MASK),"center");
		am.put("center",new AbstractAction() {
			private static final long serialVersionUID = -6623607719135188208L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.centerModel();}
		});

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,InputEvent.CTRL_DOWN_MASK),"scrollTop");
		am.put("scrollTop",new AbstractAction() {
			private static final long serialVersionUID = 1935060801261351379L;
			@Override public void actionPerformed(ActionEvent e) {editorPanel.scrollToTop();}
		});

		setSizeRespectingScreensize(1024,768);
		setMinSizeRespectingScreensize(800,600);
		setResizable(true);
		setLocationRelativeTo(this.owner); /* this.owner==ownerWindow; owner==nur JPanel oder sowas */
	}

	private ModelSurface prepareSurface(final ModelSurface original, final int[] edgesIn, final int[] edgesOut) {
		ModelSurface prepared=original.clone(false,null,null,mainSurface,model);

		if (!readOnly) {

			/* �berz�hlige Ein- und Ausg�nge l�schen */
			List<ModelElement> deleteElements=new ArrayList<>();
			int countIn=0;
			int countOut=0;
			for (ModelElement element: prepared.getElements()) {
				if (element instanceof ModelElementSubIn) {
					countIn++;
					if (countIn>edgesIn.length) deleteElements.add(element); else {
						((ModelElementSubConnect)element).setConnectionData(countIn-1,edgesIn[countIn-1]);
					}
				}
				if (element instanceof ModelElementSubOut) {
					countOut++;
					if (countOut>edgesOut.length) deleteElements.add(element); else {
						((ModelElementSubConnect)element).setConnectionData(countOut-1,edgesOut[countOut-1]);
					}
				}
			}

			for (ModelElement element: deleteElements) prepared.remove(element);

			Point pos=new Point(50,50);
			/* Fehlende Verbindungen anlegen */
			for (int i=countIn;i<edgesIn.length;i++) {
				final ModelElementSubIn connectionIn=new ModelElementSubIn(model,prepared,i,edgesIn[i]);
				while (prepared.getElementAtPosition(pos,1.0)!=null) pos.x+=50;
				connectionIn.setPosition(pos);
				pos.x+=150;
				prepared.add(connectionIn);
			}
			for (int i=countOut;i<edgesOut.length;i++) {
				final ModelElementSubOut connectionOut=new ModelElementSubOut(model,prepared,i,edgesOut[i]);
				while (prepared.getElementAtPosition(pos,1.0)!=null) pos.x+=50;
				connectionOut.setPosition(pos);
				pos.x+=150;
				prepared.add(connectionOut);
			}
		}

		return prepared;
	}

	/**
	 * Liefert das Untermodell nach dem Schlie�en des Dialogs zur�ck
	 * @return	Untergeordnetes Modell
	 */
	public ModelSurface getSurface() {
		final EditModel model=editorPanel.getModel();
		model.surface.setSelectedElement(null);
		return model.surface;
	}
}