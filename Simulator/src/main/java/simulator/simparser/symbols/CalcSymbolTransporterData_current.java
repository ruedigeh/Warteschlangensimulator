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

import simulator.simparser.coresymbols.CalcSymbolTransporterData;
import statistics.StatisticsTimePerformanceIndicator;

/**
 * Im Falle von einem Parameter:<br>
 * Liefert die Anzahl der momentan belegten Transporter in der angegebenen Transportergruppe.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die Anzahl der momentan belegten Transporter in allen Transportergruppen zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolTransporterData_current extends CalcSymbolTransporterData {

	@Override
	public String[] getNames() {
		return new String[]{"transporter","transporter_utilization"};
	}

	@Override
	protected boolean hasAllTransporterData() {return true;}

	@Override
	protected double calcAllTransporters(StatisticsTimePerformanceIndicator[] statistics) {
		int sum=0;
		for (StatisticsTimePerformanceIndicator indicator: statistics) sum+=indicator.getCurrentState();
		return sum;
	}

	@Override
	protected double calcSingleTransporterGroup(final StatisticsTimePerformanceIndicator statistics) {
		return statistics.getCurrentState();
	}
}
