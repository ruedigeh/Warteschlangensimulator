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
package simulator.logging;

import java.awt.Color;
import java.io.File;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

import simcore.SimData;
import simcore.logging.SimLogging;

/**
 * Schreibt die Logging-Daten in eine ODS-Datei.
 * @author Alexander Herzog
 * @see SimLogging
 * @see SimData#getLogger
 */
public class ODSLogger implements SimLogging {
	private final File logFile;
	private final boolean groupSameTimeEvents;
	private final boolean singleLineMode;
	private final boolean useColors;
	private long lastEventTime=-1;

	private final SpreadsheetDocument workbook;
	private final Table sheet;

	private SimLogging nextLogger;

	/**
	 * Konstruktor der Klasse <code>ODSLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param headings	Auszugebende Überschriftzeilen
	 */
	public ODSLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final String[] headings) {
		this.logFile=logFile;
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useColors=useColors;

		String[] h;
		if (headings==null || headings.length==0) h=new String[]{"Simulationsergebnisse"}; else h=headings;

		SpreadsheetDocument workbook;
		try {
			workbook=SpreadsheetDocument.newSpreadsheetDocument();
		} catch (Exception e) {
			workbook=null;
		}
		this.workbook=workbook;

		if (workbook!=null) {
			if (workbook.getTableList().size()>0) {
				sheet=workbook.getTableList().get(0);
			} else {
				sheet=workbook.addTable();
			}

			if (sheet.getRowCount()>0) sheet.removeRowsByIndex(0,sheet.getRowCount());

			/* Überschriften */
			for (int i=0;i<h.length;i++) {
				Row row=sheet.appendRow();
				Cell cell=row.getCellByIndex(0);
				cell.setFont(getCellStyle(Color.BLACK,true));
				cell.setStringValue(h[i]);
			}
			/* Leerzeile */
			sheet.appendRow();
		} else {
			sheet=null;
		}
	}

	@Override
	public boolean ready() {
		return true;
	}

	private Font getCellStyle(Color color, final boolean bold) {
		final Font font;
		if (bold) {
			font=new Font("Arial",StyleTypeDefinitions.FontStyle.BOLD,12);
		} else {
			font=new Font("Arial",StyleTypeDefinitions.FontStyle.REGULAR,12);
		}
		if (useColors) font.setColor(new org.odftoolkit.odfdom.type.Color(color.getRed(),color.getGreen(),color.getBlue()));
		return font;
	}

	@Override
	public boolean log(long time, Color color, String event, String info) {
		/* Abschnitt beginnen / beenden */
		if (groupSameTimeEvents) {
			if (lastEventTime!=time) {
				if (lastEventTime>=0) sheet.appendRow();
				Row row=sheet.appendRow();
				final Cell cell=row.getCellByIndex(0);
				cell.setFont(getCellStyle(Color.BLACK,true));
				cell.setStringValue(SimData.formatSimTime(time));
				/* Leerzeile */ sheet.appendRow();
				lastEventTime=time;
			}
		}

		/* Daten ausgeben */
		if (singleLineMode) {
			Row row=sheet.appendRow();
			int colCount=0;
			Cell cell;
			if (!groupSameTimeEvents) {
				cell=row.getCellByIndex(colCount); colCount++;
				cell.setFont(getCellStyle(color,false));
				cell.setStringValue(SimData.formatSimTime(time));
			}
			if (event!=null && !event.isEmpty()) {
				cell=row.getCellByIndex(colCount); colCount++;
				cell.setFont(getCellStyle(color,true));
				cell.setStringValue(event);
			}
			if (info!=null && !info.isEmpty()) {
				cell=row.getCellByIndex(colCount); colCount++;
				cell.setFont(getCellStyle(color,false));
				cell.setStringValue(info);
			}
		} else {
			Row row=sheet.appendRow();
			int colCount=0;
			Cell cell;
			if (!groupSameTimeEvents) {
				cell=row.getCellByIndex(colCount); colCount++;
				cell.setFont(getCellStyle(color,false));
				cell.setStringValue(SimData.formatSimTime(time));
			}
			if (event!=null && !event.isEmpty()) {
				cell=row.getCellByIndex(colCount); colCount++;
				cell.setFont(getCellStyle(color,true));
				cell.setStringValue(event);
			}
			if (info!=null && !info.isEmpty()) {
				if (event!=null && !event.isEmpty()) {
					row=sheet.appendRow();
					colCount--;
				}
				cell=row.getCellByIndex(colCount); colCount++;
				cell.setFont(getCellStyle(color,false));
				cell.setStringValue(info);
			}
		}

		if (nextLogger!=null) nextLogger.log(time,color,event,info);

		return true;
	}

	@Override
	public boolean done() {
		if (nextLogger!=null) nextLogger.done();

		if (workbook!=null) try {
			workbook.save(logFile);
			workbook.close();
		} catch (Exception e) {return false;}

		return true;
	}

	@Override
	public void setNextLogger(final SimLogging logger) {
		nextLogger=logger;
	}

	@Override
	public SimLogging getNextLogger() {
		return nextLogger;
	}
}