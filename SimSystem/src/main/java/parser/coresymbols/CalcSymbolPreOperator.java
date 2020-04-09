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
package parser.coresymbols;

import parser.CalcSystem;

/**
 * Abstrakte Basisklasse f�r Funktionen (d.h. vorangestellte Operatoren)
 * @author Alexander Herzog
 */
public abstract class CalcSymbolPreOperator extends CalcSymbolFunction {
	private static final CalcSymbol[] emptyParameters=new CalcSymbol[0];

	/**
	 * Gibt an, ob Parameter eingestellt wurden
	 */
	protected boolean parametersSet=false;

	/**
	 * Parameter der Funktion
	 */
	protected CalcSymbol[] symbols=emptyParameters;

	@Override
	public final SymbolType getType() {
		return CalcSymbol.SymbolType.TYPE_FUNCTION;
	}

	@Override
	public final int getPriority() {
		return parametersSet?0:10;
	}

	@Override
	public final boolean setParameter(CalcSymbol[] symbols) {
		if (symbols==null) return false;

		if (symbols.length==1 && symbols[0] instanceof CalcSymbolSub) {
			this.symbols=((CalcSymbolSub)(symbols[0])).getData();
		} else {
			this.symbols=symbols;
		}
		parametersSet=true;
		return true;
	}

	/**
	 * Direkter Zugriff auf das Rechensystem.<br>
	 * Steht nach dem ersten Aufruf von {@link CalcSymbolPreOperator#getValue(CalcSystem)}
	 * oder {@link CalcSymbolPreOperator#getValueOrDefault(CalcSystem, double)} zur Verf�gung.
	 */
	protected CalcSystem calcSystem;

	/**
	 * Versucht die Funktion zu berechnen, wenn die Zahlenwerte der Parameter bekannt sind
	 * @param parameters 	Zahlenwerte der Parameter
	 * @return	Liefert im Erfolgsfall das Ergebnis, sonst <code>null</code>
	 */
	protected abstract Double calc(final double[] parameters);

	/**
	 * Versucht die Funktion zu berechnen, wenn die Zahlenwerte der Parameter bekannt sind
	 * @param parameters 	Zahlenwerte der Parameter
	 * @param fallbackValue	Vorgabewert, der zur�ckgeliefert werden soll, wenn die Berechnung nicht ausgef�hrt werden konnte
	 * @return	Liefert im Erfolgsfall das Ergebnis der Berechnung, sonst den angegebenen Vorgabewert
	 */
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final Double D=calc(parameters);
		if (D==null) return fallbackValue;
		return D.doubleValue();
	}

	private double[] lastValues;

	/**
	 * Gibt an, ob es sich bei allen Parametern der Funktion um Konstanten handelt.
	 * @see #getParameterValues(CalcSystem)
	 */
	protected boolean allValuesConst=false;

	/**
	 * Berechnet die Werte der Parameter der Funktion
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @return	Array mit den Werten der Parameter (kann als Ganzes <code>null</code> sein, wenn die Berechnung fehlgeschlagen ist)
	 */
	protected final double[] getParameterValues(final CalcSystem calc) {
		double[] values;
		if (lastValues==null || lastValues.length!=symbols.length) {
			values=new double[symbols.length];
			lastValues=values;
			allValuesConst=false;
		} else {
			values=lastValues;
		}

		if (allValuesConst) return values;

		allValuesConst=true;
		for (int i=0;i<symbols.length;i++) {
			final CalcSymbol symbol=symbols[i];
			if (symbol instanceof CalcSymbolConst) {
				values[i]=((CalcSymbolConst)symbol).getValue();
			} else {
				allValuesConst=false;
				if (symbol instanceof CalcSymbolDirectValue) {
					if (((CalcSymbolDirectValue)symbol).getValueDirectOk(calc)) {
						values[i]=((CalcSymbolDirectValue)symbol).getValueDirect(calc);
					} else {
						final Double value=symbol.getValue(calc);
						if (value==null) return null;
						values[i]=value;
					}
				} else {
					final Double value=symbol.getValue(calc);
					if (value==null) return null;
					values[i]=value;
				}
			}
		}
		return values;
	}

	@Override
	public final Double getValue(final CalcSystem calc) {
		if (symbols==null) return null;

		calcSystem=calc;
		final double[] values=getParameterValues(calc);
		if (values==null) return null;

		return calc(values);
	}

	/**
	 * Liefert den Wert des Symbols
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @param fallbackValue	Vorgabewert, der zur�ckgeliefert werden soll, wenn die Berechnung nicht ausgef�hrt werden konnte
	 * @return	Aktueller Wert des Symbols oder Vorgabewert, wenn der Wert nicht berechnet werden konnte
	 * @see CalcSymbol#getValue(CalcSystem)
	 * @see CalcSystem#calcOrDefault(double[], double)
	 */
	public double getValueOrDefault(final CalcSystem calc, final double fallbackValue) {
		if (symbols==null) return fallbackValue;

		calcSystem=calc;

		double[] values;
		if (lastValues==null || lastValues.length!=symbols.length) {
			values=new double[symbols.length];
			lastValues=values;
		} else {
			values=lastValues;
		}

		if (!allValuesConst) {
			allValuesConst=true;
			for (int i=0;i<symbols.length;i++) {
				if (symbols[i] instanceof CalcSymbolConst) {
					values[i]=((CalcSymbolConst)symbols[i]).getValue();
				} else {
					allValuesConst=false;
					if (symbols[i] instanceof CalcSymbolVariable) {
						if (!((CalcSymbolVariable)symbols[i]).getValueDirectOk(calc)) return fallbackValue;
						values[i]=((CalcSymbolVariable)symbols[i]).getValueDirect(calc);
					} else {
						Double value=symbols[i].getValue(calc);
						if (value==null) return fallbackValue;
						values[i]=value;
					}
				}
			}
		}
		return calcOrDefault(values,fallbackValue);
	}

	@Override
	public final CalcSymbol cloneSymbol() {
		CalcSymbolPreOperator clone=(CalcSymbolPreOperator)super.cloneSymbol();
		clone.symbols=new CalcSymbol[symbols.length];
		for (int i=0;i<symbols.length;i++) if (symbols[i]!=null) clone.symbols[i]=symbols[i].cloneSymbol();
		return clone;
	}

	private Double getSimpleConstSub() {
		if (symbols.length!=1 || symbols[0]==null) return null;
		final Object subSimple=symbols[0].getSimplify();
		if (subSimple instanceof Double) return (Double)subSimple;
		if (subSimple instanceof CalcSymbolConst) return ((CalcSymbolConst)subSimple).getValue();
		return null;
	}

	/**
	 * Gibt an, ob die Funktion als solches deterministisch ist, also bei gleichen Parametern auch
	 * stets dasselbe Ergebnis liefert (was z.B. bei "Random()" nicht der Fall ist). Wenn dies der
	 * Fall ist und die Parameter konstante Werte sind, wird die gesamte Funktion durch
	 * {@link CalcSymbolPreOperator#getSimplify()} zu einem konstanten Wert vereinfacht.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Funktion deterministisch arbeitet. (Dies ist der Normalfall. Nur f�r nichtdeterministische Funktionen muss diese Methode �berschrieben werden.)
	 */
	protected boolean isDeterministic() {
		return true;
	}

	@Override
	public Object getSimplify() {
		if (isDeterministic()) {
			final Double sub=getSimpleConstSub();
			if (sub!=null) {
				final Double result=calc(new double[]{sub.doubleValue()});
				if (result!=null) return result;
			}
		}

		Object[] obj=new Object[symbols.length];
		for (int i=0;i<obj.length;i++) {
			obj[i]=symbols[i].getSimplify();
			if (obj[i] instanceof Double) {
				final CalcSymbolNumber number=new CalcSymbolNumber();
				number.setValue(((Double)obj[i]));
				obj[i]=number;
			}
			if (!(obj[i] instanceof CalcSymbol)) return this;
		}

		try {
			final CalcSymbolPreOperator clone=(CalcSymbolPreOperator)clone();
			clone.symbols=new CalcSymbol[obj.length];
			for (int i=0;i<obj.length;i++) clone.symbols[i]=(CalcSymbol)obj[i];
			clone.parametersSet=parametersSet;
			return clone;
		} catch (CloneNotSupportedException e) {return this;}
	}
}