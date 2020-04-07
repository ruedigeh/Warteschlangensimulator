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

/**
 * Liefert die aktuelle Anzahl an Kunden, die gerade an Station id (1. Parameter) bedient werden.<br>
 * @author Alexander Herzog
 */
public class CalcSymbolStationDataProcessCount extends CalcSymbolStationData {
	@Override
	public String[] getNames() {
		return new String[]{"Process"};
	}

	@Override
	protected double calc(RunElementData data) {
		if (data==null) return 0.0;
		return data.clientsAtStation-data.clientsAtStationQueue;
	}
}
