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
package ui.modeleditor.elements;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse kapselt einen einzelnen Batch-Bildungs-Datensatz.
 * @author Alexander Herzog
 * @see ModelElementBatch
 * @see ModelElementBatchMulti
 */
public class BatchRecord implements Cloneable {
	/**
	 * Art der Batch-Bildung
	 * @author Alexander Herzog
	 * @see BatchRecord#getBatchMode()
	 * @see BatchRecord#setBatchMode(BatchMode)
	 */
	public enum BatchMode {
		/** Sammelt die Kunden und leitet sie dann gemeinsam weiter (kein Batching im engeren Sinne). */
		BATCH_MODE_COLLECT,

		/** Fasst die Kunden zu einem tempor�ren Batch zusammen. */
		BATCH_MODE_TEMPORARY,

		/** Fasst die Kunden zu einem neuen Kunden zusammen (permanentes Batching). */
		BATCH_MODE_PERMANENT
	}

	private int batchSizeMin;
	private int batchSizeMax;
	private BatchMode batchMode;
	private String newClientType;

	/**
	 * Konstruktor der Klasse
	 */
	public BatchRecord() {
		batchSizeMin=1;
		batchSizeMax=1;
		batchMode=BatchMode.BATCH_MODE_COLLECT;
		newClientType="";
	}

	/**
	 * �berpr�ft, ob der Datensatz mit dem angegebenen Datensatz inhaltlich identisch ist.
	 * @param otherBatchRecord	Anderer Datensatz mit dem dieser Datensatz verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Datens�tze identisch sind.
	 */
	public boolean equalsBatchRecord(final BatchRecord otherBatchRecord) {
		if (otherBatchRecord==null) return false;

		if (batchMode!=otherBatchRecord.batchMode) return false;
		if (batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) {
			if (!otherBatchRecord.newClientType.equals(newClientType)) return false;
		}
		if (batchSizeMin!=otherBatchRecord.batchSizeMin) return false;
		if (batchSizeMax!=otherBatchRecord.batchSizeMax) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param copySource	Datensatz, von dem alle Einstellungen �bernommen werden sollen
	 */
	public void copyDataFrom(final BatchRecord copySource) {
		if (copySource==null) return;

		batchMode=copySource.batchMode;
		if (batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) {
			newClientType=copySource.newClientType;
		} else {
			newClientType="";
		}
		batchSizeMin=copySource.batchSizeMin;
		batchSizeMax=copySource.batchSizeMax;
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @return	Kopiertes Element
	 */
	@Override
	public BatchRecord clone() {
		final BatchRecord batchRecord=new BatchRecord();
		batchRecord.copyDataFrom(this);
		return batchRecord;
	}

	/**
	 * Liefert den aktuell gew�hlten Batch-Modus.
	 * @return	Aktueller Batch-Modus
	 * @see BatchMode
	 */
	public BatchMode getBatchMode() {
		return batchMode;
	}

	/**
	 * Stellt den Batch-Modus ein.
	 * @param batchMode	Neuer Batch-Modus
	 * @see BatchMode
	 */
	public void setBatchMode(BatchMode batchMode) {
		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			this.batchMode=BatchMode.BATCH_MODE_COLLECT;
			newClientType="";
			break;
		case BATCH_MODE_TEMPORARY:
			this.batchMode=BatchMode.BATCH_MODE_TEMPORARY;
			break;
		case BATCH_MODE_PERMANENT:
			this.batchMode=BatchMode.BATCH_MODE_PERMANENT;
			break;
		}
	}

	/**
	 * Liefert den Kundentyp, unter dem die zusammengefassten Kunden weitergeleitet werden sollen.
	 * @return	Neuer Kundentyp oder leerer String, wenn die Kunden einzeln weitergeleitet werden sollen
	 */
	public String getNewClientType() {
		return newClientType;
	}

	/**
	 * Stellt den Kundentyp ein, unter dem die zusammengefassten Kunden weitergeleitet werden sollen.
	 * @param newClientType	Neuer Kundentyp oder leerer String, wenn die Kunden einzeln weitergeleitet werden sollen
	 */
	public void setNewClientType(final String newClientType) {
		if (newClientType==null || newClientType.trim().isEmpty()) {
			this.newClientType="";
			batchMode=BatchMode.BATCH_MODE_COLLECT;
		} else {
			this.newClientType=newClientType;
			if (batchMode==BatchMode.BATCH_MODE_COLLECT) batchMode=BatchMode.BATCH_MODE_PERMANENT;
		}
	}

	/**
	 * Liefert die aktuelle minimale Batch-Gr��e
	 * @return Minimale Batch-Gr��e
	 */
	public int getBatchSizeMin() {
		return batchSizeMin;
	}

	/**
	 * Liefert die aktuelle maximale Batch-Gr��e
	 * @return Maximale Batch-Gr��e
	 */
	public int getBatchSizeMax() {
		return batchSizeMax;
	}

	/**
	 * Stellt die minimale Batch-Gr��e ein.
	 * @param batchSizeMin	Neue minimale Batch-Gr��e
	 */
	public void setBatchSizeMin(final int batchSizeMin) {
		if (batchSizeMin>=1) this.batchSizeMin=batchSizeMin;
	}

	/**
	 * Stellt die maximale Batch-Gr��e ein.
	 * @param batchSizeMax	Neue maximale Batch-Gr��e
	 */
	public void setBatchSizeMax(final int batchSizeMax) {
		if (batchSizeMax>=1) this.batchSizeMax=batchSizeMax;
	}

	/**
	 * Speichert die Einstellungen des Datensatzes als Untereintr�ge eines xml-Knotens.
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 * @see #loadProperty(String, String, Element)
	 */
	public void addDataToXML(final Document doc, final Element node) {
		Element sub;

		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
			sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Collect"));
			break;
		case BATCH_MODE_TEMPORARY:
			if (!newClientType.isEmpty()) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Temporary"));
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.ClientType")));
				sub.setTextContent(newClientType);
			} else {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Collect"));
			}
			break;
		case BATCH_MODE_PERMANENT:
			if (!newClientType.isEmpty()) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Permanent"));
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.ClientType")));
				sub.setTextContent(newClientType);
			} else {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Batch.XML.BatchMode.Collect"));
			}
			break;
		}

		if (batchSizeMin>1 || batchSizeMax>1) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.Batch")));
			sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Batch.SizeMin"),""+batchSizeMin);
			sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Batch.SizeMax"),""+batchSizeMax);
		}
	}

	/**
	 * Speichert die Einstellungen des Datensatzes als Untereintr�ge eines xml-Knotens.<br>
	 * Die Daten werden dabei in ein Zwischenelement, welches als Attribut einen Namen besitzt, geschrieben.
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 * @param recordName	Name der im Attribut im Zwischenelement verwendet werden soll
	 * @see #loadFromXML(Element)
	 */
	public void addDataToXML(final Document doc, final Element node, final String recordName) {
		Element sub;
		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Batch.XML.Record")));
		sub.setAttribute(Language.trPrimary("Surface.Batch.XML.Record.Name"),recordName);
		addDataToXML(doc,sub);
	}

	/**
	 * L�dt eine einzelne Einstellung des Batch-Datensatzes aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 * @see #addDataToXML(Document, Element)
	 */
	public String loadProperty(final String name, final String content, final Element node) {
		if (Language.trAll("Surface.Batch.XML.Batch",name)) {
			String size;

			size=Language.trAllAttribute("Surface.Batch.XML.Batch.SizeMin",node);
			if (!size.isEmpty()) {
				final Long L=NumberTools.getPositiveLong(size);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Batch.XML.Batch.SizeMin"),name,node.getParentNode().getNodeName());
				batchSizeMin=(int)((long)L);
				if (batchSizeMax<batchSizeMin) batchSizeMax=batchSizeMin;
			}

			size=Language.trAllAttribute("Surface.Batch.XML.Batch.SizeMax",node);
			if (!size.isEmpty()) {
				final Long L=NumberTools.getPositiveLong(size);
				if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Batch.XML.Batch.SizeMax"),name,node.getParentNode().getNodeName());
				batchSizeMax=(int)((long)L);
				if (batchSizeMin>batchSizeMax) batchSizeMin=batchSizeMax;
			}
			return null;
		}

		if (Language.trAll("Surface.Batch.XML.BatchMode",name)) {
			if (Language.trAll("Surface.Batch.XML.BatchMode.Collect",content)) batchMode=BatchMode.BATCH_MODE_COLLECT;
			if (Language.trAll("Surface.Batch.XML.BatchMode.Temporary",content)) batchMode=BatchMode.BATCH_MODE_TEMPORARY;
			if (Language.trAll("Surface.Batch.XML.BatchMode.Permanent",content)) batchMode=BatchMode.BATCH_MODE_PERMANENT;
			return null;
		}

		if (Language.trAll("Surface.Batch.XML.ClientType",name)) {
			newClientType=content;
			if (!content.trim().isEmpty() && batchMode==BatchMode.BATCH_MODE_COLLECT) batchMode=BatchMode.BATCH_MODE_PERMANENT;
			return null;
		}

		return null;
	}

	private String loadedRecordName="";

	/**
	 * L�dt einen kompletten Batch-Datensatz aus den Untereintr�gen eines xml-Elements
	 * @param node	xml-Zwischenelement in dessen Untereintr�gen die Daten stehen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 * @see #addDataToXML(Document, Element, String)
	 * @see #getLoadedRecordName()
	 */
	public String loadFromXML(final Element node) {
		if (!Language.trAll("Surface.Batch.XML.Record",node.getNodeName())) return null;

		loadedRecordName=Language.trAllAttribute("Surface.Batch.XML.Record.Name",node);

		final NodeList l=node.getChildNodes();
		final int size=l.getLength();
		for (int i=0; i<size;i++) {
			final Node sub=l.item(i);
			if (!(sub instanceof Element)) continue;
			Element e=(Element)sub;
			String error=loadProperty(e.getNodeName(),e.getTextContent(),e);
			if (error!=null) return error;
		}

		return null;
	}

	/**
	 * Liefert den beim Aufruf von {@link #loadFromXML(Element)} mit eingelesenen Namen f�r den Datensatz.
	 * @return	Name f�r den Datensatz oder ein leerer String, wenn keine Daten geladen wurden.
	 * @see #loadFromXML(Element)
	 */
	public String getLoadedRecordName() {
		return loadedRecordName;
	}

	/**
	 * Erstellt eine Beschreibung f�r den Datensatz
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 * @param level	Start-Level f�r die Ausgabe. Es werden <code>level</code>, <code>level+1</code> und <code>level+2</code> verwendet.
	 */
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder, final int level) {
		/* Batching Modus */
		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Mode"),Language.tr("ModelDescription.Batch.Mode.Collect"),level);
			break;
		case BATCH_MODE_TEMPORARY:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Mode"),Language.tr("ModelDescription.Batch.Mode.Temporary"),level);
			break;
		case BATCH_MODE_PERMANENT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Mode"),Language.tr("ModelDescription.Batch.Mode.Permanent"),level);
			break;
		}

		/* Batchgr��e */
		if (batchSizeMin==batchSizeMax) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Size"),""+batchSizeMin,level+1);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.Size"),""+batchSizeMin+".."+batchSizeMax,level+1);
		}

		/* Neuer Kundentyp */
		if ((batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) && !newClientType.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Batch.NewClientType"),newClientType,level+2);
		}
	}
}
