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

import com.rapidminer.gui.tools.components.BubbleWindow;
import com.rapidminer.gui.tools.dialogs.MessageDialog;
import com.rapidminer.tools.I18N;


/**
 * @author Thilo Kamradt
 *
 */
public class FinalStep extends Step {

	protected String title, text;
	
	public FinalStep(String i18nKey, String explicitTour) {
		this.title =I18N.getMessage(I18N.getGUIBundle(),"gui.bubble." + i18nKey + ".title");
		this.text = I18N.getMessage(I18N.getGUIBundle(),"gui.bubble." + i18nKey + ".body",explicitTour);
	}
	/* (non-Javadoc)
	 * @see com.rapidminer.gui.tour.Step#createBubble()
	 */
	@Override
	BubbleWindow createBubble() {
		return null;
	}

	@Override
	public void start() {
		MessageDialog tourComplete = new MessageDialog(title, text);
		tourComplete.setVisible(true);
	}
	
	@Override
	protected void stepCanceled() {
		
	}
}
