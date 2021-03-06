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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationTableDialog;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;

/**
 * Zeigt ein sich w�hrend der Animation aktualisierendes Liniendiagramm an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationLineDiagram extends ModelElementAnimationDiagramBase {
	private Semaphore drawLock=new Semaphore(1);
	private List<double[]> recordedValues;
	private List<Integer[]> recordedDrawValues;
	private double recordedDrawValuesHeight;
	private long[] recordedTimeStamps;

	private final List<String> expression=new ArrayList<>();
	private final List<Double> minValue=new ArrayList<>();
	private final List<Double> maxValue=new ArrayList<>();
	private final List<Color> expressionColor=new ArrayList<>();
	private final List<Integer> expressionWidth=new ArrayList<>();

	private long timeArea=60*5;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnimationLineDiagram(final EditModel model, final ModelSurface surface) {
		super(model,surface);
	}

	/**
	 * Stellt die Gr��e der umrandenden Box ein
	 * @param size	Gr��e der Box
	 */
	@Override
	public void setSize(final Dimension size) {
		super.setSize(size);
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationDiagram.Tooltip");
	}

	/**
	 * Liefert eine Liste der vorhandenen Diagramm-Eintr�ge.<br>
	 * Jeder Diagramm-Eintrag besteht aus 5 Objekten in einem Array: Ausdruck (String), Minimalwert (Double), Maximalwert (Double), Linienfarbe (Color), Linienbreite (Integer).
	 * @return	Liste der Diagramm-Eintr�ge
	 */
	public List<Object[]> getExpressionData() {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			if (i>=minValue.size()) break;
			if (i>=maxValue.size()) break;
			if (i>=expressionColor.size()) break;
			if (i>=expressionWidth.size()) break;

			Object[] row=new Object[5];
			row[0]=expression.get(i);
			row[1]=minValue.get(i);
			row[2]=maxValue.get(i);
			row[3]=expressionColor.get(i);
			row[4]=expressionWidth.get(i);

			data.add(row);
		}
		return data;
	}

	/**
	 * Ersetzt die bisherigen Diagramm-Eintr�ge durch eine neue Liste.<br>
	 * Jeder Diagramm-Eintrag besteht aus 5 Objekten in einem Array: Ausdruck (String), Minimalwert (Double), Maximalwert (Double), Linienfarbe (Color), Linienbreite (Integer).
	 * @param data	Liste der neuen Diagramm-Eintr�ge
	 */
	public void setExpressionData(final List<Object[]> data) {
		expression.clear();
		minValue.clear();
		maxValue.clear();
		expressionColor.clear();
		expressionWidth.clear();

		for (Object[] row: data) if (row.length==5) {
			if (!(row[0] instanceof String)) continue;
			if (!(row[1] instanceof Double) && !(row[1] instanceof Integer)) continue;
			if (!(row[2] instanceof Double) && !(row[2] instanceof Integer)) continue;
			if (!(row[3] instanceof Color)) continue;
			if (!(row[4] instanceof Integer)) continue;
			expression.add((String)row[0]);
			if (row[1] instanceof Double) minValue.add((Double)row[1]); else minValue.add(Double.valueOf(((Integer)row[1]).intValue()));
			if (row[2] instanceof Double) maxValue.add((Double)row[2]); else maxValue.add(Double.valueOf(((Integer)row[2]).intValue()));
			expressionColor.add((Color)row[3]);
			expressionWidth.add((Integer)row[4]);
		}
	}

	/**
	 * Gibt an, wie gro� der darzustellende Bereich (in Sekunden) sein soll
	 * @return	Im Diagramm darzustellender Zeitbereich (in Sekunden)
	 */
	public long getTimeArea() {
		return timeArea;
	}

	/**
	 * Stellt ein, wie gro� der darzustellende Bereich (in Sekunden) sein soll
	 * @param timeArea	Im Diagramm darzustellender Zeitbereich (in Sekunden)
	 */
	public void setTimeArea(final int timeArea) {
		if (timeArea>=1) this.timeArea=timeArea;
		fireChanged();
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationLineDiagram)) return false;

		if (expression.size()!=((ModelElementAnimationLineDiagram)element).expression.size()) return false;
		for (int i=0;i<expression.size();i++) if (!expression.get(i).equals(((ModelElementAnimationLineDiagram)element).expression.get(i))) return false;
		if (minValue.size()!=((ModelElementAnimationLineDiagram)element).minValue.size()) return false;
		for (int i=0;i<minValue.size();i++) {
			Double D1=minValue.get(i);
			Double D2=((ModelElementAnimationLineDiagram)element).minValue.get(i);
			if (D1==null || D2==null) return false;
			double d1=D1;
			double d2=D2;
			if (d1!=d2) return false;
		}
		if (maxValue.size()!=((ModelElementAnimationLineDiagram)element).maxValue.size()) return false;
		for (int i=0;i<maxValue.size();i++) {
			Double D1=maxValue.get(i);
			Double D2=((ModelElementAnimationLineDiagram)element).maxValue.get(i);
			if (D1==null || D2==null) return false;
			double d1=D1;
			double d2=D2;
			if (d1!=d2) return false;
		}
		if (expressionColor.size()!=((ModelElementAnimationLineDiagram)element).expressionColor.size()) return false;
		for (int i=0;i<expressionColor.size();i++) if (!expressionColor.get(i).equals(((ModelElementAnimationLineDiagram)element).expressionColor.get(i))) return false;

		if (expressionWidth.size()!=((ModelElementAnimationLineDiagram)element).expressionWidth.size()) return false;
		for (int i=0;i<expressionWidth.size();i++) {
			Integer I1=expressionWidth.get(i);
			Integer I2=((ModelElementAnimationLineDiagram)element).expressionWidth.get(i);
			if (I1==null || I2==null) return false;
			int i1=I1;
			int i2=I2;
			if (i1!=i2) return false;
		}

		if (timeArea!=((ModelElementAnimationLineDiagram)element).timeArea) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationLineDiagram) {

			expression.addAll(((ModelElementAnimationLineDiagram)element).expression);
			minValue.addAll(((ModelElementAnimationLineDiagram)element).minValue);
			maxValue.addAll(((ModelElementAnimationLineDiagram)element).maxValue);
			expressionColor.addAll(((ModelElementAnimationLineDiagram)element).expressionColor);
			expressionWidth.addAll(((ModelElementAnimationLineDiagram)element).expressionWidth);

			timeArea=((ModelElementAnimationLineDiagram)element).timeArea;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationLineDiagram clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationLineDiagram element=new ModelElementAnimationLineDiagram(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	private void drawDummyDiagramLines(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(Math.max(1,Math.round(2*zoom))));

		int x1=rectangle.x;
		int x2=(int)FastMath.round(rectangle.x+1.0/4.0*rectangle.width);
		int x3=(int)FastMath.round(rectangle.x+2/4.0*rectangle.width);
		int x4=(int)FastMath.round(rectangle.x+3/4.0*rectangle.width);

		int y1=(int)FastMath.round(rectangle.y+3.0/4.0*rectangle.height);
		int y2=(int)FastMath.round(rectangle.y+1.0/4.0*rectangle.height);
		int y3=(int)FastMath.round(rectangle.y+2.0/4.0*rectangle.height);

		g.drawLine(x1,y1,x2,y2);
		g.drawLine(x2,y2,x3,y3);
		g.drawLine(x3,y3,x4,y2);
	}

	private double lastZoom;
	private BasicStroke[] drawCacheStroke;
	private Color[] drawCacheColor;
	private int[] drawCacheXValues;
	private int[] drawCacheYValues;

	private static Integer[] drawIntegersPlus=new Integer[1000];
	private static Integer[] drawIntegersMinus=new Integer[1000];
	static {
		for (int i=0;i<drawIntegersPlus.length;i++) drawIntegersPlus[i]=i;
		for (int i=0;i<drawIntegersMinus.length;i++) drawIntegersMinus[i]=-i;
	}

	@Override
	protected void drawDiagramData(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		if (recordedValues==null) {
			drawDummyDiagramLines(g,rectangle,zoom);
			return;
		}

		if (recordedValues.size()<2) return;

		boolean needRecalcAll=(recordedDrawValuesHeight!=rectangle.height);
		if (needRecalcAll) recordedDrawValuesHeight=rectangle.height;

		drawLock.acquireUninterruptibly();
		try {
			/* Beim ersten Aufruf: Stroke und Color vorbereiten */
			if (drawCacheStroke==null || zoom!=lastZoom) {
				lastZoom=zoom;
				drawCacheStroke=new BasicStroke[expression.size()];
				for (int i=0;i<drawCacheStroke.length;i++) {
					final BasicStroke stroke=new BasicStroke(Math.max(1,Math.round(expressionWidth.get(i)*zoom)));
					if (i==0 || !stroke.equals(drawCacheStroke[i-1])) drawCacheStroke[i]=stroke; else drawCacheStroke[i]=null;
				}
				drawCacheColor=new Color[expression.size()];
				for (int i=0;i<drawCacheColor.length;i++) {
					final Color color=expressionColor.get(i);
					if (i==0 || !color.equals(drawCacheColor[i-1])) drawCacheColor[i]=color; else drawCacheColor[i]=null;
				}
			}

			/* Daten-Array-Cache vorbereiten */
			final int valuesLength=recordedValues.size();
			if (drawCacheYValues==null || drawCacheYValues.length<valuesLength) drawCacheYValues=new int[FastMath.max(1000,valuesLength)];
			if (drawCacheXValues==null || drawCacheXValues.length<valuesLength) drawCacheXValues=new int[FastMath.max(1000,valuesLength)];

			/* x-Positionen bestimmen */
			final double maxTime=recordedTimeStamps[recordedValues.size()-1];
			final double minTime=maxTime-timeArea*1000;
			final double scaleX=rectangle.width/(maxTime-minTime);
			for (int i=0;i<valuesLength;i++) drawCacheXValues[i]=rectangle.x+(int)FastMath.round((recordedTimeStamps[i]-minTime)*scaleX);

			/* F�r alle Datenreihen... */
			for (int i=0;i<expression.size();i++) {
				final double min=minValue.get(i);
				final double max=maxValue.get(i);
				final double scaleY=(rectangle.height-2)/(max-min);

				/* y-Positionen bestimmen */
				for (int j=0;j<valuesLength;j++) {
					final int yInt;
					final Integer I=recordedDrawValues.get(j)[i];
					if (needRecalcAll || I==null) {
						final double d=recordedValues.get(j)[i];
						yInt=rectangle.y+1+rectangle.height-2-(int)FastMath.round((d-min)*scaleY);
						Integer J=null;
						if (yInt>=0 && yInt<drawIntegersPlus.length) J=drawIntegersPlus[yInt];
						if (yInt<0 && -yInt<drawIntegersMinus.length) J=drawIntegersMinus[-yInt];
						if (J==null) J=yInt;
						recordedDrawValues.get(j)[i]=J;
					} else {
						yInt=I.intValue();
					}
					drawCacheYValues[j]=yInt;
				}

				/* Linienfarbe und -breite */
				if (drawCacheColor[i]!=null) g.setColor(drawCacheColor[i]);
				if (drawCacheStroke[i]!=null) g.setStroke(drawCacheStroke[i]);

				/* Zeichnen */
				/*
				final GeneralPath path=new GeneralPath(GeneralPath.WIND_NON_ZERO);
				path.moveTo(drawCacheXValues[0],drawCacheYValues[0]);
				for (int j=1;j<valuesLength;j++) path.lineTo(drawCacheXValues[j],drawCacheYValues[j]);
				g.draw(path);
				 */
				for (int j=1;j<valuesLength;j++) {
					g.drawLine(drawCacheXValues[j-1],drawCacheYValues[j-1],drawCacheXValues[j],drawCacheYValues[j]);
				}
			}
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationDiagram.Name");
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
			new ModelElementAnimationLineDiagramDialog(owner,ModelElementAnimationLineDiagram.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationDiagram.XML.Root");
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

		for (int i=0;i<expression.size();i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.Set"));
			node.appendChild(sub);
			sub.setTextContent(expression.get(i));
			sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.Minimum"),NumberTools.formatSystemNumber(minValue.get(i)));
			sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.Maximum"),NumberTools.formatSystemNumber(maxValue.get(i)));
			sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineColor"),EditModel.saveColor(expressionColor.get(i)));
			sub.setAttribute(Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineWidth"),""+expressionWidth.get(i));
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationDiagram.XML.Range"));
		node.appendChild(sub);
		sub.setTextContent(""+timeArea);
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

		if (Language.trAll("Surface.AnimationDiagram.XML.Set",name)) {
			Double D;
			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.Minimum",node)));
			if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.Minimum"),name,node.getParentNode().getNodeName());
			double minValue=D;
			D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.Maximum",node)));
			if (D==null || D<=minValue) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.Maximum"),name,node.getParentNode().getNodeName());
			this.minValue.add(minValue);
			maxValue.add(D);

			final Color color=EditModel.loadColor(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.LineColor",node));
			if (color==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineColor"),name,node.getParentNode().getNodeName());
			expressionColor.add(color);

			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.AnimationDiagram.XML.Set.LineWidth",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationDiagram.XML.Set.LineWidth"),name,node.getParentNode().getNodeName());
			expressionWidth.add(I);

			expression.add(content);

			return null;
		}

		if (Language.trAll("Surface.AnimationDiagram.XML.Range",name)) {
			Long L=NumberTools.getPositiveLong(content);
			if (L==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			timeArea=L;
			return null;
		}

		return null;
	}

	private ExpressionCalc[] animationExpression;

	private double calcExpression(final SimulationData simData, final int index) {
		final ExpressionCalc calc=animationExpression[index];
		if (calc==null) return 0.0;
		simData.runData.setClientVariableValues(null);
		return calc.calcOrDefault(simData.runData.variableValues,simData,null,0);
	}

	private List<double[]> cacheDouble=new ArrayList<>();
	private List<Integer[]> cacheInteger=new ArrayList<>();

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		if (isPreview) return false;

		if (recordedValues==null) {
			drawLock.acquireUninterruptibly();
			try {
				recordedValues=new ArrayList<>();
				recordedDrawValues=new ArrayList<>();
				recordedTimeStamps=new long[0];
			} finally {
				drawLock.release();
			}
		}

		final int cacheDoubleSize=cacheDouble.size();
		final int cacheIntegerSize=cacheInteger.size();

		final double[] data=(cacheDoubleSize==0)?new double[expression.size()]:cacheDouble.remove(cacheDoubleSize-1);
		for (int i=0;i<data.length;i++) data[i]=calcExpression(simData,i);

		final Integer[] drawData;
		if (cacheIntegerSize==0) {
			drawData=new Integer[data.length];
		} else {
			drawData=cacheInteger.remove(cacheIntegerSize-1);
			Arrays.fill(drawData,null);
		}

		drawLock.acquireUninterruptibly();
		try {
			int size=recordedValues.size();
			if (size>0 && recordedTimeStamps[size-1]==simData.currentTime) {
				cacheDouble.add(recordedValues.set(size-1,data));
				cacheInteger.add(recordedDrawValues.set(size-1,drawData));
			} else {
				recordedValues.add(data);
				recordedDrawValues.add(drawData);
				if (recordedDrawValues.size()>recordedTimeStamps.length) {
					if (recordedTimeStamps.length==0) {
						recordedTimeStamps=new long[1000];
					} else {
						recordedTimeStamps=Arrays.copyOf(recordedTimeStamps,2*recordedTimeStamps.length);
					}
				}
				recordedTimeStamps[recordedDrawValues.size()-1]=simData.currentTime;

				int removeCount=0;
				size=recordedValues.size();
				final long limitValue=simData.currentTime-timeArea*1000;
				for (int i=0;i<size;i++) {
					long l=recordedTimeStamps[i];
					if (l>=limitValue) break;
					removeCount++;
				}
				if (removeCount>0) {
					for (int i=0;i<removeCount;i++) {
						cacheDouble.add(recordedValues.remove(0));
						cacheInteger.add(recordedDrawValues.remove(0));
					}
					for (int i=removeCount;i<size;i++) recordedTimeStamps[i-removeCount]=recordedTimeStamps[i];
				}
			}
		} finally {
			drawLock.release();
		}

		return true;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		recordedValues=null;
		drawCacheStroke=null;
		drawCacheColor=null;
		animationExpression=new ExpressionCalc[expression.size()];

		for (int i=0;i<expression.size();i++) {
			animationExpression[i]=new ExpressionCalc(simData.runModel.variableNames);
			if (animationExpression[i].parse(expression.get(i))>=0) animationExpression[i]=null;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationDiagram";
	}

	private String getHTMLAnimationDiagram(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationDiagram(rect,borderColor,borderWidth,fillColor) {\n");

		sb.append("  if (typeof(fillColor)!=\"undefined\") {\n");
		sb.append("    context.fillStyle=fillColor;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("  context.strokeStyle=\"blue\";\n");
		sb.append("  context.lineWidth=2;\n");
		sb.append("  var x1=rect.x;\n");
		sb.append("  var x2=Math.round(rect.x+0.25*rect.w);\n");
		sb.append("  var x3=Math.round(rect.x+0.5*rect.w);\n");
		sb.append("  var x4=Math.round(rect.x+0.75*rect.w);\n");
		sb.append("  var y1=Math.round(rect.y+0.75*rect.h);\n");
		sb.append("  var y2=Math.round(rect.y+0.25*rect.h);\n");
		sb.append("  var y3=Math.round(rect.y+0.5*rect.h);\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(x1,y1);\n");
		sb.append("  context.lineTo(x2,y2);\n");
		sb.append("  context.lineTo(x3,y3);\n");
		sb.append("  context.lineTo(x4,y2);\n");
		sb.append("  context.stroke();\n");

		sb.append("}\n");

		return sb.toString();
	}

	@Override
	protected void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationDiagram",builder->getHTMLAnimationDiagram(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";

		if (backgroundColor==null) {
			outputBuilder.outputBody.append("drawAnimationDiagram("+rect+","+border+","+borderWidth+");\n");
		} else {
			final String fill="\""+HTMLOutputBuilder.colorToHTML(backgroundColor)+"\"";
			outputBuilder.outputBody.append("drawAnimationDiagram("+rect+","+border+","+borderWidth+","+fill+");\n");
		}
	}

	@Override
	public boolean hasAnimationStatisticsData(final SimulationData simData) {
		return simData!=null;
	}

	private Table getAnimationRunTimeTableData(final SimulationData simData) {
		final Table table=new Table();

		final int colCount=expression.size()+1;
		List<String> line=new ArrayList<>(colCount);
		line.add(Language.tr("Statistic.Viewer.Chart.Time"));
		line.addAll(expression);
		table.addLine(line);

		drawLock.acquireUninterruptibly();
		try {
			for (int i=0;i<recordedDrawValues.size();i++) {
				line=new ArrayList<>(colCount);
				line.add(TimeTools.formatLongTime(recordedTimeStamps[i]/1000.0));
				for (double value: recordedValues.get(i)) line.add(NumberTools.formatNumber(value));
				table.addLine(line);
			}
		} finally {
			drawLock.release();
		}

		return table;
	}

	@Override
	public void showElementAnimationStatisticsData(final Component owner, final SimulationData simData) {
		if (simData==null) return;
		new ModelElementAnimationTableDialog(owner,getContextMenuElementName()+" (id="+getId()+")",()->getAnimationRunTimeTableData(simData));
	}

	@Override
	protected void storeElementAnimationStatisticsData(final Component owner, final JPopupMenu menu, final SimulationData simData) {
		if (simData==null) return;
		ModelElementAnimationTableDialog.buildPopupMenuItem(owner,menu,getAnimationRunTimeTableData(simData));
	}
}