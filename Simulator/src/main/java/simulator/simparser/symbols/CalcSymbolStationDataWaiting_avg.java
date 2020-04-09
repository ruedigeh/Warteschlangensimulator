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
package simulator.simparser.symbols;

import simulator.coreelements.RunElementData;
import simulator.simparser.coresymbols.CalcSymbolStationData;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert die mittlere Wartezeit der Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt (in Sekunden).<br>
 * (b) Liefert die mittlere Wartezeit der Kunden an Station id (1. Parameter) (in Sekunden).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die mittlere Wartezeit �ber alle Kunden (in Sekunden).
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataWaiting_avg extends CalcSymbolStationData {
	@Override
	public String[] getNames() {
		return new String[]{
				"Wartezeit_avg","Wartezeit_average","Wartezeit_Mittelwert",
				"WaitingTime_avg","WaitingTime_average","WaitingTime_Mittelwert"
		};
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticWaiting==null) return 0;
		return data.statisticWaiting.getMean();
	}

	@Override
	protected boolean hasAllData() {
		return true;
	}

	@Override
	protected boolean hasSingleClientData() {
		return true;
	}

	@Override
	protected double calcAll() {
		return getSimData().statistics.clientsAllWaitingTimes.getMean();
	}

	@Override
	protected double calcSingleClient(final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsWaitingTimes.get(name);
		if (indicator==null) return 0.0;
		return ((StatisticsDataPerformanceIndicator)indicator).getMean();
	}
}