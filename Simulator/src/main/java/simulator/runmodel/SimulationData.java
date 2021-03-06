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
package simulator.runmodel;
import java.io.File;
import java.util.Map;

import language.Language;
import simcore.SimData;
import simcore.eventcache.AssociativeEventCache;
import simcore.eventmanager.LongRunMultiSortedArrayListEventManager;
import simcore.logging.SimLogging;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementThroughput;
import simulator.logging.MultiTypeTextLogger;
import simulator.simparser.ExpressionCalc;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsLongRunPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsStateTimePerformanceIndicator;
import statistics.StatisticsTimeAnalogPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import statistics.StatisticsValuePerformanceIndicator;
import tools.SetupData;

/**
 * Diese Klasse enth�lt alle Daten, die zur Laufzeit der Simulation von einem Simulationsthread verwendet werden.
 * @author Alexander Herzog
 */
public class SimulationData extends SimData {
	/**
	 * Instanz des Laufzeit-Modells
	 * (read-only, da zwischen allen Threads geteilt; dynamische Daten werden in <code>RunData</code>, nicht in <code>RunModel</code> abgelegt)
	 */
	public final RunModel runModel;

	/**
	 * Instanz der dynamischen Daten
	 * (thread-lokal)
	 */
	public RunData runData;

	/**
	 * Statistik-Objekt, welches w�hrend der Simulation die Daten sammelt
	 * (thread-lokal, die Ergebnisse werden am Ende zusammengef�hrt)
	 */
	public Statistics statistics;

	/**
	 * Gibt an, durch wie viel die Anzahl an Kunden f�r einen Thread geteilt werden soll.<br>
	 * (Dieser Wert wird auch in das RunModel �bertragen.)
	 */
	public final int clientCountDiv;

	/**
	 * Aktueller Simulationstag. 0-basierend.
	 */
	private long currentDay;

	/**
	 * Konstruktor der Klasse <code>SimulationData</code>
	 * @param threadNr		Gibt die Nummer des Threads an, f�r den das <code>SimDat</code>-Objekt erstellt wird.
	 * @param threadCount	Anzahl der Rechenthreads
	 * @param runModel	Laufzeit-Modell, welches die Basis der Simulation darstellt
	 * @param useStatistics	Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird das angegebene Statistikobjekt verwendet. Sonst wird ein neues Statistikobjekt erstellt. F�r eine normale Simulation sollte hier stets <code>null</code> �bergeben werden.
	 */
	public SimulationData(final int threadNr, final int threadCount, final RunModel runModel, final Statistics useStatistics) {
		/* langsam: super(new PriorityQueueEventManager(),new HashMapEventCache(),threadNr,threadCount); */
		/* schneller: super(new LongRunMultiPriorityQueueEventManager(4),new HashMapEventCache(),threadNr,threadCount); */
		/* ganz schnell: */
		super(new LongRunMultiSortedArrayListEventManager(4),new AssociativeEventCache(128),threadNr,threadCount);

		this.runModel=runModel;
		this.runData=new RunData(runModel);
		if (useStatistics!=null) {
			statistics=useStatistics;
		} else {
			statistics=new Statistics(runModel.correlationRange,runModel.correlationMode,runModel.batchMeansSize,runModel.collectWaitingTimes,runModel.distributionRecordHours);
		}

		if (runModel.repeatCount>1) {
			int div=1;
			int repeat=runModel.repeatCount;
			while (repeat<threadCount) {repeat*=2; div*=2;}
			clientCountDiv=div;
			int baseCount=repeat/threadCount;
			int addCount=repeat%threadCount;
			if (threadNr<addCount) {
				simDays=baseCount+1;
				simDaysByOtherThreads=threadNr*(baseCount+1);
			} else {
				simDays=baseCount;
				simDaysByOtherThreads=addCount*(baseCount+1)+(threadNr-addCount)*baseCount;
			}
		} else {
			clientCountDiv=threadCount;
			simDaysByOtherThreads=threadNr;
			simDays=1; /* Wir machen Longrun. */
		}
		runModel.clientCountDiv=clientCountDiv;
		/* System.out.println("Thread-"+(threadNr+1)+": "+simDays+" "+simDaysByOtherThreads); */
	}

	/**
	 * Liefert die Anzahl an Wiederholungen, die simuliert werden sollen.
	 * @return	Anzahl an Wiederholungen
	 */
	public long getRepeatCount() {
		return runModel.repeatCount;
	}

	/**
	 * Liefert die aktuell durch diesen Thread in Bearbeitung befindliche Wiederholung des Modells
	 * (0-basierend).
	 * @return	Aktuelle Wiederholung des Modells
	 */
	public long getCurrentRepeat() {
		return (simDaysByOtherThreads+currentDay)/clientCountDiv;
	}

	private void resetAllDataPerformanceIndicators(final StatisticsMultiPerformanceIndicator indicators) {
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])indicators.getAll(StatisticsDataPerformanceIndicator.class)) indicator.reset();
	}

	private void resetAllValuePerformanceIndicators(final StatisticsMultiPerformanceIndicator indicators) {
		for (StatisticsValuePerformanceIndicator indicator: (StatisticsValuePerformanceIndicator[])indicators.getAll(StatisticsValuePerformanceIndicator.class)) indicator.reset();
	}

	private void resetAllTimePerformanceIndicators(final StatisticsMultiPerformanceIndicator indicators, final double time) {
		for (StatisticsTimePerformanceIndicator indicator: (StatisticsTimePerformanceIndicator[])indicators.getAll(StatisticsTimePerformanceIndicator.class)) indicator.setTime(time);
	}

	/**
	 * Wird beim Erreichen des Endes der Einschwingphase durch eine Kundenquelle aufgerufen.
	 */
	public void endWarmUp() {
		final double time=currentTime/1000.0;
		runData.warmUpEndTime=time;

		resetAllDataPerformanceIndicators(statistics.clientsInterarrivalTime);
		resetAllDataPerformanceIndicators(statistics.stationsInterarrivalTime);
		resetAllDataPerformanceIndicators(statistics.stationsInterarrivalTimeByClientType);
		resetAllDataPerformanceIndicators(statistics.clientsInterleavingTime);
		resetAllDataPerformanceIndicators(statistics.stationsInterleavingTime);
		resetAllDataPerformanceIndicators(statistics.stationsInterleavingTimeByClientType);
		resetAllDataPerformanceIndicators(statistics.clientsWaitingTimes);
		resetAllDataPerformanceIndicators(statistics.clientsTransferTimes);
		resetAllDataPerformanceIndicators(statistics.clientsProcessingTimes);
		resetAllDataPerformanceIndicators(statistics.clientsResidenceTimes);
		statistics.clientsAllWaitingTimes.reset();
		statistics.clientsAllTransferTimes.reset();
		statistics.clientsAllProcessingTimes.reset();
		statistics.clientsAllResidenceTimes.reset();
		resetAllDataPerformanceIndicators(statistics.stationsWaitingTimes);
		resetAllDataPerformanceIndicators(statistics.stationsTransferTimes);
		resetAllDataPerformanceIndicators(statistics.stationsProcessingTimes);
		resetAllDataPerformanceIndicators(statistics.stationsResidenceTimes);
		resetAllDataPerformanceIndicators(statistics.stationsWaitingTimesByClientType);
		resetAllDataPerformanceIndicators(statistics.stationsTransferTimesByClientType);
		resetAllDataPerformanceIndicators(statistics.stationsProcessingTimesByClientType);
		resetAllDataPerformanceIndicators(statistics.stationsResidenceTimesByClientType);
		statistics.clientsInSystem.setTime(time);
		statistics.clientsInSystemQueues.setTime(time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationByStation,time);
		resetAllTimePerformanceIndicators(statistics.clientsInSystemByClient,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationQueueByStation,time);
		resetAllTimePerformanceIndicators(statistics.clientsAtStationQueueByClient,time);
		resetAllTimePerformanceIndicators(statistics.resourceCount,time);
		resetAllTimePerformanceIndicators(statistics.resourceUtilization,time);
		resetAllTimePerformanceIndicators(statistics.resourceInDownTime,time);
		resetAllTimePerformanceIndicators(statistics.transporterUtilization,time);
		resetAllTimePerformanceIndicators(statistics.transporterInDownTime,time);
		for (StatisticsSimpleCountPerformanceIndicator indicator: (StatisticsSimpleCountPerformanceIndicator[])statistics.counter.getAll(StatisticsSimpleCountPerformanceIndicator.class)) indicator.reset();
		resetAllTimePerformanceIndicators(statistics.differentialCounter,time);
		resetAllValuePerformanceIndicators(statistics.clientsCostsWaiting);
		resetAllValuePerformanceIndicators(statistics.clientsCostsTransfer);
		resetAllValuePerformanceIndicators(statistics.clientsCostsProcess);
		resetAllValuePerformanceIndicators(statistics.stationCosts);
		resetAllValuePerformanceIndicators(statistics.resourceTimeCosts);
		resetAllValuePerformanceIndicators(statistics.resourceWorkCosts);
		resetAllValuePerformanceIndicators(statistics.resourceIdleCosts);
		for (StatisticsLongRunPerformanceIndicator indicator: (StatisticsLongRunPerformanceIndicator[])statistics.longRunStatistics.getAll(StatisticsLongRunPerformanceIndicator.class)) {
			indicator.reset();
			indicator.setTime(currentTime);
		}
		resetAllDataPerformanceIndicators(statistics.userStatistics);
		for (StatisticsStateTimePerformanceIndicator indicator: (StatisticsStateTimePerformanceIndicator[])statistics.stateStatistics.getAll(StatisticsStateTimePerformanceIndicator.class)) indicator.reset();
		for (StatisticsTimeAnalogPerformanceIndicator indicator: (StatisticsTimeAnalogPerformanceIndicator[])statistics.analogStatistics.getAll(StatisticsTimeAnalogPerformanceIndicator.class)) {
			final double value=indicator.getCurrentState();
			indicator.reset();
			indicator.set(currentTime/1000.0,value);
		}
		for (RunElement element: runModel.elementsFast) if (element instanceof RunElementThroughput) {
			((RunElementThroughput)element).getData(this).reset(currentTime);
		}

		if (statistics.clientsAllWaitingTimesCollector!=null) statistics.clientsAllWaitingTimesCollector.reset();
	}

	private Statistics lastDaysStatistics=null;

	@Override
	public void initDay(final long day, final long dayGlobal, final boolean backgroundMode) {
		currentDay=day;
		/* System.out.println(Thread.currentThread().getName()+": "+day+" "+dayGlobal); */

		if (day>0) { /* Wenn mehrere Wiederholungen simuliert werden und dies nicht der erste Tag ist, Statistik sichern und RunData neu initialisieren */
			lastDaysStatistics=statistics;
			statistics=new Statistics(runModel.correlationRange,runModel.correlationMode,runModel.batchMeansSize,runModel.collectWaitingTimes,runModel.distributionRecordHours);
			runData=new RunData(runModel);
		}

		currentTime=0;
		runData.initRun(day,this);
	}

	@Override
	public void terminateCleanUp(final long now) {
		super.terminateCleanUp(now);
		runData.doneRun(now,this);

		if (lastDaysStatistics!=null) {
			/* Wenn es schon Statistik von Vorg�ngertagen gibt, diese mit diesem Tag zusammenf�hren */
			statistics.addData(lastDaysStatistics);
			lastDaysStatistics=null;
		}
	}

	@Override
	public void finalTerminateCleanUp(long eventCount) {
		super.finalTerminateCleanUp(eventCount);
		for (Map.Entry<Integer,RunElement> entry: runModel.elements.entrySet()) entry.getValue().finalCleanUp(this);
	}

	@Override
	protected SimLogging getLogger(final File logFile) {
		return new MultiTypeTextLogger(logFile,true,SetupData.getSetup().singleLineEventLog,true,new String[]{Language.tr("Simulation.Log.Title")});
	}

	/**
	 * Bricht die Simulation sofort ab und verbucht dies als Fehler.
	 * @param message	Meldung, die in Logdatei und in die Warnungen der Statistik aufgenommen werden soll.
	 */
	public void doEmergencyShutDown(final String message) {
		statistics.simulationData.emergencyShutDown=true;
		if (!runData.stopp) {
			addWarning(Language.tr("Simulation.RunTimeError").toUpperCase()+": "+message);
			logEventExecution(Language.tr("Simulation.Log.Abort"),message);
		}
		doShutDown();
	}

	/**
	 * Erfasst w�hrend der Simulation eine Warnungsmeldung
	 * @param message	Neue Warnungsmeldung
	 */
	public void addWarning(final String message) {
		statistics.simulationData.addWarning(message);
	}

	/**
	 * Bricht die Simulation sofort ohne dies als Fehler zu verbuchen.
	 */
	public void doShutDown() {
		if (eventManager!=null) eventManager.deleteAllEvents();
		runData.stopp=true;
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgef�hrt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param station	Station, an der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorStation(final ExpressionCalc calc, final RunElement station) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Station"),station.name,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgef�hrt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param stationData	Station, an der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorStation(final ExpressionCalc calc, final RunElementData stationData) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Station"),stationData.station.name,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgef�hrt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param logStationName	Name der Station, an der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorStation(final ExpressionCalc calc, final String logStationName) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Station"),logStationName,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgef�hrt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param transporterName	Name der Transportergruppe, bei der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorTransporter(final ExpressionCalc calc, final String transporterName) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Transporter"),transporterName,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler abgebrochen werden soll, so wird dies ausgef�hrt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der ausgewertet werden sollte
	 * @param resourceName	Name der Ressource, bei der der Rechenausdruck ausgewertet werden sollte
	 */
	public void calculationErrorRessource(final ExpressionCalc calc, final String resourceName) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Resource"),resourceName,calc.getText()));
	}

	/**
	 * Wenn die Simulation bei einem Rechenfehler innerhalb eines Vergleichs abgebrochen werden soll, so wird dies ausgef�hrt. (Sonst erfolgt keine Verarbeitung.)
	 * @param calc	Rechenausdruck, der im Rahmen eines Vergleichs ausgewertet werden sollte
	 */
	public void calculationErrorEval(final ExpressionCalc calc) {
		if (!runModel.stoppOnCalcError) return;
		doEmergencyShutDown(String.format(Language.tr("Simulation.CalcError.Eval"),calc.getText()));
	}

	@Override
	public void catchException(final String text) {
		doEmergencyShutDown(text);
	}

	@Override
	public void catchOutOfMemory(final String text) {
		doEmergencyShutDown(Language.tr("Simulation.OutOfMemory")+"\n"+text);
	}
}
