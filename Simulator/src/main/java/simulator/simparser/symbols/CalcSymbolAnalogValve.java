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

import org.apache.commons.math3.util.FastMath;

import simulator.coreelements.RunElement;
import simulator.elements.RunElementTank;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert den aktuellen maximalen Durchfluss an Ventil <code>nr</code> (1-basierend, 2. Parameter) an "Tank"-Element id (1. Parameter).
 * @author Alexander Herzog
 */
public class CalcSymbolAnalogValve extends CalcSymbolSimData {
	@Override
	public String[] getNames() {
		return new String[]{"VentilMaximalDurchfluss","ValveMaximumFlow"};
	}

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length!=2) return null;

		final RunElement element=getRunElementForID(parameters[0]);
		if (element==null) return null;
		if (!(element instanceof RunElementTank)) return null;
		final double[] maxFlow=((RunElementTank)element).getData(getSimData()).getValveValues();

		final int nr=(int)FastMath.round(parameters[1])-1;
		if (nr<0 || nr>=maxFlow.length) return null;

		return fastBoxedValue(maxFlow[nr]);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=2) return fallbackValue;

		final RunElement element=getRunElementForID(parameters[0]);
		if (element==null) return fallbackValue;
		if (!(element instanceof RunElementTank)) return fallbackValue;
		final double[] maxFlow=((RunElementTank)element).getData(getSimData()).getValveValues();

		final int nr=(int)FastMath.round(parameters[1])-1;
		if (nr<0 || nr>=maxFlow.length) return fallbackValue;

		return maxFlow[nr];
	}
}