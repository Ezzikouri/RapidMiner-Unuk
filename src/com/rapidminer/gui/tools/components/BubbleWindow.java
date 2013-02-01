/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.tools.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedHTMLJEditorPane;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tour.Step;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;
import com.sun.awt.AWTUtilities;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.event.DockingActionEvent;
import com.vlsolutions.swing.docking.event.DockingActionListener;

/**
 * This class creates a speech bubble-shaped JDialog, which can be attache to
 * Buttons, either by using its ID or by passing a reference. 
 * The bubble triggers two events which are obserable by the {@link BubbleListener};
 * either if the close button was clicked, or if the corresponding button was used.
 * The keys for the title and the text must be of format gui.bubble.XXX.body or gui.bubble.XXX.title .
 * 
 * @author Philipp Kersting and Thilo Kamradt
 *
 */

public class BubbleWindow extends JDialog {

	private static final long serialVersionUID = -6369389148455099450L;

	public static interface BubbleListener {

		public void bubbleClosed(BubbleWindow bw);

		public void actionPerformed(BubbleWindow bw);
	}

	private List<BubbleListener> listeners = new LinkedList<BubbleListener>();

	/** Used to define the position of the pointer of the bubble 
	 * (Describes the corner which points to the component).
	 * CENTER places the Bubble inside the Component. MIDDLE places the BubbleWindow in the middle of the mainframe( won't be checked by the BubbleWindow if chosen).
	 */
	public enum Alignment {
		TOPLEFT, TOPRIGHT, BOTTOMLEFT, BOTTOMRIGHT, LEFTTOP, LEFTBOTTOM, RIGHTTOP, RIGHTBOTTOM, INNERRIGHT, INNERLEFT, MIDDLE;
	}

	private static RenderingHints HI_QUALITY_HINTS = new RenderingHints(null);
	static {
		HI_QUALITY_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		HI_QUALITY_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	private static final int CORNER_RADIUS = 20;
	private static final int WINDOW_WIDTH = 200;

	/** Shape used for setting the shape of the window and for rendering the outline. */
	private Shape shape;
	private Alignment alignment;
	private JPanel bubble;
	private JButton close;
	private ImageIcon passiveIcon, activeIcon;
	private ActionListener listener;
	
	private String title;
	private String text;
	private String dockKey = null;

	private ComponentAdapter movementListener;
	private AbstractButton button = null;
	private Component component;
	private Component componentToBeWatched = null;
	private WindowAdapter windowListener;
	private Window owner;
	private ComponentListener compListener;
	private DockingActionListener dockListener = null;
	private final DockingDesktop desktop = RapidMinerGUI.getMainFrame().getDockingDesktop();
	private int dockingCounter;

	/**
	 * @param owner the {@link Window} on which this {@link BubbleWindow} should be shown.
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable
	 * @param i18nKey of the message which should be shown
	 * @param buttonKeyToAttach i18nKey of the Button to which this {@link BubbleWindow} should be placed relative to. 
	 */
	public BubbleWindow(Window owner, String nextDockableKey, final Alignment preferedAlignment, String i18nKey, String buttonKeyToAttach, Object ... arguments) {
		this(owner, nextDockableKey, preferedAlignment, i18nKey, buttonKeyToAttach, true, arguments);
	}

	/**
	 * @param owner the {@link Window} on which this {@link BubbleWindow} should be shown.
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable
	 * @param i18nKey of the message which should be shown
	 * @param buttonKeyToAttach i18nKey of the Button to which this {@link BubbleWindow} should be placed relative to. 
	 * @param addListener indicates whether the {@link BubbleWindow} closes if the Button was pressed or when another Listener added by a subclass of {@link Step} is fired.
	 */
	public BubbleWindow(Window owner, String nextDockableKey, final Alignment preferedAlignment, String i18nKey, String buttonKeyToAttach, boolean addListener, Object ... arguments) {
		this(owner, nextDockableKey, preferedAlignment, i18nKey, (Component) findButton(buttonKeyToAttach, RapidMinerGUI.getMainFrame().getWindow()), arguments);
		this.button = findButton(buttonKeyToAttach, RapidMinerGUI.getMainFrame().getWindow());
		this.unregisterMovementListener();
		this.registerMovementListener();
		if (addListener) {
			this.attachToButton(button);
		}
	}

	/**
	 * @param owner the {@link Window} on which this {@link BubbleWindow} should be shown.
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable
	 * @param i18nKey of the message which should be shown
	 * @param buttonToAttach {@link AbstractButton} to which this {@link BubbleWindow} should be placed relative to. 
	 */
	public BubbleWindow(Window owner, String nextDockableKey, final Alignment preferedAlignment, String i18nKey, AbstractButton buttonToAttach, Object ...arguments ) {
		this(owner, nextDockableKey, preferedAlignment, i18nKey, buttonToAttach, true, arguments);
	}

	/**
	 * @param owner the {@link Window} on which this {@link BubbleWindow} should be shown.
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable.
	 * @param i18nKey of the message which should be shown
	 * @param buttonToAttach {@link AbstractButton} to which this {@link BubbleWindow} should be placed relative to. 
	 * @param addListener indicates whether the {@link BubbleWindow} closes if the Button was pressed or when another Listener added by a subclass of {@link Step} is fired.
	 */
	public BubbleWindow(Window owner, String nextDockableKey, final Alignment preferedAlignment, String i18nKey, AbstractButton buttonToAttach, boolean addListener, Object ...arguments) {
		this(owner, nextDockableKey, preferedAlignment, i18nKey, (Component) buttonToAttach, arguments);
		this.button = buttonToAttach;
		this.unregisterMovementListener();
		this.registerMovementListener();
		if (addListener) {
			this.attachToButton(buttonToAttach);
		}
	}

	/**
	 * @param owner the {@link Window} on which this {@link BubbleWindow} should be shown.
	 * @param preferredAlignment offer for alignment but the Class will calculate by itself whether the position is usable.
	 * @param i18nKey of the message which should be shown
	 * @param ToAttach {@link Component} to which this {@link BubbleWindow} should be placed relative to. 
	 * @param addListener indicates whether the {@link BubbleWindow} closes if the Button was pressed or when another Listener added by a subclass of {@link Step} is fired.
	 */
	public BubbleWindow(Window owner, String nextDockableKey, final Alignment preferredAlignment, String i18nKey, Component toAttach, Object ...arguments ) {
		super(owner);
		this.dockingCounter = 0;
		this.owner = owner;
		this.dockKey = nextDockableKey;	
		this.component = toAttach;
		if (toAttach == null &&  !(Alignment.MIDDLE == preferredAlignment))
			throw new IllegalArgumentException("the given Component is null !!!");
		this.alignment = this.calculateAlignment(preferredAlignment, toAttach);
		setLayout(new BorderLayout());
		setUndecorated(true);

		title = I18N.getGUIBundle().getString("gui.bubble." + i18nKey + ".title");
		text = I18N.getMessage(I18N.getGUIBundle(), "gui.bubble." + i18nKey + ".body", arguments);

		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				shape = createShape(alignment);
				
				String version = System.getProperty("java.version").substring(0, 3).replace(".", "");
				try {
					Integer versionNumber = Integer.valueOf(version);
					if (versionNumber == 16) {
						// Java SE 6 Update 10
						AWTUtilities.setWindowShape(BubbleWindow.this, shape);
					} else if (versionNumber >= 17) {
						// Java 7+
						setShape(shape);
					}					
				} catch (Throwable t) {
					LogService.getRoot().log(Level.WARNING, "Could not create shaped Bubble Windows. Error: " + t.getLocalizedMessage(), t);
				}
			}
		});

		GridBagLayout gbl = new GridBagLayout();
		bubble = new JPanel(gbl) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics gr) {
				super.paintComponent(gr);
				Graphics2D g = (Graphics2D) gr;
				g.setColor(SwingTools.LIGHT_BROWN_FONT_COLOR);
				g.setStroke(new BasicStroke(3));
				g.setRenderingHints(HI_QUALITY_HINTS);
				g.draw(AffineTransform.getTranslateInstance(-.5, -.5).createTransformedShape(getShape()));
			}
		};
		bubble.setBackground(SwingTools.LIGHTEST_BLUE);
		bubble.setSize(getSize());
		getContentPane().add(bubble, BorderLayout.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		Insets insetsLabel = new Insets(10, 10, 10, 10);
		Insets insetsMainText = new Insets(0, 10, 10, 10);
		switch (alignment) {
			case TOPLEFT:
				insetsLabel = new Insets(CORNER_RADIUS + 15, 10, 10, 10);
				break;
			case TOPRIGHT:
				insetsLabel = new Insets(CORNER_RADIUS + 15, 10, 10, 10);
				break;
			case INNERLEFT:
			case LEFTTOP:
				insetsLabel = new Insets(10, CORNER_RADIUS + 15, 10, 10);
				insetsMainText = new Insets(0, CORNER_RADIUS + 15, 10, 10);
				break;
			case LEFTBOTTOM:
				insetsLabel = new Insets(10, CORNER_RADIUS + 15, 10, 10);
				insetsMainText = new Insets(0, CORNER_RADIUS + 15, 10, 10);
				break;
			case BOTTOMRIGHT:
				insetsLabel = new Insets(10, 10, 10, 10);
				insetsMainText = new Insets(0, 10, CORNER_RADIUS + 15, 10);
				break;
			case BOTTOMLEFT:
				insetsLabel = new Insets(10, 10, 10, 10);
				insetsMainText = new Insets(0, 10, CORNER_RADIUS + 15, 10);
				break;
			case INNERRIGHT:
			case RIGHTTOP:
				insetsLabel = new Insets(10, 10, 10, CORNER_RADIUS + 15);
				insetsMainText = new Insets(0, 10, 10, CORNER_RADIUS + 15);
				break;
			case RIGHTBOTTOM:
				insetsLabel = new Insets(10, 10, 10, CORNER_RADIUS + 15);
				insetsMainText = new Insets(0, 10, 10, CORNER_RADIUS + 15);
				break;
			default:
		}
		c.insets = insetsLabel;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		JLabel label = new JLabel(title);
		bubble.add(label, c);
		label.setMinimumSize(new Dimension(WINDOW_WIDTH, 12));
		label.setPreferredSize(new Dimension(WINDOW_WIDTH, 12));
		label.setFont(label.getFont().deriveFont(Font.BOLD));

		c.weightx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = insetsLabel;
		passiveIcon = new ImageIcon(DockingDesktop.class.getResource("/com/vlsolutions/swing/docking/close16v2.png"));
		activeIcon = new ImageIcon(DockingDesktop.class.getResource("/com/vlsolutions/swing/docking/close16v2rollover.png"));
		close = new JButton(passiveIcon);
		close.setBorderPainted(false);
		close.setOpaque(false);
		// change Icons and set close operation
		close.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// don't care
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				//don't care
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				BubbleWindow.this.close.setIcon(BubbleWindow.this.passiveIcon);
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				BubbleWindow.this.close.setIcon(BubbleWindow.this.activeIcon);
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				BubbleWindow.this.dispose();
				fireEventCloseClicked();
				unregister();
			}
		});
		close.setMargin(new Insets(0, 5, 0, 5));
		bubble.add(close, c);

		ExtendedHTMLJEditorPane mainText = new ExtendedHTMLJEditorPane("text/html", "<div style=\"width:" + WINDOW_WIDTH + "px\">" + text + "</div>");
		mainText.setMargin(new Insets(0, 0, 0, 0));
		mainText.installDefaultStylesheet();
		mainText.setOpaque(false);
		mainText.setEditable(false);
		mainText.setFont(mainText.getFont().deriveFont(Font.PLAIN));
		mainText.setMinimumSize(new Dimension(150, 20));
		mainText.setMaximumSize(new Dimension(WINDOW_WIDTH, 800));
		c.insets = insetsMainText;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 1;
		bubble.add(mainText, c);
		
		pack();

		positionRelative();
	}
	
	private void paintAgain() {
		alignment = this.calculateAlignment(alignment, component);
		shape = createShape(alignment);
		//choose the right call for the right version
		String version = System.getProperty("java.version").substring(0, 3).replace(".", "");
		try {
			Integer versionNumber = Integer.valueOf(version);
			if (versionNumber == 16) {
				// Java SE 6 Update 10
				AWTUtilities.setWindowShape(this, shape);
			} else if (versionNumber >= 17) {
				// Java 7+
				setShape(shape);
			}
		} catch (Throwable t) {
			LogService.getRoot().log(Level.WARNING, "Could not create shaped Bubble Windows. Error: " + t.getLocalizedMessage(), t);
		}

		bubble.removeAll();
		Insets insetsLabel = new Insets(10, 10, 10, 10);
		Insets insetsMainText = new Insets(0, 10, 10, 10);
		switch (alignment) {
			case TOPLEFT:
				insetsLabel = new Insets(CORNER_RADIUS + 15, 10, 10, 10);
				break;
			case TOPRIGHT:
				insetsLabel = new Insets(CORNER_RADIUS + 15, 10, 10, 10);
				break;
			case INNERLEFT:
			case LEFTTOP:
				insetsLabel = new Insets(10, CORNER_RADIUS + 15, 10, 10);
				insetsMainText = new Insets(0, CORNER_RADIUS + 15, 10, 10);
				break;
			case LEFTBOTTOM:
				insetsLabel = new Insets(10, CORNER_RADIUS + 15, 10, 10);
				insetsMainText = new Insets(0, CORNER_RADIUS + 15, 10, 10);
				break;
			case BOTTOMRIGHT:
				insetsLabel = new Insets(10, 10, 10, 10);
				insetsMainText = new Insets(0, 10, CORNER_RADIUS + 15, 10);
				break;
			case BOTTOMLEFT:
				insetsLabel = new Insets(10, 10, 10, 10);
				insetsMainText = new Insets(0, 10, CORNER_RADIUS + 15, 10);
				break;
			case INNERRIGHT:
			case RIGHTTOP:
				insetsLabel = new Insets(10, 10, 10, CORNER_RADIUS + 15);
				insetsMainText = new Insets(0, 10, 10, CORNER_RADIUS + 15);
				break;
			case RIGHTBOTTOM:
				insetsLabel = new Insets(10, 10, 10, CORNER_RADIUS + 15);
				insetsMainText = new Insets(0, 10, 10, CORNER_RADIUS + 15);
				break;
			default:
		}
		GridBagConstraints c = new GridBagConstraints();
		//add headline
		c.insets = insetsLabel;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		JLabel label = new JLabel(title);
		bubble.add(label, c);
		label.setMinimumSize(new Dimension(WINDOW_WIDTH, 12));
		label.setPreferredSize(new Dimension(WINDOW_WIDTH, 12));
		label.setFont(label.getFont().deriveFont(Font.BOLD));

		//add close-Button
		c.weightx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = insetsLabel;
		bubble.add(close, c);

		//add main text
		ExtendedHTMLJEditorPane mainText = new ExtendedHTMLJEditorPane("text/html", "<div style=\"width:" + WINDOW_WIDTH + "px\">" + text + "</div>");
		mainText.setMargin(new Insets(0, 0, 0, 0));
		mainText.installDefaultStylesheet();
		mainText.setOpaque(false);
		mainText.setEditable(false);
		mainText.setFont(mainText.getFont().deriveFont(Font.PLAIN));
		mainText.setMinimumSize(new Dimension(150, 20));
		mainText.setMaximumSize(new Dimension(WINDOW_WIDTH, 800));
		c.insets = insetsMainText;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		c.weighty = 1;
		bubble.add(mainText, c);
		
		
		pack();
		
		pointAtComponent();
	}

	/**
	 * 
	 * Adds a {@link BubbleListener}.
	 * 
	 * @param l The listener
	 */
	public void addBubbleListener(BubbleListener l) {
		listeners.add(l);
	}

	/**
	 * removes the given {@link BubbleListener}.
	 * @param l {@link BubbleListener} to remove.
	 */
	public void removeBubbleListener(BubbleListener l) {
		listeners.remove(l);
	}

	/**
	 * Creates a speech bubble-shaped Shape.
	 * 
	 * @param alignment The alignment of the pointer.
	 * 
	 * @return A speech-bubble <b>Shape</b>.
	 */
	public Shape createShape(Alignment alignment) {
		int w = getSize().width - 2 * CORNER_RADIUS;
		int h = getSize().height - 2 * CORNER_RADIUS;
		int o = CORNER_RADIUS;

		GeneralPath gp = new GeneralPath();
		switch (alignment) {
			case TOPLEFT:
				gp.moveTo(0, 0);
				gp.lineTo(0, h + o);
				gp.quadTo(0, h + (2 * o), o, h + (2 * o));
				gp.lineTo(w + o, h + (2 * o));
				gp.quadTo(w + (2 * o), h + (2 * o), w + (2 * o), h + o);
				gp.lineTo(w + (2 * o), (2 * o));
				gp.quadTo(w + (2 * o), o, w + o, o);
				gp.lineTo(o, o);
				gp.lineTo(0, 0);
				break;
			case TOPRIGHT:
				gp.moveTo(0, 2 * o);
				gp.lineTo(0, h + o);
				gp.quadTo(0, h + (2 * o), o, h + (2 * o));
				gp.lineTo(w + o, h + (2 * o));
				gp.quadTo(w + (2 * o), h + (2 * o), w + (2 * o), h + o);
				gp.lineTo(w + (2 * o), 0);
				gp.lineTo((w + o), o);
				gp.lineTo(o, o);
				gp.quadTo(0, o, 0, (2 * o));
				break;
			case BOTTOMLEFT:
				gp.moveTo(0, o);
				gp.lineTo(0, h + (2 * o));
				gp.lineTo(o, h + o);
				gp.lineTo(w + o, h + o);
				gp.quadTo(w + (2 * o), h + o, w + (2 * o), h);
				gp.lineTo(w + (2 * o), o);
				gp.quadTo(w + (2 * o), 0, w + o, 0);
				gp.lineTo(o, 0);
				gp.quadTo(0, 0, 0, o);
				break;
			case BOTTOMRIGHT:
				gp.moveTo(0, o);
				gp.lineTo(0, h);
				gp.quadTo(0, (h + o), o, (h + o));
				gp.lineTo(w + o, (h + o));
				gp.lineTo(w + (2 * o), h + (2 * o));
				gp.lineTo(w + (2 * o), o);
				gp.quadTo(w + (2 * o), 0, w + o, 0);
				gp.lineTo(o, 0);
				gp.quadTo(0, 0, 0, o);
				break;
			case LEFTBOTTOM:
				gp.moveTo(0, h + (2 * o));
				gp.lineTo(w + o, h + (2 * o));
				gp.quadTo(w + (2 * o), h + (2 * o), w + (2 * o), h + o);
				gp.lineTo(w + (2 * o), o);
				gp.quadTo(w + (2 * o), 0, w + o, 0);
				gp.lineTo((2 * o), 0);
				gp.quadTo(o, 0, o, o);
				gp.lineTo(o, h + o);
				gp.closePath();
				break;
			case INNERLEFT:
			case LEFTTOP:
				gp.moveTo(0, 0);
				gp.lineTo(o, o);
				gp.lineTo(o, (h + o));
				gp.quadTo(o, h + (2 * o), (2 * o), h + (2 * o));
				gp.lineTo(w + o, h + (2 * o));
				gp.quadTo(w + (2 * o), h + (2 * o), w + (2 * o), h + o);
				gp.lineTo(w + (2 * o), o);
				gp.quadTo(w + (2 * o), 0, w + o, 0);
				gp.lineTo(0, 0);
				break;
			case RIGHTBOTTOM:
				gp.moveTo(0, h + o);
				gp.quadTo(0, h + (2 * o), o, h + (2 * o));
				gp.lineTo(w + (2 * o), h + (2 * o));
				gp.lineTo(w + o, h + o);
				gp.lineTo(w + o, o);
				gp.quadTo(w + o, 0, w, 0);
				gp.lineTo(o, 0);
				gp.quadTo(0, 0, 0, o);
				gp.lineTo(0, h + o);
				break;
			case INNERRIGHT:
			case RIGHTTOP:
				gp.moveTo(o, 0);
				gp.quadTo(0, 0, 0, o);
				gp.lineTo(0, (h + o));
				gp.quadTo(0, h + (2 * o), o, h + (2 * o));
				gp.lineTo(w, h + (2 * o));
				gp.quadTo((w + o), h + (2 * o), (w + o), (h + o));
				gp.lineTo((w + o), o);
				gp.lineTo(w + (2 * o), 0);
				gp.lineTo(o, 0);
				break;
			case MIDDLE:
				gp.moveTo(o, 0);
				gp.quadTo(0, 0, 0, o);
				gp.lineTo(0, (h + o));
				gp.quadTo(0, h + (2 * o), o, h + (2 * o));
				gp.lineTo(w + o, h + (2*o));
				gp.quadTo(w + (2*o), h + (2*o), w + (2*o), h + o);
				gp.lineTo(w + (2*o), o);
				gp.quadTo(w + (2*o), 0, w + o, 0);
				gp.lineTo(o, 0);
				break;
			default:
		}
		AffineTransform tx = new AffineTransform();
		return gp.createTransformedShape(tx);
	}

	/**
	 * places the {@link BubbleWindow} relative to the Component which was given.
	 */
	private void positionRelative() {

		pointAtComponent();

		registerMovementListener();
	}

	/**
	 * places the Bubble-speech so that it points to the Component 
	 * @param component component to point to
	 */
	private void pointAtComponent() {
		double targetx = 0;
		double targety = 0;
		Point target = new Point(0, 0);
		if(alignment == Alignment.MIDDLE) {
				targetx = owner.getWidth()*0.5 - getWidth()*0.5;
				targety = owner.getHeight()*0.5 - getHeight()*0.5;
		} else {
			int x = (int) this.component.getLocationOnScreen().getX();
			int y = (int) this.component.getLocationOnScreen().getY();
			int h = this.component.getHeight();
			int w = this.component.getWidth();
			switch (alignment) {
				case TOPLEFT:
					targetx = x + 0.5 * w;
					targety = y + h;
					break;
				case TOPRIGHT:
					targetx = (x + 0.5 * w) - getWidth();
					targety = y + h;
					break;
				case LEFTBOTTOM:
					targetx = x + w;
					targety = (y + 0.5 * h) - getHeight();
					break;
				case LEFTTOP:
					targetx = x + w;
					targety = (y + 0.5 * h);
					break;
				case RIGHTBOTTOM:
					targetx = x - getWidth();
					targety = (y + 0.5 * h) - getHeight();
					break;
				case RIGHTTOP:
					targetx = x - getWidth();
					targety = (y + 0.5 * h);
					break;
				case BOTTOMLEFT:
					targetx = x + 0.5 * w;
					targety = y - getHeight();
					break;
				case BOTTOMRIGHT:
					targetx = x + 0.5 * w - getWidth();
					targety = y - getHeight();
					break;
				case INNERLEFT:
					targetx = x + w - 0.5*getWidth();
					double xShift = (targetx + getWidth()) - (owner.getX() + owner.getWidth());
					if( xShift > 0) {
						targetx -= xShift;
					} 
					targety = y + h - 0.5*getHeight();
					double yShift = (targety + getHeight()) -(owner.getY() + owner.getHeight());
					if( yShift > 0) {
						targetx -= yShift;
					} 
					break;
				case INNERRIGHT:
					targetx = x - 0.5*getWidth();
					xShift = owner.getX() - targetx;
					if( xShift > 0) {
						targetx += xShift;
					} 
					targety = y + h - 0.5*getHeight();
					yShift = (targety + getHeight()) -(owner.getY() + owner.getHeight());
					if( yShift > 0) {
						targetx -= yShift;
					} 
				default:
			}
		}
		
		target = new Point((int) Math.round(targetx), (int) Math.round(targety));
		setLocation(target);
	}

	/**
	 * method to find a dockable component on the MainFrame
	 * @param dockableKey key of the dockable you want to find
	 * @return the {@link Component} with the given key will be returned or an Exception will be thrown if the dockable was not found.
	 */
	public static Component getDockableByKey(String dockableKey) {
		DockableState[] dockables = RapidMinerGUI.getMainFrame().getDockingDesktop().getDockables();
		for (DockableState ds : dockables) {
			if (ds.getDockable().getDockKey().getKey().equals(dockableKey) && !ds.isClosed()) {
				return ds.getDockable().getComponent().getParent().getParent();
			}
		}
		return null;
	}
	
	/**
	 * method to get to know whether the dockable with the given key is on Screen
	 * @param dockableKey i18nKey of the wanted Dockable
	 * @return returns 1 if the Dockable is on the Screen and -1 if the Dockable is not on the Screen. 
	 */
	public static int isDockableOnScreen(String dockableKey) {
		Component onScreen = BubbleWindow.getDockableByKey(dockableKey);
		if(onScreen == null)
			return -1;
		return 1;
		
	}
	
	/**
	 * method to get to know whether the AbstractButton with the given key is on Screen
	 * @param dockableKey i18nKey of the wanted AbstractButton
	 * @return returns 1 if the AbstractButton is on the Screen, 0 if the AbstractButton is on Screen but the user can not see it with the current settings of the perspective and -1 if the AbstractButton is not on the Screen. 
	 */
	public static int isButtonOnScreen(String buttonKey) {
		// find the Button and return -1 if we can not find it
		Component onScreen; 
		try { onScreen = BubbleWindow.findButton(buttonKey, RapidMinerGUI.getMainFrame().getWindow()); }
		catch (NullPointerException e) {
			return -1;
		}
		if(onScreen == null)
			return -1;
		// detect whether the Button is viewable
		int xposition = onScreen.getLocationOnScreen().x;
		int yposition = onScreen.getLocationOnScreen().y;
		int otherXposition = xposition + onScreen.getWidth();
		int otherYposition = yposition + onScreen.getHeight();
		Window frame = RapidMinerGUI.getMainFrame().getWindow();
		if(otherXposition <= frame.getWidth() && otherYposition <= frame.getHeight() && xposition > 0 && yposition > 0) {
			return 1;
		} else {
			return 0;
		}
	}

	/** Positions the window such that the pointer points to the given button. 
	 *  In addition to that, adds an {@link ActionListener} to the button which
	 *  closes the BubbleWindow as soon as the button is pressed and one that 
	 *  makes sure that the pointer always points at the right position. */
	private void attachToButton(AbstractButton button) {
		this.button = button;
		this.component = button;
		listener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BubbleWindow.this.dispose();
				fireEventActionPerformed();
				unregister();
			}
		};
		button.addActionListener(listener);

	}

	/**
	 * @param name i18nKey of the Button
	 * @param searchRoot {@link Component} to search in for the Button
	 * @return returns the {@link AbstractButton} if found or null if the Button was not found. 
	 */
	public static AbstractButton findButton(String name, Component searchRoot) {
		if (searchRoot instanceof AbstractButton) {

			AbstractButton b = (AbstractButton) searchRoot;
			if (b.getAction() instanceof ResourceAction) {
				String id = (String) b.getAction().getValue("rm_id");
				if (name.equals(id)) {
					return b;
				}
			}
		}
		if (searchRoot instanceof Container) {
			Component[] all = ((Container) searchRoot).getComponents();
			for (Component child : all) {
				AbstractButton result = findButton(name, child);
				if (result != null) {
					return result;

				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link Shape} of this {@link BubbleWindow}
	 */
	public Shape getShape() {
		if (shape == null) {
			shape = createShape(alignment);
		}
		return shape;
	}

	private void registerMovementListener() {
		movementListener = new ComponentAdapter() {

			@Override
			public void componentMoved(ComponentEvent e) {
				if(BubbleWindow.this.alignment.equals(BubbleWindow.this.calculateAlignment(alignment, component))) {
					BubbleWindow.this.pointAtComponent();
				} else {
					BubbleWindow.this.paintAgain();
				}
				BubbleWindow.this.setVisible(true);
			}

			@Override
			public void componentResized(ComponentEvent e) {
				if(BubbleWindow.this.alignment.equals(BubbleWindow.this.calculateAlignment(alignment, component))) {
					BubbleWindow.this.pointAtComponent();
				} else {
					BubbleWindow.this.paintAgain();
				}
				BubbleWindow.this.setVisible(true);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				BubbleWindow.this.pointAtComponent();
				BubbleWindow.this.setVisible(true);
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				BubbleWindow.this.setVisible(false);
			}

		};
		windowListener = new WindowAdapter() {

			@Override
			public void windowIconified(WindowEvent e) {
				super.windowIconified(e);
				BubbleWindow.this.setVisible(false);
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				super.windowDeiconified(e);
				BubbleWindow.this.pointAtComponent();
				BubbleWindow.this.setVisible(true);
			}

		};
		if(BubbleWindow.this.component != null) {
			compListener = new ComponentListener() {
			
				@Override
				public void componentShown(ComponentEvent e) {
					BubbleWindow.this.pointAtComponent();
					BubbleWindow.this.setVisible(true);
				}
			
				@Override
				public void componentResized(ComponentEvent e) {
					if(BubbleWindow.this.alignment.equals(BubbleWindow.this.calculateAlignment(alignment, component))) {
						BubbleWindow.this.pointAtComponent();
					} else {
						BubbleWindow.this.paintAgain();
					}
					BubbleWindow.this.setVisible(true);
				}
			
				@Override
				public void componentMoved(ComponentEvent e) {
					if(BubbleWindow.this.alignment.equals(BubbleWindow.this.calculateAlignment(alignment, component))) {
						BubbleWindow.this.pointAtComponent();
					} else {
						BubbleWindow.this.paintAgain();
					}
					BubbleWindow.this.setVisible(true);
				}
			
				@Override
				public void componentHidden(ComponentEvent e) {
					BubbleWindow.this.setVisible(false);
				}
			};
		
			if(BubbleWindow.this.button != null && dockKey != null) {
				BubbleWindow.this.componentToBeWatched = BubbleWindow.getDockableByKey(BubbleWindow.this.dockKey);
			} else {
				BubbleWindow.this.componentToBeWatched = BubbleWindow.this.component;
			}
			BubbleWindow.this.componentToBeWatched.addComponentListener(compListener);
			if(dockKey != null && !dockKey.equals("")) {
				dockListener = new DockingActionListener() {
					
					@Override
					public void dockingActionPerformed(DockingActionEvent event) {
						// actionType 5 indicates that the Dockable was docked to another position
						// actionType 3 indicates that the Dockable has created his own position
						// actionType 6 indicates that the Dockable was separated
						if(event.getActionType() == 5 || event.getActionType() == 3) {
							if((++dockingCounter)%2 == 0) {
								//get the new component of the Dockable because the current component is disabled
								BubbleWindow.this.componentToBeWatched.removeComponentListener(compListener);
								componentToBeWatched = BubbleWindow.getDockableByKey(dockKey);
								BubbleWindow.this.componentToBeWatched.addComponentListener(compListener);
								// the reference of the button does not changes
								if(button == null)
									component = componentToBeWatched;
								//repaint
								BubbleWindow.this.paintAgain();
								BubbleWindow.this.setVisible(true);
							}
						}
						if(event.getActionType() == 6) {
							//get the new component of the Dockable because the current component is disabled
							BubbleWindow.this.componentToBeWatched.removeComponentListener(compListener);
							componentToBeWatched = BubbleWindow.getDockableByKey(dockKey);
							BubbleWindow.this.componentToBeWatched.addComponentListener(compListener);
							// the reference of the button does not changes
							if(button == null)
								component = componentToBeWatched;
							//repaint
							BubbleWindow.this.paintAgain();
							BubbleWindow.this.setVisible(true);
						}
					}
					
					@Override
					public boolean acceptDockingAction(DockingActionEvent arg0) {
						// no need to deny anything
						return true;
					}
				};
				desktop.addDockingActionListener(dockListener);
				
			}
		}
		RapidMinerGUI.getMainFrame().getWindow().addComponentListener(movementListener);
		RapidMinerGUI.getMainFrame().getWindow().addWindowStateListener(windowListener);
	}

	private void unregister() {
		if ((listener != null) && (button != null)) {
			button.removeActionListener(listener);
		}
	}
	
	private void unregisterMovementListener() {
		RapidMinerGUI.getMainFrame().getWindow().removeComponentListener(movementListener);
		RapidMinerGUI.getMainFrame().getWindow().removeWindowStateListener(windowListener);
		if(component != null) {
			componentToBeWatched.removeComponentListener(compListener);
			if(dockKey != null && !dockKey.equals("")) {
				desktop.removeDockingActionListener(dockListener);
			}
		}
	}

	/**
	 * notifies the {@link BubbleListener}s and disposes the Bubble-speech.
	 */
	public void triggerFire() {
		fireEventActionPerformed();
		dispose();
	}

	protected void fireEventCloseClicked() {
		LinkedList<BubbleListener> listenerList = new LinkedList<BubbleWindow.BubbleListener>(listeners);
		this.unregister();
		for (BubbleListener l : listenerList) {
			l.bubbleClosed(this);
		}
		unregisterMovementListener();
		
	}

	protected void fireEventActionPerformed() {
		LinkedList<BubbleListener> listenerList = new LinkedList<BubbleWindow.BubbleListener>(listeners);
		for (BubbleListener l : listenerList) {
			l.actionPerformed(this);
		}
		unregisterMovementListener();
		
	}

	private Alignment calculateAlignment(Alignment preferredAlignment, Component component) {
		if(Alignment.MIDDLE == preferredAlignment) {
			return preferredAlignment;
		}
		//get Mainframe location
		Point frameLocation = owner.getLocationOnScreen();
		double xlocFrame = Math.max(frameLocation.getX(), 0);
		double ylocFrame = Math.max(frameLocation.getY(), 0);
		
		//get Mainframe size
		int xframe = owner.getWidth();
		int yframe = owner.getHeight();
		
		//location of Component
		Point location = component.getLocationOnScreen();
		double xloc = location.getX();
		double yloc = location.getY();
		
		//size of Component
		int xSize = component.getWidth();
		int ySize = component.getHeight();
		
		//load height and with or the approximate Value of worstcase
		double xSizeBubble = this.getWidth();
		double ySizeBubble = this.getHeight();
		if(xSizeBubble == 0 || ySizeBubble == 0) {
			double approximateValue = (WINDOW_WIDTH + 2*CORNER_RADIUS);
			xSizeBubble = approximateValue;
			ySizeBubble = approximateValue;
		}
		// 0 = space above the component
		// 1 = space right of the component
		// 2 = space below the component
		// 3 = space left of the Component
		double space[] = new double[4];
		space[0] = (yloc - ylocFrame)/ySizeBubble;
		space[1] = ((xframe + xlocFrame)-(xloc+xSize))/xSizeBubble;
		space[2] = ((yframe + ylocFrame)-(yloc+ySize))/ySizeBubble;
		space[3] = (xloc - xlocFrame)/xSizeBubble;
		// check if the preferred Alignment is valid and take it if it is valid
		switch(preferredAlignment) {
			case TOPLEFT:
				if(space[2] > 1)
					return this.fineTuneAlignement(preferredAlignment, xframe, yframe, frameLocation, location, xSize, ySize);
				break;
			case TOPRIGHT:
				if(space[2] > 1)
					return this.fineTuneAlignement(preferredAlignment, xframe, yframe, frameLocation, location, xSize, ySize);
				break;
			case LEFTBOTTOM:
				if(space[1] > 1)
					return this.fineTuneAlignement(preferredAlignment, xframe, yframe, frameLocation, location, xSize, ySize);
				break;
			case LEFTTOP:
				if(space[1] > 1)
					return this.fineTuneAlignement(preferredAlignment, xframe, yframe, frameLocation, location, xSize, ySize);
				break;
			case RIGHTBOTTOM:
				if(space[3] > 1)
					return this.fineTuneAlignement(preferredAlignment, xframe, yframe, frameLocation, location, xSize, ySize);
				break;
			case RIGHTTOP:
				if(space[3] > 1)
					return this.fineTuneAlignement(preferredAlignment, xframe, yframe, frameLocation, location, xSize, ySize);
				break;
			case BOTTOMLEFT:
				if(space[0] > 1)
					return this.fineTuneAlignement(preferredAlignment, xframe, yframe, frameLocation, location, xSize, ySize);
				break;
			case BOTTOMRIGHT:
				if(space[0] > 1)
					return this.fineTuneAlignement(preferredAlignment, xframe, yframe, frameLocation, location, xSize, ySize);
				break;
			default:
		}
		//preferred Alignment was not valid. try to show bubble at the right side of the component
		if(space[1] > 1)
			return this.fineTuneAlignement(Alignment.LEFTTOP, xframe, yframe, frameLocation, location, xSize, ySize);
		// take the best fitting place
		int pointer=0;
		for (int i = 0; i < space.length; i++) {
			if(i==1)
				i++;
			if(space[i] > space[pointer])
				pointer = i;
		}
		if(space[pointer] > 1) {
			switch(pointer) {
				case 0:
					return this.fineTuneAlignement(Alignment.BOTTOMLEFT, xframe, yframe, frameLocation, location, xSize, ySize);
				case 2:
					return this.fineTuneAlignement(Alignment.TOPLEFT, xframe, yframe, frameLocation, location, xSize, ySize);
				case 3:
					return this.fineTuneAlignement(Alignment.RIGHTTOP, xframe, yframe, frameLocation, location, xSize, ySize);
				default:
					return null;
			}
		} else {
			//can not place Bubble outside of the component so we take the right side of the inner of the Component.
			return this.fineTuneAlignement(Alignment.INNERLEFT, xframe, yframe, frameLocation, location, xSize, ySize);
		}

	}
	
	/**
	 * Whether we want the north-, south, west- or eastside of the {@link Component} was 
	 * chosen before this method the decide in which direction the Bubble will expand 
	 * @param firstCompute first computed Alignment
	 * @param xframe width of the owner
	 * @param yframe height of the owner
	 * @param frameLocation location of the origin of the owner
	 * @param componentLocation location of the origin of the Component to attach to
	 * @param compWidth width of the component to attach to
	 * @param compHeight height of the component to attach to
	 * @return
	 */
	private Alignment fineTuneAlignement(Alignment firstCompute,int xframe, int yframe, Point frameLocation, Point componentLocation, int compWidth, int compHeight) {
		switch(firstCompute) {
			case TOPLEFT:
			case TOPRIGHT:
				if (((componentLocation.x - frameLocation.x) + (compWidth/2))> (xframe/2)) {
					return Alignment.TOPRIGHT;
				} else {
					return Alignment.TOPLEFT;
				}
			case LEFTBOTTOM:
			case LEFTTOP:
				if(((componentLocation.y - frameLocation.y) + (compHeight/2))> (yframe/2)) {
					return Alignment.LEFTBOTTOM;
				} else {
					return Alignment.LEFTTOP;
				}
			case RIGHTBOTTOM:
			case RIGHTTOP:
				if(((componentLocation.y - frameLocation.y) + (compHeight/2))> (yframe/2)) {
					return Alignment.RIGHTBOTTOM;
				} else {
					return Alignment.RIGHTTOP;
				}
			case BOTTOMLEFT:
			case BOTTOMRIGHT:
				if (((componentLocation.x - frameLocation.x) + (compWidth/2))> (xframe/2)) {
					return Alignment.BOTTOMRIGHT;
				} else {
					return Alignment.BOTTOMLEFT;
				}
			default:
				if((componentLocation.x - frameLocation.x) > ((xframe + frameLocation.x)-(compWidth + componentLocation.x))) {
					return Alignment.INNERRIGHT;
				} else {
					return Alignment.INNERLEFT;
				}
		}
		
	}
	
}
