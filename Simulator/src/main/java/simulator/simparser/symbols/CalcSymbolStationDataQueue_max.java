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
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * (a) Liefert die maximale Anzahl an wartenden Kunden, deren Name an Quelle bzw. Namenszuweisung id (1. Parameter) auftritt.<br>
 * (b) Liefert die maximale Anzahl an Kunden in der Warteschlange an Station id (1. Parameter).<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die maximale Anzahl an Kunden in allen Warteschlange zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataQueue_max extends CalcSymbolStationData {
	@Override
	public String[] getNames() {
		return new String[]{"NQ_max","Queue_max","Schlange_max","Warteschlange_max","NQ_Maximum","Queue_Maximum","Schlange_Maximum","Warteschlange_Maximum"};
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
		return getSimData().statistics.clientsInSystemQueues.getTimeMax();
	}

	@Override
	protected double calcSingleClient(final String name) {
		StatisticsPerformanceIndicator indicator=getSimData().statistics.clientsAtStationQueueByClient.get(name);
		if (indicator==null) return 0.0;
		return ((StatisticsTimePerformanceIndicator)indicator).getTimeMax();
	}

	@Override
	protected double calc(final RunElementData data) {
		if (data.statisticClientsAtStationQueue==null) return 0;
		return data.statisticClientsAtStationQueue.getTimeMax();
	}
}