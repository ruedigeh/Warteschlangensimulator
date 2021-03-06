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

import java.util.function.Consumer;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import parser.MathCalcError;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.js.JSRunDataFilter;
import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.statistics.Statistics;
import ui.ModelChanger;

/**
 * F�hrt die Verarbeitung eines einzelnen Modells
 * f�r die Parameter-Variationsstudien-Funktion durch
 * @author Alexander Herzog
 * @see ParameterCompareRunner
 */
public class ParameterCompareRunnerModel {
	/**
	 * @see ParameterCompareRunnerModel#getStatus()
	 */
	public enum Status {
		/** Verarbeitung wurde noch nicht gestartet */
		STATUS_WAITING,

		/** Verarbeitung l�uft */
		STATUS_RUNNING,

		/** Verarbeitung wurde abgebrochen */
		STATUS_CANCELED,

		/** Verarbeitung wurde erfolgreich abgeschlossen */
		STATUS_DONE
	}

	private final int nr;
	private final Consumer<ParameterCompareRunnerModel> whenDone;
	private final Consumer<String> logOutput;
	private final ParameterCompareSetup setup;
	private final String[] outputScripts;

	private volatile Status status;

	private volatile AnySimulator simulator;

	private ParameterCompareSetupModel model;
	private EditModel changedModel;

	/**
	 * Konstruktor der Klasse
	 * @param nr	Nummer des Modells in der Liste (0-basierend)
	 * @param whenDone	Wird aufgerufen, wenn die Verarbeitung erfolgreich abgeschlossen wurde
	 * @param logOutput	Callback �ber das Meldungen (z.B. zum Misserfolg von Skripten) ausgegeben werden
	 * @param setup	Parameter-Variationsstudien-Setup
	 * @param outputScripts	Liste mit den Ausgabeskripten
	 */
	public ParameterCompareRunnerModel(final int nr, final Consumer<ParameterCompareRunnerModel> whenDone, final Consumer<String> logOutput, final ParameterCompareSetup setup, final String[] outputScripts) {
		this.nr=nr;
		this.whenDone=whenDone;
		this.logOutput=logOutput;
		this.setup=setup;
		this.outputScripts=outputScripts;

		status=Status.STATUS_WAITING;
	}

	/**
	 * Liefert die Nummer dieses Modells in der Liste aller Variationsstudien-Modelle
	 * @return	Nummer des Modells
	 */
	public int getNr() {
		return nr;
	}

	/**
	 * Liefert den Namen des zu bearbeitenden Modells
	 * @return	Name des Modells
	 */
	public String getName() {
		if (model==null) return ""; else return model.getName();
	}

	private synchronized void logOutput(final String message) {
		if (logOutput!=null) logOutput.accept(message);
	}

	private Double calcResultValue(final Statistics statistics, final ParameterCompareSetupValueOutput output) {
		switch (output.getMode()) {
		case MODE_XML:
			final String s=ModelChanger.getStatisticValue(statistics,output.getTag());
			if (s==null) {
				logOutput(String.format(Language.tr("ParameterCompare.ResultsError.NoValue"),getNr()+1,getName(),output.getTag()));
				return null;
			}
			final Double D=NumberTools.getDouble(s);
			if (D==null) {
				logOutput(String.format(Language.tr("ParameterCompare.ResultsError.ValueNotNumber"),getNr()+1,getName(),output.getTag(),s));
			}
			return D;
		case MODE_SCRIPT_JS:
			/* Skripte werden vorab geladen und von einer anderen Funktion verarbeitet */
			return null;
		case MODE_SCRIPT_JAVA:
			/* Skripte werden vorab geladen und von einer anderen Funktion verarbeitet */
			return null;
		case MODE_COMMAND:
			ExpressionCalc calc=new ExpressionCalc(null);
			if (calc.parse(output.getTag())>=0) return null;
			try {
				return calc.calc(statistics);
			} catch (MathCalcError e) {
				return null;
			}
		default:
			return null;
		}
	}

	private Double calcResultValueByScriptJS(final Statistics statistics, final String script) {
		final JSRunDataFilter filter=new JSRunDataFilter(statistics.saveToXMLDocument());
		filter.run(script);
		if (!filter.getLastSuccess()) {
			logOutput(String.format(Language.tr("ParameterCompare.ResultsError.ScriptError"),getNr()+1,getName(),filter.getResults()));
			return null;
		} else {
			final String s=filter.getResults();
			final Double D=NumberTools.getDouble(s);
			if (D==null) {
				logOutput(String.format(Language.tr("ParameterCompare.ResultsError.ScriptValueNotNumber"),getNr()+1,getName(),s));
			}
			return D;
		}
	}

	private Double calcResultValueByScriptJava(final Statistics statistics, final String script) {
		final DynamicRunner runner=DynamicFactory.getFactory().load(script);
		if (runner.getStatus()!=DynamicStatus.OK) {
			logOutput(DynamicFactory.getLongStatusText(runner));
			return null;
		}

		final StringBuilder results=new StringBuilder();
		runner.parameter.output=new OutputImpl(line->results.append(line),false);
		runner.parameter.statistics=new StatisticsImpl(line->results.append(line),statistics.saveToXMLDocument(),false);
		runner.run();
		if (runner.getStatus()!=DynamicStatus.OK) {
			logOutput(String.format(Language.tr("ParameterCompare.ResultsError.ScriptError"),getNr()+1,getName(),DynamicFactory.getLongStatusText(runner)));
			return null;
		}

		final String s=results.toString().trim();
		final Double D=NumberTools.getDouble(s);
		if (D==null) {
			logOutput(String.format(Language.tr("ParameterCompare.ResultsError.ScriptValueNotNumber"),getNr()+1,getName(),s));
		}
		return D;
	}

	private void processResults(final Statistics statistics) {
		if (statistics!=null) for (int i=0;i<setup.getOutput().size();i++) {
			final ParameterCompareSetupValueOutput output=setup.getOutput().get(i);
			final Double value;
			switch (output.getMode()) {
			case MODE_XML:
			case MODE_COMMAND:
				value=calcResultValue(statistics,output);
				break;
			case MODE_SCRIPT_JS:
				value=calcResultValueByScriptJS(statistics,outputScripts[i]);
				break;
			case MODE_SCRIPT_JAVA:
				value=calcResultValueByScriptJava(statistics,outputScripts[i]);
				break;
			default:
				value=null;
				break;
			}
			if (value!=null) model.getOutput().put(output.getName(),value);
		}

		model.setStatistics(statistics);
	}

	/**
	 * Liefert den aktuellen Verarbeitungsstatus
	 * @return	Aktueller Status
	 */
	public Status getStatus() {
		if (simulator!=null) {
			final int percent=(int)FastMath.min(100,(simulator.getCurrentClients()*100/FastMath.max(1,simulator.getCountClients())));
			model.setInProcess(percent);
			if (!simulator.isRunning()) {
				processResults(simulator.getStatistic());
				simulator=null;
				status=Status.STATUS_DONE;
				model.setInProcess(-1);
				if (whenDone!=null) whenDone.accept(this);
			}
		} else {
			model.setInProcess(-1);
		}

		return status;
	}

	/**
	 * Bereitet ein Modell f�r die Simulation vor
	 * @param baseModel	Ausgangs-Editor-Modell
	 * @param model	Zu verwendende Variationsstudien-Einstellungen
	 * @return	Liefert im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung
	 */
	public String prepare(final EditModel baseModel, final ParameterCompareSetupModel model) {
		this.model=model;

		/* Bisherige Ergebnisse l�schen */
		model.clearOutputs();

		/* Modell verarbeiten */
		EditModel changedModel=baseModel.clone();
		for (int i=0;i<setup.getInput().size();i++) {
			final ParameterCompareSetupValueInput input=setup.getInput().get(i);
			final Double oldValue=ParameterCompareTools.getModelValue(changedModel,input);
			if (oldValue==null) return String.format(Language.tr("ParameterCompare.Run.Error.ReadValue"),getNr()+1,getName(),i+1,input.getName());
			final Double newValue=model.getInput().get(input.getName());
			if (newValue!=null) {
				final Object obj=ParameterCompareTools.setModelValue(changedModel,input,newValue);
				if (obj==null) return String.format(Language.tr("ParameterCompare.Run.Error.WriteValue"),getNr()+1,getName(),i,input.getName());
				if (obj instanceof String) return String.format(Language.tr("ParameterCompare.Run.Error.WriteValueMessage"),getNr()+1,getName(),i+1,input.getName())+((String)obj);
				changedModel=(EditModel)obj;
			}
		}

		this.changedModel=changedModel;
		return null;
	}

	/**
	 * Startet die Verarbeitung
	 * @return Fehlermeldung oder im Erfolgsfall <code>null</code>
	 */
	public String start() {
		final StartAnySimulator starter=new StartAnySimulator(changedModel);
		final String error=starter.prepare();
		if (error!=null) {
			simulator=null;
			status=Status.STATUS_DONE;
			model.setInProcess(-1);
		} else {
			status=Status.STATUS_RUNNING;
			model.setInProcess(0);
			simulator=starter.start();
		}
		return error;
	}

	/**
	 * Bricht die Verarbeitung ab
	 */
	public void cancel() {
		if (simulator!=null) {
			simulator.cancel();
			simulator=null;
			model.setInProcess(-1);
			status=Status.STATUS_DONE;
		}
	}
}
