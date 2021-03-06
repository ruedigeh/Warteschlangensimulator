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
package scripting.java;

/**
 * Teil-Interface, damit Nutzer-Java-Codes Ausgaben vornehmen kann.
 * @author Alexander Herzog
 * @see SimulationInterface
 */
public interface OutputInterface {

	/**
	 * Stellt das Ausgabeformat f�r Zahlen ein
	 * @param format	Zeichenkette, �ber die das Format (Dezimalkomma sowie optional Prozentwert) f�r Zahlenausgaben festgelegt wird
	 */
	void setFormat(final String format);

	/**
	 * Stellt ein, welches Trennzeichen zwischen den Werten bei der Ausgabe von Arrays verwendet werden soll
	 * @param separator	Bezeichner f�r das Trennzeichen
	 */
	void setSeparator(final String separator);

	/**
	 * Gibt einen String oder eine Zahl aus (ohne folgenden Zeilenumbruch)
	 * @param obj	Auszugebendes Objekt
	 */
	void print(final Object obj);

	/**
	 * Gibt einen String oder eine Zahl mit folgendem Zeilenumbruch aus
	 * @param obj	Auszugebendes Objekt
	 */
	void println(final Object obj);

	/**
	 * Gibt einen Zeilenumbruch aus
	 */
	void newLine();

	/**
	 * Gibt einen Tabulator aus
	 */
	void tab();

	/**
	 * Setzt den Abbruch-Status. (Nach einem Abbruch werden Dateiausgaben nicht mehr ausgef�hrt.)
	 */
	void cancel();

	/**
	 * Stellt ein, in welche Datei die Ausgabe erfolgen soll
	 * @param file	Ausgabedatei
	 */
	void setFile(final Object file);
}
