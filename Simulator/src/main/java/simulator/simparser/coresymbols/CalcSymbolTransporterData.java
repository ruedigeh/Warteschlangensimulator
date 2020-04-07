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
package simulator.simparser.coresymbols;

import org.apache.commons.math3.util.FastMath;

import simulator.runmodel.SimulationData;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Basisklasse f�r Funktionen, die Transporterdaten aus den Simulationsdaten auslesen.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolTransporterData extends CalcSymbolSimData {
	/**
	 * Gibt an, ob der Rechenbefehl Daten �ber alle Transportergruppen hinweg enth�lt
	 * @return	Wird hier <code>true</code> geliefert, so muss {@link #calcAllTransporters(StatisticsTimePerformanceIndicator[])} definiert sein
	 */
	protected boolean hasAllTransporterData() {
		return false;
	}

	/**
	 * Liefert Daten �ber alle Transportergruppen hinweg.
	 * @param statistics	Einzel-Statistik-Objekte die zusammengefasst werden sollen
	 * @return	Daten �ber alle Transportergruppen hinweg
	 * @see #hasAllTransporterData()
	 */
	protected double calcAllTransporters(final StatisticsTimePerformanceIndicator[] statistics) {
		return 0.0;
	}


	/**
	 * Liefert Daten f�r eine Transportergruppe.
	 * @param statistics	Statistik-Objekt f�r die Transportergruppe
	 * @return	Daten f�r eine Transportergruppe
	 */
	protected double calcSingleTransporterGroup(final StatisticsTimePerformanceIndicator statistics) {
		return 0.0;
	}

	@Override
	protected Double calc(double[] parameters) {
		final StatisticsTimePerformanceIndicator[] statistics=getTransporterUsageStatistics();
		if (statistics==null) return null;

		if (parameters.length==0 && hasAllTransporterData()) return fastBoxedValue(calcAllTransporters(statistics));

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			if (id<0 || id>=statistics.length) return null;
			return fastBoxedValue(calcSingleTransporterGroup(statistics[id]));
		}

		return null;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final StatisticsTimePerformanceIndicator[] statistics=getTransporterUsageStatistics();
		if (statistics==null) return fallbackValue;

		if (parameters.length==0 && hasAllTransporterData()) return calcAllTransporters(statistics);

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			if (id<0 || id>=statistics.length) return fallbackValue;
			return calcSingleTransporterGroup(statistics[id]);
		}

		return fallbackValue;
	}
}
