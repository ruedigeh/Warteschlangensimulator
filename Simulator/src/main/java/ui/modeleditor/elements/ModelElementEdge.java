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

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelElementCatalog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Verbindungskante zwischen zwei Modell-Elementen
 * @author Alexander Herzog
 */
public final class ModelElementEdge extends ModelElement {
	/* Element im Katalog registrieren */
	static {ModelElementCatalog.getCatalog().addElement(new ModelElementEdge(null,null,null,null));}

	private final static int ARROW_SIZE=10;
	private final static int ARC_RADIUS=10;

	/**
	 * Art der Verkn�pfungslinie
	 * @author Alexander Herzog
	 * @see ModelElementEdge#getLineMode()
	 * @see ModelElementEdge#setLineMode(LineMode)
	 */
	public enum LineMode {
		/** Gerade Verbindung von Start zu Ziel */
		DIRECT,
		/** Abgewinkelte Linie */
		MULTI_LINE,
		/** Abgewinkelte Linie mit abgerundeten Ecken */
		MULTI_LINE_ROUNDED
	}

	/**
	 * Art der Verkn�pfungslinien
	 * @see EditModel#edgeLineMode
	 */
	private LineMode lineMode=null;

	private ModelElement connectionStart;
	private ModelElement connectionEnd;

	/* Wird nur beim Laden und Clonen verwendet. */
	private int connectionStartId=-1;
	private int connectionEndId=-1;

	/**
	 * Konstruktor der Klasse <code>ModelElementEdge</code>
	 * @param model	Modell zu dem diese Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu der diese Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param connectionStart	Startpunkt der herzustellenden Verbindung (kann sp�ter nicht mehr ge�ndert werden)
	 * @param connectionEnd	Endpunkt der herzustellenden Verbindung (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementEdge(final EditModel model, final ModelSurface surface, final ModelElement connectionStart, final ModelElement connectionEnd) {
		super(model,surface);
		this.connectionStart=connectionStart;
		this.connectionEnd=connectionEnd;
	}

	/**
	 * Gibt f�r die Klasse an, ob der Name bei Vergleichen mit einbezogen werden soll.<br>
	 * (F�r normale Elemente sollte hier <code>true</code> zur�ckgegeben werden. Nur f�r Kanten usw. ist <code>false</code> sinnvoll.)
	 * @return	Gibt an, ob der Name des Elements bei Vergleichen mit einbezogen werden soll.
	 */
	@Override
	protected boolean getEqualsIncludesName() {
		return false;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementEdge)) return false;
		final ModelElementEdge otherEdge=(ModelElementEdge)element;

		if (connectionStart==null || otherEdge.connectionStart==null) return false;
		if (connectionEnd==null || otherEdge.connectionEnd==null) return false;
		if (connectionStart.getId()!=otherEdge.connectionStart.getId()) return false;
		if (connectionEnd.getId()!=otherEdge.connectionEnd.getId()) return false;
		if (lineMode!=otherEdge.lineMode) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementEdge) {
			if (((ModelElementEdge)element).connectionStart!=null) connectionStartId=((ModelElementEdge)element).connectionStart.getId();
			if (((ModelElementEdge)element).connectionEnd!=null) connectionEndId=((ModelElementEdge)element).connectionEnd.getId();
			lineMode=((ModelElementEdge)element).lineMode;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElement clone(final EditModel model, final ModelSurface surface) {
		ModelElement element=new ModelElementEdge(model,surface,null,null);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	@Override
	public void initAfterLoadOrClone() {
		super.initAfterLoadOrClone();

		if (connectionStartId>=0) {
			connectionStart=surface.getById(connectionStartId);
			connectionStartId=-1;
		}
		if (connectionEndId>=0) {
			connectionEnd=surface.getById(connectionEndId);
			connectionEndId=-1;
		}
		if ((connectionStart==null || connectionEnd==null) && surface!=null) surface.remove(this);
	}

	/**
	 * Liefert die Art der Verkn�pfungslinie.
	 * @return	Art der Verkn�pfungslinie (<code>null</code> bedeutet "Vorgabe Stil aus Modell verwenden)
	 * @see EditModel#edgeLineMode
	 * @see ModelElementEdge.LineMode
	 */
	public LineMode getLineMode() {
		return lineMode;
	}

	/**
	 * Stellt die Art der Verkn�pfungslinie ein
	 * @param lineMode	Art der Verkn�pfungslinie (<code>null</code> bedeutet "Vorgabe Stil aus Modell verwenden)
	 * @see EditModel#edgeLineMode
	 * @see ModelElementEdge.LineMode
	 */
	public void setLineMode(final LineMode lineMode) {
		this.lineMode=lineMode;
	}

	@Override
	public boolean canCopy() {
		return false;
	}

	@Override
	public boolean canArrange() {
		return false;
	}

	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final boolean readOnly) {
		final JMenu menu=new JMenu(Language.tr("Surface.Connection.LineMode"));
		popupMenu.add(menu);

		JCheckBoxMenuItem item;

		menu.add(item=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.Global"),Images.MODEL.getIcon(),lineMode==null));
		item.addActionListener(e->{lineMode=null; fireChanged();});
		menu.add(item=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.Direct"),Images.EDGE_MODE_DIRECT.getIcon(),lineMode==LineMode.DIRECT));
		item.addActionListener(e->{lineMode=LineMode.DIRECT; fireChanged();});
		menu.add(item=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.MultiLine"),Images.EDGE_MODE_MULTI_LINE.getIcon(),lineMode==LineMode.MULTI_LINE));
		item.addActionListener(e->{lineMode=LineMode.MULTI_LINE; fireChanged();});
		menu.add(item=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.MultiLineRounded"),Images.EDGE_MODE_MULTI_LINE_ROUNDED.getIcon(),lineMode==LineMode.MULTI_LINE_ROUNDED));
		item.addActionListener(e->{lineMode=LineMode.MULTI_LINE_ROUNDED; fireChanged();});
	}

	/**
	 * Gibt ein Icon an, welches neben dem Beschriftungslabel im Kontextmen� angezeigt werden soll.
	 * @return	Icon zur Beschriftung des Elements im Kontextmen� oder <code>null</code>, wenn kein Icon angezeigt werden soll.
	 */
	@Override
	public Icon buildIcon() {
		switch (getDrawLineMode(null)) {
		case DIRECT: return Images.EDGE_MODE_DIRECT.getIcon();
		case MULTI_LINE: return Images.EDGE_MODE_MULTI_LINE.getIcon();
		case MULTI_LINE_ROUNDED: return Images.EDGE_MODE_MULTI_LINE_ROUNDED.getIcon();
		default: return null;
		}
	}

	private enum Side {TOP, LEFT, BOTTOM, RIGHT}

	private static class Connect {
		public final int x;
		public final int y;
		public final Point p;
		public final Side side;

		public Connect(final int x, final int y, final Side side) {
			this.x=x;
			this.y=y;
			p=new Point(x,y);
			this.side=side;
		}
	}

	private Point lastLineP1;
	private Point lastLineP2;
	private double lastZoom;
	private Connect[] lastLine;

	private Connect[] getLine(final double zoom) {
		if (connectionStart==null || connectionEnd==null) return null;

		final Point p1=connectionStart.getMiddlePosition(true);
		final Point p2=connectionEnd.getMiddlePosition(true);
		if (p1==null || p2==null) return null;

		if (lastLine==null || lastLineP1==null || lastLineP2==null || lastLineP1.x!=p1.x || lastLineP1.y!=p1.y || lastLineP2.x!=p2.x || lastLineP2.y!=p2.y || lastZoom!=zoom) {
			if (lastLineP1==null) lastLineP1=new Point();
			lastLineP1.x=p1.x;
			lastLineP1.y=p1.y;
			if (lastLineP2==null) lastLineP2=new Point();
			lastLineP2.x=p2.x;
			lastLineP2.y=p2.y;
			lastZoom=zoom;

			final Point point1=connectionStart.getConnectionToPosition(p2);
			final Point point2=connectionEnd.getConnectionToPosition(p1);
			if (point1==null || point2==null) return null;

			final Side side1;
			if (p1.y>point1.y) side1=Side.TOP; else {
				if (p1.y<point1.y) side1=Side.BOTTOM; else {
					if (p1.x>point1.x) side1=Side.LEFT; else side1=Side.RIGHT;
				}
			}
			final Side side2;
			if (p2.y>point2.y) side2=Side.TOP; else {
				if (p2.y<point2.y) side2=Side.BOTTOM; else {
					if (p2.x>point2.x) side2=Side.LEFT; else side2=Side.RIGHT;
				}
			}

			lastLine=new Connect[]{
					new Connect((int)FastMath.round(point1.x*zoom),(int)FastMath.round(point1.y*zoom),side1),
					new Connect((int)FastMath.round(point2.x*zoom),(int)FastMath.round(point2.y*zoom),side2)
			};
		}

		return lastLine;
	}

	private LineMode getDrawLineMode(final Connect[] points) {
		LineMode drawLineMode=lineMode;
		final EditModel model=getModel();
		if (drawLineMode==null && model!=null) drawLineMode=model.edgeLineMode;
		if (drawLineMode==null) drawLineMode=LineMode.DIRECT;

		if (drawLineMode==LineMode.DIRECT) return drawLineMode;
		if (points!=null) {
			if (points[0].x==points[1].x || points[0].y==points[1].y) return LineMode.DIRECT;
			if ((points[0].side==Side.TOP || points[0].side==Side.BOTTOM) && (points[1].side!=Side.TOP && points[1].side!=Side.BOTTOM)) return LineMode.DIRECT;
			if ((points[0].side==Side.LEFT || points[0].side==Side.RIGHT) && (points[1].side!=Side.LEFT && points[1].side!=Side.RIGHT)) return LineMode.DIRECT;
		}
		return drawLineMode;
	}

	private Point arrow1=new Point();
	private Point arrow2=new Point();
	private Point middle=new Point();

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tats�chlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgew�hlt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		/* Zeichenfl�che vorbereiten */
		graphics.setClip(drawRect.x,drawRect.y,drawRect.width,drawRect.height);

		/* Punkte berechnen */
		final Connect[] points=getLine(zoom);
		if (points==null) return;
		final Point p1=points[0].p;
		final Point p2=points[1].p;

		/* Mitte bestimmen */
		middle.x=(p1.x+p2.x)/2;
		middle.y=(p1.y+p2.y)/2;

		/* Linienstil */
		final ComplexLine painter=(isSelected() && showSelectionFrames)?(getModel().edgePainterSelected):(getModel().edgePainterNormal);

		/* Linie(n) zeichnen */
		switch (getDrawLineMode(points)) {
		case DIRECT:
			drawArrow(graphics,painter,p1,p2,zoom);
			break;
		case MULTI_LINE:
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				/* vertikal, horizontal, vertikal */
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				drawLine(graphics,painter,p1,m1,zoom);
				drawLine(graphics,painter,m1,m2,zoom);
				drawArrow(graphics,painter,m2,p2,zoom);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				drawLine(graphics,painter,p1,m1,zoom);
				drawLine(graphics,painter,m1,m2,zoom);
				drawArrow(graphics,painter,m2,p2,zoom);
			}
			break;
		case MULTI_LINE_ROUNDED:
			final int radius=(int)FastMath.round(ARC_RADIUS*zoom);
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				/* vertikal, horizontal, vertikal */
				final Point m1a;
				final Point m1b;
				if (Math.abs(p1.y-m1.y)<=radius || Math.abs(m1.x-m2.x)<=2*radius) {
					m1a=m1;
					m1b=m1;
				} else {
					m1a=new Point(p1.x,m1.y+radius*((m1.y>p1.y)?-1:1));
					m1b=new Point(m1.x+radius*((m2.x>m1.x)?1:-1),m1.y);
					if (m1b.x<m1a.x ^ m1b.y<m1a.y) drawArcClockWise(graphics,painter,m1a,m1b,zoom); else drawArcClockWise(graphics,painter,m1b,m1a,zoom);
				}
				final Point m2a;
				final Point m2b;
				if (Math.abs(m1.x-m2.x)<=2*radius || Math.abs(p2.y-m2.y)<=radius) {
					m2a=m2;
					m2b=m2;
				} else {
					m2a=new Point(m2.x+radius*((m2.x>m1.x)?-1:1),m2.y);
					m2b=new Point(p2.x,m2.y+radius*((m2.y>p2.y)?-1:1));
					if (m2b.x>m2a.x ^ m2b.y<m2a.y) drawArcClockWise(graphics,painter,m2a,m2b,zoom); else drawArcClockWise(graphics,painter,m2b,m2a,zoom);
				}
				drawLine(graphics,painter,p1,m1a,zoom);
				drawLine(graphics,painter,m1b,m2a,zoom);
				drawArrow(graphics,painter,m2b,p2,zoom);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				final Point m1a;
				final Point m1b;
				if (Math.abs(p1.x-m1.x)<=radius || Math.abs(m1.y-m2.y)<=2*radius) {
					m1a=m1;
					m1b=m1;
				} else {
					m1a=new Point(m1.x+radius*((m1.x>p1.x)?-1:1),p1.y);
					m1b=new Point(m1.x,m1.y+radius*((m2.y>m1.y)?1:-1));
					if (m1b.y>m1a.y ^ m1b.x<m1a.x) drawArcClockWise(graphics,painter,m1a,m1b,zoom); else drawArcClockWise(graphics,painter,m1b,m1a,zoom);
				}
				final Point m2a;
				final Point m2b;
				if (Math.abs(m1.y-m2.y)<=2*radius || Math.abs(p2.x-m2.x)<=radius) {
					m2a=m2;
					m2b=m2;
				} else {
					m2a=new Point(m2.x,m2.y+radius*((m2.y>m1.y)?-1:1));
					m2b=new Point(m2.x+radius*((m2.x>p2.x)?-1:1),p2.y);
					if (m2b.y<m2a.y ^ m2b.x<m2a.x) drawArcClockWise(graphics,painter,m2a,m2b,zoom); else drawArcClockWise(graphics,painter,m2b,m2a,zoom);
				}
				drawLine(graphics,painter,p1,m1a,zoom);
				drawLine(graphics,painter,m1b,m2a,zoom);
				drawArrow(graphics,painter,m2b,p2,zoom);
			}
			break;
		}

		/* Text ausgeben */
		drawText(graphics,middle,zoom);
	}

	private void drawLine(final Graphics graphics, final ComplexLine painter, final Point point1, final Point point2, final double zoom) {
		painter.draw(graphics,point1,point2,zoom);
	}

	private void drawArrow(final Graphics graphics, final ComplexLine painter, final Point point1, final Point point2, final double zoom) {
		/* Pfeile berechnen */
		double v0=point2.x-point1.x, v1=point2.y-point1.y;
		final double length=Math.sqrt(v0*v0+v1*v1);
		v0=v0/length; v1=v1/length;
		final double w0=v1, w1=-v0;
		arrow1.x=(int)FastMath.round(point2.x-ARROW_SIZE*zoom*v0+ARROW_SIZE*zoom*w0);
		arrow1.y=(int)FastMath.round(point2.y-ARROW_SIZE*zoom*v1+ARROW_SIZE*zoom*w1);
		arrow2.x=(int)FastMath.round(point2.x-ARROW_SIZE*zoom*v0-ARROW_SIZE*zoom*w0);
		arrow2.y=(int)FastMath.round(point2.y-ARROW_SIZE*zoom*v1-ARROW_SIZE*zoom*w1);

		/* Linien zeichnen */
		painter.draw(graphics,point1,point2,zoom);
		painter.draw(graphics,point2,arrow1,zoom);
		painter.draw(graphics,point2,arrow2,zoom);
	}

	private void drawArcClockWise(final Graphics graphics, final ComplexLine painter, final Point point1, final Point point2, final double zoom) {
		painter.drawArc(graphics,point1,point2,zoom);
	}

	private double lastZoomFont=-1;
	private Font lastFont;

	private void drawText(final Graphics graphics, final Point middle, final double zoom) {
		final String text=getName();
		if (text!=null && !text.isEmpty()) {
			if (zoom!=lastZoomFont || lastFont==null) {
				lastFont=new Font(Font.DIALOG,0,(int)Math.round(11*zoom));
				lastZoomFont=zoom;
			}
			graphics.setFont(lastFont);
			graphics.drawString(text,middle.x-graphics.getFontMetrics().stringWidth(text)/2,middle.y+graphics.getFontMetrics().getAscent());
		}
	}

	private boolean lineContainsPoint(final Point point, final Point p1, final Point p2, final double zoom) {
		/* Gerade: u1+t1*v1 */
		final double[] u1=new double[]{p1.x,p1.y};
		final double[] v1=new double[]{p2.x-p1.x,p2.y-p1.y};

		/* Gerade von Punkt senkrecht zu Ausgangsgerade: u2+t2*v2 */
		final double[] u2=new double[]{point.x,point.y};
		final double[] v2=new double[]{v1[1]-point.x,-v1[0]-point.y};

		/* Schnitt der beiden Geraden: u1+t1*v1=u2+t2*v2*/
		double t1;
		if (v2[0]!=0) {
			t1=(u1[1]-u2[1]-v2[1]*(u1[0]-u2[0])/v2[0])/(v1[0]*v2[1]/v2[0]-v1[1]);
		} else {
			t1=(u1[0]-u2[0]-v2[0]*(u1[1]-u2[1])/v2[1])/(v1[1]*v2[0]/v2[1]-v1[0]);
		}

		if (t1<0 || t1>1) return false; /* Lotfu�punkt au�erhalb der Strecke */
		final double[] p=new double[]{u1[0]+t1*v1[0],u1[1]+t1*v1[1]}; /* Lotfu�punkt */

		/*
		Debug-Informationen:
		System.out.println("P=("+points[0].x+";"+points[0].y+") Q=("+points[1].x+";"+points[1].y+")");
		System.out.println("u1=("+Math.round(u1[0])+";"+Math.round(u1[1])+") v1=("+Math.round(v1[0])+";"+Math.round(v1[1])+")");
		System.out.println("u2=("+Math.round(u2[0])+";"+Math.round(u2[1])+") v2=("+Math.round(v2[0])+";"+Math.round(v2[1])+")");
		System.out.println("p=("+Math.round(p[0])+";"+Math.round(p[1])+")");
		System.out.println(delta);
		 */

		final double deltaX=point.x-p[0];
		final double deltaY=point.y-p[1];
		double delta=Math.sqrt(deltaX*deltaX+deltaY*deltaY);
		if (t1<0.1 || t1>0.9) {
			return (delta<3*zoom);
		} else {
			return (delta<20*zoom);
		}
	}

	/**
	 * Liefert den Start- und den Endpunkt der Verbindungskante.
	 * @param zoom	Zoomfaktor
	 * @return	Im Erfolgsfall ein Array aus zwei Elementen; im Fehlerfall <code>null</code>.
	 */
	public Point[] getConnectionLine(final double zoom) {
		final Connect[] points=getLine(zoom);
		if (points==null) return null;
		return new Point[] {points[0].p,points[1].p};
	}

	/**
	 * Pr�ft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem gepr�ft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		/* Punkte berechnen */
		final Connect[] points=getLine(zoom);
		if (points==null) return false;
		final Point p1=points[0].p;
		final Point p2=points[1].p;

		/* Mitte bestimmen */
		middle.x=(p1.x+p2.x)/2;
		middle.y=(p1.y+p2.y)/2;

		switch (getDrawLineMode(points)) {
		case DIRECT:
			return lineContainsPoint(point,p1,p2,zoom);
		case MULTI_LINE:
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				/* vertikal, horizontal, vertikal */
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				return lineContainsPoint(point,p1,m1,zoom) || lineContainsPoint(point,m1,m2,zoom) || lineContainsPoint(point,m2,p2,zoom);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				return lineContainsPoint(point,p1,m1,zoom) || lineContainsPoint(point,m1,m2,zoom) || lineContainsPoint(point,m2,p2,zoom);
			}
		case MULTI_LINE_ROUNDED:
			final int radius=(int)FastMath.round(ARC_RADIUS*zoom);
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				/* vertikal, horizontal, vertikal */
				final Point m1a;
				final Point m1b;
				if (Math.abs(p1.y-m1.y)<=radius || Math.abs(m1.x-m2.x)<=2*radius) {
					m1a=m1;
					m1b=m1;
				} else {
					m1a=new Point(p1.x,m1.y+radius*((m1.y>p1.y)?-1:1));
					m1b=new Point(m1.x+radius*((m2.x>m1.x)?1:-1),m1.y);
					if (lineContainsPoint(point,m1a,m1b,zoom)) return true;
				}
				final Point m2a;
				final Point m2b;
				if (Math.abs(m1.x-m2.x)<=2*radius || Math.abs(p2.y-m2.y)<=radius) {
					m2a=m2;
					m2b=m2;
				} else {
					m2a=new Point(m2.x+radius*((m2.x>m1.x)?-1:1),m2.y);
					m2b=new Point(p2.x,m2.y+radius*((m2.y>p2.y)?-1:1));
					if (lineContainsPoint(point,m2a,m2b,zoom)) return true;
				}
				return lineContainsPoint(point,p1,m1a,zoom) || lineContainsPoint(point,m1b,m2a,zoom) || lineContainsPoint(point,m2b,p2,zoom);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				final Point m1a;
				final Point m1b;
				if (Math.abs(p1.x-m1.x)<=radius || Math.abs(m1.y-m2.y)<=2*radius) {
					m1a=m1;
					m1b=m1;
				} else {
					m1a=new Point(m1.x+radius*((m1.x>p1.x)?-1:1),p1.y);
					m1b=new Point(m1.x,m1.y+radius*((m2.y>m1.y)?1:-1));
					if (lineContainsPoint(point,m1a,m1b,zoom)) return true;
				}
				final Point m2a;
				final Point m2b;
				if (Math.abs(m1.y-m2.y)<=2*radius || Math.abs(p2.x-m2.x)<=radius) {
					m2a=m2;
					m2b=m2;
				} else {
					m2a=new Point(m2.x,m2.y+radius*((m2.y>m1.y)?-1:1));
					m2b=new Point(m2.x+radius*((m2.x>p2.x)?-1:1),p2.y);
					if (lineContainsPoint(point,m2a,m2b,zoom)) return true;
				}
				return lineContainsPoint(point,p1,m1a,zoom) || lineContainsPoint(point,m1b,m2a,zoom) || lineContainsPoint(point,m2b,p2,zoom);
			}
		default:
			return false;
		}
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Connection.Name");
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionStart!=null) connectionStart.removeConnectionNotify(this);
		if (connectionEnd!=null) connectionEnd.removeConnectionNotify(this);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (element==connectionStart || element==connectionEnd) surface.remove(this);
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Edge.XML.Root");
	}

	/**
	 * Erm�glicht die Abfrage der Liste der Namen ohne daf�r ein Objekt anlegen zu m�ssen.
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	public static String[] getXMLNodeNamesStatic() {
		return Language.trAll("Surface.Edge.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);
		if (connectionStart!=null || connectionEnd!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.Connection"));
			node.appendChild(sub);
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Edge")); // "Kante"
			if (connectionStart!=null) sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element1"),""+connectionStart.getId()); // "Element1"
			if (connectionEnd!=null) sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element2"),""+connectionEnd.getId()); // "Element2"
		}
		if (lineMode!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.LineMode"));
			node.appendChild(sub);
			switch (lineMode) {
			case DIRECT: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.Direct")); break;
			case MULTI_LINE: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.MultiLine")); break;
			case MULTI_LINE_ROUNDED: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.MultiLineRounded")); break;
			}
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

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			String s;
			s=Language.trAllAttribute("Surface.XML.Connection.Element1",node);
			if (!s.isEmpty()) {
				I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element1"),name,node.getParentNode().getNodeName());
				connectionStartId=I;
			}
			s=Language.trAllAttribute("Surface.XML.Connection.Element2",node);
			if (!s.isEmpty()) {
				I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element2"),name,node.getParentNode().getNodeName());
				connectionEndId=I;
			}
			return null;
		}

		if (Language.trAll("Surface.XML.LineMode",name)) {
			if (Language.trAll("Surface.XML.LineMode.Direct",content)) lineMode=ModelElementEdge.LineMode.DIRECT;
			if (Language.trAll("Surface.XML.LineMode.MultiLine",content)) lineMode=ModelElementEdge.LineMode.MULTI_LINE;
			if (Language.trAll("Surface.XML.LineMode.MultiLineRounded",content)) lineMode=ModelElementEdge.LineMode.MULTI_LINE_ROUNDED;
			return null;
		}

		return null;
	}

	/**
	 * Startpunkt der Verkn�pfung
	 * @return	Startpunkt der Verkn�pfung
	 */
	public ModelElement getConnectionStart() {
		return connectionStart;
	}

	/**
	 * Endpunkt der Verkn�pfung
	 * @return	Endpunkt der Verkn�pfung
	 */
	public ModelElement getConnectionEnd() {
		return connectionEnd;
	}

	private boolean edgeInList(final ModelElementEdge[] list) {
		for (ModelElementEdge edge: list) if (edge==this) return true;
		return false;
	}

	/**
	 * Pr�ft, ob die Elemente, die die Kante verkn�pft auch von der Kante wissen.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Kante korrekt verankert ist.
	 */
	public boolean isConnectionOk() {
		if (surface==null) return true;
		if (connectionStart==null || connectionEnd==null) return false;

		/* Startelement */

		if (connectionStart instanceof ModelElementEdgeOut) {
			final ModelElementEdgeOut edgeOutElement=(ModelElementEdgeOut)connectionStart;
			if (edgeOutElement.getEdgeOut()!=this) return false;
		}

		if (connectionStart instanceof ModelElementEdgeMultiOut) {
			final ModelElementEdgeMultiOut edgesOutElement=(ModelElementEdgeMultiOut)connectionStart;
			if (!edgeInList(edgesOutElement.getEdgesOut())) return false;
		}

		if (connectionStart instanceof ModelElementProcess) {
			final ModelElementProcess process=(ModelElementProcess)connectionStart;
			if (process.getEdgeOutSuccess()!=this && process.getEdgeOutCancel()!=this) return false;
		}

		/* Zielelement */

		if (connectionEnd instanceof ModelElementEdgeMultiIn) {
			final ModelElementEdgeMultiIn edgesInElement=(ModelElementEdgeMultiIn)connectionEnd;
			if (!edgeInList(edgesInElement.getEdgesIn())) return false;
		}

		return true;
	}

	private String getHTMLDrawEdge(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawEdge(p1,p2,text) {\n");

		sb.append("  context.strokeStyle=\"Black\";\n");
		sb.append("  context.lineWidth=1;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(p1.x,p1.y);\n");
		sb.append("  context.lineTo(p2.x,p2.y);\n");
		sb.append("  context.stroke();\n");

		sb.append("  var v={x: p2.x-p1.x,y: p2.y-p1.y};\n");
		sb.append("  var length=Math.sqrt(v.x*v.x+v.y*v.y);\n");
		sb.append("  v={x: v.x/length,y: v.y/length};\n");
		sb.append("  var w={x: v.y, y: -v.x};\n");
		sb.append("  var p1a=Math.round(p2.x-"+ARROW_SIZE+"*v.x+"+ARROW_SIZE+"*w.x);\n");
		sb.append("  var p2a=Math.round(p2.y-"+ARROW_SIZE+"*v.y+"+ARROW_SIZE+"*w.y);\n");
		sb.append("  var p1b=Math.round(p2.x-"+ARROW_SIZE+"*v.x-"+ARROW_SIZE+"*w.x);\n");
		sb.append("  var p2b=Math.round(p2.y-"+ARROW_SIZE+"*v.y-"+ARROW_SIZE+"*w.y);\n");

		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(p2.x,p2.y);\n");
		sb.append("  context.lineTo(p1a,p2a);\n");
		sb.append("  context.stroke();\n");

		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(p2.x,p2.y);\n");
		sb.append("  context.lineTo(p1b,p2b);\n");
		sb.append("  context.stroke();\n");

		sb.append("  if (typeof(text)!=\"undefined\") {\n");
		sb.append("    var middle={x: Math.round((p1.x+p2.x)/2), y: Math.round((p1.y+p2.y)/2)};\n");
		sb.append("    context.font=\"11px Verdana,Lucida,sans-serif\";\n");
		sb.append("    context.textAlign=\"center\";\n");
		sb.append("    context.textBaseline=\"bottom\";\n");
		sb.append("    context.fillStyle=\"Black\";\n");
		sb.append("    context.fillText(text,middle.x,middle.y);\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		final Connect[] points=getLine(1.0);
		if (points==null) return;

		outputBuilder.addJSUserFunction("drawEdge",builder->getHTMLDrawEdge(builder));

		final String text=getName();
		if (text!=null && !text.isEmpty()) {
			outputBuilder.outputBody.append("drawEdge({x: "+points[0].x+", y: "+points[0].y+"},{x: "+points[1].x+", y: "+points[1].y+"},\""+HTMLOutputBuilder.encodeHTML(text,true)+"\");\n");
		} else {
			outputBuilder.outputBody.append("drawEdge({x: "+points[0].x+", y: "+points[0].y+"},{x: "+points[1].x+", y: "+points[1].y+"});\n");
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

	private Point readOnlyMiddle=null;

	@Override
	public Point getMiddlePosition(final boolean readOnly) {
		if (connectionStart==null || connectionEnd==null) return null;
		final Point p1=connectionStart.getPosition(true);
		final Point p2=connectionEnd.getPosition(true);
		if (p1==null || p2==null) return null;

		final int x=(p1.x+p2.x)/2;
		final int y=(p1.y+p2.y)/2;
		if (readOnly) {
			if (readOnlyMiddle==null) return readOnlyMiddle=new Point(x,y);
			readOnlyMiddle.x=x;
			readOnlyMiddle.y=y;
			return readOnlyMiddle;
		} else {
			return new Point(x,y);
		}
	}

	@Override
	protected boolean canSetDeleteProtection() {
		return false;
	}
}