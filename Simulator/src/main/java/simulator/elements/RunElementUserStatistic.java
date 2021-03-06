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
package simulator.elements;

import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementUserStatistic;

/**
 * �quivalent zu <code>ModelElementUserStatistic</code>
 * @author Alexander Herzog
 * @see ModelElementUserStatistic
 */
public class RunElementUserStatistic extends RunElementPassThrough {
	private String[] keys;
	private boolean[] isTime;
	private String[] expressions;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementUserStatistic(final ModelElementUserStatistic element) {
		super(element,buildName(element,Language.tr("Simulation.Element.UserStatistic.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementUserStatistic)) return null;
		final ModelElementUserStatistic userStatisticElement=(ModelElementUserStatistic)element;
		final RunElementUserStatistic userStatistic=new RunElementUserStatistic(userStatisticElement);

		/* Auslaufende Kante */
		final String edgeError=userStatistic.buildEdgeOut(userStatisticElement);
		if (edgeError!=null) return edgeError;

		/* Statistikgr��en */
		final List<String> keys=userStatisticElement.getKeys();
		final List<Boolean> isTime=userStatisticElement.getIsTime();
		final List<String> expressions=userStatisticElement.getExpressions();
		final int min=Math.min(keys.size(),expressions.size());
		userStatistic.keys=new String[min];
		userStatistic.isTime=new boolean[min];
		userStatistic.expressions=new String[min];
		for (int i=0;i<min;i++) {
			final String s=keys.get(i).trim();
			if (s.isEmpty()) return String.format(Language.tr("Simulation.Creator.StatisticKeyMissing"),i+1,element.getId());
			userStatistic.keys[i]=s;
			userStatistic.isTime[i]=isTime.get(i);
			final String t=expressions.get(i).trim();
			if (t.isEmpty()) return String.format(Language.tr("Simulation.Creator.StatisticExpressionMissing"),i+1,element.getId());
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
			final int error=calc.parse(t);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.StatisticExpressionInvalid"),i+1,element.getId(),error+1);
			userStatistic.expressions[i]=t;
		}

		return userStatistic;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementUserStatistic)) return null;
		final ModelElementUserStatistic userStatisticElement=(ModelElementUserStatistic)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(userStatisticElement);
		if (edgeError!=null) return edgeError;

		/* Statistikgr��en */
		final List<String> keys=userStatisticElement.getKeys();
		final List<String> expressions=userStatisticElement.getExpressions();
		final int min=Math.min(keys.size(),expressions.size());
		for (int i=0;i<min;i++) {
			final String s=keys.get(i).trim();
			if (s.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.StatisticKeyMissing"),i+1,element.getId()));
			final String t=expressions.get(i).trim();
			if (t.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.StatisticExpressionMissing"),i+1,element.getId()));
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementUserStatisticData getData(final SimulationData simData) {
		RunElementUserStatisticData data;
		data=(RunElementUserStatisticData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementUserStatisticData(this,keys,isTime,expressions,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.UserStatistic"),String.format(Language.tr("Simulation.Log.UserStatistic.Info"),client.logInfo(simData),name));

		/* Statistikerfassung durchf�hren */
		if (!simData.runData.isWarmUp) getData(simData).processClient(simData,client);

		/* Kunde zur n�chsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}