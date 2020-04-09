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

import simulator.runmodel.RunDataClient;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert 0 oder 1 zur�ck in Abh�ngigkeit davon, ob der Kunde w�hrend der Einschwingphase generiert wurde (1) oder nicht (0).
 * @author Alexander Herzog
 */
public class CalcSymbolClientWarmUp extends CalcSymbolSimData {
	@Override
	public String[] getNames() {
		return new String[]{"WarmUpKunde","WarmUpClient","isWarmUpClient"};
	}

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length!=0) return null;
		final RunDataClient client=getCurrentClient();
		if (client==null) return null;
		return fastBoxedValue(client.isWarmUp?1:0);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=0) return fallbackValue;
		final RunDataClient client=getCurrentClient();
		if (client==null) return 0;
		return client.isWarmUp?1:0;
	}
}