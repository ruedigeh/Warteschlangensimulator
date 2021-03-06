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

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * F�hrt, wenn ein Kunde diese Station passiert, ein Skript aus und h�ngt das Ergebnis
 * des Skriptes an eine Ausgabedatei an.
 * @author Alexander Herzog
 *
 */
public class ModelElementOutputJS extends ModelElementMultiInSingleOutBox implements ElementNoRemoteSimulation {
	/**
	 * Zu verwendende Programmiersprache f�r das Skript
	 * @author Alexander Herzog
	 * @see ModelElementOutputJS#getMode()
	 * @see ModelElementOutputJS#setMode(ScriptMode)
	 */
	public enum ScriptMode {
		/** Javascript als Sprache verwenden */
		Javascript,
		/** Java als Sprache verwenden */
		Java
	}

	private String script;
	private ScriptMode mode;
	private String outputFile;

	/**
	 * Konstruktor der Klasse <code>ModelElementOutputJS</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementOutputJS(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_DOCUMENT);
		script="";
		mode=ScriptMode.Javascript;
		outputFile="";
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_OUTPUT_JS.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.OutputJS.Tooltip");
	}

	/**
	 * Das aktuelle Skript.
	 * @return	Skript
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Setzt das aktuelle Skript
	 * @param script	Neues Skript
	 */
	public void setScript(final String script) {
		if (script!=null) this.script=script;
	}

	/**
	 * Gibt die Skriptsprache an
	 * @return	Skriptsprache
	 * @see ModelElementSetJS.ScriptMode
	 */
	public ScriptMode getMode() {
		return mode;
	}

	/**
	 * Stellt die Skriptsprache ein.
	 * @param mode	Skriptsprache
	 * @see ModelElementSetJS.ScriptMode
	 */
	public void setMode(final ScriptMode mode) {
		if (mode!=null) this.mode=mode;
	}

	/**
	 * Liefert den Dateinamen der Datei, die f�r die Speicherung der Ausgaben verwendet werden soll.
	 * @return	Dateiname der Datei f�r die Ausgaben
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Stellt den Dateinamen der Datei, die f�r die Speicherung der Ausgaben verwendet werden soll, ein.
	 * @param outputFile	Dateiname der Datei f�r die Ausgaben
	 */
	public void setOutputFile(final String outputFile) {
		if (outputFile!=null) this.outputFile=outputFile;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementOutputJS)) return false;

		if (!script.equals(((ModelElementOutputJS)element).script)) return false;
		if (((ModelElementOutputJS)element).mode!=mode) return false;
		if (!outputFile.equals(((ModelElementOutputJS)element).outputFile)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementOutputJS) {
			script=((ModelElementOutputJS)element).script;
			mode=((ModelElementOutputJS)element).mode;
			outputFile=((ModelElementOutputJS)element).outputFile;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementOutputJS clone(final EditModel model, final ModelSurface surface) {
		final ModelElementOutputJS element=new ModelElementOutputJS(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.OutputJS.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.OutputJS.Name.Short");
	}

	private static final Color defaultBackgroundColor=new Color(230,230,230);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementOutputJSDialog(owner,ModelElementOutputJS.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsData(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.OutputJS.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		if (!script.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.OutputJS.XML.Script")));
			switch (mode) {
			case Java:
				sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.Script.Language"),Language.trPrimary("Surface.OutputJS.XML.Script.Java"));
				break;
			case Javascript:
				sub.setAttribute(Language.trPrimary("Surface.OutputJS.XML.Script.Language"),Language.trPrimary("Surface.OutputJS.XML.Script.Javascript"));
				break;
			}
			sub.setTextContent(script);
		}

		if (!outputFile.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.OutputJS.XML.File")));
			sub.setTextContent(outputFile);
		}
	}

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.OutputJS.XML.Script",name)) {
			script=content;
			final String langName=Language.trAllAttribute("Surface.OutputJS.XML.Script.Language",node);
			if (Language.trAll("Surface.OutputJS.XML.Script.Java",langName)) mode=ScriptMode.Java;
			if (Language.trAll("Surface.OutputJS.XML.Script.Javascript",langName)) mode=ScriptMode.Javascript;
			return null;
		}

		if (Language.trAll("Surface.OutputJS.XML.File",name)) {
			outputFile=content;
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementOutputJS";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!outputFile.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.OutputJS.File"),outputFile,2000);
		}
	}
}
