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
package ui.quickaccess;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;

import ui.images.Images;

/**
 * Textfeld mit Platzhalter und Popupmen� gem�� der Eingabe.
 * @author Alexander Herzog
 */
public abstract class JQuickAccessTextField extends JPlaceholderTextField {
	private static final long serialVersionUID = 8844775209475362163L;

	private final PopupMode popupMode;
	private ExecutorService executor;
	private String lastText;
	private JPopupMenu lastMenu;

	/**
	 * Art des QuickAccess-Popupmen�s
	 * @author Alexander Herzog
	 */
	public enum PopupMode {
		/**
		 * Anzeige als Tooltip-Panel (platzsparend, keine Tastatursteuerung)
		 */
		PANEL,

		/**
		 * Anzeige als normales Popupmen� (verbraucht mehr Platz, Tastatursteuerung m�glich)
		 */
		DIRECT
	}

	/**
	 * Konstruktor der Klasse
	 * @param columns	Breite des Eingabefeldes
	 * @param placeholder	Platzhalter Text, der angezeigt wird, wenn das Feld leer ist
	 * @param popupMode	Wie soll das Popupmen� aufgebaut bzw. dargestellt werden?
	 * @see PopupMode
	 */
	public JQuickAccessTextField(final int columns, final String placeholder, final PopupMode popupMode) {
		super(columns);
		this.popupMode=popupMode;

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (lastMenu!=null && lastMenu.isVisible()) {if (processKey(e)) return;}
				if (e.getKeyCode()!=KeyEvent.VK_ESCAPE) {process(); return;}
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {if (SwingUtilities.isLeftMouseButton(e)) process();}
		});

		setPlaceholder(placeholder);

		setMaximumSize(new Dimension(500,1000));
		SwingUtilities.invokeLater(()->{
			final Container c=getParent();
			if (!(c instanceof JMenuBar)) return;
			final Dimension dimension=new Dimension(getWidth(),c.getSize().height-4);
			setMaximumSize(dimension);
			setSize(dimension);
		});
	}

	private List<JQuickAccessRecord> getPopupRecords(String text) {
		if (text==null) return null;
		text=text.trim();
		if (text.isEmpty()) return null;

		/* Liste der Ergebnisse abrufen */
		final List<JQuickAccessRecord> results=getQuickAccessRecords(text);
		if (results.size()==0) return null;

		/* Gruppieren */
		final Map<String,List<JQuickAccessRecord>> map=new HashMap<>();
		results.stream().forEach(record->{
			List<JQuickAccessRecord> list=map.get(record.category);
			if (list==null) map.put(record.category,list=new ArrayList<>());
			list.add(record);
		});

		/* Darzustellende Liste zusammenstellen */
		final String[] groups=map.keySet().stream().sorted().toArray(String[]::new);
		final List<JQuickAccessRecord> data=new ArrayList<>();
		for (String group: groups) {
			final List<JQuickAccessRecord> records=map.get(group);
			if (records==null || records.size()==0) continue;
			data.add(new JQuickAccessRecord(group,null,null,null,null));
			data.addAll(records);
		}

		return data;
	}

	private JPopupMenu getPopupWithPanel(final List<JQuickAccessRecord> data) {
		/* Panel mit den Eintr�gen */
		final JList<JQuickAccessRecord> list=new JList<JQuickAccessRecord>(data.toArray(new JQuickAccessRecord[0]));
		list.setCellRenderer(new QuickAccessListCellRenderer());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final JQuickAccessRecord record=list.getSelectedValue();
				if (record==null || record.text==null) return;
				if (lastMenu!=null) {
					lastMenu.setVisible(false);
					lastMenu=null;
				}
				record.runAction();
			}
		});
		list.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e){
				final JQuickAccessRecord record=list.getModel().getElementAt(list.locationToIndex(e.getPoint()));
				if (record==null || record.text==null || record.action==null) {
					list.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				} else {
					list.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			}
		});
		list.setBackground(SystemColor.info);
		list.setVisibleRowCount(Math.min(20,data.size()));
		final int w=list.getPreferredSize().width;
		list.setPrototypeCellValue(new JQuickAccessRecord("cat","text","textDisplay","tooltip",Images.MODEL.getIcon(),null));
		list.setFixedCellWidth(w);
		final JScrollPane scroll=new JScrollPane(list);
		scroll.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scroll.setBackground(SystemColor.info);

		/* Popupmen� */
		final JPopupMenu menu=new JPopupMenu();
		menu.add(scroll);
		menu.setBackground(SystemColor.info);

		return menu;
	}

	private JPopupMenu getPopupDirect(final List<JQuickAccessRecord> data) {
		final JPopupMenu menu=new JPopupMenu();

		for (JQuickAccessRecord record: data) {
			if (record.textDisplay==null) {
				if (menu.getComponentCount()>0) menu.addSeparator();
				final JMenuItem category=new JMenuItem("<html><body><b>"+record.category+"</b></body></html>");
				category.setEnabled(false);
				menu.add(category);
				continue;
			}

			final JMenuItem item=new JMenuItem(record.textDisplay,record.icon);
			item.addActionListener(e->record.action.accept(record));
			item.setToolTipText(record.tooltip);
			menu.add(item);
		}

		return menu;
	}

	private QuickAccessRunner lastRunner;

	private void closePopup() {
		if (lastMenu==null) return;
		lastMenu.setVisible(false);
		lastMenu=null;
	}

	private boolean processKey(final KeyEvent e) {
		if (e.getKeyCode()==KeyEvent.VK_ESCAPE) {
			closePopup();
			return true;
		}

		if (e.getKeyCode()==KeyEvent.VK_DOWN || e.getKeyCode()==KeyEvent.VK_UP) {
			lastMenu.requestFocus();
			return true;
		}

		if (e.getKeyCode()==KeyEvent.VK_ENTER) {
			processEnter();
			return true;
		}

		return false;
	}

	private void process() {
		if (lastText!=null && getText().equals(lastText) && lastMenu!=null && lastMenu.isVisible()) return;
		lastText=getText();

		if (lastRunner!=null) {
			lastRunner.cancel();
			lastRunner=null;
		}
		lastRunner=new QuickAccessRunner(lastText);

		if (executor==null) executor=new ThreadPoolExecutor(0,Integer.MAX_VALUE,5L,TimeUnit.SECONDS,new SynchronousQueue<Runnable>(),new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r,"QuickAccess");
			}
		});
		executor.execute(lastRunner);
	}

	private void processEnter() {
		final MenuElement[] path=MenuSelectionManager.defaultManager().getSelectedPath();
		if (path!=null && path.length>0 && path[path.length-1] instanceof JMenuItem) {
			processMenuItemAction((JMenuItem)path[path.length-1]);
		} else {
			if (lastMenu==null || !lastMenu.isVisible()) return;
			if (lastMenu.getComponentCount()!=2) return;
			if (!(lastMenu.getComponent(1) instanceof JMenuItem)) return;
			processMenuItemAction((JMenuItem)lastMenu.getComponent(1));
		}
	}

	private void processMenuItemAction(final JMenuItem item) {
		if (item==null) return;
		final ActionEvent event=new ActionEvent(lastMenu,1,"click");
		SwingUtilities.invokeLater(()->{
			for (ActionListener listener: item.getActionListeners()) listener.actionPerformed(event);
		});
		closePopup();
	}

	private class QuickAccessRunner implements Runnable {
		private final String text;
		private volatile boolean canceled;
		public QuickAccessRunner(final String text) {
			this.text=text;
			canceled=false;
		}

		public void cancel() {
			canceled=true;
		}

		@Override
		public void run() {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				if (canceled) return;
			}

			final List<JQuickAccessRecord> data=getPopupRecords(text);

			final JPopupMenu menu;
			if (data==null) {
				menu=null;
			} else {
				switch (popupMode) {
				case DIRECT: menu=getPopupDirect(data); break;
				case PANEL: menu=getPopupWithPanel(data); break;
				default: menu=null; break;
				}
			}

			if (canceled) return;
			if (menu==null) {closePopup(); return;}


			/* Popupmen� */
			menu.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_ENTER) processEnter();
				}
			});
			synchronized (JQuickAccessTextField.this) {
				if (popupMode==PopupMode.PANEL) menu.setFocusable(false);
				menu.show(JQuickAccessTextField.this,0,getHeight());
				if (popupMode==PopupMode.PANEL) menu.setFocusable(true);
				closePopup();
				requestFocus();
				lastMenu=menu;
			}
		}
	}

	/**
	 * Stellt die Eintr�ge f�r das QuickAccess-Men� zusammen
	 * @param quickAccessText	Eingegebener Text
	 * @return	Liste mit den Treffern
	 */
	public abstract List<JQuickAccessRecord> getQuickAccessRecords(final String quickAccessText);

	private static class QuickAccessListCellRenderer extends JLabel implements ListCellRenderer<JQuickAccessRecord> {
		private static final long serialVersionUID = 2375121310093542345L;

		@Override
		public Component getListCellRendererComponent(JList<? extends JQuickAccessRecord> list, JQuickAccessRecord value, int index, boolean isSelected, boolean cellHasFocus) {
			setIcon(null);
			setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
			setToolTipText(value.tooltip);

			if (value.text==null) {
				setText("<html><body><span style='color: gray;'><b><u>"+value.category+"</b></u></span></body></html>");
			} else {
				if (value.icon!=null) setIcon(value.icon);
				setText(value.textDisplay);
			}

			return this;
		}
	}
}
