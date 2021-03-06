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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Zeigt einen sich w�hrend der Animation aktualisierenden
 * aus mehreren Teilen zusammengesetzten Balken an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationBarStack extends ModelElementPosition implements ElementWithAnimationDisplay {
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Richtung in die der Balken aufgef�llt werden soll
	 * @author Alexander Herzog
	 * @see ModelElementAnimationBarStack#getDirection()
	 * @see ModelElementAnimationBarStack#setDirection(FillDirection)
	 */
	public enum FillDirection {
		/** Balken baut sich von unten nach oben auf */
		DIRECTION_UP,

		/** Balken baut sich von links nach rechts auf */
		DIRECTION_RIGHT,

		/** Balken baut sich von oben nach unten auf */
		DIRECTION_DOWN,

		/** Balken baut sich von rechts nach links auf */
		DIRECTION_LEFT
	}

	private FillDirection direction=FillDirection.DIRECTION_UP;

	private final List<String> expressions=new ArrayList<>();

	private Semaphore drawLock=new Semaphore(1);
	private double[] simValues=null;

	private double maxValue=0.0;

	private int borderWidth=1;
	private Color borderColor=Color.BLACK;
	private Color backgroundColor=null;
	private final List<Color> barColors=new ArrayList<>();

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationBarStack</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnimationBarStack(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50),Shapes.ShapeType.SHAPE_NONE);
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_BAR_STACK.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationBarStack.Tooltip");
	}

	/**
	 * Liefert die Richtung, in die sich der Balken aufbaut
	 * @return	Richtung des Balken
	 * @see FillDirection
	 */
	public FillDirection getDirection() {
		return direction;
	}

	/**
	 * Stellt die Richtung ein, in die sich der Balken aufbaut
	 * @param direction	Neue Richtung des Balken
	 * @see FillDirection
	 */
	public void setDirection(final FillDirection direction) {
		this.direction=direction;
		fireChanged();
	}

	/**
	 * Liefert im die aktuellen Ausdr�cke.
	 * @return	Aktuelle Ausdr�cke
	 */
	public List<String> getExpressions() {
		return expressions;
	}

	/**
	 * Liefert den Maximalwert f�r die Balkendarstellung
	 * @return	Maximalwert (&le;0 f�r automatisch)
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Stellt den Maximalwert f�r die Balkendarstellung ein
	 * @param maxValue	Neuer Maximalwert (&le;0 f�r automatisch)
	 */
	public void setMaxValue(final double maxValue) {
		this.maxValue=maxValue;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Breite der Linie
	 * @return	Aktuelle breite der Linie
	 */
	public int getBorderWidth() {
		return borderWidth;
	}

	/**
	 * Stellt die breite der Linie ein
	 * @param borderWidth	Neue Breite f�r die Linie
	 */
	public void setBorderWidth(final int borderWidth) {
		if (borderWidth>=0 && borderWidth<=50) this.borderWidth=borderWidth;
	}

	/**
	 * Liefert die aktuelle Farbe der Linie
	 * @return	Aktuelle Farbe der Linie
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * Stellt die Farbe der Linie ein
	 * @param color	Farbe der Linie
	 */
	public void setBorderColor(final Color color) {
		if (color!=null) this.borderColor=color;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle F�llfarbe des Kastens
	 * @return	Aktuelle F�llfarbe des Kastens (kann <code>null</code> sein f�r transparent)
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Stellt die F�llfarbe des Kastens ein
	 * @param color	F�llfarbe des Kastens (oder <code>null</code> f�r transparent)
	 */
	public void setBackgroundColor(final Color color) {
		this.backgroundColor=color;
		fireChanged();
	}

	/**
	 * Liefert die aktuellen Farbe der Balken
	 * @return	Aktuelle Farben der Balken
	 */
	public List<Color> getBarColors() {
		return barColors;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationBarStack)) return false;

		if (direction!=((ModelElementAnimationBarStack)element).direction) return false;
		if (expressions.size()!=((ModelElementAnimationBarStack)element).expressions.size()) return false;
		for (int i=0;i<expressions.size();i++) {
			final String s1=expressions.get(i);
			final String s2=((ModelElementAnimationBarStack)element).expressions.get(i);
			if (s1==null || s2==null || !s1.equals(s2)) return false;
		}
		if (maxValue>0) {
			if (maxValue!=((ModelElementAnimationBarStack)element).maxValue) return false;
		} else {
			if (((ModelElementAnimationBarStack)element).maxValue>0) return false;
		}
		if (borderWidth!=((ModelElementAnimationBarStack)element).borderWidth) return false;

		if (!((ModelElementAnimationBarStack)element).borderColor.equals(borderColor)) return false;
		if (!(((ModelElementAnimationBarStack)element).backgroundColor==null && backgroundColor==null)) {
			if (((ModelElementAnimationBarStack)element).backgroundColor==null || backgroundColor==null) return false;
			if (!((ModelElementAnimationBarStack)element).backgroundColor.equals(backgroundColor)) return false;
		}

		if (barColors.size()!=((ModelElementAnimationBarStack)element).barColors.size()) return false;
		for (int i=0;i<barColors.size();i++) {
			final Color c1=barColors.get(i);
			final Color c2=((ModelElementAnimationBarStack)element).barColors.get(i);
			if (c1==null || c2==null || !c1.equals(c2)) return false;
		}

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationBarStack) {
			direction=((ModelElementAnimationBarStack)element).direction;
			expressions.clear();
			expressions.addAll(((ModelElementAnimationBarStack)element).expressions);
			maxValue=((ModelElementAnimationBarStack)element).maxValue;
			borderWidth=((ModelElementAnimationBarStack)element).borderWidth;

			borderColor=((ModelElementAnimationBarStack)element).borderColor;
			backgroundColor=((ModelElementAnimationBarStack)element).backgroundColor;
			barColors.clear();
			barColors.addAll(((ModelElementAnimationBarStack)element).barColors);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationBarStack clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationBarStack element=new ModelElementAnimationBarStack(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Pr�ft, ob sich ein Punkt im Bereich einer Linie P-Q befindet
	 * @param p	Startpunkt der Linie
	 * @param q	Endpunkt der Linie
	 * @param point	Punkt, bei dem gepr�ft werden soll, ob dieser nahe der Linie P-Q ist
	 * @param zoom	Zoomfaktor gem�� dessen der Punkt skaliert wird
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Punkt nahe der Linie befindet
	 */
	protected final boolean isNearLine(final Point p, final Point q, final Point point, final double zoom) {
		final Point v=new Point(q.x-p.x,q.y-p.y); /* Verbindungsvektor P->Q */
		final Point x=new Point((int)FastMath.round(point.x/zoom),(int)FastMath.round(point.y/zoom));

		double alpha=0;
		if (v.y!=0) {
			alpha=((double)v.y)/(v.x*v.x+v.y*v.y)*(x.y-((double)v.x)/v.y*(p.x-x.x)-p.y);
		} else {
			alpha=((double)v.x)/(v.y*v.y+v.x*v.x)*(x.x-((double)v.y)/v.x*(p.y-x.y)-p.x);
		}
		final Point y=new Point((int)FastMath.round(p.x+alpha*v.x),(int)FastMath.round(p.y+alpha*v.y)); /* Lotfu�punkt von X auf P->Q */

		if (FastMath.abs(x.x-y.x)>MAX_POINT_DELTA || FastMath.abs(x.y-y.y)>MAX_POINT_DELTA) return false; /* Abstand von Gerade zu gro�? */
		final double len=FastMath.sqrt(v.x*v.x+v.y*v.y);

		if (alpha*len<-MAX_POINT_DELTA || alpha*len>len+MAX_POINT_DELTA) return false; /* Fu�punkt vor Beginn oder nach Ende der Strecke P->Q? */

		return true;
	}

	/**
	 * Pr�ft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem gepr�ft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		if (super.containsPoint(point,zoom)) return true;

		final Point p=getPosition(true);
		final Dimension s=getSize();

		if (isNearLine(new Point(p.x,p.y),new Point(p.x+s.width,p.y),point,zoom)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y),new Point(p.x+s.width,p.y+s.height),point,zoom)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y+s.height),new Point(p.x,p.y+s.height),point,zoom)) return true;
		if (isNearLine(new Point(p.x,p.y+s.height),new Point(p.x,p.y),point,zoom)) return true;

		return false;
	}

	/**
	 * Liefert die Position eines bestimmten Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @return	Position des Randpunktes oder <code>null</code>, wenn der Index au�erhalb des g�ltigen Bereichs liegt
	 */
	@Override
	public Point getBorderPointPosition(final int index) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		switch (index) {
		case 0: return new Point(p); /* Kopie, damit diese unabh�ngig von Ver�nderungen des Wertes ist */
		case 1: return new Point(p.x+s.width,p.y);
		case 2: return new Point(p.x+s.width,p.y+s.height);
		case 3: return new Point(p.x,p.y+s.height);
		default: return null;
		}
	}

	/**
	 * Setzt die Position eines Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @param point	Neue Position des Randpunktes
	 */
	@Override
	public void setBorderPointPosition(final int index, final Point point) {
		final Point p1=getPosition(true);
		final Point p2=getLowerRightPosition();

		switch (index) {
		case 0:
			setPosition(point);
			setSize(new Dimension(p2.x-point.x,p2.y-point.y));
			break;
		case 1:
			setPosition(new Point(p1.x,point.y));
			setSize(new Dimension(point.x-p1.x,p2.y-point.y));
			break;
		case 2:
			setSize(new Dimension(point.x-p1.x,point.y-p1.y));
			break;
		case 3:
			setPosition(new Point(point.x,p1.y));
			setSize(new Dimension(p2.x-point.x,point.y-p1.y));
			break;
		}
	}

	/**
	 * Zeichnet eine Markierungs-Box an einer Ecke des Elements
	 * @param graphics	Grafik-Objekt, in das gezeichnet werden soll
	 * @param point	Position des Punktes
	 * @param zoom	Zoomfaktor
	 */
	private final void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
	}

	private void fillBox(final Graphics2D g, final Rectangle rectangle) {
		double[] d=null;
		double maxVal=4;
		drawLock.acquireUninterruptibly();
		try {
			if (simValues==null || simValues.length==0) {
				d=new double[]{1,1,1};
				maxVal=4;
			} else {
				d=Arrays.copyOf(simValues,simValues.length);
				maxVal=maxValue;
			}
		} finally {
			drawLock.release();
		}

		int maxPixel=1;
		switch (direction) {
		case DIRECTION_UP:
			maxPixel=rectangle.height;
			break;
		case DIRECTION_RIGHT:
			maxPixel=rectangle.width;
			break;
		case DIRECTION_DOWN:
			maxPixel=rectangle.height;
			break;
		case DIRECTION_LEFT:
			maxPixel=rectangle.width;
			break;
		}

		if (maxVal<=0) {
			maxVal=0;
			if (d!=null) for (int i=0;i<d.length;i++) maxVal+=d[i];
		}

		double sum=0;
		if (d!=null) for (int i=0;i<d.length;i++) {
			double value=d[i];

			int delta1=(int)FastMath.round(maxPixel*sum/maxVal);
			int delta2=(int)FastMath.round(maxPixel*value/maxVal);
			delta1=FastMath.max(0,delta1);
			delta1=FastMath.min(maxPixel,delta1);
			delta2=FastMath.max(0,delta2);
			delta2=FastMath.min(maxPixel-delta1,delta2);

			if (barColors==null || barColors.size()<=i) {
				switch (i%4) {
				case 0: g.setColor(Color.RED); break;
				case 1: g.setColor(Color.GREEN); break;
				case 2: g.setColor(Color.BLUE); break;
				case 3: g.setColor(Color.ORANGE); break;
				}
			} else {
				g.setColor(barColors.get(i)); /* keine Farbverl�ufe hier */
			}

			switch (direction) {
			case DIRECTION_UP:
				g.fillRect(rectangle.x,rectangle.y+rectangle.height-delta1-delta2,rectangle.width,delta2);
				break;
			case DIRECTION_RIGHT:
				g.fillRect(rectangle.x+delta1,rectangle.y,delta2,rectangle.height);
				break;
			case DIRECTION_DOWN:
				g.fillRect(rectangle.x,rectangle.y+delta1,rectangle.width,delta2);
				break;
			case DIRECTION_LEFT:
				g.fillRect(rectangle.x+rectangle.width-delta1-delta2,rectangle.y,delta2,rectangle.height);
				break;
			}

			sum+=value;
		}
	}

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tats�chlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgew�hlt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		setClip(graphics,drawRect,null);

		final Point p=getPosition(true);
		final Dimension s=getSize();

		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		boolean drawBorder=false;
		Color lineColor=borderColor;
		if (borderWidth>0) {
			g2.setStroke(new BasicStroke(borderWidth));
			drawBorder=true;
		}

		if (isSelected() && showSelectionFrames) {
			lineColor=Color.GREEN;
			g2.setStroke(new BasicStroke(FastMath.max(borderWidth,3)));
			drawBorder=true;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				lineColor=Color.BLUE;
				g2.setStroke(new BasicStroke(FastMath.max(borderWidth,3)));
				drawBorder=true;
			}
		}

		final Rectangle rectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(FastMath.abs(s.width)*zoom),(int)FastMath.round(FastMath.abs(s.height)*zoom));
		if (backgroundColor!=null) {
			g2.setColor(backgroundColor);
			g2.fill(rectangle);
		}

		fillBox(g2,rectangle);

		if (drawBorder) {
			g2.setColor(lineColor);
			g2.draw(rectangle);
		}

		if (isSelected() && showSelectionFrames) {
			drawBorderBox(g2,p,zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y),zoom);
			drawBorderBox(g2,new Point(p.x,p.y+s.height),zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y+s.height),zoom);
		}

		setClip(graphics,drawRect,null);
		g2.setStroke(saveStroke);
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationBarStack.Name");
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
			new ModelElementAnimationBarStackDialog(owner,ModelElementAnimationBarStack.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationBarStack.XML.Root");
	}

	private String getDirectionString(final FillDirection direction) {
		switch (direction) {
		case DIRECTION_DOWN: return Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction.Down");
		case DIRECTION_LEFT: return Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction.Left");
		case DIRECTION_RIGHT: return Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction.Right");
		case DIRECTION_UP: return Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction.Up");
		default: return "";
		}
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

		for (String expression: expressions) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.DataExpression"));
			node.appendChild(sub);
			sub.setTextContent(expression);
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.DataArea"));
		node.appendChild(sub);
		if (maxValue>0) {
			sub.setAttribute(Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Max"),NumberTools.formatSystemNumber(maxValue));
		}
		sub.setAttribute(Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction"),getDirectionString(direction));

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.LineWidth"));
		node.appendChild(sub);
		sub.setTextContent(""+borderWidth);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.LineColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(borderColor));

		if (backgroundColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(backgroundColor));
		}

		for (Color barColor: barColors) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationBarStack.XML.BarColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(barColor));
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

		if (Language.trAll("Surface.AnimationBarStack.XML.DataExpression",name)) {
			expressions.add(content);
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.DataArea",name)) {
			final String maxAttr=Language.trAllAttribute("Surface.AnimationBarStack.XML.DataArea.Max",node).trim();
			if (!maxAttr.isEmpty()) {
				final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(maxAttr));
				if (D==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Max"),name,node.getParentNode().getNodeName());
				maxValue=D;
			}

			final String s=Language.trAllAttribute("Surface.AnimationBarStack.XML.DataArea.Direction",node);
			if (!s.isEmpty()) {
				boolean ok=false;
				if (!ok && Language.trAll("Surface.AnimationBarStack.XML.DataArea.Direction.Up",s)) {direction=FillDirection.DIRECTION_UP; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBarStack.XML.DataArea.Direction.Right",s)) {direction=FillDirection.DIRECTION_RIGHT; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBarStack.XML.DataArea.Direction.Down",s)) {direction=FillDirection.DIRECTION_DOWN; ok=true;}
				if (!ok && Language.trAll("Surface.AnimationBarStack.XML.DataArea.Direction.Left",s)) {direction=FillDirection.DIRECTION_LEFT; ok=true;}
				if (!ok) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationBarStack.XML.DataArea.Direction"),name,node.getParentNode().getNodeName());
			}

			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			borderWidth=I;
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.LineColor",name) && !content.trim().isEmpty()) {
			borderColor=EditModel.loadColor(content);
			if (borderColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			backgroundColor=EditModel.loadColor(content);
			if (backgroundColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationBarStack.XML.BarColor",name) && !content.trim().isEmpty()) {
			final Color color=EditModel.loadColor(content);
			if (color==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			barColors.add(color);
			return null;
		}

		return null;
	}

	private ExpressionCalc[] animationExpression;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		if (isPreview) return false;
		drawLock.acquireUninterruptibly();
		try {
			if (animationExpression==null) return false;
			for (int i=0;i<animationExpression.length;i++) {
				simValues[i]=animationExpression[i].calcOrDefault(simData.runData.variableValues,simData,null,0);
			}
		} finally {
			drawLock.release();
		}
		return true;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		animationExpression=new ExpressionCalc[expressions.size()];
		simValues=new double[expressions.size()];
		for (int i=0;i<expressions.size();i++) {
			animationExpression[i]=new ExpressionCalc(simData.runModel.variableNames);
			if (animationExpression[i].parse(expressions.get(i))>=0) animationExpression[i]=null;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationBarStack";
	}

	private String getHTMLAnimationBarStack(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationBarStack(rect,barRects,barColors,borderColor,borderWidth,fillColor) {\n");

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

		sb.append("  for (var i=0;i<Math.min(barRects.length,barColors.length);i++) {\n");
		sb.append("    context.fillStyle=barColors[i];\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(barRects[i].x,barRects[i].y);\n");
		sb.append("    context.lineTo(barRects[i].x+barRects[i].w,barRects[i].y);\n");
		sb.append("    context.lineTo(barRects[i].x+barRects[i].w,barRects[i].y+barRects[i].h);\n");
		sb.append("    context.lineTo(barRects[i].x,barRects[i].y+barRects[i].h);\n");
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

		sb.append("}\n");

		return sb.toString();
	}

	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationBarStack",builder->getHTMLAnimationBarStack(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";

		final StringBuilder barRects=new StringBuilder();
		int z;
		switch (direction) {
		case DIRECTION_DOWN:
			z=d.height/4;
			barRects.append("{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+2*z)+", w: "+d.width+", h: "+z+"}");
			break;
		case DIRECTION_LEFT:
			z=d.width/4;
			barRects.append("{x: "+p.x+", y: "+p.y+", w: "+z+", h: "+d.height+"},");
			barRects.append("{x: "+(p.x+z)+", y: "+p.y+", w: "+z+", h: "+d.height+"},");
			barRects.append("{x: "+(p.x+2*z)+", y: "+p.y+", w: "+z+", h: "+d.height+"}");
			break;
		case DIRECTION_RIGHT:
			z=d.width/4;
			barRects.append("{x: "+p.x+", y: "+(p.y+d.width-z)+", w: "+z+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.width-2*z)+", w: "+z+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.width-3*z)+", w: "+z+", h: "+z+"}");
			break;
		case DIRECTION_UP:
			z=d.height/4;
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-2*z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-3*z)+", w: "+d.width+", h: "+z+"}");
			break;
		default:
			z=d.height/4;
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-2*z)+", w: "+d.width+", h: "+z+"},");
			barRects.append("{x: "+p.x+", y: "+(p.y+d.height-3*z)+", w: "+d.width+", h: "+z+"}");
			break;
		}

		final StringBuilder barColors=new StringBuilder();
		barColors.append("\""+HTMLOutputBuilder.colorToHTML(Color.RED)+"\",");
		barColors.append("\""+HTMLOutputBuilder.colorToHTML(Color.GREEN)+"\",");
		barColors.append("\""+HTMLOutputBuilder.colorToHTML(Color.BLUE)+"\"");

		if (backgroundColor==null) {
			outputBuilder.outputBody.append("drawAnimationBarStack("+rect+",["+barRects.toString()+"],["+barColors.toString()+"],"+border+","+borderWidth+");\n");
		} else {
			final String fill="\""+HTMLOutputBuilder.colorToHTML(backgroundColor)+"\"";
			outputBuilder.outputBody.append("drawAnimationBarStack("+rect+",["+barRects.toString()+"],["+barColors.toString()+"],"+border+","+borderWidth+","+fill+");\n");
		}
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialOutputHTML((HTMLOutputBuilder)outputBuilder);
	}
}