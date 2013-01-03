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
package com.rapidminer.gui.tour;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;


/**
 * Can be started to ensure that a Button is not out of the boundaries of the Desktop.
 * 
 * @author Thilo Kamradt
 *
 */
public class NotWatchableStep extends Step {
	
	
	private Alignment alignment;
	private Window owner;
	private String i18nKey = "test";
	private String attachToKey = null;
	private Component attachTo = null;
	private String watch;
	private ComponentListener compListener = null;
	private boolean showMe;
	
	
	public NotWatchableStep(Alignment preferredAlignment, Window owner, String shouldBeViewable, Component attachTo) {
		this.owner = owner;
		this.alignment = preferredAlignment;
		this.watch = shouldBeViewable;
		this.attachTo = attachTo;
	}
	
	public NotWatchableStep(Alignment preferredAlignment, Window owner, String shouldBeViewable, String attachToKey) {
		this.owner = owner;
		this.alignment = preferredAlignment;
		this.watch = shouldBeViewable;
		this.attachToKey = attachToKey;
	}
	
	/* (non-Javadoc)
	 * @see com.rapidminer.gui.tour.Step#createBubble()
	 */
	@Override
	boolean createBubble() {
		this.showMe = BubbleWindow.isButtonOnScreen(watch) == 0;
		if(showMe) {
			if(attachTo == null)
				attachTo = BubbleWindow.getDockableByKey(attachToKey);
			bubble = new BubbleWindow(owner, alignment, i18nKey, attachTo);
			compListener = new ComponentListener() {
			
				@Override
				public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
				}
			
				@Override
				public void componentResized(ComponentEvent e) {
					if(BubbleWindow.isButtonOnScreen(watch) == 1) {
						bubble.triggerFire();
						attachTo.removeComponentListener(this);
					}
				}
			
				@Override
				public void componentMoved(ComponentEvent e) {
					// TODO Auto-generated method stub
				
				}
			
				@Override
				public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
				}
			};
			attachTo.addComponentListener(compListener);
		}
		return showMe;
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.gui.tour.Step#stepCanceled()
	 */
	@Override
	protected void stepCanceled() {
		if(compListener != null)
		attachTo.removeComponentListener(compListener);
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.gui.tour.Step#checkPreconditions()
	 */
	@Override
	public Step[] getPreconditions() {
		return new Step[] {};
	}

	
}
