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
package parser.symbols;

import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Vorzeichen einer Zahl<br>
 * Liefert bei Werten &lt;0 als Ergebnis -1.<br>
 * Liefert bei Werten &gt;0 als Ergebnis 1.<br>
 * Und liefert bei 0 als Ergebnis 0.
 * @author Alexander Herzog
 */
public final class CalcSymbolPreOperatorSign extends CalcSymbolPreOperator {
	private static Double minus=-1.0;

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length!=1) return null;
		final double sign=Math.signum(parameters[0]);
		if (sign<0) return minus;
		return fastBoxedValue(sign);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=1) return fallbackValue;
		final double sign=Math.signum(parameters[0]);
		if (sign<0) return minus;
		return sign;
	}

	@Override
	public String[] getNames() {
		return new String[]{"signum","sign","vorzeichen","sgn"};
	}
}