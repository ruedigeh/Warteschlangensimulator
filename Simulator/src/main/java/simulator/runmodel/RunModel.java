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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreator;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementSourceTable;
import simulator.elements.RunSource;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionEval;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import simulator.statistics.Statistics;
import simulator.statistics.Statistics.CorrelationMode;
import tools.SetupData;
import ui.modeleditor.ModelSequence;
import ui.modeleditor.ModelSequenceStep;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ElementWithNewVariableNames;
import ui.modeleditor.elements.InteractiveElement;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Laufzeit-Modell
 * Dieses Modell wird read-only in der Simulation verwendet. Im Gegensatz zu dem Editor-Modell ist es auf konsistenz gepr�ft,
 * Bediensationen sind per Referenzen verkn�pft, nicht mehr nur durch Freitextfelder.
 * @author Alexander Herzog
 * @see EditModel
 * @see RunModel#getRunModel(EditModel, boolean)
 */
public class RunModel {
	/**
	 * Anzahl der zu simulierenden Kundenank�nfte (kann <code>-1</code> sein, wenn das Simulationsende nicht �ber die Z�hlung der Kundenank�nfte erfolgen soll)
	 */
	public long clientCount;

	/**
	 * Gibt an, wie viele Kunden insgesamt eintreffen werden.<br>
	 * Bei Modellen, die nur Tabellenquellen verwenden, ist dies das Minimum aus der Summe aller Tabellenzeilen und {@link RunModel#clientCount}. Bei allen anderen Modellen ist dies stets {@link RunModel#clientCount}.
	 */
	public long realArrivingClientCount;

	/**
	 * L�nge der Einschwingphase (als Anteil der Kundenank�nfte), bevor die Statistikz�hlung beginnt.<br>
	 * Die Einschwingphase wird nicht von der Kundenanzahl abgezogen, sondern besateht aus zus�tzlichen Ank�nften.
	 */
	public double warmUpTime;

	/**
	 * Gibt an, wie oft der Simulationslauf als Ganzes wiederholt werden soll.
	 */
	public int repeatCount;

	/**
	 * Gibt an, durch wie viel die Anzahl an Kunden f�r einen Thread geteilt werden soll.<br>
	 * (Dieser Wert steht auch in den SimulationData zur Verf�gung.)
	 */
	public int clientCountDiv;

	/**
	 * Abbruchbedingung (kann <code>null</code> sein, wenn keine Abbruchbedingung definiert ist)
	 */
	public ExpressionEval terminationCondition;

	/**
	 * Abbruchzeitpunkt (kann -1 sein, wenn keine Abbruchzeit definiert ist)
	 */
	public long terminationTime;

	/**
	 * Liste mit Namen aller vorhandenen Kundentypen
	 */
	public String[] clientTypes;

	/**
	 * Kosten f�r die Zeitanteile pro Sekunde Kundentyp
	 */
	public double[][] clientCosts;

	/**
	 * Liste mit den Icons aller vorhandenen Kundentypen
	 */
	public String[] clientTypeIcons;

	/**
	 * Liste mit allen Variablennamen (in der Reihenfolge wie sie als Parameter dem Calc-System �bergeben werden)
	 */
	public String[] variableNames;

	/**
	 * Initiale Werte f�r die Variablen (k�nnen teilweise <code>null</code> sein, wenn nicht explizit gesetzt werden soll)
	 */
	public ExpressionCalc[] variableInitialValues;

	/**
	 * Zus�tzliche Variablennamen, die immer belegt sein sollen.
	 */
	public static final String[] additionalVariables=new String[]{"w","t","p"};

	/**
	 * Liste der Modell-Elemente des Laufzeitmodells
	 * @see RunModel#elementsFast
	 */
	public Map<Integer,RunElement> elements;

	/**
	 * Direktzugriff-Liste der Modell-Elemente des Laufzeitmodells
	 * @see RunModel#elements
	 */
	public RunElement[] elementsFast;

	/**
	 * Zuordnung von Modell-Element-Namen zu IDs
	 */
	public TreeMap<String,Integer> namesToIDs;

	/**
	 * Globales Ressourcen-Objekt, welches nur bei der Initialisierung der thread-lokalen Laufzeitdaten als
	 * Kopier-Basis verwendet wird.
	 */
	public RunDataResources resourcesTemplate;

	/**
	 * Globales Transporter-Objekt, welches nur bei der Initialisierung der thread-lokalen Laufzeitdaten als
	 * Kopier-Basis verwendet wird.
	 */
	public RunDataTransporters transportersTemplate;

	/**
	 * Festen Seed f�r den Zufallszahlengenerator verwenden?
	 * @see #fixedSeed
	 */
	public boolean useFixedSeed;

	/**
	 * Seed f�r den Zufallszahlengenerator.<br>
	 * Ist nur aktiv, wenn <code>useFixedSeed=true</code> ist.
	 * @see #useFixedSeed
	 */
	public long fixedSeed;

	/**
	 * Maximaler Autokorrelationswert der bei der Erfassung der Daten vorgesehen werden soll.
	 * @see RunModel#correlationMode
	 */
	public int correlationRange;

	/**
	 * Art der Erfassung der Autokorrelation
	 * @see CorrelationMode#CORRELATION_MODE_OFF
	 * @see CorrelationMode#CORRELATION_MODE_FAST
	 * @see CorrelationMode#CORRELATION_MODE_FULL
	 */
	public Statistics.CorrelationMode correlationMode;

	/**
	 * Gibt an ob (bei &gt;1) und wenn ja von welcher Gr��e die Batches
	 * sein sollen, auf deren Basis Batch-Means berechnet werden sollen.
	 */
	public int batchMeansSize;

	/**
	 * Sollen die individuellen Wartezeiten gespeichert werden?
	 * @see Statistics#clientsAllWaitingTimesCollector
	 */
	public boolean collectWaitingTimes;

	/**
	 * Namen der Fertigungspl�ne
	 */
	public String[] sequenceNames;

	/**
	 * Liste der Namen der Zielstationen in den einzelnen Schritten der einzelnen Fertigungspl�ne
	 */
	public String[][] sequenceStepStationNames;

	/**
	 * Liste der IDs der Zielstationen in den einzelnen Schritten der einzelnen Fertigungspl�ne
	 */
	public int[][] sequenceStepStationIDs;

	/**
	 * 0-basierende Nummern der n�chsten Schritte in den einzelnen Schritten der einzelnen Fertigungspl�ne.<br>
	 * -1 bedeutet dabei, dass kein weiterer Schritt folgt.
	 */
	public int[][] sequenceStepNext;

	/**
	 * ClientData-Nummern f�r die Zuweisungen pro Schritt
	 */
	public int[][][] sequenceStepAssignmentNr;

	/**
	 * Ausdr�cke f�r die Zuweisungen pro Schritt
	 */
	public String[][][] sequenceStepAssignmentExpression;

	/**
	 * Welcher Sekundenwert soll in der Verteilungsstatistik maximal erfasst werden (Angabe in Stunden)?
	 */
	public int distributionRecordHours;

	/**
	 * Simulation abbrechen, wenn ein Rechenausdruck nicht ausgerechnet werden kann.
	 */
	public boolean stoppOnCalcError;

	/**
	 * Gibt an, ob es sich um eine Animation (<code>true</code>) oder um eine reine Simulation ohne Grafikausgabe (<code>false</code>) handelt
	 */
	public boolean isAnimation;

	/**
	 * Zeitabstand in dem f�r Bedingung- und �hnliche Stationen zus�tzliche zeitabh�ngige Checks durchgef�hrt werden sollen.
	 * Werte &le;0 bedeuten, dass keine Checks stattfinden. Sonst ist der Wert die Millisekundenanzahl zwischen zwei Checks.
	 */
	public int timedChecksDelta=-1;

	/**
	 * Z�hlung wie h�ufig welche Stations�berg�nge stattgefunden haben
	 */
	public boolean recordStationTransitions;

	/**
	 * Erfassung aller Kundenpfade
	 */
	public boolean recordClientPaths;

	/**
	 * Simulation bei einem Scripting-Fehler abbrechen
	 */
	public boolean canelSimulationOnScriptError;

	/**
	 * Ein <code>RunModel</code> kann nicht direkt erzeugt werden, sondern es kann nur ein <code>EditModel</code>
	 * mittels der Funktion <code>getRunModel</code> in ein <code>RunModel</code> umgeformt werden. Dabei wird das
	 * Modell auf Konsistenz gepr�ft und alle notwendigen Verkn�pfungen werden hergestellt.
	 * @see EditModel
	 * @see RunModel.getRunModel
	 */
	private RunModel() {
		elements=new HashMap<>();
		namesToIDs=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * Liefert den Index den angegebenen Clienttypennamens in dem <code>clientTypes</code>-Array (ohne Ber�cksichtigung von Gro�- und Kleinschreibung)
	 * @param clientTypeName	Name, der dem Array gefunden werden soll
	 * @return	Index des Namens in dem <code>clientTypes</code>-Array oder -1, wenn der Name nicht in dem Array vorkommt
	 * @see #clientTypes
	 */
	public int getClientTypeNr(final String clientTypeName) {
		if (clientTypeName==null) return -1;
		for (int i=0;i<clientTypes.length;i++) if (clientTypeName.equalsIgnoreCase(clientTypes[i])) return i;
		return -1;
	}

	private static String initVariables(final EditModel editModel, final RunModel runModel) {
		/* Variablenliste aufstellen */
		final List<String> variables=new ArrayList<>();
		for (String variable: editModel.globalVariablesNames) {
			boolean inList=false;
			for (String s: variables) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
			for (String s: RunModel.additionalVariables) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
			if (!inList) variables.add(variable);
		}
		for (ModelElement element : editModel.surface.getElements()) if (element instanceof ElementWithNewVariableNames) {
			for (String variable: ((ElementWithNewVariableNames)element).getVariables()) {
				if (CalcSymbolClientUserData.testClientData(variable)>=0) continue;
				if (CalcSymbolClientUserData.testClientDataString(variable)!=null) continue;
				boolean varNameOk=ExpressionCalc.checkVariableName(variable);
				if (!varNameOk) return String.format(Language.tr("Simulation.Creator.InvalidVariableName"),element.getId(),variable);
				boolean inList=false;
				for (String s: variables) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
				for (String s: RunModel.additionalVariables) if (s.equalsIgnoreCase(variable)) {inList=true; break;}
				if (!inList) variables.add(variable);
			}
		}
		variables.addAll(Arrays.asList(RunModel.additionalVariables));
		runModel.variableNames=variables.toArray(new String[0]);

		/* Initiale Werte f�r Variablen bestimmen */
		runModel.variableInitialValues=new ExpressionCalc[runModel.variableNames.length];
		for (int i=0;i<FastMath.min(editModel.globalVariablesNames.size(),editModel.globalVariablesExpressions.size());i++) {
			final String varName=editModel.globalVariablesNames.get(i);
			final String varExpression=editModel.globalVariablesExpressions.get(i);
			final ExpressionCalc calc=new ExpressionCalc(runModel.variableNames);
			final int error=calc.parse(varExpression);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidInitialVariableExpression"),varName,varExpression,error+1);
			int index=-1;
			for (int j=0;j<runModel.variableNames.length;j++) if (runModel.variableNames[j].equals(varName)) {index=j; break;}
			if (index>=0) runModel.variableInitialValues[index]=calc;
		}

		return null;
	}

	private static long getArrivingRealClientCount(final RunModel runModel) {
		long sum=0;
		for (RunElement element: runModel.elementsFast) {
			if (element instanceof RunSource) {
				if (!(element instanceof RunElementSourceTable)) return runModel.clientCount;
				final long value=((RunElementSourceTable)element).getArrivalCount();
				if (value<0) return runModel.clientCount; /* Quelle hat die eigenen Daten noch nicht geladen. */
				sum+=value;
			}
		}
		return Math.min(sum,runModel.clientCount);
	}

	private static String initGeneralData(final EditModel editModel, final RunModel runModel) {
		if (!editModel.useClientCount && !editModel.useFinishTime && !(editModel.useTerminationCondition && !editModel.terminationCondition.trim().isEmpty())) return Language.tr("Simulation.Creator.NoEndCriteria");

		/* Anzahl der zu simulierenden Kundenank�nfte */
		if (editModel.useClientCount) {
			if (editModel.clientCount<=0) return String.format(Language.tr("Simulation.Creator.InvalidNumberOfClients"),editModel.clientCount);
			runModel.clientCount=editModel.clientCount;
		} else {
			runModel.clientCount=-1;
		}

		/* Einschwingphase */
		if (editModel.useClientCount) {
			if (editModel.warmUpTime<0) return String.format(Language.tr("Simulation.Creator.InvalidWarmUpPeriod"),NumberTools.formatNumber(editModel.warmUpTime));
			runModel.warmUpTime=editModel.warmUpTime;
		} else {
			runModel.warmUpTime=0;
		}

		/* Wiederholungen der Simulation */
		if (editModel.repeatCount<1) return String.format(Language.tr("Simulation.Creator.InvalidRepeatCount"),editModel.repeatCount);
		if (editModel.repeatCount>1) {
			final String noRepeat=editModel.getNoRepeatReason();
			if (noRepeat!=null) return String.format(Language.tr("Simulation.Creator.NoRepeatCountAllowed"),editModel.repeatCount,noRepeat);
		}
		runModel.repeatCount=editModel.repeatCount;

		/* Simulation bei Rechenfehlern abbrechen */
		runModel.stoppOnCalcError=editModel.stoppOnCalcError;

		/* Liste der Kundentypen */
		runModel.clientTypes=editModel.surface.getClientTypes().toArray(new String[0]);

		/* Liste mit den Kosten pro Kundentyp */
		runModel.clientCosts=new double[runModel.clientTypes.length][];
		for (int i=0;i<runModel.clientCosts.length;i++) runModel.clientCosts[i]=editModel.clientData.getCosts(runModel.clientTypes[i]);

		/* List der Kundentypicons */
		runModel.clientTypeIcons=new String[runModel.clientTypes.length];
		for (int i=0;i<runModel.clientTypeIcons.length;i++) runModel.clientTypeIcons[i]=editModel.clientData.getIcon(runModel.clientTypes[i]);

		/* Ressourcen */
		runModel.resourcesTemplate=new RunDataResources();
		String error=runModel.resourcesTemplate.loadFromEditResources(editModel.resources,editModel.schedules,runModel.variableNames);
		if (error!=null) return error;

		/* Transporter */
		runModel.transportersTemplate=new RunDataTransporters();
		error=runModel.transportersTemplate.loadFromEditTransporters(editModel.transporters,editModel.surface,runModel.variableNames);
		if (error!=null) return error;

		/* Sequenzen */
		final int seqCount=editModel.sequences.getSequences().size();
		runModel.sequenceNames=new String[seqCount];
		runModel.sequenceStepStationNames=new String[seqCount][];
		runModel.sequenceStepStationIDs=new int[seqCount][];
		runModel.sequenceStepNext=new int[seqCount][];
		runModel.sequenceStepAssignmentNr=new int[seqCount][][];
		runModel.sequenceStepAssignmentExpression=new String[seqCount][][];
		for (int i=0;i<seqCount;i++) {
			final ModelSequence sequence=editModel.sequences.getSequences().get(i);
			final int stepCount=sequence.getSteps().size();
			if (sequence.getName()==null || sequence.getName().isEmpty()) return String.format(Language.tr("Simulation.Creator.SequenceNoName"),i+1);
			if (stepCount==0) return String.format(Language.tr("Simulation.Creator.SequenceNoSteps"),sequence.getName());
			runModel.sequenceNames[i]=sequence.getName();
			runModel.sequenceStepStationNames[i]=new String[stepCount];
			runModel.sequenceStepStationIDs[i]=new int[stepCount];
			runModel.sequenceStepNext[i]=new int[stepCount];
			runModel.sequenceStepAssignmentNr[i]=new int[stepCount][];
			runModel.sequenceStepAssignmentExpression[i]=new String[stepCount][];
			for (int j=0;j<stepCount;j++) {
				final ModelSequenceStep step=sequence.getSteps().get(j);
				if (step.getTarget()==null || step.getTarget().isEmpty()) return String.format(Language.tr("Simulation.Creator.SequenceStepNoTarget"),sequence.getName(),j+1);
				runModel.sequenceStepStationNames[i][j]=step.getTarget(); /* Zuordnung der IDs erfolgt erst in Schritt 2 in initGeneralData2 */
				int next=step.getNext();
				if (next<0) {
					next=(j==stepCount-1)?-1:(j+1);
				} else {
					if (next>=stepCount) return String.format(Language.tr("Simulation.Creator.SequenceInvalidNextStep"),sequence.getName(),j+1,next+1);
				}
				runModel.sequenceStepNext[i][j]=next;
				final int assignCount=step.getAssignments().size();
				runModel.sequenceStepAssignmentNr[i][j]=new int[assignCount];
				runModel.sequenceStepAssignmentExpression[i][j]=new String[assignCount];
				int k=0;
				for (Map.Entry<Integer,String> entry: step.getAssignments().entrySet()) {
					runModel.sequenceStepAssignmentNr[i][j][k]=entry.getKey();
					runModel.sequenceStepAssignmentExpression[i][j][k]=entry.getValue();
					final int exprError=ExpressionCalc.check(entry.getValue(),editModel.surface.getMainSurfaceVariableNames(editModel.getModelVariableNames(),false));
					if (exprError>=0) return String.format(Language.tr("Simulation.Creator.SequenceInvalidExpression"),sequence.getName(),j+1,runModel.sequenceStepAssignmentNr[i][j][k],entry.getValue(),exprError+1);
					k++;
				}
			}
		}

		/* Zeitabstand in dem f�r Bedingung- und �hnliche Stationen zus�tzliche zeitabh�ngige Checks durchgef�hrt werden sollen. */
		runModel.timedChecksDelta=editModel.timedChecksDelta;

		/* Aufzeichnung der Kundenbewegungen */
		runModel.recordStationTransitions=editModel.recordStationTransitions;
		runModel.recordClientPaths=editModel.recordClientPaths;

		/* Scripting */
		runModel.canelSimulationOnScriptError=SetupData.getSetup().canelSimulationOnScriptError;

		return null;
	}

	private static String initGeneralData2(final EditModel editModel, final RunModel runModel) {
		/* Evtl. treffen weniger Kunden ein, als eingestellt ist (n�mlich wenn nur Tabellenquellen verwendet werden). */
		runModel.realArrivingClientCount=getArrivingRealClientCount(runModel);

		/* Abbruchbedingung */
		if (editModel.useTerminationCondition && !editModel.terminationCondition.trim().isEmpty()) {
			runModel.terminationCondition=new ExpressionEval(runModel.variableNames);
			final int error=runModel.terminationCondition.parse(editModel.terminationCondition);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.InvalidTerminationCondition"),editModel.terminationCondition,error+1);
		} else {
			runModel.terminationCondition=null;
		}

		/* Abbruchzeit */
		if (editModel.useFinishTime) {
			runModel.terminationTime=FastMath.max(0,editModel.finishTime);
		} else {
			runModel.terminationTime=-1;
		}

		/* Seed f�r Zufallszahlengenerator */
		runModel.useFixedSeed=editModel.useFixedSeed;
		runModel.fixedSeed=editModel.fixedSeed;

		/* Bestimmung der Autokorrelation */
		runModel.correlationRange=editModel.correlationRange;
		runModel.correlationMode=editModel.correlationMode;

		/* Batch-Means */
		runModel.batchMeansSize=editModel.batchMeansSize;

		/* Erfassung individueller Wartezeiten */
		runModel.collectWaitingTimes=editModel.collectWaitingTimes;

		/* Sequenzen - Stationen verdrahten */
		for (int i=0;i<runModel.sequenceStepStationNames.length;i++) {
			for (int j=0;j<runModel.sequenceStepStationNames[i].length;j++) {
				final Integer I=runModel.namesToIDs.get(runModel.sequenceStepStationNames[i][j]);
				if (I==null) return String.format(Language.tr("Simulation.Creator.SequenceUnknownStation"),runModel.sequenceNames[i],j+1,runModel.sequenceStepStationNames[i][j]);
				runModel.sequenceStepStationIDs[i][j]=I.intValue();
			}
		}

		/* Erfassungsbereich f�r Verteilungen */
		runModel.distributionRecordHours=editModel.distributionRecordHours;

		return null;
	}

	private static String initElementsData(final EditModel editModel, final RunModel runModel, final boolean testOnly) {
		/* Liste der RunElemente aufbauen */
		final RunModelCreator creator=new RunModelCreator(editModel,runModel,testOnly);
		boolean hasSource=false;
		boolean hasDispose=false;
		for (ModelElement element : editModel.surface.getElements()) {
			if (element instanceof ModelElementBox) {
				if (!((ModelElementBox)element).inputConnected()) continue; /* Keine Einlaufende Ecke in Element -> kann ignoriert werden */
				if (element instanceof ModelElementSource) hasSource=true;
				if (element instanceof ModelElementAnimationConnect) runModel.isAnimation=true;
				if (element instanceof ModelElementSourceMulti && !((ModelElementSourceMulti)element).getRecords().isEmpty()) hasSource=true;
				if (element instanceof ModelElementSourceTable) hasSource=true;
				if (element instanceof ModelElementSourceDB) hasSource=true;
				if (element instanceof ModelElementSourceDDE) hasSource=true;
				if (element instanceof ModelElementDispose) hasDispose=true;
				String error=creator.addElement((ModelElementBox)element);
				if (error!=null) return error;
				if (element instanceof ModelElementSub) for (ModelElement subElement : ((ModelElementSub)element).getSubSurface().getElements()) if (subElement instanceof ModelElementBox) {
					error=creator.addElement((ModelElementBox)subElement,(ModelElementSub)element);
					if (error!=null) return error;
				}
			} else {
				if ((element instanceof InteractiveElement) && (element instanceof ModelElementPosition)) {
					String error=creator.addElement((ModelElementPosition)element);
					if (error!=null) return error;
				}
			}
		}

		if (!hasSource) return Language.tr("Simulation.Creator.NoSource");
		if (!hasDispose) return Language.tr("Simulation.Creator.NoDispose");

		/* Verkn�pfungen umstellen von IDs auf Referenzen */
		int maxID=0;
		for (Map.Entry<Integer,RunElement> entry : runModel.elements.entrySet()) {
			final RunElement element=entry.getValue();
			if (element.id<0) return String.format(Language.tr("Simulation.Creator.NegativeID"),element.getClass().getName());
			if (element.id>maxID) maxID=element.id;
			element.prepareRun(runModel);
		}

		/* Direkter Zugriff auf die Elemente �ber ihre IDs */
		runModel.elementsFast=new RunElement[maxID+1];
		for (int i=0;i<=maxID;i++) runModel.elementsFast[i]=runModel.elements.get(i);

		return null;
	}

	private static String initAdditionalStatistics(final EditModel editModel, final RunModel runModel) {
		if (!editModel.longRunStatistics.isActive()) return null;

		final RunModelCreator creator=new RunModelCreator(editModel,runModel,false);
		return creator.addLongRunStatistic();
	}

	/**
	 * Wandelt ein <code>EditModel</code> in ein <code>RunModel</code> um. Dabei wird das Modell auf Konsistenz gepr�ft
	 * und alle notwendigen Verkn�pfungen werden hergestellt.
	 * @param editModel	Editor-Modell, welches in ein Laufzeit-Modell umgewandelt werden soll
	 * @param testOnly	Wird hier <code>true</code> �bergeben, so werden externe Datenquellen nicht wirklich geladen
	 * @return	Gibt im Erfolgsfall ein Objekt vom Typ <code>RunModel</code> zur�ck, sonst einen String mit einer Fehlermeldung.
	 * @see EditModel
	 */
	public static Object getRunModel(final EditModel editModel, final boolean testOnly) {
		RunModel runModel=new RunModel();

		String error;
		error=initVariables(editModel,runModel); if (error!=null) return error;
		error=initGeneralData(editModel,runModel); if (error!=null) return error;
		error=initElementsData(editModel,runModel,testOnly); if (error!=null) return error;
		error=initGeneralData2(editModel,runModel); if (error!=null) return error; /* Hier brauchen wir die Variablennamen und die werden erst in initElementsData gesetzt. */
		error=initAdditionalStatistics(editModel,runModel); if (error!=null) return error;

		return runModel;
	}

	/**
	 * Liefert die Nummer eines Fertigungsplans
	 * @param name	Name des Fertigungsplans
	 * @return	Nummer des Plans (oder -1, wenn kein Plan mit dem Namen gefunden wurde)
	 * @see RunModel#sequenceNames
	 */
	public int getSequenceNr(final String name) {
		if (name.isEmpty()) return -1;
		for (int i=0;i<sequenceNames.length;i++) if (sequenceNames[i].equalsIgnoreCase(name)) return i;
		return -1;
	}
}