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


import java.awt.Point;
import java.awt.Window;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.flow.ProcessPanel;
import com.rapidminer.gui.flow.ProcessRenderer;
import com.rapidminer.gui.tour.ProcessRendererListener;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;

/**
 * This class creates a speech bubble-shaped JDialog, which can be attache to
 * Buttons, either by using its ID or by passing a reference. 
 * The bubble triggers two events which are obserable by the {@link BubbleListener};
 * either if the close button was clicked, or if the corresponding button was used.
 * The keys for the title and the text must be of format gui.bubble.XXX.body or gui.bubble.XXX.title .
 * 
 * @author Thilo Kamradt
 *
 */

public class BubbleToOperator extends BubbleWindow {

	private static final long serialVersionUID = 7404582361212798730L;

	private int split;

	private Class<? extends Operator> operatorClass;
	private Operator onDisplay;
	private ProcessRenderer renderer = RapidMinerGUI.getMainFrame().getProcessPanel().getProcessRenderer();
	private ProcessRendererListener rendererListener;
	
	public BubbleToOperator(Window owner, final Alignment preferredAlignment, String i18nKey, Class<? extends Operator> toAttach, Object ... arguments) {
		this(owner, preferredAlignment, i18nKey, toAttach, 1, arguments);
	}
	
	public BubbleToOperator(Window owner, final Alignment preferredAlignment, String i18nKey, Class<? extends Operator> toAttach, int split, Object ... arguments) {
		super(owner, preferredAlignment, i18nKey, ProcessPanel.PROCESS_PANEL_DOCK_KEY, arguments);
		operatorClass = toAttach;
		this.split = split;
		Operator[] matchingOperators = this.getMatchingOperatorsInChain(operatorClass, renderer.getDisplayedChain());
		if(matchingOperators.length == 0) {
			//TODO Add preSteps to Enter
		} else if(matchingOperators.length <= split) {
			onDisplay = matchingOperators[split - 1];
		} else {
			onDisplay = matchingOperators[matchingOperators.length - 1];
		}
		super.buildBubble();
	}
	
	private Operator[] getMatchingOperatorsInChain(Class<? extends Operator> operator,OperatorChain displayedChain) {
		ArrayList<Operator> opAL = new ArrayList<Operator>();
		List<Operator> operatorsInChain = displayedChain.getAllInnerOperators();
		for(Operator operatorInChain : operatorsInChain) {
			if(operator.isAssignableFrom(operatorInChain.getClass())) {
				opAL.add(operatorInChain);
			}
		}
		return opAL.toArray(new Operator[0]);
	}
	
	
	@Override
	protected void registerMovementListener() {
		super.registerMovementListener();
		this.registerSpecificListener();
	}

	@Override
	protected void registerSpecificListener() {
		super.registerSpecificListener();
		rendererListener = new ProcessRendererListener() {
			
			@Override
			public void newChainShowed(OperatorChain displayedChain) {
				// TODO Hide the Bubble but let it appear again if the displayed Chain is the same as the one by the start
				
			}

			@Override
			public void repainted() {
				BubbleToOperator.this.paintAgain(false);
				
			}
		};
		renderer.addProcessRendererListener(rendererListener);
	}
	
	@Override
	protected void unregisterMovementListener() {
		super.unregisterMovementListener();
		this.unregisterSpecificListeners();
	}

	@Override
	protected void unregisterSpecificListeners() {
		super.unregisterSpecificListeners();
		renderer.removeProcessRendererListener(rendererListener);
	}

	@Override
	protected Point getObjectLocation() {
		int xDockable = dockable.getLocationOnScreen().x;
		int yDockable = dockable.getLocationOnScreen().y;
		Rectangle2D rec = renderer.getOperatorRect(onDisplay, false);
		double width = rec.getWidth();
		double height = rec.getHeight();
		double xOperator = rec.getMinX();
		double yOperator = rec.getMinY();
		Point view = RapidMinerGUI.getMainFrame().getProcessPanel().getViewPort().getViewPosition();
		renderer.scrollRectToVisible(rec.getBounds());
		return new Point((int) (xDockable + (width*0.3) +xOperator - view.x),(int) (yDockable + height*0.85 + yOperator - view.y));
	}
	
	@Override
	protected int getObjectWidth() {
		return (int) Math.round(renderer.getOperatorRect(onDisplay, false).getWidth());
	}

	@Override
	protected int getObjectHeight() {
		return (int) Math.round(renderer.getOperatorRect(onDisplay, false).getHeight());
	}

	@Override
	protected void reloadComponent() {
		dockable = BubbleWindow.getDockableByKey(docKey);
		Operator[] matchingOperators = this.getMatchingOperatorsInChain(operatorClass, renderer.getDisplayedChain());
		if(matchingOperators.length == 0) {
			//TODO Add preSteps to Enter
		} else if(matchingOperators.length <= split) {
			onDisplay = matchingOperators[split - 1];
		} else {
			onDisplay = matchingOperators[matchingOperators.length - 1];
		}
	}

}

