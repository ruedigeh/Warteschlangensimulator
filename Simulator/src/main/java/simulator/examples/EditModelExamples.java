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
package simulator.examples;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.w3c.dom.Document;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import tools.SetupData;
import ui.commandline.CommandBenchmark;

/**
 * Liefert Beispielmodelle
 * @author Alexander Herzog
 */
public class EditModelExamples {
	/**
	 * Kategorien f�r die Beispiele
	 * @author Alexander Herzog
	 */
	public enum ExampleType {
		/**
		 * Standardbeispiele
		 */
		TYPE_DEFAULT,

		/**
		 * Beispiele, die bestimmte Modellierungseigenschaften verdeutlichen
		 */
		TYPE_PROPERTIES,

		/**
		 * Beispiele zum Vergleich verschiedener Steuerungsstrategien
		 */
		TYPE_COMPARE,

		/**
		 * Beispiele, die sich auf reale Modelle bzw. Fragen beziehen
		 */
		TYPE_REAL_MODELS,

		/**
		 * Beispiele, die mathematische Zusammenh�nge verdeutlichen
		 */
		TYPE_MATH
	}

	private final List<Example> list;

	private EditModelExamples() {
		list=new ArrayList<>();
		addExamples();
	}

	/**
	 * Liefert den Namen des Beispielmodells das f�r Benchmarks verwendet werden soll.
	 * @return	Benchmark-Beispielmodell
	 * @see CommandBenchmark
	 */
	public static String getBenchmarkExampleName() {
		return Language.tr("Examples.SystemDesign");
	}

	/**
	 * Liefert den Namen einer Gruppe
	 * @param group	Gruppe deren Name bestimmt werden soll
	 * @return	Name der angegebenen Gruppe
	 */
	public static String getGroupName(final ExampleType group) {
		switch (group) {
		case TYPE_DEFAULT: return Language.tr("Examples.Type.Simple");
		case TYPE_PROPERTIES: return Language.tr("Examples.Type.Properties");
		case TYPE_COMPARE: return Language.tr("Examples.Type.Compare");
		case TYPE_REAL_MODELS: return Language.tr("Examples.Type.Real");
		case TYPE_MATH: return Language.tr("Examples.Type.Mathematics");
		default: return Language.tr("Examples.Type.Unknown");
		}
	}

	/**
	 * Liefert die Namen der Beispiele in einer bestimmten Gruppe
	 * @param group	Gruppe f�r die die Beispiele aufgelistet werden sollen
	 * @return	Liste der Namen der Beispiele in der gew�hlten Gruppe
	 */
	public static List<String> getExampleNames(final ExampleType group) {
		final EditModelExamples examples=new EditModelExamples();
		final List<String> result=new ArrayList<>();
		for (Example example: examples.list) if (example.type==group) result.add(example.names[0]);
		return result;
	}

	private void addExample(final String[] names, final String file, final ExampleType type, final boolean availableForPlayer) {
		list.add(new Example(names,file,type,availableForPlayer));
	}

	private void addExamples() {
		addExample(Language.trAll("Examples.ErlangC"),"ErlangC1.xml",ExampleType.TYPE_DEFAULT,true);
		addExample(Language.trAll("Examples.ClientTypePriorities"),"Kundentypen.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.ImpatientClientsAndRetry"),"Warteabbrecher.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.SharedResources"),"SharedResources.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.SystemDesign"),"Vergleiche2.xml",ExampleType.TYPE_COMPARE,true);
		addExample(Language.trAll("Examples.SystemDesignWithControl"),"Vergleiche3.xml",ExampleType.TYPE_COMPARE,true);
		addExample(Language.trAll("Examples.PushAndPullProduction"),"PushPull.xml",ExampleType.TYPE_COMPARE,true);
		addExample(Language.trAll("Examples.PushAndPullProductionMultiBarriers"),"PushPullMulti.xml",ExampleType.TYPE_COMPARE,true);
		addExample(Language.trAll("Examples.PushPullThroughput"),"PushPullThroughput.xml",ExampleType.TYPE_COMPARE,true);
		addExample(Language.trAll("Examples.ChangeResourceCountCompare"),"ChangeResourceCountCompare.xml",ExampleType.TYPE_COMPARE,false);
		addExample(Language.trAll("Examples.LimitedNumberOfClientsAtAStation"),"Variable.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.LawOfLargeNumbers"),"GesetzDerGro�enZahlen.xml",ExampleType.TYPE_MATH,true);
		addExample(Language.trAll("Examples.Galton"),"Galton.xml",ExampleType.TYPE_MATH,true);
		addExample(Language.trAll("Examples.Callcenter"),"Callcenter.xml",ExampleType.TYPE_REAL_MODELS,true);
		addExample(Language.trAll("Examples.OperatorsAsSimulationObjects"),"BedienerAlsSimulationsobjekte.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.Transport"),"Transport.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.Transporter"),"Transporter.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.CombiningOrdersAndItems"),"MultiSignalBarrier.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.Restaurant"),"Restaurant.xml",ExampleType.TYPE_REAL_MODELS,true);
		addExample(Language.trAll("Examples.Baustellenampel"),"Baustellenampel.xml",ExampleType.TYPE_REAL_MODELS,true);
		addExample(Language.trAll("Examples.Batch"),"Batch.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.Failure"),"Failure.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.SetUpTimes"),"SetUpTimes.xml",ExampleType.TYPE_PROPERTIES,false);
		addExample(Language.trAll("Examples.HoldJS"),"HoldJS.xml",ExampleType.TYPE_PROPERTIES,false);
		addExample(Language.trAll("Examples.RestrictedBuffer"),"RestriktierterPuffer.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.Analog"),"Analog.xml",ExampleType.TYPE_PROPERTIES,true);
		addExample(Language.trAll("Examples.PASTA"),"PASTA.xml",ExampleType.TYPE_MATH,true);
		addExample(Language.trAll("Examples.ZentralerGrenzwertsatz"),"ZentralerGrenzwertsatz.xml",ExampleType.TYPE_MATH,true);
	}

	/**
	 * Liefert eine Liste mit allen verf�gbaren Beispielen.
	 * @return	Liste mit allen verf�gbaren Beispielen
	 */
	public static String[] getExamplesList() {
		final EditModelExamples examples=new EditModelExamples();
		return examples.list.stream().map(example->example.names[0]).toArray(String[]::new);
	}

	/**
	 * Liefert eine Liste mit allen f�r den Player verf�gbaren Beispielen.
	 * @return	Liste mit allen f�r den Player verf�gbaren Beispielen
	 */
	public static String[] getPlayerExamplesList() {
		final EditModelExamples examples=new EditModelExamples();
		return examples.list.stream().filter(example->example.availableForPlayer).map(example->example.names[1]+" / "+example.names[0]).toArray(String[]::new);
	}

	/**
	 * Liefert den Index des Beispiels basieren auf dem Namen
	 * @param name	Name des Beispiels zu dem der Index bestimmt werden soll
	 * @return	Index des Beispiels oder -1, wenn es kein Beispiel mit diesem Namen gibt
	 * @see #getExampleByIndex(Component, int)
	 */
	public static int getExampleIndexFromName(final String name) {
		if (name==null || name.isEmpty()) return -1;
		final EditModelExamples examples=new EditModelExamples();

		for (int i=0;i<examples.list.size();i++) {
			for (String test: examples.list.get(i).names) if (name.trim().equalsIgnoreCase(test)) return i;
		}
		return -1;
	}

	/**
	 * Liefert ein bestimmtes Beispiel �ber seine Nummer aus der Namesliste (<code>getExamplesList()</code>)
	 * @param owner	�bergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> �bergeben, so werden Fehlermeldungen auf der Konsole ausgegeben
	 * @param index	Index des Beispiels, das zur�ckgeliefert werden soll
	 * @return	Beispiel oder <code>null</code>, wenn sich der Index au�erhalb des g�ltigen Bereichs befindet
	 * @see EditModelExamples#getExamplesList()
	 */
	public static EditModel getExampleByIndex(final Component owner, final int index) {
		final EditModelExamples examples=new EditModelExamples();
		if (index<0 || index>=examples.list.size()) return null;
		final String fileName=examples.list.get(index).file;

		final EditModel editModel=new EditModel();
		try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+Language.tr("Numbers.Language")+"/"+fileName)) {
			final String error=editModel.loadFromStream(in);
			if (error!=null) {
				if (owner==null) {
					System.out.println(error);
				} else {
					MsgBox.error(owner,Language.tr("XML.LoadErrorTitle"),error);
				}
				return null;
			}
			return editModel;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Liefert ein bestimmtes Beispiel �ber seine Nummer aus der Namesliste (<code>getPlayerExamplesList()</code>)
	 * @param index	Index des Beispiels, das zur�ckgeliefert werden soll
	 * @param languageKey	Sprache des Beispiels; "de" oder "en"
	 * @return	Beispiel oder <code>null</code>, wenn sich der Index au�erhalb des g�ltigen Bereichs befindet
	 * @see EditModelExamples#getPlayerExamplesList()
	 */
	public static EditModel getPlayerExampleByIndex(final int index, final String languageKey) {
		final EditModelExamples examples=new EditModelExamples();
		final Example[] list=examples.list.stream().filter(example->example.availableForPlayer).toArray(Example[]::new);

		if (index<0 || index>=list.length) return null;
		final String fileName=list[index].file;

		final EditModel editModel=new EditModel();
		try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+languageKey+"/"+fileName)) {
			editModel.loadFromStream(in);
			return editModel;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Pr�ft, ob das �bergebene Modell mit einem der Beispielmodelle �bereinstimmt.<br>
	 * Es werden dabei alle Sprachen f�r den Vergleich herangezogen.
	 * @param editModel	Model, bei dem gepr�ft werden soll, ob es mit einem der Beispiele �bereinstimmt
	 * @return	Index des Beispielmodells oder -1, wenn das zu pr�fende Modell mit keinem der Beispielmodelle �bereinstimmt
	 */
	public static int equalsIndex(final EditModel editModel) {
		final EditModelExamples examples=new EditModelExamples();

		for (int i=0;i<examples.list.size();i++) for (String lang: Language.getLanguages()) {
			final EditModel testModel=new EditModel();
			try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+lang+"/"+examples.list.get(i).file)) {
				testModel.loadFromStream(in);
				if (testModel.equalsEditModel(editModel)) return i;
			} catch (IOException e) {return -1;}
		}
		return -1;
	}

	private void addGroupToMenu(final Component owner, final JMenu menu, final Consumer<EditModel> listener, final ExampleType group) {
		if (menu.getItemCount()>0) menu.addSeparator();

		final JMenuItem caption=new JMenuItem(getGroupName(group));
		menu.add(caption);
		Font font=caption.getFont();
		font=new Font(font.getName(),Font.BOLD,font.getSize());
		caption.setFont(font);
		caption.setEnabled(false);
		caption.setForeground(Color.BLACK);

		for (Example example: list) if (example.type==group) {
			final JMenuItem item=new JMenuItem(example.names[0]);
			item.addActionListener(e->{
				final EditModel editModel=new EditModel();
				try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+Language.tr("Numbers.Language")+"/"+example.file)) {
					final String error=editModel.loadFromStream(in);
					if (error!=null) {
						if (owner==null) {
							System.out.println(error);
						} else {
							MsgBox.error(owner,Language.tr("XML.LoadErrorTitle"),error);
						}
						return;
					}
					if (listener!=null) listener.accept(editModel);
				} catch (IOException e1) {}
			});
			menu.add(item);
		}
	}

	/**
	 * F�gt alle Beispiele zu einem Men� hinzu
	 * @param owner	�bergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> �bergeben, so werden Fehlermeldungen auf der Konsole ausgegeben
	 * @param menu	Men�, in dem die Beispiele als Unterpunkte eingef�gt werden sollen
	 * @param listener	Listener, der mit einem Modell aufgerufen wird, wenn dieses geladen werden soll.
	 */
	public static void addToMenu(final Component owner, final JMenu menu, final Consumer<EditModel> listener) {
		final EditModelExamples examples=new EditModelExamples();
		for (ExampleType type: ExampleType.values()) examples.addGroupToMenu(owner,menu,listener,type);
	}

	/**
	 * Pr�ft, dass sich die Modelle durch eine �nderung der Systemsprache inhaltlich nicht �ndern
	 * und gibt die Ergebnisse �ber <code>System.out</code> aus.
	 * @param names	Namen der Sprachen
	 * @param setLanguage	Runnables, um die Sprache umzustellen
	 */
	public static void runLanguageTest(final String[] names, final Runnable[] setLanguage) {
		String error;

		for (String exampleName: EditModelExamples.getExamplesList()) {
			System.out.println("Teste Beispiel \""+exampleName+"\"");

			setLanguage[0].run();

			final EditModel example=EditModelExamples.getExampleByIndex(null,EditModelExamples.getExampleIndexFromName(exampleName));
			Document doc=example.saveToXMLDocument();

			if (!example.equalsEditModel(example.clone())) {
				System.out.println("  Modell ist nicht mit direkter Kopie von sich identisch.");
				continue;
			}

			EditModel model;

			boolean abort=false;
			for (int index=1;index<names.length;index++) {
				setLanguage[index].run();

				model=new EditModel();
				error=model.loadFromXML(doc.getDocumentElement());
				if (error!=null) {
					System.out.println("  Fehler beim Laden des "+names[index-1]+" Modells im "+names[index]+" Setup:");
					System.out.println("  "+error);
					abort=true;
					break;
				}

				if (!model.equalsEditModel(example)) {
					System.out.println("  "+names[index]+" Modell und Ausgangsmodell sind nicht mehr identisch.");
					abort=true;
					break;
				}

				doc=model.saveToXMLDocument();
			}
			if (abort) continue;

			setLanguage[0].run();

			final EditModel finalModel=new EditModel();
			error=finalModel.loadFromXML(doc.getDocumentElement());
			if (error!=null) {
				System.out.println("  Fehler beim Laden des "+names[names.length-1]+" Modells im "+names[0]+" Setup:");
				System.out.println("  "+error);
				continue;
			}

			if (!finalModel.equalsEditModel(example)) {
				System.out.println("  Konvertiertes Modell entspricht nicht mehr dem Ausgangsmodell.");
				continue;
			}
		}
	}

	/**
	 * Pr�ft, ob sich die Modelle durch eine �nderung der Systemsprache inhaltlich nicht �ndern
	 * und gibt die Ergebnisse �ber <code>System.out</code> aus.
	 */
	public static void runLanguageTestAll() {
		final SetupData setup=SetupData.getSetup();
		final String saveLanguage=setup.language;

		final String[] names=Language.getLanguages();
		final Runnable[] changers=Arrays.asList(names).stream().map(name->new Runnable() {
			@Override public void run() {setup.setLanguage(name);}
		}).toArray(Runnable[]::new);
		runLanguageTest(names,changers);

		setup.setLanguage(saveLanguage);
		System.out.println("done.");
	}

	/**
	 * Liefert eine Liste aller Beispiele
	 * @return	Liste aller Beispiele
	 */
	public static List<Example> getList() {
		final EditModelExamples examples=new EditModelExamples();
		return examples.list;
	}

	/**
	 * Beispielmodell
	 * @author Alexander Herzog
	 */
	public static class Example {
		/**
		 * Namen f�r das Beispiel in den verschiedenen Sprachen
		 */
		public final String[] names;

		/**
		 * Beispieldateiname
		 */
		public final String file;

		/**
		 * Gruppe in die das Beispiel f�llt
		 */
		public final ExampleType type;

		/**
		 * Ist das Beispiel auch im Player verf�gbar?
		 */
		public final boolean availableForPlayer;

		private Example(final String[] names, final String file, final ExampleType type, final boolean availableForPlayer) {
			this.names=names;
			this.file=file;
			this.type=type;
			this.availableForPlayer=availableForPlayer;
		}
	}
}