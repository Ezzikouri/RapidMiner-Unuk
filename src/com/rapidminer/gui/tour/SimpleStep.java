/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
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

import java.awt.Window;

import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.components.BubbleWindow.Alignment;

/**
 * This Subclass of {@link Step} will open a {@link BubbleWindow} which closes if the given Button was pressed.
 * 
 * @author Philipp Kersting
 *
 */

public class SimpleStep extends Step {

	private String buttonKey;
	private String i18nKey;
	private Alignment alignment;
	private Window owner;
	
	/**
	 * @param preferedAlignment offer for alignment but the Class will calculate by itself whether the position is usable.
	 * @param owner the {@link Window} on which the {@link BubbleWindow} should be shown.
	 * @param i18nKey of the message which will be shown in the {@link BubbleWindow}.
	 * @param buttonKey i18nKey of the Button to which the {@link Step} listens and the {@link BubbleWindow} will point to.
	 */
	public SimpleStep(Alignment preferedAlignment, Window owner, String i18nKey, String buttonKey){
		this.alignment = preferedAlignment;
		this.owner = owner;
		this.i18nKey = i18nKey;
		this.buttonKey = buttonKey;
	}
	
	@Override
	boolean createBubble() {
		bubble = new BubbleWindow(owner, alignment, i18nKey, buttonKey, true);
		return true;
	}

	@Override
	protected void stepCanceled() {
		// the BubbleWindow will do everything what is necessary
	}

	@Override
	public Step[] getPreconditions() {
		return new Step[] {};
	}

}
