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
package scripting.js;

import org.w3c.dom.Document;

import language.Language;
import simulator.Simulator;
import simulator.statistics.Statistics;

/**
 * Javascript-Interpreter zur Filterung von Statistikausgaben
 * @author Alexander Herzog
 */
public final class JSRunDataFilter {
	private final Document xml;
	private boolean lastSuccess;
	private String lastResults;

	/**
	 * Konstruktor der Klasse
	 * @param xml	XMl-Statistik-Daten, die gefiltert werden sollen
	 */
	public JSRunDataFilter(final Document xml) {
		this.xml=xml;
		lastSuccess=false;
		lastResults="";
	}

	/**
	 * Führt ein Skript aus
	 * @param script	Auszuführendes Skript
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	public boolean run(final String script) {
		final JSBuilder builder=new JSBuilder(2_000);
		final Statistics statistics=new Statistics();
		statistics.loadFromXML(xml.getDocumentElement());
		final JSCommandSystem commandSystem;
		builder.addBinding("System",commandSystem=new JSCommandSystem());
		commandSystem.setSimulationData(Simulator.getSimulationDataFromStatistics(statistics),null);
		builder.addBinding("Output",new JSCommandOutput(builder.output,false));
		builder.addBinding("Statistics",new JSCommandXML(builder.output,xml,false));
		final JSEngine runner=builder.build();
		if (runner==null) {
			lastResults=String.format(Language.tr("Statistics.Filter.EngineInitError"),builder.engineName.name);
			return false;
		}

		if (!runner.initScript(script)) {
			lastResults=Language.tr("Statistics.Filter.ScriptInitError");
			return false;
		}
		lastSuccess=runner.run();
		lastResults=runner.getResult();
		return lastSuccess;
	}

	/**
	 * Liefert die Ausgabe des Skriptes.
	 * @return	Ausgabe des Skriptes
	 */
	public String getResults() {
		return lastResults;
	}

	/**
	 * Gibt an, ob die letzte Skriptausführung erfolgreich war.
	 * @return	Erfolg der letzten	Skriptausführung
	 */
	public boolean getLastSuccess() {
		return lastSuccess;
	}

	/**
	 * Liefert das dem Filter zu Grunde liegende XML-Dokument zurück
	 * @return	Verwendetes XML-Dokument
	 */
	public Document getXMLDocument() {
		return xml;
	}
}
