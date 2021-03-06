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
package ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsQuotientPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.BaseDialog;
import systemtools.ImageTools;
import systemtools.MsgBox;
import tools.ButtonRotator;
import tools.SetupData;
import tools.SlidesGenerator;
import ui.dialogs.BackgroundColorDialog;
import ui.dialogs.LayersDialog;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.DrawIOExport;
import ui.modeleditor.ModelElementCatalog;
import ui.modeleditor.ModelElementCatalogListCellRenderer;
import ui.modeleditor.ModelElementCatalogTransferHandler;
import ui.modeleditor.ModelElementNavigatorListCellRenderer;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementListGroup;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementAssign;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementDifferentialCounter;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementThroughput;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modelproperties.ModelPropertiesDialog;
import ui.parameterseries.ParameterCompareTemplatesDialog;
import ui.quickaccess.JPlaceholderTextField;
import ui.speedup.BackgroundSystem;

/**
 * Diese Klasse kapselt einen vollst�ndigen Editor f�r ein <code>EditModel</code>-Objekt
 * @see EditorPanelBase
 * @author Alexander Herzog
 */
public class EditorPanel extends EditorPanelBase {
	private static final long serialVersionUID = 871808238984135272L;

	private static final boolean useTopToolBar=false;

	private boolean guiReady=false; /* Brauchen wir, wenn der Code von ProGuard ver�ndert wird */

	private boolean allowChangeOperationsOnReadOnly=false;

	private String name;
	private String description;
	private String author;
	private long clientCount;
	private double warmUpTime;

	private final Component owner;

	private ToolbarListener toolbarListener;
	private JLabel labelZoom;
	private JButton buttonTemplates;
	private JButton buttonProperties;
	private JButton buttonAddEdge;
	private JButton buttonAddElement;
	private JButton buttonEdit;
	private JButton buttonDelete;
	private JButton buttonZoomOut, buttonZoomOut2;
	private JButton buttonZoomIn, buttonZoomIn2;
	private JButton buttonZoomDefault, buttonZoomDefault2;
	private JButton buttonFindModel, buttonFindModel2;
	private JToolBar.Separator separatorUndoRedo;
	private JButton buttonUndo;
	private JButton buttonRedo;
	private JButton buttonExplorer;

	private ModelSurfacePanel surfacePanel;
	private RulerPanel rulerPanel;

	private JToolBar additionalInfoArea;
	private JLabel additionalInfoLabel;
	private JLabel statusBar;
	private JPanel leftArea;
	private JList<ModelElementPosition> templates;
	private ModelElementCatalogListCellRenderer<ModelElementPosition> templatesRenderer;
	private JPanel leftAreaTemplates;
	private JPlaceholderTextField leftAreaQuickFilter;
	private JButton leftAreaTemplatesFilterButton;

	private JPanel rightArea;
	private JList<ModelElementBox> navigator;
	private DefaultListModel<ModelElementBox> navigatorModel;
	private ModelElementNavigatorListCellRenderer<ModelElementBox> navigatorRenderer;

	private final List<ActionListener> fileDropListeners;
	private final List<ActionListener> undoRedoDoneListener;

	private Supplier<Statistics> statisticsGetter;

	private Runnable callbackElementSearch;
	private Runnable callbackElementList;

	/**
	 * Konstruktor der Klasse <code>EditorPanel</code>
	 * @param owner	�bergeordnetes Element
	 * @param model	Anzuzeigendes Modell
	 * @param readOnly	Wird hier <code>true</code> angegeben, so kann das Modell nicht ver�ndert werden.
	 * @param showEditModelProperties	Gibt an, ob die Schaltfl�che zum Bearbeiten der Modelleigenschaften sichtbar sein soll
	 * @param canUndo	Gibt an, ob das Undo/Redo-System f�r dieses EditorPanel aktiv sein soll (bei Unterelementen sollte es deaktiviert werden)
	 */
	public EditorPanel(final Component owner, final EditModel model, final boolean readOnly, final boolean showEditModelProperties, final boolean canUndo) {
		super(model,readOnly);
		this.owner=(owner==null)?this:owner;
		buttonProperties.setVisible(showEditModelProperties);
		surfacePanel.setShowEditModelProperties(showEditModelProperties);
		fileDropListeners=new ArrayList<>();
		undoRedoDoneListener=new ArrayList<>();
		if (!canUndo) {
			surfacePanel.disableUndo();
			if (separatorUndoRedo!=null) separatorUndoRedo.setVisible(false);
			if (buttonUndo!=null) buttonUndo.setVisible(false);
			if (buttonRedo!=null) buttonRedo.setVisible(false);
		}

		guiReady=true;
		fireTemplatesVisibleChanged();
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Erstellt ein leeres {@link EditorPanel}.
	 * @param owner	�bergeordnetes Element
	 */
	public EditorPanel(final Component owner) {
		super();
		this.owner=(owner==null)?this:owner;
		fileDropListeners=new ArrayList<>();
		undoRedoDoneListener=new ArrayList<>();
		guiReady=true;
		fireTemplatesVisibleChanged();
	}

	@Override
	public void paint(Graphics g) {
		setupInfoLabels(false);
		super.paint(g);
	}

	private boolean infoLabelsDone=false;

	private void setupInfoLabels(final boolean turnOff) {
		if (surfacePanel==null) return;
		final SetupData.SurfaceHelp surfaceHelp=SetupData.getSetup().surfaceHelp;

		if (!infoLabelsDone && !turnOff && surfaceHelp!=SetupData.SurfaceHelp.NEVER && buttonProperties!=null && buttonTemplates!=null && buttonAddEdge!=null && !leftAreaTemplates.isVisible()) {
			surfacePanel.setInfoPositions(new int[]{
					buttonProperties.getY()+buttonProperties.getHeight()/2,
					buttonTemplates.getY()+buttonTemplates.getHeight()/2,
					buttonAddEdge.getY()+buttonAddEdge.getHeight()/2
			});
		} else {
			surfacePanel.setInfoPositions(null);
			if (turnOff && surfaceHelp==SetupData.SurfaceHelp.START_ONLY) infoLabelsDone=true;
		}
	}

	/**
	 * Gibt an, ob ein Raster auf der Zeichenfl�che angezeigt wird
	 * @return	Rasteranzeige
	 */
	public ModelSurface.Grid getRaster() {
		return surfacePanel.getRaster();
	}

	/**
	 * Stellt ein, ob ein Raster auf der Zeichenfl�che angezeigt wird
	 * @param raster	Rasteranzeige
	 */
	public void setRaster(final ModelSurface.Grid raster) {
		surfacePanel.setRaster(raster);
	}

	/**
	 * Verringert den Zoomfaktor.
	 */
	public void zoomOut() {
		surfacePanel.zoomOut();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Vergr��ert den Zoomfaktor.
	 */
	public void zoomIn() {
		surfacePanel.zoomIn();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Stellt den Standard-Zoomfaktor wieder her.
	 */
	public void zoomDefault() {
		surfacePanel.zoomDefault();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Liefert den aktuell eingestellten Zoomfaktor
	 * @return Aktueller Zoomfaktor
	 */
	public double getZoom() {
		return surfacePanel.getZoom();
	}

	/**
	 * Stellt den Zoomfaktor ein
	 * @param zoom	Neuer Zoomfaktor
	 */
	public void setZoom(final double zoom) {
		surfacePanel.setZoom(zoom);
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Zentriert das Modell auf der Zeichenfl�che
	 */
	public void centerModel() {
		surfacePanel.centerModel();
	}

	/**
	 * Scrollt ganz nach oben links.
	 */
	public void scrollToTop() {
		surfacePanel.scrollToTop();
	}

	/**
	 * Liefert die Position der linken oberen Ecke des sichtbaren Bereichs
	 * @return	Position der linken oberen Ecke des sichtbaren Bereichs
	 */
	public Point getPosition() {
		if (!(surfacePanel.getParent() instanceof JViewport)) return new Point(0,0);
		return ((JViewport)surfacePanel.getParent()).getViewPosition();
	}

	/**
	 * Gibt an, ob die Elemente-hinzuf�ge-Leiste angezeigt wird
	 * @return	Gibt <code>true</code> zur�ck, wenn die Elemente-hinzuf�ge-Leiste angezeigt wird
	 */
	public boolean isTemplatesVisible() {
		return leftAreaTemplates.isVisible();
	}

	/**
	 * Gibt an, ob die Navigator-Leiste angezeigt wird
	 * @return	Gibt <code>true</code> zur�ck, wenn die Navigator-Leiste angezeigt wird
	 */
	public boolean isNavigatorVisible() {
		return rightArea.isVisible();
	}

	/**
	 * Gibt an, ob die Funktion zum Hinzuf�gen von Kanten momentan aktiv ist
	 * @return	Gibt <code>true</code> zur�ck, wenn die Funktion zum Hinzuf�gen von Kanten momentan aktiv ist
	 */
	public boolean isAddEdgeActive() {
		return surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP1 || surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP2;
	}

	private final Set<Runnable> templatesVisibleChangeListeners=new HashSet<>();

	/**
	 * F�gt einen Listener zu der Liste der Listener, die beim �ndern des Sichtbarkeitsstatus der Vorlagenleiste benachrichtigt werden sollen, hin.
	 * @param listener	Zuk�nftig zus�tzlich zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener zu der Liste hinzugef�gt werden konnte. Wenn er bereits in der in der Liste enthalten war, liefert die Funktion <code>false</code>.
	 */
	public boolean addTemplatesVisibleChangeListener(final Runnable listener) {
		return templatesVisibleChangeListeners.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die beim �ndern des Sichtbarkeitsstatus der Vorlagenleiste benachrichtigt werden sollen.
	 * @param listener	Zuk�nftig nicht mehr zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener aus der Liste entfernt werden konnte. Wenn er nicht enthalten war, liefert die Funktion <code>false</code>.
	 */
	public boolean removeTemplatesVisibleChangeListener(final Runnable listener) {
		return templatesVisibleChangeListeners.remove(listener);
	}

	private void fireTemplatesVisibleChanged() {
		if (templatesVisibleChangeListeners==null || !guiReady) return;
		for (Runnable listener: templatesVisibleChangeListeners)
			if (listener!=null) listener.run();
	}

	/**
	 * Stellt ein, ob die Elemente-hinzuf�ge-Leiste angezeigt werden soll
	 * @param templatesVisible	Elemente-hinzuf�ge-Leiste anzeigen
	 */
	public void setTemplatesVisible(final boolean templatesVisible) {
		leftAreaTemplates.setVisible(templatesVisible);
		leftArea.setVisible(false);
		leftArea.setVisible(true);

		if (!readOnly) {
			final SetupData setup=SetupData.getSetup();
			setup.showTemplates=isTemplatesVisible();
			setup.saveSetup();
		}

		setupInfoLabels(templatesVisible);

		fireTemplatesVisibleChanged();
	}

	private boolean selectTemplateInListDirect(final ModelElementPosition element) {
		if (element==null) return true;
		final String className=element.getClass().getName();
		setTemplatesVisible(true);
		final ListModel<ModelElementPosition> model=templates.getModel();
		final int count=model.getSize();
		boolean groupOpen=true;
		for (int i=0;i<count;i++) {
			final ModelElementPosition e=model.getElementAt(i);
			if (e instanceof ModelElementListGroup) {
				groupOpen=((ModelElementListGroup)e).isShowSub();
				continue;
			}
			if (!groupOpen) continue;
			if (e.getClass().getName().equals(className)) {
				templates.setSelectedIndex(i);
				templates.ensureIndexIsVisible(i);
				return true;
			}
		}
		return false;
	}

	private void expandGroupForElement(final ModelElementPosition element) {
		ListModel<ModelElementPosition> model=templates.getModel();
		final int count=model.getSize();
		for (int i=0;i<count;i++) {
			final ModelElementPosition e=model.getElementAt(i);
			if (e instanceof ModelElementListGroup) {
				final ModelElementListGroup group=(ModelElementListGroup)e;
				if (group.isInGroup(element) && !group.isShowSub()) {
					group.toggleShowSub();
					saveGroupOpenStatus(group.getIndex(),true);
					break;
				}
			}
		}

		updateTemplatesFilter();
		model=templates.getModel();
		templates.setModel(new DefaultListModel<>());
		templates.setModel(model);
		setTemplatesVisible(isTemplatesVisible());
	}

	/**
	 * W�hlt ein Element in der Vorlagenleiste aus (und macht diese ggf. vorher sichtbar)
	 * @param element	Zu selektierendes Element
	 */
	public void selectTemplateInList(final ModelElementPosition element) {
		if (selectTemplateInListDirect(element)) return;
		expandGroupForElement(element);
		selectTemplateInListDirect(element);
	}

	private final Set<Runnable> navigatorVisibleChangeListeners=new HashSet<>();

	/**
	 * F�gt einen Listener zu der Liste der Listener, die beim �ndern des Sichtbarkeitsstatus des Navigators benachrichtigt werden sollen, hin.
	 * @param listener	Zuk�nftig zus�tzlich zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener zu der Liste hinzugef�gt werden konnte. Wenn er bereits in der in der Liste enthalten war, liefert die Funktion <code>false</code>.
	 */
	public boolean addNavigatorVisibleChangeListener(final Runnable listener) {
		return navigatorVisibleChangeListeners.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die beim �ndern des Sichtbarkeitsstatus des Navigators benachrichtigt werden sollen.
	 * @param listener	Zuk�nftig nicht mehr zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener aus der Liste entfernt werden konnte. Wenn er nicht enthalten war, liefert die Funktion <code>false</code>.
	 */
	public boolean removeNavigatorVisibleChangeListener(final Runnable listener) {
		return navigatorVisibleChangeListeners.remove(listener);
	}

	private void fireNavigatorVisibleChanged() {
		if (navigatorVisibleChangeListeners==null || !guiReady) return;
		for (Runnable listener: navigatorVisibleChangeListeners)
			if (listener!=null) listener.run();
	}

	/**
	 * Stellt ein, ob die Navigator-Leiste angezeigt werden soll
	 * @param navigatorVisible	Navigator-Leiste anzeigen
	 */
	public void setNavigatorVisible(final boolean navigatorVisible) {
		rightArea.setVisible(navigatorVisible);
		fireNavigatorVisibleChanged();
	}

	private JButton createRotatedToolbarButton(final JToolBar toolbar, final String title, final String hint, final Icon icon) {
		ImageIcon rotatedIcon=null;

		if (icon instanceof ImageIcon) {
			final double scale=SetupData.getSetup().scaleGUI;
			if (scale!=1.0) {
				final int w=(int)Math.round(icon.getIconWidth()*scale);
				final int h=(int)Math.round(icon.getIconHeight()*scale);
				final Image temp=((ImageIcon)icon).getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH);
				rotatedIcon=new ImageIcon(temp,"");
			} else {
				rotatedIcon=(ImageIcon)icon;
			}
		}

		final JButton button=ButtonRotator.getRotatedButton(title,rotatedIcon);

		if (toolbar!=null) toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.addActionListener(toolbarListener);

		return button;
	}

	/**
	 * Legt einen neuen Symbolleisten-Eintrag an
	 * @param toolbar	�bergeordnetes Symbolleisten-Element
	 * @param title	Name des neuen Symbolleisten-Eintrags
	 * @param hint	Zus�tzlich anzuzeigender Tooltip f�r den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Pfad zu dem Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @return	Neu erstellter Symbolleisten-Eintrag
	 */
	protected final JButton createToolbarButton(final JToolBar toolbar, final String title, final String hint, final Icon icon) {
		JButton button=new JButton(title);
		if (toolbar!=null) toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.addActionListener(toolbarListener);
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	private JToolBar createTopToolBar(final boolean showZoomOnToolBar) {
		JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);

		buttonProperties=createToolbarButton(toolbar,Language.tr("Editor.ModelProperties"),Language.tr("Editor.ModelProperties.Info"),Images.MODEL.getIcon());
		toolbar.addSeparator();
		buttonAddEdge=createToolbarButton(toolbar,Language.tr("Editor.AddEdge"),Language.tr("Editor.AddEdge.Info"),Images.EDIT_EDGES_ADD.getIcon());
		buttonAddEdge.setEnabled(!readOnly);
		buttonAddElement=createToolbarButton(toolbar,Language.tr("Editor.AddElement"),Language.tr("Editor.AddElement.Info"),Images.ELEMENTTEMPLATES.getIcon());
		buttonAddElement.setEnabled(!readOnly);
		buttonEdit=createToolbarButton(toolbar,Language.tr("Editor.Edit"),Language.tr("Editor.Edit.Info"),Images.GENERAL_TOOLS.getIcon());
		buttonDelete=createToolbarButton(toolbar,Language.tr("Editor.Delete"),Language.tr("Editor.Delete.Info"),Images.EDIT_DELETE.getIcon());
		buttonDelete.setEnabled(!readOnly);
		if (showZoomOnToolBar) {
			toolbar.addSeparator();
			buttonZoomOut=createToolbarButton(toolbar,"",Language.tr("Main.Menu.View.ZoomOut"),Images.ZOOM_OUT.getIcon());
			buttonZoomIn=createToolbarButton(toolbar,"",Language.tr("Main.Menu.View.ZoomIn"),Images.ZOOM_IN.getIcon());
			buttonZoomDefault=createToolbarButton(toolbar,"",Language.tr("Main.Menu.View.ZoomDefault"),Images.ZOOM.getIcon());
			buttonFindModel=createToolbarButton(toolbar,"",Language.tr("Main.Menu.View.CenterModel"),Images.ZOOM_CENTER_MODEL.getIcon());
		}

		return toolbar;
	}

	private String keyStrokeToString(final KeyStroke key) {
		int modifiers=key.getModifiers();
		final StringBuilder text=new StringBuilder();
		if (modifiers > 0) {
			text.append(InputEvent.getModifiersExText(modifiers));
			text.append('+');
		}
		text.append(KeyEvent.getKeyText(key.getKeyCode()));
		return text.toString();
	}

	private JComponent createLeftToolBar() {
		final SetupData setup=SetupData.getSetup();

		leftArea=new JPanel(new BorderLayout());

		JToolBar leftToolbar=new JToolBar(SwingConstants.VERTICAL);
		leftArea.add(leftToolbar,BorderLayout.WEST);
		leftToolbar.setFloatable(false);

		final String hotkeyProperties=keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F2,InputEvent.CTRL_DOWN_MASK));
		buttonProperties=createRotatedToolbarButton(leftToolbar,Language.tr("Editor.ModelProperties.Short"),Language.tr("Editor.ModelProperties.Info")+" ("+hotkeyProperties+")",Images.MODEL.getIcon());
		if (!readOnly) {
			final String hotkeyToggleTemplates=keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
			buttonTemplates=createRotatedToolbarButton(leftToolbar,Language.tr("Editor.ToggleTemplates.Short"),Language.tr("Editor.ToggleTemplates.Info")+" ("+hotkeyToggleTemplates+")",Images.ELEMENTTEMPLATES.getIcon());
			buttonAddEdge=createRotatedToolbarButton(leftToolbar,Language.tr("Editor.AddEdge.Short"),Language.tr("Editor.AddEdge.Info"),Images.EDIT_EDGES_ADD.getIcon());
			buttonAddEdge.setEnabled(!readOnly);
		}

		if (!readOnly) {
			leftToolbar.add(separatorUndoRedo=new JToolBar.Separator(null));
			buttonUndo=createToolbarButton(leftToolbar,"",Language.tr("Main.Menu.Edit.Undo"),Images.EDIT_UNDO.getIcon());
			buttonUndo.setEnabled(false);
			buttonRedo=createToolbarButton(leftToolbar,"",Language.tr("Main.Menu.Edit.Redo"),Images.EDIT_REDO.getIcon());
			buttonRedo.setEnabled(false);
		}

		templates=new JList<>();
		templates.setCellRenderer(templatesRenderer=new ModelElementCatalogListCellRenderer<>());
		if (setup.onlyOneOpenTemplatesGroup) enforceOnlyOneGroupOpen();

		templates.setModel(ModelElementCatalog.getCatalog().getTemplatesListModel(setup.visibleTemplateGroups,setup.openTemplateGroups,"",model.surface.getParentSurface()!=null));

		templates.setDragEnabled(true);
		templates.setTransferHandler(new ModelElementCatalogTransferHandler());
		templates.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					final int index=templates.locationToIndex(e.getPoint());
					if (index<0) return;
					final ModelElementPosition element=templates.getModel().getElementAt(index);
					if (!(element instanceof ModelElementListGroup)) return;
					((ModelElementListGroup)element).toggleShowSub();
					saveGroupOpenStatus(((ModelElementListGroup)element).getIndex(),((ModelElementListGroup)element).isShowSub());
					updateTemplatesFilter();
					ListModel<ModelElementPosition> model=templates.getModel();
					templates.setModel(new DefaultListModel<>());
					templates.setModel(model);
					setTemplatesVisible(isTemplatesVisible());
				}
			}
		});
		final JScrollPane leftAreaScrollPane=new JScrollPane(templates);

		leftAreaTemplates=new JPanel(new BorderLayout());
		final JPanel sub=new JPanel(new BorderLayout());
		leftAreaTemplates.add(sub,BorderLayout.NORTH);
		sub.add(getTopInfoPanel(Language.tr("Editor.Templates"),Images.ELEMENTTEMPLATES.getIcon(),e->setTemplatesVisible(false),null,"F2"),BorderLayout.NORTH);

		sub.add(leftAreaQuickFilter=new JPlaceholderTextField(),BorderLayout.CENTER);
		leftAreaQuickFilter.setPlaceholder(Language.tr("Editor.QuickFilter"));
		leftAreaQuickFilter.setToolTipText(Language.tr("Editor.QuickFilter.Tooltip"));
		leftAreaQuickFilter.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {updateTemplatesFilter(); leftAreaQuickFilter.requestFocus();}
			@Override public void keyReleased(KeyEvent e) {updateTemplatesFilter(); leftAreaQuickFilter.requestFocus();}
			@Override public void keyPressed(KeyEvent e) {updateTemplatesFilter(); leftAreaQuickFilter.requestFocus();}
		});
		final JToolBar miniToolbar=new JToolBar(SwingConstants.HORIZONTAL);
		miniToolbar.setFloatable(false);
		sub.add(miniToolbar,BorderLayout.EAST);
		miniToolbar.add(leftAreaTemplatesFilterButton=new JButton());
		leftAreaTemplatesFilterButton.setToolTipText(Language.tr("Editor.TemplateFilter.Tooltip"));
		leftAreaTemplatesFilterButton.setIcon(Images.ELEMENTTEMPLATES_FILTER.getIcon());
		leftAreaTemplates.add(leftAreaScrollPane,BorderLayout.CENTER);
		leftArea.add(leftAreaTemplates,BorderLayout.CENTER);
		leftAreaTemplatesFilterButton.addActionListener(e->showFilterTemplatesPopup());

		if (!readOnly) setTemplatesVisible(setup.startTemplateMode==SetupData.StartTemplateMode.START_TEMPLATE_VISIBLE || (setup.startTemplateMode==SetupData.StartTemplateMode.START_TEMPLATE_LASTSTATE && setup.showTemplates)); else setTemplatesVisible(false);

		leftToolbar.add(Box.createVerticalGlue());
		buttonExplorer=createRotatedToolbarButton(leftToolbar,Language.tr("Editor.ModelOverview.Short"),Language.tr("Editor.ModelOverview.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F12,InputEvent.CTRL_DOWN_MASK))+")",Images.GENERAL_FIND.getIcon());

		return leftArea;
	}

	private JComponent createNavigatorPanel() {
		navigator=new JList<>();
		navigator.setCellRenderer(navigatorRenderer=new ModelElementNavigatorListCellRenderer<>());
		navigator.setModel(navigatorModel=new DefaultListModel<>());

		navigator.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					final int index=navigator.locationToIndex(e.getPoint());
					if (index<0 || index>=navigator.getModel().getSize()) return;
					final ModelElementBox element=navigator.getModel().getElementAt(index);
					surfacePanel.selectAndScrollToElement(element.getId());
				}
			}
		});

		rightArea=new JPanel(new BorderLayout());
		rightArea.add(getTopInfoPanel(Language.tr("Editor.Navigator"),Images.NAVIGATOR.getIcon(),null,e->setNavigatorVisible(false),"F12"),BorderLayout.NORTH);

		rightArea.add(new JScrollPane(navigator),BorderLayout.CENTER);
		rightArea.setVisible(false);

		return rightArea;
	}

	private JPanel getTopInfoPanel(final String title, final Icon icon, final ActionListener actionLeft, final ActionListener actionRight, final String hotkey) {
		final JPanel top=new JPanel(new BorderLayout());
		top.setBackground(Color.LIGHT_GRAY);

		final JLabel label=new JLabel(title);
		final Font font=label.getFont();
		label.setFont(new java.awt.Font(font.getFontName(),java.awt.Font.BOLD,font.getSize()+2));
		if (icon!=null) label.setIcon(icon);
		final JPanel middle=new JPanel(new FlowLayout(FlowLayout.CENTER));
		middle.setOpaque(false);
		middle.add(label);
		top.add(middle,BorderLayout.CENTER);

		if (actionLeft!=null) {
			final JToolBar toolbar=new JToolBar();
			toolbar.setFloatable(false);
			toolbar.setOpaque(false);
			top.add(toolbar,BorderLayout.WEST);
			final JButton button=new JButton();
			toolbar.add(button);
			button.setOpaque(false);
			button.addActionListener(actionLeft);
			button.setToolTipText(Language.tr("Editor.CloseSidebarTooltip")+((hotkey!=null && !hotkey.isEmpty())?(" ("+hotkey+")"):""));
			button.setIcon(Images.ELEMENTTEMPLATES_CLOSEPANEL.getIcon());
		} else {
			if (actionRight!=null) {
				final JPanel filler=new JPanel(new FlowLayout(FlowLayout.LEFT));
				filler.setOpaque(false);
				filler.add(Box.createHorizontalStrut(18));
				top.add(filler,BorderLayout.WEST);
			}
		}

		if (actionRight!=null) {
			final JToolBar toolbar=new JToolBar();
			toolbar.setFloatable(false);
			toolbar.setOpaque(false);
			top.add(toolbar,BorderLayout.EAST);
			final JButton button=new JButton();
			toolbar.add(button);
			button.setOpaque(false);
			button.addActionListener(actionRight);
			button.setToolTipText(Language.tr("Editor.CloseSidebarTooltip")+((hotkey!=null && !hotkey.isEmpty())?(" ("+hotkey+")"):""));
			button.setIcon(Images.NAVIGATOR_CLOSEPANEL.getIcon());
		} else {
			if (actionLeft!=null) {
				final JPanel filler=new JPanel(new FlowLayout(FlowLayout.LEFT));
				filler.setOpaque(false);
				filler.add(Box.createHorizontalStrut(18));
				top.add(filler,BorderLayout.EAST);
			}
		}

		return top;
	}

	private void saveGroupOpenStatus(final int groupIndex, final boolean open) {
		final SetupData setup=SetupData.getSetup();
		final String s=setup.openTemplateGroups;
		final StringBuilder sb=new StringBuilder();

		int openCount=open?1:0;
		for (int i=0;i<ModelElementCatalog.getCatalog().getGroupCount();i++) {
			boolean visible=(i>=setup.visibleTemplateGroups.length() || (setup.visibleTemplateGroups.charAt(i)!='-' && setup.visibleTemplateGroups.charAt(i)!='0'));
			if (visible && setup.onlyOneOpenTemplatesGroup) {
				if (i==groupIndex) {
					sb.append(open?'X':'-');
				} else {
					if (s.length()<=i || s.charAt(i)=='X') {
						openCount++;
						if (openCount<2) sb.append('X'); else sb.append('-');
					} else {
						sb.append('-');
					}
				}
			} else {
				if (i==groupIndex) sb.append(open?'X':'-'); else sb.append((i>=s.length())?'X':s.charAt(i));
			}
		}

		setup.openTemplateGroups=sb.toString();
		setup.saveSetup();
	}

	private void updateTemplatesFilter() {
		final SetupData setup=SetupData.getSetup();
		final String filter=leftAreaQuickFilter.getText().trim();
		final String visibleGroups=setup.visibleTemplateGroups;
		final String openGroups=setup.openTemplateGroups;
		templates.setModel(ModelElementCatalog.getCatalog().getTemplatesListModel(visibleGroups,openGroups,filter,getModel().surface.getParentSurface()!=null));
		setTemplatesVisible(isTemplatesVisible());
	}

	/**
	 * F�gt einen Listener hinzu, der benachrichtigt wird, wenn eine Datei auf der Komponente abgelegt wird
	 * @param fileDropListener	Zu benachrichtigender Listener (der Dateiname ist �ber die <code>getActionCommand()</code>-Methode des �bergebenen <code>ActionEvent</code>-Objekts abrufbar)
	 */
	public void addFileDropListener(final ActionListener fileDropListener) {
		if (fileDropListeners.indexOf(fileDropListener)<0) fileDropListeners.add(fileDropListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer auf dieser Komponente abgelegten Datei zu benachrichtigenden Listener
	 * @param fileDropListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeFileDropListener(final ActionListener fileDropListener) {
		return fileDropListeners.remove(fileDropListener);
	}

	private boolean dropFile(final FileDropperData data) {
		final ActionEvent event=FileDropperData.getActionEvent(data);
		for (ActionListener listener: fileDropListeners) listener.actionPerformed(event);
		return true;
	}

	@Override
	protected void buildGUI() {
		toolbarListener=new ToolbarListener();
		setLayout(new BorderLayout());

		JPanel p=new JPanel(new BorderLayout());
		add(p,BorderLayout.NORTH);
		if (useTopToolBar) p.add(createTopToolBar(false),BorderLayout.NORTH);
		p.add(additionalInfoArea=new JToolBar(),BorderLayout.CENTER);
		additionalInfoArea.setVisible(false);
		additionalInfoArea.setFloatable(false);
		additionalInfoArea.setBackground(new Color(255,255,240));
		additionalInfoArea.add(additionalInfoLabel=new JLabel(""));
		additionalInfoLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		additionalInfoArea.add(Box.createHorizontalGlue());
		JButton button=new JButton(Language.tr("Editor.AddEdge.Hint.RemoveButton"));
		button.setBackground(new Color(255,255,240));
		button.setToolTipText(Language.tr("Editor.AddEdge.Hint.RemoveButton.Hint"));
		additionalInfoArea.add(button);
		button.setIcon(Images.EDIT_EDGES_ADD_CLOSEPANEL.getIcon());
		button.addActionListener(e->{
			additionalInfoArea.setVisible(false);
			InfoPanel.getInstance().setVisible(InfoPanel.globalAddEdge,false);
		});

		JPanel main=new JPanel(new BorderLayout());
		add(main,BorderLayout.CENTER);

		if (!useTopToolBar) main.add(createLeftToolBar(),BorderLayout.WEST);

		final boolean isSubSurface=(model!=null && model.surface!=null && model.surface.getParentSurface()!=null);
		surfacePanel=new ModelSurfacePanel(readOnly,!isSubSurface);
		surfacePanel.setAdditionalTooltipGetter(box->getStatisticsInfoForElement(box));
		surfacePanel.addSelectionListener(e->fireSelectionListener());
		surfacePanel.addLinkListener(link->fireLinkListener(link));
		surfacePanel.setRaster(SetupData.getSetup().grid);
		if (model!=null) surfacePanel.setColors(model.surfaceColors);
		rulerPanel=new RulerPanel(surfacePanel,SetupData.getSetup().showRulers);
		surfacePanel.addStateChangeListener(e->updateStatusBar());
		surfacePanel.addUndoRedoDoneListener(e->updateCanUndoRedo());
		surfacePanel.setDropAccept(true);
		surfacePanel.addFileDropListener(e->dropFile((FileDropperData)e.getSource()));
		surfacePanel.addShowModelPropertiesListener(e->{
			final String cmd=e.getActionCommand();
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES)) {
				showModelPropertiesDialog(null);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_OPERATORS)) {
				showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.OPERATORS);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_TRANSPORTERS)) {
				showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.TRANSPORTERS);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_BACKGROUND)) {
				showBackgroundColorDialog();
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_LAYERS)) {
				showLayersDialog();
				return;
			}
		});
		surfacePanel.addResourceCountSetter((name,count)->changeResourceCount(name,count.intValue()));
		surfacePanel.addZoomChangeListener(e->{
			labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
			templatesRenderer.setZoom(surfacePanel.getZoom());
			updateTemplatesFilter();
			navigatorRenderer.setZoom(surfacePanel.getZoom());
			navigatorModel.clear();
			updateNavigatorList();
		});
		surfacePanel.addBuildParameterSeriesListener(template->fireBuildParameterSeries(template));
		surfacePanel.addSetElementTemplatesVisibilityListener(visibility->{switch (visibility) {
		case HIDE: setTemplatesVisible(false); break;
		case SHOW: setTemplatesVisible(true); break;
		case TOGGLE: setTemplatesVisible(!isTemplatesVisible()); break;
		}});
		main.add(rulerPanel,BorderLayout.CENTER);

		main.add(createNavigatorPanel(),BorderLayout.EAST);

		JPanel statusPanel=new JPanel(new BorderLayout());
		add(statusPanel,BorderLayout.SOUTH);
		statusPanel.add(statusBar=new JLabel(""),BorderLayout.CENTER);
		statusBar.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		JPanel zoomArea=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		statusPanel.add(zoomArea,BorderLayout.EAST);
		statusBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) && buttonProperties.isVisible()) {
					setupInfoLabels(true); repaint(); showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.INFO);
				}
			}
		});

		zoomArea.add(labelZoom=new JLabel(Math.round(100*surfacePanel.getZoom())+"% "));
		labelZoom.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {showZoomContextMenu(labelZoom);}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
		});
		labelZoom.setToolTipText(Language.tr("Editor.SetupZoom"));
		zoomArea.add(buttonZoomOut2=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomOut"),Images.ZOOM_OUT.getIcon()));
		buttonZoomOut2.setPreferredSize(new Dimension(20,20));
		buttonZoomOut2.setBorderPainted(false);
		buttonZoomOut2.setFocusPainted(false);
		buttonZoomOut2.setContentAreaFilled(false);
		zoomArea.add(buttonZoomIn2=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomIn"),Images.ZOOM_IN.getIcon()));
		buttonZoomIn2.setPreferredSize(new Dimension(20,20));
		buttonZoomIn2.setBorderPainted(false);
		buttonZoomIn2.setFocusPainted(false);
		buttonZoomIn2.setContentAreaFilled(false);
		zoomArea.add(buttonZoomDefault2=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomDefault"),Images.ZOOM.getIcon()));
		buttonZoomDefault2.setPreferredSize(new Dimension(20,20));
		buttonZoomDefault2.setBorderPainted(false);
		buttonZoomDefault2.setFocusPainted(false);
		buttonZoomDefault2.setContentAreaFilled(false);
		zoomArea.add(buttonFindModel2=createToolbarButton(null,"",Language.tr("Main.Menu.View.CenterModel"),Images.ZOOM_CENTER_MODEL.getIcon()));
		buttonFindModel2.setPreferredSize(new Dimension(20,20));
		buttonFindModel2.setBorderPainted(false);
		buttonFindModel2.setFocusPainted(false);
		buttonFindModel2.setContentAreaFilled(false);

		updateStatusBar();
	}

	private static final String statusHTMLStart="<html><body>";
	private static final String statusHTMLGreen="<span style=\"color: green;\">";
	private static final String statusHTMLOrange="<span style=\"color: orange;\">";
	private static final String statusHTMLRed="<span style=\"color: red;\">";
	private static final String statusHTMLSpanEnd="</span>";
	private static final String statusHTMLEnd="</body></html>";

	private void updateNavigatorList() {
		if (surfacePanel==null) return;
		if (surfacePanel.isOperationRunning()) return;
		final ModelSurface surface=surfacePanel.getSurface();

		final List<ModelElementBox> boxElements=surface.getElements().stream()
				.filter(element->surface.isVisibleOnLayer(element))
				.filter(element->element instanceof ModelElementBox)
				.map(element->(ModelElementBox)element)
				.collect(Collectors.toList());
		if (navigatorModel.getSize()!=boxElements.size()) {
			navigatorModel.clear();
			for (ModelElementBox element: boxElements) navigatorModel.addElement(element);
		} else {
			for (int i=0;i<boxElements.size();i++) if (navigatorModel.getElementAt(i)!=boxElements.get(i)) navigatorModel.setElementAt(boxElements.get(i),i);
		}
		final int navigatorIndex=boxElements.indexOf(surface.getSelectedElement());
		if (navigatorIndex>=0) {
			navigator.setSelectedIndex(navigatorIndex);
			navigator.ensureIndexIsVisible(navigatorIndex);
		}

		navigator.repaint();
	}

	/**
	 * Muss aufgerufen werden, wenn die Statuszeile aktualisiert werden soll.<br>
	 * (Z.B. nach dem Neuladen des Setup.)
	 */
	public void updateStatusBar() {
		if (surfacePanel==null) return;
		if (surfacePanel.isOperationRunning()) return;
		final ModelSurface surface=surfacePanel.getSurface();
		if (surface==null) return;
		final SetupData setup=SetupData.getSetup();

		if (buttonAddEdge!=null) buttonAddEdge.setSelected(surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP1 || surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP2);
		if (buttonAddElement!=null) buttonAddElement.setSelected(surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_ELEMENT);

		if (buttonEdit!=null) buttonEdit.setEnabled(surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_NORMAL && surface.getSelectedElement()!=null && surface.getSelectedElement() instanceof ModelElementBox);
		if (buttonDelete!=null) buttonDelete.setEnabled(!readOnly && surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_NORMAL && (surface.getSelectedElement()!=null || !surface.getSelectedArea(true).isEmpty()));

		updateNavigatorList();

		if (statusBar!=null) switch (surfacePanel.getMode()) {
		case MODE_NORMAL:
			setAdditionalInfoLabel(false,null);
			final int count=surface.getElementCount();
			final String elements=count+" "+((count==1)?Language.tr("Editor.Element.Singular"):Language.tr("Editor.Element.Plural"));

			final EditModel model=getModel();
			final BackgroundSystem backgroundSystem=BackgroundSystem.getBackgroundSystem();
			String check="";
			String tooltip=null;
			Icon icon=null;
			if (!readOnly && setup.autoSaveMode==SetupData.AutoSaveMode.AUTOSAVE_ALWAYS && getLastFile()!=null && isModelChanged()) {
				saveModel(getLastFile());
			}

			if (backgroundSystem.canCheck(model.surface)) {
				final String error=backgroundSystem.process(model,!readOnly);
				if (error==null) {
					check=", "+statusHTMLGreen+Language.tr("Editor.Check.Ok")+statusHTMLSpanEnd;
				} else {
					check=", "+statusHTMLRed+Language.tr("Editor.Check.Error")+statusHTMLSpanEnd;
					tooltip=statusHTMLStart+statusHTMLRed+"<b>"+error+"</b>"+statusHTMLSpanEnd+statusHTMLEnd;
					icon=Images.GENERAL_WARNING.getIcon();
				}
			}
			setStatusBarInfo(statusHTMLStart+elements+check+statusHTMLEnd,tooltip,icon);
			break;
		case MODE_ADD_ELEMENT:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceElement")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_STATION.getIcon());
			break;
		case MODE_ADD_EDGE_STEP1:
			setAdditionalInfoLabel(InfoPanel.getInstance().isVisible(InfoPanel.globalAddEdge),statusHTMLStart+"<b>"+Language.tr("Editor.AddEdge.PlacePoint1.Long")+"</b>"+statusHTMLEnd);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddEdge.PlacePoint1")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.EDIT_EDGES_ADD.getIcon());
			break;
		case MODE_ADD_EDGE_STEP2:
			setAdditionalInfoLabel(InfoPanel.getInstance().isVisible(InfoPanel.globalAddEdge),statusHTMLStart+"<b>"+Language.tr("Editor.AddEdge.PlacePoint2.Long")+"</b>"+statusHTMLEnd);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddEdge.PlacePoint2")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.EDIT_EDGES_ADD.getIcon());
			break;
		case MODE_INSERT_ELEMENTS_FROM_CLIPBOARD:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceClipboardElement")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_STATION.getIcon());
			break;
		case MODE_INSERT_ELEMENTS_FROM_TEMPLATE:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceTemplateElement")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_STATION.getIcon());
			break;
		case MODE_INSERT_IMAGE:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceClipboardImage")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_IMAGE.getIcon());
			break;
		case MODE_INSERT_TEXT:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceClipboardText")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_TEXT.getIcon());
			break;
		}
	}

	private void setAdditionalInfoLabel(final boolean show, final String text) {
		additionalInfoArea.setVisible(show && text!=null);
		if (text!=null) additionalInfoLabel.setText(statusHTMLStart+"<b>"+text+"</b>"+statusHTMLEnd);

		additionalInfoLabel.setIcon(Images.GENERAL_INFO.getIcon());
	}

	private void setStatusBarInfo(final String text, final String tooltip, final Icon icon) {
		statusBar.setText(text);
		statusBar.setToolTipText(tooltip);
		statusBar.setIcon(icon);
	}

	/**
	 * Blendet die Statuszeile ein oder aus
	 * @param statusBarVisible	Sichtbarkeitsstatus der Statuszeile
	 */
	public void setStatusBarVisible(final boolean statusBarVisible) {
		statusBar.setVisible(statusBarVisible);
	}

	/**
	 * Schaltet einige Funktionen zum Bearbeiten auch w�hrend des Read-Only-Modus frei.<br>
	 * Erlaubt wird das Bearbeiten von Elementen sowie das Verschieben.
	 * Das L�schen von Elementen und das Erstellen neuer Elemente bleibt aber verboten.
	 */
	public void allowEditorDialogs() {
		allowChangeOperationsOnReadOnly=true;
		surfacePanel.allowEditorDialogs();
	}

	private void updateCanUndoRedo() {
		buttonUndo.setEnabled(!readOnly && surfacePanel.canUndo());
		buttonRedo.setEnabled(!readOnly && surfacePanel.canRedo());
		fireUndoRedoDoneListener();
	}

	@Override
	protected void writeGUIDataToModel() {
		model.name=name;
		model.description=description;
		model.author=author;
		model.clientCount=clientCount;
		model.warmUpTime=warmUpTime;
		ModelSurface surface=surfacePanel.getSurface();
		model.resources=surface.getResources().clone();
		model.schedules=surface.getSchedules().clone();
		model.surface=surface.clone(false,model.resources,model.schedules,surface.getParentSurface(),model);
		final Color[] colors=surfacePanel.getColors();
		model.surfaceColors[0]=colors[0];
		model.surfaceColors[1]=colors[1];
		if (colors.length>2) model.surfaceColors[2]=colors[2];
	}

	@Override
	protected void loadGUIDataFromModel() {
		name=model.name;
		description=model.description;
		author=model.author;
		clientCount=model.clientCount;
		warmUpTime=model.warmUpTime;
		if (surfacePanel!=null) {
			surfacePanel.setColors(model.surfaceColors); /* Reihenfolge ist wichtig. setSurface w�rde bedingt durch fireNotify Farbe von Modell aus surfacePanel �berschreiben, daher erst Farbe aus Modell in surfacePanel �bertragen. */
			surfacePanel.setSurface(model,model.surface.clone(false,model.resources.clone(),model.schedules.clone(),model.surface.getParentSurface(),model),model.clientData,model.sequences);
		}
		if (model.surface.getElementCount()>0) setupInfoLabels(true);
	}

	/**
	 * Zeigt den Modelleigenschaftendialog an.
	 * @param initialPage	Beim Aufruf des Dialogs anzuzeigende Seite (darf <code>null</code> sein)
	 */
	public void showModelPropertiesDialog(final ModelPropertiesDialog.InitialPage initialPage) {
		final ModelPropertiesDialog dialog=new ModelPropertiesDialog(owner,getModel(),readOnly,initialPage);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK && !getModel().equalsEditModel(dialog.getModel())) {
			File file=getLastFile();
			setModel(dialog.getModel());
			setModelChanged(true);
			setLastFile(file);
		}
	}

	/**
	 * Zeigt den Dialog zum Bearbeiten der Ebenen an.
	 */
	public void showLayersDialog() {
		final LayersDialog dialog=new LayersDialog(owner,getModel(),readOnly);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK && !getModel().equalsEditModel(dialog.getModel())) {
			File file=getLastFile();
			setModel(dialog.getModel());
			setModelChanged(true);
			setLastFile(file);
		}
	}

	/**
	 * Zeigt den Dialog zur Konfiguration von Hintergrund- und Rasterfarbe an.
	 */
	public void showBackgroundColorDialog() {
		final EditModel model=getModel();

		final BackgroundColorDialog dialog=new BackgroundColorDialog(owner,model.surfaceColors,readOnly);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		final Color[] colors=dialog.getColors();
		if (colors==null || colors.length!=3) return;
		if (!Objects.deepEquals(model.surfaceColors,colors)) {
			model.surfaceColors[0]=colors[0];
			model.surfaceColors[1]=colors[1];
			model.surfaceColors[2]=colors[2];
			final File file=getLastFile();
			setModel(model);
			setLastFile(file);
			setModelChanged(true);
		}
	}

	private boolean changeResourceCount(final String name, final int count) {
		final EditModel model=getModel();
		final ModelResource resource=model.resources.getNoAutoAdd(name);
		if (resource==null || resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return false;
		if (count<=0) return false;
		if (resource.getCount()==count) return true;
		resource.setCount(count);
		setModel(model);
		setModelChanged(true);
		return true;
	}

	private class ToolbarListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source=e.getSource();

			if (source==buttonProperties) {setupInfoLabels(true); repaint(); showModelPropertiesDialog(null); return;}
			if (source==buttonTemplates) {setTemplatesVisible(!isTemplatesVisible()); return;}
			if (source==buttonAddEdge) {
				setupInfoLabels(true);
				if (surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP1 || surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP2) {
					surfacePanel.cancelAdd();
					return;
				}
				surfacePanel.startAddEdge();
				return;
			}
			if (buttonAddElement!=null && source==buttonAddElement) {
				if (surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_ELEMENT) {
					surfacePanel.cancelAdd();
					return;
				}
				JPopupMenu popupMenu=new JPopupMenu();
				ModelElementCatalog catalog=ModelElementCatalog.getCatalog();
				for (String name: catalog.getMenuNames()) {
					JMenuItem item=new JMenuItem(name);
					item.addActionListener(ev->{
						if (!(ev.getSource() instanceof JMenuItem)) return;
						ModelElementPosition element=ModelElementCatalog.getCatalog().getMenuElement(((JMenuItem)(ev.getSource())).getText());
						if (element!=null) surfacePanel.startAddElement(element);
					});
					item.setIcon(catalog.getMenuIcon(name));
					item.setToolTipText(catalog.getMenuToolTip(name));
					popupMenu.add(item);
				}
				popupMenu.show(buttonAddElement,0,buttonAddElement.getHeight());
				return;
			}
			if (source==buttonEdit) {surfacePanel.editSelectedElement(); return;}
			if (source==buttonDelete) {surfacePanel.deleteSelectedElements(); return;}
			if (source==buttonZoomOut || source==buttonZoomOut2) {zoomOut();  return;}
			if (source==buttonZoomIn || source==buttonZoomIn2) {zoomIn(); return;}
			if (source==buttonZoomDefault || source==buttonZoomDefault2) {zoomDefault(); return;}
			if (source==buttonFindModel || source==buttonFindModel2) {centerModel(); return;}
			if (source==buttonUndo) {doUndo(); return;}
			if (source==buttonRedo) {doRedo(); return;}
			if (source==buttonExplorer) {showExplorer(buttonExplorer); return;}
		}
	}

	private File showExportDialog(Component parent, final String title) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(title);
		final FileFilter jpg=new FileNameExtensionFilter(Language.tr("FileType.jpeg")+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(Language.tr("FileType.gif")+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(Language.tr("FileType.png")+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(Language.tr("FileType.bmp")+" (*.bmp)","bmp");
		final FileFilter pdf=new FileNameExtensionFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
		final FileFilter svg=new FileNameExtensionFilter(Language.tr("FileType.svg")+" (*.svg)","svg");
		final FileFilter eps=new FileNameExtensionFilter(Language.tr("FileType.eps")+" (*.eps)","eps");
		final FileFilter docx=new FileNameExtensionFilter(Language.tr("FileType.WordImage")+" (*.docx)","docx");
		final FileFilter pptx=new FileNameExtensionFilter(Language.tr("SlidesGenerator.FileTypePPTX")+" (*.pptx)","pptx");
		final FileFilter html=new FileNameExtensionFilter(Language.tr("FileType.HTML")+" (*.html)","html");
		final FileFilter drawio=new FileNameExtensionFilter(Language.tr("FileType.drawio")+" (*.drawio)","drawio");
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		fc.addChoosableFileFilter(pdf);
		fc.addChoosableFileFilter(svg);
		fc.addChoosableFileFilter(eps);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(pptx);
		fc.addChoosableFileFilter(html);
		fc.addChoosableFileFilter(drawio);
		fc.setFileFilter(png);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
			if (fc.getFileFilter()==svg) file=new File(file.getAbsoluteFile()+".svg");
			if (fc.getFileFilter()==eps) file=new File(file.getAbsoluteFile()+".eps");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==pptx) file=new File(file.getAbsoluteFile()+".pptx");
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
			if (fc.getFileFilter()==drawio) file=new File(file.getAbsoluteFile()+".drawio");
		}

		return file;
	}

	/**
	 * Exportiert das aktuelle Modell als Bilddatei oder pdf. Wird <code>null</code> als Parameter angegeben, so wird zun�chst ein Dateiauswahldialog angezeigt.
	 * @param file	Name der Datei, in der das Modell gespeichert werden soll. Wird <code>null</code> �bergeben, so wird ein Dialog zur Auswahl der Datei angezeigt.
	 * @return	Gibt im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung.
	 */
	public String exportModelToFile(File file) {
		if (file==null) {
			file=showExportDialog(getParent(),Language.tr("Editor.ExportModel"));
			if (file==null) return null;
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(getOwnerWindow(),file)) return null;
		}

		String format="png";
		if (file.getName().toLowerCase().endsWith(".jpg")) format="jpg";
		if (file.getName().toLowerCase().endsWith(".jpeg")) format="jpg";
		if (file.getName().toLowerCase().endsWith(".gif")) format="gif";
		if (file.getName().toLowerCase().endsWith(".pdf")) format="pdf";
		if (file.getName().toLowerCase().endsWith(".svg")) format="svg";
		if (file.getName().toLowerCase().endsWith(".eps")) format="eps";
		if (file.getName().toLowerCase().endsWith(".docx")) format="docx";
		if (file.getName().toLowerCase().endsWith(".pptx")) format="pptx";
		if (file.getName().toLowerCase().endsWith(".html")) format="html";
		if (file.getName().toLowerCase().endsWith(".drawio")) format="drawio";

		if (format.equalsIgnoreCase("html")) {
			/* HTML-Modus */
			final HTMLOutputBuilder builder=new HTMLOutputBuilder(getModel());
			return builder.build(file);
		}

		if (format.equalsIgnoreCase("pptx")) {
			/* pptx-Modus */
			final SlidesGenerator slides=new SlidesGenerator(getModel(),getExportImage());
			if (!slides.save(file)) return Language.tr("Editor.ExportModel.Error");
			return null;
		}

		if (format.equalsIgnoreCase("drawio")) {
			/* draw.io-Modus */
			final DrawIOExport export=new DrawIOExport(file);
			final EditModel model=getModel();
			export.process(model.surface,model);
			if (!export.save()) return Language.tr("Editor.ExportModel.Error");
			return null;
		}

		final SetupData setup=SetupData.getSetup();

		if (format.equalsIgnoreCase("docx")) {
			/* docx-Modus */
			if (!surfacePanel.saveDOCXToFile(file,setup.imageSize,setup.imageSize,true)) return Language.tr("Editor.ExportModel.Error");
			return null;
		}

		/* Bild */
		if (!surfacePanel.saveImageToFile(file,format,setup.imageSize,setup.imageSize)) return Language.tr("Editor.ExportModel.Error");
		return null;
	}

	/**
	 * Kopiert das aktuelle Modell als Bild in die Zwischenablage.
	 */
	public void exportModelToClipboard() {
		ImageTools.copyImageToClipboard(getExportImage());
	}

	/**
	 * Liefert das Bild in der Form, in der es auch per {@link #exportModelToClipboard()}
	 * in die Zwischenablage kopiert werden w�rde, direkt zur�ck.
	 * @return	Bild des Modells
	 */
	public BufferedImage getExportImage() {
		final SetupData setup=SetupData.getSetup();
		final BufferedImage image=surfacePanel.getImage(setup.imageSize,setup.imageSize);
		final Graphics g=image.getGraphics();
		g.setClip(0,0,setup.imageSize,setup.imageSize);
		return image;
	}

	/**
	 * Kopiert die ausgew�hlten Elemente in die Zwischenablage.
	 */
	public void copySelectedElementsToClipboard() {
		surfacePanel.copyToClipboard();
	}

	/**
	 * Kopiert die ausgew�hlten Elemente in die Zwischenablage und l�scht sie dann von der Zeichenfl�che
	 */
	public void cutSelectedElementsToClipboard() {
		surfacePanel.copyToClipboard();
		surfacePanel.deleteSelectedElements();
	}

	/**
	 * F�gt die in der Zwischenablage befindlichen Elemente in die Zeichnung ein.
	 * Befinden sich keine Elemente in der Zwischenablage, daf�r aber Text, so wird dieser
	 * als neues Text-Element eingef�gt.
	 */
	public void pasteFromClipboard() {
		surfacePanel.pasteFromClipboard();
		setupInfoLabels(true);
	}

	/**
	 * F�gt die als Stream vorliegenden Elemente in die Zeichnung ein.
	 * @param data	Einzuf�gende Elemente
	 */
	public void pasteDirect(final ByteArrayInputStream data) {
		surfacePanel.pasteDirect(data);
		setupInfoLabels(true);
	}

	/**
	 * L�scht die gew�hlten Elemente.
	 */
	public void deleteSelectedElements() {
		surfacePanel.deleteSelectedElements();
	}

	/**
	 * W�hlt alle Element aus.
	 */
	public void selectAll() {
		surfacePanel.selectAll();
	}

	/**
	 * Scrollt zu einem bestimmten Element und w�hlt es aus
	 * @param id	ID des auszuw�hlenden Elements
	 */
	public void selectAndScrollToElement(final int id) {
		surfacePanel.selectAndScrollToElement(id);
	}

	/**
	 * Gibt an, ob R�ckg�ngig-Schritte vorhanden sind
	 * @return	Gibt <code>true</code> zur�ck, wenn die R�ckg�ngig-Funktion zur Verf�gung steht
	 * @see #doUndo()
	 */
	public boolean canUndo() {
		return surfacePanel.canUndo();
	}

	/**
	 * Gibt an, ob Wiederholen-Schritte vorhanden sind
	 * @return	Gibt <code>true</code> zur�ck, wenn die Wiederholen-Funktion zur Verf�gung steht
	 * @see #doRedo()
	 */
	public boolean canRedo() {
		return surfacePanel.canRedo();
	}

	/**
	 * F�hrt (wenn m�glich) einen R�ckg�ngig-Schritt aus
	 * @see #canUndo()
	 */
	public void doUndo() {
		surfacePanel.doUndo();
	}

	/**
	 * F�hrt (wenn m�glich) einen Wiederholen-Schritt aus
	 * @see #canUndo()
	 */
	public void doRedo() {
		surfacePanel.doRedo();
	}

	private void fireUndoRedoDoneListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"undoredodone");
		for (ActionListener listener: undoRedoDoneListener) listener.actionPerformed(event);
	}

	/**
	 * F�gt einen Listener, der benachrichtigt wird, wenn sich die Verf�gbarkeit von Undo/Redo-Schritten �ndert, zu der Liste aller Listener hinzu.
	 * @param listener	Hinzuzuf�gender Listener
	 */
	public void addUndoRedoDoneListener(final ActionListener listener) {
		if (undoRedoDoneListener.indexOf(listener)<0) undoRedoDoneListener.add(listener);
	}

	/**
	 * Entfernt einen Listener, der benachrichtigt wird, wenn sich die Verf�gbarkeit von Undo/Redo-Schritten �ndert, aus der Liste aller Listener.
	 * @param listener	Zu entfernender Listener
	 * @return	Gibt <code>true</code> zur�ck, wenn sich der Listener bisher in der Liste befand
	 */
	public boolean removeUndoRedoDoneListener(final ActionListener listener) {
		return undoRedoDoneListener.remove(listener);
	}

	/**
	 * Liefert das Modell als Bild in einer drucktauglichen Variante
	 * @param size	Gr��e des Bildes (in x- bzw., y-Pixeln)
	 * @return	Modell als Bild
	 */
	public BufferedImage getPrintImage(final int size) {
		return surfacePanel.getImage(size,size);
	}

	private void showZoomContextMenu(final Component parent) {
		final JPopupMenu popup=new JPopupMenu();

		final int value=Math.max(1,Math.min(20,(int)Math.round(getZoom()*5)));

		final JSlider slider=new JSlider(SwingConstants.VERTICAL,1,20,value);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		final Dictionary<Integer,JComponent> labels=new Hashtable<>();
		labels.put(1,new JLabel("20%"));
		labels.put(2,new JLabel("40%"));
		labels.put(3,new JLabel("60%"));
		labels.put(4,new JLabel("80%"));
		labels.put(5,new JLabel("100%"));
		labels.put(6,new JLabel("120%"));
		labels.put(10,new JLabel("200%"));
		labels.put(15,new JLabel("300%"));
		labels.put(20,new JLabel("400%"));
		slider.setLabelTable(labels);
		slider.setPaintLabels(true);
		slider.setValue(value);
		slider.addChangeListener(e->setZoom(slider.getValue()/5.0));
		slider.setPreferredSize(new Dimension(slider.getPreferredSize().width,350));

		popup.add(slider);

		popup.show(parent,0,-350);
	}

	/**
	 * Stellt die Callback-Methode, die aufgerufen werden soll, wenn in der Modell-�bersicht auf "Suchen" geklickt wird, ein.
	 * @param callbackElementSearch	Callback-Methode, die aufgerufen werden soll, wenn in der Modell-�bersicht auf "Suchen" geklickt wird
	 */
	public void setElementSearchCallback(final Runnable callbackElementSearch) {
		this.callbackElementSearch=callbackElementSearch;
	}

	/**
	 * Stellt die Callback-Methode, die aufgerufen werden soll, wenn in der Modell-�bersicht auf "Elementeliste" geklickt wird, ein.
	 * @param callbackElementList	Callback-Methode, die aufgerufen werden soll, wenn in der Modell-�bersicht auf "Elementeliste" geklickt wird
	 */
	public void setElementListCallback(final Runnable callbackElementList) {
		this.callbackElementList=callbackElementList;
	}

	/**
	 * Zeigt die Modell�bersicht an.
	 */
	public void showExplorer() {
		showExplorer(buttonExplorer);
	}

	private void showExplorer(final Component parent) {
		final JPopupMenu popup=new JPopupMenu();

		final JPanel explorerFrame=new JPanel(new BorderLayout());

		final ModelExplorer explorer=new ModelExplorer(surfacePanel,500,250);
		explorerFrame.add(explorer,BorderLayout.CENTER);

		final JToolBar toolbar=new JToolBar();
		toolbar.setOrientation(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		JButton button;
		if (!isNavigatorVisible()) {
			toolbar.add(button=new JButton(Language.tr("Editor.ModelOverview.Navigator")));
			button.setToolTipText(Language.tr("Editor.ModelOverview.Navigator.Hint"));
			final JButton showNavigator=button;
			button.addActionListener(e->{setNavigatorVisible(true); showNavigator.setVisible(false);});
			button.setIcon(Images.NAVIGATOR.getIcon());
		}
		toolbar.add(button=new JButton(Language.tr("Editor.ModelOverview.Search")));
		button.setToolTipText(Language.tr("Editor.ModelOverview.Search.Hint"));
		button.addActionListener(e->{if (callbackElementSearch!=null) callbackElementSearch.run();});
		button.setIcon(Images.GENERAL_FIND.getIcon());
		toolbar.add(button=new JButton(Language.tr("Editor.ModelOverview.List")));
		button.setToolTipText(Language.tr("Editor.ModelOverview.List.Hint"));
		button.addActionListener(e->{if (callbackElementList!=null) callbackElementList.run();});
		button.setIcon(Images.MODEL_LIST_ELEMENTS.getIcon());
		explorerFrame.add(toolbar,BorderLayout.SOUTH);

		popup.add(explorerFrame);
		popup.show(parent,parent.getWidth()+1,-explorer.getHeight()-30+parent.getHeight());
	}

	private void enforceOnlyOneGroupOpen() {
		final SetupData setup=SetupData.getSetup();

		/* Anzahl an offenen Gruppen bestimmen */
		int openCount=0;
		for (int i=0;i<ModelElementCatalog.getCatalog().getGroupCount();i++) {
			final boolean visible=setup.visibleTemplateGroups.length()<=i || (setup.visibleTemplateGroups.charAt(i)!='-' && setup.visibleTemplateGroups.charAt(i)!='0');
			if (visible && (setup.openTemplateGroups.length()<=i || setup.openTemplateGroups.charAt(i)=='X')) openCount++;
		}
		if (openCount<=1) return;

		final StringBuilder sb=new StringBuilder();
		openCount=0;
		for (int i=0;i<ModelElementCatalog.getCatalog().getGroupCount();i++) {
			final boolean visible=setup.visibleTemplateGroups.length()<=i || (setup.visibleTemplateGroups.charAt(i)!='-' && setup.visibleTemplateGroups.charAt(i)!='0');
			boolean open=(setup.openTemplateGroups.length()<=i || setup.openTemplateGroups.charAt(i)=='X');
			if (visible) {
				if (open) openCount++;
				if (openCount>1) sb.append('-'); else sb.append(open?'X':'-');
			} else {
				sb.append(open?'X':'-');
			}
		}
		setup.openTemplateGroups=sb.toString();
		setup.saveSetup();
	}

	private void showFilterTemplatesPopup() {
		JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Editor.TemplateFilter.Info")));
		item.setEnabled(false);
		popup.addSeparator();

		final SetupData setup=SetupData.getSetup();
		final String groups=setup.visibleTemplateGroups;
		for (int i=0;i<ModelElementCatalog.GROUP_ORDER.length;i++) {
			final JCheckBoxMenuItem check=new JCheckBoxMenuItem(ModelElementCatalog.GROUP_ORDER[i]);
			check.setSelected(groups.length()<=i || (groups.charAt(i)!='0' && groups.charAt(i)!='-'));
			popup.add(check);
			final int index=i;
			check.addActionListener(e->{
				String s=setup.visibleTemplateGroups;
				final StringBuilder sb=new StringBuilder();
				int j=Math.min(index,s.length());
				if (j>=1) sb.append(s.substring(0,j));
				while (sb.length()<index) sb.append("X");
				if (s.length()<=index || (s.charAt(index)!='0' && s.charAt(index)!='-')) sb.append('-'); else sb.append('X');
				if (s.length()>index+1) sb.append(s.substring(index+1));
				setup.visibleTemplateGroups=sb.toString();
				setup.saveSetup();
				if (setup.onlyOneOpenTemplatesGroup) enforceOnlyOneGroupOpen();
				updateTemplatesFilter();
			});
		}
		popup.addSeparator();
		item=new JMenuItem(Language.tr("Editor.TemplateFilter.ShowAll"));
		popup.add(item);
		item.addActionListener(e->{
			setup.visibleTemplateGroups="";
			if (setup.onlyOneOpenTemplatesGroup) enforceOnlyOneGroupOpen();
			setup.saveSetup();
			updateTemplatesFilter();
		});

		popup.addSeparator();

		item=new JMenuItem(Language.tr("Editor.TemplateFilter.OpenAll"));
		popup.add(item);
		item.setIcon(Images.ELEMENTTEMPLATES_GROUP_OPEN.getIcon());
		item.addActionListener(e->{
			setup.openTemplateGroups="";
			setup.onlyOneOpenTemplatesGroup=false;
			setup.saveSetup();
			updateTemplatesFilter();
		});
		item=new JMenuItem(Language.tr("Editor.TemplateFilter.CloseAll"));
		popup.add(item);
		item.setIcon(Images.ELEMENTTEMPLATES_GROUP_CLOSE.getIcon());
		item.addActionListener(e->{
			final StringBuilder sb=new StringBuilder();
			for (int i=0;i<ModelElementCatalog.getCatalog().getGroupCount();i++) sb.append("-");
			setup.openTemplateGroups=sb.toString();
			setup.saveSetup();
			updateTemplatesFilter();
		});
		final JCheckBoxMenuItem check=new JCheckBoxMenuItem(Language.tr("Editor.TemplateFilter.OnlyOneGroup"));
		check.setSelected(setup.onlyOneOpenTemplatesGroup);
		popup.add(check);
		check.addActionListener(e->{
			setup.onlyOneOpenTemplatesGroup=!setup.onlyOneOpenTemplatesGroup;
			setup.saveSetup();
			if (setup.onlyOneOpenTemplatesGroup) enforceOnlyOneGroupOpen();
			updateTemplatesFilter();
		});

		popup.show(leftAreaTemplatesFilterButton,0,leftAreaTemplatesFilterButton.getHeight());
	}

	/**
	 * Bewegt das momentan ausgew�hlte Element um eine oder mehrere Stufen in der Zeichenfl�chen-Hierarchie nach vorne
	 * @param max	Wird hier <code>true</code> �bergeben, so wird das Element ganz nach vorne verschoben; ansonsten nur um eine Stufe nach vorne
	 */
	public void moveSelectedElementToFront(final boolean max) {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surfacePanel.moveSelectedElementToFront(max);
	}

	/**
	 * Bewegt das momentan ausgew�hlte Element um eine oder mehrere Stufen in der Zeichenfl�chen-Hierarchie nach hinten
	 * @param max	Wird hier <code>true</code> �bergeben, so wird das Element ganz nach hinten verschoben; ansonsten nur um eine Stufe nach hinten
	 */
	public void moveSelectedElementToBack(final boolean max) {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surfacePanel.moveSelectedElementToBack(max);
	}

	/**
	 * Richtet die ausgew�hlten Element an einer gemeinsamen Oberkante aus.
	 */
	public void alignSelectedElementsTop() {
		surfacePanel.alignSelectedElementsTop();
	}

	/**
	 * Richtet die ausgew�hlten Element so aus, dass sich ihre vertikale Mitte auf derselben H�he befindet.
	 */
	public void alignSelectedElementsMiddle() {
		surfacePanel.alignSelectedElementsMiddle();
	}

	/**
	 * Richtet die ausgew�hlten Element an einer gemeinsamen Unterkante aus.
	 */
	public void alignSelectedElementsBottom() {
		surfacePanel.alignSelectedElementsBottom();
	}

	/**
	 * Richtet die ausgew�hlten Element an einer gemeinsamen linken Kante aus.
	 */
	public void alignSelectedElementsLeft() {
		surfacePanel.alignSelectedElementsLeft();
	}

	/**
	 * Richtet die ausgew�hlten Element so aus, dass sich ihre horizontale Mitte auf derselben Linie befindet.
	 */
	public void alignSelectedElementsCenter() {
		surfacePanel.alignSelectedElementsCenter();
	}

	/**
	 * Richtet die ausgew�hlten Element an einer gemeinsamen rechten Kante aus.
	 */
	public void alignSelectedElementsRight() {
		surfacePanel.alignSelectedElementsRight();
	}

	/**
	 * Gibt an, ob Kanten zu neu eingef�gten Elementen wenn m�glich automatisch hinzugef�gt werden sollen.
	 * @return	Kanten-Hinzuf�ge-Modus
	 */
	public ModelSurfacePanel.ConnectMode getAutoConnect() {
		return surfacePanel.getAutoConnect();
	}

	/**
	 * Stellt ein, ob Kanten zu neu eingef�gten Elementen wenn m�glich automatisch hinzugef�gt werden sollen.
	 * @param autoConnect	Kanten-Hinzuf�ge-Modus
	 */
	public void setAutoConnect(final ModelSurfacePanel.ConnectMode autoConnect) {
		surfacePanel.setAutoConnect(autoConnect);
	}

	private List<ActionListener> selectionListeners=new ArrayList<>();

	/**
	 * F�gt einen Listener hinzu, der benachrichtigt wird, wenn Elemente ausgew�hlt werden
	 * @param selectionListener	Zu benachrichtigender Listener
	 */
	public void addSelectionListener(final ActionListener selectionListener) {
		if (selectionListeners.indexOf(selectionListener)<0) selectionListeners.add(selectionListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer Auswahl von Elementen zu benachrichtigenden Listener
	 * @param selectionListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeSelectionListener(final ActionListener selectionListener) {
		return selectionListeners.remove(selectionListener);
	}

	private void fireSelectionListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"selectionchanged");
		for (ActionListener listener: selectionListeners) listener.actionPerformed(event);
	}

	private List<IntConsumer> linkListeners=new ArrayList<>();

	/**
	 * F�gt einen Listener hinzu, der benachrichtigt wird, wenn ein Zeichenfl�chen-Link angeklickt wird
	 * @param linkListener	Zu benachrichtigender Listener
	 */

	public void addLinkListener(final IntConsumer linkListener) {
		if (linkListeners.indexOf(linkListener)<0) linkListeners.add(linkListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle eins Klicks auf einen Zeichenfl�chen-Link zu benachrichtigenden Listener
	 * @param linkListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeLinkListener(final IntConsumer linkListener) {
		return linkListeners.remove(linkListener);
	}

	private void fireLinkListener(final int link) {
		for (IntConsumer linkListener: linkListeners) linkListener.accept(link);
	}

	/**
	 * Liefert einen direkten Verweis auf die in dem Editor verwendete Zeichenfl�che.<br>
	 * Es k�nnen hier direkt die Elemente inkl. ihrem Selektionsstatus ausgelesen werden. Aber
	 * es d�rfen nicht einfach extern Ver�nderungen vorgenommen werden.
	 * @return	Innerhalb des Editors verwendete Zeichenfl�che
	 */
	public ModelSurface getOriginalSurface() {
		return surfacePanel.getSurface();
	}

	/**
	 * Stellt ein, ob die Lineale sichtbar sein sollen.
	 * @param visible	Lineale sichtbar
	 */
	public void setRulersVisible(final boolean visible) {
		rulerPanel.setRulerVisible(visible);
	}

	/**
	 * Liefert das aktuelle selektierte Element
	 * @return	Aktuell selektiertes Element oder <code>null</code>, falls nichts selektiert ist.
	 */
	public ModelElement getSelectedElement() {
		return surfacePanel.getSelectedElement();
	}

	/**
	 * Liefert eine Liste der Elemente, die �ber eine Bereichsselektion markiert sind.
	 * @param addRegularSelectedElement	Auch die normal ausgew�hlten Objekte hinzuf�gen?
	 * @return	Liste der markierten Elemente (ist nie <code>null</code>, kann aber leer sein)
	 */
	public List<ModelElement> getSelectedArea(final boolean addRegularSelectedElement) {
		return surfacePanel.getSelectedArea(addRegularSelectedElement);
	}

	/**
	 * Liefert das aktuelle selektierte Element. Ist kein Element direkt selektiert,
	 * so wird das erste Element einer Bereichsselektion geliefert.
	 * @return	Aktuell selektiertes Element oder <code>null</code>, falls nichts selektiert ist.
	 */
	public ModelElement getSelectedElementDirectOrArea() {
		final ModelElement element=getSelectedElement();
		if (element!=null) return element;

		final List<ModelElement> list=getSelectedArea(false);
		if (list.size()==1) return list.get(0);

		return null;
	}

	/**
	 * Stellt ein Callback ein, welches im Bedarfsfall (zur Anzeige von Tooltipdaten) ein Statistik-Objekt liefert
	 * @param statisticsGetter	Callback, welches ein Statistik-Objekt liefert (das Callback kann <code>null</code> sein und auch das zur�ckgelieferte Statistik-Objekt kann <code>null</code> sein)
	 */
	public void setStatisticsGetter(final Supplier<Statistics> statisticsGetter) {
		this.statisticsGetter=statisticsGetter;
	}

	private String formatStatisticsData(final String[] lines) {
		if (lines==null || lines.length==0) return null;
		final StringBuilder sb=new StringBuilder();
		sb.append("<i><span style='color: green;'>");
		sb.append(Language.tr("Main.Toolbar.ShowStatistics"));
		sb.append(":<br>");
		sb.append(String.join("<br>",lines));
		sb.append("</span></i>");
		return sb.toString();
	}

	private String formatStatisticsData(final List<String> lines) {
		return formatStatisticsData(lines.toArray(new String[0]));
	}

	private String formatStatisticsData(final String line) {
		return formatStatisticsData(new String[]{line});
	}

	private String formatTime(final double time) {
		return TimeTools.formatExactTime(time)+" ("+NumberTools.formatNumber(time)+")";
	}

	private String getStatisticsInfoForElement(final ModelElementBox element) {
		if (statisticsGetter==null) return null;
		final Statistics statistics=statisticsGetter.get();
		if (statistics==null) return null;

		if (element instanceof ModelElementSource) {
			final String nameStation=RunElement.buildName(element,Language.tr("Simulation.Element.Source.Name"));
			final String nameClient=element.getName();
			final StatisticsDataPerformanceIndicator inter=((StatisticsDataPerformanceIndicator)statistics.clientsInterarrivalTime.getOrNull(nameStation));
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator transfer=((StatisticsDataPerformanceIndicator)statistics.clientsTransferTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator process=((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator residence=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.getOrNull(nameClient));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.getOrNull(nameClient));
			final StatisticsTimePerformanceIndicator nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByClient.getOrNull(nameClient));
			final List<String> lines=new ArrayList<>();
			if (inter!=null) lines.add("E[I]="+formatTime(inter.getMean()));
			lines.add(nameClient);
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
			if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
			if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
			if (nq!=null && nq.getTimeMean()>0) lines.add("E[NQ]="+NumberTools.formatNumber(nq.getTimeMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+NumberTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementAssign) {
			final String nameClient=element.getName();
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator transfer=((StatisticsDataPerformanceIndicator)statistics.clientsTransferTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator process=((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator residence=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.getOrNull(nameClient));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.getOrNull(nameClient));
			final StatisticsTimePerformanceIndicator nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByClient.getOrNull(nameClient));
			final List<String> lines=new ArrayList<>();
			lines.add(nameClient);
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
			if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
			if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
			if (nq!=null && nq.getTimeMean()>0) lines.add("E[NQ]="+NumberTools.formatNumber(nq.getTimeMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+NumberTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementProcess) {
			final String nameStation=RunElement.buildName(element,Language.tr("Simulation.Element.Process.Name"));
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(nameStation));
			final StatisticsDataPerformanceIndicator transfer=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.getOrNull(nameStation));
			final StatisticsDataPerformanceIndicator process=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.getOrNull(nameStation));
			final StatisticsDataPerformanceIndicator residence=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.getOrNull(nameStation));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(nameStation));
			final StatisticsTimePerformanceIndicator nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByStation.getOrNull(nameStation));
			final List<String> lines=new ArrayList<>();
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
			if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
			if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
			if (nq!=null && nq.getTimeMean()>0) lines.add("E[NQ]="+NumberTools.formatNumber(nq.getTimeMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+NumberTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementCounter) {
			final String groupName=((ModelElementCounter)element).getGroupName().replace('-','_')+"-";
			final StatisticsSimpleCountPerformanceIndicator indicator=((StatisticsSimpleCountPerformanceIndicator)statistics.counter.getOrNull(groupName+element.getName()));
			final long value=(indicator==null)?0:indicator.get();
			long sum=0;
			for (String name: statistics.counter.getNames()) {
				if (name.startsWith(groupName)) sum+=((StatisticsSimpleCountPerformanceIndicator)statistics.counter.get(name)).get();
			}
			return formatStatisticsData(NumberTools.formatLong(value)+" ("+NumberTools.formatPercent(((double)value)/sum)+")");
		}

		if (element instanceof ModelElementDifferentialCounter) {
			final StatisticsTimePerformanceIndicator counter=(StatisticsTimePerformanceIndicator)statistics.differentialCounter.getOrNull(element.getName());
			if (counter==null) return null;
			return formatStatisticsData(Language.tr("Statistics.Average")+"="+NumberTools.formatNumber(counter.getTimeMean()));
		}

		if (element instanceof ModelElementThroughput) {
			final StatisticsQuotientPerformanceIndicator indicator=(StatisticsQuotientPerformanceIndicator)statistics.throughputStatistics.get(element.getName());
			if (indicator==null) return null;
			double value=indicator.getQuotient();
			String unit=Language.tr("Statistics.TimeUnit.Second");
			if (value<1) {
				value*=60;
				unit=Language.tr("Statistics.TimeUnit.Minute");
				if (value<1) {
					value*=60;
					unit=Language.tr("Statistics.TimeUnit.Hour");
					if (value<1) {
						value*=24;
						unit=Language.tr("Statistics.TimeUnit.Day");
					}
				}
			}
			return formatStatisticsData(Language.tr("Statistics.Throughput")+" "+name+": "+NumberTools.formatNumber(value,2)+" (1/"+unit+")");
		}

		return null;
	}

	private Set<Consumer<ParameterCompareTemplatesDialog.TemplateRecord>> buildParameterSeriesListeners=new HashSet<>();

	/**
	 * F�gt einen Listener zu der Liste der Listener hinzu, die benachrichtigt werden sollen, wenn der Nutzer per Kontextmen� die Erstellung einer Parameterreihe ausl�st.
	 * @param listener	Zus�tzlicher zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener zu der Liste der zu benachrichtigenden Listener hinzugef�gt werden konnte
	 */
	public boolean addBuildParameterSeriesListener(final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> listener) {
		return buildParameterSeriesListeners.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die benachrichtigt werden sollen, wenn der Nutzer per Kontextmen� die Erstellung einer Parameterreihe ausl�st.
	 * @param listener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener aus der Liste der zu benachrichtigenden Listener entfernt werden konnte
	 */
	public boolean removeBuildParameterSeriesListener(final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> listener) {
		return buildParameterSeriesListeners.remove(listener);
	}

	private void fireBuildParameterSeries(final ParameterCompareTemplatesDialog.TemplateRecord template) {
		buildParameterSeriesListeners.stream().forEach(listener->listener.accept(template));
	}
}