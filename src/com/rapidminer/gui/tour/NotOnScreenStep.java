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

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;


/**
 * 
 * 
 * @author Thilo Kamradt
 *
 */
public class NotOnScreenStep extends Step {

	private boolean showMe = false;
	private Window owner = RapidMinerGUI.getMainFrame();
	private String dockableKey;
	private String i18nKey;
	
	public NotOnScreenStep(String i18nMessageKey, String dockableKey) {
		this.i18nKey = i18nMessageKey;
		this.dockableKey = dockableKey;
	}
	
	/* (non-Javadoc)
	 * @see com.rapidminer.gui.tour.Step#createBubble()
	 */
	@Override
	boolean createBubble() {
		this.showMe = BubbleWindow.isDockableOnScreen(dockableKey) == -1;
		// TODO: delete
		showMe = false;
		if(showMe)
			bubble = new BubbleWindow(owner, Alignment.MIDDLE, i18nKey, (Component) null);
			//TODO: get dockableMenu
			//TODO: listener adden and compare with the given dockableKey
		return showMe;
	}

	/* (non-Javadoc)
	 * @see com.rapidminer.gui.tour.Step#stepCanceled()
	 */
	@Override
	protected void stepCanceled() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.rapidminer.gui.tour.Step#checkPreconditions()
	 */
	@Override
	public Step[] getPreconditions() {
		return new Step[] {};
	}

}
