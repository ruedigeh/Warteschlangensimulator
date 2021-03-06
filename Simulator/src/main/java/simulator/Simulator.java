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
package simulator;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.SeedableThreadLocalRandomGenerator;
import mathtools.distribution.tools.ThreadLocalRandomGenerator;
import simcore.SimData;
import simcore.SimThread;
import simcore.SimulatorBase;
import simcore.logging.SimLogging;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsSimpleValuePerformanceIndicator;
import tools.SetupData;
import tools.UsageStatistics;
import ui.statistics.StatisticViewerOverviewText;

/**
 * Vollst�ndiger Multi-Core-f�higer Simulator
 * @author Alexander Herzog
 */
public class Simulator extends SimulatorBase implements AnySimulator {
	/**
	 * Laufzeit-Modell (global f�r alle Threads)
	 */
	protected RunModel runModel;

	/**
	 * Editor-Modell (wird nur ben�tigt, da es am Ende mit in die Statistik aufgenommen wird)
	 */
	protected EditModel editModel;

	/**
	 * Steht hier ein Wert ungleich <code>null</code>, so wird in den Single-Core-Modus geschaltet und der Lauf wird in der angegebenen Log-Datei aufgezeichnet
	 */
	protected final SimLogging logging;

	/**
	 * Da die Statistik nur einmal aus den Daten erhoben wird, wird diese f�r wiederholte Aufrufe von <code>getStatistic()</code> hier aufgehoben
	 * @see getStatistic
	 */
	private Statistics statistics=null;

	/**
	 * Erfassung der Anzahl an simulierten Kunden in der Statistik?
	 * @see #doNotRecordSimulatedClientsToStatistics()
	 * @see UsageStatistics
	 */
	private boolean recordSimulatedClientsToStatistics=true;

	private static int getAllowMaxCore(final boolean multiCore, final int maxCoreCount) {
		if (!multiCore) return 1;
		final int a=Math.max(1,SetupData.getSetup().useMultiCoreSimulationMaxCount);
		final int b=Math.max(1,maxCoreCount);
		return Math.min(a,b);
	}

	private static boolean getNUMAAware() {
		return SetupData.getSetup().useNUMAMode;
	}

	/**
	 * Konstruktor der Klasse <code>Simulator</code>
	 * @param multiCore	Wird hier <code>true</code> �bergeben, so wird auf allen verf�gbaren CPU-Kernen gerechnet. (Ausnahme: Wird in <code>logFile</code> ein Wert ungleich <code>null</code> �bergeben, so wird stets nur ein Kern verwendet.)
	 * @param editModel	Editor-Modell
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; anonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 */
	public Simulator(final boolean multiCore, final EditModel editModel, final SimLogging logging) {
		super(getAllowMaxCore(multiCore && logging==null,Integer.MAX_VALUE),false,getNUMAAware());
		this.editModel=editModel;
		this.logging=logging;
	}

	/**
	 * Konstruktor der Klasse <code>Simulator</code>
	 * @param maxCoreCount	Gibt die maximale Anzahl an zu verwendenden Threads an. (Wird in <code>logFile</code> ein Wert ungleich <code>null</code> �bergeben, so wird stets nur ein Kern verwendet.)
	 * @param editModel	Editor-Modell
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 */
	public Simulator(final int maxCoreCount, final EditModel editModel, final SimLogging logging) {
		super(getAllowMaxCore(logging==null,maxCoreCount),false,getNUMAAware());
		this.editModel=editModel;
		this.logging=logging;
	}

	/**
	 * Konstruktor der Klasse <code>Simulator</code>
	 * @param editModel	Editor-Modell
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 */
	public Simulator(final EditModel editModel, final SimLogging logging) {
		this(SetupData.getSetup().useMultiCoreSimulation && (editModel.allowMultiCore() || editModel.repeatCount>1),editModel,logging);
	}

	private static void prepareStatic(final boolean fixedSeed) {
		if (fixedSeed) {
			DistributionRandomNumber.generator=new SeedableThreadLocalRandomGenerator();
		} else {
			DistributionRandomNumber.generator=new ThreadLocalRandomGenerator();
		}
	}

	/**
	 * Bereitet die Simulation vor
	 * @return	Liefert <code>null</code> zur�ck, wenn die Simulation erfolgreich vorbereitet werden konnte, sonst eine Fehlermeldung
	 */
	public String prepare() {
		prepareStatic(editModel.useFixedSeed);

		final Object obj=RunModel.getRunModel(editModel,false);
		if (obj instanceof String) return (String)obj;
		runModel=(RunModel)obj;

		return null;
	}

	/**
	 * Wird intern verwendet, um die Statistikdaten von den Threads einzusammeln.
	 * Diese Funktion wird von <code>getStatistic</code> aufgerufen. <code>getStatistic</code> speichert die einmal erhobenen
	 * Daten f�r sp�tere Abrufe zwischen, so dass <code>collectStatistics</code> nur einmal aufgerufen werden muss.
	 * @return	Liefert eine Statistikobjekt, welches die zusammengefassten Statistikdaten �ber alle Threads enth�lt
	 */
	protected Statistics collectStatistics() {
		final Statistics statistics=new Statistics(runModel.correlationRange,runModel.correlationMode,runModel.batchMeansSize,runModel.collectWaitingTimes,runModel.distributionRecordHours);

		/* Basisdaten zum Modell und zum Simulationslauf festhalten */

		statistics.editModel=editModel.clone();
		statistics.editModel.version=EditModel.systemVersion;
		if (statistics.editModel.author==null || statistics.editModel.author.trim().isEmpty()) statistics.editModel.author=EditModel.getDefaultAuthor();

		statistics.simulationData.runUser=EditModel.getDefaultAuthor();
		statistics.simulationData.runTime=runTime;
		statistics.simulationData.runThreads=threadCount;
		statistics.simulationData.runEvents=getEventCount();
		statistics.simulationData.runRepeatCount=editModel.repeatCount;
		statistics.simulationData.numaAwareMode=getNUMAAware();
		statistics.simulationData.threadRunTimes=getThreadRuntimes();

		/* Daten von den Threads einsammeln */
		int count1=0;
		int count2=0;
		double waiting1=0;
		double waiting2=0;
		final List<StatisticsDataPerformanceIndicator> partialWaitingTime=new ArrayList<>();
		for (int i=0;i<threads.length;i++) {
			final Statistics partialStatistics=((SimulationData)threads[i].simData).statistics;
			partialWaitingTime.add(partialStatistics.clientsAllWaitingTimes);
			statistics.addData(partialStatistics);
			final double waiting=partialStatistics.clientsAllWaitingTimes.getMean();
			if (i<threads.length/2) {count1++; waiting1+=waiting;} else {count2++; waiting2+=waiting;}
		}

		/* Warnung, wenn die mittlere Wartezeit der ersten H�lfte von der zweiten H�lfte abweicht */
		if (count1>0 && count2>0 && waiting1>0 && waiting2>0) {
			if (!editModel.useTerminationCondition) {
				final double delta=Math.abs((waiting1/count1)-(waiting2/count2))/((waiting1+waiting2)/(count1+count2));
				/* System.out.println("Delta="+NumberTools.formatPercent(delta,2)); */
				if (delta>0.1) statistics.simulationData.addWarning(String.format(Language.tr("Statistics.Warnings.SimulationRunNotLongEnough"),NumberTools.formatPercent(delta)));
			}
		}

		/* Aufzeichnung der Thread-basierenden Konfidenzniveaus f�r die Wartezeiten */
		if (threads.length>1) {
			final double[] halfWidth=StatisticsDataPerformanceIndicator.getConfidenceHalfWideByMultiStatistics(partialWaitingTime.toArray(new StatisticsDataPerformanceIndicator[0]),statistics.clientsAllWaitingTimes,StatisticViewerOverviewText.CONFIDENCE_LEVELS);
			if (halfWidth!=null) for (int i=0;i<halfWidth.length;i++) {
				((StatisticsSimpleValuePerformanceIndicator)statistics.threadBasedConfidence.get(NumberTools.formatPercent(1-StatisticViewerOverviewText.CONFIDENCE_LEVELS[i]))).set(halfWidth[i]);
			}
		}

		/* Aufbereitete Daten berechnen */
		statistics.calc();

		/* Nutzungsstatistik erfassen */
		if (recordSimulatedClientsToStatistics) {
			long sum=0;
			for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();
			UsageStatistics.getInstance().addSimulationClients(sum);
		}

		return statistics;
	}

	/**
	 * Liefert nach Abschluss der Simulation die Statistikergebnisse zur�ck.
	 * @return	Statistik-Objekt, welches alle Daten des Simulationslaufs enth�lt
	 */
	@Override
	public final Statistics getStatistic() {
		finalizeRun();
		if (statistics==null) {
			statistics=collectStatistics();
			for (int i=0;i<threads.length;i++) threads[i]=null;
			runModel=null;
		}
		return statistics;
	}

	@Override
	protected SimData getSimDataForThread(final int threadNr, final int threadCount) {
		final SimData data;
		if (numaAware) {
			final Object obj=RunModel.getRunModel(editModel,false);
			data=new SimulationData(threadNr,threadCount,(RunModel)obj,null);
		} else {
			data=new SimulationData(threadNr,threadCount,runModel,null);
		}
		if (logging!=null) data.activateLogging(logging);
		return data;
	}

	/**
	 * Startet die Simulationssthreads mit normaler Priorit�t.
	 * @see SimulatorBase#start(boolean)
	 */
	public final void start() {
		start(false);
	}

	/**
	 * Startet die Simulationssthreads mit normaler Priorit�t.
	 * @param startPaused	Startet die Simulation im Pause/Einzelschritt-Modus
	 * @see SimulatorBase#start(boolean)
	 */
	@Override
	public final void start(final boolean startPaused) {
		if (runModel==null) return;
		super.start(!SetupData.getSetup().highPriority,startPaused);
	}

	/**
	 * Liefert die Gesamtanzahl an Wiederholungen in der Simulation.
	 * @return	Anzahl an Wiederholungen (�ber alle Threads) der Simulation.
	 */
	@Override
	public final long getSimDaysCount() {
		return runModel.clientCount*runModel.repeatCount; /* wird �berhaupt nicht verwendet, da wir immer im LongRun-Modus sind */
	}

	/**
	 * Liefert die Gesamtanzahl an zu simulierenden Kundenank�nften
	 * @return	Gesamtanzahl an zu simulierenden Kundenank�nften
	 */
	@Override
	public final long getCountClients() {
		return (runModel.realArrivingClientCount+runModel.clientCountDiv*FastMath.round(runModel.warmUpTime*runModel.realArrivingClientCount))*runModel.repeatCount;
	}

	/**
	 * Liefert die Anzahl an bislang simulierten Kundenank�nften
	 * @return	Anzahl an bislang simulierten Kundenank�nften
	 */
	@Override
	public final long getCurrentClients() {
		long sum=0;
		for (SimThread thread: threads) {
			if (thread==null) continue;
			final SimulationData data=(SimulationData)(thread.simData);
			if (data==null) continue;
			if (runModel.clientCount>0) sum+=(thread.currentDay-1)*FastMath.round(runModel.clientCount*(1+runModel.warmUpTime)/runModel.clientCountDiv);
			if (!data.runData.isWarmUp && runModel.clientCount>0) sum+=FastMath.round(runModel.warmUpTime*runModel.clientCount);
			sum+=data.runData.clientsArrived;
		}
		return sum;
	}

	/**
	 * Liefert die aktuelle Anzahl an Kunden im System
	 * @return	Aktuelle Anzahl an Kunden im System
	 */
	@Override
	public int getCurrentWIP() {
		int sum=0;
		int threadCount=0;
		for (SimThread thread: threads) {
			if (thread==null) continue;
			final SimulationData data=(SimulationData)(thread.simData);
			if (data==null) continue;
			sum+=data.statistics.clientsInSystem.getCurrentState();
			threadCount++;
		}
		return (threadCount==0)?0:sum/threadCount;
	}

	/**
	 * Liefert das im Konstruktor angegebene Editor-Modell zur�ck
	 * @return	Editor-Modell
	 */
	public EditModel getEditModel() {
		return editModel;
	}

	/**
	 * Liefert das aktuell verwendete Laufzeit-Modell zur�ck
	 * @return	Laufzeit-Modell
	 */
	public RunModel getRunModel() {
		return runModel;
	}

	/**
	 * Erstellt einen Simulationdatenobjekt aus einem Statistikobjekt.<br>
	 * So k�nnen nach Simulationsende (auf Basis der Statistik) dennoch die Rechnenfunktionen zur Abfrage bestimmter Werte (die eigentlich auf dem Simulationsdatenobjekt operieren) verwendet werden.
	 * @param statistics	Statistikobjekt, aus dem wieder ein Simulationsdatenobjekt gewonnen werden soll
	 * @return	Simulationsdatenobjekt das sich so verh�lt als w�re es die Basis f�r das angegebene Statistikobjekt
	 */
	public static SimulationData getSimulationDataFromStatistics(final Statistics statistics) {
		if (statistics==null) return null;
		final Simulator simulator=new Simulator(statistics.editModel,null);
		if (simulator.prepare()!=null) return null;
		final SimulationData simData=new SimulationData(0,simulator.threads.length,simulator.runModel,statistics);

		simData.runData.initRun(0,simData);
		for (RunElement station: simData.runModel.elementsFast) if (station!=null) simData.runData.explicitInitStatistics(simData,station);

		return simData;
	}

	/**
	 * Deaktiviert die Z�hlung der simulierten Kunden in der Statistik.<br>
	 * Diese Funktion muss vor {@link #collectStatistics()} aufgerufen werden.
	 */
	public void doNotRecordSimulatedClientsToStatistics() {
		recordSimulatedClientsToStatistics=false;
	}

	private long[] nanos;

	/**
	 * Summe der Nanosekunden, die die Rechenthreads aktiv waren.
	 * @return	Mit Simulation verbrachte Nanosekunden �ber alle Threads zusammen
	 */
	public long getNanos() {
		long sum=0;
		final ThreadMXBean bean=ManagementFactory.getThreadMXBean();
		if (threads!=null) {
			if (nanos==null) nanos=new long[threadCount];
			for (int i=0;i<threadCount;i++) {
				final Thread thread=threads[i];
				if (thread!=null) nanos[i]=FastMath.max(nanos[i],bean.getThreadCpuTime(thread.getId()));
				sum+=nanos[i];
			}
		}
		return sum;
	}
}
