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
package ui;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import ui.modeleditor.ModelResource;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;

/**
 * Versucht ein Modell automatisiert (nach Nutzerr�ckfrage) zu reparieren.
 * @author Alexander Herzog
 */
public class EditorPanelRepair {
	private enum RepairState {NOT_CHANGED, FIXED, USER_CANCELED}

	private final EditorPanel editorPanel;

	/**
	 * Konstruktor der Klasse
	 * @param editorPanel	Editor-Panel aus dem das Modell entnommen und das korrigierte zur�ckgegeben werden soll
	 */
	public EditorPanelRepair(final EditorPanel editorPanel) {
		this.editorPanel=editorPanel;
	}

	private void reloadToEditor(final EditModel model) {
		final File file=editorPanel.getLastFile();
		editorPanel.setModel(model);
		editorPanel.setLastFile(file);
		editorPanel.setModelChanged(true);
	}

	private RepairState fixResources(final EditModel model) {
		/* Enth�lt das Modell eine einzelne Bedienstation? */
		final List<ModelElementProcess> process=new ArrayList<>();
		for (ModelElement element: model.surface.getElements()) if (element instanceof ModelElementProcess) {
			process.add((ModelElementProcess)element);
		}

		RepairState state=RepairState.NOT_CHANGED;

		for (ModelElementProcess p: process) {

			if (process.size()==0) return RepairState.NOT_CHANGED;
			final String processName=(p.getName().trim().isEmpty())?("id="+p.getId()):("\""+p.getName()+"\" (id="+p.getId()+")");

			/* Keine Bediener? */
			if (p.getNeededResources().size()!=1) continue;
			if (p.getNeededResources().get(0).size()!=0) continue;

			/* Name der AutoAdd-Ressource festlegen. */
			final String resourceName=String.format(Language.tr("Window.Check.AutoFixResources.ResourceName"),p.getId());

			/* Kein AutoAdd, wenn es die Ressource schon gibt. */
			ModelResource resource=model.resources.getNoAutoAdd(resourceName);
			if (resource!=null) continue;

			/* Nutzer fragen */
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixResources.Title"),String.format(Language.tr("Window.Check.AutoFixResources.Info"),processName),Language.tr("Window.Check.AutoFixResources.YesInfo"),Language.tr("Window.Check.AutoFixResources.NoInfo"))) return RepairState.USER_CANCELED;

			/* Ressource anlegen */
			resource=model.resources.get(resourceName);
			resource.clear();
			resource.setName(resourceName);

			/* Ressource in Station eintragen */
			p.getNeededResources().get(0).put(resourceName,1);

			state=RepairState.FIXED;
		}

		return state;
	}

	private RepairState fixRepeatCount(final EditModel model) {
		/* Nur eine Wiederholung? -> Nichts zu tun*/
		if (model.repeatCount<=1) return RepairState.NOT_CHANGED;

		/* Pr�fen, ob es Gr�nde gibt, dass das Modell nicht mehrfach simuliert werden kann. */
		final String noRepeat=model.getNoRepeatReason();
		if (noRepeat==null) return RepairState.NOT_CHANGED;

		/* Nutzer fragen */
		if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixRepeatCount.Title"),String.format(Language.tr("Window.Check.AutoFixRepeatCount.Info"),model.repeatCount,noRepeat),Language.tr("Window.Check.AutoFixRepeatCount.YesInfo"),Language.tr("Window.Check.AutoFixRepeatCount.NoInfo"))) return RepairState.USER_CANCELED;

		/* Anzahl an Wiederholungen auf 1 reduzieren */
		model.repeatCount=1;
		return RepairState.FIXED;
	}

	private RepairState fixConnections(final EditModel model) {
		final List<ModelElementSource> source=new ArrayList<>();
		final List<ModelElementDelay> delay=new ArrayList<>();
		final List<ModelElementProcess> process=new ArrayList<>();
		final List<ModelElementDispose> dispose=new ArrayList<>();
		int maxX=0;

		/* Elemente finden */
		for (ModelElement element:	model.surface.getElements()) {
			if (element instanceof ModelElementBox) {
				final int x=((ModelElementBox)element).getPosition(true).x;
				final int width=Math.max(0,((ModelElementBox)element).getSize().width);
				maxX=Math.max(maxX,x+width);
			}

			if (element instanceof ModelElementSource) {
				source.add((ModelElementSource)element);
				continue;
			}
			if (element instanceof ModelElementProcess) {
				process.add((ModelElementProcess)element);
				continue;
			}
			if (element instanceof ModelElementDelay) {
				delay.add((ModelElementDelay)element);
				continue;
			}
			if (element instanceof ModelElementDispose) {
				dispose.add((ModelElementDispose)element);
				continue;
			}
		}
		if (source.size()==0) return RepairState.NOT_CHANGED;
		if (process.size()==0 && delay.size()==0) return RepairState.NOT_CHANGED;

		RepairState state=RepairState.NOT_CHANGED;

		/* Modell hat kein Ausgang-Element? */
		if (dispose.size()==0) {
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixDispose.Title"),Language.tr("Window.Check.AutoFixDispose.Info"),Language.tr("Window.Check.AutoFixDispose.YesInfo"),Language.tr("Window.Check.AutoFixDispose.NoInfo"))) return RepairState.USER_CANCELED;
			final int x=maxX+100;
			int y=0;
			int ySum=0;
			for (ModelElementProcess p: process) {y=+p.getPosition(true).y; ySum++;}
			for (ModelElementDelay d: delay) {y=+d.getPosition(true).y; ySum++;}
			final ModelElementDispose d=new ModelElementDispose(model,model.surface);
			d.setPosition(new Point(x,y/ySum));
			model.surface.add(d);
			state=RepairState.FIXED;
			dispose.add(d);
		}

		/* Quelle hat keinen Ausgang? */
		for (ModelElementSource s: source) if (s.getEdgeOut()==null) {
			if (process.size()+delay.size()!=1) continue; /* Unklar, wohin wir verbinden wollen. */
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixConnection.Title"),String.format(Language.tr("Window.Check.AutoFixConnection.InfoSourceProcess"),s.getId()),Language.tr("Window.Check.AutoFixConnection.YesInfo"),Language.tr("Window.Check.AutoFixConnection.NoInfo"))) return RepairState.USER_CANCELED;
			ModelElementEdge edge=null;
			if (process.size()>0) {
				edge=new ModelElementEdge(model,model.surface,s,process.get(0));
				s.addEdgeOut(edge);
				process.get(0).addEdgeIn(edge);
			} else {
				if (delay.size()>0) {
					edge=new ModelElementEdge(model,model.surface,s,delay.get(0));
					s.addEdgeOut(edge);
					delay.get(0).addEdgeIn(edge);
				}
			}
			if (edge!=null) {
				model.surface.add(edge);
				state=RepairState.FIXED;
			}
		}

		/* Verz�gerungstation hat keinen Ausgang? */
		for (ModelElementDelay d: delay) if (d.getEdgeOut()==null) {
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixConnection.Title"),String.format(Language.tr("Window.Check.AutoFixConnection.InfoDelayDispose"),d.getId()),Language.tr("Window.Check.AutoFixConnection.YesInfo"),Language.tr("Window.Check.AutoFixConnection.NoInfo"))) return RepairState.USER_CANCELED;
			final ModelElementEdge edge=new ModelElementEdge(model,model.surface,d,dispose.get(0));
			d.addEdgeOut(edge);
			dispose.get(0).addEdgeIn(edge);
			model.surface.add(edge);
			state=RepairState.FIXED;
		}

		/* Bedienstation hat keinen Ausgang? */
		for (ModelElementProcess p: process) if (p.getEdgeOutSuccess()==null) {
			if (!MsgBox.confirm(editorPanel.getTopLevelAncestor(),Language.tr("Window.Check.AutoFixConnection.Title"),String.format(Language.tr("Window.Check.AutoFixConnection.InfoProcessDispose"),p.getId()),Language.tr("Window.Check.AutoFixConnection.YesInfo"),Language.tr("Window.Check.AutoFixConnection.NoInfo"))) return RepairState.USER_CANCELED;
			final ModelElementEdge edge=new ModelElementEdge(model,model.surface,p,dispose.get(0));
			p.addEdgeOut(edge);
			dispose.get(0).addEdgeIn(edge);
			model.surface.add(edge);
			state=RepairState.FIXED;
		}

		return state;
	}

	/**
	 * Versucht das Modell zu reparieren.
	 * @return	Gibt an, ob das Modell korrigiert wurde
	 */
	public boolean work() {
		final EditModel model=editorPanel.getModel();

		final List<Function<EditModel,RepairState>> fixFunctions=new ArrayList<>();
		fixFunctions.add(m->fixResources(m));
		fixFunctions.add(m->fixRepeatCount(m));
		fixFunctions.add(m->fixConnections(m));

		boolean fixed=false;
		for (Function<EditModel,RepairState> func: fixFunctions) {
			final RepairState state=func.apply(model);
			if (state==RepairState.USER_CANCELED) return false;
			if (state==RepairState.FIXED) fixed=true;
		}

		if (fixed) {
			reloadToEditor(model);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Hilfsroutine, die die Erstellung eines Objektes einspart.
	 * @param editorPanel	Editor-Panel aus dem das Modell entnommen und das korrigierte zur�ckgegeben werden soll
	 * @return	Gibt an, ob das Modell korrigiert wurde
	 */
	public static boolean autoFix(final EditorPanel editorPanel) {
		final EditorPanelRepair modelRepairer=new EditorPanelRepair(editorPanel);
		return modelRepairer.work();
	}
}
