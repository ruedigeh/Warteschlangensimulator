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

import java.util.ArrayList;
import java.util.Arrays;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.coreelements.RunElementMultiQueueData;
import simulator.runmodel.RunDataClient;

/**
 * Laufzeitdaten eines <code>RunElementMatch</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementMatch
 * @see RunElementData
 */
public class RunElementMatchData extends RunElementData implements RunElementMultiQueueData {
	/**
	 * Liste der wartenden Kunden in den Schlangen
	 */
	public final ArrayList<RunDataClient>[] waitingClients;

	/**
	 * Kunden, die gemeinsam weitergeleitet werden, werden f�r die Animation hier gesammelt
	 */
	public final RunDataClient[] moveClientsList;

	/**
	 * Gibt an, f�r wie viele der gemeinsam weiterzuleitenden Kunden schon das Leave-Ereignis abgearbeitet wurde.
	 * Erst beim letzten Kunden wird dann ein multiSend ausgel�st.
	 */
	public int moveNr=-1;

	/**
	 * Indices der Kunden in den anderen Warteschlangen, die dort entnommen werden sollen und
	 * zusammen mit dem gerade eingetroffenen Kunden weitergeleitet (bzw. gebatcht) werden sollen.<br>
	 * (Dieses Array vermeidet das wiederholte Anlagen von entsprechenden Arrays w�hrend der Simulation.)
	 */
	public final int[] selectQueuedClients;

	/**
	 * Konstruktor der Klasse <code>RunElementMatchData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param numberOfQueues	Anzahl der einlaufenden Verbindungen
	 */
	@SuppressWarnings("unchecked")
	public RunElementMatchData(final RunElement station, final int numberOfQueues) {
		super(station);

		waitingClients=new ArrayList[numberOfQueues];
		for (int i=0;i<numberOfQueues;i++) waitingClients[i]=new ArrayList<>();

		moveClientsList=new RunDataClient[numberOfQueues];

		selectQueuedClients=new int[numberOfQueues];
		Arrays.fill(selectQueuedClients,0);
	}

	@Override
	public int getQueueCount() {
		return waitingClients.length;
	}

	@Override
	public int getQueueSize(int queueNumber) {
		if (queueNumber<0 || queueNumber>=waitingClients.length) return 0;
		return waitingClients[queueNumber].size();
	}
}