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
package ui.statistics;

import java.net.URL;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import language.Language;
import mathtools.Table;
import simulator.statistics.Statistics;
import statistics.StatisticsDataCollector;
import systemtools.statistics.StatisticViewerJFreeChart;
import ui.help.Help;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementRecord;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Dieser Viewer stellt die Daten, die an einer Datenaufzeichnung-Station
 * w�hrend der Simulation erhoben wurden in Form eines X-Y-Punktediagramms dar.
 * @author Alexander Herzog
 * @see StatisticViewerJFreeChart
 * @see ModelElementRecord
 */
public class StatisticViewerXYPlot extends StatisticViewerJFreeChart {
	private final Statistics statistics;
	private final String data;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 * @param data	Name der Datenaufzeichnung-Station deren Daten hier angezeigt werden sollen
	 */
	public StatisticViewerXYPlot(final Statistics statistics, final String data) {
		super();
		this.statistics=statistics;
		this.data=data;
	}

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_XY;
	}

	@Override
	public void unZoom() {
		final XYPlot plot=(XYPlot)chart.getPlot();
		plot.getDomainAxis().setAutoRange(true);
		plot.getRangeAxis().setAutoRange(true);
	}

	private void addDescription(final String topic) {
		final URL url=StatisticViewerXYPlot.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	private final ModelElementRecord getStation(final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementRecord && element.getName().equals(data)) return (ModelElementRecord)element;
			if (element instanceof ModelElementSub) {
				final ModelElementRecord record=getStation(((ModelElementSub)element).getSubSurface());
				if (record!=null) return record;
			}
		}
		return null;
	}

	private String[] getExpressions() {
		final ModelElementRecord record=getStation(statistics.editModel.surface);
		if (record==null) return new String[]{Language.tr("Statistics.Value1"),Language.tr("Statistics.Value2")};
		return new String[]{record.getExpression1(),record.getExpression2()};
	}

	@Override
	protected void firstChartRequest() {
		final XYSeriesCollection dataset=new XYSeriesCollection();
		final XYSeries series=new XYSeries(data);

		final StatisticsDataCollector collector1=(StatisticsDataCollector)statistics.valueRecording.get(data+"-1");
		final StatisticsDataCollector collector2=(StatisticsDataCollector)statistics.valueRecording.get(data+"-2");
		final double[] xValues=collector1.getValuesReadOnly();
		final double[] yValues=collector2.getValuesReadOnly();
		int count=Math.min(Math.min(Math.min(xValues.length,yValues.length),collector1.getCount()),collector2.getCount());
		if (count>Table.MAX_EXCEL_ROW_COUNT) count=Table.MAX_EXCEL_ROW_COUNT;
		for (int i=0;i<count;i++) series.add(xValues[i],yValues[i],false);
		series.fireSeriesChanged();
		dataset.addSeries(series);

		final String[] axisLabels=getExpressions();
		initChart(ChartFactory.createScatterPlot(data,axisLabels[0],axisLabels[1],dataset));

		/* Infotext  */
		addDescription("PlotXY");
	}
}
