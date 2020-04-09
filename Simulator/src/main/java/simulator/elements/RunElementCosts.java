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

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementCosts;
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu <code>ModelElementCosts</code>
 * @author Alexander Herzog
 * @see ModelElementCosts
 */
public class RunElementCosts extends RunElementPassThrough {
	private String stationCosts;
	private String clientWaitingCosts;
	private String clientTransferCosts;
	private String clientProcessCosts;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zugeh�riges Editor-Element
	 */
	public RunElementCosts(final ModelElementCosts element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Costs.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementCosts)) return null;
		final ModelElementCosts costsElement=(ModelElementCosts)element;
		final RunElementCosts costs=new RunElementCosts(costsElement);

		/* Auslaufende Kante */
		final String edgeError=costs.buildEdgeOut(costsElement);
		if (edgeError!=null) return edgeError;

		/* Kosten */
		String text;

		text=costsElement.getStationCosts();
		if (text==null || text.trim().isEmpty()) {
			costs.stationCosts=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorStation"),text,element.getId(),error+1);
			costs.stationCosts=text;
		}

		text=costsElement.getClientWaitingCosts();
		if (text==null || text.trim().isEmpty()) {
			costs.clientWaitingCosts=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorWaiting"),text,element.getId(),error+1);
			costs.clientWaitingCosts=text;
		}

		text=costsElement.getClientTransferCosts();
		if (text==null || text.trim().isEmpty()) {
			costs.clientTransferCosts=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorTransfer"),text,element.getId(),error+1);
			costs.clientTransferCosts=text;
		}

		text=costsElement.getClientProcessCosts();
		if (text==null || text.trim().isEmpty()) {
			costs.clientProcessCosts=null;
		} else {
			final int error=ExpressionCalc.check(text,runModel.variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CostsErrorProcess"),text,element.getId(),error+1);
			costs.clientProcessCosts=text;
		}

		return costs;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementCosts)) return null;
		final ModelElementCosts costsElement=(ModelElementCosts)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(costsElement);
		if (edgeError!=null) return edgeError;

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementCostsData getData(final SimulationData simData) {
		RunElementCostsData data;
		data=(RunElementCostsData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementCostsData(this,stationCosts,clientWaitingCosts,clientTransferCosts,clientProcessCosts,simData.runModel.variableNames);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementCostsData data=getData(simData);

		simData.runData.setClientVariableValues(client);

		final double stationCosts;
		if (data.stationCosts==null) {
			stationCosts=0.0;
		} else {
			if (simData.runModel.stoppOnCalcError) {
				final Double D=data.stationCosts.calc(simData.runData.variableValues,simData,client);
				if (D==null) simData.calculationErrorStation(data.stationCosts,this);
				stationCosts=(D==null)?0.0:D.doubleValue();
			} else {
				stationCosts=data.stationCosts.calcOrDefault(simData.runData.variableValues,simData,client,0);
			}
		}
		final double clientWaitingCosts;
		if (data.clientWaitingCosts==null) {
			clientWaitingCosts=0.0;
		} else {
			if (simData.runModel.stoppOnCalcError) {
				final Double D=data.clientWaitingCosts.calc(simData.runData.variableValues,simData,client);
				if (D==null) simData.calculationErrorStation(data.clientWaitingCosts,this);
				clientWaitingCosts=(D==null)?0.0:D.doubleValue();
			} else {
				clientWaitingCosts=data.clientWaitingCosts.calcOrDefault(simData.runData.variableValues,simData,client,0);
			}
		}
		final double clientTransferCosts;
		if (data.clientTransferCosts==null) {
			clientTransferCosts=0.0;
		} else {
			if (simData.runModel.stoppOnCalcError) {
				final Double D=data.clientTransferCosts.calc(simData.runData.variableValues,simData,client);
				if (D==null) simData.calculationErrorStation(data.clientTransferCosts,this);
				clientTransferCosts=(D==null)?0.0:D.doubleValue();
			} else {
				clientTransferCosts=data.clientTransferCosts.calcOrDefault(simData.runData.variableValues,simData,client,0);
			}
		}
		final double clientProcessCosts;
		if (data.clientProcessCosts==null) {
			clientProcessCosts=0.0;
		} else {
			if (simData.runModel.stoppOnCalcError) {
				final Double D=data.clientProcessCosts.calc(simData.runData.variableValues,simData,client);
				if (D==null) simData.calculationErrorStation(data.clientProcessCosts,this);
				clientProcessCosts=(D==null)?0.0:D.doubleValue();
			} else {
				clientProcessCosts=data.clientProcessCosts.calcOrDefault(simData.runData.variableValues,simData,client,0);
			}
		}

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.Costs"),String.format(Language.tr("Simulation.Log.Costs.Info"),client.logInfo(simData),name,NumberTools.formatNumber(stationCosts),NumberTools.formatNumber(clientWaitingCosts),NumberTools.formatNumber(clientTransferCosts),NumberTools.formatNumber(clientProcessCosts)));

		if (!simData.runData.isWarmUp) {
			/* Stationskosten verarbeiten */
			simData.runData.logStationCosts(simData,this,stationCosts);

			/* Kundenkosten verarbeiten */
			client.waitingAdditionalCosts+=clientWaitingCosts;
			client.transferAdditionalCosts+=clientTransferCosts;
			client.processAdditionalCosts+=clientProcessCosts;
		}

		/* Kunde zur n�chsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
	}
}